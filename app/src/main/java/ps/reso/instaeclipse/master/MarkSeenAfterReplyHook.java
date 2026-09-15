package ps.reso.instaeclipse.master;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import ps.reso.instaeclipse.utils.feature.FeatureFlags;
import ps.reso.instaeclipse.utils.log.ModuleLog;

/**
 * Prime-equivalent "Mark as seen after reply" behavior.
 *
 * InstaEclipse's normal ghost-seen hook suppresses the DM read receipt. When a real send button is
 * clicked, this hook waits for the outgoing message to enter the thread, then briefly releases the
 * ghost-seen guard while nudging the message list at the bottom. That reuses Instagram's own
 * ordinary mark-seen path rather than forging a private API request.
 *
 * The hook is intentionally ID-driven and resolves several known Instagram composer IDs at runtime,
 * so it can tolerate layout variants without hard-coding numeric resource IDs.
 */
public final class MarkSeenAfterReplyHook {
    private MarkSeenAfterReplyHook() {}

    private static volatile boolean installed;
    private static volatile Activity activity;
    private static final Set<Integer> sendIds = new HashSet<>();
    private static volatile int messageListId;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static volatile long lastTriggerMs;

    private static final String[] SEND_NAMES = {
            "row_thread_composer_button_send",
            "row_thread_composer_button_send_prism",
            "row_thread_composer_direct_send_button",
            "row_thread_composer_send_button_container",
            "row_thread_composer_send_silently",
            "direct_composer_send_silently_button",
            "messaging_send_button",
            "reply_composer_send"
    };

    public static void bind(Activity a) {
        if (a == null) return;
        activity = a;
        resolveIds(a);
        installOnce();
    }

    private static void resolveIds(Activity a) {
        try {
            String pkg = a.getPackageName();
            android.content.res.Resources r = a.getResources();
            synchronized (sendIds) {
                for (String n : SEND_NAMES) {
                    int id = r.getIdentifier(n, "id", pkg);
                    if (id != 0) sendIds.add(id);
                }
            }
            if (messageListId == 0) messageListId = r.getIdentifier("message_list", "id", pkg);
        } catch (Throwable ignored) {}
    }

    private static synchronized void installOnce() {
        if (installed) return;
        installed = true;
        try {
            XposedHelpers.findAndHookMethod(View.class, "performClick", new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) {
                    if (!MasterFeatureFlags.markSeenAfterReply || !FeatureFlags.isGhostSeen) return;
                    View v = (View) param.thisObject;
                    int id = v.getId();
                    synchronized (sendIds) {
                        if (id == 0 || !sendIds.contains(id)) return;
                    }
                    long now = android.os.SystemClock.uptimeMillis();
                    if (now - lastTriggerMs < 700) return;
                    lastTriggerMs = now;
                    MAIN.postDelayed(MarkSeenAfterReplyHook::releaseSeenOnce, 325L);
                }
            });
            ModuleLog.line("(MasterIG) ✅ mark-seen-after-reply hook installed");
        } catch (Throwable t) {
            installed = false;
            ModuleLog.line("(MasterIG) ❌ mark-seen-after-reply hook: " + t.getMessage());
        }
    }

    private static void releaseSeenOnce() {
        Activity a = activity;
        if (a == null || a.isFinishing() || !MasterFeatureFlags.markSeenAfterReply) return;
        if (!FeatureFlags.isGhostSeen) return;
        try {
            if (messageListId == 0) resolveIds(a);
            View list = messageListId == 0 ? null : a.findViewById(messageListId);
            if (!(list instanceof ViewGroup)) {
                ModuleLog.line("(MasterIG|SeenAfterReply) message_list not found; leaving ghost state untouched");
                return;
            }

            ViewGroup group = (ViewGroup) list;
            group.scrollBy(0, 100_000);
            final boolean previous = FeatureFlags.isGhostSeen;
            FeatureFlags.isGhostSeen = false;
            group.scrollBy(0, -200);
            MAIN.postDelayed(() -> {
                try { group.scrollBy(0, 200); }
                finally { FeatureFlags.isGhostSeen = previous; }
                ModuleLog.line("(MasterIG|SeenAfterReply) released seen receipt after reply");
            }, 260L);
        } catch (Throwable t) {
            FeatureFlags.isGhostSeen = true;
            ModuleLog.line("(MasterIG|SeenAfterReply) failed: " + t.getMessage());
        }
    }
}
