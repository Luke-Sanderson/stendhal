/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc.condition;

import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.parser.Sentence;
import games.stendhal.server.entity.player.Player;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Does the player carry the specified item?
 * 
 * @see games.stendhal.server.entity.npc.action.DropItemAction
 */
public class PlayerHasItemWithHimCondition implements ChatCondition {

	private final String itemName;
	private final int amount;

	/**
	 * Creates a new PlayerHasItemWithHim.
	 * 
	 * @param itemName
	 *            name of item
	 */
	public PlayerHasItemWithHimCondition(final String itemName) {
		this.itemName = itemName;
		this.amount = 1;
	}

	/**
	 * Creates a new PlayerHasItemWithHim.
	 * 
	 * @param itemName
	 *            name of item
	 * @param amount
	 *            for StackableItems
	 */
	public PlayerHasItemWithHimCondition(final String itemName, final int amount) {
		this.itemName = itemName;
		this.amount = amount;
	}

	public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
		return player.isEquipped(itemName, amount);
	}

	@Override
	public String toString() {
		return "player has item <" + amount + " " + itemName + ">";
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, false,
				PlayerHasItemWithHimCondition.class);
	}
}
