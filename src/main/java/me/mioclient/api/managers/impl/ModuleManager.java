/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.fml.common.eventhandler.EventBus
 *  org.lwjgl.input.Keyboard
 */
package me.mioclient.api.managers.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import me.mioclient.api.events.impl.Render2DEvent;
import me.mioclient.api.events.impl.Render3DEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.mod.Mod;
import me.mioclient.mod.gui.screen.ClickGui;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.impl.client.Desktop;
import me.mioclient.mod.modules.impl.client.DiscordRPC;
import me.mioclient.mod.modules.impl.client.FontMod;
import me.mioclient.mod.modules.impl.client.FovMod;
import me.mioclient.mod.modules.impl.client.HUD;
import me.mioclient.mod.modules.impl.combat.Aura;
import me.mioclient.mod.modules.impl.combat.Blocker;
import me.mioclient.mod.modules.impl.combat.Criticals;
import me.mioclient.mod.modules.impl.combat.PacketExp;
import me.mioclient.mod.modules.impl.exploit.Clip;
import me.mioclient.mod.modules.impl.exploit.FakePearl;
import me.mioclient.mod.modules.impl.misc.BetterChat;
import me.mioclient.mod.modules.impl.misc.KillEffects;
import me.mioclient.mod.modules.impl.misc.UnfocusedCPU;
import me.mioclient.mod.modules.impl.movement.*;
import me.mioclient.mod.modules.impl.player.FakePlayer;
import me.mioclient.mod.modules.impl.player.FreeLook;
import me.mioclient.mod.modules.impl.player.NameProtect;
import me.mioclient.mod.modules.impl.player.Replenish;
import me.mioclient.mod.modules.impl.render.Ambience;
import me.mioclient.mod.modules.impl.render.BreadCrumbs;
import me.mioclient.mod.modules.impl.render.BreakingESP;
import me.mioclient.mod.modules.impl.render.Chams;
import me.mioclient.mod.modules.impl.render.CrystalChams;
import me.mioclient.mod.modules.impl.render.ESP;
import me.mioclient.mod.modules.impl.render.Highlight;
import me.mioclient.mod.modules.impl.render.HoleESP;
import me.mioclient.mod.modules.impl.render.NameTags;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.lwjgl.input.Keyboard;

public class ModuleManager
extends Mod {
    public static Minecraft mc = Minecraft.getMinecraft();
    public ArrayList<Module> modules = new ArrayList();
    public List<Module> sortedLength = new ArrayList<Module>();
    public List<String> sortedAbc = new ArrayList<String>();

    public void init() {
        this.registerModules();
    }

    public void sortModules(Ordering ordering) {
        if (ordering == Ordering.LENGTH) {
            this.sortedLength = this.getEnabledModules().stream().filter(Module::isDrawn).sorted(Comparator.comparing(module -> Managers.TEXT.getStringWidth(HUD.getInstance().lowerCase.getValue() != false ? module.getArrayListInfo().toLowerCase() : module.getArrayListInfo()) * -1)).collect(Collectors.toList());
        } else {
            this.sortedAbc = new ArrayList<String>(this.getEnabledModulesString());
            this.sortedAbc.sort(String.CASE_INSENSITIVE_ORDER);
        }
    }

    public ArrayList<Module> getEnabledModules() {
        ArrayList<Module> modules = new ArrayList<Module>();
        for (Module module : this.modules) {
            if (!module.isOn()) continue;
            modules.add(module);
        }
        return modules;
    }

    public ArrayList<String> getEnabledModulesString() {
        ArrayList<String> modules = new ArrayList<String>();
        for (Module module : this.modules) {
            if (!module.isOn() || !module.isDrawn()) continue;
            modules.add(module.getArrayListInfo());
        }
        return modules;
    }

    public Module getModuleByName(String name) {
        for (Module module : this.modules) {
            if (!module.getName().equalsIgnoreCase(name)) continue;
            return module;
        }
        return null;
    }

    public ArrayList<Module> getModulesByCategory(Category category) {
        ArrayList<Module> modules = new ArrayList<Module>();
        this.modules.forEach(module -> {
            if (module.getCategory() == category) {
                modules.add((Module)module);
            }
        });
        return modules;
    }

    public ArrayList<Module> getModules() {
        return this.modules;
    }

    public List<Category> getCategories() {
        return Arrays.asList(Category.values());
    }

    public void onUnloadPre() {
        this.modules.forEach(((EventBus)MinecraftForge.EVENT_BUS)::unregister);
        this.modules.forEach(Module::onUnload);
    }

    public void onUnloadPost() {
        for (Module module : this.modules) {
            module.enabled.setValue(false);
        }
    }

    public void onKeyInput(int key) {
        if (key == 0 || !Keyboard.getEventKeyState() || ModuleManager.mc.currentScreen instanceof ClickGui) {
            return;
        }
        this.modules.forEach(module -> {
            if (module.getBind().getKey() == key) {
                module.toggle();
            }
        });
    }

    public void onLoad() {
        this.modules.stream().filter(Module::isListening).forEach(((EventBus)MinecraftForge.EVENT_BUS)::register);
        this.modules.forEach(Module::onLoad);
    }

    public void onUpdate() {
        this.modules.stream().filter(Module::isOn).forEach(Module::onUpdate);
    }

    public void onTick() {
        this.modules.stream().filter(Module::isOn).forEach(Module::onTick);
    }

    public void onRender2D(Render2DEvent event) {
        this.modules.stream().filter(Module::isOn).forEach(module -> module.onRender2D(event));
    }

    public void onRender3D(Render3DEvent event) {
        this.modules.stream().filter(Module::isOn).forEach(module -> module.onRender3D(event));
    }

    public void onTotemPop(EntityPlayer player) {
        this.modules.stream().filter(Module::isOn).forEach(module -> module.onTotemPop(player));
    }

    public void onDeath(EntityPlayer player) {
        this.modules.stream().filter(Module::isOn).forEach(module -> module.onDeath(player));
    }

    public void onLogout() {
        this.modules.forEach(Module::onLogout);
    }

    public void onLogin() {
        this.modules.forEach(Module::onLogin);
    }

    private void registerModules() {
        //CLIENT
        this.modules.add(new me.mioclient.mod.modules.impl.client.ClickGui());
        this.modules.add(new FontMod());
        this.modules.add(new HUD());
        this.modules.add(new FovMod());
        this.modules.add(new DiscordRPC());
        this.modules.add(new Desktop());
        //RENDER
        this.modules.add(new Highlight());
        this.modules.add(new HoleESP());
        this.modules.add(new CrystalChams());
        this.modules.add(new Chams());
        this.modules.add(new NameTags());
        this.modules.add(new Ambience());
        this.modules.add(new ESP());
        this.modules.add(new BreadCrumbs());
        this.modules.add(new BreakingESP());
        //COMBAT
        this.modules.add(new PacketExp());
        this.modules.add(new Aura());
        this.modules.add(new Criticals());
        this.modules.add(new Blocker());
        //PLAYER
        this.modules.add(new Replenish());
        this.modules.add(new FakePlayer());
        this.modules.add(new FreeLook());
        this.modules.add(new NameProtect());
        //MISC
        this.modules.add(new BetterChat());
        this.modules.add(new KillEffects());
        this.modules.add(new UnfocusedCPU());
        //MOVEMENT
        this.modules.add(new InventoryMove());
        this.modules.add(new FastWeb());
        this.modules.add(new FastFall());
        this.modules.add(new Sprint());
        this.modules.add(new AntiVoid());
        this.modules.add(new AntiGlide());
        this.modules.add(new Velocity());
        //EXPLOIT
        this.modules.add(new FakePearl());
        this.modules.add(new Clip());
    }

    public static enum Ordering {
        ABC,
        LENGTH;

    }
}

