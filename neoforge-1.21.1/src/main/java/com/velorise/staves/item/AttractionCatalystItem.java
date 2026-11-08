package com.velorise.staves.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Toggleable magnetic artifact that pulls dropped items and experience orbs. */
public class AttractionCatalystItem extends Item {
    private static final double PULL_RADIUS = 10.0D;
    private static final double PULL_STRENGTH = 0.35D;

    public AttractionCatalystItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isActive(stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            boolean active = !isActive(stack);
            setActive(stack, active);
            player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.6F, active ? 1.2F : 0.8F);
            player.displayClientMessage(Component.translatable(active
                ? "message.velorise_staves.catalyst_enabled"
                : "message.velorise_staves.catalyst_disabled"), true);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer player) || !isActive(stack)) return;
        if (level.getGameTime() % 3L != 0L) return;

        Vec3 targetPos = player.position().add(0.0D, 0.5D, 0.0D);

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class,
            player.getBoundingBox().inflate(PULL_RADIUS),
            item -> item.isAlive() && !item.hasPickUpDelay());

        for (ItemEntity item : items) {
            Vec3 dir = targetPos.subtract(item.position());
            if (dir.lengthSqr() <= 0.1D) continue;
            item.setDeltaMovement(item.getDeltaMovement().add(dir.normalize().scale(PULL_STRENGTH)));
        }

        List<ExperienceOrb> orbs = level.getEntitiesOfClass(ExperienceOrb.class,
            player.getBoundingBox().inflate(PULL_RADIUS),
            Entity::isAlive);

        for (ExperienceOrb orb : orbs) {
            Vec3 dir = targetPos.subtract(orb.position());
            if (dir.lengthSqr() <= 0.1D) continue;
            orb.setDeltaMovement(orb.getDeltaMovement().add(dir.normalize().scale(PULL_STRENGTH)));
        }
    }

    private static CompoundTag tag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    private static void save(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static boolean isActive(ItemStack stack) {
        return tag(stack).getBoolean("active");
    }

    public static void setActive(ItemStack stack, boolean active) {
        CompoundTag tag = tag(stack);
        tag.putBoolean("active", active);
        save(stack, tag);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
        boolean active = isActive(stack);
        tooltip.add(Component.translatable("tooltip.velorise_staves.toggle_hint",
            Component.translatable(active ? "tooltip.velorise_staves.active" : "tooltip.velorise_staves.inactive")));
    }
}
