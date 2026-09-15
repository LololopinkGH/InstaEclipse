package ps.reso.instaeclipse.master;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import ps.reso.instaeclipse.utils.log.ModuleLog;

/** Hides the base module's injected DM "mark seen" eye button without patching upstream code. */
public final class HideManualSeenButtonHook {
    private HideManualSeenButtonHook() {}

    private static final String GHOST_BTN_TAG = "ie_ghost_seen_btn";
    private static volatile boolean installed;

    public static void bind(Activity activity) {
        if (activity == null) return;
        installOnce();
        try {
            View decor = activity.getWindow() == null ? null : activity.getWindow().getDecorView();
            if (decor != null) applyToTree(decor);
        } catch (Throwable ignored) {}
    }

    private static synchronized void installOnce() {
        if (installed) return;
        installed = true;
        try {
            XposedHelpers.findAndHookMethod(View.class, "onAttachedToWindow", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    View view = (View) param.thisObject;
                    if (GHOST_BTN_TAG.equals(view.getTag())) apply(view);
                }
            });
            ModuleLog.line("(MasterIG) ✅ manual Mark Seen visibility hook installed");
        } catch (Throwable t) {
            installed = false;
            ModuleLog.line("(MasterIG) ❌ manual Mark Seen visibility hook: " + t.getMessage());
        }
    }

    private static void apply(View view) {
        view.setVisibility(MasterFeatureFlags.hideManualSeenButton ? View.GONE : View.VISIBLE);
    }

    private static void applyToTree(View root) {
        if (GHOST_BTN_TAG.equals(root.getTag())) apply(root);
        if (!(root instanceof ViewGroup)) return;
        ViewGroup group = (ViewGroup) root;
        for (int i = 0; i < group.getChildCount(); i++) applyToTree(group.getChildAt(i));
    }
}
