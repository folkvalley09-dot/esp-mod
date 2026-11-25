package com.example.espmod;

/** Shared freecam state. */
public final class Freecam {
    public static boolean active = false;
    public static double x, y, z;
    public static float yaw, pitch;
    public static double speed = 0.6; // blocks per tick
    private Freecam() {}
}
