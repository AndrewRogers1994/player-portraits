package com.mmo.overlays.impl;

import com.mmo.MmoHudConfig;
import com.mmo.overlays.HeadOverlay;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetModelType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.NPCManager;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

@Slf4j
public class TargetOverlay extends HeadOverlay {

    @Inject
    private ClientThread clientThread;

    @Inject
    private Client client;

    private BufferedImage myImage;

    private BufferedImage barTexture;

    private Rectangle lastRender;

    private boolean hasCalcedHealth = false;

    private boolean hasChatHead = false;

    @Inject
    NPCManager npcManager;

    public NPC target;

    @Inject
    private TargetOverlay() {
        isHidden = true;
        setPosition(OverlayPosition.DYNAMIC);
        setMovable(true);
        setDragTargetable(true);
        setPriority(OverlayPriority.HIGHEST);
        loadImages();

        lastRender = new Rectangle(0, 0, 200, 50);
        setLayer(OverlayLayer.MANUAL);
        drawAfterLayer(WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE);
        drawAfterLayer(WidgetInfo.RESIZABLE_VIEWPORT_OLD_SCHOOL_BOX);
        drawAfterLayer(WidgetInfo.FIXED_VIEWPORT);
    }

    @Inject
    private MmoHudConfig config;

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
            return new Dimension((int) (myImage.getWidth() * scale), (int) (myImage.getHeight() * scale));
        }

        int currentHealth = calculateHp();

        if (currentHealth == -1 && hasCalcedHealth) {
            return new Dimension((int) (myImage.getWidth() * scale), (int) (myImage.getHeight() * scale));
        }

        drawPortrait(graphics, scale);
        drawHpBar(graphics, scale);
        drawCombatLevel(graphics, scale);
        drawMissingModel(graphics, scale);

        return new Dimension((int) (myImage.getWidth() * scale), (int) (myImage.getHeight() * scale));
    }


    public void loadImages() {
        try {

            myImage = ImageUtil.loadImageResource(getClass(), "/images/hpbar_inverted.png");
            barTexture = ImageUtil.loadImageResource(getClass(), "/images/hp_gradient_gs.png");

            // Convert if not ARGB
            if (barTexture.getType() != BufferedImage.TYPE_INT_ARGB) {
                BufferedImage converted = new BufferedImage(
                        barTexture.getWidth(), barTexture.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = converted.createGraphics();
                g2.drawImage(barTexture, 0, 0, null);
                g2.dispose();
                barTexture = converted;
            }
        } catch (NullPointerException e) {
            log.error("Failed to load images for TargetOverlay: hpbar_inverted.png, hp_gradient_gs.png", e);
        }
    }

    public void setTarget(NPC target) {
        if(!parentSet) {
            return;
        }

        this.target = target;
        hasCalcedHealth = false;
        if(target != null) {
            setHidden(false);
            if(target.getComposition().getChatheadModels() != null) {
                hasChatHead = true;
                headWidget.setModelId(target.getId());
                drawHeadWidget();
                return;
            }
            hasChatHead = false;
            return;
        }
        headWidget.setModelId(-1);
        hasChatHead = false;
        setHidden(true);
    }

    private void drawPortrait(Graphics2D graphics, float scale) {
        if (myImage == null)
            return;

        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        if (headWidget == null || headWidget.isHidden()) {

            if (lastRender != null) {
                graphics.drawImage(myImage, lastRender.x, lastRender.y, (int) (myImage.getWidth() * scale), (int) (myImage.getHeight() * scale), null);
            }

            return;
        }

        graphics.drawImage(myImage, 0, 0, (int) (myImage.getWidth() * scale), (int) (myImage.getHeight() * scale), null);

    }

    private void drawHpBar(Graphics2D graphics, float scale) {
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int maxHealth = npcManager.getHealth(target.getId());


        int currentHealth = calculateHp();


        if (currentHealth == -1) {
            currentHealth = maxHealth;
        }

        int fullWidth = (int) (128 * scale);           // fixed width
        int height = (int) (29 * scale);               // fixed height
        double percentRemaining = currentHealth / (double) maxHealth;

        int barWidth = (int) (fullWidth * percentRemaining);

        // RGBA multipliers (1f = no change)
        // e.g. (1f, 0.5f, 0.5f, 1f) will make the texture more red
        float[] scales = {1f, 0f, 0f, 1f};
        float[] offsets = {0f, 0f, 0f, 0f};

        RescaleOp tint = new RescaleOp(scales, offsets, null);
        BufferedImage tinted = tint.filter(barTexture, null);

        graphics.drawImage(tinted, (int) (7 * scale), (int) (36 * scale), barWidth, height, null);


        graphics.setColor(new Color(146, 17, 5));
        //  graphics.fillRoundRect(6, 36, barWidth, 28, 5, 5);


        Font font = new Font("SansSerif", Font.PLAIN, (int) (18 * scale));
        graphics.setFont(font);

        if (currentHealth <= 0) {
            // Draw shadow / outline
            graphics.setColor(new Color(100, 10, 4));
            graphics.drawString("100%", (int) (40 * scale) + 2, (int) (57 * scale) + 2);

            // Draw main text
            graphics.setColor(new Color(238, 209, 149));
            graphics.drawString("100%", (int) (40 * scale), (int) (57 * scale));
            return;
        }


        // Draw shadow / outline
        graphics.setColor(new Color(100, 10, 4));
        graphics.drawString(currentHealth + "/" + maxHealth, (int) (40 * scale) + 2, (int) (57 * scale) + 2);

        // Draw main text
        graphics.setColor(new Color(238, 209, 149));
        graphics.drawString(currentHealth + "/" + maxHealth, (int) (40 * scale), (int) (57 * scale));

    }

    private void drawCombatLevel(Graphics2D graphics, float scale) {
        int combatLevel = target.getCombatLevel();

        Font font = new Font("SansSerif", Font.PLAIN, 10); // 24pt font

        graphics.setFont(font);
        // Draw shadow / outline
        graphics.setColor(new Color(238, 209, 149));
        graphics.drawString("" + combatLevel, (int) (175 * scale), (int) (97 * scale));  // offset by 1-2 pixels
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

            headWidget.setOriginalX((int) (loc.x + 164 * scale));
            headWidget.setOriginalY((int) (loc.y + 32 * scale));
            headWidget.setOriginalWidth((int) (32 * scale));
            headWidget.setOriginalHeight((int) (32 * scale));
            headWidget.setModelZoom((int) (1200 / scale)); //1200 was what he had before
            headWidget.setRotationZ(0); // 1882 was what we had before

            headWidget.revalidate();
        });
    }

    private int calculateHp() {

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
            hasCalcedHealth = true;
            return (minHealth + maxHealth + 1) / 2;
        }

        return -1;
    }
}
