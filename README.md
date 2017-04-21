# [Inventory Sync](https://github.com/BrainStone/InvSync)

[![Build Status](https://gitlab.brainstonemod.com/BrainStone/InvSync/badges/master/build.svg)](https://gitlab.brainstonemod.com/BrainStone/InvSync/commits/master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/09e53d10121c46d693e2cb251fd12bf0)](https://www.codacy.com/app/BrainStone/InvSync?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=BrainStone/InvSync&amp;utm_campaign=Badge_Grade)

This plugin synchronizes the player inventory with a database 

## [Issue Reporting](https://github.com/BrainStone/InvSync/issues)

If you found a bug or even are experiencing a crash please report it so we can fix it. Please check at first if a bug report for the issue already
[exits](https://github.com/BrainStone/InvSync/issues). If not just create a [new issue](https://github.com/BrainStone/InvSync/issues/new) and fill out the form.

Please include the following:

* Minecraft version
* Inventory Sync version
* Sponge version/build
* Versions of any mods/plugins potentially related to the issue 
* Any relevant screenshots are greatly appreciated.
* For crashes:
  * Steps to reproduce
  * latest.log (the FML log) from the log folder of the server
 

## [Feature Requests](https://github.com/BrainStone/InvSync/issues)

If you want a new feature added, go ahead an open a [new issue](https://github.com/BrainStone/InvSync/issues/new), remove the existing form and describe your
feature the best you can. The more details you provide the easier it will be implementing it.  
You can also talk to me on IRC on my channel #BrainStone on esper.net

## Developing with My Plugin

So you want to use items or blocks from my mod, add support or even develop an addon for my mod then you can easily add it to your development environment! All
releases beginning from version 1.10.2-4.0.19 get uploaded to my maven repository.  
So all you have to do to include the mod is add these lines *(in the appropriate places)* to your build.gradle

    repositories {
        maven { // InvSync
            url "https://maven.jnc.world"
        }
        // Other repos...
    }
    
    dependencies {
        compile "world.jnc.invsync:InvSync:<version>"
        // Other dependencies
    }

## Setting up a Workspace/Compiling from Source

* Setup: Run [gradle] in the repository root: `gradlew[.bat] installLombok [eclipse|idea]`
* Build: Run [gradle] in the repository root: `gradlew[.bat] build`
* If obscure Gradle issues are found try running `gradlew clean` and `gradlew cleanCache`

## Jar Signing

All jars from all official download sources will be signed. The signature will always have a SHA-1 hash of `2238d4a92d81ab407741a2fdb741cebddfeacba6` and you
are free to verify it.

## Copyright and License

### Copyright
Copyright 2017 [Yannick Schinko](https://github.com/BrainStone). All rights reserved

### License
InvSync is licensed under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.html)
