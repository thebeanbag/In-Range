package com.example;

import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.ChatMessageType;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.events.*;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.npcoverlay.HighlightedNpc;
import net.runelite.client.game.npcoverlay.NpcOverlayService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import com.google.gson.Gson;
import net.runelite.client.ui.ClientUI;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@PluginDescriptor(
	name = "In Range"
)
public class InRangePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private InRangeConfig config;

	private Map<Integer, Integer> weaponRangeMap;

	private Integer	attackRange = 1;

	private Integer currentAttackStyle = -1;

	private Integer currentWeaponType = -1;

	private boolean isAttackableNPCHovered = false;

	private NPC currentNPC = null;

	private boolean eventFlag = false;

	private Integer tickLastSeenNPC = 0;

	private boolean attackRangeSet = false;

	@Inject
	private ClientUI clientUI;

	@Inject
	private NpcOverlayService npcOverlayService;
	private final Map<NPC, HighlightedNpc> highlightedNpcs = new HashMap<>();
	private final Function<NPC, HighlightedNpc> isHighlighted = highlightedNpcs::get;

	@Override
	protected void startUp() throws Exception
	{
		log.info("In Range started!");
		//Get JSON string from file and turn into list of Weapons
		Gson gson = new Gson();
		TypeToken<List<Weapon>> listType = new TypeToken<List<Weapon>>(){};
		String json = new String(Files.readAllBytes(Paths.get("C:\\Users\\Ethan\\IdeaProjects\\In-Range\\weapons.json")));
		List<Weapon> weaponRangeList = gson.fromJson(json, listType.getType());
		//Convert list of weapons into map for easy lookup by item id
		weaponRangeMap = weaponRangeList.stream().collect(Collectors.toMap(Weapon::getId, Weapon::getRange));
		log.info("Loaded weapon stats");

		npcOverlayService.registerHighlighter(isHighlighted);
	}

	@Override
	protected void shutDown() throws Exception
	{
		npcOverlayService.unregisterHighlighter(isHighlighted);
		highlightedNpcs.clear();
		log.info("In Range stopped!");
	}


	private void getAttackRange()
	{
		ItemContainer equippedItem = client.getItemContainer(InventoryID.EQUIPMENT);
		Item weapon = equippedItem != null ? equippedItem.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx()) : null;
		int weaponID = weapon != null ? weapon.getId() : -1;
		int weaponRange = weaponRangeMap.get(weaponID);

		if(isLongRange()) attackRange = Math.min(weaponRange + 2, 10);
		else attackRange = weaponRange;

	}

	private boolean isLongRange()
	{
		switch (currentWeaponType)
		{
			case 3: //shortbow
			case 5: //longbow
				return currentAttackStyle == 3; // Longrange is style 3 for bows
			case 6: //crossbow
				return currentAttackStyle == 2; // Longrange is style 2 for crossbows
			case 4: //thrown
				return currentAttackStyle == 3; // Longrange is style 3 for thrown weapons
			default:
				return false;
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int weaponType = client.getVarbitValue(Varbits.EQUIPPED_WEAPON_TYPE);
		int newStyle = client.getVarpValue(VarPlayerID.COM_MODE);
		//If the attack style has changed ie from Rapid to Long range, we need to recalculate the attack range
		if(newStyle != currentAttackStyle || weaponType != currentWeaponType){
			currentAttackStyle = newStyle;
			currentWeaponType = weaponType;
			getAttackRange();
		}
	}

	/*@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		//If container change is equipment, get new attack range
		if(event.getContainerId() == InventoryID.EQUIPMENT.getId())
		{
			getAttackRange();
		}
	}*/

	@Subscribe
	public void onGameTick(GameTick event) {

		if(!attackRangeSet)
		{
			getAttackRange();
			attackRangeSet = true;
		}


		if (isAttackableNPCHovered) {
			Player player = client.getLocalPlayer();
			WorldArea npcWorldArea = currentNPC.getWorldArea();
			WorldArea playerWorldArea = player.getWorldArea();
			WorldView worldView = client.getTopLevelWorldView();
			int distance = playerWorldArea.distanceTo(npcWorldArea);

			if (distance < attackRange && playerWorldArea.hasLineOfSightTo(worldView, npcWorldArea)) {
				log.info("Adding highlight " + client.getTickCount());
				highlightedNpcs.put(currentNPC, HighlightedNpc.builder().npc(currentNPC).highlightColor(Color.GREEN).hull(true).outline(true).build());
				npcOverlayService.rebuild();
			} else {
				log.info("Adding highlight " + client.getTickCount());
				highlightedNpcs.put(currentNPC, HighlightedNpc.builder().npc(currentNPC).highlightColor(Color.RED).hull(true).outline(true).build());
				npcOverlayService.rebuild();
			}
		} else {
			log.info("Removing highlight " + client.getTickCount());
			highlightedNpcs.remove(currentNPC);
			npcOverlayService.rebuild();
			currentNPC = null;
		}

		//In case user alt-tabs with NPC hovered
		if (!clientUI.isFocused()) {
			if (isAttackableNPCHovered) {
				highlightedNpcs.remove(currentNPC);
				npcOverlayService.rebuild();
				currentNPC = null;
			}
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		//Check if we are hovering over an NPC
		if (event.getType() == MenuAction.EXAMINE_NPC.getId() ||
				event.getType() == MenuAction.NPC_FIRST_OPTION.getId() ||
				event.getType() == MenuAction.NPC_SECOND_OPTION.getId() ||
				event.getType() == MenuAction.NPC_THIRD_OPTION.getId() ||
				event.getType() == MenuAction.NPC_FOURTH_OPTION.getId() ||
				event.getType() == MenuAction.NPC_FIFTH_OPTION.getId())
		{
			NPC npc = event.getMenuEntry().getNpc();
			//Check if we actually got an NPC object and check that the NPC is attackable
			if (npc != null && npc.getCombatLevel() > 0)
			{
				//Need to set the last tick we are hovered over NPC, this is so we don't de-highlight the NPC too soon after seeing a 'Walk Here' menu action
				tickLastSeenNPC = client.getTickCount();
				//Skip if its the same NPC
				if(npc != currentNPC)
				{
					log.info("New NPC detected " + client.getTickCount());
					highlightedNpcs.remove(currentNPC);
					currentNPC = npc;
					isAttackableNPCHovered = true;
				}
			}
			//Non-attackable NPC
			else
			{
				//if we are not hovering over an attackable NPC and isAttackableNPCHovered is set to true
				//that means we were previously hovered over an attackable NPC
				//we need to set isAttackableNPCHovered to false and set event flag to true so we can update on next game tick
				if(isAttackableNPCHovered)
				{
					log.info("Non attackable NPC " + client.getTickCount());
					isAttackableNPCHovered = false;
				}
			}
		}
		//Need a way to check that we are no longer hovered over an NPC
		else if(event.getType() == MenuAction.WALK.getId())
		{
			//if we are not hovering over an NPC and isAttackableNPCHovered is set to true
			//that means we were previously hovered over an attackable NPC
			//we need to set isAttackableNPCHovered to false

			//We get the Attack menu option on tick 1 and the Walk here menu option on tick 2
			//We only want to dehighlight NPC is we go two ticks without seeing the Attack option
			if(isAttackableNPCHovered && client.getTickCount() > tickLastSeenNPC + 1)
			{
				log.info("Not hovering over NPC " + client.getTickCount());
				isAttackableNPCHovered = false;
			}
		}
	}

	@Provides
	InRangeConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(InRangeConfig.class);
	}
}

class Weapon {
	private int range;
	private int id;

	public int getId() {
		return id;
	}

	public int getRange() {
		return range;
	}
}
