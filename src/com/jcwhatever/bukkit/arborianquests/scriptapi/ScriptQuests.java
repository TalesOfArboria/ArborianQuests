package com.jcwhatever.bukkit.arborianquests.scriptapi;

import com.jcwhatever.bukkit.arborianquests.ArborianQuests;
import com.jcwhatever.bukkit.arborianquests.Msg;
import com.jcwhatever.bukkit.arborianquests.quests.Quest;
import com.jcwhatever.bukkit.arborianquests.quests.QuestManager;
import com.jcwhatever.bukkit.arborianquests.quests.QuestStatus.CurrentQuestStatus;
import com.jcwhatever.bukkit.arborianquests.quests.QuestStatus.QuestCompletionStatus;
import com.jcwhatever.bukkit.generic.collections.LifespanEndAction;
import com.jcwhatever.bukkit.generic.collections.TimedSet;
import com.jcwhatever.bukkit.generic.commands.response.CommandRequests;
import com.jcwhatever.bukkit.generic.commands.response.ResponseHandler;
import com.jcwhatever.bukkit.generic.commands.response.ResponseRequest;
import com.jcwhatever.bukkit.generic.commands.response.ResponseType;
import com.jcwhatever.bukkit.generic.scripting.IEvaluatedScript;
import com.jcwhatever.bukkit.generic.scripting.IScriptApiInfo;
import com.jcwhatever.bukkit.generic.scripting.api.GenericsScriptApi;
import com.jcwhatever.bukkit.generic.scripting.api.IScriptApiObject;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Provide scripts with API for quests.
 */
@IScriptApiInfo(
        variableName = "quests",
        description = "Provide scripts API access for quests.")
public class ScriptQuests extends GenericsScriptApi {

    TimedSet<ResponseRequest> _requests = new TimedSet<ResponseRequest>(20, 600);

    /**
     * Constructor.
     *
     * @param plugin  required plugin constructor for script api repository.
     */
    public ScriptQuests(Plugin plugin) {

        // quests is always the owning plugin.
        super(ArborianQuests.getInstance());

        _requests.addOnLifetimeEnd(new LifespanEndAction<ResponseRequest>() {
            @Override
            public void onEnd(ResponseRequest item) {
                CommandRequests.cancel(item);
            }
        });

    }

    @Override
    public IScriptApiObject getApiObject(IEvaluatedScript script) {
        return this;
    }

    @Override
    public void reset() {
        for (ResponseRequest request : _requests)
            CommandRequests.cancel(request);
    }

    /**
     * Create a quest via script. Quests are not stored and must be
     * created by the script each time it is loaded.
     *
     * @param name         The name of the quest.
     * @param displayName  The displayName of the quest.
     * @return
     */
    public Quest create(String name, String displayName) {
        return QuestManager.create(name, displayName);
    }

    /**
     * Remove a quest.
     *
     * @param quest  The quest to discard.
     */
    public boolean dispose(Quest quest) {
        PreCon.notNull(quest);

        return QuestManager.dispose(quest.getName());
    }

    /**
     * Determine if a player has accepted a quest.
     *
     * @param questName  The name of the quest.
     * @return
     */
    public boolean isInQuest(Player p, String questName) {
        PreCon.notNull(p);
        PreCon.notNullOrEmpty(questName);

        Quest quest = QuestManager.get(questName);
        return quest != null && quest.getStatus(p).getCurrentStatus() == CurrentQuestStatus.IN_PROGRESS;
    }

    /**
     * Determine if a player has already completed a quest.
     *
     * @param p
     * @param questName
     * @return
     */
    public boolean hasCompleted(Player p, String questName) {
        PreCon.notNull(p);
        PreCon.notNullOrEmpty(questName);

        Quest quest = QuestManager.get(questName);
        return quest != null && quest.getStatus(p).getCompletionStatus() == QuestCompletionStatus.COMPLETED;
    }

    /**
     * Ask the player to accept a quest.
     *
     * @param questName  The name of the quest.
     * @param p          The player to ask.
     * @param onAccept   Runnable to run if the player accepts.
     */
    public void query(final Player p, String questName, final Runnable onAccept) {

        final Quest quest = QuestManager.get(questName);
        if (quest == null)
            return;

        ResponseHandler handler = new ResponseHandler() {

            @Override
            public void onResponse(ResponseType response) {

                if (response == ResponseType.ACCEPT) {

                    quest.acceptQuest(p);

                    if (onAccept != null)
                        onAccept.run();
                }
            }
        };

        ResponseRequest request = CommandRequests.request(ArborianQuests.getInstance(),
                questName, p, handler, ResponseType.ACCEPT);

        _requests.add(request);

        Msg.tell(p, "{WHITE}Type '{YELLOW}/accept{WHITE}' to accept the quest.");
        Msg.tell(p, "Expires in 30 seconds.");

    }

}