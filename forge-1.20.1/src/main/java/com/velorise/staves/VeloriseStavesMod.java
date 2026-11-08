package com.velorise.staves;

import com.velorise.staves.item.ArcaneStaffItem;
import com.velorise.staves.item.MagicMirrorItem;
import com.velorise.staves.item.VoidTranslocatorItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

@Mod(VeloriseStavesMod.MODID)
public final class VeloriseStavesMod {
    public static final String MODID = "velorise_staves";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Sounds
    public static final Supplier<SoundEvent> MAGIC_MIRROR_USE_SOUND = SOUND_EVENTS.register("magic_mirror_use",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "magic_mirror_use")));

    // Combat Staves
    public static final Supplier<Item> DRAGONFIRE_SCEPTER = ITEMS.register("dragonfire_scepter",
        () -> new ArcaneStaffItem(ArcaneStaffItem.Kind.DRAGONFIRE, 500));

    public static final Supplier<Item> RESONANT_ECHO = ITEMS.register("resonant_echo",
        () -> new ArcaneStaffItem(ArcaneStaffItem.Kind.ECHO, 450));

    public static final Supplier<Item> INFERNO_ROD = ITEMS.register("inferno_rod",
        () -> new ArcaneStaffItem(ArcaneStaffItem.Kind.INFERNO, 550));

    public static final Supplier<Item> GHASTFIRE_STAFF = ITEMS.register("ghastfire_staff",
        () -> new ArcaneStaffItem(ArcaneStaffItem.Kind.GHASTFIRE, 400));

    public static final Supplier<Item> WITHER_SCEPTER = ITEMS.register("wither_scepter",
        () -> new ArcaneStaffItem(ArcaneStaffItem.Kind.WITHER, 450));

    public static final Supplier<Item> SHULKER_SEEKER = ITEMS.register("shulker_seeker",
        () -> new ArcaneStaffItem(ArcaneStaffItem.Kind.SHULKER, 450));

    public static final Supplier<Item> GUARDIAN_LENS = ITEMS.register("guardian_lens",
        () -> new ArcaneStaffItem(ArcaneStaffItem.Kind.GUARDIAN, 500));

    public static final Supplier<Item> EVOKER_FANG_STAFF = ITEMS.register("evoker_fang_staff",
        () -> new ArcaneStaffItem(ArcaneStaffItem.Kind.EVOKER, 400));

    public static final Supplier<Item> STORMCALLER = ITEMS.register("stormcaller",
        () -> new ArcaneStaffItem(ArcaneStaffItem.Kind.LIGHTNING, 450));

    public static final Supplier<Item> FROSTBOLT_WAND = ITEMS.register("frostbolt_wand",
        () -> new ArcaneStaffItem(ArcaneStaffItem.Kind.FROST, 500));

    public static final Supplier<Item> WEBCASTER = ITEMS.register("webcaster",
        () -> new ArcaneStaffItem(ArcaneStaffItem.Kind.WEB, 450));

    // Utility & Mobility Artifacts
    public static final Supplier<Item> VOID_TRANSLOCATOR = ITEMS.register("void_translocator",
        () -> new VoidTranslocatorItem(250));

    public static final Supplier<Item> MAGIC_MIRROR = ITEMS.register("magic_mirror",
        () -> new MagicMirrorItem(100));

    // Creative Tab
    public static final Supplier<CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("velorise_staves_tab",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.velorise_staves"))
            .icon(() -> new ItemStack(DRAGONFIRE_SCEPTER.get()))
            .displayItems((parameters, output) -> {
                output.accept(DRAGONFIRE_SCEPTER.get());
                output.accept(RESONANT_ECHO.get());
                output.accept(INFERNO_ROD.get());
                output.accept(GHASTFIRE_STAFF.get());
                output.accept(WITHER_SCEPTER.get());
                output.accept(SHULKER_SEEKER.get());
                output.accept(GUARDIAN_LENS.get());
                output.accept(EVOKER_FANG_STAFF.get());
                output.accept(STORMCALLER.get());
                output.accept(FROSTBOLT_WAND.get());
                output.accept(WEBCASTER.get());
                output.accept(VOID_TRANSLOCATOR.get());
                output.accept(MAGIC_MIRROR.get());
            })
            .build());

    public VeloriseStavesMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
