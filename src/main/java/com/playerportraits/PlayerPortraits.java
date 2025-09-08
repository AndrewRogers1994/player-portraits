package com.playerportraits;

import com.google.inject.Provides;
import javax.inject.Inject;
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

import java.util.Arrays;

@Slf4j
@PluginDescriptor(
	name = "Player Portraits"
)
public class PlayerPortraits extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private PlayerPortraitsConfig config;

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
	protected void startUp() throws Exception
	{
		log.info("Example started!");

        overlayManager.add(playerOverlay);
        overlayManager.add(targetOverlay);

	}

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event)
    {
        if(event.getGroupId() == WidgetID.RESIZABLE_VIEWPORT_OLD_SCHOOL_BOX_GROUP_ID)
        {
            playerOverlay.SetParentTarget(WidgetID.RESIZABLE_VIEWPORT_OLD_SCHOOL_BOX_GROUP_ID, RESIZABLE_CLASSIC_CHILD_INDEX);
            targetOverlay.SetParentTarget(WidgetID.RESIZABLE_VIEWPORT_OLD_SCHOOL_BOX_GROUP_ID, RESIZABLE_CLASSIC_CHILD_INDEX);
            playerOverlay.CreateHeadWidget();
            return;
        }

        if(event.getGroupId() == WidgetID.RESIZABLE_VIEWPORT_BOTTOM_LINE_GROUP_ID)
        {
            playerOverlay.SetParentTarget(WidgetID.RESIZABLE_VIEWPORT_BOTTOM_LINE_GROUP_ID, RESIZABLE_MODERN_CHILD_INDEX);
            targetOverlay.SetParentTarget(WidgetID.RESIZABLE_VIEWPORT_BOTTOM_LINE_GROUP_ID, RESIZABLE_MODERN_CHILD_INDEX);
            playerOverlay.CreateHeadWidget();
            return;
        }

        if(event.getGroupId() == WidgetID.FIXED_VIEWPORT_GROUP_ID)
        {
            playerOverlay.SetParentTarget(WidgetID.FIXED_VIEWPORT_GROUP_ID, FIXED_CHILD_INDEX);
            targetOverlay.SetParentTarget(WidgetID.FIXED_VIEWPORT_GROUP_ID, FIXED_CHILD_INDEX);
            playerOverlay.CreateHeadWidget();
        }
    }

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
        overlayManager.remove(playerOverlay);
	}

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if(event.getKey().contains("player"))
        {
            playerOverlay.CreateHeadWidget();
        }

        if(event.getKey().contains("enemyRotation"))
        {
            targetOverlay.CreateHeadWidget();
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event)
    {
        // Only care about the equipment container
        if (event.getContainerId() != InventoryID.EQUIPMENT.getId())
            return;

        Item[] equipment = event.getItemContainer().getItems();

        // OSRS helmet slot is usually index 0
        Item helmet = equipment[EquipmentInventorySlot.HEAD.getSlotIdx()];

        if(previousHelment == null && helmet != null || previousHelment != null && helmet == null || previousHelment.getId() != helmet.getId())
        {
            playerOverlay.CreateHeadWidget();
            previousHelment = helmet;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        Actor opponent = client.getLocalPlayer().getInteracting();
        if (opponent != null)
        {
            if (opponent instanceof NPC)
            {
                NPC npc = (NPC) opponent;

                if(npc.getCombatLevel() > 0)
                {
                    targetOverlay.SetTarget(npc);
                    targetOverlay.CreateHeadWidget();
                }

            }
            else if (opponent instanceof Player)
            {
                Player player = (Player) opponent;

            }
        }
        else
        {
            targetOverlay.SetTarget(null);
        }

    }

	@Provides
    PlayerPortraitsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PlayerPortraitsConfig.class);
	}
}
