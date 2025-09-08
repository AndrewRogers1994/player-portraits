package com.mmo;

import com.google.inject.Provides;

import javax.inject.Inject;

import com.mmo.overlays.impl.PlayerOverlay;
import com.mmo.overlays.impl.TargetOverlay;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
   name = "MMO Hud",
   description = "Adds a MMO Type Hud",
   tags = {"mmo", "hud", "overlay", "health", "player", "npc", "head"}
)
@Slf4j
public class MmoHud extends Plugin {
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

    public int RESIZABLE_MODERN_CHILD_INDEX = 66;
    public int FIXED_CHILD_INDEX = 0;
    public int RESIZABLE_CLASSIC_CHILD_INDEX = 34;

    private int currentChildIndex;
    private int currentParent;

    boolean test = false;

    public Widget parent;

    private Item previousHelment = null;

    @Override
    protected void startUp() throws Exception {
        overlayManager.add(playerOverlay);
        overlayManager.add(targetOverlay);

    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        if (event.getGroupId() == WidgetID.RESIZABLE_VIEWPORT_OLD_SCHOOL_BOX_GROUP_ID) {
            playerOverlay.setParentTarget(WidgetID.RESIZABLE_VIEWPORT_OLD_SCHOOL_BOX_GROUP_ID, RESIZABLE_CLASSIC_CHILD_INDEX);
            targetOverlay.setParentTarget(WidgetID.RESIZABLE_VIEWPORT_OLD_SCHOOL_BOX_GROUP_ID, RESIZABLE_CLASSIC_CHILD_INDEX);
            playerOverlay.createHeadWidget();
            return;
        }

        if (event.getGroupId() == WidgetID.RESIZABLE_VIEWPORT_BOTTOM_LINE_GROUP_ID) {
            playerOverlay.setParentTarget(WidgetID.RESIZABLE_VIEWPORT_BOTTOM_LINE_GROUP_ID, RESIZABLE_MODERN_CHILD_INDEX);
            targetOverlay.setParentTarget(WidgetID.RESIZABLE_VIEWPORT_BOTTOM_LINE_GROUP_ID, RESIZABLE_MODERN_CHILD_INDEX);
            playerOverlay.createHeadWidget();
            return;
        }

        if (event.getGroupId() == WidgetID.FIXED_VIEWPORT_GROUP_ID) {
            playerOverlay.setParentTarget(WidgetID.FIXED_VIEWPORT_GROUP_ID, FIXED_CHILD_INDEX);
            targetOverlay.setParentTarget(WidgetID.FIXED_VIEWPORT_GROUP_ID, FIXED_CHILD_INDEX);
            playerOverlay.createHeadWidget();
        }
    }

    @Override
    protected void shutDown() throws Exception {
        log.info("Example stopped!");
        overlayManager.remove(playerOverlay);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (event.getKey().contains("player")) {
            playerOverlay.createHeadWidget();
        }

        if (event.getKey().contains("enemyRotation")) {
            targetOverlay.createHeadWidget();
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        // Only care about the equipment container
        if (event.getContainerId() != InventoryID.EQUIPMENT.getId())
            return;

        Item[] equipment = event.getItemContainer().getItems();

        // OSRS helmet slot is usually index 0
        Item helmet = equipment[EquipmentInventorySlot.HEAD.getSlotIdx()];

        if (previousHelment == null && helmet != null || previousHelment != null && helmet == null || previousHelment.getId() != helmet.getId()) {
            playerOverlay.createHeadWidget();
            previousHelment = helmet;
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
                    targetOverlay.createHeadWidget();
                }

            } else if (opponent instanceof Player) {
                Player player = (Player) opponent;

            }
        } else {
            targetOverlay.setTarget(null);
        }

    }

    @Provides
    MmoHudConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(MmoHudConfig.class);
    }
}
