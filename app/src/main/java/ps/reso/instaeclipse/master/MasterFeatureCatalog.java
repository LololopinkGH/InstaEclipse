package ps.reso.instaeclipse.master;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** UI metadata only. `flag` is a public boolean field on FeatureFlags for implemented toggles.
 * Planned items intentionally have flag == null and are rendered disabled to avoid placebo toggles. */
public final class MasterFeatureCatalog {
    private MasterFeatureCatalog() {}

    public static final class Item {
        public final String category;
        public final String title;
        public final String description;
        public final String flag;
        public Item(String category, String title, String description, String flag) {
            this.category = category; this.title = title; this.description = description; this.flag = flag;
        }
        public boolean implemented() { return flag != null && !flag.isEmpty(); }
    }

    private static Item on(String c, String t, String d, String f) { return new Item(c,t,d,f); }
    private static Item todo(String c, String t, String d) { return new Item(c,t,d,null); }

    public static final List<Item> ITEMS = Collections.unmodifiableList(Arrays.asList(
        on("Privacy & Ghost", "Hide DM seen", "Read DMs without sending the normal seen receipt.", "isGhostSeen"),
        on("Privacy & Ghost", "Hide typing", "Suppress the typing indicator.", "isGhostTyping"),
        on("Privacy & Ghost", "Hide story views", "Do not submit the normal story-view receipt.", "isGhostStory"),
        on("Privacy & Ghost", "Hide live presence", "Suppress normal live presence reporting.", "isGhostLive"),
        on("Privacy & Ghost", "Bypass screenshot detection", "Suppress Instagram's disappearing-media screenshot signal.", "isGhostScreenshot"),
        on("Privacy & Ghost", "Allow screenshots in DMs", "Remove client-side screenshot restrictions where possible.", "allowScreenshots"),
        on("Privacy & Ghost", "Hide view-once opened", "Avoid the normal opened state for view-once media.", "isGhostViewOnce"),
        on("Privacy & Ghost", "Permanent view-once", "Keep view-once/view-twice media available locally.", "permanentViewMode"),
        on("Privacy & Ghost", "Keep disappearing messages", "Prevent ephemeral DM items from disappearing locally.", "keepEphemeralMessages"),
        on("Privacy & Ghost", "Keep unsent messages", "Keep a private local copy when a delivered DM is unsent.", "keepUnsentMessages"),
        on("Privacy & Ghost", "Mark seen after replying", "Keep DM seen hidden until you send a reply, then release Instagram's normal seen path.", "master:markSeenAfterReply"),
        todo("Privacy & Ghost", "Secret voice-note playback", "Separate playback receipt suppression if Instagram's current build uses a distinct receipt path."),
        on("Privacy & Ghost", "Hide manual Mark Seen button", "Hide the eye button injected by the base module in DM composer.", "master:hideManualSeenButton"),

        on("Messages", "Hide specific chats", "Remove selected threads from the normal inbox.", "hideSpecificChats"),
        on("Messages", "Lock DMs", "Require the module passcode/biometric before opening DMs.", "lockDirectMessages"),
        on("Messages", "Lock whole app", "Require unlock when entering Instagram.", "lockWholeApp"),
        todo("Messages", "Message edit history", "Keep prior text revisions and show a timeline."),
        todo("Messages", "Hide media in chats", "Prime-equivalent client-side media hiding."),
        todo("Messages", "Anti-reply image/media", "Recreate Prime behavior after hook verification."),
        todo("Messages", "Send audio as voice note", "Send a selected local audio file through the voice-note path."),

        on("Feed", "Block ads", "Drop sponsored units.", "isAdBlockEnabled"),
        on("Feed", "Block analytics", "Suppress supported analytics/telemetry calls.", "isAnalyticsBlocked"),
        on("Feed", "Disable tracking links", "Remove supported tracking parameters from copied/shared links.", "disableTrackingLinks"),
        on("Feed", "Hide suggestions", "Remove suggested feed/reel units.", "hideSuggestionsInFeed"),
        on("Feed", "Hide Threads suggestions", "Remove Threads cross-promotion units.", "hideThreadsSuggestions"),
        on("Feed", "Disable video autoplay", "Require explicit playback.", "disableVideoAutoPlay"),
        on("Feed", "Disable double-tap like", "Avoid accidental likes.", "disableDoubleTapLike"),
        todo("Feed", "Following-only mode", "Filter feed to followed accounts only."),
        todo("Feed", "Advanced content filters", "Image/video, public/private, liked/seen filters."),
        todo("Feed", "Enhanced photo quality", "Prefer higher-quality photo variants and upload parameters where available."),

        on("Focus", "Disable Stories", "Hide/disable Stories.", "disableStories"),
        on("Focus", "Disable Feed", "Hide/disable the main feed.", "disableFeed"),
        on("Focus", "Disable Reels", "Hide/disable Reels.", "disableReels"),
        on("Focus", "Disable Explore", "Hide/disable Explore.", "disableExplore"),
        on("Focus", "Disable Comments", "Hide/disable comment surfaces.", "disableComments"),

        on("Stories", "Disable story auto-advance", "Stop automatic story flipping.", "disableStoryFlipping"),
        on("Stories", "View story mentions", "Expose story @mentions.", "enableStoryMentions"),
        on("Stories", "Cache viewed stories", "Keep viewed story media locally for the configured cache window.", "cacheStories"),
        todo("Stories", "Story ring size", "Prime-equivalent UI sizing control."),
        todo("Stories", "Upload time format", "Prime-equivalent story time-format control."),
        todo("Stories", "Mention indicator style", "Prime-equivalent mention indicator styles."),

        on("Downloads", "Download posts", "Save posts/carousels.", "enablePostDownload"),
        on("Downloads", "Download stories", "Save stories.", "enableStoryDownload"),
        on("Downloads", "Download reels", "Save reels.", "enableReelDownload"),
        on("Downloads", "Download profile pictures", "Save full profile images.", "enableProfileDownload"),
        on("Downloads", "Copy direct media link", "Copy the CDN media URL.", "copyMediaLink"),
        on("Downloads", "Save Instants", "Save received Instants locally.", "saveInstants"),
        on("Downloads", "Upload Instant from gallery", "Use gallery media as an Instant.", "uploadInstants"),
        todo("Downloads", "Download audio only", "Extract/save the audio stream from supported media."),

        on("Appearance", "Custom theme", "Apply a custom Instagram palette.", "customThemeEnabled"),
        on("Appearance", "Custom font", "Apply a user-selected local font.", "customFontEnabled"),
        on("Appearance", "Custom emoji", "Apply a compatible user-selected emoji font.", "customEmojiEnabled"),
        todo("Appearance", "Emoji presets", "iOS/Samsung/Facebook/Twitter/JoyPixel selectors without bundling proprietary font files."),
        todo("Appearance", "Navigation manager", "Reorder/hide tabs and select startup tab."),
        todo("Appearance", "Private screen", "Configurable privacy overlay/opacity."),

        on("Profiles & Text", "Follower indicator", "Show whether the opened profile follows you.", "showFollowerToast"),
        on("Profiles & Text", "Copy comments", "Enable copying comment text.", "enableCopyComment"),
        on("Profiles & Text", "Copy captions", "Enable copying post/reel captions.", "enableCaptionCopy"),
        on("Profiles & Text", "Photo zoom", "Open feed photos in a zoomable viewer.", "enablePhotoZoom"),
        on("Profiles & Text", "Hide discover people", "Remove people-you-may-know surfaces.", "disableDiscoverPeople"),
        on("Profiles & Text", "Always expand profile bio", "Best-effort removal of client-side bio ellipsizing / … more.", "master:alwaysExpandProfileBio"),
        todo("Profiles & Text", "Bio/caption history", "Prime-equivalent local history databases."),
        todo("Profiles & Text", "OCR", "Extract text from images locally/on-device where possible."),
        todo("Profiles & Text", "Copy mentions/hashtags/links", "Add direct copy actions across relevant surfaces."),
        todo("Profiles & Text", "Translation language override", "Override target language for in-app translation actions."),

        on("Advanced", "Developer options", "Enable supported internal developer/config tooling.", "isDevEnabled"),
        on("Advanced", "Remove build-expired popup", "Suppress the old-build expiry dialog.", "removeBuildExpiredPopup"),
        on("Advanced", "Remove Meta AI entry points", "Hide supported Meta AI UI entry points.", "removeMetaAI"),
        on("Advanced", "Spoof last seen", "Freeze/alter supported activity reporting.", "spoofLastSeen"),
        on("Advanced", "Spoof location", "Report configured coordinates to Instagram.", "spoofLocation")
    ));
}
