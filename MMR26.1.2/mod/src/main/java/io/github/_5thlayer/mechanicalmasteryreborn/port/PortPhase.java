// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.port;

import net.minecraft.util.StringRepresentable;

/** How a Port looks, by whether its team plays its challenge: closed (dark), open (lit) or won (a steady glow). */
public enum PortPhase implements StringRepresentable {

    CLOSED("closed", 0),
    OPEN("open", 7),
    WON("won", 15);

    private final String name;
    private final int light;

    PortPhase(String name, int light) {
        this.name = name;
        this.light = light;
    }

    /** The light the Port gives off. */
    int light() {
        return light;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
