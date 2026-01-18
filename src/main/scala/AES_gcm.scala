package AES_gcm
import chisel3._
import chisel3.util._

class AESGCM_BlackBox extends ExtModule {
        val clk = IO(Input(Clock()))
        val reset_n = IO(Input(Bool()))
        val chip_en = IO(Input(Bool()))
        val we = IO(Input(Bool()))
        val address = IO(Input(UInt(8.W)))
        val datain = IO(Input(UInt(32.W)))
        val valid_i  = IO(Output(Bool()))
        val valid_o  = IO(Output(Bool()))
        val dataout = IO(Output(UInt(32.W)))
    
    addResource("/vsrc/aes_gcm/aes.v")
    addResource("/vsrc/aes_gcm/aes_round_key.v")
    addResource("/vsrc/aes_gcm/aes_sbox.v")
    addResource("/vsrc/aes_gcm/aes_sub_bytes.v")
    addResource("/vsrc/aes_gcm/aes_shift_rows.v")
    addResource("/vsrc/aes_gcm/aes_mix_columns.v")
    addResource("/vsrc/aes_gcm/aes_inc32.v")
    addResource("/vsrc/aes_gcm/fifo.v")
    addResource("/vsrc/aes_gcm/aes_gf128_mul.v")

    // addResource("/vsrc/aes_gcm/gf128_mul.sv")
    // addResource("/vsrc/aes_gcm/gf128_reduction.sv")
    // addResource("/vsrc/aes_gcm/karatsuba.sv")
    // addResource("/vsrc/aes_gcm/karatsuba_core.sv")
}


class AESGCM_Wrapper extends Module {
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

    val aes_gcm = Module(new AESGCM_BlackBox)

    aes_gcm.clk := clock
    aes_gcm.reset_n := io.rst_n
    aes_gcm.chip_en := io.chip_en
    aes_gcm.we := io.we
    aes_gcm.address := io.address
    aes_gcm.datain := io.datain

    io.dataout   := aes_gcm.dataout
    io.valid_i   := aes_gcm.valid_i
    io.valid_o   := aes_gcm.valid_o
}