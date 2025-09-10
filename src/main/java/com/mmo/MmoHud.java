package com.mmo;

import com.google.inject.Provides;

import javax.inject.Inject;

import com.mmo.overlays.impl.PlayerOverlay;
import com.mmo.overlays.impl.TargetOverlay;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.widgets.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.combatlevel.CombatLevelConfig;
import net.runelite.client.plugins.combatlevel.CombatLevelPlugin;
import net.runelite.client.plugins.itemstats.ItemStatPlugin;
import net.runelite.client.ui.overlay.OverlayManager;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

@PluginDescriptor(
   name = "MMO Hud",
   description = "Adds a MMO Type Hud",
   tags = {"mmo", "hud", "overlay", "health", "player", "npc", "head"}
)
@PluginDependency(ItemStatPlugin.class)
@PluginDependency(CombatLevelPlugin.class)
@Slf4j
public class MmoHud extends Plugin {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.###");
    private static final String CONFIG_GROUP = "combatlevel";
    private static final String ATTACK_RANGE_CONFIG_KEY = "wildernessAttackLevelRange";
    private static final Pattern WILDERNESS_LEVEL_PATTERN = Pattern.compile("^Level: (\\d+)$");
    private static final int MIN_COMBAT_LEVEL = 3;
    private static final String COMBAT_LEVEL_SECTION_TEXT = "Combat Level:";
    private static final Color CHARACTER_SUMMARY_GREEN = Color.decode("#0dc10d");

    @Inject
    private Client client;

    @Inject
    private MmoHudConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private PlayerOverlay playerOverlay;

    @Inject
    private TargetOverlay targetOverlay;

    @Inject
    public ConfigManager configManager;

    public CombatLevelConfig combatLevelConfig;

    @Provides
    MmoHudConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(MmoHudConfig.class);
    }

    private int previousHelment = -1;
    public String combatLevelStr;

    @Override
    protected void startUp() {
        combatLevelConfig = configManager.getConfig(CombatLevelConfig.class);
        double combatLevel = Experience.getCombatLevelPrecise(
                client.getRealSkillLevel(Skill.ATTACK),
                client.getRealSkillLevel(Skill.STRENGTH),
                client.getRealSkillLevel(Skill.DEFENCE),
                client.getRealSkillLevel(Skill.HITPOINTS),
                client.getRealSkillLevel(Skill.MAGIC),
                client.getRealSkillLevel(Skill.RANGED),
                client.getRealSkillLevel(Skill.PRAYER)
        );
        combatLevelStr = DECIMAL_FORMAT.format(combatLevel);

        overlayManager.add(playerOverlay);
        overlayManager.add(targetOverlay);
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        int groupId = event.getGroupId();
        int packed = -1;

        if(groupId == InterfaceID.POH_LOADING) {
            playerOverlay.setHidden(true);
            return;
        }

        if (groupId == InterfaceID.TOPLEVEL_OSRS_STRETCH) {
            packed = InterfaceID.ToplevelOsrsStretch.GAMEFRAME;
        } else if (groupId == InterfaceID.TOPLEVEL_PRE_EOC) {
            packed = InterfaceID.ToplevelPreEoc.GAMEFRAME;
        } else if (groupId == InterfaceID.TOPLEVEL) {
            packed = InterfaceID.Toplevel.GAMEFRAME;
        }

        int child = WidgetUtil.componentToId(packed);

        if (child != -1) {
            playerOverlay.setParentTarget(groupId, child);
            targetOverlay.setParentTarget(groupId, child);
        }
    }

    @Subscribe
    public void onWidgetClosed(WidgetClosed event)
    {
        int groupId = event.getGroupId();

        if (groupId == InterfaceID.POH_LOADING) {
            playerOverlay.setHidden(false);
        }
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(playerOverlay);
    }

    @Subscribe
    private void onStatChanged(StatChanged statChanged)
    {
        Skill skill = statChanged.getSkill();
        if (skill == Skill.ATTACK || skill == Skill.DEFENCE || skill == Skill.STRENGTH || skill == Skill.HITPOINTS
                || skill == Skill.MAGIC || skill == Skill.PRAYER || skill == Skill.RANGED)
        {
            double combatLevel = Experience.getCombatLevelPrecise(
                    client.getRealSkillLevel(Skill.ATTACK),
                    client.getRealSkillLevel(Skill.STRENGTH),
                    client.getRealSkillLevel(Skill.DEFENCE),
                    client.getRealSkillLevel(Skill.HITPOINTS),
                    client.getRealSkillLevel(Skill.MAGIC),
                    client.getRealSkillLevel(Skill.RANGED),
                    client.getRealSkillLevel(Skill.PRAYER)
            );
            combatLevelStr = DECIMAL_FORMAT.format(combatLevel);
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (event.getKey().contains("player")) {
            playerOverlay.forceRedraw();
        }

        if (event.getKey().contains("enemyRotation")) {
            targetOverlay.forceRedraw();
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        // Only care about the equipment container
        if (event.getContainerId() != InventoryID.WORN)
            return;

        Item[] equipment = event.getItemContainer().getItems();

        Item helmet = equipment[EquipmentInventorySlot.HEAD.getSlotIdx()];

        if (previousHelment == -1 && helmet != null || previousHelment != -1 && helmet == null || previousHelment != helmet.getId()) {
            playerOverlay.forceRedraw();
            previousHelment = helmet.getId();
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        Actor opponent = client.getLocalPlayer().getInteracting();
        if (opponent != null) {
            if (opponent instanceof NPC) {
                NPC npc = (NPC) opponent;
                if (npc.getCombatLevel() > 0) {
                    targetOverlay.setTarget(npc);
                }
            }
        } else {
            targetOverlay.setTarget(null);
        }

    }

}
