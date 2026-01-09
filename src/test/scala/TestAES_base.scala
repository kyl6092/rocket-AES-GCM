import AES_base._
import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.funspec.AnyFunSpec


class TestAES_base_encipher extends AnyFunSpec with ChiselSim {
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
  val ADDR_STATUS = 9
  val ADDR_CONFIG = 10
  val ADDR_KEY0   = 16
  val ADDR_BLOCK0 = 32
  val ENC = 1
  val DEC = 0
  val AES128 = 0
  val AES256 = 1
  val ADDR_RESULT0 = 48

  // Testcase settings
  val SIZE_OF_DATA = 1024*1024

  describe ("TestAES_base encipher") {
    it ("do checking encipher") {
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
        var input_len = 0
        var cipher_len = 0

        // System Config
        val CFG = ((AES128) | (ENC))&0x3
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
        while (processed < SIZE_OF_DATA) {
          // Ensure to read a complete block
          c.io.we.poke(true.B)
          while (total < 16) {
            input_len = input_fptr.read(input_buffer, total, 16-total)
            total+=input_len
          }
          processed+=16
          total = 0

          // Transferring data to AES core
          for (i <- 0 until 4) {
            c.io.address.poke(ADDR_BLOCK0+i)
            val byte1 = input_buffer(4*i) & 0xff
            val byte2 = input_buffer(4*i+1) & 0xff
            val byte3 = input_buffer(4*i+2) & 0xff
            val byte4 = input_buffer(4*i+3) & 0xff
            var tmp = (byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4) & 0xFFFFFFFFL
            if (verbose == 1) {
              println("Plain Text: "+tmp.toHexString)
            }
            c.io.write_data.poke(tmp)
            c.clock.step()
            cycle+=1
          }

          // System Control init (the design is iterative)
          c.io.address.poke(ADDR_CTRL)
          c.io.write_data.poke(1.U)
          c.clock.step(2)
          c.io.write_data.poke(0.U)
          c.io.we.poke(false.B)
          c.io.address.poke(ADDR_STATUS)
          c.clock.step()
          cycle+=3
          while (c.io.read_data.peek().litValue != 1 && iter < endcycle) {
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
          while ((c.io.read_data.peek().litValue & 0x2) != 2 && iter < endcycle) {
            c.clock.step()
            iter+=1
            cycle+=1
          }

          // Ensure to read a complete block
          while (total < 16) {
            cipher_len = cipher_fptr.read(cipher_buffer, total, 16-total)
            total+=cipher_len
          }
          total = 0

          // Comparison with Golden data (cipher)
          for (i <- 0 until 4) {
            val byte1 = cipher_buffer(4*i) & 0xff
            val byte2 = cipher_buffer(4*i+1) & 0xff
            val byte3 = cipher_buffer(4*i+2) & 0xff
            val byte4 = cipher_buffer(4*i+3) & 0xff
            var tmp = (byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4) & 0xFFFFFFFFL
            if (verbose == 1) {
              println("Cipher: "+tmp.toHexString)
            }
            c.io.address.poke(ADDR_RESULT0+i)
            c.io.read_data.expect(tmp.U(32.W))
            // In aes.v, the output port doesn't have Flip-Flops.
            // Here, we assume each 32-bit result requires one cycle to be read.
            cycle+=1
          }
        }
        
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

class TestAES_base_decipher extends AnyFunSpec with ChiselSim {
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
  val ADDR_STATUS = 9
  val ADDR_CONFIG = 10
  val ADDR_KEY0   = 16
  val ADDR_BLOCK0 = 32
  val ENC = 1
  val DEC = 0
  val AES128 = 0
  val AES256 = 1
  val ADDR_RESULT0 = 48

  // Testcase settings
  val SIZE_OF_DATA = 1024

  describe("TestAES_base decipher") {
    it ("do checking decipher") {
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
        val decipher_buffer = new Array[Byte](16)
        val key_len = key_fptr.read(key_buffer)
        var input_len = 0
        var decipher_len = 0

        // System Config
        val CFG = ((AES128) | (DEC))&0x3
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
        while (processed < SIZE_OF_DATA) {
          // Ensure to read a complete block
          c.io.we.poke(true.B)
          while (total < 16) {
            input_len = cipher_fptr.read(input_buffer, total, 16-total)
            total+=input_len
          }
          processed+=16
          total = 0

          // Transferring data to AES core
          for (i <- 0 until 4) {
            c.io.address.poke(ADDR_BLOCK0+i)
            val byte1 = input_buffer(4*i) & 0xff
            val byte2 = input_buffer(4*i+1) & 0xff
            val byte3 = input_buffer(4*i+2) & 0xff
            val byte4 = input_buffer(4*i+3) & 0xff
            var tmp = (byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4) & 0xFFFFFFFFL
            if (verbose == 1) {
              println("Plain Text: "+tmp.toHexString)
            }
            c.io.write_data.poke(tmp)
            c.clock.step()
            cycle+=1
          }
          // System Control init (the design is iterative)
          c.io.address.poke(ADDR_CTRL)
          c.io.write_data.poke(1.U)
          c.clock.step(2)
          c.io.write_data.poke(0.U)
          c.io.we.poke(false.B)
          c.io.address.poke(ADDR_STATUS)
          c.clock.step()
          cycle+=3
          while (c.io.read_data.peek().litValue != 1 && iter < endcycle) {
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
          while ((c.io.read_data.peek().litValue & 0x2) != 2 && iter < endcycle) {
            c.clock.step()
            iter+=1
            cycle+=1
          }

          // Ensure to read a complete block
          while (total < 16) {
            decipher_len = decipher_fptr.read(decipher_buffer, total, 16-total)
            total+=decipher_len
          }
          total = 0

          // Comparison with Golden data (Input)
          for (i <- 0 until 4) {
            val byte1 = decipher_buffer(4*i) & 0xff
            val byte2 = decipher_buffer(4*i+1) & 0xff
            val byte3 = decipher_buffer(4*i+2) & 0xff
            val byte4 = decipher_buffer(4*i+3) & 0xff
            var tmp = (byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4) & 0xFFFFFFFFL
            if (verbose == 1) {
              println("Decipher: "+tmp.toHexString)
            }
            c.io.address.poke(ADDR_RESULT0+i)
            c.io.read_data.expect(tmp.U(32.W))
            // In aes.v, the output port doesn't have Flip-Flops.
            // Here, we assume each 32-bit result requires one cycle to be read.
            cycle+=1
          }
        }

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


