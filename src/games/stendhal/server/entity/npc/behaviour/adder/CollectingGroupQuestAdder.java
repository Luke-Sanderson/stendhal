/***************************************************************************
 *                      (C) Copyright 2020 - Stendhal                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc.behaviour.adder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.dbcommand.UpdateGroupQuestCommand;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.CollectingGroupQuestBehaviour;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import marauroa.server.db.command.DBCommandQueue;

public class CollectingGroupQuestAdder {

	private static final class IsRemainingItem implements ChatCondition {
		private final CollectingGroupQuestBehaviour behaviour;

		private IsRemainingItem(CollectingGroupQuestBehaviour behaviour) {
			this.behaviour = behaviour;
		}

		@Override
		public boolean fire(Player player, Sentence sentence, Entity npc) {
			Map<String, Integer> remaining = behaviour.calculateRemainingItems();
			String item = Grammar.singular(sentence.getNormalized());
			return remaining.get(item) != null;
		}
	}

	public void add(SpeakerNPC npc, CollectingGroupQuestBehaviour behaviour) {
		addGreeting(npc, behaviour);
		addProgress(npc, behaviour);
		addQuest(npc, behaviour);
		addCollectingItems(npc, behaviour);
	}
	
	private void addGreeting(SpeakerNPC npc, CollectingGroupQuestBehaviour behaviour) {
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new QuestCompletedCondition(behaviour.getQuestSlot()),
				ConversationStates.ATTENDING,
				"Thanks again for your help. We are making #progress. Hopefully we will finish in time.",
				null);
		
		npc.addReply(
				Arrays.asList("inexperienced", "inexperience", "experience"), 
				"I am very sorry, I don't have time to teach you right now. You should explore the world and gain some experience.");
	}
	
	private void addProgress(SpeakerNPC npc, CollectingGroupQuestBehaviour behaviour) {
		npc.addReply(Arrays.asList("status", "progress"),
				null,
				new ChatAction() {
			
			@Override
			public void fire(Player player, Sentence sentence, EventRaiser npc) {
				int percent = behaviour.getProgressPercent();
				if (percent < 10) {
					npc.say("There is still so much to do before the " + behaviour.getProjectName()  + " can start. We have hardly started.");
				} else if (percent < 50) {
					npc.say("There is still so much to do before the " + behaviour.getProjectName() + " can start. We have not even reached the half way point");
				} else if (percent < 75) {
					npc.say("There is still so much to do before the " + behaviour.getProjectName() + " can start. We have barly reached the half way point.");
				} else if (percent < 90) {
					npc.say("We are almost there. But still, there is more work to be done before the " + behaviour.getProjectName() + " can start.");
				}
			}
		});
	}

	private void addQuest(SpeakerNPC npc, CollectingGroupQuestBehaviour behaviour) {
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new QuestNotCompletedCondition(behaviour.getQuestSlot()),
						new LevelLessThanCondition(5)
				),
				ConversationStates.ATTENDING,
				"I am sorry, I am very busy at the moment, trying to finish this #project in time. I would ask you for help, but you seem to be very #inexperienced.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new QuestNotCompletedCondition(behaviour.getQuestSlot()),
						new LevelGreaterThanCondition(4)
				),
				ConversationStates.QUEST_OFFERED,
				null,
				new ChatAction() {
					
					@Override
					public void fire(Player player, Sentence sentence, EventRaiser npc) {
						npc.say("Could you provide any of the following items to help with construction? ");
						Map<String, Integer> remaining = behaviour.calculateRemainingItems();
						Set<String> entries = new HashSet<>();
						for (Map.Entry<String, Integer> entry : remaining.entrySet()) {
							int chunkSize = behaviour.getChunkSize(entry.getKey()).intValue();
							String entr = Grammar.numberString(chunkSize) + " #'" + Grammar.plnoun(chunkSize, entry.getKey()) + "'";
							if (chunkSize < entry.getValue().intValue()) {
								entr = entr + " out of " + Grammar.numberString(entry.getValue().intValue()) + " still needed";
							}
							entries.add(entr);
						}
						npc.say(Grammar.enumerateCollection(entries) + ".");
					}
				});
	}


	private void addCollectingItems(SpeakerNPC npc, CollectingGroupQuestBehaviour behaviour) {
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(behaviour.getQuestSlot()),
				ConversationStates.ATTENDING,
				"Thank you for your help! At the moment I won't bother you again.",
				null);

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Oh, thank you. Which items could you provide?",
				null);

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Oh, too bad.",
				null);

		npc.add(ConversationStates.QUEST_OFFERED,
				"",
				new NotCondition(new IsRemainingItem(behaviour)),
				ConversationStates.QUEST_OFFERED,
				"Thank you, for offering your help. But we don't need that item. Is there anything else from the list of needed items, that you would like to fetch?",
				null);

		npc.add(ConversationStates.QUEST_OFFERED,
				"",
				new IsRemainingItem(behaviour),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					
					@Override
					public void fire(Player player, Sentence sentence, EventRaiser npc) {
						String item = Grammar.singular(sentence.getNormalized());
						String hint = behaviour.getHint(item);
						if (!player.isEquipped(item, 1)) {
							npc.say("I am sorry, you don't seem to have any " + Grammar.plural(item) + ".");
							if (hint != null) {
								npc.say(hint);
							}
							return;
						}
						Integer stackSize = behaviour.getChunkSize(item);
						if (!player.isEquipped(item, stackSize)) {
							npc.say("I am sorry, you don't seem to have " + Grammar.numberString(stackSize) + " " + Grammar.plural(item) + ".");
							if (hint != null) {
								npc.say(hint);
							}
							return;
						}
						player.drop(item, stackSize);

						player.setQuest(behaviour.getQuestSlot(), 0, "done");
						UpdateGroupQuestCommand command = new UpdateGroupQuestCommand(behaviour.getQuestSlot(), item, player.getName(), stackSize);
						DBCommandQueue.get().enqueue(command);
						// TODO: reward players
						npc.say("Thank you for your help!");
					}
				});
	}
}
