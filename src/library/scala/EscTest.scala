package scala

object BugImplicitLocalDefaultDisallows2ndClassFn {
  trait NonPar
  @local val nonPar = new NonPar {}

  def ap[A, U](f: A => U)(implicit @local np: NonPar) {}
  def apDflt[A, U](f: A => U)(implicit @local np: NonPar = nonPar) {}
  def ap2[A, U](@local f: A => U)(implicit @local np: NonPar) {}
  def ap2Dflt[A, U](@local f: A => U)(implicit @local np: NonPar = nonPar) {}
  def mkEfapct(@local ct: CanThrow) {}

  def sameCap(@local c: CanThrow) {
    // apDflt((x: Int) => mkEfapct(c))  // ok, must be 1st class
    // ap2Dflt((x: Int) => mkEfapct(c)) // FIXME: works in noCap and below

    implicit val ic = new NonPar {}
    // ap((x: Int) => mkEfapct(c))  // ok, must be 1st class
    ap2((x: Int) => mkEfapct(c))     // ok, 2nd class allowed
  }

  def noCap() {
    apDflt((x: Int) => x + 1) // ok, 1st class and using default implicit arg
    ap2Dflt((x: Int) => x + 1) // ok, 1st class and using default implicit arg

    implicit val ic = new NonPar {}
    ap((x: Int) => ESC.TRY { c => mkEfapct(c) }) // ok, 1st class
    ap2((x: Int) => ESC.TRY { c => mkEfapct(c) }) // ok, 1st class
    ap((x: Int) => x + 1) // ok, 1st class
    ap2((x: Int) => x + 1) // ok, 1st class
  }
}

object MotivatingExamples {
  case class File(name: String) {
    def close() = ???
  }

  def withCan[R](body: =>R): R = body

  object AutoResourceManagement {
    def withFile[R](n: String)(@local fun: File -> R): R = {
      val f = new File(n); val ret = fun(f); f.close(); ret
    }

    def closure = {
      @local var f2: File = null
      @local val f0 = withFile("foo.out") { f =>
        // f2 = f
        // f
        withFile("bar") { f1 =>
          // f2 = f1
          ()
        }
      }
    }

    object Implicits {
      @local val f: File = null
    }

    def imp = {
      @local var f2: File = null
      @local val f0 = withCan {
        // import Implicits._
        @local val f: File = null
        // f2 = f
        // f
      }
    }
  }

  object HigherOrderFuns {
    def doLazily[A](fn: A => Unit) {}
    def doEagerly[A](@local fn: A => Unit) {}
    def mkEffect(implicit @local cap: File) {}

    def secondClassOuterScope = {
      @local val f: File = null // putting it out eliminates most errors below?

      // doLazily((u: Unit) => f.close()) // error (unless f is out of method)
      doEagerly((u: Unit) => f.close())

      // doLazily((u: Unit) => mkEffect(f)) // error (unless f is out of method)
      doEagerly((u: Unit) => mkEffect(f))

      @local implicit val f0 = f
      // doLazily((u: Unit) => mkEffect) // error
      doEagerly((u: Unit) => mkEffect)

      def mkEffect1(x: Int)(implicit @local cap: File = null) {}
      // doLazily(mkEffect1) // error
      doEagerly(mkEffect1)

      // {
      //   @local // error? (unless f is out of method)
      //   def mkEffect2(x: Int)(implicit @local cap: File = f) {}
      //   // doLazily(mkEffect2) // error
      //   doEagerly(mkEffect2)
      // }
      {
        @local def mkEffect2(x: Int)(implicit @local cap: File) {} // ok
        // doLazily(mkEffect2) // error
        doEagerly(mkEffect2)
      }
    }

    def firstClass = {
      doLazily((f: File) => mkEffect(f)) // ok (note there is no @local)
      doEagerly((f: File) => mkEffect(f))

      doLazily { (u: Unit) =>
        @local val f: File = null
        mkEffect(f)
      }

      doLazily { (u: Unit) =>
        @local implicit val f: File = null
        mkEffect
      }
    }
  }
}
