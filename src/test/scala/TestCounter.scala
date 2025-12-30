import counter_blackbox._
import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.funspec.AnyFunSpec

class TestCounter extends AnyFunSpec with ChiselSim {
  var aligner = 1
  describe ("TestCounter") {
  it ("do checking ouput value") {
    simulate(new MyCounter(6)) { c=>
      c.io.rst.poke(true.B)
      c.clock.step()
      c.io.out.expect(0.U)

      c.io.rst.poke(false.B)
      c.clock.step()
      c.io.out.expect(0.U)

      c.io.en.poke(true.B)
      // c.clock.stepUntil(c.io.out, 8, 100)
      // println(c.io.out.peek().litValue)
      for (i <- 0 until 63) {
        c.clock.step()
        if (aligner % 8 === 0)
          println(c.io.out.peek().litValue)
        else
          print(c.io.out.peek().litValue+" ")
        aligner+=1
        c.io.out.expect((i+1).U)
        
      }
      c.clock.step()
      println(c.io.out.peek().litValue)
      c.io.out.expect(0.U)
    }
  }
  }
}