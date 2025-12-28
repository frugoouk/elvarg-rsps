package com.elvarg.game.entity.impl.playerbot.interaction;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.ChatMessage;
import com.elvarg.util.Misc;

public class ChatInteraction {

    private final PlayerBot playerBot;

    // --- DELAY SYSTEM ---
    private String pendingResponse = null;
    private int responseDelay = 0;

    // --- COOLDOWN SYSTEM ---
    private long lastChatTime;
    private static final int CHAT_COOLDOWN_MS = 5000;

    // --- TRANSLATION TABLE (The "Alphabet" for the Game Client) ---
    public static final char[] VALID_CHARS = {
            ' ', 'e', 't', 'a', 'o', 'i', 'h', 'n', 's', 'r', 'd', 'l', 'u', 'm', 'w', 'c', 'y', 'f', 'g', 'p', 'b', 'v', 'k', 'x', 'j', 'q', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ' ', '!', '?', '.', ',', ':', ';', '(', ')', '-', '&', '*', '\\', '\'', '@', '#', '+',
            '=', '\243', '$', '%', '"', '[', ']'
    };

    public ChatInteraction(PlayerBot playerBot) {
        this.playerBot = playerBot;
    }

    /**
     * CALLED EVERY TICK
     */
    public void process() {
        if (responseDelay > 0) {
            responseDelay--;
            if (responseDelay == 0 && pendingResponse != null) {
                say(pendingResponse);
                pendingResponse = null;
            }
        }
    }

    /**
     * Makes the bot talk.
     * NOW WITH TRANSLATOR: Converts text to "Packed Bytes" so it reads correctly!
     */
    private void say(String text) {
        // 1. Overhead Text (Yellow) - Needs Raw String
        playerBot.forceChat(text);

        // 2. Chatbox History - Needs PACKED Bytes (The Fix)
        byte[] packedData = pack(text);
        playerBot.getChatMessageQueue().add(new ChatMessage(0, 0, packedData));
    }

    /**
     * THE PACKER (Translator)
     * Converts "Hi Josh" into the computer numbers the Client understands.
     */
    public byte[] pack(String text) {
        if (text.length() > 80) text = text.substring(0, 80);
        text = text.toLowerCase();

        byte[] buffer = new byte[256];
        int offset = 0;
        int carryOverNibble = -1;

        for (int idx = 0; idx < text.length(); idx++) {
            char c = text.charAt(idx);
            int tableIdx = 0;

            // Find the character in our "Alphabet"
            for (int i = 0; i < VALID_CHARS.length; i++) {
                if (c == VALID_CHARS[i]) {
                    tableIdx = i;
                    break;
                }
            }

            // Compress it
            if (tableIdx > 12) tableIdx += 195;
            if (carryOverNibble == -1) {
                if (tableIdx < 13) carryOverNibble = tableIdx;
                else buffer[offset++] = (byte) tableIdx;
            } else if (tableIdx < 13) {
                buffer[offset++] = (byte) ((carryOverNibble << 4) + tableIdx);
                carryOverNibble = -1;
            } else {
                buffer[offset++] = (byte) ((carryOverNibble << 4) + (tableIdx >> 4));
                carryOverNibble = tableIdx & 0xf;
            }
        }
        if (carryOverNibble != -1) buffer[offset++] = (byte) (carryOverNibble << 4);

        // Resize array to fit exact data
        byte[] result = new byte[offset];
        System.arraycopy(buffer, 0, result, 0, offset);
        return result;
    }

    // --- LISTENERS ---
    public void receivedGameMessage(String message) { }
    public void receivedPrivateMessage(byte[] message, Player participant) { }

    public void heard(ChatMessage message, Player source) {
        String text;
        try {
            text = Misc.textUnpack(message.getText(), message.getText().length);
        } catch (Exception e) {
            text = new String(message.getText());
        }
        handleReceivedMessage(source, text);
    }

    /**
     * AI BRAIN - FULL VERSION
     */
    public void handleReceivedMessage(Player source, String message) {
        // 1. Safety Checks
        if (System.currentTimeMillis() - lastChatTime < CHAT_COOLDOWN_MS) return;
        if (source == playerBot) return;
        if (responseDelay > 0) return; // Already typing a reply

        String text = message.toLowerCase();
        String botName = playerBot.getUsername().toLowerCase();

        // 2. Trigger Range (7 Tiles)
        boolean isTalkingToMe = text.contains(botName) || source.getLocation().getDistance(playerBot.getLocation()) <= 7;

        if (!isTalkingToMe) return;

        String reply = null;

        // --- CATEGORY 1: GREETINGS ---
        if (text.matches(".*\\b(hi|hey|hello|yo|sup|wag1|greetings)\\b.*")) {
            reply = getRandom(new String[]{
                    "Yo", "Sup", "Hey", "Hi there", "Yo " + source.getUsername(), "?", "Hey man", "Salutations", "Hi"
            });
        }

        // --- CATEGORY 2: "WHAT ARE YOU DOING?" (Wuu2) ---
        else if (text.contains("wuu2") || text.contains("wyd") || text.contains("doing") || text.contains("whats up")) {
            reply = getRandom(new String[]{
                    "nm u?",
                    "just chilling",
                    "bank standing lol",
                    "watching yt u?",
                    "not much",
                    "waiting for a mate",
                    "bored af tbh",
                    "xp wasting",
                    "looking for a team",
                    "listening to music"
            });
        }

        // --- CATEGORY 3: BOT ACCUSATIONS ---
        else if (text.contains("bot") || text.contains("macro") || text.contains("fake") || text.contains("ai")) {
            reply = getRandom(new String[]{
                    "lol?",
                    "Im watching netflix chill",
                    "Why would i bot this?",
                    "?",
                    "calling me a bot? lol ok",
                    "stfu",
                    "im just afk mostly",
                    "wish i was a bot, this is boring",
                    "beep boop... jk",
                    "rude",
                    "im literally typing to u"
            });
        }

        // --- CATEGORY 4: LEVELS / STATS ---
        else if (text.contains("lvl") || text.contains("level") || text.contains("xp") || text.contains("stats")) {
            int level = playerBot.getSkillManager().getCombatLevel();
            reply = getRandom(new String[]{
                    "im " + level,
                    "only " + level + " :/",
                    "going for max soon",
                    "check highscores",
                    "combat lvl " + level,
                    "almost leveled up",
                    "my stats are trash tbh",
                    "grinding for 99"
            });
        }

        // --- CATEGORY 5: MONEY / BEGGING ---
        else if (text.contains("free") || text.contains("money") || text.contains("gp") || text.contains("coins")) {
            reply = getRandom(new String[]{
                    "im broke sorry",
                    "nah i need my gp",
                    "go kill cows lol",
                    "nty",
                    "earned mine u should 2",
                    "i dont give handouts",
                    "ask the admins lol",
                    "saving for ags sorry"
            });
        }

        // --- CATEGORY 6: TRADING / ITEMS ---
        else if (text.contains("trade") || text.contains("buy") || text.contains("sell") || text.contains("price")) {
            reply = getRandom(new String[]{
                    "use the ge",
                    "nty",
                    "im not selling sorry",
                    "trade me",
                    "what u offer?",
                    "check the trading post",
                    "im an ironman sorry"
            });
        }

        // --- CATEGORY 7: LOCATION / HELP ---
        else if (text.contains("where") || text.contains("loc") || text.contains("how do i")) {
            reply = getRandom(new String[]{
                    "idk check wiki",
                    "google it",
                    "north i think?",
                    "follow me... actually nvm i forgot",
                    "no idea mate",
                    "ask in yell",
                    "teleport home and ask"
            });
        }

        // --- CATEGORY 8: COMPLIMENTS ---
        else if (text.contains("nice") || text.contains("cool") || text.contains("sick") || text.contains("awesome")) {
            reply = getRandom(new String[]{
                    "ty",
                    "thanks",
                    "ik right",
                    "tyty",
                    "took forever to get",
                    "cheers",
                    "<3"
            });
        }

        // --- CATEGORY 9: PVP / WILDERNESS ---
        else if (text.contains("wild") || text.contains("pvp") || text.contains("fight") || text.contains("edge")) {
            reply = getRandom(new String[]{
                    "meet me edgeville",
                    "nah im busy",
                    "u would die",
                    "gearing up now",
                    "maybe later",
                    "1v1 me then",
                    "sit kid"
            });
        }

        // --- QUEUE THE REPLY ---
        if (reply != null) {
            this.pendingResponse = reply;
            // Random delay between 2 to 5 ticks (1.2s to 3.0s)
            this.responseDelay = 2 + Misc.getRandom(3);
            this.lastChatTime = System.currentTimeMillis();
        }
    }

    private String getRandom(String[] options) {
        return options[Misc.getRandom(options.length - 1)];
    }
}