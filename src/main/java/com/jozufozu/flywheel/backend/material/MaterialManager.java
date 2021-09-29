package com.jozufozu.flywheel.backend.material;

import com.jozufozu.flywheel.backend.state.IRenderState;
import com.jozufozu.flywheel.backend.state.RenderLayer;
import com.jozufozu.flywheel.backend.state.TextureRenderState;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.core.Vec3i;

public interface MaterialManager {

	/**
	 * Get a material group that will render in the given layer with the given state.
	 *
	 * @param layer The {@link RenderLayer} you want to draw in.
	 * @param state The {@link IRenderState} you need to draw with.
	 * @return A material group whose children will
	 */
	MaterialGroup state(RenderLayer layer, IRenderState state);

	Vec3i getOriginCoordinate();

	default MaterialGroup solid(IRenderState state) {
		return state(RenderLayer.SOLID, state);
	}

	default MaterialGroup cutout(IRenderState state) {
		return state(RenderLayer.CUTOUT, state);
	}

	default MaterialGroup transparent(IRenderState state) {
		return state(RenderLayer.TRANSPARENT, state);
	}

	default MaterialGroup defaultSolid() {
		return solid(TextureRenderState.get(InventoryMenu.BLOCK_ATLAS));
	}

	default MaterialGroup defaultCutout() {
		return cutout(TextureRenderState.get(InventoryMenu.BLOCK_ATLAS));
	}

	default MaterialGroup defaultTransparent() {
		return transparent(TextureRenderState.get(InventoryMenu.BLOCK_ATLAS));
	}
}
