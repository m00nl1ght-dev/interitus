package m00nl1ght.interitus.structures;

import m00nl1ght.interitus.util.VarBlockPos;
import m00nl1ght.interitus.world.InteritusChunkGenerator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class Condition {
	
	public final VarBlockPos pos;
	public final ConditionType type;
	public final boolean negate;
	
	public Condition(ConditionType type, BlockPos pos, boolean negate) {
		this.type=type; this.pos=new VarBlockPos(pos); this.negate = negate;
	}
	
	public boolean apply(InteritusChunkGenerator gen, BlockPos origin) {
		this.pos.reset(origin);
		return this.type.apply(gen, this.pos);
	}
	
	public Condition toAbsolute(BlockPos structurePos) {
		this.pos.reset();
		return new Condition(this.type, this.pos.varSubtract(structurePos), this.negate);
	}

	public Condition toRelative(BlockPos structurePos) {
		this.pos.reset();
		return new Condition(this.type, this.pos.varAdd(structurePos), this.negate);
	}

	public NBTTagCompound writeToNbt(NBTTagCompound tag) {
		this.pos.reset();
		tag.setInteger("x", pos.getX());
		tag.setInteger("y", pos.getY());
		tag.setInteger("z", pos.getZ());
		tag.setBoolean("n", this.negate);
		tag.setString("id", type.getName());
		return tag;
	}
	
	public static Condition readFromNBT(StructurePack pack, NBTTagCompound tag) {
		ConditionType type = pack.getConditionType(tag.getString("id"));
		if (type==null) {throw new IllegalStateException("Failed toload condition from nbt, condition type not found: "+tag.getString("id"));}
		return new Condition(type, new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z")), tag.getBoolean("n"));
	}
	
	@Override
	public String toString() {
		return pos.toString()+" "+type.getName();
	}

}
