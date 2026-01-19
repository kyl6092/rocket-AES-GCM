import AES_gcm._
import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.funspec.AnyFunSpec


class TestAES_gcm_encipher extends AnyFunSpec with ChiselSim {
  // I/O related
  val key_filename =      "GCM/key.bin"
  val input_filename =    "GCM/Input.bin"
  val cipher_filename =   "GCM/cipher.bin"
  val decipher_filename = "GCM/decipher.bin"
  val iv_filename =       "GCM/iv.bin"
  val tag_filename =       "GCM/tag.bin"
  val key_fptr = getClass.getClassLoader.getResourceAsStream(key_filename)
  val input_fptr = getClass.getClassLoader.getResourceAsStream(input_filename)
  val cipher_fptr = getClass.getClassLoader.getResourceAsStream(cipher_filename)
  val decipher_fptr = getClass.getClassLoader.getResourceAsStream(decipher_filename)
  val iv_fptr = getClass.getClassLoader.getResourceAsStream(iv_filename)
  val tag_fptr = getClass.getClassLoader.getResourceAsStream(tag_filename)

  // variables
  var mode = 0
  var total = 0
  var processed = 0
  val verbose = 0
  val endcycle = 100000000

  // Control related
  val ADDR_CTRL   = 8
  val ADDR_DATA = 9
  val ADDR_DATA_END = 11
  val ADDR_CONFIG = 10
  val ADDR_KEY0   = 16
  val ADDR_IV0    = 32
  val ENC = 1
  val DEC = 0
  val AES128 = 0
  val AES192 = 1
  val AES256 = 2
  val ECB = 0
  val CTR = 1
  val GCM = 2
  val opmode = GCM

  // Testcase settings
  val KEYLEN = 4
  val SIZE_OF_DATA = 1024
  val IVLEN = 12
  mode = AES128

  describe ("TestAES_gcm_encipher") {
    it ("do checking waveform") {
      simulate(new AESGCM_Wrapper) { c=>
        // System Reset
        c.io.rst_n.poke(false.B)
        c.clock.step()
        c.clock.step()
        c.io.rst_n.poke(true.B)
        c.io.chip_en.poke(true.B)
        c.io.we.poke(true.B)

        // TestCase Preparation
        val key_buffer = new Array[Byte](KEYLEN*4)
        val iv_buffer = new Array[Byte](16)
        val input_buffer = new Array[Byte](16)
        val cipher_buffer = new Array[Byte](SIZE_OF_DATA)
        var result_buffer = new Array[Byte](SIZE_OF_DATA)
        var tag_expect_buffer = new Array[Byte](16)
        var tag_rt_buffer = new Array[Byte](16)

        var key_len     = 0
        var iv_len      = 0
        var input_len   = 0
        var cipher_len  = 0
        var tag_len     = 0

        while (total < KEYLEN*4) {
          key_len = key_fptr.read(key_buffer, total, KEYLEN*4-total)
          total+=key_len
        }
        total = 0

        while (total < IVLEN) {
          iv_len = iv_fptr.read(iv_buffer, total, 16-total)
          total+=iv_len
        }
        total = 0
        // System Config
        val CFG = ((ENC<<4) | opmode<<2 | (mode))&0x1f
        c.io.address.poke(ADDR_CONFIG)
        c.io.datain.poke(CFG)
        c.clock.step()

        // Monitor variable
        var cycle = 0
        var idx  = 0
        var success = 0

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
        // IV Data Transferring
        for (i <- 0 until 4-1) {
          c.io.address.poke(ADDR_IV0+i)
          val byte1 = iv_buffer(4*i) & 0xff
          val byte2 = iv_buffer(4*i+1) & 0xff
          val byte3 = iv_buffer(4*i+2) & 0xff
          val byte4 = iv_buffer(4*i+3) & 0xff
          var tmp = (byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4)
          c.io.datain.poke(tmp)
          c.clock.step()
          cycle+=1
        }

        // Key expansion
        c.io.address.poke(ADDR_CTRL)
        c.io.datain.poke(1)
        c.clock.step()
        while(c.io.valid_i.peek().litValue != 1) {
          c.clock.step()
          cycle+=1
        }
        c.io.address.poke(ADDR_DATA)

        // Data Stream
        while (processed < SIZE_OF_DATA) {
          // Ensure to read a complete block
          while (total < 16) {
            input_len = input_fptr.read(input_buffer, total, 16-total)
            total+=input_len
          }
          processed+=16
          total = 0

          // Transferring data to AES core
          for (i <- 0 until 4) {
            val byte1 = input_buffer(4*i) & 0xff
            val byte2 = input_buffer(4*i+1) & 0xff
            val byte3 = input_buffer(4*i+2) & 0xff
            val byte4 = input_buffer(4*i+3) & 0xff
            var tmp = (byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4) & 0xFFFFFFFFL
            if (verbose == 1) {
              println("Plain Text: "+tmp.toHexString)
            }
            c.io.datain.poke(tmp)

            // Read data stream
            if (c.io.valid_o.peek().litValue == 1) {
              val tmp = c.io.dataout.peek().litValue
              val byte1 = (tmp>>24 & 0xff).toByte
              val byte2 = (tmp>>16 & 0xff).toByte
              val byte3 = (tmp>>8 & 0xff).toByte
              val byte4 = (tmp & 0xff).toByte
              result_buffer(idx)   = byte1
              result_buffer(idx+1) = byte2
              result_buffer(idx+2) = byte3
              result_buffer(idx+3) = byte4
              idx+=4
            }
            c.clock.step()
            cycle+=1
          }
        }
        c.io.address.poke(ADDR_DATA_END)
        // Continue reading data stream
        while (idx < SIZE_OF_DATA) {
          if (c.io.valid_o.peek().litValue == 1) {
            val tmp = c.io.dataout.peek().litValue
            val byte1 = (tmp>>24 & 0xff).toByte
            val byte2 = (tmp>>16 & 0xff).toByte
            val byte3 = (tmp>>8 & 0xff).toByte
            val byte4 = (tmp & 0xff).toByte
            result_buffer(idx)   = byte1
            result_buffer(idx+1) = byte2
            result_buffer(idx+2) = byte3
            result_buffer(idx+3) = byte4
            idx+=4
          }
          c.clock.step()
          cycle+=1
        }
        idx = 0
        c.clock.step()
        while (idx < 16) {
          if (c.io.valid_o.peek().litValue == 1) {
            val tmp = c.io.dataout.peek().litValue
            val byte1 = (tmp>>24 & 0xff).toByte
            val byte2 = (tmp>>16 & 0xff).toByte
            val byte3 = (tmp>>8 & 0xff).toByte
            val byte4 = (tmp & 0xff).toByte
            tag_rt_buffer(idx)   = byte1
            tag_rt_buffer(idx+1) = byte2
            tag_rt_buffer(idx+2) = byte3
            tag_rt_buffer(idx+3) = byte4
            idx+=4
          }
          c.clock.step()
          cycle+=1
        }
        idx = 0

        // Ensure to read complete blocks
        while (total < SIZE_OF_DATA) {
          cipher_len = cipher_fptr.read(cipher_buffer, total, SIZE_OF_DATA-total)
          total+=cipher_len
        }
        // Comparison with godlen data
        total = 0
        while (idx < SIZE_OF_DATA) {
          val output = result_buffer(idx)&0xff
          val golden = cipher_buffer(idx)&0xff
          if ( output == golden)
            success += 1
          if (verbose==1) {
            println("output: ",result_buffer(idx))
            println("golden: ",cipher_buffer(idx))
          }
          idx+=1
        }
        if (success == SIZE_OF_DATA)
          println("Cipher Success!")
        else
          println("Cipher Failure!")
        success = 0
        idx = 0

        while (total < 16) {
          tag_len = tag_fptr.read(tag_expect_buffer, total, 16-total)
          total+=tag_len
        }
        total = 0
        while (idx < 16) {
          val output = tag_rt_buffer(idx)&0xff
          val golden = tag_expect_buffer(idx)&0xff
          if (output == golden)
            success += 1
          if (verbose==1) {
            println("output: ",tag_rt_buffer(idx))
            println("golden: ",tag_expect_buffer(idx))
          }
          idx+=1
        }
        if (success == 16)
          println("Tag Success!")
        else
          println("Tag Failure!")
        success = 0
        idx = 0
        
        
        // Report Clock Cycle
        print("Cycles = "+cycle.toString+"\n\n")

        // I/O related
        key_fptr.close()
        input_fptr.close()
        cipher_fptr.close()
        decipher_fptr.close()
        iv_fptr.close()
      }
    }
  }
}