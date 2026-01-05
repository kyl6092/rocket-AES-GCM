import AES_base._
import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.funspec.AnyFunSpec


class TestAES_base extends AnyFunSpec with ChiselSim {
  val key_filename = "key.bin"
  val input_filename = "Input.bin"
  val cipher_filename = "cipher.bin"
  val key_fptr = getClass.getClassLoader.getResourceAsStream(key_filename)
  val input_fptr = getClass.getClassLoader.getResourceAsStream(input_filename)
  val cipher_fptr = getClass.getClassLoader.getResourceAsStream(cipher_filename)

  val ADDR_CTRL   = 8
  val ADDR_STATUS = 9
  val ADDR_CONFIG = 10
  val ADDR_KEY0   = 16
  val ADDR_BLOCK0 = 32
  val ENC = 1
  val DEC = 0
  val AES128 = 0
  val AES256 = 1
  val ADDR_RESULT0 = 48


  val CFG = ((AES128) | (ENC))&0x3

  describe ("TestAES_base") {
    it ("do checking status") {
      simulate(new AESbase_Wrapper) { c=>

        // System Reset
        c.io.rst_n.poke(false.B)
        c.clock.step()
        c.clock.step()
        c.io.rst_n.poke(true.B)
        c.io.cs.poke(true.B)
        c.io.we.poke(true.B)

        // TestCase Preparation
        val key_buffer = new Array[Byte](16)
        val input_buffer = new Array[Byte](16)
        val cipher_buffer = new Array[Byte](16)
        val key_len = key_fptr.read(key_buffer)
        val input_len = input_fptr.read(input_buffer)
        val cipher_len = cipher_fptr.read(cipher_buffer)
        key_fptr.close()
        input_fptr.close()

        // System Config
        c.io.address.poke(ADDR_CONFIG)
        c.io.write_data.poke(CFG)
        c.clock.step()

        // Monitor variable
        var cycle = 0
        var iter = 0

        // Key Data Transferring
        for (i <- 0 until key_len/4) {
          c.io.address.poke(ADDR_KEY0+i)
          val byte1 = key_buffer(4*i) & 0xff
          val byte2 = key_buffer(4*i+1) & 0xff
          val byte3 = key_buffer(4*i+2) & 0xff
          val byte4 = key_buffer(4*i+3) & 0xff
          var tmp = (byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4)
          c.io.write_data.poke(tmp)
          c.clock.step()
          cycle+=1
        }

        // Block Data Transferring
        for (i <- 0 until input_len/4) {
          c.io.address.poke(ADDR_BLOCK0+i)
          val byte1 = input_buffer(4*i) & 0xff
          val byte2 = input_buffer(4*i+1) & 0xff
          val byte3 = input_buffer(4*i+2) & 0xff
          val byte4 = input_buffer(4*i+3) & 0xff
          var tmp = (byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4) & 0xFFFFFFFFL
          println("Plain Text: "+tmp.toHexString)
          c.io.write_data.poke(tmp)
          c.clock.step()
          cycle+=1
        }
        println("")

        // System Control init (the design is iterative)
        c.io.address.poke(ADDR_CTRL)
        c.io.write_data.poke(1.U)
        c.clock.step(2)
        c.io.write_data.poke(0.U)
        c.io.we.poke(false.B)
        c.io.address.poke(ADDR_STATUS)
        c.clock.step()
        cycle+=3
        while (c.io.read_data.peek().litValue != 1 && iter < 100) {
          c.clock.step()
          iter+=1
          cycle+=1
        }

        // System Control next (the design is iterative)
        c.io.we.poke(true.B)
        c.io.address.poke(ADDR_CTRL)
        c.io.write_data.poke(2.U)
        c.clock.step(2)
        c.io.we.poke(false.B)
        c.io.address.poke(ADDR_STATUS)
        c.clock.step()
        cycle+=3
        while ((c.io.read_data.peek().litValue & 0x2) != 2 && iter < 100) {
          c.clock.step()
          iter+=1
          cycle+=1
        }

        // Comparison with Golden data (cipher)
        for (i <- 0 until cipher_len/4) {
          val byte1 = cipher_buffer(4*i) & 0xff
          val byte2 = cipher_buffer(4*i+1) & 0xff
          val byte3 = cipher_buffer(4*i+2) & 0xff
          val byte4 = cipher_buffer(4*i+3) & 0xff
          var tmp = (byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4) & 0xFFFFFFFFL
          println("Cipher: "+tmp.toHexString)
          c.io.address.poke(ADDR_RESULT0+i)
          c.io.read_data.expect(tmp.U(32.W))
          
        }
        println("")

        // Report Clock Cycle
        print("Cycles = "+cycle.toString+"\n")
      }
    }
  }
}