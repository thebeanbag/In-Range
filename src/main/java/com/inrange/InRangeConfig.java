package com.inrange;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup("example")
public interface InRangeConfig extends Config
{
	@ConfigSection(
			name = "Colors",
			description = "Set color for various states.",
			position = 0
	)
	String colorSection = "colorSection";

	@ConfigSection(
			name = "Areas",
			description = "Check which areas you'd like the plugin to be enabled (uncheck all to enable everywhere).",
			position =  1
	)
	String areaSection = "areaSection";

	@ConfigItem(
			keyName = "inRangeColor",
			name = "In Range",
			description = "Color to use for when an NPC is in range",
			position =  1,
			section = colorSection
	)
	default Color inRangeColor()
	{
		return Color.GREEN;
	}

	@ConfigItem(
			keyName = "lineOfSightColor",
			name = "Line of Sight",
			description = "Color to use for when an NPC is in range but not in line of sight",
			position =  3,
			section = colorSection
	)
	default Color lineOfSightColor()
	{
		return Color.ORANGE;
	}

	@ConfigItem(
			keyName = "outOfRangeColor",
			name = "Out Of Range",
			description = "Color to use for when an NPC is out of range",
			position =  2,
			section = colorSection
	)
	default Color outOfRangeColor()
	{
		return Color.RED;
	}

	@ConfigItem(
			keyName = "infernoEnabled",
			name = "Inferno",
			description = "Enable plugin within the Inferno",
			position = 1,
			section = areaSection
	)
	default boolean infernoEnabled()
	{
		return true;
	}

	@ConfigItem(
			keyName = "colosseumEnabled",
			name = "Fortis Colosseum",
			description = "Enable plugin within the Colosseum",
			position = 2,
			section = areaSection
	)
	default boolean colosseumEnabled()
	{
		return true;
	}

	@ConfigItem(
			keyName = "fightCavesEnabled",
			name = "Fight Caves",
			description = "Enable plugin within the Fight Caves",
			position = 3,
			section = areaSection
	)
	default boolean fightCavesEnabled()
	{
		return true;
	}

	@ConfigItem(
			keyName = "jadChallengeEnabled",
			name = "TzHaar-Ket-Rak's Challenges",
			description = "Enable plugin within TzHaar-Ket-Rak's Challenges",
			position = 4,
			section = areaSection
	)
	default boolean jadChallengeEnabled()
	{
		return true;
	}
}
