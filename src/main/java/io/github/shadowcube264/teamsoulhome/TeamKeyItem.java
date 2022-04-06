package io.github.shadowcube264.teamsoulhome;

import javax.annotation.Nonnull;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.data.Team;

import java.util.List;

import leaf.soulhome.items.SoulKeyItem;
import leaf.soulhome.utils.MathUtils;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;

public class TeamKeyItem extends SoulKeyItem
{

    //Just "borrowing" some code ;)

    final int USE_TICKS_REQUIRED = 80;

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onUseTick(World world, LivingEntity livingEntity, ItemStack stack, int count)
    {
        if (livingEntity.level.isClientSide)
        {
            float percentage = MathUtils.clamp01((USE_TICKS_REQUIRED - count) / (float) USE_TICKS_REQUIRED);
            int particlesToCreate = MathHelper.floor((percentage * percentage * percentage) * USE_TICKS_REQUIRED);

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
                            new RedstoneParticleData(teamColour.redf(),teamColour.greenf(),teamColour.bluef(),2),
                            livingEntity.getX() + MathHelper.sin(MathHelper.wrapDegrees(ang)) * radius,
                            livingEntity.getY(),
                            livingEntity.getZ() + MathHelper.cos(MathHelper.wrapDegrees(ang)) * radius,
                            0.0D,
                            0.0D,
                            0.0D);
                } else {
                    livingEntity.level.addParticle(
                            ParticleTypes.SMOKE,
                            livingEntity.getX() + MathHelper.sin(MathHelper.wrapDegrees(ang)) * radius,
                            livingEntity.getY(),
                            livingEntity.getZ() + MathHelper.cos(MathHelper.wrapDegrees(ang)) * radius,
                            0.0D,
                            0.0D,
                            0.0D);
                }
            }
        }
    }

    @Nonnull
    @Override
    public ItemStack finishUsingItem(ItemStack stack, World world, LivingEntity livingEntity)
    {
        if (!livingEntity.level.isClientSide && livingEntity instanceof PlayerEntity)
        {
            //find all creatures in range
            AxisAlignedBB areaOfEffect = new AxisAlignedBB(livingEntity.blockPosition()).inflate(2.5d);
            List<Entity> entitiesInRange = world.getEntitiesOfClass(Entity.class, areaOfEffect);
            TeamDimensionHelper.FlipDimension((PlayerEntity) livingEntity, livingEntity.getServer(), entitiesInRange);
        }

        return stack;
    }
}
