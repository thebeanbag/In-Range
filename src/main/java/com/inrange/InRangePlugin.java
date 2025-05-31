/*
 * Copyright (c) 2025, thebeanbag <https://github.com/thebeanbag/In-Range>
 * Copyright (c) 2023, Buchus <http://github.com/MoreBuchus>
 * Copyright (c) 2021, Adam <Adam@sigterm.info>
 * Copyright (c) 2017, honeyhoney <https://github.com/honeyhoney>
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

import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import com.google.gson.Gson;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.ArrayUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;


@Slf4j
@PluginDescriptor(
	name = "In Range"
)
public class InRangePlugin extends Plugin
{

	private static final int FIGHT_CAVES_REGION = 9551;
	private static final int INFERNO_REGION = 9043;
	private static final int JAD_CHALLENGE_VAR = 11878;
	private static final int COLOSSEUM_REGION = 7216;
	private static final int ZEBAK_REGION = 15700;

	@Inject
	private Client client;
	@Inject
	private InRangeConfig config;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private InRangeOverlay inRangeOverlay;
	@Inject
	private Gson gson;

	private Map<Integer, Integer> weaponRangeMap;

	public Integer attackRange = 1;
	private Integer currentAttackStyle = -1;
	private Integer currentWeaponType = -1;
	private Integer currentWeaponID = -1;

	private boolean isAttackRangeSet = false;
	public boolean enablePlugin = false;

	@Provides
	InRangeConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(InRangeConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(inRangeOverlay);

		final TypeToken<Map<Integer,Integer>> typeToken = new TypeToken<>(){};
		try(InputStream getWeaponRanges = InRangePlugin.class.getResourceAsStream("/weaponRanges.json"))
		{
            assert getWeaponRanges != null;
            weaponRangeMap = gson.fromJson(new InputStreamReader(getWeaponRanges), typeToken.getType());
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(inRangeOverlay);
	}


	private void getAttackRange()
	{
		ItemContainer equippedItem = client.getItemContainer(InventoryID.WORN);
		if(equippedItem == null)
		{
			return;
		}
		Item weapon = equippedItem.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
		if(weapon == null)
		{
			attackRange = 1;
			currentWeaponID = -1;
			return;
		}
		getAttackRange(weapon.getId());

	}

	private void getAttackRange(int weaponID)
	{
		Integer weaponRange = weaponRangeMap.get(weaponID);
		if(weaponRange == null)
		{
			//If a weapon is not found in the Map, set the attack range back to 1.
			attackRange = 1;
			return;
		}

		//Blue moon spear
		if(currentWeaponType == 22)
		{
			//Probably a better way to do this
			int[] weaponStyleStructs = { 3722, 3723, 3721, 3725, 3728, 3725 };
			StructComposition attackStyleStruct = client.getStructComposition(weaponStyleStructs[currentAttackStyle]);
			String attackStyle = attackStyleStruct.getStringValue(ParamID.ATTACK_STYLE_NAME);
			if(!attackStyle.equals("Casting"))
			{
				attackRange = 1;
			}
			else
			{
				attackRange = weaponRange;
			}
			return;
		}

		int weaponStyleEnum = client.getEnum(EnumID.WEAPON_STYLES).getIntValue(currentWeaponType);
		int[] weaponStyleStructs = client.getEnum(weaponStyleEnum).getIntVals();
		StructComposition attackStyleStruct = client.getStructComposition(weaponStyleStructs[currentAttackStyle]);
		String attackStyle = attackStyleStruct.getStringValue(ParamID.ATTACK_STYLE_NAME);

		//If Staff or Bladed Staff and not Casting, set the range to 1
		if((currentWeaponType == 18 || currentWeaponType == 21) && !attackStyle.equals("Casting"))
		{
			attackRange = 1;
		}
		//If Longrange on a ranged weapon or Defensive(Longrange) on magic weapon with built-in spell, add 2 to attack range up to a max of 10
		else if (attackStyle.equals("Longrange") || attackStyle.equals("Defensive"))
		{
			attackRange = Math.min(weaponRange + 2, 10);
		}
		else
		{
			attackRange = weaponRange;
		}

	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarpId() == VarPlayerID.COM_MODE)
		{
			int weaponType = client.getVarbitValue(VarbitID.COMBAT_WEAPON_CATEGORY);
			int newStyle = client.getVarpValue(VarPlayerID.COM_MODE);

			if (newStyle != currentAttackStyle || weaponType != currentWeaponType)
			{
				currentAttackStyle = newStyle;
				currentWeaponType = weaponType;
				getAttackRange();
			}
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{

		if(event.getContainerId() != InventoryID.WORN)
		{
			return;
		}

		ItemContainer itemContainer = event.getItemContainer();
		if(itemContainer == null)
		{
			return;
		}

		Item weapon = itemContainer.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
		if(weapon == null)
		{
			//If a weapon is unequipped, set range back to 1
			currentWeaponID = -1;
			attackRange = 1;
			isAttackRangeSet = true;
			return;
		}

		int weaponID = weapon.getId();
		if(weaponID != currentWeaponID)
		{
			currentWeaponID = weaponID;
			getAttackRange(weaponID);
			isAttackRangeSet = true;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if(!isAttackRangeSet)
		{
			getAttackRange();
			isAttackRangeSet = true;
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		updateEnablePlugin();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		updateEnablePlugin();
	}

	//Definitely a better way to do this...
	public void updateEnablePlugin()
	{
		if(isAreaSpecificChecked())
		{
			enablePlugin = isInFightCaves() || isInInferno() || isInColosseum() || isInJadChallenge() || isInZebak();
		}
		else
		{
			enablePlugin = true;
		}
	}

	public boolean isInFightCaves()
	{
		return config.fightCavesEnabled() && ArrayUtils.contains(client.getTopLevelWorldView().getMapRegions(), FIGHT_CAVES_REGION);
	}

	public boolean isInInferno()
	{
		return config.infernoEnabled() && ArrayUtils.contains(client.getTopLevelWorldView().getMapRegions(), INFERNO_REGION);
	}

	public boolean isInColosseum()
	{
		return config.colosseumEnabled() && ArrayUtils.contains(client.getTopLevelWorldView().getMapRegions(), COLOSSEUM_REGION);
	}

	public boolean isInJadChallenge()
	{
		return config.jadChallengeEnabled() && ArrayUtils.contains(client.getVarps(), JAD_CHALLENGE_VAR);
	}

	public boolean isInZebak()
	{
		return config.zebakEnabled() && ArrayUtils.contains(client.getTopLevelWorldView().getMapRegions(), ZEBAK_REGION);
	}

	public boolean isAreaSpecificChecked()
	{
		return config.infernoEnabled() || config.colosseumEnabled() || config.fightCavesEnabled() || config.jadChallengeEnabled() || config.zebakEnabled();
	}
}
