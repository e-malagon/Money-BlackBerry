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
package com.beigebinder.ui.misc;

import com.beigebinder.persist.AccountPersist;
import com.beigebinder.persist.MiscellaneousPersist;
import com.beigebinder.resource.MoneyResource;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;

public final class RegistrationScreen {
	private EditField _key;
	private LabelField _message;
	private MainScreen _mscreen;
	private boolean _okB;
	private static ResourceBundle _resources = ResourceBundle.getBundle(MoneyResource.BUNDLE_ID,
			MoneyResource.BUNDLE_NAME);

	public RegistrationScreen(MainScreen mainScreen, boolean dok) {
		MiscellaneousPersist logic = MiscellaneousPersist.getInstance();
		_mscreen = mainScreen;
		_okB = dok;
		_mainScreen.setTitle(_resources.getString(MoneyResource.REGISTRATION));
		_key = new EditField(_resources.getString(MoneyResource.ENDTRIAL), "", 5, EditField.EDITABLE
				| EditField.NO_NEWLINE);

		_message = new LabelField(_resources.getString(MoneyResource.CONTACT));

		if (dok)
			_key.setLabel(_resources.getString(MoneyResource.ENDTRIAL2));
		_key.setText(logic.getKey());
		_mainScreen.add(_key);
		_mainScreen.add(new LabelField(" "));
		_mainScreen.add(_message);

		UiApplication.getUiApplication().pushScreen(_mainScreen);
	}

	private MainScreen _mainScreen = new MainScreen(MainScreen.DEFAULT_MENU | MainScreen.DEFAULT_CLOSE) {
		public boolean isDirty() {
			return false;
		}

		public boolean isDataValid() {
			MiscellaneousPersist logic = MiscellaneousPersist.getInstance();
			logic.setKey(_key.getText());
			try {
				AccountPersist.getInstance().findAccount();
				_okB = true;
			} catch (Exception ex) {
				Dialog.alert(_resources.getString(MoneyResource.ENDTRIALERR));
				logic.setKey("");
				return false;
			}
			return super.isDataValid();
		}

		public void close() {
			if (!_okB)
				_mscreen.close();
			super.close();
		}

		protected void makeMenu(Menu menu, int instance) {
			menu.add(_ok);
		};
	};

	private MenuItem _ok = new MenuItem(_resources, MoneyResource.SAVE, 100, 100) {
		public void run() {
			if (_mainScreen.isDataValid()) {
				_mainScreen.close();
			}
		}
	};
}
