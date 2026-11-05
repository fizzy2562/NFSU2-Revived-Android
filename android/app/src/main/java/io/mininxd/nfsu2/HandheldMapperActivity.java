package io.mininxd.nfsu2;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Full-screen mapper: tap an action, then press a physical control to bind it.
 */
public final class HandheldMapperActivity extends Activity {
    private HandheldGameAction pending;
    private LinearLayout list;

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        HandheldBindings.ensureDefaults(this);
        GameOptions.ensureHandheldDefaults(this);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 24, 32, 24);
        root.setBackgroundColor(Color.parseColor("#101820"));

        TextView title = new TextView(this);
        title.setText("Handheld button layout");
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        root.addView(title);

        TextView help = new TextView(this);
        help.setText("Retroid Pocket 5 · Tap an action, then press the physical button or trigger to assign. "
                + "Sticks use axis bindings (defaults: left stick steer, L2/R2 brake/gas).");
        help.setTextColor(Color.LTGRAY);
        help.setPadding(0, 12, 0, 16);
        root.addView(help);

        ImageView art = new ImageView(this);
        art.setImageResource(R.drawable.handheld_retroid_outline);
        art.setAdjustViewBounds(true);
        root.addView(art);

        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(0, 16, 0, 8);
        root.addView(list);

        Button reset = new Button(this);
        reset.setText("Reset to defaults");
        reset.setOnClickListener(v -> {
            HandheldBindings.resetDefaults(this);
            refreshRows();
            Toast.makeText(this, "Defaults restored", Toast.LENGTH_SHORT).show();
        });
        root.addView(reset);

        Button done = new Button(this);
        done.setText("Done");
        done.setOnClickListener(v -> {
            getSharedPreferences("nfsu2_game", MODE_PRIVATE).edit()
                    .putBoolean("handheld_mapper_shown", true).apply();
            finish();
        });
        root.addView(done);

        scroll.addView(root);
        setContentView(scroll);
        refreshRows();
    }

    private void refreshRows() {
        list.removeAllViews();
        for (HandheldGameAction action : HandheldGameAction.values()) {
            if (action == HandheldGameAction.STEER_RIGHT) continue;
            int bind = HandheldBindings.bindingFor(this, action);
            String label = action.name().replace('_', ' ');
            if (action == HandheldGameAction.STEER_LEFT) label = "Steer (left stick X)";
            TextView row = new TextView(this);
            row.setText(label + "  →  " + HandheldBindings.labelForCode(bind));
            row.setTextColor(pending == action ? Color.parseColor("#7CFC00") : Color.WHITE);
            row.setPadding(16, 20, 16, 20);
            row.setBackgroundColor(Color.parseColor("#1a2530"));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = 6;
            row.setLayoutParams(lp);
            row.setGravity(Gravity.CENTER_VERTICAL);
            HandheldGameAction target = action;
            row.setOnClickListener(v -> {
                pending = target;
                refreshRows();
                Toast.makeText(this, "Press a button for: " + target.name(), Toast.LENGTH_LONG).show();
            });
            list.addView(row);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (pending == null) return super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            pending = null;
            refreshRows();
            return true;
        }
        HandheldBindings.setBinding(this, pending, keyCode);
        Toast.makeText(this, pending.name() + " → " + HandheldBindings.labelForCode(keyCode), Toast.LENGTH_SHORT).show();
        pending = null;
        refreshRows();
        return true;
    }
}
