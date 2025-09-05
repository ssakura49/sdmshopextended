package com.ssakura49.sdmshopextended.common.shop;

import com.ssakura49.sdmshopextended.SDMSE;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.quest.reward.ItemReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.util.INBTSerializable;
import net.sixik.sdmshoprework.SDMShopR;
import net.sixik.sdmshoprework.SDMShopRework;
import net.sixik.sdmshoprework.api.IConstructor;
import net.sixik.sdmshoprework.api.shop.AbstractShopEntry;
import net.sixik.sdmshoprework.api.shop.AbstractShopEntryType;
import net.sixik.sdmshoprework.common.utils.NBTUtils;
import net.sixik.sdmshoprework.common.utils.item.ItemHandlerHelper;

import java.util.*;

public class ShopLotteryEntryType extends AbstractShopEntryType {
    public static final String ID = "sdmshopextended:lottery";
    public RewardTable table;
    public boolean giveCrateInstead = false;


    public ShopLotteryEntryType(RewardTable rewardTable) {
        super();
        this.table = rewardTable;
    }

    @Override
    public void getConfig(ConfigGroup group) {
        // 使用 ConfigQuestObject 支持奖励表下拉选择
        group.add("table", new ConfigQuestObject<>(QuestObjectType.REWARD_TABLE),
                        table, t -> table = t, table)
                .setNameKey("sdmshopextended.shop.entry.type.lottery.config.reward_table");
        group.addBool("give_crate", giveCrateInstead, v -> giveCrateInstead = v, false)
                .setNameKey("sdmshopextended.shop.entry.type.lottery.config.give_crate");
    }

    @Override
    public AbstractShopEntryType copy() {
        return new ShopLotteryEntryType(table);
    }

    @Override
    public SellType getSellType() {
        return SellType.ONLY_BUY;
    }

    @Override
    public Component getTranslatableForCreativeMenu() {
        return Component.translatable("sdmshopextended.shop.entry.type.lottery");
    }

    @Override
    public List<Component> getDescriptionForContextMenu() {
        List<Component> list = new ArrayList<>();
        list.add(Component.translatable("sdmshopextended.shop.entry.type.lottery.description"));
        if (table != null) {
            list.add(Component.translatable("sdmshopextended.shop.entry.type.lottery.reward_table")
                    .append(": ")
                    .append(table.getTitleOrElse(Component.literal("null")).copy().withStyle(ChatFormatting.BLUE)));
        }
        return list;
    }

    @Override
    public void sendNotifiedMessage(Player player) {
        if (shopEntry.isSell) {
            player.displayClientMessage(Component.translatable("sdmshopextended.shop.entry.type.lottery.cannot_sell")
                    .withStyle(ChatFormatting.RED), false);
        }
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public Icon getIcon() {
        if (table != null) {
            if (giveCrateInstead && table.getLootCrate() != null) {
                return ItemIcon.getItemIcon(table.getLootCrate().createStack());
            } else {
                List<WeightedReward> rewards = table.getWeightedRewards();
                if (!rewards.isEmpty()) {
                    Reward r = rewards.get(playerRandomIndex(rewards)).getReward();
                    if (r instanceof ItemReward itemReward) {
                        return ItemIcon.getItemIcon(itemReward.getItem());
                    }
                }
                return ItemIcon.getItemIcon(new ItemStack(Items.CHEST));
            }
        }
        return ItemIcon.getItemIcon(new ItemStack(Items.CHEST));
    }

    private int playerRandomIndex(List<?> list) {
        if (list.isEmpty()) return 0;
        Random rand = new Random();
        return rand.nextInt(list.size());
    }

    @Override
    public void buy(Player player, int countBuy, AbstractShopEntry entry) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        if (table == null) {
            serverPlayer.displayClientMessage(Component.translatable("sdmshopextended.shop.entry.type.lottery.error.no_table")
                    .withStyle(ChatFormatting.RED), false);
            return;
        }

        long playerMoney = SDMShopR.getMoney(player);
        long needMoney = entry.entryPrice * countBuy;

        boolean success;
        if (giveCrateInstead) {
            success = giveCrates(serverPlayer, table, countBuy);
        } else {
            success = executeLotteryDraw(serverPlayer, table, countBuy);
        }

        if (success) {
            SDMShopR.setMoney(player, playerMoney - needMoney);
            serverPlayer.displayClientMessage(Component.translatable("sdmshopextended.shop.entry.type.lottery.success")
                    .withStyle(ChatFormatting.GREEN), false);
        } else {
            serverPlayer.displayClientMessage(Component.translatable("sdmshopextended.shop.entry.type.lottery.fail")
                    .withStyle(ChatFormatting.YELLOW), false);
        }
    }

    private boolean executeLotteryDraw(ServerPlayer player, RewardTable table, int totalDraws) {
        if (table == null || totalDraws <= 0) return false;

        try {
            Collection<WeightedReward> results = table.generateWeightedRandomRewards(player.getRandom(), totalDraws, false);
            if (results.isEmpty()) return false;

            for (WeightedReward wr : results) {
                wr.getReward().claim(player, true);
            }
            return true;
        } catch (Exception e) {
            SDMShopRework.LOGGER.error("Failed to execute lottery draw for player " + player.getName().getString(), e);
            return false;
        }
    }

    private boolean giveCrates(ServerPlayer player, RewardTable table, int count) {
        if (table.getLootCrate() == null) return false;

        ItemStack stack = table.getLootCrate().createStack();
        stack.setCount(count);

        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        return true;
    }

    @Override
    public void sell(Player player, int countSell, AbstractShopEntry entry) {
        player.sendSystemMessage(Component.translatable("sdmshopextended.shop.entry.type.lottery.cannot_sell")
                .withStyle(ChatFormatting.RED));
    }

    @Override
    public boolean canExecute(Player player, boolean isSell, int countSell, AbstractShopEntry entry) {
        if (isSell) return false;
        if (table == null) return false;

        long playerMoney = SDMShopR.getMoney(player);
        long needMoney = entry.entryPrice * countSell;
        return playerMoney >= needMoney;
    }

    @Override
    public int howMany(Player player, boolean isSell, AbstractShopEntry entry) {
        if (isSell) return 0;
        if (table == null || entry.entryPrice == 0) return 0;

        long playerMoney = SDMShopR.getMoney(player);
        return (int) (playerMoney / entry.entryPrice);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("shopEntryTypeID", ID);
        if (table != null) {
            nbt.putLong("table_id", table.id);
        }
        nbt.putBoolean("give_crate", giveCrateInstead);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        nbt.getString(ID);
        if (nbt.contains("table_id")) {
            long id = nbt.getLong("table_id");
            if (id != -1L) {
                table = ServerQuestFile.INSTANCE.getRewardTable(id);
            } else {
                table = null;
            }
        } else {
            table = null;
        }
        giveCrateInstead = nbt.getBoolean("give_crate");
    }

    public static class Constructor implements IConstructor<AbstractShopEntryType> {
        @Override
        public ShopLotteryEntryType createDefaultInstance() {
            return new ShopLotteryEntryType(null);
        }
    }
}