package delta.module.modules

import delta.event.TickEvent
import delta.module.Category
import delta.module.Module
import delta.util.CrystalUtils
import delta.util.MessageUtils
import delta.util.PlayerUtils
import delta.util.RenderUtils
import delta.util.bed.Bed
import me.bush.eventbus.annotation.EventListener
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import delta.util.bed.BedUtils as bedHelper

/*
    hi this is noat
    shout out to bush and perry
    - perry for telling me how to add kotlin
    - bush for telling me how kotlin works and letting me read 711 code
 */
class BedAura : Module("Bed Aura", "Sleeping in nether on steroids", Category.COMBAT) {
    val range = setting("Range", 6.0, 0.0, 6.0, false)
    val minDamage = setting("Damage", 6.0, 0.0, 36.0, false)
    var render: BlockPos? = null
    var damage = 0.0
    companion object {
        @JvmStatic
        fun getBedAura(): BedAura {
            return BedAura()
        }
    }

    override fun onEnable() {
        MinecraftForge.EVENT_BUS.register(this)
    }

    override fun onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this)
    }

    @SubscribeEvent
    fun onRender3d(event: RenderWorldLastEvent) {
        if (render != null) {
            RenderUtils.drawBoxESP(
                render, Color(
                    255,
                    0,
                    0,
                    100
                ), 1.0f, true, true, 100, -0.8
            )
            RenderUtils.drawText(render, "" + damage)

        }
    }
    @EventListener
    fun onPlayerUpdate(tickEvent: TickEvent.Pre) {
        if (fullNullCheck()) return
        bedPlace()
    }

    fun bedBreak(pos: BlockPos) {
        mc.playerController.processRightClickBlock(
            mc.player,
            mc.world,
            pos,
            EnumFacing.DOWN,
            Vec3d(pos),
            EnumHand.MAIN_HAND
        )

    }


    fun bedPlace() {
        val bed = getOptimalPlacePosition()
        if (bed != null) {
            bedHelper.placeBed(bed, EnumFacing.DOWN)
            bedBreak(bed)
            render = bed
        }
    }

    private fun getOptimalPlacePosition(): BlockPos? {
        var entityPlayer: EntityPlayer?
        val list = mutableListOf<Bed>()
        for (entity in mc.world.playerEntities) {
            if (entity != mc.player && entity.getDistance(mc.player) <= 11 && !entity.isDead) {
                entityPlayer = entity
                for (pos in CrystalUtils.getSphere(
                    PlayerUtils.getPlayerPos(mc.player),
                    range.dVal.toFloat(),
                    range.dVal.toInt(),
                    false,
                    true,
                    0
                )) {
                    if (bedHelper.isBedPlaceable(pos) && bedHelper.calculateBedDamage(
                            pos,
                            entityPlayer
                        ) >= minDamage.dVal
                    ) {
                        list.add(Bed(bedHelper.calculateBedDamage(pos, entityPlayer).toDouble(), pos))
                    }
                }
            }
        }
        if (list.isEmpty()) return null
        list.sortBy { it.bD }
        var bedCpe = ""
        for (bed in list) {
            bedCpe = bedCpe + "" + bed.bD.toString() + ", "
        }
        MessageUtils.sendMessage(bedCpe)
        damage = list[list.size - 1].bD
        return list[list.size - 1].bP

    }
}