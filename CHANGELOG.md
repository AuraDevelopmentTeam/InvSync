0.6.0
-----

- Added individual permissions to allow fine tuning (Closes [#17](https://github.com/BrainStone/InvSync/issues/17))

0.5.51
------

- Made plugin work with API version 5.x.x again
- Fixed console spam on error during synchronization (Fixes [#16](https://github.com/BrainStone/InvSync/issues/16))
- Improved and made game mode synchronizing work again (Fixes [#15](https://github.com/BrainStone/InvSync/issues/15))
- Improved debugging
- Synchronizing selected slot as well to make transition even smoother!

0.5.42
------

- Added URL to plugin info
- Improved event prevention (More events and they don't get prevented if the module is disabled)
- Added warning when previous server took too long

0.5.37
------

- Synchronizing experience is now using a different method (Fixes [#4](https://github.com/BrainStone/InvSync/issues/4))
- No longer using deprecated class

0.5.32
------

- Updated to API 7.0.0
- Removed Synchronizing Achievements because it's working by default (Fixes [#10](https://github.com/BrainStone/InvSync/issues/10) and [#11](https://github.com/BrainStone/InvSync/issues/11))
- Synchronizing PotionEffects (Not working because of a bug in Sponge. See: [#12](https://github.com/BrainStone/InvSync/issues/12))
- Added Manifest to jar

0.5.19-beta
-----------

- Now Synchronizing Achievements (Fixes [#9](https://github.com/BrainStone/InvSync/issues/9))
- Not perfect as achievements get announced
- Added hidden debug setting

0.5.14
------

- Added health to synchronization
- Added hunger to synchronization
- Internal improvements

0.5.10
------

- Files are now additionally signed with PGP (For real this time!)

0.5.7
-----

- Files are now additionally signed with PGP (Not working! See next release)

0.5.4
-----

- Added config option for individual parts to synchronize

0.5.0-beta
----------

- Fixed [#6](https://github.com/BrainStone/InvSync/issues/6): Serialize inventories to NBT
- Data in database is now way more portable
- Fixed [#7](https://github.com/BrainStone/InvSync/issues/7): Only one instance of the plugin can work with a H2 database

0.4.6-beta
----------

- Fixed [#5](https://github.com/BrainStone/InvSync/issues/5): Database connection sometimes times out and should be reconnected (Really this time)
- Internal improvements

0.4.2-beta
----------

- Fixed [#5](https://github.com/BrainStone/InvSync/issues/5): Database connection sometimes times out and should be reconnected (Not really fixed)

0.4.0-beta
----------

- Gamemode and Experience are now synced too (experience is bugged atm)
- Waiting for other server to finish instead of waiting a fixed amount of time. Makes it more secure!

0.3.18-beta
-----------

- Fixed [#3](https://github.com/BrainStone/InvSync/issues/3): Special chars in the MySQL password do not work

0.3.16-beta
-----------

- Fixed [#2](https://github.com/BrainStone/InvSync/issues/2): When switching servers by bungeecord, the inventory gets loaded from the database before the new data is written 
- No item pickup while waiting for synchronization
- Storing inventories to database when server stops
- Internal improvements

0.3.8-beta
----------

- Synchronizing from database now working
- Fixed reloading bug
- Fixed several bugs with statements

0.3.0-alpha
-----------

- Added config
- Added reload functionality
- Fixed serialization for enchantments

0.2.0-alpha
-----------

- Removed MySQL driver (sponge alerady has it implemented)
- Added inventory serialization and deserialization (Not working 100%)
- Added README
- Compressing serialized data to safe space

0.1.0-alpha
-----------

- Added MySQL driver
- Fixed signing
- Added Lombok to project
- Added basic functionality
- Added MySQL connection handler/wrapper

0.0.13-alpha
------------

- Set up project
- First semi working plugin version!

0.0.0-alpha
-----------

- Initial commit
