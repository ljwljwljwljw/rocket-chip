package soclite

import chipsalliance.rocketchip.config.Parameters
import chisel3._
import chisel3.experimental._
import chisel3.util.IrrevocableIO
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.diplomacy._

class DummySlaveDevice(edge: AXI4EdgeParameters) extends BlackBox {

  val io = IO(new Bundle() {
    val axi = Flipped(new AXI4Bundle(edge.bundle))
  })

}

class DummySlaveWrapper(implicit p: Parameters) extends LazyModule {

  // set slave device's parameters here
  val portParams = AXI4SlavePortParameters(Seq(
    AXI4SlaveParameters(
      address = Consts.dummySalveAddress,
      regionType = RegionType.UNCACHED,
      executable = false,
      supportsRead = TransferSizes(1, 4),
      supportsWrite = TransferSizes(1, 4)
    )
  ), beatBytes = 4)

  val node = AXI4SlaveNode(Seq(portParams))

  lazy val module = new LazyModuleImp(this){

    val (in, edge) = node.in.head

    val dummyDevice = Module(new DummySlaveDevice(edge))

    dummyDevice.io.axi <> in

  }
}
