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
