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

import com.beigebinder.data.SavedTransaction;
import com.beigebinder.data.Transaction;
import com.beigebinder.misc.UpdateCallback;
import com.beigebinder.persist.MiscellaneousPersist;
import com.beigebinder.persist.SavedTransactionsPersist;
import com.beigebinder.persist.TemplatesPersist;
import com.beigebinder.resource.MoneyResource;
import com.beigebinder.ui.edit.EditTemplate;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.container.MainScreen;

public final class TemplatesList implements UpdateCallback {
	private TemplatesList _miscList;
	private ObjectListField _itemsList;
	private static ResourceBundle _resources = ResourceBundle.getBundle(
			MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public TemplatesList() {
		this._miscList = this;
		_itemsList = new ObjectListField();
		_mainScreen.setTitle(_resources.getString(MoneyResource.TEMPLATES));

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
			new EditTemplate();
		}
	};

	private MenuItem _editItem = new MenuItem(_resources, MoneyResource.EDIT, 110, 110) {
		public void run() {
			int selectedIndex = _itemsList.getSelectedIndex();
			SavedTransaction object = (SavedTransaction) _itemsList.get(
					_itemsList, selectedIndex);

			new EditTemplate(object);
		}
	};

	private MenuItem _deleteItem = new MenuItem(_resources,	MoneyResource.DELETE, 120, 120) {
		public void run() {
			String ask;
			int selectedIndex = _itemsList.getSelectedIndex();

			ask = _resources.getString(MoneyResource.ASKFORDELETETRANSACTION);

			if (Dialog.ask(Dialog.D_DELETE, ask, Dialog.CANCEL) == Dialog.DELETE) {
				Object obj = _itemsList.get(_itemsList, selectedIndex);

				if (obj instanceof SavedTransaction) {
					SavedTransactionsPersist logic = SavedTransactionsPersist
							.getInstance();
					SavedTransaction savedTransaction = (SavedTransaction) obj;
					Transaction transaction = logic.get(savedTransaction
							.getTransactionId());
					logic.remove(transaction);
					TemplatesPersist.getInstance().remove(savedTransaction);
				}
				update();
			}
		}
	};

	public void update() {
		Object[] elements;
		elements = TemplatesPersist.getInstance().get();
		_itemsList.set(elements);
	}
}
