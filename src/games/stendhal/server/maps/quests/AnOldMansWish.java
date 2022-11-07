/***************************************************************************
 *                    Copyright © 2003-2022 - Arianne                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.NPCEmoteAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;


/** Quest to increase number of bag slots.
 *
 *  NPCs:
 *  - Elias Breland
 *  - Niall Breland
 *  - Marianne
 */
public class AnOldMansWish extends AbstractQuest {

	public static final String QUEST_SLOT = "an_old_mans_wish";
	private static final int min_level = 100;

	private final SpeakerNPC elias = npcs.get("Elias Breland");


	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "AnOldMansWish";
	}

	@Override
	public String getRegion() {
		return Region.DENIRAN;
	}

	@Override
	public String getNPCName() {
		return elias.getName();
	}

	@Override
	public int getMinLevel() {
		return min_level;
	}

	@Override
	public List<String> getHistory(final Player player) {
		final String[] states = player.getQuest(QUEST_SLOT).split(";");
		final String quest_state = states[0];
		String find_myling = null;
		String find_apothecary = null;
		for (final String st: states) {
			if (st.startsWith("find_myling:")) {
				find_myling = st.split(":")[1];
			}
			if (st.startsWith("find_apothecary:")) {
				find_apothecary = st.split(":")[1];
			}
		}

		final List<String> res = new ArrayList<>();
		res.add(elias.getName() + " wishes to know what has become of his"
			+ " estranged grandson.");

		if (quest_state.equals("rejected")) {
			res.add("I have no time for senile old men.");
		} else {
			res.add("I have agreed to investigate.");
			if (find_myling != null) {
				res.add("Marianne mentioned that Niall wanted to"
					+ " explore the graveyard near Semos City.");
				if (find_myling.equals("done")) {
					res.add("Niall has been turned into a myling. Elias will be"
						+ " devestated. But I must tell him.");
				}
			}
			if (find_apothecary != null) {
				res.add("There may be hope yet. I must go to the apothecary"
					+ " for help changing Niall back to normal.");
				if (find_apothecary.equals("done")) {
					res.add("The apothecary asked me to gather some items.");
				}
			}
			if (quest_state.equals("done")) {
				res.add("Elias and his grandson have been"
					+ " reunited.");
			}
		}

		return res;
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
			"An Old Man's Wish",
			elias.getName() + " is grieved over the loss of his grandson.",
			false
		);
		requestStep();
		findMylingStep();
		findApothecaryStep();
		prepareCompleteStep();
	}

	private void requestStep() {
		// requests quest but does not meet minimum level requirement
		elias.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(
				new QuestNotStartedCondition(QUEST_SLOT),
				new LevelLessThanCondition(min_level)),
			ConversationStates.ATTENDING,
			"My grandson disappeared over a year ago. But I need help from a"
				+ " more experienced adventurer.",
			null);

		// requests quest
		elias.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(
				new QuestNotStartedCondition(QUEST_SLOT),
				new NotCondition(new LevelLessThanCondition(min_level))),
			ConversationStates.QUEST_OFFERED,
			"My grandson disappeared over a year ago. I fear the worst and"
				+ " have nearly given up all hope. What I would give to just"
				+ " know what happened to him! If you learn anything will"
				+ " you bring me the news?",
			null);

			// already accepted quest
			elias.add(
				ConversationStates.ANY,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Thank you for accepting my plea for help. Please tell me if"
					+ " you hear any news about what has become of my grandson."
					+ " He used to play with a little girl named #Marianne.",
				null);

			// already completed quest
			elias.add(
				ConversationStates.ANY,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Thank you for returning my grandson to me. I am overfilled"
					+ " with joy!",
				null);

			// rejects quest
			elias.add(
				ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Alas! What has become of my grandson!?",
				new MultipleActions(
					new SetQuestAction(QUEST_SLOT, "rejected;;;"),
					new DecreaseKarmaAction(15)));

			// accepts quest
			elias.add(
				ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				ConversationStates.ATTENDING,
				"Oh thank you! My grandson's name is #Niall. You could talk"
					+ " to #Marianne. They used to play together.",
				new MultipleActions(
					new SetQuestAction(QUEST_SLOT, "investigate;;;"),
					new IncreaseKarmaAction(15)));

			// ask about Niall
			elias.add(
				ConversationStates.ANY,
				Arrays.asList("Niall", "grandson"),
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Niall is my grandson. I am so distraught over his"
					+ " disappearance. Ask the girl #Marianne. The often played"
					+ " together.",
				null);

			// ask about Marianne
			elias.add(
				ConversationStates.ANY,
				"Marianne",
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Marianne lives here in Deniran. Ask her about #Niall.",
				null);
	}

	private void findMylingStep() {
		final SpeakerNPC marianne = npcs.get("Marianne");

		final ChatCondition investigating = new QuestActiveCondition(QUEST_SLOT);

		marianne.add(
			ConversationStates.ATTENDING,
			"Niall",
			investigating,
			ConversationStates.ATTENDING,
			"Oh! My friend Niall! I haven't seen him in a long time. Every"
				+ " time I go to his grandfather's house to #play, he is not"
				+ " home.",
			new NPCEmoteAction("suddenly looks very melancholy."));

		marianne.add(
			ConversationStates.ATTENDING,
			"play",
			investigating,
			ConversationStates.ATTENDING,
			"Not only was he fun to play with, but he was also very helpful."
				+ " He used to help me gather chicken eggs whenever I was too"
				+ " #afraid to do it myself.",
			new NPCEmoteAction("looks even more melancholy."));

		marianne.add(
			ConversationStates.ATTENDING,
			"afraid",
			investigating,
			ConversationStates.ATTENDING,
			"Know what he told me once? He said he wanted to go all the way"
				+ " to Semos to see the #graveyard there. Nuh uh! No way! That"
				+ " sounds more scary than chickens.",
			new MultipleActions(
				new NPCEmoteAction("shivers."),
				new SetQuestAction(QUEST_SLOT, 1, "find_myling:start")));

		marianne.add(
			ConversationStates.ATTENDING,
			Arrays.asList("graveyard", "cemetary"),
			investigating,
			ConversationStates.ATTENDING,
			"I hope he didn't go to that scary graveyard. Who knows what kind"
				+ " of monsters are there.",
			null);

		marianne.add(
			ConversationStates.ATTENDING,
			"Niall",
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"I heard that Niall came home! He sure was gone for a long time."
				+ " I am glad he is home safe.",
			new NPCEmoteAction("lets out a sigh of relief."));
	}

	private void findApothecaryStep() {
			final ChatCondition found_myling = new QuestInStateCondition(QUEST_SLOT, 1, "find_myling:done");

			// tells Elias that Niall has been turned into a myling
			elias.add(
				ConversationStates.ANY,
				Arrays.asList("Niall", "myling"),
				found_myling,
				ConversationStates.ATTENDING,
				"Oh no! My dear grandson! If only there were a way to #change"
					+ " him back.",
				null);

			elias.add(
				ConversationStates.ANY,
				"change",
				found_myling,
				ConversationStates.ATTENDING,
				"Wait! I heard there was an apothecary that lives somewhere"
					+ " near Semos. Please, go to him and plead for help.",
				new SetQuestAction(QUEST_SLOT, 2, "find_apothecary:start"));

			elias.add(
				ConversationStates.ANY,
				Arrays.asList("Niall", "myling", "apothecary"),
				new QuestInStateCondition(QUEST_SLOT, 2, "find_apothecary:start"),
				ConversationStates.ATTENDING,
				"Please! Find the apothecary. Maybe he can do something to"
					+ " help my grandson.",
				null);
	}

	private void prepareCompleteStep() {
	}
}
