import AES_base._
import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.funspec.AnyFunSpec


class TestAES_base extends AnyFunSpec with ChiselSim {
  val key_filename = "key.bin"
  val input_filename = "Input.bin"
  val key_fptr = getClass.getClassLoader.getResourceAsStream(key_filename)
  val input_fptr = getClass.getClassLoader.getResourceAsStream(input_filename)

  val ADDR_CTRL   = 8
  val ADDR_STATUS = 9
  val ADDR_CONFIG = 10
  val ADDR_KEY0   = 16
  val ADDR_BLOCK0 = 32
  
  
  describe ("TestAES_base") {
    it ("do checking status") {
      simulate(new AESbase_Wrapper) { c=>
        c.io.rst_n.poke(false.B)
        c.clock.step()
        c.clock.step()
        c.io.rst_n.poke(true.B)
        c.io.cs.poke(true.B)
        c.io.we.poke(true.B)

        
        val key_buffer = new Array[Byte](16)
        val input_buffer = new Array[Byte](16)
        val key_len = key_fptr.read(key_buffer)
        val input_len = input_fptr.read(input_buffer)
        key_fptr.close()
        input_fptr.close()


        // c.io.address.poke()

        for (i <- 0 until key_len/4) {
          c.io.address.poke(ADDR_KEY0+i)
          val byte1 = key_buffer(4*i) & 0xff
          val byte2 = key_buffer(4*i+1) & 0xff
          val byte3 = key_buffer(4*i+2) & 0xff
          val byte4 = key_buffer(4*i+3) & 0xff
          var tmp = (byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4)
          c.io.write_data.poke(tmp)
          c.clock.step()
        }

        for (i <- 0 until input_len/4) {
          c.io.address.poke(ADDR_BLOCK0+i)
          val byte1 = input_buffer(4*i) & 0xff
          val byte2 = input_buffer(4*i+1) & 0xff
          val byte3 = input_buffer(4*i+2) & 0xff
          val byte4 = input_buffer(4*i+3) & 0xff
          var tmp = (byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4)
          c.io.write_data.poke(tmp)
          c.clock.step()
        }

        c.clock.step(100)
      }
    }
  }
}