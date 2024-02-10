# Stick-Arena-Private-Server
Improved version of BallistickEMU. The original is probably entirely written by Simon but I'm not sure.

# What changed?

- Added CredTicketHandler. You can now collect cred ticket when it's available.
- Added DoubleLoginHandler. You can't have two of the same accounts in the same server at the same time anymore, you will be notified that somebody just logged in to your account.
- Better ban handling. Ban packet actually being sent to the client (user getting banned) with a time and message, instead of getting disconnected.
- Fixed lobby appearance bug. Sometimes users were invisible after joining lobby and.. it was a mess.
- Fixed game bug - reset player's kills and deaths if they rejoin the room.
- Wrote win/loss logic, same as on original SA server. Not the best code, change it if you can cus I don't care.
- Partially fixed the inventory glitch. You can now select the spinner you purchased/added without logging out first, but when you log out you will have your first spinner selected.
- Added !bluehead command. Gives you the original bluehead color and the spinner as on original stick arena. Requires 588 version of SA SWF client.
- !fuzzyspinner, !builderspinner, !candycane, !heartsspinner are not simply just !fuzzy, !builder, !cane and !hearts.
- Easier to read syntax help for color and spinner commands (no annoying black glitch with the <> characters)
- Fixed stick_arena.php to work with PHP 7 and up.

# Additional Changes @andre-jar

- Changed project to maven project, which makes handling depedencies and building a jar less of a hassle imo.
- Buying and cheating items will now correctly set the items in the client without overwriting previous items.
- Added logic to kick players in matches. Players can't join after they got kicked until a new round starts. Mods can kick without having votes.
- Added version checking to php script. Accepts 558, 588 and 598 (Dimensions) clients per default. Could be used to restrict Dimensions clients from joining (due to their better viewrange) if you want to allow older clients.
- Rooms will now properly get deregistered and also threads that will no longer be in use get removed so the garbage collection can do its thing. Also removed the deprecated finalizers. It has never been a good idea to rely on them(although they didn't do much here).
- setKills now correctly updates kills and relogging is no longer required.
- setColor also does not require relogging anymore.
- Changed the ban system. It is now possible to ban players a specific amount of time and ip banning also can be time limited (or infinite) from now on. Bans get stored with a timestamp that gets checked at login if the player is still banned.
- Cred tickets now have different probabilities. To win 5000$ for example is now only a chance of ~0.95% the next ticket an additional ~0.95% and so on. Cred ticket is also now available every 8 hours like on the original servers. Similarly to the bans a timestamp is used to check for a ticket at login. 
- Maps can now be loaded and saved on the database with the provided php script. It also now possible to buy map slots if you have a labpass. It is also possible to load maps from the xgen servers for accounts that are not used on your local server. Saving can only be done on local servers of course.
- Added the possibility to add emails to your accounts. They can be used for verification and recovery later on. 
- Improved/fixed stick_arena.php to work with newer php versions. Also added a check for invalid usernames at creation.
- Changed php sql statements to actual prepared statements to minimize risk of SQL injections.
- Made quick start spinners much more like the original. Added missing player names to quickstart.
- Added possibility to handle map ratings. Implement your own logic if you want to process them.
- Added missing SA Ballistick and Dimensions spinners to shop.
- Added highscore boards as php scripts. You can choose between the old style and the new style.
- Rounds stat is now updated properly. Every started round counts towards completed/forfeited rounds stat. Multiple joining of the same match will only be counted once.
- Added support for all the slash commands from the game. Added missing handlers for them.
- Method killrooom now sends the correct packet to the clients.
- Database now stores the creation date and last login date of users.
- Created Event on database to reduce the labpass days every day. 
- Fixed a bug where vips would not get added properly. Also only vips and labpass owners can now join labpass games. Also kicks vip players now if the game creator left. Also kicks players if it is a custom map and the creator left.
- Labpass matches are no longer visible for normal players and also full rooms are also no longer visible. Also full games can't be joined any more now.
- Restricted a few player commands. Normal players should not be allowed to cheat kills or change color any way they want (which would kill the purpose of shop and credits a bit). Also restricted some of the special spinners to labpass owners.
- Changed ids of tables to start at lower numbers.
- Changed all System.out calls to loggers. Additionally it is now possible to log the chat and also logs can be saved into a logfile.
- Added a few additional commands (::spy to spy on private messages, ::banrecord to view a banrecord of a player, ::lastlogin to view the last login date of a player, !creator to see the creator of a game, !showgames to see active games and !listplayers to list players).
- Added possibility to enable console commands. You can type "help" for a list of commands. 
- Updates labpass expiry now when player returns to the lobby so you can't bypass the expiry date by staying logged in.
- Added a few filler php sites to fill the missing links in the swf files.
- Added the possibility to verify emails and recover accounts with the provided php scripts.
- Added a few example sqls to create example users.
- Fixed a bug where you could not select an empty pet.
- Users do not have a labpass as default now.

# How to set up

- Requirements: Java JDK, Maven, Flash Decompiler(JPEXS is the best I tested so far and I tested a lot), webserver with PHP installed, MySQL database (You can use xampp for webserver and database as a quick start), Flash player(Discontinued but there are versions floating around in the internet) 
- To setup your database create a new database, select it and use the script "Import database\ballistick_struct.sql". It will create any required tables for you. If you use xampp you can use phpAdmin to do this. There are also 2 extra sql files in it. The file "example_users.sql" creates a few example users on the database with a 123 password (They are named after my old accounts). The file "map_users.sql" creates a few fake users just to give an example how maps could be saved if you want to provide your users a few premade maps.
- Place the contents of the folder "htdocs\stickarena" at the root of your webserver. There is also a folder called "htdocs\swf_filler_pages". It contains a few extra sites just to fill in the missing links in the swf files. They are not really neccessary. I just included them to make everything complete.
- The last file you need now is a executable jar to run with Java to start you server. Inside the folder "ballistickemu" there is a maven project inside. If you have properly installed maven just run the command "mvn install" inside the folder. If everything worked there should be a file called "ballistickemu-0.0.1-SNAPSHOT-jar-with-dependencies.jar" inside the target folder.
- After you got all the files you can already run the server in a localhost environment. Just run the provided "ballistickemu\run.bat" after starting the database to start the server. You can use the provided swfs that are bound to 127.0.0.1 to see if everything works as it should. The database, webserver and stickarena server have to run all at the same time. You need to connect directly to the swf-File(Adobe Flashplayer can directly connect to URLs)
- Edit stick_arena.php in your webserver root and change the first few lines to your database credentials and address.
- If you don't want to let users also download maps from xgen servers you can set the variable getMapsXGenServer in api.php to false.
- If you want to provide email verification or account recovery you also have to set up a mail server and put in the right email header in stick_arena.php. You can also use a private email to send emails from. You can propably find tutorials online on this subject.
- Edit crossdomain.xml and add entries for your ip and domain like the provided example entries.
- Open settings558b.ini/settings588b.ini/settings598.ini depending on which client you want to use and search for the entries which start with &sConnectPort and &sServerName. Change the entries to the port and ip where the java server is running. You can also set the server type there. This is the list that the client uses to connect to a server.
- In the folder where the run.bat is located there is also a file called config.properties. Put in the credentials and addresses of your server and database in it. You can also set if the chat should be logged with the property "logchat". The property "enable_prompt" enables the possiblity to type in commands into the console. Type "help" to get the command list with this option enabled.
- There is also a file called "log4j2.xml" in it. By default the server creates a logfile that gets stored in a folder called "logs". If you don't want to store log files simply comment out or delete the tag "RollingFile" and it's contents. You can look up the log4j documentation for further info.
- The final step now is to let the swf clients point at the right ip. By default there are the files sab558.swf, sab588.swf and dimensions.swf in your webserver root folder. These are the stickarena/ballistick clients to connect to the server. You can remove them including the numbered ini files if you don't want to provide the specific swf to users. 
- You have to edit the swf files with a flash decompiler and change every value where a 127.0.0.1 IP is set to your ip where your server runs. If you use JPEXS be sure to only edit the P-Code and only edit the values neccessary. It is easy to break things here. The 588 and dimensions clients are obfuscated so only very few decompilers will be able to do the job. It should however work with JPEXS(with deobfuscation option enabled).
- The last step you can do if you want only specific clients on your server is to edit the version_check.php in your webserver root. It checks for the version number and prevents users with differing client numbers from joining.
- Additionally you should also implement the standard security measures before actually using your server (Things like disabling php error display, whitelisting database or disable access over http, disable phpinfo and so on. Again just have a look at a few tutorials on this subject if you are concerned about security issues).
- Feel free to report any bugs you may find. Simply create an issue in the github page, I will see what I can do about it. All features are tested with multiple clients but I can not assure everything works (let alone the fact that I didn't write a huge portion of the code).

# Troubleshooting

- If you can't connect somehow check if everthing is setup like described first. The ips have to the set to the same everywhere. It can also make a difference if you use a domain(like localhost) instead of the ip. You can use both but stick to one of the options in all your configurations. When you connect to the swf it also has to be the same ip. If everything is setup to use a domain it can probably fail if you are trying to connect with the ip instead(even if you now it is the same machine).
- Even if you host locally you have to access the swf files over the IP. This is because of the security sandbox measures implemented in flash. You can read about it. 

# Additional info

- Normal accounts without moderator permissions have limitations on which colors they can use. They will appear as red if the color is not allowed. This seems to be coded into the clients, it will still get saved properly on the server and database. Moderators are exempt from this restriction.
- If the file gets stored with windows line-endings (CR LF) instead of linux endings (LF) there can be minor issues that arise (credits bugged, text strings not how they should be).

# Future updates 

- As I already spent way too much time on this project I will NOT continue to develop this project any further. If you come across any bugs I will still try to fix them. Just create an issue and I will see what I can do about it.