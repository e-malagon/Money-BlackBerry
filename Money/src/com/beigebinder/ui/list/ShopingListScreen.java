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

import com.beigebinder.data.ShopingList;
import com.beigebinder.misc.UpdateCallback;
import com.beigebinder.persist.MiscellaneousPersist;
import com.beigebinder.persist.ShopingListPersist;
import com.beigebinder.resource.MoneyResource;
import com.beigebinder.ui.edit.EditShopingList;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.container.MainScreen;

public class ShopingListScreen implements UpdateCallback {
	private ShopingListScreen _shopingListScreen;
	private ObjectListField _shopingLists;

	private static ResourceBundle _resources = ResourceBundle.getBundle(
			MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public ShopingListScreen() {
		_shopingListScreen = this;
		_mainScreen.setTitle(_resources.getString(MoneyResource.SHOPINGLISTS));
		_shopingLists = new ObjectListField();
		update();
		_mainScreen.add(_shopingLists);
		MiscellaneousPersist.getInstance().suscribe(_shopingListScreen);
		UiApplication.getUiApplication().pushScreen(_mainScreen);
	}

	private MainScreen _mainScreen = new MainScreen(MainScreen.DEFAULT_MENU
			| MainScreen.DEFAULT_CLOSE) {

		protected void makeMenu(Menu menu, int instance) {
			int selectedIndex = _shopingLists.getSelectedIndex();
			if (selectedIndex != -1) {
				menu.add(_newTransaction);
				menu.add(_editTransaction);
				menu.add(_deleteTransaction);
			} else {
				menu.add(_newTransaction);
			}
			super.makeMenu(menu, instance);
		}

		public void close() {
			super.close();
			MiscellaneousPersist.getInstance().unSuscribe(_shopingListScreen);
		}
	};

	MenuItem _newTransaction = new MenuItem(_resources,	MoneyResource.NEWSHOPINGLIST, 100, 100) {
		public void run() {
			new EditShopingList();
		}
	};

	MenuItem _editTransaction = new MenuItem(_resources, MoneyResource.EDITSHOPINGLIST, 110, 110) {
		public void run() {
			int selectedIndex = _shopingLists.getSelectedIndex();
			ShopingList shopingList = (ShopingList) _shopingLists.get(
					_shopingLists, selectedIndex);
			new EditShopingList(shopingList);
		}
	};

	MenuItem _deleteTransaction = new MenuItem(_resources, MoneyResource.DELETESHOPINGLIST, 120, 120) {
		public void run() {
			if (Dialog.ask(Dialog.D_DELETE, _resources
					.getString(MoneyResource.ASKFORDELETESHOPINGLIST),
					Dialog.CANCEL) == Dialog.DELETE) {
				int selectedIndex = _shopingLists.getSelectedIndex();
				ShopingList shopingList = (ShopingList) _shopingLists.get(
						_shopingLists, selectedIndex);
				ShopingListPersist.getInstance().remove(shopingList);
				update();
			}
		}
	};

	public void update() {
		_shopingLists.set(ShopingListPersist.getInstance().get());
	}
}
