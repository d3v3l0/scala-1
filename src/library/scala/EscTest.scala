package scala

object BugImplicitLocalDefaultDisallows2ndClassFn {
  import ESC._
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
    ap((x: Int) => TRY { c => mkEfapct(c) }) // ok, 1st class
    ap2((x: Int) => TRY { c => mkEfapct(c) }) // ok, 1st class
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

// Note: must be top-level, so compiler phase can detect it by prefix "->"
trait `->*`[P, -A,+B] extends Function1[A,B] {
  def apply(@local[P] y: A): B
}

object Finer2ndClassValues {
  object SecretCannotLeak {
    // Secret can simply be the only level of 2nd-class
    // type Secret = Any
    // type Public = Nothing

    // Nevertheless, let's make both 2nd-class values...
    trait Secret extends Any
    trait Public extends Secret

    class File {
      def read() = ???  // FIXME: cannot return 2nd-class value: @local[Public]
      def write[T](@local[Public] obj: T) {}
    }
    val file = new File

    def exposeSecret[U](
      fn: ->*[Public, String, U]
    ) = fn("password")

    def testExpose() {
      exposeSecret { secret =>
        file.read          // ok
        // file.write(secret) // error
        () // fool-proof return
      }
    }

    // This probably makes more sense, as secrets don't come out of thin air :)
    def protectSecret[T, U](@local[Secret] obj: T)(
      @local[Public] fn: `->*`[Secret, T, U]) = fn(obj)

    def testProtect() {
      @local[Secret] val outerSecret = "password"

      // user code that attempting to store a secret
      @local[Public] val leakChannel = new {
        def leak(@local[Secret] secret: Any) {}
      }

      // leakChannel.leak(s)  // FIXME: should type-check, but compiler crashes

      protectSecret("anotherPassword") { secret =>
        file.read          // ok
        // file.write(secret) // error
        leakChannel // Note: this should fail, as we could call leak()
                    //       Unfortunatly, putting @local[Secret] on fn
                    //       would have the opposite effect; that is,
                    //       no access to outer secrets would be possible
                    // So, we see that hierarchy should be backward for reads,
                    // but then, we would have the problem with writing.
        outerSecret // FIXME: compiler phase bug? (free var: @local[>:Public])
        // leakChannel.leak(secret)  // FIXME: fails becase of a wrong reason:
                                  // value secret @local[Any] cannot be used
                                  // as 1st class value
                                  // @local[...SecretCannotLeak.Secret]
                                     // OR crashes if Secret = Any
        () // fool-proof return
      }

      // Another way of achieving the above (buggy) behavior
      @local[Public] val sUnprotected = "semi-secret";
      {  // but we can simply up-cast to pretect it against file write
        @local[Secret] val sProtected = sUnprotected
        // file.write(sProtected) // error
      }
    }
  }
}
