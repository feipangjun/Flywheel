package com.jozufozu.flywheel.backend.instancing.indirect;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.material.Material;

public final class IndirectDraw<T extends InstancedPart> {
	final IndirectInstancer<T> instancer;
	final IndirectMeshPool.BufferedMesh mesh;
	final Material material;
	final RenderStage stage;
	int baseInstance = -1;

	boolean needsFullWrite = true;

	IndirectDraw(IndirectInstancer<T> instancer, Material material, RenderStage stage, IndirectMeshPool.BufferedMesh mesh) {
		this.instancer = instancer;
		this.material = material;
		this.stage = stage;
		this.mesh = mesh;
	}

	public void prepare(int baseInstance) {
		instancer.update();
		if (baseInstance == this.baseInstance) {
			needsFullWrite = false;
			return;
		}
		this.baseInstance = baseInstance;
		needsFullWrite = true;
	}

	void writeObjects(long objectPtr, long batchIDPtr, int batchID) {
		if (needsFullWrite) {
			instancer.writeFull(objectPtr, batchIDPtr, batchID);
		} else if (instancer.anyToUpdate) {
			instancer.writeSparse(objectPtr, batchIDPtr, batchID);
		}
		instancer.anyToUpdate = false;
	}

	public void writeIndirectCommand(long ptr) {
		var boundingSphere = mesh.mesh.getBoundingSphere();

		MemoryUtil.memPutInt(ptr, mesh.getIndexCount()); // count
		MemoryUtil.memPutInt(ptr + 4, 0); // instanceCount - to be incremented by the compute shader
		MemoryUtil.memPutInt(ptr + 8, 0); // firstIndex - all models share the same index buffer
		MemoryUtil.memPutInt(ptr + 12, mesh.getBaseVertex()); // baseVertex
		MemoryUtil.memPutInt(ptr + 16, baseInstance); // baseInstance

		boundingSphere.getToAddress(ptr + 20); // boundingSphere
	}
}
