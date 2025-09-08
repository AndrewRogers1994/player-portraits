package com.mmo.overlays;

import net.runelite.client.ui.overlay.Overlay;

public abstract class HeadOverlay extends Overlay {
    protected int currentChildIndex;
    protected int currentParent;
    protected boolean parentSet = false;

    public void setParentTarget(int parentId, int childId) {
        currentParent = parentId;
        currentChildIndex = childId;
        parentSet = true;
    }
}
