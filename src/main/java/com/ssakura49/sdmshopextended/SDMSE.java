package com.ssakura49.sdmshopextended;

import com.mojang.logging.LogUtils;
import com.ssakura49.sdmshopextended.common.shop.ShopLotteryEntryType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.sixik.sdmshoprework.api.register.ShopContentRegister;
import org.slf4j.Logger;

@Mod(SDMSE.MODID)
public class SDMSE {
    public static final String MODID = "sdmshopextended";
    private static final Logger LOGGER = LogUtils.getLogger();

    public SDMSE(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
        init();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    public static void init() {
        ShopContentRegister.registerType(new ShopLotteryEntryType.Constructor());
        //ShopContentRegister.SHOP_ENTRY_TYPES.put(ShopLotteryEntryType.ID, ShopLotteryEntryType::new);
    }

}
