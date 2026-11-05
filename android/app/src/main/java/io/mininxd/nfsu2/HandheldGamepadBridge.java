package io.mininxd.nfsu2;

import android.content.Context;
import android.util.AttributeSet;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

/**
 * Captures gamepad events in Java and drives the guest through {@code nativeKey} keyboard
 * emulation. Used on Retroid Pocket devices where the guest DirectInput gamepad path can
 * stay acquired with zero axis data while key emulation is disabled.
 */
public final class HandheldGamepadBridge extends View {
    public interface KeySink {
        void setScanKey(int scan, boolean down);
        boolean drivingControls();
    }

    private final KeySink sink;
    private final boolean[] heldScans = new boolean[256];
    private float deadZone = 0.15f;
    private boolean padConnected;
    private boolean loggedTriggers;

    public HandheldGamepadBridge(Context context, KeySink sink) {
        super(context);
        this.sink = sink;
        setFocusable(true);
        setFocusableInTouchMode(true);
        setClickable(true);
        deadZone = GameOptions.padStickDeadZone(context);
    }

    public HandheldGamepadBridge(Context context, AttributeSet attrs, KeySink sink) {
        super(context, attrs);
        this.sink = sink;
        setFocusable(true);
        setFocusableInTouchMode(true);
        setClickable(true);
    }

    void setDeadZone(float zone) {
        deadZone = Math.max(0.05f, Math.min(0.5f, zone));
    }

    void setPadConnected(boolean connected) {
        padConnected = connected;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        post(this::requestFocus);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!isGamepadEvent(event)) return super.onKeyDown(keyCode, event);
        if (handleButton(keyCode, true)) return true;
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!isGamepadEvent(event)) return super.onKeyUp(keyCode, event);
        if (handleButton(keyCode, false)) return true;
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (!isGamepadSource(event.getSource())) return super.onGenericMotionEvent(event);
        if (event.getAction() != MotionEvent.ACTION_MOVE) return true;
        float steer = HandheldBindings.steerX(event);
        float lt = HandheldBindings.leftTrigger(event);
        float rt = HandheldBindings.rightTrigger(event);
        boolean driving = sink.drivingControls();
        if (driving) {
            setScan(HandheldGameAction.STEER_LEFT.defaultScan, steer < -deadZone);
            setScan(HandheldGameAction.STEER_RIGHT.defaultScan, steer > deadZone);
            setScan(HandheldGameAction.BRAKE.defaultScan, lt > deadZone);
            setScan(HandheldGameAction.GAS.defaultScan, rt > deadZone);
        } else {
            setScan(HandheldGameAction.DPAD_LEFT.defaultScan, steer < -deadZone);
            setScan(HandheldGameAction.DPAD_RIGHT.defaultScan, steer > deadZone);
            setScan(HandheldGameAction.DPAD_UP.defaultScan, HandheldBindings.axisValue(event, MotionEvent.AXIS_Y) < -deadZone);
            setScan(HandheldGameAction.DPAD_DOWN.defaultScan, HandheldBindings.axisValue(event, MotionEvent.AXIS_Y) > deadZone);
        }
        if (lt > deadZone || rt > deadZone) {
            if (!loggedTriggers) {
                loggedTriggers = true;
                android.util.Log.i("NFS", String.format("Handheld triggers L=%.2f R=%.2f", lt, rt));
            }
        }
        return true;
    }

    private boolean handleButton(int keyCode, boolean down) {
        boolean handled = false;
        for (HandheldGameAction action : HandheldGameAction.values()) {
            int bind = HandheldBindings.bindingFor(getContext(), action);
            if (bind < 0 || bind == HandheldBindings.AXIS_STEER_X
                    || bind == HandheldBindings.AXIS_LTRIGGER || bind == HandheldBindings.AXIS_RTRIGGER) {
                continue;
            }
            if (bind != keyCode) continue;
            int scan = scanFor(action, sink.drivingControls());
            setScan(scan, down);
            handled = true;
        }
        return handled;
    }

    private static int scanFor(HandheldGameAction action, boolean driving) {
        if (!driving) {
            switch (action) {
                case HANDBRAKE:
                case CONFIRM: return 0x1c;
                case NITRO:
                case BACK: return 0x01;
                case CAMERA: return 0x39;
                case SHIFT_UP: return 0x2a;
                default: break;
            }
        }
        return action.defaultScan;
    }

    private void setScan(int scan, boolean down) {
        if (scan < 0 || scan >= heldScans.length) return;
        if (heldScans[scan] == down) return;
        heldScans[scan] = down;
        sink.setScanKey(scan, down);
    }

    void releaseAll() {
        for (int scan = 0; scan < heldScans.length; scan++) {
            if (heldScans[scan]) {
                heldScans[scan] = false;
                sink.setScanKey(scan, false);
            }
        }
    }

    private static boolean isGamepadSource(int source) {
        return (source & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
                || (source & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK;
    }

    private static boolean isGamepadEvent(KeyEvent event) {
        return isGamepadSource(event.getSource()) || event.getDeviceId() > 0 && isRetroidPad(event);
    }

    private static boolean isRetroidPad(KeyEvent event) {
        InputDevice d = event.getDevice();
        if (d == null) return false;
        String name = d.getName() != null ? d.getName().toLowerCase() : "";
        return name.contains("retroid") || name.contains("pocket controller");
    }
}
