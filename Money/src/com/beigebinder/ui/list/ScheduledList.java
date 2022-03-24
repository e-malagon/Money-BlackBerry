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
package com.beigebinder.ui.list;

import com.beigebinder.data.Notification;
import com.beigebinder.data.Transaction;
import com.beigebinder.misc.UpdateCallback;
import com.beigebinder.persist.MiscellaneousPersist;
import com.beigebinder.persist.NotificationPersist;
import com.beigebinder.persist.SavedTransactionsPersist;
import com.beigebinder.resource.MoneyResource;
import com.beigebinder.ui.edit.EditScheduled;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.container.MainScreen;

public final class ScheduledList implements UpdateCallback {
	private ScheduledList _miscList;
	private static ResourceBundle _resources = ResourceBundle.getBundle(
			MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public ScheduledList() {
		this._miscList = this;
		_mainScreen
				.setTitle(_resources.getString(MoneyResource.SCHEDULESTITLE));

		this.update();
		_mainScreen.add(_itemsList);
		MiscellaneousPersist.getInstance().suscribe(_miscList);
		UiApplication.getUiApplication().pushScreen(_mainScreen);
	}

	private MainScreen _mainScreen = new MainScreen(MainScreen.DEFAULT_MENU
			| MainScreen.DEFAULT_CLOSE) {
		protected void makeMenu(Menu menu, int instance) {
			if (UiApplication.getUiApplication().getActiveScreen()
					.getFieldWithFocus().equals(_itemsList)) {
				int selectedIndex = _itemsList.getSelectedIndex();
				if (selectedIndex != -1) {
					menu.add(_newItem);
					menu.add(_editItem);
					menu.add(_deleteItem);
				} else {
					menu.add(_newItem);
				}
				menu.add(MenuItem.separator(121));
			}
			super.makeMenu(menu, instance);
		}

		public void close() {
			MiscellaneousPersist.getInstance().unSuscribe(_miscList);
			super.close();
		}

	};

	private MenuItem _newItem = new MenuItem(_resources, MoneyResource.NEW, 100, 100) {
		public void run() {
			new EditScheduled();
		}
	};

	private MenuItem _editItem = new MenuItem(_resources, MoneyResource.EDIT, 110, 110) {
		public void run() {
			int selectedIndex = _itemsList.getSelectedIndex();
			Notification object = (Notification) _itemsList.get(_itemsList,
					selectedIndex);

			new EditScheduled(object);
		}
	};

	private MenuItem _deleteItem = new MenuItem(_resources,	MoneyResource.DELETE, 120, 120) {
		public void run() {
			String ask;
			int selectedIndex = _itemsList.getSelectedIndex();
			ask = _resources.getString(MoneyResource.ASKFORDELETENOTIFICATION);

			if (Dialog.ask(Dialog.D_DELETE, ask, Dialog.CANCEL) == Dialog.DELETE) {
				Object obj = _itemsList.get(_itemsList, selectedIndex);

				if (obj instanceof Notification) {
					Notification schedule = (Notification) obj;
					Transaction transaction = SavedTransactionsPersist
							.getInstance().get(schedule.getTransactionId());
					SavedTransactionsPersist.getInstance().remove(transaction);
					NotificationPersist.getInstance().remove(schedule);
				}

				update();
			}
		}
	};

	public void update() {
		Object[] elements;
		elements = NotificationPersist.getInstance().get();
		_itemsList.set(elements);
	}

	private ObjectListField _itemsList = new ObjectListField() {

	};
}
