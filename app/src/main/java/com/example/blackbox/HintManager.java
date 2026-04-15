package com.example.blackbox;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONException;
import org.json.JSONObject;

public class HintManager {

    public static final int COINS_PER_HINT = 2;
    private static final int INITIAL_COINS = 10;
    private static final int HINTS_PER_LEVEL = 3;

    // Cơ sở dữ liệu Hint cho các Level
    private static final String[][] HINTS_DATA = {
        {"You completed one of them! Congrats!!", "Follow the flow of water.", "Try to rotate your phone."}, // Level 1
        {"Why are there 2 boxes?", "It's sunny today. You should go out!", "You tone it down when in the dark."}, // Level 2
        {"Notice the position.", "Up and down...", "It's all about audio."}, // Level 3
        {"Do you see that?", "Do you here that?", "Did you cover that?"}, // Level 4
        {"It is what it is.", "Is there another way?", "You have to turn it off because it may affect the _____'s signals."}, // Level 5
        {"Look closely.", "Oh. Not that close.", "1...2...3...Pose"}, // Level 6
        {"There are 2 same things on my screen.", "What time is it?", "24/7"}, // Level 7
        {"Solve the other challenges and come back, you'll see it.", "It turned red. You spent a lot of time, huh?", "CHARGE ME, NOW!!!"}, // Level 8
        {"Water surface with ripples.", "A Silent Voice - Koe no Katachi", "shhhhhh....."}, // Level 9
        {"Hi!!!", "Google Assistant is my sister.", "How can I help you?"}, // Level 10
        {"Time will answer everything.", "Light and dark.", "I love Matcha Mooncake."}, // Level 11
        {"Yay!!!", "You might have been accidentally solve this challenge multiple times in the past.", "Shake it off is Taylor's best song ever."}, // Level 17
        {"Something's on the screen, right?", "I need to take a break, bye.", "Power off!"}, // Level 20
        {"This challenge is an exception.", "Don't touch the screen?", "Keep it for a while."}, // Level 29
        {"I can feel the danger nearby.", "This challenge can be used to improve your health.", "Let's go for a walk, or maybe something faster."} // Level 15
    };

    private static int getHintIndex(int puzzleId) {
        switch (puzzleId) {
            case 1:  return 0; case 2:  return 1; case 3:  return 2;
            case 4:  return 3; case 5:  return 4; case 6:  return 5;
            case 7:  return 6; case 8:  return 7; case 9:  return 8;
            case 10: return 9; case 11: return 10; case 17: return 11;
            case 20: return 12; case 29: return 13; case 15: return 14;
            default: return -1;
        }
    }

    public static int getCoins(Context context) {
        SharedPreferences pref = getPref(context);
        if (!pref.contains(context.getString(R.string.prefCoins))) {
            pref.edit().putInt(context.getString(R.string.prefCoins), INITIAL_COINS).apply();
            return INITIAL_COINS;
        }
        return pref.getInt(context.getString(R.string.prefCoins), INITIAL_COINS);
    }

    public static void addCoin(Context context, int amount) {
        int current = getCoins(context);
        getPref(context).edit().putInt(context.getString(R.string.prefCoins), current + amount).apply();
    }

    public static boolean spendCoins(Context context, int amount) {
        int current = getCoins(context);
        if (current < amount) return false;
        getPref(context).edit().putInt(context.getString(R.string.prefCoins), current - amount).apply();
        return true;
    }

    public static String getHintText(int puzzleId, int hintIndex) {
        int idx = getHintIndex(puzzleId);
        if (idx < 0 || hintIndex < 0 || hintIndex >= HINTS_PER_LEVEL) return "Không có gợi ý.";
        return HINTS_DATA[idx][hintIndex];
    }

    public static boolean isHintUnlocked(Context context, int puzzleId, int hintIndex) {
        JSONObject hintsUsed = getHintsUsed(context);
        return hintsUsed.optBoolean(puzzleId + "_" + hintIndex, false);
    }

    public static boolean unlockHint(Context context, int puzzleId, int hintIndex) {
        if (isHintUnlocked(context, puzzleId, hintIndex)) return true;
        if (!spendCoins(context, COINS_PER_HINT)) return false;
        try {
            JSONObject hintsUsed = getHintsUsed(context);
            hintsUsed.put(puzzleId + "_" + hintIndex, true);
            getPref(context).edit().putString(context.getString(R.string.prefHintsUsed), hintsUsed.toString()).apply();
            return true;
        } catch (JSONException e) { return false; }
    }

    private static JSONObject getHintsUsed(Context context) {
        String json = getPref(context).getString(context.getString(R.string.prefHintsUsed), "{}");
        try { return new JSONObject(json); } catch (JSONException e) { return new JSONObject(); }
    }

    private static SharedPreferences getPref(Context context) {
        return context.getSharedPreferences(context.getString(R.string.pref), Context.MODE_PRIVATE);
    }
}
