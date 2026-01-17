package AES_ctr
import chisel3._
import chisel3.util._

class AESCTR_BlackBox extends ExtModule {
        val clk = IO(Input(Clock()))
        val reset_n = IO(Input(Bool()))
        val chip_en = IO(Input(Bool()))
        val we = IO(Input(Bool()))
        val address = IO(Input(UInt(8.W)))
        val datain = IO(Input(UInt(32.W)))
        val valid_i  = IO(Output(Bool()))
        val valid_o  = IO(Output(Bool()))
        val dataout = IO(Output(UInt(32.W)))
    
    addResource("/vsrc/aes_ctr/aes.v")
    addResource("/vsrc/aes_ctr/aes_round_key.v")
    addResource("/vsrc/aes_ctr/aes_sbox.v")
    addResource("/vsrc/aes_ctr/aes_sub_bytes.v")
    addResource("/vsrc/aes_ctr/aes_shift_rows.v")
    addResource("/vsrc/aes_ctr/aes_mix_columns.v")
    addResource("/vsrc/aes_ctr/aes_inc32.v")
    addResource("/vsrc/aes_ctr/fifo.v")
}


class AESCTR_Wrapper extends Module {
    val io = IO(new Bundle{
        val rst_n = Input(Bool())
        val chip_en = Input(Bool())
        val we = Input(Bool())
        val address = Input(UInt(8.W))
        val datain = Input(UInt(32.W))
        val valid_i  = Output(Bool())
        val valid_o  = Output(Bool())
        val dataout = Output(UInt(32.W))
    })

    val aes_ctr = Module(new AESCTR_BlackBox)

    aes_ctr.clk := clock
    aes_ctr.reset_n := io.rst_n
    aes_ctr.chip_en := io.chip_en
    aes_ctr.we := io.we
    aes_ctr.address := io.address
    aes_ctr.datain := io.datain

    io.dataout   := aes_ctr.dataout
    io.valid_i   := aes_ctr.valid_i
    io.valid_o   := aes_ctr.valid_o
}