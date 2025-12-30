import hello._
import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.funspec.AnyFunSpec

class TestHello extends AnyFunSpec with ChiselSim {
  describe ("Hello") {
    it ("do cheking output value") {
      simulate(new Hello) { c =>
          c.io.out.expect(42.U)
      }
    }
  }
}