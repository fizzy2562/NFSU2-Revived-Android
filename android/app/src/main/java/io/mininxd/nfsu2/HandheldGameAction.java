package io.mininxd.nfsu2;

/** Logical controls mapped from physical gamepad input (scan codes are DirectInput keyboard emulation). */
public enum HandheldGameAction {
    STEER_LEFT(0xcb),
    STEER_RIGHT(0xcd),
    GAS(0xc8),
    BRAKE(0xd0),
    HANDBRAKE(0x39),
    NITRO(0x38),
    CAMERA(0x2e),
    LOOK_BACK(0x30),
    SHIFT_UP(0x2a),
    SHIFT_DOWN(0x1d),
    PAUSE(0x01),
    CONFIRM(0x1c),
    BACK(0x01),
    DPAD_UP(0xc8),
    DPAD_DOWN(0xd0),
    DPAD_LEFT(0xcb),
    DPAD_RIGHT(0xcd);

    public final int defaultScan;

    HandheldGameAction(int defaultScan) {
        this.defaultScan = defaultScan;
    }
}
