package ps.reso.instaeclipse.master;

/**
 * MasterIG-owned flags. These are intentionally separate from upstream InstaEclipse FeatureFlags
 * so the overlay can be rebased without editing the upstream flag class for every new feature.
 */
public final class MasterFeatureFlags {
    private MasterFeatureFlags() {}

    /** When Ghost DM Seen is active, send the receipt only after the user sends a reply. */
    public static boolean markSeenAfterReply = false;

    /** Hide InstaEclipse's injected manual "mark seen" eye button in the DM composer. */
    public static boolean hideManualSeenButton = false;

    /** Expand the profile biography TextView instead of allowing client-side ellipsizing. */
    public static boolean alwaysExpandProfileBio = false;
}
