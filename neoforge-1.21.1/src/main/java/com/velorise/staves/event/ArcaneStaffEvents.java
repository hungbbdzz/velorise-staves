package com.velorise.staves.event;

import com.velorise.staves.VeloriseStavesMod;
import com.velorise.staves.item.ArcaneStaffItem;
import com.velorise.staves.manager.DragonfireCloudManager;
import com.velorise.staves.manager.TemporaryWebManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;

/** Server-side impact resolution for Arcane Staves projectiles and server tick managers. */
@EventBusSubscriber(modid = VeloriseStavesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class ArcaneStaffEvents {
    public static final String TAG_KIND = "ArcaneStaffKind";
    private static final String TAG_ORIGIN_X = "ArcaneStaffOriginX";
    private static final String TAG_ORIGIN_Y = "ArcaneStaffOriginY";
    private static final String TAG_ORIGIN_Z = "ArcaneStaffOriginZ";
    private static final String TAG_MAX_RANGE = "ArcaneStaffMaxRange";

    private ArcaneStaffEvents() {}

    public static void markRange(Projectile projectile, Vec3 origin, double maxRange) {
        projectile.getPersistentData().putDouble(TAG_ORIGIN_X, origin.x);
        projectile.getPersistentData().putDouble(TAG_ORIGIN_Y, origin.y);
        projectile.getPersistentData().putDouble(TAG_ORIGIN_Z, origin.z);
        projectile.getPersistentData().putDouble(TAG_MAX_RANGE, Math.max(1.0D, maxRange));
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        Projectile projectile = event.getProjectile();
        if (!(projectile.level() instanceof ServerLevel level)) return;
        String kind = projectile.getPersistentData().getString(TAG_KIND);
        if (kind.isEmpty()) return;

        Vec3 hit = event.getRayTraceResult().getLocation();
        if (isBeyondRange(projectile, hit)) {
            event.setCanceled(true);
            level.sendParticles(ParticleTypes.SMOKE, hit.x, hit.y, hit.z, 8, 0.12D, 0.12D, 0.12D, 0.01D);
            projectile.discard();
            return;
        }

        Entity owner = projectile.getOwner();

        if ("ghastfire".equals(kind)) {
            resolveGhastfire(level, hit, owner);
            return;
        }

        event.setCanceled(true);
        switch (kind) {
            case "dragonfire" -> resolveDragonfire(level, hit, owner);
            case "inferno" -> resolveInferno(level, hit, owner, event);
            case "wither" -> resolveWither(level, hit, owner);
            case "shulker" -> resolveShulker(level, hit, owner, event);
            case "frost" -> resolveFrost(level, hit, owner, event);
            case "web" -> resolveWeb(level, hit, owner, event);
            default -> {}
        }
        projectile.discard();
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        DragonfireCloudManager.tick(event.getServer());
        TemporaryWebManager.tick(event.getServer());
    }

    private static void resolveDragonfire(ServerLevel level, Vec3 hit, Entity owner) {
        if (owner instanceof ServerPlayer player) {
            DragonfireCloudManager.spawn(level, hit, player);
        }
        level.playSound(null, hit.x, hit.y, hit.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.9F, 1.15F);
    }

    private static void resolveInferno(ServerLevel level, Vec3 hit, Entity owner, ProjectileImpactEvent event) {
        LivingEntity target = hitEntity(event);
        if (target != null && target != owner) {
            target.igniteForSeconds(4);
            target.hurt(level.damageSources().magic(), 6.0F);
        }
        level.sendParticles(ParticleTypes.FLAME, hit.x, hit.y, hit.z, 25, 0.3D, 0.3D, 0.3D, 0.04D);
        level.playSound(null, hit.x, hit.y, hit.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    private static void resolveGhastfire(ServerLevel level, Vec3 hit, Entity owner) {
        double radius = 3.5D;
        float damage = 12.0F;
        List<LivingEntity> affected = targets(level, hit, radius, owner);
        for (LivingEntity target : affected) {
            target.igniteForSeconds(5);
            target.hurt(level.damageSources().magic(), damage);
        }
        level.sendParticles(ParticleTypes.FLAME, hit.x, hit.y, hit.z, 35,
            radius * 0.35D, radius * 0.25D, radius * 0.35D, 0.05D);
    }

    private static void resolveWither(ServerLevel level, Vec3 hit, Entity owner) {
        double radius = 3.0D;
        float damage = 8.0F;
        for (LivingEntity target : targets(level, hit, radius, owner)) {
            target.hurt(level.damageSources().magic(), damage);
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
        }
        level.sendParticles(ParticleTypes.SMOKE, hit.x, hit.y, hit.z, 30,
            radius * 0.4D, radius * 0.35D, radius * 0.4D, 0.04D);
        level.playSound(null, hit.x, hit.y, hit.z, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.PLAYERS, 0.8F, 1.2F);
    }

    private static void resolveShulker(ServerLevel level, Vec3 hit, Entity owner, ProjectileImpactEvent event) {
        LivingEntity target = hitEntity(event);
        if (target != null && target != owner) {
            target.hurt(level.damageSources().magic(), 8.0F);
            target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 100, 0));
        }
        level.sendParticles(ParticleTypes.END_ROD, hit.x, hit.y, hit.z, 16, 0.25D, 0.25D, 0.25D, 0.03D);
        level.playSound(null, hit.x, hit.y, hit.z, SoundEvents.SHULKER_BULLET_HIT, SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    private static void resolveFrost(ServerLevel level, Vec3 hit, Entity owner, ProjectileImpactEvent event) {
        LivingEntity target = hitEntity(event);
        if (target != null && target != owner) {
            target.hurt(level.damageSources().freeze(), 6.0F);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
            target.setTicksFrozen(Math.max(target.getTicksFrozen(), 140));
        }
        level.sendParticles(ParticleTypes.SNOWFLAKE, hit.x, hit.y, hit.z, 20, 0.3D, 0.3D, 0.3D, 0.025D);
        level.playSound(null, hit.x, hit.y, hit.z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.65F, 1.6F);
    }

    private static void resolveWeb(ServerLevel level, Vec3 hit, Entity owner, ProjectileImpactEvent event) {
        BlockPos pos;
        if (event.getRayTraceResult() instanceof EntityHitResult entityHit) {
            pos = entityHit.getEntity().blockPosition();
        } else if (event.getRayTraceResult() instanceof BlockHitResult blockHit) {
            pos = blockHit.getBlockPos().relative(blockHit.getDirection());
        } else {
            pos = BlockPos.containing(hit);
        }
        int webTicks = 100; // 5s
        int poisonAmplifier = 0;
        java.util.UUID webOwner = owner instanceof ServerPlayer player ? player.getUUID() : null;
        if (!TemporaryWebManager.place(level, pos, webTicks, poisonAmplifier, webOwner)) {
            TemporaryWebManager.place(level, pos.above(), webTicks, poisonAmplifier, webOwner);
        }
        level.sendParticles(ParticleTypes.POOF, hit.x, hit.y, hit.z, 14, 0.25D, 0.25D, 0.25D, 0.02D);
        level.playSound(null, hit.x, hit.y, hit.z, SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 0.7F, 1.5F);
    }

    public static void resolveLightning(ServerLevel level, Vec3 hit, ServerPlayer owner) {
        double radius = 3.5D;
        float damage = 10.0F;
        for (LivingEntity target : targets(level, hit, radius, owner)) {
            target.igniteForSeconds(4);
            target.hurt(level.damageSources().lightningBolt(), damage);
        }
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, hit.x, hit.y + 0.5D, hit.z, 30,
            radius * 0.35D, 0.8D, radius * 0.35D, 0.08D);
        level.playSound(null, hit.x, hit.y, hit.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static LivingEntity hitEntity(ProjectileImpactEvent event) {
        return event.getRayTraceResult() instanceof EntityHitResult entityHit
            && entityHit.getEntity() instanceof LivingEntity living ? living : null;
    }

    private static List<LivingEntity> targets(ServerLevel level, Vec3 hit, double radius, Entity owner) {
        double radiusSqr = radius * radius;
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(hit, hit).inflate(radius),
            target -> target.isAlive() && target != owner && target.position().distanceToSqr(hit) <= radiusSqr);
    }

    private static boolean isBeyondRange(Projectile projectile, Vec3 hit) {
        double maxRange = projectile.getPersistentData().getDouble(TAG_MAX_RANGE);
        if (maxRange <= 0.0D) return false;
        Vec3 origin = new Vec3(
            projectile.getPersistentData().getDouble(TAG_ORIGIN_X),
            projectile.getPersistentData().getDouble(TAG_ORIGIN_Y),
            projectile.getPersistentData().getDouble(TAG_ORIGIN_Z));
        return origin.distanceToSqr(hit) > maxRange * maxRange;
    }
}
