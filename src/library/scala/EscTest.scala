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
