package m00nl1ght.interitus.util;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Stack;

import javax.annotation.Nullable;

import m00nl1ght.interitus.Interitus;
import m00nl1ght.interitus.structures.StructurePack;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;


public class InteritusProfiler implements IDebugObject {
	
	protected HashMap<String, Long> profilerData = new HashMap<String, Long>();
	
	public ProfilerStack newStack(String name) {
		return new ProfilerStack(name);
	}
	
	@Override
	public void debugMsg(ICommandSender sender) {
		this.send(sender, "##### ONELASTDAY PROFILER ######");
		StructurePack.get().debugMsg(sender);
		if (!profilerData.isEmpty()) {
			this.send(sender, "-----------------------------");
			for (Entry<String, Long> entry : profilerData.entrySet()) {
				this.send(sender, entry.getKey() + " = " + entry.getValue());
			}
		}
		this.send(sender, "#############################");
	}
	
	public static void send(ICommandSender sender, String msg) {
		sender.sendMessage(new TextComponentString(msg));
	}
	
	@Override
	public void resetStats() {
		StructurePack.get().resetStats();
	}
	
	public long getData(String name) {
		if (profilerData.containsKey(name)) {
			return profilerData.get(name);
		} else {return 0L;}
	}
	
	public HashMap<String, Long> getData() {
		return profilerData;
	}
	
	public void clear() {
		profilerData.clear();
	}
	
	public void printChunkMap(World world, int x, int z, int w, int h, @Nullable EntityPlayer player) {
		String[] s = new String[h];
		for (int i=0; i<h; i++) {
			s[i]="";
			for (int j=0; j<h; j++) {
				Chunk chunk = world.getChunkProvider().getLoadedChunk(i+x, j+z);
				if (player!=null && Math.floorDiv((int)player.posX, 16)==i+x && Math.floorDiv((int)player.posZ, 16)==j+z) {s[i]+="X"; continue;}
				if (chunk==null) {s[i]+="_"; continue;}
				if (chunk.isPopulated()) {s[i]+="P"; continue;}
				if (chunk.isTerrainPopulated()) {s[i]+="T"; continue;}
				s[i]+="G";
			}
		}
		Interitus.logger.info("CHUNK PROFILER FOR ("+x+"/"+z+"_"+w+"/"+h+")");
		for (int k=0; k<h; k++) {
			Interitus.logger.info(s[k]);
		}
		Interitus.logger.info("#################################################");
	}

	protected static class Data {
		
		protected String name;
		protected long time;
		
		protected Data(String name, long time) {
			this.name=name; this.time=time;
		}
		
	}
	
	protected class ProfilerStack {
		
		protected String name;
		protected Stack<Data> entries = new Stack<Data>();
		protected boolean froozen = false; 
		
		public ProfilerStack(String name) {
			this.name=name;
		}
		
		public void startSection(String name) {
			if (froozen) {return;}
			if (!entries.isEmpty()) {
				name=entries.peek().name+"."+name;
			}
			entries.push(new Data(name, System.nanoTime()));
		}
		
		public void nextSection(String name) {
			endSection();
			startSection(name);
		}
		
		public void endSection() {
			if (froozen) {return;}
			processEntry(entries.pop());
		}
		
		public void endSection(String name) {
			if (froozen) {return;}
			do {endSection();} 
			while (!entries.isEmpty() && !entries.peek().name.equals(name));
		}
		
		public void endAllSections() {
			if (froozen) {return;}
			while (!entries.isEmpty()) {processEntry(entries.pop());}
		}
		
		protected void processEntry(Data entry) {
			processEntry(entry.name, System.nanoTime()-entry.time);
		}
		
		protected void processEntry(String name, long data) {
			profilerData.put(name, data);
		}
		
		public String getSection() {
			return entries.isEmpty()?"":entries.peek().name;
		}
		
		public boolean active() {
			return !entries.isEmpty();
		}
		
		public void freeze(boolean freeze) {
			this.froozen=freeze;
		}
		
	}

}
