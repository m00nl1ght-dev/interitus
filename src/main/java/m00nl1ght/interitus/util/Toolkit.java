package m00nl1ght.interitus.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.FMLCommonHandler;

public final class Toolkit {
	
	public static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("minecraft","textures/gui/widgets.png");

	public static void serverBroadcastMsg(String message) {
		MinecraftServer minecraftserver = FMLCommonHandler.instance().getMinecraftServerInstance();

		Iterator<?> iterator = minecraftserver.getPlayerList().getPlayers().iterator();
		while (iterator.hasNext()) {
			EntityPlayer entityplayer = (EntityPlayer)iterator.next();
			sendMessageToPlayer(entityplayer, message);
		}
	}

	public static void sendMessageToPlayer(EntityPlayer player, String message) {

		if (message.isEmpty()) {return;}
		String[] lines = message.split("\r\n");
		for (String s: lines) {
			player.sendMessage(new TextComponentString(s));
		}

	}
	
	public static boolean isPlayerOnServer(EntityPlayer player) {
		return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers().contains(player);
	}
	
	public static RayTraceResult rayTrace(EntityPlayer player, double blockReachDistance, float partialTicks) {
        Vec3d vec3d = player.getPositionEyes(partialTicks);
        Vec3d vec3d1 = player.getLook(partialTicks);
        Vec3d vec3d2 = vec3d.addVector(vec3d1.x * blockReachDistance, vec3d1.y * blockReachDistance, vec3d1.z * blockReachDistance);
        return player.world.rayTraceBlocks(vec3d, vec3d2, false, false, true);
    }

	public static int getTopSolidBlock(Chunk chunk, int x, int z, int yA) { // recently changed, untested
		int k;
		IBlockState block = null;
		for (k = yA; k > 0; k--) {
			block = chunk.getBlockState(new BlockPos(x, k, z));
			if (block.getMaterial().isOpaque() && !(block.getMaterial() == Material.WOOD)) {
				break;
			} // recently changed from .isSolid() in .isOpaque() -> PROBLEMS?
		}

		return k;
	}

	public static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	public static void setChunkBlock(Chunk chunk, BlockPos pos, IBlockState newBlock) {
		IBlockState orgState = chunk.getBlockState(pos);
		chunk.setBlockState(pos, newBlock);
		chunk.getWorld().notifyBlockUpdate(new BlockPos(chunk.x*16+pos.getX(), pos.getY(), chunk.z*16+pos.getZ()), orgState, newBlock, 8);
	}
	
}
