package soclite

import chipsalliance.rocketchip.config.Parameters
import chisel3._
import chisel3.util._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.diplomacy.{IdRange, LazyModule, LazyModuleImp}

class CPUAXIBundle extends Bundle {
  val bundleParams = AXI4BundleParameters(
    addrBits = 32,
    dataBits = 32,
    idBits = 4
  )
  val ar = Output(new AXI4BundleAR(bundleParams))
  val r = Input(new AXI4BundleR(bundleParams))
  val aw = Output(new AXI4BundleAW(bundleParams))
  val w = Output(new AXI4BundleW(bundleParams))
  val b = Input(new AXI4BundleB(bundleParams))
  val ar_valid, aw_valid, w_valid = Output(Bool())
  val ar_ready, aw_ready, w_ready = Input(Bool())
  val r_valid, b_valid = Input(Bool())
  val r_ready, b_ready = Output(Bool())
}

class Debug extends Bundle {
  val wb_pc = UInt(32.W)
  val wb_rf_wen = Bool()
  val wb_rf_wnum = UInt(5.W)
  val wb_rf_wdata = UInt(32.W)
}

class mycpu_top extends BlackBox {
  val io = IO(new Bundle() {
    val cpu = new CPUAXIBundle
    val ext_int = Input(UInt(6.W))
    val aclk = Input(Bool())
    val aresetn = Input(Bool())
    val debug = Output(new Debug)
  })
}

class CPU(implicit p: Parameters) extends LazyModule {

  val portParam = AXI4MasterPortParameters(Seq(AXI4MasterParameters(
    name = "mycpu",
    id = IdRange(0, 16)
  )))

  val node = AXI4MasterNode(Seq(portParam))

  lazy val module = new LazyModuleImp(this){
    val (out, edge) = node.out.head
    val debug = IO(Output(new Debug))
    val my_cpu = Module(new mycpu_top)
    out.elements.foreach {
      case (str, data: IrrevocableIO[AXI4BundleBase]) =>
        str match {
          case "r" | "b" =>
            my_cpu.io.cpu.elements.filter(_._1 == str).head._2 <> data.bits
          case "ar" | "aw" | "w" =>
            data.bits <> my_cpu.io.cpu.elements.filter(_._1 == str).head._2
        }
        data.valid <> my_cpu.io.cpu.elements.filter(_._1 == s"${str}_valid").head._2
        data.ready <> my_cpu.io.cpu.elements.filter(_._1 == s"${str}_ready").head._2
    }
    my_cpu.io.ext_int := 0.U
    my_cpu.io.aclk := this.clock.asBool()
    my_cpu.io.aresetn := !this.reset.asBool()
    debug := my_cpu.io.debug
  }
}