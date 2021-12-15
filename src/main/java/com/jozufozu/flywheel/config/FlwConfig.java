package com.jozufozu.flywheel.config;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class FlwConfig {

	private static final FlwConfig INSTANCE = new FlwConfig();

	public final ClientConfig client;

	public FlwConfig() {
		Pair<ClientConfig, ForgeConfigSpec> client = new ForgeConfigSpec.Builder().configure(ClientConfig::new);

		this.client = client.getLeft();

		ModLoadingContext.get()
				.registerConfig(ModConfig.Type.CLIENT, client.getRight());
	}

	public static FlwConfig get() {
		return INSTANCE;
	}

	public boolean enabled() {
		return client.enabled.get();
	}

	public boolean debugNormals() {
		return client.debugNormals.get();
	}

	public static void init() {
	}

	public static class ClientConfig {
		public final BooleanValue enabled;
		public final ForgeConfigSpec.EnumValue<FlwEngine> engine;
		public final BooleanValue debugNormals;

		public ClientConfig(ForgeConfigSpec.Builder builder) {

			enabled = builder.comment("Enable or disable the entire engine")
					.define("enabled", true);

			engine = builder.comment("Enable or disable the entire engine")
					.defineEnum("backend", FlwEngine.GL33);

			debugNormals = builder.comment("Enable or disable a debug overlay that colors pixels by their normal")
					.define("debugNormals", false);
		}
	}
}
