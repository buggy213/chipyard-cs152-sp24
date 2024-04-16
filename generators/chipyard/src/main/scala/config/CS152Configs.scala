package chipyard

import org.chipsalliance.cde.config.{Config}
import freechips.rocketchip.rocket._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tile.{RocketTileParams}

class WithNLab5Cores(
  n: Int,
  crossing: RocketCrossingParams = RocketCrossingParams()
) extends Config((site, here, up) => {
  case TilesLocated(InSubsystem) => {
    val prev = up(TilesLocated(InSubsystem), site)
    val idOffset = up(NumTiles)
    val lab5 = RocketTileParams(
      core = RocketCoreParams(
        mulDiv = Some(MulDivParams(
          mulUnroll = 8,
          mulEarlyOut = true,
          divEarlyOut = true))
      ),
      dcache = Some(DCacheParams(
        rowBits = site(SystemBusKey).beatBits,
        nSets = 16,
        nWays = 4,
        nTLBSets = 1,
        nTLBWays = 4,
        nMSHRs = 0,
        blockBytes = site(CacheBlockBytes))),
      icache = Some(ICacheParams(
        rowBits = site(SystemBusKey).beatBits,
        nSets = 64,
        nWays = 4,
        nTLBSets = 1,
        nTLBWays = 4,
        blockBytes = site(CacheBlockBytes))))
    List.tabulate(n)(i => RocketTileAttachParams(
      lab5.copy(tileId = i + idOffset),
      crossing
    )) ++ prev
  }
  case NumTiles => up(NumTiles) + n
})

class Lab5RocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithoutTLMonitors ++
  new chipyard.config.WithSystemBusFrequency(500.0) ++
  new chipyard.config.WithMemoryBusFrequency(500.0) ++
  new chipyard.config.WithPeripheryBusFrequency(500.0) ++
  new chipyard.config.AbstractConfig)

class Lab5MESIDualRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithMESICoherence ++
  new WithNLab5Cores(2) ++                                       // dual rocket cores
  new Lab5RocketConfig)

class Lab5MSIDualRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithMSICoherence ++
  new WithNLab5Cores(2) ++                                       // dual rocket cores
  new Lab5RocketConfig)

class Lab5MIDualRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithMICoherence ++
  new WithNLab5Cores(2) ++                                       // dual rocket cores
  new Lab5RocketConfig)
