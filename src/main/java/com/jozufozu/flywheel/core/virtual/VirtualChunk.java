package com.jozufozu.flywheel.core.virtual;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.util.Mods;

import ca.spottedleaf.starlight.common.chunk.ExtendedChunk;
import ca.spottedleaf.starlight.common.light.StarLightEngine;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.TickContainerAccess;

public class VirtualChunk extends ChunkAccess {
	public final VirtualRenderWorld world;

	private final VirtualChunkSection[] sections;

	private boolean needsLight;

	public VirtualChunk(VirtualRenderWorld world, int x, int z) {
		super(new ChunkPos(x, z), UpgradeData.EMPTY, world, world.registryAccess()
			.registry(Registry.BIOME_REGISTRY)
			.orElseThrow(), 0L, null, null);

		this.world = world;

		int sectionCount = world.getSectionsCount();
		this.sections = new VirtualChunkSection[sectionCount];

		for (int i = 0; i < sectionCount; i++) {
			sections[i] = new VirtualChunkSection(this, i << 4);
		}

		this.needsLight = true;

		Mods.STARLIGHT.executeIfInstalled(() -> () -> {
			((ExtendedChunk) this).setBlockNibbles(StarLightEngine.getFilledEmptyLight(this));
			((ExtendedChunk) this).setSkyNibbles(StarLightEngine.getFilledEmptyLight(this));
		});
	}

	@Override
	@Nullable
	public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
		return null;
	}

	@Override
	public void setBlockEntity(BlockEntity blockEntity) {
	}

	@Override
	public void addEntity(Entity entity) {
	}

	@Override
	public Set<BlockPos> getBlockEntitiesPos() {
		return Collections.emptySet();
	}

	@Override
	public LevelChunkSection[] getSections() {
		return sections;
	}

	@Override
	public Collection<Map.Entry<Heightmap.Types, Heightmap>> getHeightmaps() {
		return Collections.emptySet();
	}

	@Override
	public void setHeightmap(Heightmap.Types type, long[] data) {
	}

	@Override
	public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types type) {
		return null;
	}

	@Override
	public int getHeight(Heightmap.Types type, int x, int z) {
		return 0;
	}

	@Override
	@Nullable
	public StructureStart getStartForFeature(ConfiguredStructureFeature<?, ?> structure) {
		return null;
	}

	@Override
	public void setStartForFeature(ConfiguredStructureFeature<?, ?> structure, StructureStart start) {
	}

	@Override
	public Map<ConfiguredStructureFeature<?, ?>, StructureStart> getAllStarts() {
		return Collections.emptyMap();
	}

	@Override
	public void setAllStarts(Map<ConfiguredStructureFeature<?, ?>, StructureStart> structureStarts) {
	}

	@Override
	public LongSet getReferencesForFeature(ConfiguredStructureFeature<?, ?> structure) {
		return LongSets.emptySet();
	}

	@Override
	public void addReferenceForFeature(ConfiguredStructureFeature<?, ?> structure, long reference) {
	}

	@Override
	public Map<ConfiguredStructureFeature<?, ?>, LongSet> getAllReferences() {
		return Collections.emptyMap();
	}

	@Override
	public void setAllReferences(Map<ConfiguredStructureFeature<?, ?>, LongSet> structureReferences) {
	}

	@Override
	public void setUnsaved(boolean unsaved) {
	}

	@Override
	public boolean isUnsaved() {
		return false;
	}

	@Override
	public ChunkStatus getStatus() {
		return ChunkStatus.LIGHT;
	}

	@Override
	public void removeBlockEntity(BlockPos pos) {
	}

	@Override
	public ShortList[] getPostProcessing() {
		return new ShortList[0];
	}

	@Override
	@Nullable
	public CompoundTag getBlockEntityNbt(BlockPos pos) {
		return null;
	}

	@Override
	@Nullable
	public CompoundTag getBlockEntityNbtForSaving(BlockPos pos) {
		return null;
	}

	@Override
	public Stream<BlockPos> getLights() {
		return world.blockStates.entrySet()
			.stream()
			.filter(it -> {
				BlockPos blockPos = it.getKey();
				boolean chunkContains = SectionPos.blockToSectionCoord(blockPos.getX()) == chunkPos.x && SectionPos.blockToSectionCoord(blockPos.getZ()) == chunkPos.z;
				return chunkContains && it.getValue()
					.getLightEmission(world, blockPos) != 0;
			})
			.map(Map.Entry::getKey);
	}

	@Override
	public TickContainerAccess<Block> getBlockTicks() {
		return BlackholeTickAccess.emptyContainer();
	}

	@Override
	public TickContainerAccess<Fluid> getFluidTicks() {
		return BlackholeTickAccess.emptyContainer();
	}

	@Override
	public TicksToSave getTicksForSerialization() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getInhabitedTime() {
		return 0;
	}

	@Override
	public void setInhabitedTime(long amount) {
	}

	@Override
	public boolean isLightCorrect() {
		return needsLight;
	}

	@Override
	public void setLightCorrect(boolean lightCorrect) {
		this.needsLight = lightCorrect;
	}

	@Override
	@Nullable
	public BlockEntity getBlockEntity(BlockPos pos) {
		return world.getBlockEntity(pos);
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return world.getBlockState(pos);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return world.getFluidState(pos);
	}
}
