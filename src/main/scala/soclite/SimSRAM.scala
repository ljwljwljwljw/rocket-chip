package soclite

import chipsalliance.rocketchip.config.Parameters
import chisel3._
import chisel3.util._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.diplomacy._

class SimSRAM(memByte: Long)(implicit p: Parameters) extends LazyModule {
  val portParams = AXI4SlavePortParameters(Seq(
    AXI4SlaveParameters(
      address = Consts.ramAddress,
      regionType = RegionType.CACHED,
      supportsRead = TransferSizes(1, 4),
      supportsWrite = TransferSizes(1, 4),
      interleavedId = Some(0)
    )
  ), beatBytes = 4)
  val node = AXI4SlaveNode(Seq(portParams))

  lazy val module = new SimSRAMImpl(this){

    val offsetBits = log2Up(memByte)
    val offsetMask = (1 << offsetBits) - 1
    def index(addr: UInt) = ((addr & offsetMask.U) >> log2Ceil(4)).asUInt()
    def inRange(idx: UInt) = idx < (memByte / 8).U

    val wIdx = index(waddr) + writeBeatCnt
    val rIdx = index(raddr) + readBeatCnt
    val wen = in.w.fire() && inRange(wIdx)

    val mem = Module(new RAMHelper())
    mem.io.clk := clock
    mem.io.rIdx := rIdx
    mem.io.wIdx := wIdx
    mem.io.wdata := in.w.bits.data
    mem.io.wmask := fullMask
    mem.io.wen := wen

    val rdata = mem.io.rdata
    in.r.bits.data := RegEnable(rdata, ren)
  }

}

class SimSRAMImpl(outer: SimSRAM) extends LazyModuleImp(outer) {
  val (in, edge) = outer.node.in.head

  val fullMask = MaskExpand(in.w.bits.strb)
  def genWdata(originData: UInt) = (originData & ~fullMask) | (in.w.bits.data & fullMask)

  val raddr = Wire(UInt())
  val ren = Wire(Bool())
  val (readBeatCnt, rLast) = {
    val c = Counter(256)
    val beatCnt = Counter(256)
    val len = HoldUnless(in.ar.bits.len, in.ar.fire())
    val burst = HoldUnless(in.ar.bits.burst, in.ar.fire())
    val wrapAddr = in.ar.bits.addr & ~(in.ar.bits.len.asTypeOf(UInt(edge.bundle.addrBits.W)) << in.ar.bits.size)
    raddr := HoldUnless(wrapAddr, in.ar.fire())
    in.r.bits.last := (c.value === len)
    when (ren) {
      beatCnt.inc()
      when (burst === AXI4Parameters.BURST_WRAP && beatCnt.value === len) { beatCnt.value := 0.U }
    }
    when (in.r.fire()) {
      c.inc()
      when (in.r.bits.last) { c.value := 0.U }
    }
    when (in.ar.fire()) {
      beatCnt.value := (in.ar.bits.addr >> in.ar.bits.size) & in.ar.bits.len
      when (in.ar.bits.len =/= 0.U && in.ar.bits.burst === AXI4Parameters.BURST_WRAP) {
        assert(in.ar.bits.len === 1.U || in.ar.bits.len === 3.U ||
          in.ar.bits.len === 7.U || in.ar.bits.len === 15.U)
      }
    }
    (beatCnt.value, in.r.bits.last)
  }

  val r_busy = BoolStopWatch(in.ar.fire(), in.r.fire() && rLast, startHighPriority = true)
  in.ar.ready := in.r.ready || !r_busy
  in.r.bits.resp := AXI4Parameters.RESP_OKAY
  ren := RegNext(in.ar.fire(), init=false.B) || (in.r.fire() && !rLast)
  in.r.valid := BoolStopWatch(ren && (in.ar.fire() || r_busy), in.r.fire(), startHighPriority = true)


  val waddr = Wire(UInt())
  val (writeBeatCnt, wLast) = {
    val c = Counter(256)
    waddr := HoldUnless(in.aw.bits.addr, in.aw.fire())
    when (in.w.fire()) {
      c.inc()
      when (in.w.bits.last) { c.value := 0.U }
    }
    (c.value, in.w.bits.last)
  }

  val w_busy = BoolStopWatch(in.aw.fire(), in.b.fire(), startHighPriority = true)
  in.aw.ready := !w_busy
  in. w.ready := in.aw.valid || (w_busy)
  in.b.bits.resp := AXI4Parameters.RESP_OKAY
  in.b.valid := BoolStopWatch(in.w.fire() && wLast, in.b.fire(), startHighPriority = true)

  in.b.bits.id   := RegEnable(in.aw.bits.id, in.aw.fire())
  in.b.bits.user := RegEnable(in.aw.bits.user, in.aw.fire())
  in.r.bits.id   := RegEnable(in.ar.bits.id, in.ar.fire())
  in.r.bits.user := RegEnable(in.ar.bits.user, in.ar.fire())
}

class RAMHelper(val DataBits: Int =  32) extends BlackBox {
  val io = IO(new Bundle {
    val clk   = Input(Clock())
    val en    = Input(Bool())
    val rIdx  = Input(UInt(DataBits.W))
    val rdata = Output(UInt(DataBits.W))
    val wIdx  = Input(UInt(DataBits.W))
    val wdata = Input(UInt(DataBits.W))
    val wmask = Input(UInt(DataBits.W))
    val wen   = Input(Bool())
  })
}

object HoldUnless {
  def apply[T <: Data](x: T, en: Bool): T = Mux(en, x, RegEnable(x, 0.U.asTypeOf(x), en))
}

object ReadAndHold {
  def apply[T <: Data](x: Mem[T], addr: UInt, en: Bool): T = HoldUnless(x.read(addr), en)
  def apply[T <: Data](x: SyncReadMem[T], addr: UInt, en: Bool): T = HoldUnless(x.read(addr, en), RegNext(en))
}

object BoolStopWatch {
  def apply(start: Bool, stop: Bool, startHighPriority: Boolean = false) = {
    val r = RegInit(false.B)
    if (startHighPriority) {
      when (stop) { r := false.B }
      when (start) { r := true.B }
    }
    else {
      when (start) { r := true.B }
      when (stop) { r := false.B }
    }
    r
  }
}

object MaskExpand {
  def apply(m: UInt) = Cat(m.asBools.map(Fill(8, _)).reverse)
}
