package com.mmo.overlays.impl;

import com.mmo.MmoHud;
import com.mmo.MmoHudConfig;
import com.mmo.config.StatusPosition;
import com.mmo.config.TextScale;
import com.mmo.config.FontType;
import com.mmo.config.BarType;
import com.mmo.overlays.HeadOverlay;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
public class PlayerOverlay extends HeadOverlay {

    @Inject
    private ClientThread clientThread;

    @Inject
    private Client client;

    @Inject
    private MmoHud mmoHud;

    private BufferedImage myImage;

    private BufferedImage headContainer;

    private Rectangle lastRender;

    private int dialogId = 5000;

    private BufferedImage healthIcon;
    private BufferedImage prayerIcon;
    
    private int previousHp = -1;
    private int previousPrayer = -1;
    private int animatedHp = -1;
    private int animatedPrayer = -1;
    private long lastHpChangeTime = 0;
    private long lastPrayerChangeTime = 0;
    private static final long ANIMATION_DURATION = 800;
    private static final int ANIMATION_THRESHOLD = 4;

    @Inject
    private PlayerOverlay(SpriteManager spriteManager) {
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
        headWidget.setOriginalX((int) (loc.x + 28 * scale));
        headWidget.setOriginalY((int) (loc.y + 32 * scale));
        headWidget.setOriginalWidth((int) (32 * scale));
        headWidget.setOriginalHeight((int) (32 * scale));
        headWidget.setModelZoom((int) (1200 / scale));
        headWidget.setAnimationId(588);
        headWidget.setRotationZ(0);
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

        drawHpBar(graphics, scale);
        drawPortrait(graphics, scale);
        drawPrayerBar(graphics, scale);
        graphics.drawImage(headContainer, 0, 0, (int) (headContainer.getWidth() * scale), (int) (headContainer.getHeight() * scale), null);
        drawCombatLevel(graphics, scale);
        return new Dimension((int) (myImage.getWidth() * scale), (int) (myImage.getHeight() * scale));
    }

    public void loadImage() {
        try {
            healthIcon = ImageUtil.loadImageResource(getClass(), "/images/hp_icon.png");
            prayerIcon = ImageUtil.loadImageResource(getClass(), "/images/prayer_icon.png");
            myImage = ImageUtil.loadImageResource(getClass(), "/images/player_hpbar.png");
            headContainer = ImageUtil.loadImageResource(getClass(), "/images/head_container.png");
        } catch (NullPointerException e) {
            log.error("Failed to load images for PlayerOverlay: player_hpbar.png, hp_gradient_gs.png, head_container.png", e);
        }
    }

    private void drawHpBar(Graphics2D graphics, float scale) {
        int currentHp = client.getBoostedSkillLevel(Skill.HITPOINTS);
        int maxHp = client.getRealSkillLevel(Skill.HITPOINTS);
        int animatedHpValue = getAnimatedValue(currentHp, previousHp, "hp");
        String text = animatedHpValue + "/" + maxHp;
        drawBar(graphics, scale, BarType.HEALTH, animatedHpValue, maxHp, text, healthIcon);
    }

    private void drawPrayerBar(Graphics2D graphics, float scale) {
        int currentPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
        int maxPrayer = client.getRealSkillLevel(Skill.PRAYER);
        int animatedPrayerValue = getAnimatedValue(currentPrayer, previousPrayer, "prayer");
        String text = animatedPrayerValue + "/" + maxPrayer;
        drawBar(graphics, scale, BarType.PRAYER, animatedPrayerValue, maxPrayer, text, prayerIcon);
    }

    private int getAnimatedValue(int current, int previous, String type) {
        long currentTime = System.currentTimeMillis();
        
        // Check if value changed significantly
        if (current != previous) {
            int change = Math.abs(current - previous);
            if (change >= ANIMATION_THRESHOLD) {
                // Start animation for significant changes
                if (type.equals("hp")) {
                    lastHpChangeTime = currentTime;
                    animatedHp = previous;
                    previousHp = current;
                } else if (type.equals("prayer")) {
                    lastPrayerChangeTime = currentTime;
                    animatedPrayer = previous;
                    previousPrayer = current;
                }
                return previous; // Return previous value to start animation
            } else {
                // Small changes, no animation
                if (type.equals("hp")) {
                    previousHp = current;
                    animatedHp = current;
                } else if (type.equals("prayer")) {
                    previousPrayer = current;
                    animatedPrayer = current;
                }
                return current;
            }
        }
        
        // Handle animation progress
        long lastTime = type.equals("hp") ? lastHpChangeTime : lastPrayerChangeTime;
        if (lastTime > 0) {
            long elapsed = currentTime - lastTime;
            double progress = Math.min(1.0, (double) elapsed / ANIMATION_DURATION);

            int startValue = type.equals("hp") ? animatedHp : animatedPrayer;
            int newAnimated = (int) (startValue + (current - startValue) * progress);

            if (type.equals("hp")) {
                animatedHp = newAnimated;
            } else {
                animatedPrayer = newAnimated;
            }

            if (progress >= 1.0) {
                if (type.equals("hp")) {
                    lastHpChangeTime = 0;
                } else {
                    lastPrayerChangeTime = 0;
                }
            }
            
            return newAnimated;
        }
        
        // No animation, return current value
        return current;
    }

    private int calculateIconPosition(StatusPosition position, int barStartX, int fullWidth, int iconWidth, int margin) {
        switch (position) {
            case OFF:
                return -1;
            case LEFT:
                return barStartX + margin;
            case CENTER:
                return barStartX + (fullWidth - iconWidth) / 2;
            case RIGHT:
                return barStartX + fullWidth - iconWidth - margin;
            default:
                return barStartX;
        }
    }

    private int calculateTextPosition(Graphics2D graphics, StatusPosition position, int barStartX, int fullWidth, String text, int margin, float scale) {
        if (position == StatusPosition.OFF) {
            return -1;
        }

        Font baseFont = getFontForType(config.playerTextFont(), (int) (18 * scale));
        Graphics2D tempGraphics = (Graphics2D) graphics.create();
        tempGraphics.setFont(baseFont);
        FontMetrics metrics = tempGraphics.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        tempGraphics.dispose();

        switch (position) {
            case LEFT:
                return barStartX + margin;
            case CENTER:
                return barStartX + (fullWidth - textWidth) / 2;
            case RIGHT:
                return barStartX + fullWidth - textWidth - margin;
            default:
                return barStartX;
        }
    }

    private int adjustTextForIconOverlap(Graphics2D graphics, int textX, int iconX, int iconWidth, String text, float scale) {
        int spacing = (int) (2 * scale);
        int iconEndX = iconX + iconWidth;
        
        if (config.playerStatusPosition() == StatusPosition.LEFT) {
            return iconEndX + spacing;
        } else if (config.playerStatusPosition() == StatusPosition.RIGHT) {
            Font baseFont = getFontForType(config.playerTextFont(), (int) (18 * scale));
            Graphics2D tempGraphics = (Graphics2D) graphics.create();
            tempGraphics.setFont(baseFont);
            FontMetrics metrics = tempGraphics.getFontMetrics();
            int textWidth = metrics.stringWidth(text);
            tempGraphics.dispose();
            return iconX - textWidth - spacing;
        }
        return textX;
    }


    private Font getFontForType(FontType fontType, int fontSize) {
        switch (fontType) {
            case RUNESCAPE:
                return FontManager.getRunescapeFont().deriveFont((float) fontSize);
            case RUNESCAPE_SMALL:
                return FontManager.getRunescapeSmallFont().deriveFont((float) fontSize);
            case RUNESCAPE_BOLD:
                return FontManager.getRunescapeBoldFont().deriveFont((float) fontSize);
            case DEFAULT:
            default:
                return new Font("SansSerif", Font.PLAIN, fontSize);
        }
    }

    private void drawBar(Graphics2D graphics, float scale, BarType barType, int current, int max, String text, BufferedImage icon) {
        int fullWidth = (int) (barType.getWidth() * scale);
        int height = (int) (barType.getHeight() * scale);
        int currentBarWidth = (int) (fullWidth * (current / (double) max));
        int barStartX = (int) (barType.getX() * scale);
        int barY = (int) (barType.getY() * scale);
        int barCenterY = barY + height / 2;
        int margin = (int) (10 * scale);
        int iconWidth = (int) (15 * scale);
        int iconHeight = (int) (14 * scale);

        graphics.setColor(Color.BLACK);
        graphics.fillRect(barStartX, barY, fullWidth, height);
        graphics.setColor(barType.getBarColor());
        graphics.fillRect(barStartX, barY, currentBarWidth, height);

        int iconX = calculateIconPosition(config.playerIconPosition(), barStartX, fullWidth, iconWidth, margin);
        int textX = calculateTextPosition(graphics, config.playerStatusPosition(), barStartX, fullWidth, text, margin, scale);
        
        if (iconX != -1 && textX != -1 && config.playerStatusPosition() == config.playerIconPosition()) {
            textX = adjustTextForIconOverlap(graphics, textX, iconX, iconWidth, text, scale);
        }

        if (icon != null && iconX != -1) {
            graphics.drawImage(icon, iconX, barCenterY - iconHeight / 2, iconWidth, iconHeight, null);
        }

        if (textX != -1) {
            drawBarText(graphics, text, textX, barCenterY, barStartX, fullWidth, margin, scale, barType);
        }
    }

    private void drawBarText(Graphics2D graphics, String text, int textX, int barCenterY, int barStartX, int fullWidth, int margin, float scale, BarType barType) {
        float textScaleMultiplier = config.playerTextScale().getMultiplier();
        int scaledFontSize = (int) (barType.getFontSize() * scale * textScaleMultiplier);
        Font scaledFont = getFontForType(config.playerTextFont(), scaledFontSize);
        graphics.setFont(scaledFont);

        FontMetrics scaledMetrics = graphics.getFontMetrics();
        int scaledTextWidth = scaledMetrics.stringWidth(text);

        if (config.playerStatusPosition() == StatusPosition.CENTER) {
            textX = barStartX + (fullWidth - scaledTextWidth) / 2;
        } else if (config.playerStatusPosition() == StatusPosition.RIGHT) {
            textX = barStartX + fullWidth - scaledTextWidth - margin;
        }

        int textY = barCenterY + (int) (barType.getTextYOffset() * scale);
        if (config.playerTextScale() == TextScale.LARGE) {
            textY += (int) (2 * scale);
        }
        if (config.playerTextFont() != FontType.DEFAULT) {
            textY += (int) (1 * scale);
        }

        graphics.setColor(new Color(238, 209, 149));
        graphics.drawString(text, textX + 2, textY + 2);
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

            headWidget.setOriginalX((int) (loc.x + 28 * scale));
            headWidget.setOriginalY((int) (loc.y + 32 * scale));
            headWidget.setOriginalWidth((int) (32 * scale));
            headWidget.setOriginalHeight((int) (32 * scale));
            headWidget.setModelZoom((int) (1200 / scale)); //1200 was what he had before
            headWidget.setRotationZ(0); // 1882 was what we had before

            headWidget.revalidate();
        });
    }
}
