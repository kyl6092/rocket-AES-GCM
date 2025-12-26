import hello._
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
class TestHello extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "Hello"
  it should "do cheking output value" in {
    test(new Hello) { c =>
        c.io.out.expect(42.U)
    }
  }
}