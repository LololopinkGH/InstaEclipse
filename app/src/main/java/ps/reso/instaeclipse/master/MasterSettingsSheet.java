package ps.reso.instaeclipse.master;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ps.reso.instaeclipse.utils.core.SettingsManager;
import ps.reso.instaeclipse.utils.feature.FeatureFlags;

/** Full-screen, searchable in-Instagram control center. No XML/resource IDs are required. */
public final class MasterSettingsSheet {
    private MasterSettingsSheet() {}

    private static int dp(Activity a, int v) {
        return Math.round(v * a.getResources().getDisplayMetrics().density);
    }

    private static GradientDrawable rounded(int color, float radiusPx) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(radiusPx); return d;
    }

    public static void show(Activity activity) {
        if (activity == null || activity.isFinishing()) return;
        final Dialog dialog = new Dialog(activity, android.R.style.Theme_Material_NoActionBar);
        Window w = dialog.getWindow();
        if (w != null) {
            w.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        final int bg = Color.rgb(10,10,12);
        final int card = Color.rgb(24,24,28);
        final int text = Color.WHITE;
        final int sub = Color.rgb(170,170,180);
        final int accent = Color.rgb(195,90,255);

        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(activity,18), dp(activity,16), dp(activity,18), dp(activity,8));
        root.setBackgroundColor(bg);

        LinearLayout header = new LinearLayout(activity);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(activity);
        title.setText("MasterIG"); title.setTextColor(text); title.setTextSize(25); title.setTypeface(Typeface.DEFAULT_BOLD);
        header.addView(title, new LinearLayout.LayoutParams(0, dp(activity,48), 1f));

        TextView close = new TextView(activity);
        close.setText("✕"); close.setTextColor(text); close.setTextSize(22); close.setGravity(Gravity.CENTER);
        close.setOnClickListener(v -> dialog.dismiss());
        header.addView(close, new LinearLayout.LayoutParams(dp(activity,48), dp(activity,48)));
        root.addView(header);

        EditText search = new EditText(activity);
        search.setHint("Search every setting"); search.setHintTextColor(sub); search.setTextColor(text);
        search.setSingleLine(true); search.setPadding(dp(activity,14),0,dp(activity,14),0);
        search.setBackground(rounded(card, dp(activity,14)));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(activity,48));
        sp.setMargins(0, dp(activity,6), 0, dp(activity,12)); root.addView(search, sp);

        TextView note = new TextView(activity);
        note.setText("Working hooks are switchable. Planned Prime/Honista-equivalent features are shown disabled until their hooks are real — no placebo toggles.");
        note.setTextColor(sub); note.setTextSize(12.5f); note.setPadding(dp(activity,2),0,dp(activity,2),dp(activity,10));
        root.addView(note);

        ScrollView scroll = new ScrollView(activity);
        LinearLayout list = new LinearLayout(activity); list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1f));

        final List<Row> rows = new ArrayList<>();
        Map<String, LinearLayout> categoryContainers = new LinkedHashMap<>();
        Map<String, TextView> categoryHeaders = new LinkedHashMap<>();

        for (MasterFeatureCatalog.Item item : MasterFeatureCatalog.ITEMS) {
            LinearLayout category = categoryContainers.get(item.category);
            if (category == null) {
                TextView ch = new TextView(activity);
                ch.setText(item.category.toUpperCase(Locale.ROOT)); ch.setTextColor(accent); ch.setTextSize(12);
                ch.setTypeface(Typeface.DEFAULT_BOLD); ch.setPadding(dp(activity,3),dp(activity,15),0,dp(activity,7));
                list.addView(ch); categoryHeaders.put(item.category, ch);
                category = new LinearLayout(activity); category.setOrientation(LinearLayout.VERTICAL);
                list.addView(category); categoryContainers.put(item.category, category);
            }

            LinearLayout row = new LinearLayout(activity);
            row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(activity,14),dp(activity,10),dp(activity,10),dp(activity,10));
            row.setBackground(rounded(card, dp(activity,13)));
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rp.setMargins(0,0,0,dp(activity,7)); category.addView(row,rp);

            LinearLayout labels = new LinearLayout(activity); labels.setOrientation(LinearLayout.VERTICAL);
            TextView tv = new TextView(activity); tv.setText(item.title); tv.setTextColor(item.implemented()?text:sub); tv.setTextSize(15.5f);
            TextView dv = new TextView(activity); dv.setText(item.implemented()?item.description:(item.description + "  •  planned")); dv.setTextColor(sub); dv.setTextSize(11.5f);
            labels.addView(tv); labels.addView(dv);
            row.addView(labels, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1f));

            SwitchCompat sw = new SwitchCompat(activity);
            sw.setEnabled(item.implemented());
            if (item.implemented()) {
                sw.setChecked(readFlag(item.flag));
                sw.setOnCheckedChangeListener((button, checked) -> {
                    writeFlag(item.flag, checked);
                    try { SettingsManager.saveAllFlags(); } catch (Throwable ignored) {}
                    HideManualSeenButtonHook.bind(activity);
                });
            }
            row.addView(sw);
            rows.add(new Row(item,row));
        }

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a) {}
            @Override public void onTextChanged(CharSequence s,int st,int b,int c) { filter(s==null?"":s.toString()); }
            @Override public void afterTextChanged(Editable e) {}
            private void filter(String raw) {
                String q = raw.trim().toLowerCase(Locale.ROOT);
                Map<String,Boolean> any = new LinkedHashMap<>();
                for (Row r: rows) {
                    MasterFeatureCatalog.Item i=r.item;
                    boolean visible = q.isEmpty() || (i.category+" "+i.title+" "+i.description).toLowerCase(Locale.ROOT).contains(q);
                    r.view.setVisibility(visible?View.VISIBLE:View.GONE);
                    if (visible) any.put(i.category,true);
                }
                for (Map.Entry<String,TextView> e: categoryHeaders.entrySet()) {
                    boolean vis = q.isEmpty() || Boolean.TRUE.equals(any.get(e.getKey()));
                    e.getValue().setVisibility(vis?View.VISIBLE:View.GONE);
                    LinearLayout cat=categoryContainers.get(e.getKey()); if(cat!=null) cat.setVisibility(vis?View.VISIBLE:View.GONE);
                }
            }
        });

        dialog.setContentView(root);
        dialog.show();
        if (dialog.getWindow()!=null) dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT);
    }

    private static boolean readFlag(String name) {
        try {
            if (name != null && name.startsWith("master:")) {
                Field f=MasterFeatureFlags.class.getField(name.substring("master:".length()));
                return f.getBoolean(null);
            }
            Field f=FeatureFlags.class.getField(name); return f.getBoolean(null);
        } catch(Throwable t){ return false; }
    }
    private static void writeFlag(String name, boolean value) {
        try {
            if (name != null && name.startsWith("master:")) {
                Field f=MasterFeatureFlags.class.getField(name.substring("master:".length()));
                f.setBoolean(null,value);
                MasterPrefs.save();
                return;
            }
            Field f=FeatureFlags.class.getField(name); f.setBoolean(null,value);
        } catch(Throwable ignored) {}
    }
    private static final class Row {
        final MasterFeatureCatalog.Item item; final View view;
        Row(MasterFeatureCatalog.Item item, View view){this.item=item;this.view=view;}
    }
}
