// See LICENSE.SiFive for license details.

package freechips.rocketchip.system // TODO this should really be in a testharness package

import chisel3._
import freechips.rocketchip.amba._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.config.{Parameters}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.{CanHaveMasterAXI4MMIOPort, CanHaveMasterAXI4MemPort, ExtMem}

/** Memory with AXI port for use in elaboratable test harnesses. */
class SimAXIMem(edge: AXI4EdgeParameters, size: BigInt)(implicit p: Parameters) extends SimpleLazyModule {
  val node = AXI4MasterNode(List(edge.master))
  val srams = AddressSet.misaligned(0, size).map { aSet =>
    LazyModule(new AXI4RAM(
      address = aSet,
      beatBytes = edge.bundle.dataBits/8,
      wcorrupt=edge.slave.requestKeys.contains(AMBACorrupt)))
  }
  val xbar = AXI4Xbar()
  srams.foreach{ s => s.node := AXI4Buffer() := AXI4Fragmenter() := xbar }
  xbar := node
  val io_axi4 = InModuleBody { node.makeIOs() }
}

object SimAXIMem {
  def connectMMIO(dut: CanHaveMasterAXI4MMIOPort)(implicit p: Parameters): Seq[SimAXIMem] = {
    dut.mmio_axi4.zip(dut.mmioAXI4Node.in).map { case (io, (_, edge)) =>
      // test harness size capped to 4KB (ignoring p(ExtMem).get.master.size)
      val mmio_mem = LazyModule(new SimAXIMem(edge, size = 4096))
      Module(mmio_mem.module).suggestName("mmio_mem")
      mmio_mem.io_axi4.head <> io
      mmio_mem
    }
  }

  def connectMem(dut: CanHaveMasterAXI4MemPort)(implicit p: Parameters): Seq[SimAXIMem] = {
    dut.mem_axi4.zip(dut.memAXI4Node.in).map { case (io, (_, edge)) =>
      val mem = LazyModule(new SimAXIMem(edge, size = p(ExtMem).get.master.size))
      Module(mem.module).suggestName("mem")
      mem.io_axi4.head <> io
      mem
    }
  }
}
