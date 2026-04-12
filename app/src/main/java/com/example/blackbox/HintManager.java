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
        {"Dùng nút âm lượng", "Thử cắm sạc", "Xoay ngược điện thoại"}, // Level 1
        {"Độ sáng màn hình", "Cảm biến ánh sáng", "Che camera trước"}, // Level 2
        {"Lắc điện thoại", "Đặt máy xuống bàn", "Để im 5 giây"}, // Level 3
        {"Cắm tai nghe", "Bật bluetooth", "Rút tai nghe"}, // Level 4
        {"Chụp màn hình", "Nút nguồn", "Tăng âm lượng"}, // Level 5
        {"Wifi đang bật", "Tắt mạng", "Chế độ máy bay"}, // Level 6
        {"Pin yếu", "Cắm sạc nhanh", "Rút sạc"}, // Level 7
        {"Dùng 2 ngón tay", "Chạm đa điểm", "Vuốt từ cạnh"}, // Level 8
        {"Tìm trong cài đặt", "Thông tin ứng dụng", "Quyền truy cập"}, // Level 9
        {"Micrô đang thu âm", "Nói thật to", "Thổi vào mic"}, // Level 10
        {"Thời gian hệ thống", "Đổi múi giờ", "Chờ đến nửa đêm"}, // Level 11
        {"Vị trí GPS", "Di chuyển 10m", "Tắt định vị"}, // Level 17
        {"Gõ vào mặt sau", "Rung điện thoại", "Cảm biến tiệm cận"}, // Level 20
        {"Kết nối USB", "Bật chế độ tối", "Thay đổi ngôn ngữ"} // Level 29
    };

    private static int getHintIndex(int puzzleId) {
        switch (puzzleId) {
            case 1:  return 0; case 2:  return 1; case 3:  return 2;
            case 4:  return 3; case 5:  return 4; case 6:  return 5;
            case 7:  return 6; case 8:  return 7; case 9:  return 8;
            case 10: return 9; case 11: return 10; case 17: return 11;
            case 20: return 12; case 29: return 13;
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
