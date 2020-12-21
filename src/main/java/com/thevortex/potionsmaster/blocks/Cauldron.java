package com.thevortex.potionsmaster.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.thevortex.potionsmaster.entity.TileEntityCauldron;
import com.thevortex.potionsmaster.init.ModItems;
import com.thevortex.potionsmaster.init.ModPotions;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.CauldronBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.server.command.TextComponentHelper;

import static net.minecraft.block.CampfireBlock.LIT;

public class Cauldron extends CauldronBlock {


	public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_0_3;
	public VoxelShape blockshape = Block.makeCuboidShape(4.0D, 0.0D, 4.0D, 12.0D, 2.0D, 12.0D);

	public Cauldron() {
		super(Properties.create(Material.GLASS).sound(SoundType.STONE).hardnessAndResistance(2.0f));
		this.setDefaultState(this.stateContainer.getBaseState().with(LEVEL, Integer.valueOf(0)));

	}

	public static boolean isOpaque(VoxelShape shape) {
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	private static void spawnParticles(World world, BlockPos worldIn, int intensity) {
		Random random = world.rand;

		for (Direction direction : Direction.values()) {
			BlockPos blockpos = worldIn.offset(direction);
			if (!world.getBlockState(blockpos).isOpaqueCube(world, blockpos)) {
				Direction.Axis direction$axis = direction.getAxis();
				double d1 = direction$axis == Direction.Axis.X ? 0.5D + 0.5625D * (double) direction.getXOffset() : (double) random.nextFloat();
				double d2 = direction$axis == Direction.Axis.Y ? 0.5D + 0.5625D * (double) direction.getYOffset() : (double) random.nextFloat();
				double d3 = direction$axis == Direction.Axis.Z ? 0.5D + 0.5625D * (double) direction.getZOffset() : (double) random.nextFloat();


				if (intensity == 3) {
					world.addParticle(new RedstoneParticleData(0F, 1F, 0F, 1F), (double) worldIn.getX() + d1, (double) worldIn.getY() + d2, (double) worldIn.getZ() + d3, 0.0D, 0.0D, 0.0D);

				}
			}
		}
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (placer instanceof ServerPlayerEntity) {
			if (!(worldIn.getBlockState(pos.down()).getBlock() instanceof CampfireBlock) && (state.getBlock() instanceof Cauldron)) {
				worldIn.destroyBlock(pos, true);

			} else {

			}

			super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		}
	}

	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {

		super.tick(state, worldIn, pos, rand);
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {

		return PushReaction.DESTROY;
	}

	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
											 Hand handIn, BlockRayTraceResult p_225533_6_) {
		if (!worldIn.isRemote()) {
			ServerPlayerEntity player2 = (ServerPlayerEntity) player;
			ItemStack itemstack = player2.getHeldItem(handIn);
			TileEntityCauldron cauldron = (TileEntityCauldron) worldIn.getTileEntity(pos);
			if (itemstack.isEmpty()) {
				cauldron.resetAll();
				return ActionResultType.PASS;
			} else {
				int i = state.get(LEVEL);
				if (i == 3) {
					return ActionResultType.PASS;
				}
				Item item = itemstack.getItem();

				Cauldron cauldronBlock = (Cauldron) worldIn.getBlockState(pos).getBlock();
				if (item == Items.POTION) {

					List<EffectInstance> effects = PotionUtils.getEffectsFromStack(itemstack);
					if (effects.isEmpty()) {
						return ActionResultType.PASS;
					}

					if (!(cauldron.INPUT_A.isEmpty()) && (!(cauldron.INPUT_B.isEmpty()))) {
						return ActionResultType.PASS;
					}
					if (cauldron.INPUT_A.isEmpty()) {
						for (EffectInstance e : effects) {
							cauldron.INPUT_A.add(e);
						}
						ItemStack itemstack3 = new ItemStack(Items.GLASS_BOTTLE);
						player2.setHeldItem(handIn, itemstack3);
						cauldronBlock.setWaterLevel(worldIn, pos, state, 1);
						return ActionResultType.SUCCESS;
					}
					if (!(cauldron.INPUT_A.isEmpty()) && (cauldron.INPUT_B.isEmpty() == true)) {
						for (EffectInstance e : effects) {
							cauldron.INPUT_B.add(e);
						}
						ItemStack itemstack3 = new ItemStack(Items.GLASS_BOTTLE);
						player2.setHeldItem(handIn, itemstack3);
						cauldron.goForReady();

						cauldronBlock.setWaterLevel(worldIn, pos, state, 2);
						return ActionResultType.SUCCESS;
					}

				}
				if (!(cauldron.INPUT_A.isEmpty()) && (item == Items.GLOWSTONE_DUST) && !(cauldron.INPUT_B.isEmpty())) {
					cauldron.goForReady();
					itemstack.setCount(itemstack.getCount() - 1);
					cauldron.giveCatalyst();
					cauldronBlock.setWaterLevel(worldIn, pos, state, 3);
					cauldron.startBrewing();
				}
				if ((item == Items.GLASS_BOTTLE) && (cauldron.isComplete())) {
					ItemStack itemstack2 = new ItemStack(Items.POTION);

					PotionUtils.appendEffects(itemstack2, cauldron.getOutput());
					itemstack2.setDisplayName((ITextComponent) TextComponentHelper.createComponentTranslation(player2, "Blended Potion", new Object()));
					player2.setHeldItem(handIn, itemstack2.copy());
					cauldronBlock.setWaterLevel(worldIn, pos, state, i - 1);
					cauldron.resetAll();
				}

			}
		}
		return ActionResultType.SUCCESS;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return this.blockshape;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (((TileEntityCauldron) worldIn.getTileEntity(pos)).isComplete()) {
			spawnParticles(worldIn, pos, stateIn.get(LEVEL));
		}

	}

	@Deprecated
	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		List<ItemStack> list = new ArrayList<ItemStack>();
		list.add(new ItemStack(ModItems.ITEM_CAULDRON));
		return list;
	}

	@Nullable
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new TileEntityCauldron((World) world, state);

	}
}
