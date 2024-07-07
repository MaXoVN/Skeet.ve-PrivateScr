/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiScreen
 */
package me.mioclient.mod.modules.impl.client;

import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import net.minecraft.client.gui.GuiScreen;

public class Appearance
extends Module {
    public Appearance() {
        super("Appearance", "Drag HUD elements all over your screen.", Category.CLIENT);
    }

    @Override
    public void onEnable() {
        mc.displayGuiScreen((GuiScreen) me.mioclient.mod.gui.screen.Appearance.getClickGui());
    }

    @Override
    public void onTick() {
        if (!(me.mioclient.mod.modules.impl.client.Appearance.mc.currentScreen instanceof me.mioclient.mod.gui.screen.Appearance)) {
            this.disable();
        }
    }
}

