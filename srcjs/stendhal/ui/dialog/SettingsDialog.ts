/***************************************************************************
 *                     Copyright © 2003-2022 - Arianne                     *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { DialogContentComponent } from "../component/DialogContentComponent";

declare var marauroa: any;
declare var stendhal: any;


export class SettingsDialog extends DialogContentComponent {

	private reloadRequired = false;


	constructor() {
		super("settingsdialog-template");

		const chk_blood = this.getCheckBox("chk_blood")!;
		chk_blood.checked = stendhal.config.gamescreen.blood;
		chk_blood.addEventListener("change", (e) => {
			stendhal.config.gamescreen.blood = chk_blood.checked;
			this.reloadRequired = true; // only required to immediately update corpses & tiles
		});

		const chk_shadows = this.getCheckBox("chk_shadows")!;
		chk_shadows.checked = stendhal.config.gamescreen.shadows;
		chk_shadows.addEventListener("change", (e) => {
			stendhal.config.gamescreen.shadows = chk_shadows.checked;
		});

		const chk_dblclick = this.getCheckBox("chk_dblclick")!;
		chk_dblclick.checked = stendhal.config.itemDoubleClick;
		chk_dblclick.addEventListener("change", (e) => {
			stendhal.config.itemDoubleClick = chk_dblclick.checked;
		});

		const chk_movecont = this.getCheckBox("chk_movecont")!;
		chk_movecont.checked = stendhal.config.moveCont;
		chk_movecont.addEventListener("change", (e) => {
			stendhal.config.moveCont = chk_movecont.checked;
			const action = {"type": "move.continuous"} as {[index: string]: string;};
			if (chk_movecont.checked) {
				action["move.continuous"] = "";
			}

			marauroa.clientFramework.sendAction(action);
		});

		const sel_theme = <HTMLSelectElement>
				this.componentElement.querySelector("select[id='selecttheme']")!;
		const themes = Object.keys(stendhal.config.theme.index);
		for (const key of themes) {
			const opt = document.createElement("option");
			opt.value = key;
			if (key === "wood") {
				opt.innerHTML = key + " (default)";
			} else {
				opt.innerHTML = key;
			}
			sel_theme.appendChild(opt);
		}
		sel_theme.selectedIndex = themes.indexOf(stendhal.config.theme.current);
		sel_theme.addEventListener("change", (o) => {
			stendhal.config.theme.current = themes[sel_theme.selectedIndex];
			this.reloadRequired = true;
		});

		const btn_accept = this.getButton("config_accept")!;
		const btn_cancel = this.getButton("config_cancel")!;

		btn_accept.addEventListener("click", (e: Event) => {
			if (this.frame != null) {
				this.frame.close();
			}

			if (this.reloadRequired) {
				// FIXME: need confirmation dialog
				let params = "?char=" + stendhal.config.character
						+ "&theme=" + stendhal.config.theme.current;

				if (stendhal.config.itemDoubleClick) {
					params += "&item_doubleclick";
				}
				if (stendhal.config.moveCont) {
					params += "&movecont";
				}
				if (!stendhal.config.gamescreen.blood) {
					params += "&noblood";
				}
				if (!stendhal.config.gamescreen.shadows) {
					params += "&noshadows";
				}

				document.location = params;
			}
		});

		btn_cancel.addEventListener("click", (e: Event) => {
			if (this.frame != null) {
				this.frame.close();
			}
		});
	}

	public override getConfigId(): string {
		return "settings";
	}

	/**
	 * Retrieves checkbox element.
	 *
	 * @param id
	 *     Identifier of element to retrieve.
	 * @return
	 *     HTMLInputElement.
	 */
	private getCheckBox(id: string): HTMLInputElement {
		return <HTMLInputElement> this.componentElement.querySelector(
			"input[type=checkbox][id=" + id + "]");
	}

	/**
	 * Retrieves a button element.
	 *
	 * @param id
	 *     Identifier of element to retrieve.
	 * @return
	 *     HTMLButtonElement.
	 */
	private getButton(id: string): HTMLButtonElement {
		return <HTMLButtonElement> this.componentElement.querySelector(
			"button[id=" + id + "]");
	}
}
