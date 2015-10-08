# Friends sample

Downloads VK friends records and puts them to SQLite database. Allows sorting friend entries by age or name.

Example of 
- SQLliteDatabase / CursorAdapter
- LoaderManager
- Handling AsyncTask instance on activity recreation 
(with retained fragment - better alternative would be to use Robospice library that hosts requests in
the background service, see https://raw.githubusercontent.com/stephanenicolas/robospice/master/gfx/RoboSpice-InfoGraphics.png). 
- Wrapping all Vkontakte SDK into a facade class VkConnector. 

vksdk_library not included, get it from https://github.com/VKCOM/vk-android-sdk


