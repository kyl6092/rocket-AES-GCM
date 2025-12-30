package counter_blackbox
import chisel3._
import chisel3.util._

class CounterBlackBox(val bitwidth: Int) extends ExtModule(Map("bitwidth" -> bitwidth)) {
    
        val clk = IO(Input(Clock()))
        val rst = IO(Input(Bool()))
        val en = IO(Input(Bool()))
        val out = IO(Output(UInt(bitwidth.W)))
    
    addResource("/vsrc/counter.v")
}


class MyCounter(val bitwidth: Int) extends Module {
    val io = IO(new Bundle{
        val rst = Input(Bool())
        val en = Input(Bool())
        val out = Output(UInt(bitwidth.W))
    })
    val cnt = Module(new CounterBlackBox(bitwidth))

    cnt.clk := clock
    cnt.rst := io.rst
    cnt.en  := io.en

    io.out := cnt.out
}