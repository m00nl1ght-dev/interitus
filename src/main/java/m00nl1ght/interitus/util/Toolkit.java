package m00nl1ght.interitus.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

import com.google.common.base.Predicate;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
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
	
	public static final Predicate<String> FLOAT_VALIDATOR = new Predicate<String>() {
		@Override
		public boolean apply(String input) {
			Float f = Floats.tryParse(input);
			return input.isEmpty() || f!=null && Float.isFinite(f);
		}
	};
	
	public static final Predicate<String> INT_VALIDATOR = new Predicate<String>() {
		@Override
		public boolean apply(String input) {
			return input.isEmpty() || Ints.tryParse(input)!=null;
		}
	};

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

	public static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
	
	public static long intPairToLong(int a, int b) {
		return (long)a << 32 | b & 0xFFFFFFFFL;
	}
	
	public static int longToIntPairA(long l) {
		return (int)(l >> 32);
	}
	
	public static int longToIntPairB(long l) {
		return (int)l;
	}

	public static void setChunkBlock(Chunk chunk, BlockPos pos, IBlockState newBlock) {
		IBlockState orgState = chunk.getBlockState(pos);
		chunk.setBlockState(pos, newBlock);
		chunk.getWorld().notifyBlockUpdate(new BlockPos(chunk.x*16+pos.getX(), pos.getY(), chunk.z*16+pos.getZ()), orgState, newBlock, 8);
	}

	public static void drawStringRight(FontRenderer fontRenderer, String string, int x, int y, int c) {
		fontRenderer.drawStringWithShadow(string, x-fontRenderer.getStringWidth(string), y, c);
	}
	
}
