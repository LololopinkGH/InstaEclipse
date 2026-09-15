package ps.reso.instaeclipse.master;

import android.app.Activity;
import android.view.View;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import ps.reso.instaeclipse.utils.log.ModuleLog;

/**
 * Opens MasterIG controls from a long press on Instagram's bottom Home tab.
 * Uses View.performLongClick just like InstaEclipse's existing inbox long-press path so it remains
 * reliable even when Instagram replaces listeners on its navigation views.
 */
public final class HomeLongPressSettingsHook {
    private HomeLongPressSettingsHook() {}

    private static volatile boolean installed;
    private static volatile int feedTabId;
    private static volatile Activity currentActivity;

    public static void bind(Activity activity) {
        if (activity == null) return;
        currentActivity = activity;
        MasterPrefs.init(activity);
        MarkSeenAfterReplyHook.bind(activity);
        ProfileBioExpandHook.bind(activity);
        HideManualSeenButtonHook.bind(activity);
        if (feedTabId == 0) {
            try {
                feedTabId = activity.getResources().getIdentifier("feed_tab", "id", activity.getPackageName());
            } catch (Throwable ignored) {}
        }
        installOnce();
    }

    private static synchronized void installOnce() {
        if (installed) return;
        installed = true;
        try {
            XposedHelpers.findAndHookMethod(View.class, "performLongClick", new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam param) {
                    if (feedTabId == 0) return;
                    View view = (View) param.thisObject;
                    if (view.getId() != feedTabId) return;
                    Activity activity = currentActivity;
                    if (activity == null || activity.isFinishing()) return;
                    activity.runOnUiThread(() -> MasterSettingsSheet.show(activity));
                    try {
                        Object vibrator = activity.getSystemService(android.content.Context.VIBRATOR_SERVICE);
                        if (vibrator instanceof android.os.Vibrator) {
                            android.os.Vibrator v = (android.os.Vibrator) vibrator;
                            if (android.os.Build.VERSION.SDK_INT >= 26) {
                                v.vibrate(android.os.VibrationEffect.createOneShot(35, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                //noinspection deprecation
                                v.vibrate(35);
                            }
                        }
                    } catch (Throwable ignored) {}
                    param.setResult(true);
                }
            });
            ModuleLog.line("(MasterIG) ✅ Home long-press settings hook installed");
        } catch (Throwable t) {
            installed = false;
            ModuleLog.line("(MasterIG) ❌ Home long-press hook: " + t.getMessage());
        }
    }
}
