/*
 * Copyright (c) 2021, Adam <Adam@sigterm.info>
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

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;


@Slf4j
public class InRangeOverlay extends Overlay
{
    private final Client client;
    private final InRangePlugin plugin;
    private final InRangeConfig config;
    private final ModelOutlineRenderer modelOutlineRenderer;

    @Inject
    private InRangeOverlay(Client client, InRangePlugin plugin, InRangeConfig config, ModelOutlineRenderer modelOutlineRenderer)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.modelOutlineRenderer = modelOutlineRenderer;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(PRIORITY_HIGH);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if( !plugin.enablePlugin)
        {
            return null;
        }
        renderMouseover();
        return null;
    }

    private void renderMouseover()
    {
        MenuEntry[] menuEntries = client.getMenu().getMenuEntries();
        if (menuEntries.length == 0)
        {
            return;
        }

        MenuEntry entry = client.isMenuOpen() ? hoveredMenuEntry(menuEntries) : menuEntries[menuEntries.length - 1];
        MenuAction menuAction = entry.getType();

        switch (menuAction)
        {
            case WIDGET_TARGET_ON_NPC:
            case NPC_FIRST_OPTION:
            case NPC_SECOND_OPTION:
            case NPC_THIRD_OPTION:
            case NPC_FOURTH_OPTION:
            case NPC_FIFTH_OPTION:
            case EXAMINE_NPC:
            {
                NPC npc = entry.getNpc();
                if (npc != null && npc.getCombatLevel() > 0)
                {
                    Player player = client.getLocalPlayer();
                    WorldArea npcWorldArea = npc.getWorldArea();
                    WorldArea playerWorldArea = player.getWorldArea();
                    WorldView worldView = client.getTopLevelWorldView();
                    int distance = playerWorldArea.distanceTo(npcWorldArea);
                    //In range but no line of sight
                    if (distance < plugin.attackRange && !playerWorldArea.hasLineOfSightTo(worldView, npcWorldArea))
                    {
                        modelOutlineRenderer.drawOutline(npc, 4, config.lineOfSightColor(), 4);

                    }
                    //In range
                    else if(distance < plugin.attackRange)
                    {
                        modelOutlineRenderer.drawOutline(npc, 4, config.inRangeColor(), 4);
                    }
                    //Out of range
                    else
                    {
                        modelOutlineRenderer.drawOutline(npc, 4, config.outOfRangeColor(), 4);
                    }
                }
                break;
            }
        }
    }

    private MenuEntry hoveredMenuEntry(final MenuEntry[] menuEntries)
    {
        Menu menu = client.getMenu();
        final int menuX = menu.getMenuX();
        final int menuY = menu.getMenuY();
        final int menuWidth = menu.getMenuWidth();
        final Point mousePosition = client.getMouseCanvasPosition();

        int dy = mousePosition.getY() - menuY;
        dy -= 19; // Height of Choose Option
        if (dy < 0)
        {
            return menuEntries[menuEntries.length - 1];
        }

        int idx = dy / 15; // Height of each menu option
        idx = menuEntries.length - 1 - idx;

        if (mousePosition.getX() > menuX && mousePosition.getX() < menuX + menuWidth
                && idx >= 0 && idx < menuEntries.length)
        {
            return menuEntries[idx];
        }
        return menuEntries[menuEntries.length - 1];
    }
}
