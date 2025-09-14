package com.mmo.overlays;

import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;

import javax.inject.Inject;
import java.awt.image.BufferedImage;

public abstract class HeadOverlay extends Overlay {

    @Inject
    private SpriteManager spriteManager;
    @Inject
    protected Client client;
    protected int currentChildIndex;
    protected int currentParent;
    protected boolean parentSet = false;
    protected Widget parent;
    protected Widget headWidget;
    protected boolean isHidden = false;

    public void setParentTarget(int parentId, int childId) {
        currentParent = parentId;
        currentChildIndex = childId;
        parentSet = true;
        parent = client.getWidget(currentParent, currentChildIndex);

        if(parent != null) {
            headWidget = parent.createChild(-1, WidgetType.MODEL);

            if(headWidget != null) {
                setDefaultHeadProperties();
            }
        }
    }

    public void setHidden(boolean isHidden) {
        this.isHidden = isHidden;

        if(headWidget != null) {
            headWidget.setHidden(isHidden);
            headWidget.revalidate();
        }
    }

    public BufferedImage loadSprite(int spriteId) {

        return spriteManager.getSprite(spriteId, 0);
    }

    public abstract void setDefaultHeadProperties();
    public abstract void forceRedraw();
}
