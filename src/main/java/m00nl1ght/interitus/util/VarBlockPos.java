package m00nl1ght.interitus.util;

import java.util.Iterator;

import com.google.common.collect.AbstractIterator;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

public class VarBlockPos extends BlockPos {
	
	public static final VarBlockPos PUBLIC_CACHE = new VarBlockPos();
    protected int x, y, z;

	public VarBlockPos() {
		this(0, 0, 0);
	}

	public VarBlockPos(BlockPos pos) {
		this(pos.getX(), pos.getY(), pos.getZ());
	}

	public VarBlockPos(int x, int y, int z) {
		super(x, y, z);
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public VarBlockPos varAdd(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public VarBlockPos varAdd(int x, int y, int z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public BlockPos varAdd(Vec3i vec) {
		return this.varAdd(vec.getX(), vec.getY(), vec.getZ());
	}

	public BlockPos varSubtract(Vec3i vec) {
		return this.varAdd(-vec.getX(), -vec.getY(), -vec.getZ());
	}

	public VarBlockPos varOffset(EnumFacing facing, int n) {
		this.x += facing.getFrontOffsetX() * n;
		this.y += facing.getFrontOffsetY() * n;
		this.z += facing.getFrontOffsetZ() * n;
		return this;
	}

	public VarBlockPos varRotate(Rotation rotation) {
		switch (rotation) {
			case CLOCKWISE_90:
				int ox = x;
				this.x = -z;
				this.z = ox;
				return this;
			case CLOCKWISE_180:
				this.x = -x;
				this.z = -z;
				return this;
			case COUNTERCLOCKWISE_90:
				int ux = x;
				this.x = z;
				this.z = -ux;
				return this;
			default:
				return this;
		}
	}

	@Override
	public int getX() {
		return this.x;
	}

	@Override
	public int getY() {
		return this.y;
	}

	@Override
	public int getZ() {
		return this.z;
	}
	
	public int inChunkX() {
		return this.x & 15;
	}
	
	public int inChunkZ() {
		return this.z & 15;
	}
	
	public int chunkX() {
		return this.x >> 4;
	}
	
	public int chunkZ() {
		return this.z >> 4;
	}
	
	public void setY(int y) {
		this.y = y;
	}

	public VarBlockPos set(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public VarBlockPos set(double x, double y, double z) {
		this.x = MathHelper.floor(x);
		this.y = MathHelper.floor(y);
		this.z = MathHelper.floor(z);
		return this;
	}

	public VarBlockPos set(Entity entityIn) {
		return this.set(entityIn.posX, entityIn.posY, entityIn.posZ);
	}

	public VarBlockPos set(Vec3i vec) {
		this.x = vec.getX();
		this.y = vec.getY();
		this.z = vec.getZ();
		return this;
	}
	
	public VarBlockPos setAdd(Vec3i pos1, Vec3i pos2) {
		this.x = pos1.getX() + pos2.getX();
		this.y = pos1.getY() + pos2.getY();
		this.z = pos1.getZ() + pos2.getZ();
		return this;
	}
	
	public VarBlockPos setSubtract(Vec3i pos1, Vec3i pos2) {
		this.x = pos1.getX() - pos2.getX();
		this.y = pos1.getY() - pos2.getY();
		this.z = pos1.getZ() - pos2.getZ();
		return this;
	}
	
	public VarBlockPos reset() {
		return reset(0, 0, 0);
	}
	
	public VarBlockPos reset(int offsetX, int offsetY, int offsetZ) {
		this.x = super.getX() + offsetX;
		this.y = super.getY() + offsetY;
		this.z = super.getZ() + offsetZ;
		return this;
	}
	
	public VarBlockPos reset(Vec3i offset) {
		return this.reset(offset.getX(), offset.getY(), offset.getZ());
	}

	public int[] toArray() {
		return new int[] {this.x, this.y, this.z};
	}
	
	@Override
	public BlockPos toImmutable() {
		return new BlockPos(this);
	}
	
	public static Iterable<VarBlockPos> getBoxIterator(BlockPos from, BlockPos to) {
		return getBoxIterator(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()), Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
	}

	public static Iterable<VarBlockPos> getBoxIterator(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {
		return new Iterable<VarBlockPos>() {

			@Override
			public Iterator<VarBlockPos> iterator() {
				return new AbstractIterator<VarBlockPos>() {

					private VarBlockPos pos;

					@Override
					protected VarBlockPos computeNext() {
						if (pos==null) {return pos = new VarBlockPos(x1, y1, z1);}
						if (pos.x < x2) {pos.x++;} else {
							pos.x=x1;
							if (pos.y < y2) {pos.y++;} else {
								pos.y=y1;
								if (pos.z < z2) {pos.z++;} else {
									return this.endOfData();
								}
							}
						}
						return pos;
					}
				};
			}
		};
	}

}
