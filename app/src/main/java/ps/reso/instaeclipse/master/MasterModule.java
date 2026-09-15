package ps.reso.instaeclipse.master;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Independent Xposed entry point for MasterIG.
 *
 * Keeping this separate from InstaEclipse's upstream Module means the overlay can be rebased with
 * almost no conflicts. The only upstream file MasterIG needs to touch is assets/xposed_init, where
 * this class is registered as a second entry point.
 */
public final class MasterModule implements IXposedHookLoadPackage {
    private static final String INSTAGRAM_PACKAGE = "com.instagram.android";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static volatile boolean activityHookInstalled;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (!INSTAGRAM_PACKAGE.equals(lpparam.packageName)) return;
        installActivityHook();
    }

    private static synchronized void installActivityHook() {
        if (activityHookInstalled) return;
        activityHookInstalled = true;
        try {
            XposedHelpers.findAndHookMethod(Activity.class, "onResume", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!(param.thisObject instanceof Activity)) return;
                    final Activity activity = (Activity) param.thisObject;
                    MAIN.post(() -> {
                        try {
                            if (!activity.isFinishing()) {
                                HomeLongPressSettingsHook.bind(activity);
                                HideManualSeenButtonHook.bind(activity);
                            }
                        } catch (Throwable t) {
                            XposedBridge.log("(MasterIG) activity bind failed: " + t.getMessage());
                        }
                    });
                }
            });
            XposedBridge.log("(MasterIG) Activity lifecycle hook installed");
        } catch (Throwable t) {
            activityHookInstalled = false;
            XposedBridge.log("(MasterIG) Activity hook failed: " + t);
        }
    }
}
