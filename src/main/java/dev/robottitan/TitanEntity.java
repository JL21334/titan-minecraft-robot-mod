package dev.robottitan;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Robot Titan. Right-click it to climb in. W/A/S/D walks it, the mouse aims it,
 * Shift gets you out. Hold the Titan Core and right-click to fire the cannon.
 */
public class TitanEntity extends Mob {

    public TitanEntity(EntityType<TitanEntity> type, Level level) {
        super(type, level);
        this.setMaxUpStep(3.0F);      // steps up 3-block walls
        this.xpReward = 0;
        this.setPersistenceRequired(); // never despawns
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 300.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    // ---------- getting in ----------

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.isVehicle()) {
            if (!level().isClientSide) player.startRiding(this);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public LivingEntity getControllingPassenger() {
        Entity first = this.getFirstPassenger();
        return first instanceof Player p ? p : null;
    }

    /** Where the pilot sits: in the open hatch on top of the torso, looking over the head. */
    @Override
    public double getPassengersRidingOffset() {
        return 5.15D;
    }

    // ---------- piloting ----------

    @Override
    public void travel(Vec3 travelVector) {
        LivingEntity pilot = this.getControllingPassenger();
        if (this.isVehicle() && pilot != null) {
            this.setYRot(pilot.getYRot());
            this.yRotO = this.getYRot();
            this.setXRot(pilot.getXRot() * 0.5F);
            this.setRot(this.getYRot(), this.getXRot());
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.yBodyRot;

            float strafe = pilot.xxa * 0.5F;
            float forward = pilot.zza;
            if (forward < 0) forward *= 0.5F;

            if (this.isControlledByLocalInstance()) {
                this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED));
                super.travel(new Vec3(strafe, travelVector.y, forward));
            } else {
                this.setDeltaMovement(Vec3.ZERO);
            }
        } else {
            super.travel(travelVector);
        }
    }

    // ---------- weapons ----------

    /** Cannon: a big explosive fireball in the direction the pilot is looking. */
    public void fireCannon(Player pilot) {
        Vec3 look = pilot.getLookAngle();
        LargeFireball ball = EntityType.FIREBALL.create(level());
        if (ball == null) return;
        ball.setOwner(pilot);
        ball.setPos(getX() + look.x * 2.5, getY() + 4.2 + look.y * 2.5, getZ() + look.z * 2.5);
        ball.setDeltaMovement(look.scale(1.6));
        CompoundTag tag = ball.saveWithoutId(new CompoundTag());
        tag.putByte("ExplosionPower", (byte) 3);
        ListTag power = new ListTag();
        power.add(DoubleTag.valueOf(look.x * 0.084));
        power.add(DoubleTag.valueOf(look.y * 0.084));
        power.add(DoubleTag.valueOf(look.z * 0.084));
        tag.put("power", power);
        ball.load(tag);
        level().addFreshEntity(ball);
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 2.0F, 0.5F);
    }

    /** Stomp: while piloted, hostile mobs standing right next to the titan get crushed. */
    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && this.isVehicle() && this.tickCount % 8 == 0) {
            for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(1.5, 0, 1.5), x -> x instanceof Enemy && x != this)) {
                e.hurt(this.damageSources().mobAttack(this), 10.0F);
            }
        }
    }

    // ---------- toughness, sounds, drops ----------

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 0.5F);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    /** When the titan is destroyed it drops its core so you can summon it again. */
    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(RobotTitanMod.CORE.get());
    }
}
