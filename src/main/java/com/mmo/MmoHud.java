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
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.awt.image.BufferedImage;

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

    @Provides
    MmoHudConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(MmoHudConfig.class);
    }

    private Item previousHelment = null;

    @Override
    protected void startUp() {
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
        log.info("Example stopped!");
        overlayManager.remove(playerOverlay);
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

        // OSRS helmet slot is usually index 0
        Item helmet = equipment[EquipmentInventorySlot.HEAD.getSlotIdx()];

        if (previousHelment == null && helmet != null || previousHelment != null && helmet == null || previousHelment.getId() != helmet.getId()) {
            playerOverlay.forceRedraw();
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
                }

            } else if (opponent instanceof Player) {
                Player player = (Player) opponent;

            }
        } else {
            targetOverlay.setTarget(null);
        }

    }

}
