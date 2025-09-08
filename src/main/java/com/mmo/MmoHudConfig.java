package com.mmo;

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

    // Define a collapsible section
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
        position = 3
	)
	default int playerRotation()
	{
		return 0;
	}

    @ConfigItem(
            keyName = "playerHeadXOffset",
            name = "X Offset",
            description = "The player head rotation to use for portrait",
            section = playerHeadSection,
            position = 4
    )
    default int playerHeadXOffset()
    {
        return 28;
    }

    @ConfigItem(
            keyName = "playerHeadYOffset",
            name = "Y Offset",
            description = "The player head rotation to use for portrait",
            section = playerHeadSection,
            position = 5
    )
    default int playerHeadYOffset()
    {
        return 32;
    }

    @ConfigSection(
            name = "Player health bar",
            description = "Settings that control the player health bar",
            position = 6,
            closedByDefault = true
    )
    String playerHealthBarSection = "playerHealthBarSection";

    @ConfigItem(
            keyName = "playerHealthBarHeight",
            name = "Height",
            description = "The player health bar height",
            section = playerHealthBarSection,
            position = 7

    )
    default int playerHealthBarHeight()
    {
        return 29;
    }

    @ConfigItem(
            keyName = "playerHealthBarWidth",
            name = "Width",
            description = "The player health bar width",
            section = playerHealthBarSection,
            position = 8
    )
    default int playerHealthBarWidth()
    {
        return 125;
    }

    @ConfigItem(
            keyName = "playerHealthBarXOffset",
            name = "X Offset",
            description = "The player health bar x offset",
            section = playerHealthBarSection,
            position = 9
    )
    default int playerHealthBarXOffset()
    {
        return 80;
    }

    @ConfigItem(
            keyName = "playerHealthBarYOffset",
            name = "Y Offset",
            description = "The player health bar y offset",
            section = playerHealthBarSection,
            position = 10
    )
    default int playerHealthBarYOffset()
    {
        return 21;
    }


    @ConfigItem(
            keyName = "playerHealthTextSize",
            name = "Text size",
            description = "The player health text size",
            section = playerHealthBarSection,
            position = 11
    )
    default int playerHealthTextSize()
    {
        return 18;
    }

    @ConfigItem(
            keyName = "playerHealthTextXOffset",
            name = "Text x offset",
            description = "The player health text x offset",
            section = playerHealthBarSection,
            position = 12
    )
    default int playerHealthTextXOffset()
    {
        return 115;
    }

    @ConfigItem(
            keyName = "playerHealthTextYOffset",
            name = "Text Y offset",
            description = "The player health text y offset",
            section = playerHealthBarSection,
            position = 13
    )
    default int playerHealthTextYOffset()
    {
        return 40;
    }

    @ConfigSection(
            name = "Player prayer bar",
            description = "Settings that control the player prayer bar",
            position = 14,
            closedByDefault = true
    )
    String playerPrayerBarSection = "playerPrayerBarSection";

    @ConfigItem(
            keyName = "playerPrayerBarHeight",
            name = "Height",
            description = "The player prayer bar height",
            section = playerPrayerBarSection,
            position = 15
    )
    default int playerPrayerBarHeight()
    {
        return 18;
    }

    @ConfigItem(
            keyName = "playerPrayerBarWidth",
            name = "Width",
            description = "The player prayer bar width",
            section = playerPrayerBarSection,
            position = 16
    )
    default int playerPrayerBarWidth()
    {
        return 90;
    }

    @ConfigItem(
            keyName = "playerPrayerBarXOffset",
            name = "X Offset",
            description = "The player prayer bar x offset",
            section = playerPrayerBarSection,
            position = 17
    )
    default int playerPrayerBarXOffset()
    {
        return 74;
    }

    @ConfigItem(
            keyName = "playerPrayerBarYOffset",
            name = "Y offset",
            description = "The player prayer bar y offset",
            section = playerPrayerBarSection,
            position = 18
    )
    default int playerPrayerBarYOffset()
    {
        return 59;
    }

    @ConfigItem(
            keyName = "playerPrayerTextSize",
            name = "Text size",
            description = "The player prayer text size",
            section = playerPrayerBarSection,
            position = 19
    )
    default int playerPrayerTextSize()
    {
        return 14;
    }

    @ConfigItem(
            keyName = "playerPrayerTextXOffset",
            name = "Text x offset",
            description = "The player prayer text x offset",
            section = playerPrayerBarSection,
            position = 20
    )
    default int playerPrayerTextXOffset()
    {
        return 100;
    }

    @ConfigItem(
            keyName = "playerPrayerTextYOffset",
            name = "Text Y offset",
            description = "The player prayer text y offset",
            section = playerPrayerBarSection,
            position = 21
    )
    default int playerPrayerTextYOffset()
    {
        return 72;
    }

    @ConfigSection(
            name = "Enemy head",
            description = "Settings that control the enemy head bar",
            position = 22,
            closedByDefault = true
    )
    String enemyHeadSection = "enemyHeadSection";

    @ConfigItem(
            keyName = "enemyRotation",
            name = "Rotation",
            description = "The enemy head rotation to use for portrait",
            section = enemyHeadSection,
            position = 23
    )
    default int enemyRotation()
    {
        return 0;
    }

    @ConfigItem(
            keyName = "enemyHeadXOffset",
            name = "X Offset",
            description = "The enemy head x offset",
            section = enemyHeadSection,
            position = 24
    )
    default int enemyHeadXOffset()
    {
        return 164;
    }

    @ConfigItem(
            keyName = "enemyHeadYOffset",
            name = "Y Offset",
            description = "The enemy head y offset",
            section = enemyHeadSection,
            position = 25
    )
    default int enemyHeadYOffset()
    {
        return 32;
    }


    @ConfigSection(
            name = "Enemy health bar",
            description = "Settings that control the enemy health bar",
            position = 26,
            closedByDefault = true
    )
    String enemyHealthBarSection = "enemyHealthBarSection";

    @ConfigItem(
            keyName = "enemyHealthBarHeight",
            name = "Height",
            description = "The enemy health bar height",
            section = enemyHealthBarSection,
            position = 27

    )
    default int enemyHealthBarHeight()
    {
        return 29;
    }

    @ConfigItem(
            keyName = "enemyHealthBarWidth",
            name = "Width",
            description = "The enemy health bar width",
            section = enemyHealthBarSection,
            position = 28
    )
    default int enemyHealthBarWidth()
    {
        return 128;
    }

    @ConfigItem(
            keyName = "enemyHealthBarXOffset",
            name = "X Offset",
            description = "The enemy health bar x offset",
            section = enemyHealthBarSection,
            position = 29
    )
    default int enemyHealthBarXOffset()
    {
        return 7;
    }

    @ConfigItem(
            keyName = "enemyHealthBarYOffset",
            name = "Y Offset",
            description = "The enemy health bar y offset",
            section = enemyHealthBarSection,
            position = 30
    )
    default int enemyHealthBarYOffset()
    {
        return 36;
    }


    @ConfigItem(
            keyName = "enemyHealthTextSize",
            name = "Text size",
            description = "The enemy health text size",
            section = enemyHealthBarSection,
            position = 31
    )
    default int enemyHealthTextSize()
    {
        return 18;
    }

    @ConfigItem(
            keyName = "enemyHealthTextXOffset",
            name = "Text x offset",
            description = "The enemy health text x offset",
            section = enemyHealthBarSection,
            position = 32
    )
    default int enemyHealthTextXOffset()
    {
        return 40;
    }

    @ConfigItem(
            keyName = "enemyHealthTextYOffset",
            name = "Text Y offset",
            description = "The enemy health text y offset",
            section = enemyHealthBarSection,
            position = 33
    )
    default int enemyHealthTextYOffset()
    {
        return 57;
    }

    @ConfigItem(
            keyName = "enemyNoPortraitHead",
            name = "enemyNoPortraitHead",
            description = "The enemy health text y offset",
            section = enemyHealthBarSection,
            position = 33
    )
    default int enemyNoPortraitHead()
    {
        return 0;
    }
}
