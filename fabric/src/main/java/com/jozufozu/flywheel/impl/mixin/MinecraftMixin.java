package com.jozufozu.flywheel.impl.mixin;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.api.event.EndClientResourceReloadCallback;
import com.jozufozu.flywheel.impl.FabricFlwConfig;
import com.jozufozu.flywheel.impl.FlwImpl;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {
	@Shadow
	@Final
	private ReloadableResourceManager resourceManager;

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;createReload(Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Ljava/util/List;)Lnet/minecraft/server/packs/resources/ReloadInstance;"))
	private void flywheel$onBeginInitialResourceReload(CallbackInfo ci) {
		FlwImpl.freezeRegistries();
		// Load the config after we freeze registries,
		// so we can find third party backends.
		FabricFlwConfig.INSTANCE.load();
	}

	@Inject(method = "method_53522", at = @At("HEAD"))
	private void flywheel$onEndInitialResourceReload(@Coerce Object gameLoadCookie, Optional<Throwable> error, CallbackInfo ci) {
		EndClientResourceReloadCallback.EVENT.invoker()
				.onEndClientResourceReload((Minecraft) (Object) this, resourceManager, true, error);
	}

	@Inject(method = "method_24228", at = @At("HEAD"))
	private void flywheel$onEndManualResourceReload(boolean recovery, @Coerce Object gameLoadCookie, CompletableFuture<Void> future,
													Optional<Throwable> error, CallbackInfo ci) {
		EndClientResourceReloadCallback.EVENT.invoker()
				.onEndClientResourceReload((Minecraft) (Object) this, resourceManager, false, error);
	}
}
