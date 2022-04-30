![Logo](https://magicbane.com/images/MBLogo.jpg)

# Magicbane Open-Source MMO Project
### *The Community written Shadowbane emulator*
[Magicbane](http://www.magicbane.com)
</BR>
[Public Repository](http://repo.magicbane.com)
<BR>
<magicbot@magicbane.com>

> Magicbane is an emulator for the long dead but much beloved Ubisoft MMO, [Shadowbane](https://en.wikipedia.org/wiki/Shadowbane).
The project was founded in 2013 with the concept of free availability of gameplay; players unencumbered by any factor other than a desire to again play a game they once loved.  A game where the developers do not play is guaranteed to be the fairest game.

The Magicbane Team has wanted to open source Shadowbane for half a decade.  We are excited to now finally have the opportunity, along with some new technology, to truly democratize Shadowbane.

- Written in some 80k lines of Java 8 and bash.
- Project with real infrastructure; Production and development servers supporting multiple containerized apps.
- MagicBox container technology enables _instant deployment_ and trouble free development on modest hardware.
- MagicBox plugin capability allows for new and interesting player experiences.

## Contributing

- Inbound == Outbound.
- IntelliJ is the supported IDE.
- Write code today see it running on the Magicbane production server tomorrow.

## Project setup

**Prerequisites**:

- Git
- IntelliJ
- Java 8 JDK
- Account on the Magicbane [Public Repository](http://repo.magicbane.com)


Copy the HTTP link if you haven't yet installed a public key:

![CopyUrl](https://www.magicbane.com/Development/images/repo.png)

- Clone the Magicbane public repo to your local machine using the copied URL.

![CloneURL](https://www.magicbane.com/Development/images/intellij1.png)

- Under Settings->VersionControl->Git make sure to turn off these two settings.

![CommitOff](https://www.magicbane.com/Development/images/commit.png)

- Select the Project Structure settings within the IDE.

![ProjectStructure](https://www.magicbane.com/Development/images/projectstructure.png)

- Select Java 8 as the IDE target as shown.

![Java8](https://www.magicbane.com/Development/images/project.png)

- Delete and recreate content root pointing at the **Server** directory.
- Make sure the Language Level still reflects Java 8.
- The IDE should now autodetect the cloned source.

![hmm](https://www.magicbane.com/Development/images/module.png)

Magicbane currently has the following dependencies.
<br>

- [EnumBitSet](https://github.com/claudemartin/enum-bit-set)
- [HikariCP](https://github.com/brettwooldridge/HikariCP)
- [JDA](https://github.com/DV8FromTheWorld/JDA)
- [JodaTime](https://github.com/JodaOrg/joda-time)
- [TinyLog](https://github.com/tinylog-org/tinylog/tree/v1.3)
- [MySqlConnector](https://dev.mysql.com/downloads/connector/j/)

They are all directly obtainable from a running MagicBox instance.

``` docker cp magicbox:/usr/share/java/EnumBitSet.jar Dependencies/```

Add the jar files as project libraries as shown.

![Libs](https://www.magicbane.com/Development/images/libraries.png)

You should now be able to build the game!

![Build](https://magicbane.com/Development/images/buildproject.png)

### What now?

Support is available through the repo Wiki or in the Magicbane Discord server.  Feel free to come in and pick MagicBot's brain! the Magicbane Shadowbane Emulator