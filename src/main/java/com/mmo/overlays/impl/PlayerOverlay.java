package com.mmo.overlays.impl;

import com.mmo.MmoHudConfig;
import com.mmo.overlays.HeadOverlay;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

@Slf4j
public class PlayerOverlay extends HeadOverlay {

    @Inject
    private ClientThread clientThread;

    @Inject
    private Client client;

    private BufferedImage myImage;

    private BufferedImage barTexture;

    private BufferedImage headContainer;

    private Rectangle lastRender;

    private int dialogId = 5000;


    @Inject
    private PlayerOverlay() {
        setPosition(OverlayPosition.DYNAMIC);
        setMovable(true);
        setDragTargetable(true);
        setPriority(OverlayPriority.HIGHEST);
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
        float scale = ((float) config.playerFrameScale() / 100);

        headWidget.setType(6);
        headWidget.setModelId(dialogId);
        headWidget.setModelType(3);
        headWidget.setOriginalX((int) (loc.x + (config.playerHeadXOffset()) * scale));
        headWidget.setOriginalY((int) (loc.y + (config.playerHeadYOffset()) * scale));
        headWidget.setOriginalWidth((int) (32 * scale));
        headWidget.setOriginalHeight((int) (32 * scale));
        headWidget.setModelZoom((int) (1200 / scale));
        headWidget.setAnimationId(588);
        headWidget.setRotationZ(config.playerRotation());
        headWidget.revalidate();
    }

    @Override
    public void forceRedraw() {
        dialogId++;
        headWidget.setModelId(dialogId);
        drawHeadWidget();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if(isHidden || !parentSet) {
            return new Dimension(0,0);
        }

        float scale = (float) config.playerFrameScale() / 100;

        drawPortrait(graphics, scale);
        drawHpBar(graphics, scale);
        drawPrayerBar(graphics, scale);
        graphics.drawImage(headContainer, 0, 0, (int) (headContainer.getWidth() * scale), (int) (headContainer.getHeight() * scale), null);
        drawCombatLevel(graphics, scale);
        return new Dimension((int) (myImage.getWidth() * scale), (int) (myImage.getHeight() * scale));
    }

    public void loadImage() {
        try {
            myImage = ImageUtil.loadImageResource(getClass(), "/images/player_hpbar.png");
            barTexture = ImageUtil.loadImageResource(getClass(), "/images/hp_gradient_gs.png");
            headContainer = ImageUtil.loadImageResource(getClass(), "/images/head_container.png");


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
            log.error("Failed to load images for PlayerOverlay: player_hpbar.png, hp_gradient_gs.png, head_container.png", e);
        }
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

    private void drawHeadWidget() {
        clientThread.invoke(() ->
        {
            if(!parentSet) {
                return;
            }

            var loc = getPreferredLocation();

            float scale = ((float) config.playerFrameScale() / 100);

            headWidget.setOriginalX((int) (loc.x + (config.playerHeadXOffset()) * scale));
            headWidget.setOriginalY((int) (loc.y + (config.playerHeadYOffset()) * scale));
            headWidget.setOriginalWidth((int) (32 * scale));
            headWidget.setOriginalHeight((int) (32 * scale));
            headWidget.setModelZoom((int) (1200 / scale)); //1200 was what he had before
            headWidget.setRotationZ(config.playerRotation()); // 1882 was what we had before

            headWidget.revalidate();
        });
    }
}
