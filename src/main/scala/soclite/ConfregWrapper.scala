package soclite

import chipsalliance.rocketchip.config.Parameters
import chisel3._
import chisel3.experimental._
import chisel3.util.IrrevocableIO
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.diplomacy._

class confreg
(override val params: Map[String, Param] =
 Map("SIMULATION" -> RawParam("1'b1"))
) extends BlackBox {
  val io = IO(new Bundle {
    val aclk = Input(Bool())
    val timer_clk = Input(Bool())
    val aresetn = Input(Bool())
    val led = Output(UInt(16.W))
    val led_rg0 = Output(UInt(2.W))
    val led_rg1 = Output(UInt(2.W))
    val num_csn = Output(UInt(8.W))
    val num_a_g = Output(UInt(7.W))
    val switch = Input(UInt(8.W))
    val btn_key_col = Output(UInt(4.W))
    val btn_key_row = Input(UInt(4.W))
    val btn_step = Input(UInt(2.W))
    val ram_random_mask = Output(UInt(5.W))

    val arid = Input(UInt(4.W))
    val araddr = Input(UInt(32.W))
    val arlen = Input(UInt(8.W))
    val arsize = Input(UInt(3.W))
    val arburst = Input(UInt(2.W))
    val arlock = Input(UInt(2.W))
    val arcache = Input(UInt(4.W))
    val arprot =  Input(UInt(3.W))
    val arvalid = Input(Bool())
    val arready = Output(Bool())

    val awid = Input(UInt(4.W))
    val awaddr = Input(UInt(32.W))
    val awlen = Input(UInt(8.W))
    val awsize = Input(UInt(3.W))
    val awburst = Input(UInt(2.W))
    val awlock = Input(UInt(2.W))
    val awcache = Input(UInt(4.W))
    val awprot =  Input(UInt(3.W))
    val awvalid = Input(Bool())
    val awready = Output(Bool())

    val rid = Output(UInt(4.W))
    val rdata = Output(UInt(32.W))
    val rresp = Output(UInt(2.W))
    val rlast = Output(Bool())
    val rvalid = Output(Bool())
    val rready = Input(Bool())

    val wid = Input(UInt(4.W))
    val wdata = Input(UInt(32.W))
    val wstrb = Input(UInt(4.W))
    val wlast = Input(Bool())
    val wvalid = Input(Bool())
    val wready = Output(Bool())

    val bid = Output(UInt(4.W))
    val bresp = Output(UInt(2.W))
    val bvalid = Output(Bool())
    val bready = Input(Bool())
  })
}

class ConfregWrapper(implicit p: Parameters) extends LazyModule {
  val portParams = AXI4SlavePortParameters(Seq(
    AXI4SlaveParameters(
      address = Consts.confregAddress,
      regionType = RegionType.UNCACHED,
      executable = false,
      supportsRead = TransferSizes(1, 4),
      supportsWrite = TransferSizes(1, 4)
    )
  ), beatBytes = 4)
  val node = AXI4SlaveNode(Seq(portParams))
  lazy val module = new LazyModuleImp(this){
    val u_confreg = Module(new confreg)
    val (in, edge) = node.in.head
    in.elements.foreach {
      case (str, data: IrrevocableIO[AXI4BundleBase]) =>
        val prefix = str
        data.bits.elements.foreach{
          case (name, elt) =>
            val confregPort = u_confreg.io.elements.filter(_._1 == s"${prefix}${name}")
            if(confregPort.nonEmpty){
              println(prefix + name)
              elt <> confregPort.head._2
            } else {
              println(prefix + name + " not found!")
            }
        }
        data.elements.foreach{
          case (name, elt) =>
            val confregPort = u_confreg.io.elements.filter(_._1 == s"${prefix}${name}")
            if(confregPort.nonEmpty){
              println(prefix + name)
              elt <> confregPort.head._2
            } else {
              println(prefix + name + " not found!")
            }
        }
    }
    u_confreg.io.aclk := this.clock.asBool()
    u_confreg.io.switch := "hff".U
    u_confreg.io.btn_key_row := 0.U
    u_confreg.io.btn_step := 3.U
  }
}
