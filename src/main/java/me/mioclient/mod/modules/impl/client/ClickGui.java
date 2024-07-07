package me.mioclient.mod.modules.impl.client;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Color;
import me.mioclient.api.events.impl.ClientEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.mod.commands.Command;
import me.mioclient.mod.gui.screen.MioClickGui;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class ClickGui extends Module {
    public static ClickGui INSTANCE;
    public final Setting<String> prefix = this.add(new Setting<>("Prefix", ";"));
    public final Setting<Boolean> guiMove = this.add(new Setting<>("GuiMove", true));
    public final Setting<Style> style = this.add(new Setting<>("Style", Style.DOTGOD));
    public final Setting<Integer> height = this.add(new Setting<>("ButtonHeight", 4, 1, 5));
    public final Setting<Boolean> blur = this.add(new Setting<>("Blur", false));
    public final Setting<Boolean> line = this.add(new Setting<>("Line", true).setParent());
    public final Setting<Boolean> rollingLine = this.add(new Setting<>("RollingLine", true, v -> this.line.isOpen()));
    public final Setting<Boolean> rect = this.add(new Setting<>("Rect", true).setParent());
    public final Setting<Boolean> colorRect = this.add(new Setting<>("ColorRect", false, v -> this.rect.isOpen()));
    public final Setting<Boolean> gear = this.add(new Setting<>("Gear", true));
    public final Setting<Boolean> particles = this.add(new Setting<>("Particles", true).setParent());
    public final Setting<Boolean> colorParticles = this.add(new Setting<>("ColorParticles", true, v -> this.particles.isOpen()));
    public final Setting<Boolean> background = this.add(new Setting<>("Background", true));
    public final Setting<Boolean> cleanGui = this.add(new Setting<>("CleanGui", false));
    public final Setting<Color> color = this.add(new Setting<>("Color", new Color(125, 125, 213)).hideAlpha());
    public final Setting<Boolean> rainbow = this.add(new Setting<>("Rainbow", false).setParent());
    public final Setting<Rainbow> rainbowMode = this.add(new Setting<>("Mode", Rainbow.NORMAL, v -> this.rainbow.isOpen()));
    public final Setting<Float> rainbowBrightness = this.add(new Setting<>("Brightness", 150.0f, 1.0f, 255.0f, v -> this.rainbow.isOpen() && this.rainbowMode.getValue() == Rainbow.NORMAL));
    public final Setting<Float> rainbowSaturation = this.add(new Setting<>("Saturation", 150.0f, 1.0f, 255.0f, v -> this.rainbow.isOpen() && this.rainbowMode.getValue() == Rainbow.NORMAL));
    public final Setting<Color> secondColor = this.add(new Setting<>("SecondColor", new Color(255, 255, 255), v -> this.rainbow.isOpen() && this.rainbowMode.getValue() == Rainbow.DOUBLE).hideAlpha());
    public final Setting<HudRainbow> hudRainbow = this.add(new Setting<>("HUD", HudRainbow.STATIC, v -> this.rainbow.isOpen()));
    public final Setting<Integer> rainbowDelay = this.add(new Setting<>("Delay", 240, 0, 600, v -> this.rainbow.isOpen()));
    private final KeyBinding[] keys;

    public ClickGui() {
        super("ClickGui", "Opens the ClickGui.", Category.CLIENT, true);
        this.keys = new KeyBinding[]{
                ClickGui.mc.gameSettings.keyBindForward,
                ClickGui.mc.gameSettings.keyBindBack,
                ClickGui.mc.gameSettings.keyBindLeft,
                ClickGui.mc.gameSettings.keyBindRight,
                ClickGui.mc.gameSettings.keyBindJump,
                ClickGui.mc.gameSettings.keyBindSprint
        };
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (ClickGui.mc.world != null) {
            mc.displayGuiScreen(MioClickGui.INSTANCE);
        }
        if (this.blur.getValue()) {
            ClickGui.mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
        }
    }

    @Override
    public void onLoad() {
        Managers.COLORS.setCurrent(this.color.getValue());
        Managers.COMMANDS.setPrefix(this.prefix.getValue());
    }

    @Override
    public void onTick() {
        if (!(ClickGui.mc.currentScreen instanceof MioClickGui) && !(ClickGui.mc.currentScreen instanceof GuiMainMenu)) {
            this.disable();
        }
    }

    @Override
    public void onUpdate() {
        if (this.guiMove.getValue() && !(ClickGui.mc.currentScreen instanceof GuiChat)) {
            for (KeyBinding key : this.keys) {
                KeyBinding.setKeyBindState(key.getKeyCode(), Keyboard.isKeyDown(key.getKeyCode()));
            }
        } else {
            for (KeyBinding key : this.keys) {
                if (!Keyboard.isKeyDown(key.getKeyCode())) {
                    KeyBinding.setKeyBindState(key.getKeyCode(), false);
                }
            }
        }
    }

    @SubscribeEvent
    public void onSettingChange(ClientEvent event) {
        if (event.getStage() == 2 && event.getSetting().getMod().equals(this)) {
            if (event.getSetting().equals(this.prefix)) {
                Managers.COMMANDS.setPrefix(this.prefix.getPlannedValue());
                Command.sendMessage("Prefix set to " + ChatFormatting.DARK_GRAY + Managers.COMMANDS.getCommandPrefix());
            }
            Managers.COLORS.setCurrent(this.color.getValue());
        }
    }

    public int getButtonHeight() {
        return 11 + this.height.getValue();
    }

    public enum Style {
        DOTGOD,
        // Add other styles if necessary
    }

    public enum Rainbow {
        NORMAL,
        PLAIN,
        DOUBLE
    }

    public enum HudRainbow {
        STATIC,
        ROLLING
    }
}