package com.jozufozu.flywheel.core.model;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Random;
import java.util.function.Supplier;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.model.BufferBuilderExtension;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.BufferBuilder.DrawState;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class ModelUtil {
	/**
	 * An alternative BlockRenderDispatcher that circumvents the Forge rendering pipeline to ensure consistency.
	 * Meant to be used for virtual rendering.
	 */
	public static final BlockRenderDispatcher VANILLA_RENDERER = createVanillaRenderer();

	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	private static BlockRenderDispatcher createVanillaRenderer() {
		BlockRenderDispatcher defaultDispatcher = Minecraft.getInstance().getBlockRenderer();
		BlockRenderDispatcher dispatcher = new BlockRenderDispatcher(null, null, null);
		try {
			for (Field field : BlockRenderDispatcher.class.getDeclaredFields()) {
				field.setAccessible(true);
				field.set(dispatcher, field.get(defaultDispatcher));
			}
			ObfuscationReflectionHelper.setPrivateValue(BlockRenderDispatcher.class, dispatcher, new ModelBlockRenderer(Minecraft.getInstance().getBlockColors()), "f_110900_");
		} catch (Exception e) {
			Flywheel.LOGGER.error("Failed to initialize vanilla BlockRenderDispatcher!", e);
			return defaultDispatcher;
		}
		return dispatcher;
	}

	public static ShadeSeparatedBufferedData endAndCombine(BufferBuilder shadedBuilder, BufferBuilder unshadedBuilder) {
		int unshadedStartVertex = ((BufferBuilderExtension) shadedBuilder).flywheel$getVertices();
		unshadedBuilder.end();
		Pair<DrawState, ByteBuffer> unshadedData = unshadedBuilder.popNextBuffer();
		((BufferBuilderExtension) shadedBuilder).flywheel$appendBufferUnsafe(unshadedData.getSecond());
		shadedBuilder.end();
		Pair<DrawState, ByteBuffer> data = shadedBuilder.popNextBuffer();
		return new ShadeSeparatedBufferedData.NativeImpl(data.getSecond(), data.getFirst(), unshadedStartVertex);
	}

	public static ShadeSeparatedBufferedData getBufferedData(Bufferable bufferable) {
		ModelBlockRenderer blockRenderer = VANILLA_RENDERER.getModelRenderer();
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

		objects.begin();

		bufferable.bufferInto(objects.shadeSeparatingWrapper, blockRenderer, objects.random);

		return objects.end();
	}

	public static ShadeSeparatedBufferedData getBufferedData(BakedModel model, BlockState referenceState) {
		return new BakedModelBuilder(model).withReferenceState(referenceState)
				.build();
	}

	public static ShadeSeparatedBufferedData getBufferedData(BakedModel model, BlockState referenceState, PoseStack poseStack) {
		return new BakedModelBuilder(model).withReferenceState(referenceState)
				.withPoseStack(poseStack)
				.build();
	}

	public static ShadeSeparatedBufferedData getBufferedData(BlockAndTintGetter renderWorld, BakedModel model, BlockState referenceState, PoseStack poseStack) {
		return new BakedModelBuilder(model).withReferenceState(referenceState)
				.withPoseStack(poseStack)
				.withRenderWorld(renderWorld)
				.build();
	}

	public static ShadeSeparatedBufferedData getBufferedDataFromTemplate(BlockAndTintGetter renderWorld, RenderType layer, Collection<StructureTemplate.StructureBlockInfo> blocks) {
		return new WorldModelBuilder(layer).withRenderWorld(renderWorld)
				.withBlocks(blocks)
				.build();
	}

	public static ShadeSeparatedBufferedData getBufferedDataFromTemplate(BlockAndTintGetter renderWorld, RenderType layer, Collection<StructureTemplate.StructureBlockInfo> blocks, PoseStack poseStack) {
		return new WorldModelBuilder(layer).withRenderWorld(renderWorld)
				.withBlocks(blocks)
				.withPoseStack(poseStack)
				.build();
	}

	public static Supplier<PoseStack> rotateToFace(Direction facing) {
		return () -> {
			PoseStack stack = new PoseStack();
			TransformStack.cast(stack)
					.centre()
					.rotateToFace(facing.getOpposite())
					.unCentre();
			return stack;
		};
	}

	private static class ThreadLocalObjects {
		public final Random random = new Random();
		public final ShadeSeparatingVertexConsumer shadeSeparatingWrapper = new ShadeSeparatingVertexConsumer();
		public final BufferBuilder shadedBuilder = new BufferBuilder(512);
		public final BufferBuilder unshadedBuilder = new BufferBuilder(512);

		private void begin() {
			this.shadedBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
			this.unshadedBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
			this.shadeSeparatingWrapper.prepare(this.shadedBuilder, this.unshadedBuilder);
		}

		private ShadeSeparatedBufferedData end() {
			this.shadeSeparatingWrapper.clear();
			return ModelUtil.endAndCombine(shadedBuilder, unshadedBuilder);
		}
	}
}
