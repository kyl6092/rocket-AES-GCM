package counter_blackbox
import chisel3._
import chisel3.experimental._
import chisel3.util._

class CounterBlackBox(val bitwidth: Int) extends BlackBox(Map("bitwidth" -> IntParam(bitwidth))) with HasBlackBoxResource {
    val io = IO(new Bundle{
        val clk = Input(Clock())
        val rst = Input(Bool())
        val en = Input(Bool())
        val out = Output(UInt(bitwidth.W))
    })
    addResource("/vsrc/counter.v")
}


class MyCounter(val bitwidth: Int) extends Module {
    val io = IO(new Bundle{
        val rst = Input(Bool())
        val en = Input(Bool())
        val out = Output(UInt(bitwidth.W))
    })
    val cnt = Module(new CounterBlackBox(bitwidth))

    cnt.io.clk := clock
    cnt.io.rst := io.rst
    cnt.io.en  := io.en

    io.out := cnt.io.out
}