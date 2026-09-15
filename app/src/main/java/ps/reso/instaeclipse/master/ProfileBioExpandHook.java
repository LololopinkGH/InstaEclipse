package ps.reso.instaeclipse.master;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import ps.reso.instaeclipse.utils.log.ModuleLog;

/** Best-effort equivalent of Prime's "Remove ... more from User Bio" client-side expansion. */
public final class ProfileBioExpandHook {
    private ProfileBioExpandHook() {}

    private static volatile boolean installed;
    private static volatile int bioId;

    public static void bind(Activity activity) {
        if (activity == null) return;
        if (bioId == 0) {
            try { bioId = activity.getResources().getIdentifier("profile_header_bio_text", "id", activity.getPackageName()); }
            catch (Throwable ignored) {}
        }
        installOnce();
    }

    private static synchronized void installOnce() {
        if (installed) return;
        installed = true;
        try {
            XposedHelpers.findAndHookMethod(View.class, "onAttachedToWindow", new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) {
                    if (!MasterFeatureFlags.alwaysExpandProfileBio || bioId == 0) return;
                    View v = (View) param.thisObject;
                    if (v.getId() != bioId || !(v instanceof TextView)) return;
                    TextView tv = (TextView) v;
                    tv.setMaxLines(Integer.MAX_VALUE);
                    tv.setSingleLine(false);
                    tv.setEllipsize(null);
                    tv.post(() -> {
                        try {
                            tv.setMaxLines(Integer.MAX_VALUE);
                            tv.setSingleLine(false);
                            tv.setEllipsize(null);
                        } catch (Throwable ignored) {}
                    });
                }
            });
            ModuleLog.line("(MasterIG) ✅ profile bio expansion hook installed");
        } catch (Throwable t) {
            installed = false;
            ModuleLog.line("(MasterIG) ❌ profile bio hook: " + t.getMessage());
        }
    }
}
