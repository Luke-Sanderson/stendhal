/***************************************************************************
 *                     Copyright © 2020 - Arianne                          *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.rp.achievement.factory;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.core.rp.achievement.condition.BoughtNumberOfCondition;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.behaviour.journal.MerchantsRegister;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.player.Player;


/**
 * Factory for buying & selling items.
 */
public class CommerceAchievementFactory extends AbstractAchievementFactory {

	private static final MerchantsRegister MR = MerchantsRegister.get();

	public static final String[] ITEMS_HAPPY_HOUR = {"beer", "wine"};
	public static final String ID_HAPPY_HOUR = "buy.drink.alcohol";
	public static final int COUNT_HAPPY_HOUR = 100;


	@Override
	protected Category getCategory() {
		return Category.COMMERCE;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		final List<Achievement> achievements = new LinkedList<Achievement>();

		achievements.add(createAchievement(
			ID_HAPPY_HOUR, "It's Happy Hour Somewhere", "Purchase 100 bottles of beer & 100 glasses of wine",
			Achievement.EASY_BASE_SCORE, true,
			new BoughtNumberOfCondition(COUNT_HAPPY_HOUR, ITEMS_HAPPY_HOUR)));

		achievements.add(createAchievement(
			"commerce.buy.all", "Community Supporter", "Spend money around the world",
			Achievement.MEDIUM_BASE_SCORE, false,
			new HasSpentAmountAtSellers()));

		return achievements;
	}


	private class HasSpentAmountAtSellers implements ChatCondition {

		// default value for sellers not included in this list is 1000
		private final Map<String, Integer> TRADE_ALL_AMOUNTS = new HashMap<String, Integer>() {{
			put("Margaret", 500);
			put("Ilisa", 4000);
			put("Adena", 500);
			put("Coralia", 500);
			put("Dale", 500);
			put("Mayor Chalmers", 10000);
			put("Mia", 2000);
			put("Mrotho", 2500);
			put("Karl", 50);
			put("Philomena", 200);
			put("Dr. Feelgood", 2000);
			put("Haizen", 10000);
			put("Edward", 22000); // 1 scuba gear
			put("Mirielle", 20000);
			put("D J Smith", 2000);
			put("Wanda", 10000);
			put("Sarzina", 5000);
			put("Xhiphin Zohos", 8000);
			put("Orchiwald", 6000);
			put("Sam", 300);
			put("Hazel", 10000);
			put("Erodel Bmud", 15000);
			put("Kendra Mattori", 8000);
			put("Diehelm Brui", 400);
			put("Lorithien", 10000);
			put("Jynath", 16000);
			put("Ouchit", 400);
			put("Xin Blanca", 800);
			put("Xoderos", 570); // 1 of each item
			put("Barbarus", 400); // 1 pick
			put("Jenny", 500);
			put("Nishiya", 60); // 2 sheep
			put("Wrviliza", 200);

			// excluded
			put("Gulimo", 0);
			put("Hagnurk", 0);
			put("Ognir", 0);
			put("Mrs. Yeti", 0);
			put("Wrvil", 0);
			put("Zekiel", 0);
			put("Mizuno", 0);
			put("Rengard", 0);
			put("Felina", 0);
			put("Fidorea", 0);
		}};


		public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
			for (final String seller: MR.getSellersNames()) {
				if (!player.has("npc_purchases", seller)) {
					return false;
				} else {
					final int amount = Integer.parseInt(player.get("npc_purchases", seller));
					if (TRADE_ALL_AMOUNTS.containsKey(seller)) {
						if (amount < TRADE_ALL_AMOUNTS.get(seller)) {
							return false;
						}
					} else if (amount < 1000) {
						return false;
					}
				}
			}

			return true;
		}
	}
}
