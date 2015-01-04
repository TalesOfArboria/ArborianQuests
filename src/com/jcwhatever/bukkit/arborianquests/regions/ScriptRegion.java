/* This file is part of ArborianQuests for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package com.jcwhatever.bukkit.arborianquests.regions;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.jcwhatever.bukkit.arborianquests.ArborianQuests;
import com.jcwhatever.bukkit.arborianquests.quests.Quest;
import com.jcwhatever.nucleus.regions.Region;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A region used by scripts for region related events.
 */
public class ScriptRegion extends Region {

    private List<IScriptRegionResult> _onEnter = new ArrayList<>(10);
    private List<IScriptRegionResult> _onLeave = new ArrayList<>(10);

    private ListMultimap<Quest, IScriptRegionResult> _onQuestEnter =
            MultimapBuilder.hashKeys(30).linkedListValues().build();

    private ListMultimap<Quest, IScriptRegionResult> _onQuestLeave =
            MultimapBuilder.hashKeys(30).linkedListValues().build();

    /**
     * Constructor.
     *
     * @param name      The name of the region.
     * @param settings  The data node to load and save settings.
     */
    public ScriptRegion(String name, IDataNode settings) {
        super(ArborianQuests.getPlugin(), name, settings);
    }

    /**
     * Remove all handlers
     */
    public void clearHandlers() {
        _onEnter.clear();
        _onLeave.clear();
        _onQuestEnter.clear();
        _onQuestLeave.clear();
        setEventListener(false);
    }

    /**
     * Add a handler to be run whenever a player enters the region.
     *
     * @param handler  The handler.
     */
    public boolean addOnEnter(IScriptRegionResult handler) {
        PreCon.notNull(handler);

        _onEnter.add(handler);
        setEventListener(true);
        return true;
    }

    /**
     * Add a handler to be run whenever a player enters the region and
     * is on the specified quest.
     *
     * @param questName  The name of the quest.
     * @param handler    The handler.
     */
    public boolean addOnQuestEnter(String questName, IScriptRegionResult handler) {
        PreCon.notNullOrEmpty(questName);
        PreCon.notNull(handler);

        questName = questName.toLowerCase();

        Quest quest = ArborianQuests.getQuestManager().getPrimary(questName);
        if (quest == null)
            return false;

        _onQuestEnter.put(quest, handler);
        setEventListener(true);
        return true;
    }

    /**
     * Add a handler to be run whenever a player leaves the region.
     *
     * @param handler  The handler.
     */
    public boolean addOnLeave(IScriptRegionResult handler) {
        PreCon.notNull(handler);

        _onLeave.add(handler);
        setEventListener(true);
        return true;
    }

    /**
     * Add a handler to be run whenever a player leaves the region.
     *
     * @param handler  The handler.
     */
    public boolean addOnQuestLeave(String questName, IScriptRegionResult handler) {
        PreCon.notNullOrEmpty(questName);
        PreCon.notNull(handler);

        questName = questName.toLowerCase();

        Quest quest = ArborianQuests.getQuestManager().getPrimary(questName);
        if (quest == null)
            return false;

        _onQuestLeave.put(quest, handler);
        setEventListener(true);
        return true;
    }


    @Override
    protected void onPlayerEnter(Player p, EnterRegionReason reason) {

        // run global handlers
        for (IScriptRegionResult subscriber : _onEnter) {
            subscriber.call(p, this);
        }

        // run quest handlers
        Set<Quest> quests = Quest.getPlayerQuests(p);
        if (quests != null && !quests.isEmpty()) {

            for (Quest quest : quests) {
                LinkedList<IScriptRegionResult> handler = (LinkedList<IScriptRegionResult>)_onQuestEnter.get(quest);
                if (!handler.isEmpty()) {

                    handler.getLast().call(p, this);
                    break;
                }
            }
        }
    }

    @Override
    protected void onPlayerLeave(Player p, LeaveRegionReason reason) {

        // run global handlers
        for (IScriptRegionResult subscriber : _onLeave) {
            subscriber.call(p, this);
        }

        // run quest handlers
        Set<Quest> quests = Quest.getPlayerQuests(p);
        if (quests != null && !quests.isEmpty()) {

            for (Quest quest : quests) {
                LinkedList<IScriptRegionResult> handlers = (LinkedList<IScriptRegionResult>)_onQuestLeave.get(quest);
                if (!handlers.isEmpty()) {
                    handlers.getLast().call(p, this);
                    break;
                }
            }
        }
    }

    @Override
    protected boolean canDoPlayerEnter(Player p, EnterRegionReason reason) {
        return !_onEnter.isEmpty() || !_onQuestEnter.isEmpty();
    }

    @Override
    protected boolean canDoPlayerLeave(Player p, LeaveRegionReason reason) {
        return !_onLeave.isEmpty() || !_onQuestLeave.isEmpty();
    }
}
