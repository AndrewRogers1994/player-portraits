package com.mmo;

import com.mmo.config.StatusPosition;
import com.mmo.config.TextScale;
import com.mmo.config.FontType;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("MMOHud")
public interface MmoHudConfig extends Config
{
    @ConfigItem(
            keyName = "playerFrameScale",
            name = "Player frame scale (%)",
            description = "The overriding scale for the player portrait",
            position = 0
    )
    default int playerFrameScale()
    {
        return 100;
    }

    @ConfigItem(
            keyName = "enemyFrameScale",
            name = "Enemy frame scale (%)",
            description = "The overriding scale for the enemy portrait",
            position = 1
    )
    default int enemyFrameScale()
    {
        return 100;
    }

    @ConfigSection(
            name = "Player head",
            description = "Settings that control the player head",
            position = 2,
            closedByDefault = true
    )
    String playerHeadSection = "playerHeadSections";

    @ConfigItem(
            keyName = "playerRotation",
            name = "Rotation",
            description = "The player head rotation to use for portrait",
            section = playerHeadSection,
            position = 0
    )
    default int playerRotation()
    {
        return 0;
    }

    @ConfigItem(
            keyName = "playerStatusPosition",
            name = "Status Position",
            description = "Sets the position of the player's portrait status text.",
            section = playerHeadSection,
            position = 1
    )
    default StatusPosition playerStatusPosition()
    {
        return StatusPosition.LEFT;
    }

    @ConfigItem(
            keyName = "playerIconPosition",
            name = "Status Icon Position",
            description = "Sets the position of the second portrait status icon.",
            section = playerHeadSection,
            position = 2
    )
    default StatusPosition playerIconPosition()
    {
        return StatusPosition.LEFT;
    }

    @ConfigItem(
            keyName = "playerTextScale",
            name = "Text Scale",
            description = "Sets the scale of the status text.",
            section = playerHeadSection,
            position = 3
    )
    default TextScale playerTextScale()
    {
        return TextScale.MEDIUM;
    }

    @ConfigItem(
            keyName = "playerTextFont",
            name = "Text Font",
            description = "Sets the font type for the status text.",
            section = playerHeadSection,
            position = 4
    )
    default FontType playerTextFont()
    {
        return FontType.RUNESCAPE;
    }


}
