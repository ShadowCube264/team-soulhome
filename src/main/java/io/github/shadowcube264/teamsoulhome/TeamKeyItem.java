package io.github.shadowcube264.teamsoulhome;

import javax.annotation.Nonnull;

import com.mojang.math.Vector3f;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.data.Team;

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

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onUseTick(Level world, LivingEntity livingEntity, ItemStack stack, int count)
    {
        if (livingEntity.level.isClientSide)
        {
            float percentage = MathUtils.clamp01((USE_TICKS_REQUIRED - count) / (float) USE_TICKS_REQUIRED);
            int particlesToCreate = Mth.floor((percentage * percentage * percentage) * USE_TICKS_REQUIRED);

            final float maxRadius = 5;
            float bits = 360f / particlesToCreate;
            float radius = percentage * maxRadius;

            for (int i = particlesToCreate; i >= 0; --i)
            {
                float ang = (bits * i);// + (Math.random() * 10);
                
                Team team = FTBTeamsAPI.getPlayerTeam(livingEntity.getUUID());

                if (team != null)
                {
                    Color4I teamColour = Color4I.rgb(team.getColor());
                    livingEntity.level.addParticle(
                            new DustParticleOptions(new Vector3f(teamColour.redf(),teamColour.greenf(),teamColour.bluef()),2),
                            livingEntity.getX() + Mth.sin(Mth.wrapDegrees(ang)) * radius,
                            livingEntity.getY(),
                            livingEntity.getZ() + Mth.cos(Mth.wrapDegrees(ang)) * radius,
                            0.0D,
                            0.0D,
                            0.0D);
                } else {
                    livingEntity.level.addParticle(
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
        if (!livingEntity.level.isClientSide && livingEntity instanceof Player)
        {
            //find all creatures in range
            AABB areaOfEffect = new AABB(livingEntity.blockPosition()).inflate(2.5d);
            List<Entity> entitiesInRange = world.getEntitiesOfClass(Entity.class, areaOfEffect);
            TeamDimensionHelper.FlipDimension((Player) livingEntity, livingEntity.getServer(), entitiesInRange);
        }

        return stack;
    }
}
