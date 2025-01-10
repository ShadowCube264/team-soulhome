package io.github.shadowcube264.teamsoulhome;

import javax.annotation.Nonnull;

import org.joml.Vector3f;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI.API;
import dev.ftb.mods.ftbteams.api.client.ClientTeamManager;
import dev.ftb.mods.ftbteams.api.property.TeamProperties;

import java.util.List;

import leaf.soulhome.items.SoulKeyItem;
import leaf.soulhome.utils.MathUtils;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;

public class TeamKeyItem extends SoulKeyItem
{

    //Just "borrowing" some code ;)

    final int USE_TICKS_REQUIRED = 80;

    private API teamAPI = null;
    private ClientTeamManager clientManager = null;

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onUseTick(Level world, LivingEntity livingEntity, ItemStack stack, int count)
    {
        if (livingEntity.level().isClientSide)
        {
            if (teamAPI == null)
            {
                teamAPI = FTBTeamsAPI.api();
                clientManager = teamAPI.getClientManager();
            }
            if (!clientManager.isValid())
            {
                clientManager = teamAPI.getClientManager();
            }

            float percentage = MathUtils.clamp01((USE_TICKS_REQUIRED - count) / (float) USE_TICKS_REQUIRED);
            int particlesToCreate = Mth.floor((percentage * percentage * percentage) * USE_TICKS_REQUIRED);

            final float maxRadius = 5;
            float bits = 360f / particlesToCreate;
            float radius = percentage * maxRadius;

            Team team = clientManager.selfTeam();

            if (team != null)
            {
                Color4I teamColour = team.getProperty(TeamProperties.COLOR);
                Vector3f colorVec = new Vector3f(teamColour.redf(),teamColour.greenf(),teamColour.bluef());

                for (int i = particlesToCreate; i >= 0; --i)
                {
                    float ang = (bits * i);// + (Math.random() * 10);
                    
                    livingEntity.level().addParticle(
                            new DustParticleOptions(colorVec,2.0f),
                            livingEntity.getX() + Mth.sin(Mth.wrapDegrees(ang)) * radius,
                            livingEntity.getY(),
                            livingEntity.getZ() + Mth.cos(Mth.wrapDegrees(ang)) * radius,
                            0.0D,
                            0.0D,
                            0.0D);
                }
            } else {
                for (int i = particlesToCreate; i >= 0; --i)
                {
                    float ang = (bits * i);// + (Math.random() * 10);

                    livingEntity.level().addParticle(
                            ParticleTypes.SMOKE,
                            livingEntity.getX() + Mth.sin(Mth.wrapDegrees(ang)) * radius,
                            livingEntity.getY(),
                            livingEntity.getZ() + Mth.cos(Mth.wrapDegrees(ang)) * radius,
                            0.0D,
                            0.0D,
                            0.0D);
                }
            }
        }
    }

    @Nonnull
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity livingEntity)
    {
        if (!livingEntity.level().isClientSide && livingEntity instanceof Player)
        {
            //find all creatures in range
            AABB areaOfEffect = new AABB(livingEntity.blockPosition()).inflate(2.5d);
            List<Entity> entitiesInRange = world.getEntitiesOfClass(Entity.class, areaOfEffect);
            TeamDimensionHelper.FlipDimension((Player) livingEntity, livingEntity.getServer(), entitiesInRange);
        }

        return stack;
    }
}
