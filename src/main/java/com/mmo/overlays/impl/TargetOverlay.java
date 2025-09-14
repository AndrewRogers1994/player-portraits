package com.mmo.overlays.impl;

import com.mmo.BarType;
import com.mmo.MmoHud;
import com.mmo.MmoHudConfig;
import com.mmo.overlays.BarRenderer;
import com.mmo.overlays.HeadOverlay;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetModelType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.NPCManager;
import net.runelite.client.plugins.statusbars.StatusBarsConfig;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

@Slf4j
public class TargetOverlay extends HeadOverlay {

    public static final Color HEALTH_COLOR = new Color(225, 35, 0, 125);

    public static final Color HEAL_COLOR = new Color(255, 112, 6, 150);

    @Inject
    private ClientThread clientThread;

    private boolean hasChatHead = false;

    @Inject
    NPCManager npcManager;

    @Inject
    private MmoHudConfig config;

    @Inject
    private MmoHud plugin;

    public NPC target;

    public BufferedImage flippedBarContainer;

    public BufferedImage flippedHeadContainer;

    public BufferedImage flippedBarBackground;

    private Model npcModel;

    private float minX;
    private float maxX;
    private float minY;
    private float maxY;

    private final Map<StatusBarsConfig.BarMode, BarRenderer> barRenderers = new EnumMap<>(StatusBarsConfig.BarMode.class);

    @Inject
    private TargetOverlay() {
        isHidden = true;
        setPosition(OverlayPosition.DYNAMIC);
        setMovable(true);
        setDragTargetable(true);
        setPriority(OverlayPriority.HIGHEST);
        initRenderers();

        setLayer(OverlayLayer.MANUAL);
        drawAfterLayer(WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE);
        drawAfterLayer(WidgetInfo.RESIZABLE_VIEWPORT_OLD_SCHOOL_BOX);
        drawAfterLayer(WidgetInfo.FIXED_VIEWPORT);
    }

    @Override
    public void setPreferredPosition(OverlayPosition preferredPosition) {
        super.setPreferredPosition(preferredPosition);
        drawHeadWidget();
    }

    @Override
    public void setPreferredLocation(java.awt.Point preferredLocation) {
        super.setPreferredLocation(preferredLocation);
        drawHeadWidget();
    }

    @Override
    public void setDefaultHeadProperties() {
        var loc = getPreferredLocation();
        float scale = ((float) config.enemyFrameScale() / 100);
        headWidget.setType(6);
        headWidget.setModelType(WidgetModelType.NPC_CHATHEAD);
        headWidget.setOriginalX((int) (loc.x + 164 * scale));
        headWidget.setOriginalY((int) (loc.y + 32 * scale));
        headWidget.setOriginalWidth((int) (32 * scale));
        headWidget.setOriginalHeight((int) (32 * scale));
        headWidget.setModelZoom((int) (1200 / scale));
        headWidget.setAnimationId(614);
        headWidget.setRotationZ(0);
        headWidget.setHidden(true);
        headWidget.revalidate();
    }

    @Override
    public void forceRedraw() {
        drawHeadWidget();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        float scale = (float) config.enemyFrameScale() / 100;

        if (target == null || isHidden) {
              return new Dimension((int) (220 * scale), (int) (119 * scale));
          }

        renderConfiguredBar(StatusBarsConfig.BarMode.HITPOINTS, BarType.TARGET_BAR1, 38 ,   110 - 5, graphics, scale);

        graphics.drawImage(flippedHeadContainer, 32, 0, (int) (flippedHeadContainer.getWidth() * scale), (int) (flippedHeadContainer.getHeight() * scale), null);
        drawBadge(graphics, scale);
        drawFullModel(scale);

        return new Dimension((int) (220 * scale), (int) (119 * scale));
    }


    public void setTarget(NPC target) {
        if (!parentSet || target == null) {
            return;
        }

        if(this.target == target) {
            return;
        }

        this.target = target;

        setHidden(false);
        if (target.getComposition().getChatheadModels() != null) {
            hasChatHead = true;
            headWidget.setModelId(target.getId());
            drawHeadWidget();
            return;
        } else {
            //npcModel = target.getModel();
            var modelids = target.getComposition().getModels();
            var modelDatas = new ArrayList<ModelData>();
            for(var i = 0; i < modelids.length; i++) {
                modelDatas.add(client.loadModelData(modelids[i]));
            }
            var mergedModel = client.mergeModels(modelDatas.toArray(new ModelData[0]));
            npcModel = mergedModel.light();

            // --- Compute model bounds ---
            float modelMinX = Float.MAX_VALUE;
            float modelMaxX = -Float.MAX_VALUE;
            for (float x : npcModel.getVerticesX()) {
                if (x < modelMinX) modelMinX = x;
                if (x > modelMaxX) modelMaxX = x;
            }

            float modelMinY = Float.MAX_VALUE;
            float modelMaxY = -Float.MAX_VALUE;
            for (float y : npcModel.getVerticesY()) {
                if (y < modelMinY) modelMinY = y;
                if (y > modelMaxY) modelMaxY = y;
            }

            minX = modelMinX;
            minY = modelMinY;
            maxX = modelMaxX;
            maxY = modelMaxY;

        }
        hasChatHead = false;
        barRenderers.clear();
        initRenderers();

    }

    public void clearTarget() {
        if(!parentSet) {
            return;
        }

        this.target = null;

        if(headWidget != null) {
            headWidget.setModelId(-1);
        }
        hasChatHead = false;
        setHidden(true);
    }

    private void initRenderers()
    {
        barRenderers.put(StatusBarsConfig.BarMode.HITPOINTS, new BarRenderer(
                () ->
                {
                    if(target != null)
                    {
                        return npcManager.getHealth(target.getId());
                    }
                    return -1;
                },
                this::calculateHp,
                () -> 0,
                () -> HEALTH_COLOR,
                () -> HEAL_COLOR,
                () -> loadSprite(SpriteID.OrbIcon.HITPOINTS)
        ));
    }

    private void renderConfiguredBar(StatusBarsConfig.BarMode barMode, BarType type, int y, int offset, Graphics2D graphics, float scale) {
        BarRenderer bar = barRenderers.get(barMode);
        if (bar != null) {
            bar.renderBar(config, graphics, type, 0, y, scale, flippedBarBackground, flippedBarContainer, offset);
        }
    }

    private void drawBadge(Graphics2D graphics, float scale) {
        int pipWidth = (int) (plugin.pipImage.getWidth() * scale);
        int pipHeight = (int) (plugin.pipImage.getHeight() * scale);
        int pipX = (int) (5 * scale) - 42 + plugin.headContainer.getWidth();
        int pipY = (int) ((plugin.headContainer.getHeight() - pipHeight - 5) * scale) + 10;

        graphics.drawImage(plugin.pipImage, pipX, pipY, pipWidth, pipHeight, null);

        String displayText = "" + target.getCombatLevel();
        if (displayText == null) return;

        Font pipFont = FontManager.getDefaultFont().deriveFont(17 * scale);
        if (displayText.contains(".")) {
            pipFont = FontManager.getDefaultFont().deriveFont(10 * scale);
        }
        graphics.setFont(pipFont);
        graphics.setColor(new Color(255, 255, 255));

        FontMetrics metrics = graphics.getFontMetrics();
        int textWidth = metrics.stringWidth(displayText);
        int textHeight = metrics.getHeight();

        int margin = displayText.length() <= 2 ? 2 : 0;
        int textX = pipX + (pipWidth - textWidth) / 2 + margin;
        int textY = pipY + (pipHeight - textHeight) / 2 + metrics.getAscent();

        graphics.setColor(new Color(0, 0, 0, 128));
        graphics.drawString(displayText, textX + 1, textY + 1);
        graphics.setColor(new Color(255, 255, 255));
        graphics.drawString(displayText, textX, textY);
    }

    private void drawFullModel(float scale) {

        int drawHeight = (int) (flippedHeadContainer.getHeight() * scale) - 39;
        int drawWidth = (int) (flippedHeadContainer.getWidth() * scale) - 95;

        int xOffset = 126;
        int yOffset = 5;

        Rasterizer r = client.getRasterizer();

        float width = maxX - minX;
        float height = maxY - minY;

        r.setDrawRegion(
                getPreferredLocation().x + xOffset,
                getPreferredLocation().y + yOffset,
                getPreferredLocation().x + xOffset + drawWidth,
                getPreferredLocation().y + yOffset + drawHeight);
        r.resetRasterClipping();


        int faceForwardRotation = 1600;

        if (height > width) {
            int referenceHeight = 462;
            int referenceZoom = 1800;
            float heightScale = referenceHeight / height;

            if (referenceHeight > height) {
                heightScale = height / referenceHeight;
            }

            npcModel.drawFrustum(
                    faceForwardRotation,
                    0,
                    0,
                    (int) (height / heightScale) - 5,
                    0,
                    (int) (referenceZoom * heightScale),
                    0);
        } else {
            int referenceWidth = 186;
            int referenceZoom = 1100;

            float widthScale = referenceWidth / width;

            if (referenceWidth > width) {
                widthScale = widthScale / referenceWidth;
            }

            npcModel.drawFrustum(
                    faceForwardRotation,
                    0,
                    0,
                    (int) 500,
                    0,
                    (int) (referenceZoom / widthScale),
                    0);
        }
    }

    private void drawMissingModel(Graphics2D graphics, float scale) {
        if (hasChatHead) {
            return;
        }
        Font font = new Font("SansSerif", Font.BOLD, 60); // 24pt font

        graphics.setFont(font);
        graphics.setColor(new Color(176, 143, 39));

        // Draw shadow / outline
        graphics.drawString("?", (int) (164 - 3 * scale) + 1, (int) (32 + 30 * scale) + 1);


        // Draw main text
        graphics.setColor(new Color(237, 186, 45));
        graphics.drawString("?", (int) (164 - 3 * scale), (int) (32 + 30 * scale));
    }

    private void drawHeadWidget() {
        clientThread.invoke(() ->
        {
            if(!parentSet) {
                return;
            }

            var loc = getPreferredLocation();

            float scale = ((float) config.enemyFrameScale() / 100);

            headWidget.setOriginalX((int) (loc.x + (plugin.headContainer.getWidth() * scale) - 32));
            headWidget.setOriginalY((int) (loc.y + 32 * scale));
            headWidget.setOriginalWidth((int) (32 * scale));
            headWidget.setOriginalHeight((int) (32 * scale));
            headWidget.setModelZoom((int) (1200 / scale)); //1200 was what he had before
            headWidget.setRotationZ(0); // 1882 was what we had before

            headWidget.revalidate();
        });
    }

    private int calculateHp() {
        if(target == null) {
            return -1;
        }

        int lastMaxHealth = npcManager.getHealth(target.getId());
        int lastRatio = target.getHealthRatio(); // current health proportion
        int healthScale = target.getHealthScale(); // max health proportion

        int health = 0;
        if (lastRatio > 0) {
            int minHealth = 1;
            int maxHealth;
            if (healthScale > 1) {
                if (lastRatio > 1) {
                    // This doesn't apply if healthRatio = 1, because of the special case in the server calculation that
                    // health = 0 forces healthRatio = 0 instead of the expected healthRatio = 1
                    minHealth = (lastMaxHealth * (lastRatio - 1) + healthScale - 2) / (healthScale - 1);
                }
                maxHealth = (lastMaxHealth * lastRatio - 1) / (healthScale - 1);
                if (maxHealth > lastMaxHealth) {
                    maxHealth = lastMaxHealth;
                }
            } else {
                // If healthScale is 1, healthRatio will always be 1 unless health = 0
                // so we know nothing about the upper limit except that it can't be higher than maxHealth
                maxHealth = lastMaxHealth;
            }
            // Take the average of min and max possible healths
            return (minHealth + maxHealth + 1) / 2;
        }

        return -1;
    }
}
