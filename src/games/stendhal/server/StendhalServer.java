/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.AutomaticGenerateINI;
import games.stendhal.server.core.engine.GenerateINI;
import games.stendhal.server.core.rp.DaylightPhase;

/**
 * Starts a Stendhal server
 *
 * @author hendrik
 */
public class StendhalServer {

	private static final Logger logger = Logger.getLogger(StendhalServer.class);

	private static String serverIni = "server.ini";
	private static boolean automatic = false;

	/**
	 * parses the command line for overwriten configuration file.
	 *
	 * @param args command line parameters
	 */
	private static void parseCommandLine(String[] args) {
		int i = 0;

		while (i < args.length - 1) {
			if (args[i].equals("-c")) {
				serverIni = args[i + 1];
			}
			if (args[i].equals("-auto-config")) {
				automatic = true;
			}
			i++;
		}
	}

	/**
	 * Starts a Stendhal server
	 *
	 * @param args command line arguments
	 * @throws FileNotFoundException in case the init file cannot be generated
	 */
	public static void main(String[] args) throws FileNotFoundException {
		parseCommandLine(args);
		if (!new File(serverIni).exists()) {
			if (!automatic) {
				System.out.println("Welcome to your own Stendhal Server.");
				System.out.println("");
				System.out.println("This seems to be the very first start because we could not find a server.ini.");
				System.out.println("So there are some simple questions for you to create it...");
				System.out.println("");
				GenerateINI.main(args, serverIni);
			} else {
				Map<String, String> environment = getFilteredEnvironment();
				new AutomaticGenerateINI(environment).write(new FileOutputStream(serverIni));
			}
		}
		marauroa.server.marauroad.main(args);

		// permanently sets DaylightPhase for testing purposes
		final String testPhase = System.getProperty("testing.daylightphase");
		if (testPhase != null) {
			for (DaylightPhase phase: DaylightPhase.values()) {
				if (phase.toString().equalsIgnoreCase(testPhase)) {
					logger.info("Setting testing DaylightPhase: " + phase);

					DaylightPhase.setTestingPhase(phase);
					break;
				}
			}
		}
	}

	/**
	 * @return {@link Map} with STENDHAL_ fixed key value pairs read from the environment.
	 */
	private static Map<String, String> getFilteredEnvironment() {
		return System.getenv().entrySet()
				.stream()
				.filter(e -> e.getKey().startsWith("STENDHAL_"))
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
	}
}
