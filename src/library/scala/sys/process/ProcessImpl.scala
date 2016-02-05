/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package sys
package process

import processInternal._
import java.io.{ PipedInputStream, PipedOutputStream }

private[process] trait ProcessImpl {
  self: Process.type =>

  /** Runs provided code in a new Thread and returns the Thread instance. */
  private[process] object Spawn {
    def apply(f: => Unit): Thread = apply(f, daemon = false)
    def apply(f: => Unit, daemon: Boolean): Thread = {
      val thread = new Thread() { override def run() = { f } }
      thread.setDaemon(daemon)
      thread.start()
      thread
    }
  }
  private[process] object Future {
    def apply[T](f: => T): CanThrow -> T = {
      val result = new SyncVar[Either[Throwable, T]]
      def run(): Unit =
        try result set Right(f)
        catch { case e: Exception => result set Left(e) }

      Spawn(run())

      cc => result.get(cc) match {
        case Right(value)    => value
        case Left(exception) => ESC.THROW { throw exception }(cc)
      }
    }
  }

  private[process] class AndProcess(
    a: ProcessBuilder,
    b: ProcessBuilder,
    io: ProcessIO
  ) extends SequentialProcess(a, b, io, _ == 0)

  private[process] class OrProcess(
    a: ProcessBuilder,
    b: ProcessBuilder,
    io: ProcessIO
  ) extends SequentialProcess(a, b, io, _ != 0)

  private[process] class ProcessSequence(
    a: ProcessBuilder,
    b: ProcessBuilder,
    io: ProcessIO
  ) extends SequentialProcess(a, b, io, _ => true)

  private[process] class SequentialProcess(
    a: ProcessBuilder,
    b: ProcessBuilder,
    io: ProcessIO,
    evaluateSecondProcess: Int => Boolean
  ) extends CompoundProcess {

    protected[this] override def runAndExitValue()(@local cc: CanThrow) = {
      val first = a.run(io)(cc)
      runInterruptible(first.exitValue()(cc))(first.destroy()(cc))(cc) flatMap { codeA =>
        if (evaluateSecondProcess(codeA)) {
          val second = b.run(io)(cc)
          runInterruptible(second.exitValue()(cc))(second.destroy()(cc))(cc)
        }
        else Some(codeA)
      }
    }
  }

  private[process] abstract class BasicProcess extends Process {
    def start(): Unit
  }

  private[process] abstract class CompoundProcess extends BasicProcess {
    def destroy()(@local cc: CanThrow)   = destroyer()
    def exitValue()(@local cc: CanThrow) = getExitValue(cc) getOrElse scala.sys.error("No exit code: process destroyed.")
    def start()     = () => ESC.TRY(getExitValue) // TODO(leo) not sure this is right (initial type: () => T)

    protected lazy val (getExitValue, destroyer) = {
      val code = new SyncVar[Option[Int]]()
      code set None
      val thread = Spawn(code set (ESC.TRY { cc => runAndExitValue()(cc) })) // TODO(leo)

      (
        Future { ESC.NO(thread.join()); ESC.TRY(code.get) }, // TODO(leo) use "CanThrow ->" instead of by-value arg in Future?
        () => thread.interrupt()
      )
    }

    /** Start and block until the exit value is available and then return it in Some.  Return None if destroyed (use 'run')*/
    protected[this] def runAndExitValue()(@local cc: CanThrow): Option[Int]

    protected[this] def runInterruptible[T](action: => T)(destroyImpl: => Unit)(@local cc: CanThrow): Option[T] = ESC.THROW {
      try   Some(action)
      catch onInterrupt { destroyImpl; None }
    }(cc)
  }

  private[process] class PipedProcesses(a: ProcessBuilder, b: ProcessBuilder, defaultIO: ProcessIO, toError: Boolean) extends CompoundProcess {
    protected[this] override def runAndExitValue()(@local cc: CanThrow) = {
      val currentSource = new SyncVar[Option[InputStream]]
      val pipeOut       = new PipedOutputStream
      val source        = new PipeSource(currentSource, pipeOut, a.toString)
      source.start()

      val pipeIn      = ESC.THROW(new PipedInputStream(pipeOut))(cc)
      val currentSink = new SyncVar[Option[OutputStream]]
      val sink        = new PipeSink(pipeIn, currentSink, b.toString)
      sink.start()

      def handleOutOrError(fromOutput: InputStream): CanThrow -> Unit = currentSource.put(Some(fromOutput))(_)

      val firstIO =
        if (toError)
          defaultIO.withError(handleOutOrError)
        else
          defaultIO.withOutput(handleOutOrError)
      val secondIO = defaultIO.withInput(toInput => currentSink.put(Some(toInput)))

      val second = b.run(secondIO)(cc)
      val first = a.run(firstIO)(cc)
      try {
        runInterruptible {
          val exit1 = first.exitValue()(cc)
          currentSource.put(None)(cc)
          currentSink.put(None)(cc)
          val exit2 = second.exitValue()(cc)
          // Since file redirection (e.g. #>) is implemented as a piped process,
          // we ignore its exit value so cmd #> file doesn't always return 0.
          if (b.hasExitValue) exit2 else exit1
        } {
          first.destroy()(cc)
          second.destroy()(cc)
        }(cc)
      }
      finally {
        BasicIO close pipeIn
        BasicIO close pipeOut
      }
    }
  }

  private[process] abstract class PipeThread(isSink: Boolean, labelFn: () => String) extends Thread {
    def run()(@local cc: CanThrow): Unit

    private[process] def runloop(src: InputStream, dst: OutputStream)(@local cc: CanThrow): Unit = ESC.THROW {
      try     BasicIO.transferFully(src, dst)(cc)
      catch   ioFailure(ioHandler)
      finally BasicIO close {
        if (isSink) dst else src
      }
    }(cc)
    private def ioHandler(e: IOException) {
      println("I/O error " + e.getMessage + " for process: " + labelFn())
      e.printStackTrace()
    }
  }

  private[process] class PipeSource(
    currentSource: SyncVar[Option[InputStream]],
    pipe: PipedOutputStream,
    label: => String
  ) extends PipeThread(false, () => label) {

    final override def run()(@local cc: CanThrow): Unit = currentSource.get(cc) match {
      case Some(source) =>
        try runloop(source, pipe)(cc)
        finally currentSource.unset()

        run()(cc)
      case None =>
        currentSource.unset()
        BasicIO close pipe
    }
  }
  private[process] class PipeSink(
    pipe: PipedInputStream,
    currentSink: SyncVar[Option[OutputStream]],
    label: => String
  ) extends PipeThread(true, () => label) {

    final override def run()(@local cc: CanThrow): Unit = currentSink.get(cc) match {
      case Some(sink) =>
        try runloop(pipe, sink)(cc)
        finally currentSink.unset()

        run()(cc)
      case None =>
        currentSink.unset()
    }
  }

  /** A thin wrapper around a java.lang.Process.  `ioThreads` are the Threads created to do I/O.
  * The implementation of `exitValue` waits until these threads die before returning. */
  private[process] class DummyProcess(action: => Int) extends Process {
    private[this] val exitCode = Future(action)
    override def exitValue()(@local cc: CanThrow) = exitCode(cc)
    override def destroy()(@local cc: CanThrow) { }
  }
  /** A thin wrapper around a java.lang.Process.  `outputThreads` are the Threads created to read from the
  * output and error streams of the process.  `inputThread` is the Thread created to write to the input stream of
  * the process.
  * The implementation of `exitValue` interrupts `inputThread` and then waits until all I/O threads die before
  * returning. */
  private[process] class SimpleProcess(p: JProcess, inputThread: Thread, outputThreads: List[Thread]) extends Process {
    override def exitValue()(@local cc: CanThrow) = ESC.THROW {
      try p.waitFor()                   // wait for the process to terminate
      finally inputThread.interrupt()   // we interrupt the input thread to notify it that it can terminate
      outputThreads foreach (_.join())  // this ensures that all output is complete before returning (waitFor does not ensure this)

      p.exitValue()
    }(cc)
    override def destroy()(@local cc: CanThrow) = ESC.THROW {
      try {
        outputThreads foreach (_.interrupt()) // on destroy, don't bother consuming any more output
        p.destroy()
      }
      finally inputThread.interrupt()
    }(cc)
  }
  private[process] final class ThreadProcess(thread: Thread, success: SyncVar[Boolean]) extends Process {
    override def exitValue()(@local cc: CanThrow) = {
      ESC.THROW { thread.join() }(cc)
      if (success.get(cc)) 0 else 1
    }
    override def destroy()(@local cc: CanThrow) { thread.interrupt() }
  }
}
