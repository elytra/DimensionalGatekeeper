package capitalthree.dimgate

import com.elytradev.concrete.rulesengine.Effect
import com.elytradev.concrete.rulesengine.EvaluationContext
import com.elytradev.concrete.rulesengine.RulesEngine
import com.elytradev.concrete.common.Either
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent
import net.minecraftforge.fml.common.registry.ForgeRegistries
import java.util.function.Predicate

fun handleEvent(e: EntityTravelToDimensionEvent, killable: Boolean) {
    ForcefieldRules.act(TeleportCTX(e, killable))?.let{ logger.error("Error in DimensionalForcefield rules eval: $it")}
}

object ForcefieldRules: RulesEngine<TeleportCTX>() {
    override fun getDomainPredicates(): Map<Char, (String) -> Either<Predicate<TeleportCTX>, String>> = mapOf()

    override fun defaultPredicate(s: String): Either<out Predicate<TeleportCTX>, String> {
        return Either.left(EntityClassPredicate(
            if (s == "player" || s == "minecraft:player") EntityPlayer::class.java
            else ForgeRegistries.ENTITIES.getValue(ResourceLocation(s))?.entityClass
            ?: return Either.right("Entity not found: \"$s\"")
        ))
    }

    override fun getEffectSlots(): Set<Int> = setOf(0)
    override fun parseEffect(s: String): Either<Iterable<Effect<TeleportCTX>>, String> = when (s) {
        "y" -> Either.left(listOf(CanTeleportEffect.YES))
        "n" -> Either.left(listOf(CanTeleportEffect.NO))
        "k" -> Either.left(listOf(CanTeleportEffect.KILL))
        else -> Either.right("Only y or n allowed")
    }

    private val interestingNumberList = listOf("to", "from", "x", "y", "z", "light")
    override fun interestingNumberList() = interestingNumberList
    override fun genInterestingNumbers(from: TeleportCTX) =
        doubleArrayOf(from.e.dimension.toDouble(), from.e.entity.dimension.toDouble(),
                from.e.entity.posX, from.e.entity.posY, from.e.entity.posZ, 16.0*from.e.entity.brightness)

    override fun genDefaultRules() = """
# Dimensional Gatekeeper is a rules engine mod.
# See documentation for the .rules format here: https://github.com/elytra/Concrete/wiki/.rules-file-End-User-Documentation

# Predicates: entity resource names, see https://minecraft.gamepedia.com/Java_Edition_data_values/Entity_IDs
# Variables: to/from (dim ids), x/y/z, light
# For dimension ids see https://ftbwiki.org/Dimension
# Effects: y, n, k
# y: allow travel, n: deny travel, k: kill on sight

# All effects use the same slot, so note that n can mask a lower priority k.  It is recommended to define kill rules
# narrowly and give them high rule priority.

# n and k will both simply block teleportation attempts without any killing.
# additionally, k rules matching an item with from=to will kill an entity if it spawns through means other than cross-dimensional travel.
# In such a case, to and from will be the same dimension id.

# Silly example rule to require players to light up nether portals to return to the overworld:
# 0 (from=-1) player (light<12) (to=0) -> n
# No dropped items allowed in the nether ever:
# 5 (from=-1) item -> k
# And of course, the reason this mod was made, keep some goofy modded mod in its goofy modded dimension:
# (replace 99 with dimension id or tag for ids, and somemod:dragon with your mob or tag for mobs)
# 5 (from!=99) somemod:dragon -> k

# These are just usage examples.  There is no sensible default behavior.  This mod is intentionally left blank.
"""
}

class TeleportCTX(val e: EntityTravelToDimensionEvent, val killable: Boolean): EvaluationContext()

class EntityClassPredicate(val clazz: Class<out Entity>): Predicate<TeleportCTX> {
    override fun test(ctx: TeleportCTX) = clazz.isInstance(ctx.e.entity)
}

enum class CanTeleportEffect: Effect<TeleportCTX> {
    YES, NO, KILL;

    override fun getSlot(): Int = 0
    override fun accept(ctx: TeleportCTX) {
        if (this != YES)
            ctx.e.isCanceled = true
        if (ctx.killable && this == KILL)
            ctx.e.entity.setDead()
    }
}