/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.audio.ISound
 *  net.minecraft.client.audio.PositionedSoundRecord
 *  net.minecraft.init.SoundEvents
 *  net.minecraft.util.SoundEvent
 */
package me.mioclient.mod.gui.click.items.buttons;

import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.gui.click.Component;
import me.mioclient.mod.gui.click.items.Item;
import me.mioclient.mod.gui.screen.ClickGui;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;

public class Button
extends Item {
    private boolean state;

    public Button(String name) {
        super(name);
        this.height = me.mioclient.mod.modules.impl.client.ClickGui.INSTANCE.getButtonHeight();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        boolean dotgod = me.mioclient.mod.modules.impl.client.ClickGui.INSTANCE.style.getValue() == me.mioclient.mod.modules.impl.client.ClickGui.Style.DOTGOD;

        if (dotgod) {
            RenderUtil.drawRect(this.x, this.y, this.x + (float) this.width, this.y + (float) this.height - 0.5f,
                    this.getState() ? (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(65) : Managers.COLORS.getCurrentWithAlpha(90)) :
                            (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(26) : Managers.COLORS.getCurrentWithAlpha(35)));
            Managers.TEXT.drawStringWithShadow(this.getName(), this.x + 2.3f, this.y - 2.0f - (float) ClickGui.INSTANCE.getTextOffset(),
                    this.getState() ? Managers.COLORS.getCurrentGui(240) : 0xB0B0B0);
        } else {
            RenderUtil.drawRect(this.x, this.y, this.x + (float) this.width, this.y + (float) this.height - 0.5f,
                    this.getState() ? (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(120) : Managers.COLORS.getCurrentWithAlpha(200)) :
                            (!this.isHovering(mouseX, mouseY) ? 0x11555555 : -2007673515));
            Managers.TEXT.drawStringWithShadow(this.getName(), this.x + 2.3f, this.y - 2.0f - (float) ClickGui.INSTANCE.getTextOffset(),
                    this.getState() ? -1 : -5592406);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY)) {
            this.onMouseClick();
        }
    }

    public void onMouseClick() {
        this.state = !this.state;
        this.toggle();
        mc.getSoundHandler().playSound((ISound)PositionedSoundRecord.getMasterRecord((SoundEvent)SoundEvents.UI_BUTTON_CLICK, (float)1.0f));
    }

    public void toggle() {
    }

    public boolean getState() {
        return this.state;
    }

    @Override
    public int getHeight() {
        return me.mioclient.mod.modules.impl.client.ClickGui.INSTANCE.getButtonHeight() - 1;
    }

    public boolean isHovering(int mouseX, int mouseY) {
        for (Component component : ClickGui.INSTANCE.getComponents()) {
            if (!component.drag) continue;
            return false;
        }
        return (float)mouseX >= this.getX() && (float)mouseX <= this.getX() + (float)this.getWidth() && (float)mouseY >= this.getY() && (float)mouseY <= this.getY() + (float)this.height;
    }
}

