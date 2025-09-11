package com.mmo.overlays;

import com.mmo.BarDirection;
import com.mmo.MmoHudConfig;
import com.mmo.BarType;
import com.mmo.config.FontType;
import com.mmo.config.StatusPosition;
import com.mmo.config.TextScale;
import lombok.RequiredArgsConstructor;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class BarRenderer {

    private static final long ANIMATION_DURATION = 800;
    private static final int ANIMATION_THRESHOLD = 4;
    
    private final Supplier<Integer> maxValueSupplier;
    private final Supplier<Integer> currentValueSupplier;
    private final Supplier<Integer> healSupplier;
    private final Supplier<Color> colorSupplier;
    private final Supplier<Color> healColorSupplier;
    private final Supplier<Image> iconSupplier;
    
    private int maxValue;
    public int currentValue;
    private int previousValue = -1;
    private int animatedValue = -1;
    private long lastChangeTime = 0;
    
    private int healValue;
    private int previousHealValue = -1;
    private int animatedHealValue = -1;
    private long lastHealChangeTime = 0;



    private void refreshSkills() {
        maxValue = maxValueSupplier.get();
        int newCurrentValue = currentValueSupplier.get();
        currentValue = getAnimatedValue(newCurrentValue, previousValue);
        previousValue = newCurrentValue;
        
        int newHealValue = healSupplier.get();
        healValue = getAnimatedHealValue(newHealValue, previousHealValue);
        previousHealValue = newHealValue;
    }

    private int getAnimatedValue(int current, int previous) {
        long currentTime = System.currentTimeMillis();

        if (current != previous) {
            int change = Math.abs(current - previous);
            if (change >= ANIMATION_THRESHOLD) {
                lastChangeTime = currentTime;
                animatedValue = previous;
                return previous;
            } else {
                animatedValue = current;
                return current;
            }
        }

        if (lastChangeTime > 0) {
            long elapsed = currentTime - lastChangeTime;
            double progress = Math.min(1.0, (double) elapsed / ANIMATION_DURATION);

            int newAnimated = (int) (animatedValue + (current - animatedValue) * progress);
            animatedValue = newAnimated;

            if (progress >= 1.0) {
                lastChangeTime = 0;
            }
            
            return newAnimated;
        }

        return current;
    }

    private int getAnimatedHealValue(int current, int previous) {
        long currentTime = System.currentTimeMillis();

        if (current != previous) {
            int change = Math.abs(current - previous);
            if (change >= ANIMATION_THRESHOLD) {
                lastHealChangeTime = currentTime;
                animatedHealValue = previous;
                return previous;
            } else {
                animatedHealValue = current;
                return current;
            }
        }

        if (lastHealChangeTime > 0) {
            long elapsed = currentTime - lastHealChangeTime;
            double progress = Math.min(1.0, (double) elapsed / ANIMATION_DURATION);

            int newAnimated = (int) (animatedHealValue + (current - animatedHealValue) * progress);
            animatedHealValue = newAnimated;

            if (progress >= 1.0) {
                lastHealChangeTime = 0;
            }
            
            return newAnimated;
        }

        return current;
    }

    public void renderBar(MmoHudConfig config, Graphics2D graphics, BarType barType, int barRenderX, int barRenderY, float scale, BufferedImage barBackground, BufferedImage barContainer, int marginOffset) {

        refreshSkills();

        int fullWidth = (int) (barType.getWidth() * scale);
        int height = (int) (20 * scale);
        int currentBarWidth = (int) (fullWidth * (currentValue / (double) maxValue));
        int barStartX = (int) (barType.getX() * scale);
        int barY = (int) ((barRenderY + 5) * scale);
        int barCenterY = barY + height / 2;
        int margin = (int) (marginOffset * scale);
        int iconWidth = (int) (iconSupplier.get().getWidth(null) * scale);
        int iconHeight = (int) (iconSupplier.get().getHeight(null) * scale);


        int scaledBarWidth = (int) (151 * scale);
        int scaledBarHeight = (int) (30 * scale);

        graphics.drawImage(barBackground, barRenderX, barRenderY, scaledBarWidth, scaledBarHeight, null);

        if (healValue > 0) {
            int totalAfterHeal = Math.min(currentValue + healValue, maxValue);
            int healBarWidth = (int) (fullWidth * (totalAfterHeal / (double) maxValue));
            int healBarX = barStartX + currentBarWidth;
            int healBarWidthAdjusted = Math.max(0, healBarWidth - currentBarWidth);
            
            if (healBarWidthAdjusted > 0) {
                graphics.setColor(healColorSupplier.get());
                graphics.fillRect(healBarX, barY, healBarWidthAdjusted, height);
            }
        }

        graphics.setColor(colorSupplier.get());
        graphics.fillRect(barStartX, barY, currentBarWidth, height);
        graphics.drawImage(barContainer, barRenderX, barRenderY, scaledBarWidth, scaledBarHeight, null);

        final String counterText = Integer.toString(currentValue);

        int iconX = calculateIconPosition(config.playerIconPosition(), barStartX, fullWidth, iconWidth, margin);
        int textX = calculateTextPosition(config,graphics, config.playerStatusPosition(), barStartX, fullWidth, counterText, margin, scale);

        if (iconX != -1 && textX != -1 && config.playerStatusPosition() == config.playerIconPosition()) {
            textX = adjustTextForIconOverlap(config,graphics, textX, iconX, iconWidth, counterText, scale);
        }

        if (iconSupplier.get() != null && iconX != -1) {
            graphics.drawImage(iconSupplier.get(), iconX, barCenterY - iconHeight / 2, iconWidth, iconHeight, null);
        }

        if (textX != -1) {
            drawBarText(config,graphics, counterText, textX, barCenterY, barStartX, fullWidth, margin, scale, barType);
        }

    }

    public void renderArc(Graphics2D graphics, int centerX, int centerY, int radius, float scale) {
        refreshSkills();

        if (maxValue <= 0) return;
        
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (healValue > 0) {
            int totalAfterHeal = Math.min(currentValue + healValue, maxValue);
            double healPercentage = Math.min(totalAfterHeal / (double) maxValue, 1.0);
            int healArcAngle = (int) (360 * healPercentage);
            
            graphics.setColor(healColorSupplier.get());
            graphics.setStroke(new BasicStroke(3 * scale));
            graphics.drawArc(centerX - radius, centerY - radius - 1, radius * 2, (radius + 2) * 2, -90, healArcAngle);
        }

        // Draw health arc on top
        double healthPercentage = Math.min(currentValue / (double) maxValue, 1.0);
        int arcAngle = (int) (360 * healthPercentage);
        
        graphics.setColor(colorSupplier.get());
        graphics.setStroke(new BasicStroke(3 * scale));
        graphics.drawArc(centerX - radius, centerY - radius - 1, radius * 2, (radius + 2) * 2, -90, arcAngle);
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

    private int calculateTextPosition(MmoHudConfig config,Graphics2D graphics, StatusPosition position, int barStartX, int fullWidth, String text, int margin, float scale) {
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

    private int adjustTextForIconOverlap(MmoHudConfig config,Graphics2D graphics, int textX, int iconX, int iconWidth, String text, float scale) {
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

    private void drawBarText(MmoHudConfig config,Graphics2D graphics, String text, int textX, int barCenterY, int barStartX, int fullWidth, int margin, float scale, BarType barType) {
        float textScaleMultiplier = config.playerTextScale().getMultiplier();
        int scaledFontSize = (int) (18 * scale * textScaleMultiplier);
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

        graphics.setColor(new Color(0, 0, 0, 200));
        graphics.drawString(text, textX + 1, textY + 1);
        graphics.drawString(text, textX + 1, textY - 1);
        graphics.drawString(text, textX - 1, textY + 1);
        graphics.drawString(text, textX - 1, textY - 1);
        graphics.drawString(text, textX + 2, textY);
        graphics.drawString(text, textX - 2, textY);
        graphics.drawString(text, textX, textY + 2);
        graphics.drawString(text, textX, textY - 2);
        
        // Draw main text
        graphics.setColor(new Color(238, 209, 149));
        graphics.drawString(text, textX, textY);
    }



}
