package io.github.shadowcube264.teamsoulhome;

import java.util.UUID;

import dev.ftb.mods.ftbteams.api.event.PlayerJoinedPartyTeamEvent;
import dev.ftb.mods.ftbteams.api.event.TeamEvent;
import leaf.soulhome.utils.CompoundNBTHelper;
import leaf.soulhome.utils.PlayerHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod("teamsoulhome")
public class TeamSoulhome
{
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "teamsoulhome");
    public static final RegistryObject<Item> TEAM_KEY = ITEMS.register("teamkey", () -> new TeamKeyItem());
    public static final RegistryObject<Item> LOST_SOULKEY = ITEMS.register("lost_soulkey", () -> new LostSoulKeyItem());

    public TeamSoulhome() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::buildContents);
        TeamEvent.PLAYER_JOINED_PARTY.register(this::onTeamJoin);
        ITEMS.register(modBus);
    }

    //Add items to creative inventory
    @SubscribeEvent
    public void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(TEAM_KEY);
            event.accept(LOST_SOULKEY);
        }
    }

    //Store data about past teams
    public void onTeamJoin(PlayerJoinedPartyTeamEvent event) {
        ServerPlayer player = event.getPlayer();
        UUID teamID = event.getTeam().getId();
        IntArrayTag teamIDTag = NbtUtils.createUUID(teamID);
        
        CompoundTag playerTag = PlayerHelper.getPersistentTag(player, "team-soulhome");
        ListTag uuidList = CompoundNBTHelper.getList(playerTag, "past-uuids", 11, false);

        //No need to store the same UUID twice
        if (!uuidList.contains(teamIDTag)) {
            uuidList.add(teamIDTag);
            CompoundNBTHelper.setList(playerTag, "past-uuids", uuidList);
        }
    }
}
