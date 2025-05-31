/*
 * Copyright (c) 2025, thebeanbag <https://github.com/thebeanbag/In-Range>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.inrange;

import net.runelite.client.config.*;
import java.awt.*;

@ConfigGroup(InRangeConfig.GROUP)
public interface InRangeConfig extends Config
{
	String GROUP = "In Range";

	@ConfigSection(
			name = "Highlight Settings",
			description =  "Check which modes you'd like to highlight enemies.",
			position = 0
	)
	String highlightSection = "highlightSection";

	@ConfigItem(
			keyName = "inRangeEnabled",
			name = "In Range",
			description = "Highlight enemy when they are in range.",
			position = 1,
			section = highlightSection
	)
	default boolean inRangeEnabled()
	{
		return true;
	}

	@ConfigItem(
			keyName = "losEnabled",
			name = "Line of Sight",
			description = "Highlight enemy when they are in range but not in line of sight.",
			position = 2,
			section = highlightSection
	)
	default boolean losEnabled()
	{
		return true;
	}

	@ConfigItem(
			keyName = "outOfRangeEnabled",
			name = "Out of Range",
			description = "Highlight enemy when they are out of range.",
			position = 3,
			section = highlightSection
	)
	default boolean outOfRangeEnabled()
	{
		return true;
	}

	//------------------------------------------------------------//
	// Color Settings
	//------------------------------------------------------------//

	@ConfigSection(
			name = "Colors",
			description = "Set color for various states.",
			position = 1
	)
	String colorSection = "colorSection";

	@Alpha
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
	@Alpha
	@ConfigItem(
			keyName = "lineOfSightColor",
			name = "Line of Sight",
			description = "Color to use for when an NPC is in range but not in line of sight",
			position =  2,
			section = colorSection
	)
	default Color lineOfSightColor()
	{
		return Color.ORANGE;
	}
	@Alpha
	@ConfigItem(
			keyName = "outOfRangeColor",
			name = "Out Of Range",
			description = "Color to use for when an NPC is out of range",
			position =  3,
			section = colorSection
	)
	default Color outOfRangeColor()
	{
		return Color.RED;
	}

	//------------------------------------------------------------//
	// Area Settings
	//------------------------------------------------------------//

	@ConfigSection(
			name = "Areas",
			description = "Check which areas you'd like the plugin to be enabled (uncheck all to enable everywhere).",
			position = 2
	)
	String areaSection = "areaSection";

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

	@ConfigItem(
			keyName = "zebakEnabled",
			name = "Zebak Room in Tombs of Amascut",
			description = "Enable plugin during Zebak fight",
			position = 5,
			section = areaSection
	)
	default boolean zebakEnabled()
	{
		return true;
	}
}
