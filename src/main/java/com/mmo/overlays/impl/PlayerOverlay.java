package com.mmo.overlays.impl;

import com.mmo.MmoHud;
import com.mmo.MmoHudConfig;
import com.mmo.BarType;
import com.mmo.config.BadgeDisplay;
import com.mmo.overlays.BarRenderer;
import com.mmo.overlays.HeadOverlay;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.AlternateSprites;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.combatlevel.CombatLevelConfig;
import net.runelite.client.plugins.itemstats.Effect;
import net.runelite.client.plugins.itemstats.ItemStatChangesService;
import net.runelite.client.plugins.itemstats.StatChange;
import net.runelite.client.plugins.statusbars.StatusBarsConfig;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.util.ImageUtil;
import net.runelite.api.gameval.SpriteID;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

@Slf4j
public class PlayerOverlay extends HeadOverlay {

    private static final Color PRAYER_COLOR = new Color(50, 200, 200, 175);
    private static final Color ACTIVE_PRAYER_COLOR = new Color(57, 255, 186, 225);
    public static final Color HEALTH_COLOR = new Color(225, 35, 0, 125);
    private static final Color POISONED_COLOR = new Color(0, 145, 0, 150);
    private static final Color VENOMED_COLOR = new Color(0, 65, 0, 150);
    public static final Color HEAL_COLOR = new Color(255, 112, 6, 150);
    private static final Color PRAYER_HEAL_COLOR = new Color(57, 255, 186, 75);
    private static final Color ENERGY_HEAL_COLOR = new Color (199,  118, 0, 218);
    private static final Color RUN_STAMINA_COLOR = new Color(160, 124, 72, 255);
    private static final Color SPECIAL_ATTACK_COLOR = new Color(3, 153, 0, 195);
    private static final Color ENERGY_COLOR = new Color(199, 174, 0, 220);
    private static final Color DISEASE_COLOR = new Color(255, 193, 75, 181);
    private static final Color PARASITE_COLOR = new Color(196, 62, 109, 181);
    private static final int MAX_SPECIAL_ATTACK_VALUE = 100;
    private static final int MAX_RUN_ENERGY_VALUE = 100;

    @Inject
    private ClientThread clientThread;

    @Inject
    private Client client;

    @Inject
    private MmoHud plugin;

    @Inject
    private ItemStatChangesService itemStatService;

    @Inject
    private SpriteManager spriteManager;

    @Inject
    private SkillIconManager skillIconManager;

    private int dialogId = 5000;

    private final Image heartDisease;
    private final Image heartPoison;
    private final Image heartVenom;

    private final Map<StatusBarsConfig.BarMode, BarRenderer> barRenderers = new EnumMap<>(StatusBarsConfig.BarMode.class);

    @Inject
    private PlayerOverlay()
    {

        setPosition(OverlayPosition.DYNAMIC);
        setMovable(true);
        setDragTargetable(true);
        setPriority(OverlayPriority.HIGHEST);

        heartDisease = ImageUtil.loadImageResource(AlternateSprites.class, AlternateSprites.DISEASE_HEART);
        heartPoison = ImageUtil.loadImageResource(AlternateSprites.class, AlternateSprites.POISON_HEART);
        heartVenom = ImageUtil.loadImageResource(AlternateSprites.class, AlternateSprites.VENOM_HEART);

        initRenderers();

        setLayer(OverlayLayer.MANUAL);
        drawAfterLayer(WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE);
        drawAfterLayer(WidgetInfo.RESIZABLE_VIEWPORT_OLD_SCHOOL_BOX);
        drawAfterLayer(WidgetInfo.FIXED_VIEWPORT);

    }

    private void initRenderers()
    {
        barRenderers.put(StatusBarsConfig.BarMode.HITPOINTS, new BarRenderer(
                () -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.HITPOINTS),
                () -> client.getBoostedSkillLevel(Skill.HITPOINTS),
                () -> getRestoreValue(Skill.HITPOINTS.getName()),
                () ->
                {
                    final int poisonState = client.getVarpValue(VarPlayerID.POISON);

                    if (poisonState >= 1000000)
                    {
                        return VENOMED_COLOR;
                    }

                    if (poisonState > 0)
                    {
                        return POISONED_COLOR;
                    }

                    if (client.getVarpValue(VarPlayerID.DISEASE) > 0)
                    {
                        return DISEASE_COLOR;
                    }

                    if (client.getVarbitValue(VarbitID.PARASITE) >= 1)
                    {
                        return PARASITE_COLOR;
                    }

                    return HEALTH_COLOR;
                },
                () -> HEAL_COLOR,
                () ->
                {
                    final int poisonState = client.getVarpValue(VarPlayerID.POISON);

                    if (poisonState > 0 && poisonState < 50)
                    {
                        return heartPoison;
                    }

                    if (poisonState >= 1000000)
                    {
                        return heartVenom;
                    }

                    if (client.getVarpValue(VarPlayerID.DISEASE) > 0)
                    {
                        return heartDisease;
                    }

                    return loadSprite(SpriteID.OrbIcon.HITPOINTS);
                }
        ));
        barRenderers.put(StatusBarsConfig.BarMode.PRAYER, new BarRenderer(
                () -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.PRAYER),
                () -> client.getBoostedSkillLevel(Skill.PRAYER),
                () -> getRestoreValue(Skill.PRAYER.getName()),
                () ->
                {
                    Color prayerColor = PRAYER_COLOR;

                    for (Prayer pray : Prayer.values())
                    {
                        if (client.isPrayerActive(pray))
                        {
                            prayerColor = ACTIVE_PRAYER_COLOR;
                            break;
                        }
                    }

                    return prayerColor;
                },
                () -> PRAYER_HEAL_COLOR,
                () -> skillIconManager.getSkillImage(Skill.PRAYER, true)
        ));
        barRenderers.put(StatusBarsConfig.BarMode.RUN_ENERGY, new BarRenderer(
                () -> MAX_RUN_ENERGY_VALUE,
                () -> client.getEnergy() / 100,
                () -> getRestoreValue("Run Energy"),
                () ->
                {
                    if (client.getVarbitValue(VarbitID.STAMINA_ACTIVE) != 0)
                    {
                        return RUN_STAMINA_COLOR;
                    }
                    else
                    {
                        return ENERGY_COLOR;
                    }
                },
                () -> ENERGY_HEAL_COLOR,
                () -> loadSprite(SpriteID.OrbIcon.WALK)
        ));
        barRenderers.put(StatusBarsConfig.BarMode.SPECIAL_ATTACK, new BarRenderer(
                () -> MAX_SPECIAL_ATTACK_VALUE,
                () -> client.getVarpValue(VarPlayerID.SA_ENERGY) / 10,
                () -> 0,
                () -> SPECIAL_ATTACK_COLOR,
                () -> null,
                () -> loadSprite(SpriteID.OrbIcon.SPECIAL)
        ));
        barRenderers.put(StatusBarsConfig.BarMode.WARMTH, new BarRenderer(
                () -> 100,
                () -> client.getVarbitValue(VarbitID.WINT_WARMTH) / 10,
                () -> 0,
                () -> new Color(244, 97, 0),
                () -> null,
                () -> skillIconManager.getSkillImage(Skill.FIREMAKING, true)
        ));
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
        headWidget.setOriginalX((int) (loc.x + 35 * scale));
        headWidget.setOriginalY((int) (loc.y + 35 * scale));
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
        if (headWidget != null) {
            headWidget.setModelId(dialogId);
        }
        drawHeadWidget();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (isHidden || !parentSet) {
            return new Dimension(0, 0);
        }

        float scale = (float) config.playerFrameScale() / 100f;

        renderConfiguredBar(config.bar1(), BarType.BAR1, 6, 12, graphics, scale);
        renderConfiguredBar(config.bar2(), BarType.BAR2, 38, 5, graphics, scale);
        renderConfiguredBar(config.bar3(), BarType.BAR3, 70, 18, graphics, scale);

        graphics.drawImage(plugin.headContainer, 0, 0, (int) (plugin.headContainer.getWidth() * scale), (int) (plugin.headContainer.getHeight() * scale), null);

        drawHealArc(barRenderers.get(config.archType()), graphics, scale);

        if (config.badgeDisplay() != BadgeDisplay.DISABLED && plugin.pipImage != null) {
            drawBadge(graphics, scale);
        }

        return new Dimension((int) (220 * scale), (int) (119 * scale));
    }

    private void renderConfiguredBar(StatusBarsConfig.BarMode barMode, BarType type, int y, int offset, Graphics2D graphics, float scale) {
        BarRenderer bar = barRenderers.get(barMode);
        if (bar != null) {
            bar.renderBar(config, graphics, type, 68, y, scale, plugin.barBackground, plugin.barContainer, offset);
        }
    }

    private void drawHeadWidget() {
        clientThread.invoke(() ->
        {
            if(!parentSet) {
                return;
            }

            var loc = getPreferredLocation();

            float scale = ((float) config.playerFrameScale() / 100);

            if (headWidget == null) {
                return;
            }

            headWidget.setOriginalX((int) (loc.x + 35 * scale));
            headWidget.setOriginalY((int) (loc.y + 35 * scale));
            headWidget.setOriginalWidth((int) (32 * scale));
            headWidget.setOriginalHeight((int) (32 * scale));
            headWidget.setModelZoom((int) (1300 / scale));
            headWidget.setRotationZ(0);

            headWidget.revalidate();
        });
    }


    private int getRestoreValue(String skill)
    {
        final MenuEntry[] menu = client.getMenuEntries();
        final int menuSize = menu.length;
        if (menuSize == 0)
        {
            return 0;
        }

        final MenuEntry entry = menu[menuSize - 1];
        final Widget widget = entry.getWidget();
        int restoreValue = 0;

        if (widget != null && widget.getId() == InterfaceID.Inventory.ITEMS)
        {
            final Effect change = itemStatService.getItemStatChanges(widget.getItemId());

            if (change != null)
            {
                for (final StatChange c : change.calculate(client).getStatChanges())
                {
                    final int value = c.getTheoretical();

                    if (value != 0 && c.getStat().getName().equals(skill))
                    {
                        restoreValue = value;
                    }
                }
            }
        }

        return restoreValue;
    }


    private BufferedImage loadSprite(int spriteId)
    {
        return spriteManager.getSprite(spriteId, 0);
    }

    private boolean inLms()
    {
        return client.getWidget(InterfaceID.BrOverlay.CONTENT) != null;
    }

    private void drawBadge(Graphics2D graphics, float scale) {
        int pipWidth = (int) (plugin.pipImage.getWidth() * scale);
        int pipHeight = (int) (plugin.pipImage.getHeight() * scale);
        int pipX = (int) (5 * scale) + 24;
        int pipY = (int) ((plugin.headContainer.getHeight() - pipHeight - 5) * scale) + 10;

        graphics.drawImage(plugin.pipImage, pipX, pipY, pipWidth, pipHeight, null);

        String displayText = getBadgeText();
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

    private String getBadgeText() {
        BadgeDisplay display = config.badgeDisplay();
        
        switch (display) {
            case COMBAT_LEVEL:
                return plugin.combatLevelConfig.showPreciseCombatLevel() ? plugin.combatLevelStr : getPlayerCombatLevel();
                
            case ARC_STATUS_AMOUNT:
                BarRenderer arcBar = barRenderers.get(config.archType());
                if (arcBar == null) return null;
                try {
                    int currentValue = arcBar.currentValue;
                    return String.valueOf(currentValue);
                } catch (Exception e) {
                    return null;
                }
            default:
                return null;
        }
    }

    private void drawHealArc(BarRenderer barRenderer, Graphics2D graphics, float scale) {
        if (barRenderer == null) return;


        barRenderer.renderArc(graphics, 50, 52, 43, scale);
    }

    private String getPlayerCombatLevel() {
        if (client.getLocalPlayer() == null) {
            return "0";
        }
        return String.valueOf(client.getLocalPlayer().getCombatLevel());
    }

}
