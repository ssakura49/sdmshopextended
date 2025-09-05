package com.ssakura49.sdmshopextended;

import com.mojang.logging.LogUtils;
import com.ssakura49.sdmshopextended.common.shop.ShopLotteryEntryType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.sixik.sdmshoprework.api.register.ShopContentRegister;
import org.slf4j.Logger;

@Mod(SDMSE.MODID)
public class SDMSE {
    public static final String MODID = "sdmshopextended";
    private static final Logger LOGGER = LogUtils.getLogger();

    public SDMSE(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        init();
    }


    public static void init() {
        ShopContentRegister.registerType(new ShopLotteryEntryType.Constructor());
    }

}
