package io.mininxd.nfsu2;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.json.JSONObject;

/**
 * Persisted handheld gamepad bindings: each {@link HandheldGameAction} maps to an Android key code
 * (for buttons) or axis id (for analog steering / triggers). Stick/trigger actions use sentinel
 * key codes {@link #AXIS_STEER_X}, {@link #AXIS_LTRIGGER}, {@link #AXIS_RTRIGGER}.
 */
public final class HandheldBindings {
    static final int AXIS_STEER_X = -100;
    static final int AXIS_LTRIGGER = -101;
    static final int AXIS_RTRIGGER = -102;

    private static final String PREFS = "handheld_bindings";
    private static final String KEY_JSON = "map_v1";

    private HandheldBindings() {}

    static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static void ensureDefaults(Context context) {
        if (prefs(context).contains(KEY_JSON)) return;
        resetDefaults(context);
    }

    static void resetDefaults(Context context) {
        try {
            JSONObject o = new JSONObject();
            o.put(HandheldGameAction.GAS.name(), AXIS_RTRIGGER);
            o.put(HandheldGameAction.BRAKE.name(), AXIS_LTRIGGER);
            o.put(HandheldGameAction.STEER_LEFT.name(), AXIS_STEER_X);
            o.put(HandheldGameAction.STEER_RIGHT.name(), AXIS_STEER_X);
            o.put(HandheldGameAction.HANDBRAKE.name(), KeyEvent.KEYCODE_BUTTON_A);
            o.put(HandheldGameAction.NITRO.name(), KeyEvent.KEYCODE_BUTTON_B);
            o.put(HandheldGameAction.CAMERA.name(), KeyEvent.KEYCODE_BUTTON_X);
            o.put(HandheldGameAction.LOOK_BACK.name(), KeyEvent.KEYCODE_BUTTON_Y);
            o.put(HandheldGameAction.SHIFT_UP.name(), KeyEvent.KEYCODE_BUTTON_R1);
            o.put(HandheldGameAction.SHIFT_DOWN.name(), KeyEvent.KEYCODE_BUTTON_L1);
            o.put(HandheldGameAction.PAUSE.name(), KeyEvent.KEYCODE_BUTTON_START);
            o.put(HandheldGameAction.CONFIRM.name(), KeyEvent.KEYCODE_BUTTON_A);
            o.put(HandheldGameAction.BACK.name(), KeyEvent.KEYCODE_BUTTON_B);
            o.put(HandheldGameAction.DPAD_UP.name(), KeyEvent.KEYCODE_DPAD_UP);
            o.put(HandheldGameAction.DPAD_DOWN.name(), KeyEvent.KEYCODE_DPAD_DOWN);
            o.put(HandheldGameAction.DPAD_LEFT.name(), KeyEvent.KEYCODE_DPAD_LEFT);
            o.put(HandheldGameAction.DPAD_RIGHT.name(), KeyEvent.KEYCODE_DPAD_RIGHT);
            prefs(context).edit().putString(KEY_JSON, o.toString()).apply();
        } catch (Exception e) {
            android.util.Log.w("NFS", "Failed to write default handheld bindings", e);
        }
    }

    static int bindingFor(Context context, HandheldGameAction action) {
        ensureDefaults(context);
        try {
            JSONObject o = new JSONObject(prefs(context).getString(KEY_JSON, "{}"));
            if (o.has(action.name())) return o.getInt(action.name());
        } catch (Exception ignored) {}
        return defaultBinding(action);
    }

    static void setBinding(Context context, HandheldGameAction action, int androidKeyOrAxis) {
        ensureDefaults(context);
        try {
            JSONObject o = new JSONObject(prefs(context).getString(KEY_JSON, "{}"));
            o.put(action.name(), androidKeyOrAxis);
            prefs(context).edit().putString(KEY_JSON, o.toString()).apply();
        } catch (Exception e) {
            android.util.Log.w("NFS", "Failed to save binding for " + action, e);
        }
    }

    static String labelForCode(int code) {
        if (code == AXIS_STEER_X) return "Left stick X";
        if (code == AXIS_LTRIGGER) return "L2 / left trigger";
        if (code == AXIS_RTRIGGER) return "R2 / right trigger";
        return KeyEvent.keyCodeToString(code).replace("KEYCODE_", "");
    }

    private static int defaultBinding(HandheldGameAction action) {
        switch (action) {
            case GAS: return AXIS_RTRIGGER;
            case BRAKE: return AXIS_LTRIGGER;
            case STEER_LEFT:
            case STEER_RIGHT: return AXIS_STEER_X;
            case HANDBRAKE: return KeyEvent.KEYCODE_BUTTON_A;
            case NITRO: return KeyEvent.KEYCODE_BUTTON_B;
            case CAMERA: return KeyEvent.KEYCODE_BUTTON_X;
            case LOOK_BACK: return KeyEvent.KEYCODE_BUTTON_Y;
            case SHIFT_UP: return KeyEvent.KEYCODE_BUTTON_R1;
            case SHIFT_DOWN: return KeyEvent.KEYCODE_BUTTON_L1;
            case PAUSE: return KeyEvent.KEYCODE_BUTTON_START;
            case CONFIRM: return KeyEvent.KEYCODE_BUTTON_A;
            case BACK: return KeyEvent.KEYCODE_BUTTON_B;
            case DPAD_UP: return KeyEvent.KEYCODE_DPAD_UP;
            case DPAD_DOWN: return KeyEvent.KEYCODE_DPAD_DOWN;
            case DPAD_LEFT: return KeyEvent.KEYCODE_DPAD_LEFT;
            case DPAD_RIGHT: return KeyEvent.KEYCODE_DPAD_RIGHT;
            default: return action.defaultScan;
        }
    }

    static float axisValue(MotionEvent event, int axis) {
        try {
            return event.getAxisValue(axis);
        } catch (Exception ignored) {
            return 0f;
        }
    }

    static float steerX(MotionEvent event) {
        return axisValue(event, MotionEvent.AXIS_X);
    }

    static float leftTrigger(MotionEvent event) {
        return Math.max(axisValue(event, MotionEvent.AXIS_LTRIGGER), axisValue(event, MotionEvent.AXIS_BRAKE));
    }

    static float rightTrigger(MotionEvent event) {
        return Math.max(axisValue(event, MotionEvent.AXIS_RTRIGGER), axisValue(event, MotionEvent.AXIS_GAS));
    }
}
