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


package com.jcwhatever.bukkit.arborianquests;

import com.jcwhatever.bukkit.arborianquests.commands.CommandHandler;
import com.jcwhatever.bukkit.arborianquests.regions.ScriptRegionManager;
import com.jcwhatever.bukkit.arborianquests.scriptapi.ScriptQuests;
import com.jcwhatever.bukkit.arborianquests.scriptapi.ScriptRegions;
import com.jcwhatever.bukkit.generic.GenericsPlugin;
import com.jcwhatever.bukkit.generic.scripting.GenericsScriptManager;
import com.jcwhatever.bukkit.generic.scripting.IEvaluatedScript;
import com.jcwhatever.bukkit.generic.scripting.IScript;
import com.jcwhatever.bukkit.generic.scripting.ScriptApiRepo;
import com.jcwhatever.bukkit.generic.scripting.ScriptHelper;
import com.jcwhatever.bukkit.generic.scripting.api.IScriptApi;
import com.jcwhatever.bukkit.generic.scripting.api.ScriptApiFlags;
import com.jcwhatever.bukkit.generic.scripting.api.ScriptApiMeta;
import com.jcwhatever.bukkit.generic.storage.DataStorage;
import com.jcwhatever.bukkit.generic.storage.DataStorage.DataPath;
import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.utils.FileUtils.DirectoryTraversal;
import com.jcwhatever.bukkit.generic.utils.TextUtils.TextColor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ArborianQuests extends GenericsPlugin {

    private static ArborianQuests _instance;

    public static ArborianQuests getInstance() {
        return _instance;
    }

    private GenericsScriptManager _scriptManager;
    private ScriptRegionManager _scriptRegionManager;
    private List<IScriptApi> _scriptApi;
    private List<IEvaluatedScript> _evaluatedScripts = new ArrayList<>(50);

    public ArborianQuests() {
        super();

        _instance = this;
    }

    public GenericsScriptManager getScriptManager() {
        return _scriptManager;
    }

    public ScriptRegionManager getScriptRegionManager() {
        return _scriptRegionManager;
    }

    public List<IEvaluatedScript> getEvaluatedScripts() {
        return _evaluatedScripts;
    }

    @Override
    public String getChatPrefix() {
        return TextColor.WHITE + "[" + TextColor.BLUE + "Quests" + TextColor.WHITE + "] ";
    }

    @Override
    public String getConsolePrefix() {
        return "[ArborianQuests] ";
    }

    @Override
    protected void onEnablePlugin() {

        IDataNode _flagNode = DataStorage.getStorage(ArborianQuests.getInstance(), new DataPath("flags"));
        _flagNode.load();

        IDataNode _metaNode = DataStorage.getStorage(ArborianQuests.getInstance(), new DataPath("meta"));
        _metaNode.load();

        _scriptManager = new GenericsScriptManager(this, ScriptHelper.getGlobalEngineManager());

        _scriptApi = ScriptHelper.getDefaultApi(this, _scriptManager);
        _scriptApi.add(new ScriptApiFlags(this, _flagNode));
        _scriptApi.add(new ScriptApiMeta(this, _metaNode));
        _scriptApi.add(new ScriptQuests(this));
        _scriptApi.add(new ScriptRegions(this));

        ScriptApiRepo.registerApiType(this, ScriptQuests.class);
        ScriptApiRepo.registerApiType(this, ScriptRegions.class);

        IDataNode regionNode = DataStorage.getStorage(this, new DataPath("regions"));
        regionNode.load();

        _scriptRegionManager = new ScriptRegionManager(regionNode);

        reloadScripts();

        registerCommands(new CommandHandler());
    }

    @Override
    protected void onDisablePlugin() {
        resetApi();

        ScriptApiRepo.unregisterApiType(this, ScriptQuests.class);
        ScriptApiRepo.unregisterApiType(this, ScriptRegions.class);
    }

    public void reloadScripts() {

        resetApi();
        _evaluatedScripts.clear();

        File scriptDir = new File(this.getDataFolder(), "scripts");
        if (!scriptDir.exists() && !scriptDir.mkdirs())
            return;

        File questScripts = new File(scriptDir, "quests");
        if (!questScripts.exists() && !questScripts.mkdirs())
            return;

        File libsDir = new File(scriptDir, "libs");
        if (!libsDir.exists() && !libsDir.mkdirs())
            return;

        _scriptManager.clearScripts();
        List<IScript> scripts = _scriptManager.loadScripts(questScripts, DirectoryTraversal.RECURSIVE);

        for (IScript script : scripts) {
            IEvaluatedScript evaluated = script.evaluate(_scriptApi);
            if (evaluated != null) {
                _evaluatedScripts.add(evaluated);
            }
        }
    }

    private void resetApi() {
        if (_scriptApi == null)
            return;

        for (IScriptApi api : _scriptApi) {
            api.reset();
        }
    }
}
