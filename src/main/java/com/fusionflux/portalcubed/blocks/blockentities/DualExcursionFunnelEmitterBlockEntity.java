package com.fusionflux.portalcubed.blocks.blockentities;

import com.fusionflux.portalcubed.blocks.PortalCubedBlocks;
import com.fusionflux.portalcubed.blocks.properties.PortalCubedProperties;
import com.fusionflux.portalcubed.entity.Portal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


public class DualExcursionFunnelEmitterBlockEntity extends AbstractExcursionFunnelEmitterBlockEntity {

    public DualExcursionFunnelEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(PortalCubedBlocks.DUAL_EXCURSION_FUNNEL_EMITTER_ENTITY, pos, state);
    }

    public static void tick2(Level world, BlockPos pos, @SuppressWarnings("unused") BlockState state, DualExcursionFunnelEmitterBlockEntity blockEntity) {
        if (!world.isClientSide) {
            boolean redstonePowered = world.hasNeighborSignal(blockEntity.getBlockPos());

            if (!redstonePowered) {

                if (world.getBlockState(pos).getValue(BlockStateProperties.POWERED)) {
                    blockEntity.togglePowered(world.getBlockState(pos));
                    blockEntity.dualTogglePowered(world.getBlockState(pos));
                }

                BlockPos translatedPos = pos;
                BlockPos savedPos = pos;
                if (blockEntity.funnels != null) {
                    Set<BlockPos> modFunnels = new HashSet<>();
                    Set<BlockPos> portalFunnels = new HashSet<>();
                    boolean teleported = false;
                    Direction storedDirection = blockEntity.getBlockState().getValue(BlockStateProperties.FACING);
                    for (int i = 0; i <= blockEntity.maxRange; i++) {
                        if (!teleported) {
                            translatedPos = translatedPos.relative(storedDirection);
                        } else {
                            teleported = false;
                        }

                        if (translatedPos.getY() < world.getMaxBuildHeight() && translatedPos.getY() > world.getMinBuildHeight() && (world.isEmptyBlock(translatedPos) || (world.getBlockState(translatedPos).getDestroySpeed(world, translatedPos) <= 0.1F && world.getBlockState(translatedPos).getDestroySpeed(world, translatedPos) != -1F) || world.getBlockState(translatedPos).getBlock().equals(PortalCubedBlocks.EXCURSION_FUNNEL)) && !world.getBlockState(translatedPos).getBlock().equals(Blocks.BARRIER)) {

                            world.setBlockAndUpdate(translatedPos, PortalCubedBlocks.EXCURSION_FUNNEL.defaultBlockState());

                            ExcursionFunnelMainBlockEntity funnel = ((ExcursionFunnelMainBlockEntity) Objects.requireNonNull(world.getBlockEntity(translatedPos)));

                            modFunnels.add(funnel.getBlockPos());
                            blockEntity.funnels.add(funnel.getBlockPos());
                            if (!savedPos.equals(pos)) {
                                portalFunnels.add(funnel.getBlockPos());
                                blockEntity.portalFunnels.add(funnel.getBlockPos());
                            }

                            if (!funnel.facing.contains(storedDirection)) {
                                funnel.facing.add(storedDirection);
                                funnel.emitters.add(funnel.facing.indexOf(storedDirection), pos);
                                funnel.portalEmitters.add(funnel.facing.indexOf(storedDirection), savedPos);
                            }

                            funnel.updateState(world.getBlockState(translatedPos), world, translatedPos, funnel);

                            AABB portalCheckBox = new AABB(translatedPos);

                            List<Portal> list = world.getEntitiesOfClass(Portal.class, portalCheckBox);


                            for (Portal portal : list) {
                                if (portal.getFacingDirection().getOpposite().equals(storedDirection) && portal.getActive()) {
                                    assert portal.getOtherAxisH().isPresent();
                                    assert portal.getOtherNormal().isPresent();
                                    assert portal.getDestination().isPresent();

                                    Direction otherPortalVertFacing = Direction.fromNormal(BlockPos.containing(portal.getOtherAxisH().get().x, portal.getOtherAxisH().get().y, portal.getOtherAxisH().get().z));
                                    int offset = (int)(((portal.blockPosition().getX() - translatedPos.getX()) * Math.abs(portal.getAxisH().x)) + ((portal.blockPosition().getY() - translatedPos.getY()) * Math.abs(portal.getAxisH().y)) + ((portal.blockPosition().getZ() - translatedPos.getZ()) * Math.abs(portal.getAxisH().z)));
                                    Direction mainPortalVertFacing = Direction.fromNormal(BlockPos.containing(portal.getAxisH().x, portal.getAxisH().y, portal.getAxisH().z));
                                    assert mainPortalVertFacing != null;
                                    if (mainPortalVertFacing.equals(Direction.SOUTH)) {
                                        offset = (Math.abs(offset) - 1) * -1;
                                    }
                                    if (mainPortalVertFacing.equals(Direction.EAST)) {
                                        offset = (Math.abs(offset) - 1) * -1;
                                    }

                                    translatedPos = BlockPos.containing(portal.getDestination().get().x, portal.getDestination().get().y, portal.getDestination().get().z).relative(otherPortalVertFacing, offset);
                                    savedPos = translatedPos;
                                    assert otherPortalVertFacing != null;
                                    if (otherPortalVertFacing.equals(Direction.SOUTH)) {
                                        translatedPos = translatedPos.relative(Direction.NORTH, 1);
                                    }
                                    if (otherPortalVertFacing.equals(Direction.EAST)) {
                                        translatedPos = translatedPos.relative(Direction.WEST, 1);
                                    }

                                    final Vec3 otherNormal = portal.getOtherNormal().get();
                                    storedDirection = Direction.fromNormal((int)otherNormal.x, (int)otherNormal.y, (int)otherNormal.z);
                                    teleported = true;
                                    blockEntity.funnels = modFunnels;
                                    blockEntity.portalFunnels = portalFunnels;
                                }
                            }
                        } else {
                            blockEntity.funnels = modFunnels;
                            blockEntity.portalFunnels = portalFunnels;
                            break;
                        }
                    }
                }

            }

            if (redstonePowered) {
                if (!world.getBlockState(pos).getValue(BlockStateProperties.POWERED)) {
                    blockEntity.togglePowered(world.getBlockState(pos));
                    blockEntity.dualTogglePowered(world.getBlockState(pos));
                }


                BlockPos translatedPos = pos;
                BlockPos savedPos = pos;
                if (blockEntity.funnels != null) {
                    Set<BlockPos> modFunnels = new HashSet<>();
                    Set<BlockPos> portalFunnels = new HashSet<>();
                    boolean teleported = false;
                    Direction storedDirection = blockEntity.getBlockState().getValue(BlockStateProperties.FACING);
                    for (int i = 0; i <= blockEntity.maxRange; i++) {
                        if (!teleported) {
                            translatedPos = translatedPos.relative(storedDirection);
                        } else {
                            teleported = false;
                        }

                        if (translatedPos.getY() < world.getMaxBuildHeight() && translatedPos.getY() > world.getMinBuildHeight() && (world.isEmptyBlock(translatedPos) || world.getBlockState(translatedPos).getDestroySpeed(world, translatedPos) <= 0.1F || world.getBlockState(translatedPos).getBlock().equals(PortalCubedBlocks.EXCURSION_FUNNEL)) && !world.getBlockState(translatedPos).getBlock().equals(Blocks.BARRIER)) {

                            world.setBlockAndUpdate(translatedPos, PortalCubedBlocks.EXCURSION_FUNNEL.defaultBlockState());

                            ExcursionFunnelMainBlockEntity funnel = ((ExcursionFunnelMainBlockEntity) Objects.requireNonNull(world.getBlockEntity(translatedPos)));

                            modFunnels.add(funnel.getBlockPos());
                            blockEntity.funnels.add(funnel.getBlockPos());
                            if (!savedPos.equals(pos)) {
                                portalFunnels.add(funnel.getBlockPos());
                                blockEntity.portalFunnels.add(funnel.getBlockPos());
                            }
                            if (!funnel.facing.contains(storedDirection)) {
                                funnel.facing.add(storedDirection);
                                funnel.emitters.add(funnel.facing.indexOf(storedDirection), pos);
                                funnel.portalEmitters.add(funnel.facing.indexOf(storedDirection), savedPos);
                            }

                            funnel.updateState(world.getBlockState(translatedPos), world, translatedPos, funnel);

                            AABB portalCheckBox = new AABB(translatedPos);

                            List<Portal> list = world.getEntitiesOfClass(Portal.class, portalCheckBox);


                            for (Portal portal : list) {
                                if (portal.getFacingDirection().getOpposite().equals(storedDirection) && portal.getActive()) {
                                    assert portal.getOtherAxisH().isPresent();
                                    assert portal.getOtherNormal().isPresent();
                                    assert portal.getDestination().isPresent();

                                    Direction otherPortalVertFacing = Direction.fromNormal(BlockPos.containing(portal.getOtherAxisH().get().x, portal.getOtherAxisH().get().y, portal.getOtherAxisH().get().z));
                                    int offset = (int)(((portal.blockPosition().getX() - translatedPos.getX()) * Math.abs(portal.getAxisH().x)) + ((portal.blockPosition().getY() - translatedPos.getY()) * Math.abs(portal.getAxisH().y)) + ((portal.blockPosition().getZ() - translatedPos.getZ()) * Math.abs(portal.getAxisH().z)));
                                    Direction mainPortalVertFacing = Direction.fromNormal(BlockPos.containing(portal.getAxisH().x, portal.getAxisH().y, portal.getAxisH().z));
                                    assert mainPortalVertFacing != null;
                                    if (mainPortalVertFacing.equals(Direction.SOUTH)) {
                                        offset = (Math.abs(offset) - 1) * -1;
                                    }
                                    if (mainPortalVertFacing.equals(Direction.EAST)) {
                                        offset = (Math.abs(offset) - 1) * -1;
                                    }

                                    translatedPos = BlockPos.containing(portal.getDestination().get().x, portal.getDestination().get().y, portal.getDestination().get().z).relative(otherPortalVertFacing, offset);
                                    savedPos = translatedPos;
                                    assert otherPortalVertFacing != null;
                                    if (otherPortalVertFacing.equals(Direction.SOUTH)) {
                                        translatedPos = translatedPos.relative(Direction.NORTH, 1);
                                    }
                                    if (otherPortalVertFacing.equals(Direction.EAST)) {
                                        translatedPos = translatedPos.relative(Direction.WEST, 1);
                                    }

                                    final Vec3 otherNormal = portal.getOtherNormal().get();
                                    storedDirection = Direction.fromNormal((int)otherNormal.x, (int)otherNormal.y, (int)otherNormal.z);
                                    teleported = true;
                                    blockEntity.funnels = modFunnels;
                                    blockEntity.portalFunnels = portalFunnels;
                                }
                            }
                        } else {
                            blockEntity.funnels = modFunnels;
                            blockEntity.portalFunnels = portalFunnels;
                            break;
                        }
                    }
                }

            }

        }


    }

    public void dualTogglePowered(BlockState state) {
        assert level != null;
        level.setBlockAndUpdate(worldPosition, state.cycle(PortalCubedProperties.REVERSED));
    }
}
