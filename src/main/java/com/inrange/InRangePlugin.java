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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@PluginDescriptor(
	name = "In Range"
)
public class InRangePlugin extends Plugin {

	private static final int FIGHT_CAVES_REGION = 9551;
	private static final int INFERNO_REGION = 9043;
	private static final int JAD_CHALLENGE_VAR = 11878; // 0 = out, 1 = in
	private static final int COLOSSEUM_REGION = 7216;

	@Inject
	private Client client;
	@Inject
	private InRangeConfig config;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private InRangeOverlay inRangeOverlay;

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
	protected void startUp() throws Exception
	{
		overlayManager.add(inRangeOverlay);
		updateEnablePlugin();

		//Get a JSON string from a file and turn into a list of Weapons
		Gson gson = new Gson();
		TypeToken<List<Weapon>> listType = new TypeToken<>() {};
		String json = new String(Files.readAllBytes(Paths.get(".\\weapons.json")));
		List<Weapon> weaponRangeList = gson.fromJson(json, listType.getType());

		//Convert a list of weapons into a map for easy lookup by item id
		weaponRangeMap = weaponRangeList.stream().collect(Collectors.toMap(Weapon::getId, Weapon::getRange));
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
			return;
		}
		getAttackRange(weapon.getId());

	}

	private void getAttackRange(int weaponID)
	{
		Integer weaponRange = weaponRangeMap.get(weaponID);
		if(weaponRange == null)
		{
			return;
		}
		if (isLongRange()) attackRange = Math.min(weaponRange + 2, 10);
		else attackRange = weaponRange;

	}

	private boolean isLongRange()
	{
		int weaponStyleEnum = client.getEnum(EnumID.WEAPON_STYLES).getIntValue(currentWeaponType);
		int[] weaponStyleStructs = client.getEnum(weaponStyleEnum).getIntVals();
		StructComposition attackStyleStruct = client.getStructComposition(weaponStyleStructs[currentAttackStyle]);
		String attackStyleName = attackStyleStruct.getStringValue(ParamID.ATTACK_STYLE_NAME);
		return attackStyleName.equals("Longrange") || attackStyleName.equals("Defensive");
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

	public void updateEnablePlugin()
	{
		if(isAreaSpecificChecked())
		{
			enablePlugin = isInFightCaves() || isInInferno() || isInColosseum() || isInJadChallenge();
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

	public boolean isAreaSpecificChecked()
	{
		return config.infernoEnabled() || config.colosseumEnabled() || config.fightCavesEnabled() || config.jadChallengeEnabled();
	}
}

class Weapon
{
	private final int id;
	private final int range;

    Weapon(int id, int range) {
        this.id = id;
		this.range = range;
    }

    public int getId() {
        return id;
	}
	public int getRange() {
        return range;
	}
}
