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


package com.jcwhatever.bukkit.arborianquests.quests;

import com.jcwhatever.bukkit.arborianquests.ArborianQuests;
import com.jcwhatever.bukkit.generic.storage.DataStorage;
import com.jcwhatever.bukkit.generic.storage.DataStorage.DataPath;
import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.generic.utils.TextUtils;

import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public class QuestManager {

    private final Plugin _plugin;
    private final IDataNode _dataNode;
    private final Map<String, Quest> _quests = new HashMap<>(20);
    private final Map<String, Quest> _created = new HashMap<>(20);

    public QuestManager(Plugin plugin, IDataNode dataNode) {
        PreCon.notNull(plugin);
        PreCon.notNull(dataNode);

        _plugin = plugin;
        _dataNode = dataNode;

        loadSettings();
    }

    public Plugin getPlugin() {
        return _plugin;
    }

    public IDataNode getDataNode() {
        return _dataNode;
    }

    public Quest create(String questName, String displayName) {
        PreCon.notNullOrEmpty(questName);
        PreCon.notNull(displayName);

        questName = questName.toLowerCase();

        Quest quest = _quests.get(questName);
        if (quest != null)
            return quest;

        IDataNode dataNode = DataStorage.getStorage(ArborianQuests.getPlugin(), new DataPath("quests." + questName));
        dataNode.load();

        dataNode.set("display", displayName);
        dataNode.saveAsync(null);

        quest = new PrimaryQuest(questName, displayName, dataNode);

        _quests.put(questName, quest);
        _created.put(questName, quest);

        return quest;
    }

    public boolean dispose(Quest quest) {
        PreCon.notNull(quest);

        if (quest instanceof PrimaryQuest) {

            DataStorage.removeStorage(ArborianQuests.getPlugin(), new DataPath("quests." + quest.getName()));
            _created.remove(quest.getName());
            return _quests.remove(quest.getName()) != null;
        }
        else if (quest instanceof SubQuest) {

            SubQuest subQuest = (SubQuest)quest;

            Quest parent = subQuest.getParent();
            return parent.removeQuest(subQuest.getName());
        }
        else {
            throw new AssertionError();
        }
    }

    @Nullable
    public Quest get(String questName) {
        PreCon.notNullOrEmpty(questName);

        if (questName.indexOf('.') != -1) {
            return getSubQuest(questName);
        }

        return _quests.get(questName.toLowerCase());
    }

    /**
     * Get all current quests. These are the quests that
     * are created by the scripts during the server session.
     */
    public List<Quest> getQuests() {
        return new ArrayList<>(_quests.values());
    }

    /**
     * Get all quests saved, even ones not re-generated by scripts.
     */
    public List<Quest> getCreatedQuests() {
        return new ArrayList<>(_created.values());
    }

    private void loadSettings() {

        // load quests
        List<String> questNames = _dataNode.getStringList("quests", null);
        if (questNames != null) {

            for (String questName : questNames) {

                IDataNode node = DataStorage.getStorage(_plugin, new DataPath("quests." + questName));
                node.load();

                String displayName = node.getString("display", questName);
                if (displayName == null)
                    throw new AssertionError();

                PrimaryQuest quest = new PrimaryQuest(questName, displayName, node);

                _created.put(questName, quest);
            }
        }
    }

    @Nullable
    private Quest getSubQuest(String questName) {

        String[] names = TextUtils.PATTERN_DOT.split(questName);

        Quest current = get(names[0]);
        if (current == null)
            return null;

        for (int i= 1; i < names.length; i++) {
            current = current.getQuest(names[i]);
            if (current == null)
                return null;
        }

        return current;
    }


}
