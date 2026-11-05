package io.mininxd.nfsu2;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Retroid Pocket 5 diagram with tap hit-targets aligned to {@link R.drawable#handheld_retroid_outline}.
 */
public final class HandheldMapperDiagramView extends FrameLayout {
    public interface OnControlTapped {
        void onControlTapped(HandheldGameAction action);
    }

    private static final float VIEW_W = 720f;
    private static final float VIEW_H = 340f;

    private ImageView artView;
    private OnControlTapped listener;
    /** Normalized 0..1 rects in 720×340 viewport space. */
    private static final class Hit {
        final RectF rect;
        final HandheldGameAction action;
        Hit(float l, float t, float r, float b, HandheldGameAction action) {
            rect = new RectF(l, t, r, b);
            this.action = action;
        }
    }

    private static final Hit[] HITS = {
            new Hit(0.10f, 0.40f, 0.24f, 0.58f, HandheldGameAction.STEER_LEFT),
            new Hit(0.12f, 0.70f, 0.22f, 0.82f, HandheldGameAction.DPAD_UP),
            new Hit(0.78f, 0.40f, 0.92f, 0.58f, HandheldGameAction.CAMERA),
            new Hit(0.78f, 0.54f, 0.90f, 0.66f, HandheldGameAction.LOOK_BACK),
            new Hit(0.78f, 0.66f, 0.90f, 0.78f, HandheldGameAction.CAMERA), // X
            new Hit(0.86f, 0.66f, 0.96f, 0.78f, HandheldGameAction.GAS), // B
            new Hit(0.78f, 0.78f, 0.90f, 0.90f, HandheldGameAction.BRAKE), // A
            new Hit(0.06f, 0.12f, 0.24f, 0.24f, HandheldGameAction.NITRO), // L2
            new Hit(0.76f, 0.12f, 0.94f, 0.24f, HandheldGameAction.HANDBRAKE), // R2
            new Hit(0.42f, 0.66f, 0.50f, 0.76f, HandheldGameAction.PAUSE),
            new Hit(0.50f, 0.66f, 0.58f, 0.76f, HandheldGameAction.SELECT),
    };

    public HandheldMapperDiagramView(Context context) {
        super(context);
        init(context);
    }

    public HandheldMapperDiagramView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        artView = new ImageView(context);
        artView.setImageResource(R.drawable.handheld_retroid_outline);
        artView.setAdjustViewBounds(true);
        artView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        addView(artView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        setClickable(true);
    }

    public void setOnControlTappedListener(OnControlTapped listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP || listener == null) {
            return super.onTouchEvent(event);
        }
        float nx;
        float ny;
        if (artView != null && artView.getWidth() > 0 && artView.getHeight() > 0) {
            float scale = Math.min(
                    artView.getWidth() / VIEW_W,
                    artView.getHeight() / VIEW_H);
            float drawnW = VIEW_W * scale;
            float drawnH = VIEW_H * scale;
            float left = artView.getLeft() + (artView.getWidth() - drawnW) * 0.5f;
            float top = artView.getTop() + (artView.getHeight() - drawnH) * 0.5f;
            nx = (event.getX() - left) / drawnW;
            ny = (event.getY() - top) / drawnH;
        } else {
            nx = event.getX() / Math.max(getWidth(), 1f);
            ny = event.getY() / Math.max(getHeight(), 1f);
        }
        if (nx < 0f || nx > 1f || ny < 0f || ny > 1f) {
            return super.onTouchEvent(event);
        }
        for (Hit hit : HITS) {
            if (hit.rect.contains(nx, ny)) {
                listener.onControlTapped(hit.action);
                return true;
            }
        }
        return super.onTouchEvent(event);
    }
}
