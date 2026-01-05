package AES_base
import chisel3._
import chisel3.util._

class AESbase_BlackBox extends ExtModule {

        val clk = IO(Input(Clock()))
        val reset_n = IO(Input(Bool()))
        val cs = IO(Input(Bool()))
        val we = IO(Input(Bool()))
        val address = IO(Input(UInt(8.W)))
        val write_data = IO(Input(UInt(32.W)))
        val read_data = IO(Output(UInt(32.W)))
    
    addResource("/vsrc/aes_baseline/aes.v")
    addResource("/vsrc/aes_baseline/aes_core.v")
    addResource("/vsrc/aes_baseline/aes_decipher_block.v")
    addResource("/vsrc/aes_baseline/aes_encipher_block.v")
    addResource("/vsrc/aes_baseline/aes_inv_sbox.v")
    addResource("/vsrc/aes_baseline/aes_key_mem.v")
    addResource("/vsrc/aes_baseline/aes_sbox.v")
}


class AESbase_Wrapper extends Module {
    val io = IO(new Bundle{
        val rst_n = Input(Bool())
        val cs = Input(Bool())
        val we = Input(Bool())
        val address = Input(UInt(8.W))
        val write_data = Input(UInt(32.W))
        val read_data = Output(UInt(32.W))
    })

    val aes_base = Module(new AESbase_BlackBox)

    aes_base.clk := clock
    aes_base.reset_n := io.rst_n
    aes_base.cs := io.cs
    aes_base.we := io.we
    aes_base.address := io.address
    aes_base.write_data := io.write_data

    io.read_data := aes_base.read_data
}