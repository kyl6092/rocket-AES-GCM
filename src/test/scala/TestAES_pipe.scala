import AES_pipe._
import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.funspec.AnyFunSpec


class TestAES_pipe_encipher extends AnyFunSpec with ChiselSim {
  // I/O related
  val key_filename = "key.bin"
  val input_filename = "Input.bin"
  val cipher_filename = "cipher.bin"
  val decipher_filename = "decipher.bin"
  val key_fptr = getClass.getClassLoader.getResourceAsStream(key_filename)
  val input_fptr = getClass.getClassLoader.getResourceAsStream(input_filename)
  val cipher_fptr = getClass.getClassLoader.getResourceAsStream(cipher_filename)
  val decipher_fptr = getClass.getClassLoader.getResourceAsStream(decipher_filename)
  var total = 0
  var processed = 0
  val verbose = 0
  val endcycle = 100000000

  // Control related
  val ADDR_CTRL   = 8
//   val ADDR_STATUS = 9
  val ADDR_CONFIG = 10
  val ADDR_KEY0   = 16
  val ENC = 1
  val DEC = 0
  val AES128 = 0
  val AES192 = 1
  val AES256 = 2
//   val ADDR_RESULT0 = 48

  // Testcase settings
  val KEYLEN = 8
  val SIZE_OF_DATA = 16

  describe ("TestAES_pipe encipher") {
    it ("do checking waveform") {
      simulate(new AESpipe_Wrapper) { c=>
        // System Reset
        c.io.rst_n.poke(false.B)
        c.clock.step()
        c.clock.step()
        c.io.rst_n.poke(true.B)
        c.io.chip_en.poke(true.B)
        c.io.we.poke(true.B)

        // TestCase Preparation
        val key_buffer = new Array[Byte](KEYLEN*4)
        val input_buffer = new Array[Byte](16)
        val cipher_buffer = new Array[Byte](16)
        val key_len = key_fptr.read(key_buffer)
        var input_len = 0
        var cipher_len = 0

        // System Config
        val CFG = ((ENC<<4) | (AES256))&0x1f
        c.io.address.poke(ADDR_CONFIG)
        c.io.datain.poke(CFG)
        c.clock.step()

        // Monitor variable
        var cycle = 0
        var iter = 0

        // Key Data Transferring
        for (i <- 0 until KEYLEN) {
          c.io.address.poke(ADDR_KEY0+i)
          val byte1 = key_buffer(4*i) & 0xff
          val byte2 = key_buffer(4*i+1) & 0xff
          val byte3 = key_buffer(4*i+2) & 0xff
          val byte4 = key_buffer(4*i+3) & 0xff
          var tmp = (byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4)
          c.io.datain.poke(tmp)
          c.clock.step()
          cycle+=1
        }

        c.io.address.poke(ADDR_CTRL)
        c.io.datain.poke(1)
        c.clock.step(50)



        
        
        // Report Clock Cycle
        print("Cycles = "+cycle.toString+"\n\n")

        // I/O related
        key_fptr.close()
        input_fptr.close()
        cipher_fptr.close()
        decipher_fptr.close()
      }
    }
  }
}