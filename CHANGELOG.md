Version 0.6.19-DEV
------------------

\+ Added Baubles support.  
\+ Implemented module system to allow mods to easily add support themselves.  
\+ Added automatic conversion from old format to new format.  
\* Fixed issue with players not properly saving on server shutdown (Fixes [#32](https://github.com/AuraDevelopmentTeam/InvSync/issues/32))  

Changes since last beta:

\+ Added debugging for modules. And general better debugging
\* Fixed config names for modules.  
\* Fixed using wrong permissions.  
\* Loads of internal improvements.  
\* `RuntimeException`s while printing debug info won't cause any data being lost.  


Version 0.6.18
--------------

\+ Added GradleCommon (helper project. Only used for compiling)  
\* Using new versioning System (due to GradleCommon)  
\* Using SpongeAPI 7.1.0 (instead of 7.1.0-SNAPSHOT)  
\* Using better config code.  
\* Fixed Connection Leaks (Fixes [#28](https://github.com/AuraDevelopmentTeam/InvSync/issues/28))  
\* Improved database performance.  


Version 0.6.17
--------------

\* Fixed synchronizing crashing when unknow item is being synchronized (Fixes [#18](https://github.com/AuraDevelopmentTeam/InvSync/issues/18))  


Version 0.6.14
--------------

\* Prevented a very rare race condition  


Version 0.6.9
-------------

\+ Added bStats metrics  
\* Internal restructuring  


Version 0.6.0
-------------

\+ Added individual permissions to allow fine tuning (Closes [#17](https://github.com/AuraDevelopmentTeam/InvSync/issues/17))  


Version 0.5.51
--------------

\* Made plugin work with API version 5.x.x again  
\* Fixed console spam on error during synchronization (Fixes [#16](https://github.com/AuraDevelopmentTeam/InvSync/issues/16))  
\* Improved and made game mode synchronizing work again (Fixes [#15](https://github.com/AuraDevelopmentTeam/InvSync/issues/15))  
\* Improved debugging  
\* Synchronizing selected slot as well to make transition even smoother!  


Version 0.5.42
--------------

\+ Added URL to plugin info  
\* Improved event prevention (More events and they don't get prevented if the module is disabled)  
\* Added warning when previous server took too long  


Version 0.5.37
--------------

\* Synchronizing experience is now using a different method (Fixes [#4](https://github.com/AuraDevelopmentTeam/InvSync/issues/4))  
\* No longer using deprecated class  


Version 0.5.32
--------------

\+ Added Manifest to jar  
\* Updated to API 7.0.0  
\* Removed Synchronizing Achievements because it's working by default (Fixes [#10](https://github.com/AuraDevelopmentTeam/InvSync/issues/10) and [#11](https://github.com/AuraDevelopmentTeam/InvSync/issues/11))  
\* Synchronizing PotionEffects (Not working because of a bug in Sponge. See: [#12](https://github.com/AuraDevelopmentTeam/InvSync/issues/12))  


Version 0.5.19-beta
-------------------

\+ Added hidden debug setting  
\* Now Synchronizing Achievements (Fixes [#9](https://github.com/AuraDevelopmentTeam/InvSync/issues/9))  
\* Not perfect as achievements get announced  


Version 0.5.14
--------------

\+ Added health to synchronization  
\+ Added hunger to synchronization  
\* Internal improvements  


Version 0.5.10
--------------

\* Files are now additionally signed with PGP (For real this time!)  


Version 0.5.7
-------------

\* Files are now additionally signed with PGP (Not working! See next release)  


Version 0.5.4
-------------

\+ Added config option for individual parts to synchronize  


Version 0.5.0-beta
------------------

\* Fixed [#6](https://github.com/AuraDevelopmentTeam/InvSync/issues/6): Serialize inventories to NBT  
\* Data in database is now way more portable  
\* Fixed [#7](https://github.com/AuraDevelopmentTeam/InvSync/issues/7): Only one instance of the plugin can work with a H2 database  


Version 0.4.6-beta
------------------

\* Fixed [#5](https://github.com/AuraDevelopmentTeam/InvSync/issues/5): Database connection sometimes times out and should be reconnected (Really this time)  
\* Internal improvements  


Version 0.4.2-beta
------------------

\* Fixed [#5](https://github.com/AuraDevelopmentTeam/InvSync/issues/5): Database connection sometimes times out and should be reconnected (Not really fixed)  


Version 0.4.0-beta
------------------

\* Gamemode and Experience are now synced too (experience is bugged atm)  
\* Waiting for other server to finish instead of waiting a fixed amount of time. Makes it more secure!  


Version 0.3.18-beta
-------------------

\* Fixed [#3](https://github.com/AuraDevelopmentTeam/InvSync/issues/3): Special chars in the MySQL password do not work  


Version 0.3.16-beta
-------------------

\* Fixed [#2](https://github.com/AuraDevelopmentTeam/InvSync/issues/2): When switching servers by bungeecord, the inventory gets loaded from the database before the new data is written  
\* No item pickup while waiting for synchronization  
\* Storing inventories to database when server stops  
\* Internal improvements  


Version 0.3.8-beta
------------------

\* Synchronizing from database now working  
\* Fixed reloading bug  
\* Fixed several bugs with statements  


Version 0.3.0-alpha
-------------------

\+ Added config  
\+ Added reload functionality  
\* Fixed serialization for enchantments  


Version 0.2.0-alpha
-------------------

\+ Added inventory serialization and deserialization (Not working 100%)  
\+ Added README  
\* Compressing serialized data to safe space  
\- Removed MySQL driver (sponge alerady has it implemented)  


Version 0.1.0-alpha
-------------------

\+ Added MySQL driver  
\+ Added Lombok to project  
\+ Added basic functionality  
\+ Added MySQL connection handler/wrapper  
\* Fixed signing  


Version 0.0.13-alpha
--------------------

\* Set up project  
\* First semi working plugin version!  


Version 0.0.0-alpha
-------------------

\* Initial commit  
