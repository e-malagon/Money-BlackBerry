/*
** This program is free software: you can redistribute it and/or modify
** it under the terms of the GNU General Public License as published by
** the Free Software Foundation, either version 3 of the License, or
** (at your option) any later version.
**
** This program is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU General Public License for more details.
**
** You should have received a copy of the GNU General Public License
** along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.beigebinder.key;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.CodeModuleGroup;
import net.rim.device.api.system.CodeModuleGroupManager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class PantallaPrincipal {
	private EditField _text;
	private VerticalFieldManager _keys;
	MainScreen _mainScreen = new MainScreen(MainScreen.DEFAULT_MENU
			| MainScreen.DEFAULT_CLOSE);

	public PantallaPrincipal() {
		ApplicationDescriptor apDes = ApplicationDescriptor
				.currentApplicationDescriptor();

		StringBuffer names = new StringBuffer();

		CodeModuleGroup group;
		group = null;
		CodeModuleGroup[] groups = CodeModuleGroupManager.loadAll();
		String myAppName = "Money";
		group = null;
		if (groups != null) {
			for (int i = 0; i < groups.length; ++i) {
				if (groups[i].containsModule(myAppName)) {
					group = groups[i];
					break;
				}
			}
		}
		if (group != null) {
			names.append("Se localizo(1):\n '" + myAppName + "'\n Key: '"+group.getProperty("RIM_APP_WORLD_LICENSE_KEY")+"'\n");
		}

		myAppName = "Money for BlackBerry:BeigeBinder";
		group = CodeModuleGroupManager.load(myAppName);

		if (group != null) {
			names.append("Se localizo(2):\n '" + myAppName + "'\n Key: '"+group.getProperty("RIM_APP_WORLD_LICENSE_KEY")+"'\n");
		}

		
		
		RichTextField texto = new RichTextField();
		texto.setText(names.toString());

		_text = new EditField("PIN: ", "", 8, EditField.NO_NEWLINE);
		_keys = new VerticalFieldManager();
		_mainScreen.add(_text);
		_mainScreen.add(_keys);
		_mainScreen.add(new LabelField(" "));
		_mainScreen.add(texto);

		if (KeyPersist.getInstance().isValidKey()) {
			_mainScreen.addMenuItem(_obtain);
		} else {
			_mainScreen.addMenuItem(_activate);
		}
		_mainScreen.setTitle("Keys - Version " + apDes.getVersion());
		UiApplication.getUiApplication().pushScreen(_mainScreen);
	}

	private MenuItem _activate = new MenuItem("Activate", 1, 17) {
		public void run() {
			TextPopupScreen popupScreen = new TextPopupScreen("Key");
			if (popupScreen.pickText()) {
				try {
					long key = Long.parseLong(popupScreen.getText());
					KeyPersist.getInstance().setKey(key);
					if (KeyPersist.getInstance().isValidKey()) {
						_mainScreen.removeMenuItem(_activate);
						_mainScreen.addMenuItem(_obtain);
					} else {
						Dialog.inform("Invalid key");
					}
				} catch (Exception e) {
					Dialog.inform("Invalid key");
				}
			}
		}
	};

	private MenuItem _obtain = new MenuItem("Obtain", 1, 18) {
		public void run() {
			_keys.deleteAll();
			_keys.add(new EditField("Money: ", getMoneyKey(_text.getText()),
					10, EditField.NO_EDIT_MODE_INPUT));
			_text.setDirty(false);

		}
	};

	private String getMoneyKey(String pin) {
		byte[] opCharKey = { 1, 2, 3, 4, 5, 6 };
		opCharKey = pin.toUpperCase().getBytes();

		int size = opCharKey.length;
		int opKey = 0;
		for (int i = 0; i < size; i++) {
			opKey += (i == 4 ? (opKey * 13) : 0);
			opKey += (opCharKey[i] * 11);
		}

		opKey = opKey & 0xFFFF;

		return String.valueOf(opKey);
	}
}
