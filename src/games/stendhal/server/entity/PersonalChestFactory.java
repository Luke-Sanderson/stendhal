/*
 * @(#) src/games/stendhal/server/entity/PersonalChestFactory.java
 *
 * $Id$
 */

package games.stendhal.server.entity;

import games.stendhal.server.config.factory.ConfigurableFactory;
import games.stendhal.server.config.factory.ConfigurableFactoryContext;

/**
 * A factory for <code>PersonalChest</code> objects.
 */
public class PersonalChestFactory implements ConfigurableFactory {

	//
	// PersonalChestFactory
	//

	/**
	 * Extract the slot name from a context.
	 *
	 * @param ctx The configuration context. 'slot' must be defined in ctx
	 * @return The slot name.
	 *
	 */
	protected String getSlot(ConfigurableFactoryContext ctx) {
		return ctx.getString("slot", PersonalChest.DEFAULT_BANK);
	}

	//
	// ConfigurableFactory
	//

	/**
	 * Create a personal chest.
	 *
	 * @param ctx
	 *            Configuration context.
	 *
	 * @return A PersonalChest.
	 *
	 * @throws IllegalArgumentException
	 *             If there is a problem with the attributes. The exception
	 *             message should be a value suitable for meaningful user
	 *             interpretation.
	 *
	 * @see PersonalChest
	 */
	public Object create(ConfigurableFactoryContext ctx) {
		return new PersonalChest(getSlot(ctx));
	}
}
