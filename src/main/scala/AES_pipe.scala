package AES_pipe
import chisel3._
import chisel3.util._

class AESPipe_BlackBox extends ExtModule {
        val clk = IO(Input(Clock()))
        val reset_n = IO(Input(Bool()))
        val chip_en = IO(Input(Bool()))
        val we = IO(Input(Bool()))
        val address = IO(Input(UInt(8.W)))
        val datain = IO(Input(UInt(32.W)))
        val dataout = IO(Output(UInt(32.W)))
    
    addResource("/vsrc/aes_pipeline/bandwidth32/aes.v")
    addResource("/vsrc/aes_pipeline/bandwidth32/aes_round_key.v")
    addResource("/vsrc/aes_pipeline/bandwidth32/aes_sbox.v")
    addResource("/vsrc/aes_pipeline/bandwidth32/aes_sub_bytes.v")
    addResource("/vsrc/aes_pipeline/bandwidth32/aes_shift_rows.v")
    addResource("/vsrc/aes_pipeline/bandwidth32/aes_mix_columns.v")
}


class AESpipe_Wrapper extends Module {
    val io = IO(new Bundle{
        val rst_n = Input(Bool())
        val chip_en = Input(Bool())
        val we = Input(Bool())
        val address = Input(UInt(8.W))
        val datain = Input(UInt(32.W))
        val dataout = Output(UInt(32.W))
    })

    val aes_pipe = Module(new AESPipe_BlackBox)

    aes_pipe.clk := clock
    aes_pipe.reset_n := io.rst_n
    aes_pipe.chip_en := io.chip_en
    aes_pipe.we := io.we
    aes_pipe.address := io.address
    aes_pipe.datain := io.datain

    io.dataout := aes_pipe.dataout
}