package io.github.shadowcube264.teamsoulhome;

import net.minecraft.world.item.Item;
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


    public TeamSoulhome() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
