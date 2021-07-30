package soclite

import freechips.rocketchip.diplomacy.AddressSet

object Consts {
  val confregAddress = Seq(
    AddressSet(0xbfaf0000L, 0xffffL)
  )
  val ramAddress = Seq(
    AddressSet(0xbfc00000L, 0xfffffL)
  )
}
