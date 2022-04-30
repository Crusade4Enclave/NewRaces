// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import java.util.concurrent.atomic.AtomicInteger;

public class GuildStatusController {

	/*
	 * 	Status is stored in a single integer contained within the Character Table
	 * 
	 * 	This class is responsible for maintaining and interpreting that value.
	 * 
	 *  Byte 1 - All		: Title 			[0x000000FF]
	 *  Byte 2 - Low		: isFullMember 		[0x00000F00]
	 *  Byte 2 - High		: isTaxCollector	[0x0000F000]
	 *  Byte 3 - Low		: isRecruiter		[0x000F0000]
	 *  Byte 3 - High		: isInnerCouncil	[0x00F00000]
	 *  Byte 4 - Low		: isGuildLeader		[0x0F000000]
	 *  Byte 4 - High		: Empty				[0xF0000000]
	 */

	//Getters
	public static boolean isGuildLeader(AtomicInteger status) {
		return ((status.get() & GUILDLEADER)  > 0);
	}
	
	public static boolean isInnerCouncil(AtomicInteger status) {
		return ((status.get() & INNERCOUNCIL)  > 0);
	}
	
	public static boolean isRecruiter(AtomicInteger status) {
		return ((status.get() & RECRUITER)  > 0);
	}
	
	public static boolean isTaxCollector(AtomicInteger status) {
		return ((status.get() & TAXCOLLECTOR)  > 0);
	}
	
	public static boolean isFullMember(AtomicInteger status) {
		return ((status.get() & FULLMEMBER)  > 0);
	}
	
	public static int getTitle(AtomicInteger status) {
		return status.get() & TITLE;
	}
	
	public static int getRank(AtomicInteger status) {
		int value = status.get();
		
		//Guild Leader
		if(value > 0x00FFFFFF) {
			return 10;
		} 
		
		//Inner Council
		if(value > 0x000FFFFF) {
			return 9;
		}
		
		//Recruiter
		if(value > 0x0000FFFF) {
			return 8;
		}
		
		//Tax Collector
		if(value > 0x00000FFF) {
			return 7;
		}
		
		//Full Member
		if(value > 0x000000FF) {
			return 6;
		}
		
		//Petitioner
		return 5;
	}
	
	//Setters
	public static void setTitle(AtomicInteger current, int i) {
		int value;
		i &= TITLE;
		do {
			value = current.get();
		}while(!current.compareAndSet(value, (value & ~TITLE) | i));
	}
	
	
	public static void setFullMember(AtomicInteger status, boolean newValue) {
		setNibble(status, newValue, FULLMEMBER);
	}
	
	public static void setTaxCollector(AtomicInteger status, boolean newValue) {
		setNibble(status, newValue, TAXCOLLECTOR);
	}
	
	public static void setRecruiter(AtomicInteger status, boolean newValue) {
		setNibble(status, newValue, RECRUITER);
	}
	
	public static void setInnerCouncil(AtomicInteger status, boolean newValue) {
		setNibble(status, newValue, INNERCOUNCIL);
	}
	
	public static void setGuildLeader	(AtomicInteger status, boolean newValue) {
		setNibble(status, newValue, GUILDLEADER);
	}
	
	private static void setNibble(AtomicInteger current, boolean newValue, int mask) {
		int value, i = ((newValue)?mask & -1:0);
		do {
			value = current.get();
		}while(!current.compareAndSet(value, (value & ~mask) | i));
	}
	
	//Constants
	private static final int TITLE = 0x000000FF; // 00, F0 and 0F had no effect
	private static final int FULLMEMBER = 0x00000F00;
	private static final int TAXCOLLECTOR = 0x0000F000;
	private static final int RECRUITER = 0x000F0000;
	private static final int INNERCOUNCIL = 0x00F00000;
	private static final int GUILDLEADER = 0x0F000000;
}
