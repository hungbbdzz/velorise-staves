package com.velorise.staves.manager;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Manages lingering mystic damage zones created by Dragonfire Scepter impacts. */
public final class DragonfireCloudManager {
    private static final Map<ServerLevel, List<Cloud>> CLOUDS = new IdentityHashMap<>();

    private DragonfireCloudManager() {}

    public static void spawn(ServerLevel level, Vec3 center, ServerPlayer owner) {
        double radius = 4.0D;
        float pulseDamage = 5.0F;
        int durationTicks = 120; // 6 seconds
        int pulseIntervalTicks = 10; // every 0.5s

        CLOUDS.computeIfAbsent(level, ignored -> new ArrayList<>()).add(new Cloud(
            center, owner.getUUID(), radius, pulseDamage, durationTicks, pulseIntervalTicks));
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y, center.z, 1, 0, 0, 0, 0);
    }

    public static void tick(MinecraftServer server) {
        Iterator<Map.Entry<ServerLevel, List<Cloud>>> levelIterator = CLOUDS.entrySet().iterator();
        while (levelIterator.hasNext()) {
            Map.Entry<ServerLevel, List<Cloud>> entry = levelIterator.next();
            ServerLevel level = entry.getKey();
            if (level.getServer() != server) continue;
            List<Cloud> clouds = entry.getValue();
            Iterator<Cloud> iterator = clouds.iterator();
            while (iterator.hasNext()) {
                Cloud cloud = iterator.next();
                if (!cloud.tick(level)) iterator.remove();
            }
            if (clouds.isEmpty()) levelIterator.remove();
        }
    }

    private static final class Cloud {
        private final Vec3 center;
        private final UUID ownerId;
        private final double radius;
        private final float pulseDamage;
        private final int duration;
        private final int interval;
        private int age;

        private Cloud(Vec3 center, UUID ownerId, double radius, float pulseDamage, int duration, int interval) {
            this.center = center;
            this.ownerId = ownerId;
            this.radius = radius;
            this.pulseDamage = pulseDamage;
            this.duration = duration;
            this.interval = Math.max(1, interval);
        }

        private boolean tick(ServerLevel level) {
            age++;
            if (age == 1 || age % interval == 0) pulse(level);
            return age < duration;
        }

        private void pulse(ServerLevel level) {
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerId);
            level.sendParticles(ParticleTypes.DRAGON_BREATH, center.x, center.y + 0.25D, center.z,
                35, radius * 0.45D, 0.5D, radius * 0.45D, 0.02D);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                    new AABB(center, center).inflate(radius), entity -> entity.isAlive() && entity != owner)) {
                if (target.position().distanceToSqr(center) > radius * radius) continue;
                target.invulnerableTime = 0;
                target.setSecondsOnFire(3);
                target.hurt(level.damageSources().magic(), pulseDamage);
            }
        }
    }
}
