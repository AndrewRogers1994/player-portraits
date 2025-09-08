package com.mmo.overlays.impl;

import com.mmo.MmoHudConfig;
import com.mmo.overlays.HeadOverlay;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.overlay.*;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;

public class PlayerOverlay extends HeadOverlay {
    @Inject
    private ClientThread clientThread;

    private final Client client;

    private Widget headWidget;

    private BufferedImage myImage;

    private BufferedImage barTexture;

    private BufferedImage headContainer;

    private Rectangle lastRender;

    public Widget parent;

    private int dialogId = 5000;


    @Inject
    private PlayerOverlay(Client client) {
        setPosition(OverlayPosition.DYNAMIC);
        setMovable(true);
        setDragTargetable(true);
        setPriority(OverlayPriority.HIGHEST);
        this.client = client;
        loadImage();

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
        createHeadWidget();

    }


    public void loadImage() {
        try {
            myImage = ImageIO.read(getClass().getResourceAsStream("/images/player_hpbar.png"));

            barTexture = ImageIO.read(getClass().getResourceAsStream("/images/hp_gradient_gs.png"));

            headContainer = ImageIO.read(getClass().getResourceAsStream("/images/head_container.png"));


            // Convert if not ARGB
            if (barTexture.getType() != BufferedImage.TYPE_INT_ARGB) {
                BufferedImage converted = new BufferedImage(
                        barTexture.getWidth(), barTexture.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = converted.createGraphics();
                g2.drawImage(barTexture, 0, 0, null);
                g2.dispose();
                barTexture = converted;
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }


    @Override
    public Dimension render(Graphics2D graphics) {
        float scale = (float) config.playerFrameScale() / 100;

        drawPortrait(graphics, scale);
        drawHpBar(graphics, scale);
        drawPrayerBar(graphics, scale);
        graphics.drawImage(headContainer, 0, 0, (int) (headContainer.getWidth() * scale), (int) (headContainer.getHeight() * scale), null);
        drawCombatLevel(graphics, scale);
        return new Dimension((int) (myImage.getWidth() * scale), (int) (myImage.getHeight() * scale));
    }

    private void drawPortrait(Graphics2D graphics, float scale) {
        if (myImage == null || headWidget == null)
            return;

        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (headWidget == null || headWidget.isHidden()) {

            if (lastRender != null) {
                graphics.drawImage(
                        myImage,
                        lastRender.x,
                        lastRender.y,
                        (int) (myImage.getWidth() * scale),
                        (int) (myImage.getHeight() * scale), null);
            }

            return;
        }

        graphics.drawImage(myImage, 0, 0, (int) (myImage.getWidth() * scale), (int) (myImage.getHeight() * scale), null);

    }

    @Override
    public void setPreferredLocation(java.awt.Point preferredLocation) {
        super.setPreferredLocation(preferredLocation);
        createHeadWidget();
    }

    private void drawHpBar(Graphics2D graphics, float scale) {
        int currentHp = client.getBoostedSkillLevel(Skill.HITPOINTS);
        int maxHp = client.getRealSkillLevel(Skill.HITPOINTS);

        int fullWidth = (int) (config.playerHealthBarWidth() * scale);           // width when at 100%
        // fixed height
        double percentRemaining = currentHp / (double) maxHp;

        int barWidth = (int) (fullWidth * percentRemaining);

        // RGBA multipliers (1f = no change)
        // e.g. (1f, 0.5f, 0.5f, 1f) will make the texture more red
        float[] scales = {1f, 0f, 0f, 1f};
        float[] offsets = {0f, 0f, 0f, 0f};

        RescaleOp tint = new RescaleOp(scales, offsets, null);
        BufferedImage tinted = tint.filter(barTexture, null);

        graphics.drawImage(tinted, (int) (config.playerHealthBarXOffset() * scale), (int) (config.playerHealthBarYOffset() * scale), barWidth, (int) (config.playerHealthBarHeight() * scale), null);

        graphics.setColor(new Color(146, 17, 5));

        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);


        Font font = new Font("SansSerif", Font.PLAIN, (int) (config.playerHealthTextSize() * scale));

        graphics.setFont(font);
        // Draw shadow / outline
        graphics.setColor(new Color(100, 10, 4));
        graphics.drawString(currentHp + "/" + maxHp, (int) (config.playerHealthTextXOffset() * scale), (int) (config.playerHealthTextYOffset() * scale));  // offset by 1-2 pixels

        // Draw main text
        graphics.setColor(new Color(238, 209, 149));
        graphics.drawString(currentHp + "/" + maxHp, (int) (config.playerHealthTextXOffset() * scale + 2), (int) (config.playerHealthTextYOffset() * scale + 2));  // offset by 1-2 pixels
    }

    private void drawPrayerBar(Graphics2D graphics, float scale) {
        int currentPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
        int maxPrayer = client.getRealSkillLevel(Skill.PRAYER);

        int fullWidth = (int) (config.playerPrayerBarWidth() * scale);           // width when at 100%
        // fixed height
        double percentRemaining = currentPrayer / (double) maxPrayer;

        int barWidth = (int) (fullWidth * percentRemaining);

        // RGBA multipliers (1f = no change)
        // e.g. (1f, 0.5f, 0.5f, 1f) will make the texture more red
        float[] scales = {0f, 0.5f, 1f, 1f};
        float[] offsets = {0f, 0f, 0f, 0f};

        RescaleOp tint = new RescaleOp(scales, offsets, null);
        BufferedImage tinted = tint.filter(barTexture, null);

        graphics.drawImage(tinted, (int) (config.playerPrayerBarXOffset() * scale), (int) (config.playerPrayerBarYOffset() * scale), barWidth, (int) (config.playerPrayerBarHeight() * scale), null);

        graphics.setColor(new Color(146, 17, 5));

        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);


        Font font = new Font("SansSerif", Font.PLAIN, (int) (config.playerPrayerTextSize() * scale));

        graphics.setFont(font);
        // Draw shadow / outline
        // graphics.setColor(new Color(100,10,4));
        //graphics.drawString(currentHp + "/" + maxHp, config.playerPrayerTextXOffset(),  config.playerPrayerTextYOffset());  // offset by 1-2 pixels

        // Draw main text
        graphics.setColor(new Color(238, 209, 149));
        graphics.drawString(currentPrayer + "/" + maxPrayer, (int) (config.playerPrayerTextXOffset() * scale) + 2, (int) (config.playerPrayerTextYOffset() * scale) + 2);  // offset by 1-2 pixels
    }

    private void drawCombatLevel(Graphics2D graphics, float scale) {
        int combatLevel = client.getLocalPlayer().getCombatLevel();

        Font font = new Font("SansSerif", Font.PLAIN, (int) (10 * scale)); // 24pt font

        graphics.setFont(font);
        // Draw shadow / outline
        graphics.setColor(new Color(238, 209, 149));
        graphics.drawString("" + combatLevel, (int) (32 * scale), (int) (97 * scale));  // offset by 1-2 pixels
    }

    public void createHeadWidget() {
        clientThread.invoke(() ->
        {
            var loc = getPreferredLocation();

            if (!parentSet) {
                return;
            }


            if (parent == null) {
                Widget p = client.getWidget(currentParent, currentChildIndex);

                if (p == null) {
                    return;
                }

                parent = p;
            }

            dialogId++;

            if (headWidget == null) {
                headWidget = parent.createChild(-1, WidgetType.MODEL);
            }

            float scale = ((float) config.playerFrameScale() / 100);

            headWidget.setType(6);
            headWidget.setContentType(0);
            headWidget.setItemId(-1);
            headWidget.setItemQuantity(0);
            headWidget.setItemQuantityMode(2);
            headWidget.setModelId(dialogId);
            headWidget.setModelType(3);
            headWidget.setSpriteId(-1);

            headWidget.setOriginalX((int) (loc.x + (config.playerHeadXOffset()) * scale));
            headWidget.setOriginalY((int) (loc.y + (config.playerHeadYOffset()) * scale));
            headWidget.setOriginalWidth((int) (32 * scale));
            headWidget.setOriginalHeight((int) (32 * scale));

            headWidget.setModelZoom((int) (1200 / scale)); //1200 was what he had before
            headWidget.setRotationX(0);
            headWidget.setAnimationId(588); // 588 was what we had // 614 is angry
            headWidget.setRotationZ(config.playerRotation()); // 1882 was what we had before

            headWidget.revalidate();
        });
    }
}
