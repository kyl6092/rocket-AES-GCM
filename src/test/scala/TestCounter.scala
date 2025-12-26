import counter_blackbox._
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
class TestCounter extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "counter_blackbox"
  it should "do cheking output value" in {
    test(new MyCounter(4)).withAnnotations(Seq(VerilatorBackendAnnotation)) { c =>
      c.io.rst.poke(true.B)
      c.clock.step()
      c.io.out.expect(0.U)

      c.io.rst.poke(false.B)
      c.clock.step()
      c.io.out.expect(0.U)

      c.io.en.poke(true.B)
      for (i <- 0 until 15) {
        c.clock.step()
        // println(c.io.out.peek().litValue)
        c.io.out.expect((i+1).U)
      }
      c.clock.step()
      // println(c.io.out.peek().litValue)
      c.io.out.expect(0.U)
    }

  }
}