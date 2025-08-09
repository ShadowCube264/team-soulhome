package io.github.shadowcube264.teamsoulhome;

import javax.annotation.Nonnull;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamManager;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI.API;
import dev.ftb.mods.ftbteams.api.client.ClientTeamManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import leaf.soulhome.items.SoulKeyItem;
import leaf.soulhome.utils.CompoundNBTHelper;
import leaf.soulhome.utils.DimensionHelper;
import leaf.soulhome.utils.MathUtils;
import leaf.soulhome.utils.PlayerHelper;
import leaf.soulhome.utils.TextHelper;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;

public class LostSoulKeyItem extends SoulKeyItem
{

    final int USE_TICKS_REQUIRED = 80;

    private API teamAPI = null;
    private ClientTeamManager clientManager = null;
    private TeamManager teamManager = null;

    private UUID chosenID = null;

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(TextHelper.createTranslatedText("key.soulhome.soul.charge", new Object[0]));
        tooltip.add(Component.translatable("item.teamsoulhome.lost_soulkey.tooltip"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onUseTick(Level world, LivingEntity livingEntity, ItemStack stack, int count)
    {
        if (livingEntity.level().isClientSide)
        {
            if (teamAPI == null)
            {
                teamAPI = FTBTeamsAPI.api();
            }
            if (clientManager == null || !clientManager.isValid())
            {
                clientManager = teamAPI.getClientManager();
            }

            float percentage = MathUtils.clamp01((USE_TICKS_REQUIRED - count) / (float) USE_TICKS_REQUIRED);
            int particlesToCreate = Mth.floor((percentage * percentage * percentage) * USE_TICKS_REQUIRED);

            final float maxRadius = 5;
            float bits = 360f / particlesToCreate;
            float radius = percentage * maxRadius;

            for (int i = particlesToCreate; i >= 0; --i)
            {
                float ang = (bits * i);// + (Math.random() * 10);

                livingEntity.level().addParticle(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        livingEntity.getX() + Mth.sin(Mth.wrapDegrees(ang)) * radius,
                        livingEntity.getY(),
                        livingEntity.getZ() + Mth.cos(Mth.wrapDegrees(ang)) * radius,
                        0.0D,
                        0.0D,
                        0.0D);
            }
        }
    }

    @Nonnull
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity livingEntity)
    {
        if (!livingEntity.level().isClientSide && livingEntity instanceof Player && chosenID != null)
        {
            //find all creatures in range
            AABB areaOfEffect = new AABB(livingEntity.blockPosition()).inflate(2.5d);
            List<Entity> entitiesInRange = world.getEntitiesOfClass(Entity.class, areaOfEffect);
            TeamDimensionHelper.FlipDimension((Player) livingEntity, livingEntity.getServer(), entitiesInRange, chosenID);

            //Remove ID from playerdata
            CompoundTag playerTag = PlayerHelper.getPersistentTag((Player)livingEntity, "team-soulhome");
            ListTag uuidList = CompoundNBTHelper.getList(playerTag, "past-uuids", 11, false);
            uuidList.remove(NbtUtils.createUUID(chosenID));
            CompoundNBTHelper.setList(playerTag, "past-uuids", uuidList);

        }
        return stack;
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (playerIn instanceof ServerPlayer player) {

            //Check whether in a soul dimension first
            if (DimensionHelper.isInSoulDimension(player)) {
                player.sendSystemMessage(Component.translatable("message.teamsoulhome.wrong_dim"));
                return new InteractionResultHolder<ItemStack>(InteractionResult.FAIL, stack);
            }

            if (teamAPI == null)
            {
                teamAPI = FTBTeamsAPI.api();
            }
            if (teamManager == null)
            {
                teamManager = teamAPI.getManager();
            }

            //Get existing team IDs
            Collection<Team> teams = teamManager.getTeams();
            ArrayList<UUID> teamIDs = new ArrayList<>();

            for (Team team : teams) {
                teamIDs.add(team.getId());
            }

            //Get historic team IDs
            CompoundTag playerTag = PlayerHelper.getPersistentTag(player, "team-soulhome");
            ListTag uuidList = CompoundNBTHelper.getList(playerTag, "past-uuids", 11, false);
            ArrayList<UUID> allowedIDs = new ArrayList<>();

            //Find "lost" team IDs
            for (Tag idTag : uuidList) {
                UUID id = NbtUtils.loadUUID(idTag);
                if (!teamIDs.contains(id)) {
                    allowedIDs.add(id);
                }
            }

            if (!allowedIDs.isEmpty()) {
                chosenID = allowedIDs.get(worldIn.getRandom().nextIntBetweenInclusive(1, allowedIDs.size())-1);
                player.startUsingItem(handIn);
            } else {
                player.sendSystemMessage(Component.translatable("message.teamsoulhome.no_team"));
                return new InteractionResultHolder<ItemStack>(InteractionResult.FAIL, stack);
            }
        }

        return new InteractionResultHolder<ItemStack>(InteractionResult.CONSUME, stack);
    }
}
