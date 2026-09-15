package ps.reso.instaeclipse.master;

import android.content.Context;
import android.content.SharedPreferences;

/** Persistent settings owned by the MasterIG overlay. */
public final class MasterPrefs {
    private MasterPrefs() {}

    private static final String NAME = "masterig_prefs";
    private static SharedPreferences prefs;

    public static synchronized void init(Context context) {
        if (context == null) return;
        if (prefs == null) {
            prefs = context.getApplicationContext().getSharedPreferences(NAME, Context.MODE_PRIVATE);
        }
        load();
    }

    private static void load() {
        if (prefs == null) return;
        MasterFeatureFlags.markSeenAfterReply = prefs.getBoolean("markSeenAfterReply", false);
        MasterFeatureFlags.hideManualSeenButton = prefs.getBoolean("hideManualSeenButton", false);
        MasterFeatureFlags.alwaysExpandProfileBio = prefs.getBoolean("alwaysExpandProfileBio", false);
    }

    public static synchronized void save() {
        if (prefs == null) return;
        prefs.edit()
                .putBoolean("markSeenAfterReply", MasterFeatureFlags.markSeenAfterReply)
                .putBoolean("hideManualSeenButton", MasterFeatureFlags.hideManualSeenButton)
                .putBoolean("alwaysExpandProfileBio", MasterFeatureFlags.alwaysExpandProfileBio)
                .apply();
    }
}
