package dev.robottitan;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Right-click a block: summon your titan there (uses up the core, you get it back if the titan dies).
 * While piloting a titan: right-click anywhere to fire the cannon.
 */
public class TitanCoreItem extends Item {

    public TitanCoreItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        // Inside a titan: let use() handle it (fire the cannon) instead of summoning another one.
        if (player != null && player.getVehicle() instanceof TitanEntity) return InteractionResult.PASS;

        Level level = ctx.getLevel();
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;

        BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
        TitanEntity titan = RobotTitanMod.TITAN.get().create(server);
        if (titan == null) return InteractionResult.FAIL;

        titan.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player != null ? player.getYRot() : 0.0F, 0.0F);
        server.addFreshEntity(titan);

        server.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 2.0F, 0.5F);
        server.sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 1.5, 0.3, 1.5, 0.0);

        if (player == null || !player.getAbilities().instabuild) ctx.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getVehicle() instanceof TitanEntity titan) {
            if (!level.isClientSide) {
                titan.fireCannon(player);
                player.getCooldowns().addCooldown(this, 15);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }
}
