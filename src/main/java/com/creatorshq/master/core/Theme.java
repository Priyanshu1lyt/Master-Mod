package com.creatorshq.master.core;

/** Immutable colour palette. All colours are ARGB ints. Pure Java: no Minecraft types. */
public record Theme(String id, String name,
                    int background, int panel, int panelAlt, int border,
                    int accent, int accentHover, int text, int textDim,
                    int navBar, int field, int danger, int success) {

    private static final int OPAQUE = 0xFF000000;

    /** Accent colour mixed into a surface; used for hover/selected fills. */
    public int tint(int base, double amount) {
        int r = (int) (((base >> 16) & 0xFF) * (1 - amount) + ((accent >> 16) & 0xFF) * amount);
        int g = (int) (((base >> 8) & 0xFF) * (1 - amount) + ((accent >> 8) & 0xFF) * amount);
        int b = (int) ((base & 0xFF) * (1 - amount) + (accent & 0xFF) * amount);
        return OPAQUE | (r << 16) | (g << 8) | b;
    }
}
