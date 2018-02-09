package capitalthree.dimgate

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.logging.log4j.Logger

const val MODID = "dimgate"

lateinit var logger: Logger

@Mod(modid = MODID, version = "1", acceptableRemoteVersions="*")
class LingeringLoot {
    @Mod.EventHandler
    fun preInit (event: FMLPreInitializationEvent) {
        logger = event.modLog

        ForcefieldRules.loadRulesFile(event.modConfigurationDirectory.resolve("dimgate.rules"), logger)
        MinecraftForge.EVENT_BUS.register(EventHandler)
    }

    @Mod.EventHandler
    fun start(e: FMLServerStartingEvent) {
        ForcefieldRules.registerReloadCommand(e, "dgreload")
    }
}

object EventHandler {
    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onTeleportAttempt(event: EntityTravelToDimensionEvent) {
        if (! event.entity.world.isRemote)
            handleEvent(event, false)
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onJoin(event: EntityJoinWorldEvent) {
        if (! event.entity.world.isRemote)
            handleEvent(EntityTravelToDimensionEvent(event.entity, event.entity.dimension), true)
    }
}