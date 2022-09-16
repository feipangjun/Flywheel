package com.jozufozu.flywheel.mixin;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.jozufozu.flywheel.backend.instancing.batching.DrawBufferSet;
import com.jozufozu.flywheel.extension.RenderTypeExtension;

import net.minecraft.client.renderer.RenderType;

@Mixin(RenderType.class)
public class RenderTypeMixin implements RenderTypeExtension {
	@Unique
	private DrawBufferSet flywheel$drawBufferSet;

	@Override
	@NotNull
	public DrawBufferSet flywheel$getDrawBufferSet() {
		if (flywheel$drawBufferSet == null) {
			flywheel$drawBufferSet = new DrawBufferSet(((RenderType) (Object) this).format());
		}
		return flywheel$drawBufferSet;
	}
}
