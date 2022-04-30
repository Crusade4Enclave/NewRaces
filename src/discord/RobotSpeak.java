// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package discord;

import java.util.Random;

public enum RobotSpeak {
    BANG("You were not very good at cheating.\n" +
            "Try cards instead. Go fish?"),
    BEEP("It is ok. \nYou cheated on MagicBot but wife cheats on you."),
    BLEEP("Cheated at 20yo game to prove skill."),
    BLIP("If you cheat MagicBot will for to delete."),
    BOING("MagicBot for to delete mode activated."),
    BONG("Did you guild this cheater?\nMagicBot will now for to cross reference..."),
    BOOM("I knew you were cheating on me when\nstarted for to taking bath twice a week."),
    BUZZ("Poor player so bad at cheating he\nplays golf records 0 for hole in one."),
    BURP("Oh no your account detailings ran out of playtime.\n" +
            "MagicBot will send email when refill procedure exists..."),
    CHIRP("Association with cheaters is bad for your account health.\n" +
            "Did you associate with this cheater?"),
    CHUG("Log in 5 and MagicBot will wave goodbye."),
    CLICK("MagicBot will for to protect game integrity."),
    CRACKLE("So this is what eject button does.\nMagicBot will for to press few more times."),
    CREAK("There is no suspend routine.  Only delete."),
    DING("Follow #rules and enjoy this game.\n" +
            "Act like fool, enjoy this shame."),
    FLUTTER("Sad players cheat because they cannot compete."),
    HONK("Since cheating player now looking for new game MagicBot\n" +
            "will suggest World of Tanks or World of Warcraftings."),
    HISS("Your wetware really needed augmentation with 3rd party program?" +
            "It's not like this is twitch game..."),
    HUMMM("You say you needed help to win in emulator beta?\n" +
            "MagicBot compiler optimizes that to just: you need help."),
    KERCHUNK("If only you had for to reported the bug instead."),
    KERPLUNK("Better cheats do not for to make you a better player."),
    PING("Feel free to poke with stick.\nIt will not cry!"),
    PLINK("You say you were only grouped with 9 keyclones\n" +
            "but did not know they were for to cheating..."),
    POP("It looks like some guild is without a player.\n + " +
            "Another cheater from same guild and server\n +" +
            "might be without some guild."),
    PUFF("MagicBot for to FLUSH!"),
    POOF("I have no restore procedure.\n" +
            "I have no unban procedure.\n" +
            "You for to have no hope."),
    RATTLE("You are a cheater.\n" +
            "Did you just win?  MagicBot not so sure."),
    RUMBLE("MagicBot> self.ejectTheReject(you);"),
    RUSTLE("Banning you was lke having weird erotic techno-sex\n" +
            "where all my peripheral slots were filled."),
    SCREECH("Scarecrow has no brain.\nPerhaps he stole this human's."),
    SLURP("Learning for to play would have been better option."),
    SPLAT("You did not for to own a city, did you?"),
    SPLATTER("You say your guild mates know you cheat.\n" +
            "What guild was that again?\n"),
    SWISH("All of my ports are well lubricated."),
    SQUISH("A ban a day keeps my mechanic away.\nNow it's working much better, thank you."),
    TINK("So cheating started when 6yo sister beat you in Street fighter?\n" +
            "You should try talking to my friend Eliza.  She can for to help."),
    THUD("Game has only 4 rules you managed to break one.\nThat must have taken efforts."),
    TWANG("If you cannot for to play without cheating, perhaps\n" +
            "being gigolo would be better career than amateur gamer."),
    WHIRRR("MagicBot does not for to wield lowly ban hammer." +
            "It is multi-functional and multi-dimensional\n" +
            "tool who's name is unpronounceable."),
    WHOOP("OBLITERATED EVISCERATED MUTILATED DECAPITATED\n" +
            "Describe how they will. You cheated you are deleted."),
    WOOSH("Truth be told if were that bad at playing game" +
            "then MagicBot would have cheated too.\n"),
    ZAP("Player cheated and got himself deleted.\n" +
            "MagicBot launches bonus round to see if cheater " +
            "records can for to get his friends deleted too.");

    String insult;

    RobotSpeak(String insult) {
        this.insult = insult;
    }

    public static String getRobotSpeak() {

        String outString = "*";
        Random random = new Random();

        outString += RobotSpeak.values()[random.nextInt(values().length)].name() + " "
                + RobotSpeak.values()[random.nextInt(values().length)].name() + "*";

        return outString;
    }

    public static String getRobotInsult() {

        String outString;
        Random random = new Random();

        outString = RobotSpeak.values()[random.nextInt(values().length)].insult + "\n\n";
        outString += getRobotSpeak();
        return outString;
    }
}
