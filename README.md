MonopDroid
==========

Monopoly-like Android Game using the monopd servers. Similar to GtkAtlantic.

Store description
-----------------
This is a game like Monopoly(tm). You can play the board game on Android-powered device with other players who use this or the related programs GtkAtlantic (for Windows and Linux) or GtkMonop (for Linux). The rules are more or less the rules of real Monopoly (http://en.wikibooks.org/wiki/Monopoly/Official_Rules). Join a game with friends and play!

It uses the "monopd" service to connect with other players. All components of the server and client are Open Source Software. It supports all of the features of Monopoly, including (importantly!) trading with other players. This game allows plain-text chat with other players.

This project: https://github.com/nmbook/monopdroid

Server project: monopd (http://sourceforge.net/projects/monopd/)

Desktop client projects: GtkAtlantic (http://gtkatlantic.sourceforge.net/), GtkMonop (http://gtkmonop.sourceforge.net/

Known issues/my todo and wishlist
---------------------------------

* [GAMELIST/missing server feature] The server does not indicate the creator of a game.
* [GAMELIST/server error] The server indicates the wrong number of players in games.
* [BOARD:CONFIG/graphical error] The game lobby's configuration page does not handle text wrapping of option text properly.
* [BOARD:CONFIG/graphical error] The game lobby's configuration page does not have scrolling for too many options.
* [BOARD:CONFIG/server error] Restoring configuration values to the game lobby's configuration page to a game sometimes only restores one or two options.
* [BOARD:GAME/missing feature] The game board does not display current houses/hotels (this was removed because it was buggy).
* [BOARD:GAME/missing feature] The game board does not display estate icons (community chest, chance, railroad, ...) (this was removed because it was buggy).
* [BOARD:GAME/missing feature] The game board displays estate ownership and mortgage in a weird way. I consider showing a star symbol instead.
* [BOARD:GAME/missing server feature] The server does not indicate what estates have special rent besides being in estate groups called "Railraods" and "Utilities", nor what those special rents are.
* [BOARD:PIECE/graphical error] Pieces on the game board appear for players who have lost.
* [BOARD:PIECE/missing feature] Pieces on the game board do not get offset properly when multiple pieces are on the same space.
* [BOARD:PIECE/missing feature] Pieces on the game board do not animate to the next estate in a smooth manner (this was removed because it was buggy).
* [BOARD:OVERLAY/graphical error] The game overlays do not have scrolling and instead text falls off the bottom on some devices or for some overlays.
* [BOARD:OVERLAY/missing feature] The ability to re-open Trade and Auction overlays that are somehow marked "active" without having to go to chat.
* [BOARD:OVERLAY/missing feature] The currently opened overlay could highlight the Player or Estate that it references, or the Auction or Trade if they had an active indicator.
* [BOARD:OVERLAY/missing feature] The player overlay should show the currently owned cards, i.e. a get out of jail free card.
* [BOARD:OVERLAY/missing feature] The player overlay could show the owned estate deed cards, perhaps with a full graphic.
* [BOARD:OVERLAY/missing feature] The estate overlay could show the estate deed card as a full graphic.
* [BOARD:ENDGAME/graphical error] The board doesn't know what to do with itself when the game ends. It should display the winner and a working "End game" (leave game) button, perhaps.
* [BOARD:ENDGAME/server error] You can somehow keep getting turn change messages after the game mode becomes "end". Huh?
* [GAME:PLAYERLIST/graphical error] The player list is often not refreshed after the soft keyboard has moved it offscreen and back.
* [GAME:TRADE/missing feature] Add the ability to remove trade proposals from a trade.
* [GAME:TRADE/missing feature] Add the ability to go backwards in a trade proposal add sequence (i.e. the dialogs other than the first one would have "Back" as well as "Cancel").
* [GAME:TRADE/server error] On receiving a trade request from another player, the only participant is yourself until trade proposals are added or the sending player "accepts" the empty trade. Even though the other side sees both users as participants at the start.
* [GAME:RECONNECT/server error] The server does not send us the full state on successful reconnect. Current trades, current auctions, and actual piece positions are not correct until they change again.
* [GAME:INTERACTION/missing feature] Add a notification/sound effect for pending actions (it's your turn to roll, choose to buy/auction, ...), incoming trades. starting auctions, or someone !ping-ing you or saying your name.
* [CODE/missing feature] Using the HTML parser for ChatItem, BoardViewSurfaceThread, and MonopolyDialog is inefficient, and should be replaced with direct manipulation of CharSequence spanning objects.
* [CODE/missing feature] Upgrade the preferences activity and setup to use Android 4 style, specifically Fragments.

