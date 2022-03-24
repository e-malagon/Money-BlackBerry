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

import com.beigebinder.data.Category;
import com.beigebinder.misc.UpdateCallback;
import com.beigebinder.persist.CategoryPersist;
import com.beigebinder.persist.MiscellaneousPersist;
import com.beigebinder.resource.MoneyResource;
import com.beigebinder.ui.edit.EditCategory;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.container.MainScreen;

public final class CategoryList implements UpdateCallback {
	private CategoryList _categoryList;
	private static ResourceBundle _resources = ResourceBundle.getBundle(
			MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public CategoryList() {
		_categoryList = this;
		_mainScreen.setTitle(_resources
				.getString(MoneyResource.CATEGORIESTITLE));
		_listField.set(CategoryPersist.getInstance().get());
		_mainScreen.add(_listField);
		UiApplication.getUiApplication().pushScreen(_mainScreen);
		MiscellaneousPersist.getInstance().suscribe(_categoryList);
	}

	public void update() {
		_listField.set(CategoryPersist.getInstance().get());
	}

	private MainScreen _mainScreen = new MainScreen(MainScreen.DEFAULT_MENU
			| MainScreen.DEFAULT_CLOSE) {
		protected void makeMenu(Menu menu, int instance) {
			int selectedIndex = _listField.getSelectedIndex();
			if (selectedIndex != -1) {
				menu.add(_newItem);
				menu.add(_editItem);
				menu.add(_deleteItem);
			} else {
				menu.add(_newItem);
			}
			menu.add(MenuItem.separator(121));

			super.makeMenu(menu, instance);
		}
		
		public void close() {
			MiscellaneousPersist.getInstance().unSuscribe(_categoryList);
			super.close();
		};


	};

	private MenuItem _newItem = new MenuItem(_resources, MoneyResource.NEW,
			100, 100) {
		public void run() {
			EditCategory editCategoryPopupScreen = new EditCategory(_resources
					.getString(MoneyResource.EDIT));
			if (editCategoryPopupScreen.pickCategory()) {
				Category category = editCategoryPopupScreen.getCategory();
				if (CategoryPersist.getInstance().exist(category)) {
					Dialog.alert(_resources
							.getString(MoneyResource.CATEGORYEXISTS));
					return;
				}
				CategoryPersist.getInstance().add(category);
				MiscellaneousPersist.getInstance().update();
			}
		}
	};

	private MenuItem _editItem = new MenuItem(_resources, MoneyResource.EDIT,
			110, 110) {
		public void run() {
			Category oldCategory = (Category) _listField.get(_listField,
					_listField.getSelectedIndex());
			EditCategory editCategoryPopupScreen = new EditCategory(_resources
					.getString(MoneyResource.EDIT));
			editCategoryPopupScreen.setCategory(oldCategory);
			if (editCategoryPopupScreen.pickCategory()) {
				Category newCategory = editCategoryPopupScreen.getCategory();
				if (CategoryPersist.getInstance().exist(newCategory)) {
					Dialog.alert(_resources
							.getString(MoneyResource.CATEGORYEXISTS));
					return;
				}
				CategoryPersist.getInstance().update(oldCategory, newCategory);
				MiscellaneousPersist.getInstance().update();
			}
		}
	};

	private MenuItem _deleteItem = new MenuItem(_resources,
			MoneyResource.DELETE, 120, 120) {
		public void run() {
			if (Dialog.ask(Dialog.D_DELETE, _resources
					.getString(MoneyResource.ASKFORDELETECATEGORY),
					Dialog.CANCEL) == Dialog.DELETE) {
				Category category = (Category) _listField.get(_listField,
						_listField.getSelectedIndex());
				CategoryPersist.getInstance().remove(category);
				_listField.set(CategoryPersist.getInstance().get());
				MiscellaneousPersist.getInstance().update();
			}
		}
	};

	private ObjectListField _listField = new ObjectListField() {
		public void drawListRow(ListField listField, Graphics graphics,
				int index, int y, int width) {
			Category category = (Category) this.get(this, index);
			String text;
			if (category.getType() == 0)
				text = "\u00bb " + category.getDescription();
			else
				text = "\u00ab " + category.getDescription();
			graphics.drawText(text, 0, y, 0, width);
		}
	};

}
