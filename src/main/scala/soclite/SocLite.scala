package soclite

import chipsalliance.rocketchip.config.Parameters
import chisel3.Output
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import freechips.rocketchip.amba.axi4.{AXI4Buffer, AXI4Xbar}
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp}

class SocLite(implicit p: Parameters) extends LazyModule {

  // axi master
  val axi_cpu = LazyModule(new CPU)

  val axi_xbar = AXI4Xbar()

  // slave devices
  val axi_ram = LazyModule(new SimSRAM(262144 * 4))
  val confreg_wrapper = LazyModule(new ConfregWrapper())
  val dummy_slave = LazyModule(new DummySlaveWrapper())


  /*
        ram          -
        confreg      -  xbar  - cpu
        dummy_slave  -
   */
  axi_xbar := AXI4Buffer() := axi_cpu.node
  axi_ram.node := AXI4Buffer() := axi_xbar
  confreg_wrapper.node := AXI4Buffer() := axi_xbar
  dummy_slave.node := AXI4Buffer() := axi_xbar

  lazy val module = new LazyModuleImp(this){
    val debug = IO(Output(new Debug))
    debug := axi_cpu.module.debug
  }

}

object SocLite extends App {
  override def main(args: Array[String]): Unit = {
    implicit val config = Parameters.empty
    val top = LazyModule(new SocLite())
    (new ChiselStage).execute(args, Seq(
      ChiselGeneratorAnnotation(() => top.module)
    ))
  }
}
