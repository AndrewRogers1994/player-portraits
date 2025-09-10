package com.mmo;

import com.mmo.config.StatusPosition;
import com.mmo.config.TextScale;
import com.mmo.config.FontType;
import com.mmo.config.BadgeDisplay;
import net.runelite.client.config.*;
import net.runelite.client.plugins.statusbars.StatusBarsConfig;

@ConfigGroup("MMOHud")
public interface MmoHudConfig extends Config
{

    @ConfigItem(
            keyName = "playerFrameScale",
            name = "Player Portrait Scale",
            description = "Adjust the size of your player portrait and status bars (50-200%)",
            position = 0
    )
    default int playerFrameScale()
    {
        return 100;
    }

    @ConfigItem(
            keyName = "enemyFrameScale",
            name = "Enemy Portrait Scale",
            description = "Adjust the size of enemy portraits and health bars (50-200%)",
            position = 1
    )
    default int enemyFrameScale()
    {
        return 100;
    }

    @ConfigSection(
            name = "Player Portrait",
            description = "Customize your player portrait appearance and status bars",
            position = 2,
            closedByDefault = false
    )
    String playerHeadSection = "playerHeadSections";

    @ConfigItem(
            keyName = "playerStatusPosition",
            name = "Text Position",
            description = "Where to display the status text (HP, Prayer, etc.) on the bars",
            section = playerHeadSection,
            position = 1
    )
    default StatusPosition playerStatusPosition()
    {
        return StatusPosition.LEFT;
    }

    @ConfigItem(
            keyName = "playerIconPosition",
            name = "Icon Position",
            description = "Where to display the skill icons (heart, prayer, etc.) on the bars",
            section = playerHeadSection,
            position = 2
    )
    default StatusPosition playerIconPosition()
    {
        return StatusPosition.LEFT;
    }

    @ConfigItem(
            keyName = "playerTextScale",
            name = "Text Size",
            description = "Adjust the size of the status text on the bars",
            section = playerHeadSection,
            position = 3
    )
    default TextScale playerTextScale()
    {
        return TextScale.MEDIUM;
    }

    @ConfigItem(
            keyName = "playerTextFont",
            name = "Text Style",
            description = "Choose the font style for the status text",
            section = playerHeadSection,
            position = 4
    )
    default FontType playerTextFont()
    {
        return FontType.RUNESCAPE_BOLD;
    }

    @ConfigItem(
            keyName = "firstBarMode",
            name = "Top Bar",
            description = "What to display in the top status bar (HP, Prayer, Run Energy, etc.)",
            section = playerHeadSection,
            position = 5
    )
    default StatusBarsConfig.BarMode bar1()
    {
        return StatusBarsConfig.BarMode.HITPOINTS;
    }

    @ConfigItem(
            keyName = "secondBarMode",
            name = "Middle Bar",
            description = "What to display in the middle status bar (HP, Prayer, Run Energy, etc.)",
            section = playerHeadSection,
            position = 6
    )
    default StatusBarsConfig.BarMode bar2()
    {
        return StatusBarsConfig.BarMode.PRAYER;
    }

    @ConfigItem(
            keyName = "thirdBarMode",
            name = "Bottom Bar",
            description = "What to display in the bottom status bar (HP, Prayer, Run Energy, etc.)",
            section = playerHeadSection,
            position = 7
    )
    default StatusBarsConfig.BarMode bar3()
    {
        return StatusBarsConfig.BarMode.SPECIAL_ATTACK;
    }

    @ConfigItem(
            keyName = "arcMode",
            name = "Status Arc",
            description = "Which stat to display in the circular arc around your portrait",
            section = playerHeadSection,
            position = 8
    )
    default StatusBarsConfig.BarMode archType()
    {
        return StatusBarsConfig.BarMode.RUN_ENERGY;
    }

    @ConfigItem(
            keyName = "badgeDisplay",
            name = "Badge Display",
            description = "What to show in the small badge on your portrait",
            section = playerHeadSection,
            position = 9
    )
    default BadgeDisplay badgeDisplay()
    {
        return BadgeDisplay.COMBAT_LEVEL;
    }




}
