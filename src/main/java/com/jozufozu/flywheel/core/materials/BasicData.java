package com.jozufozu.flywheel.core.materials;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.backend.instancing.InstanceData;

public abstract class BasicData extends InstanceData implements FlatLight {

	public byte blockLight;
	public byte skyLight;

	public byte r = (byte) 0xFF;
	public byte g = (byte) 0xFF;
	public byte b = (byte) 0xFF;
	public byte a = (byte) 0xFF;

	@Override
	public BasicData setBlockLight(int blockLight) {
		this.blockLight = (byte) (blockLight << 4);
		markDirty();
		return this;
	}

	@Override
	public BasicData setSkyLight(int skyLight) {
		this.skyLight = (byte) (skyLight << 4);
		markDirty();
		return this;
	}

	public BasicData setColor(int color) {
		return setColor(color, false);
	}

	public BasicData setColor(int color, boolean alpha) {
		byte r = (byte) ((color >> 16) & 0xFF);
		byte g = (byte) ((color >> 8) & 0xFF);
		byte b = (byte) (color & 0xFF);

		if (alpha) {
			byte a = (byte) ((color >> 24) & 0xFF);
			return setColor(r, g, b, a);
		} else {
			return setColor(r, g, b);
		}
	}

	public BasicData setColor(int r, int g, int b) {
		return setColor((byte) r, (byte) g, (byte) b);
	}

	public BasicData setColor(byte r, byte g, byte b) {
		this.r = r;
		this.g = g;
		this.b = b;
		markDirty();
		return this;
	}

	public BasicData setColor(byte r, byte g, byte b, byte a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		markDirty();
		return this;
	}

	@Override
	public void write(VecBuffer buf) {
		buf.putByteArray(new byte[]{blockLight, skyLight, r, g, b, a});
	}
}
