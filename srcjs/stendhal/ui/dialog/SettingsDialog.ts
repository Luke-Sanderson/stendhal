/***************************************************************************
 *                    Copyright © 2003-2022 - Arianne                      *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { Component } from "../toolkit/Component";

declare let stendhal: any;


export class SettingsDialog extends Component {

	public override configId = "settings";

	constructor() {
		super("settingsdialog-template");

		// FIXME: need a button to refresh page after adjusting settings

		const chk_blood = this.getCheckBox("chk_blood")!;
		chk_blood.setting = "blood";
		chk_blood.checked = stendhal.config.gamescreen.blood;

		const chk_shadows = this.getCheckBox("chk_shadows")!;
		chk_shadows.setting = "shadows";
		chk_shadows.checked = stendhal.config.gamescreen.shadows;

		[chk_blood, chk_shadows].forEach(chk => {
			this.addGameScreenCheckListener(chk);
		});

		const chk_dblclick = this.getCheckBox("chk_dblclick")!;
		chk_dblclick.setting = "itemDoubleClick";
		chk_dblclick.checked = stendhal.config.itemDoubleClick;
		this.addGeneralCheckListener(chk_dblclick);

		// apply theme
		this.componentElement.style.setProperty("background",
			"url(/data/gui/" + stendhal.config.theme + ")");
	}

	/**
	 * Sets up a checkbox change state listener.
	 *
	 * @param chk
	 *     CheckBox to apply listener.
	 */
	private addGeneralCheckListener(chk: CheckBox) {
		chk.addEventListener("change", e => {
			this.onToggleGeneralCheck(chk);
		});
	}

	/**
	 * Sets up a checkbox change state listener.
	 *
	 * @param chk
	 *     CheckBox to apply listener.
	 */
	private addGameScreenCheckListener(chk: CheckBox) {
		chk.addEventListener("change", e => {
			this.onToggleGameScreenCheck(chk);
		});
	}

	/**
	 * Updates configuration when checkbox values are changed.
	 *
	 * @param chk
	 *     The checkbox that changed state.
	 */
	private onToggleGeneralCheck(chk: CheckBox) {
		// FIXME: settings not being updated
		stendhal.config[chk.setting] = chk.checked;
	}

	/**
	 * Updates configuration when checkbox values are changed.
	 *
	 * @param chk
	 *     The checkbox that changed state.
	 */
	private onToggleGameScreenCheck(chk: CheckBox) {
		// FIXME: settings not being updated
		stendhal.config.gamescreen[chk.setting] = chk.checked;
	}

	/**
	 * Retrieves checkbox element.
	 *
	 * @param id
	 *     Identifier of element to retrieve.
	 * @return
	 *     HTMLInputElement.
	 */
	private getCheckBox(id: string): CheckBox {
		return <CheckBox> this.componentElement.querySelector(
			"input[type=checkbox][id=" + id + "]");
	}

	/**
	 * Updates window state for this session.
	 */
	public override onMoved() {
		/*
		const rect = this.componentElement.getBoundingClientRect();
		let newX = rect.left;
		let newY = rect.top;

		// keep dialog within view bounds
		if (newX < 0) {
			newX = 0;
			this.componentElement.style.left = "0px";
		}
		if (newY < 0) {
			newY = 0;
			this.componentElement.style.top = "0px";
		}

		// FIXME: need to check bounds of view width & height

		stendhal.config.windowstates.settings = {x: newX, y: newY};

		// DEBUG:
		console.log("SettingsDialog moved: " + newX + "," + newY);
		*/
	}
}


class CheckBox extends HTMLInputElement {
	public setting: string = "";
}