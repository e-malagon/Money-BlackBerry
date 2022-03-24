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
package com.beigebinder.ui.edit;

import com.beigebinder.data.Category;
import com.beigebinder.persist.CategoryPersist;
import com.beigebinder.resource.MoneyResource;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.synchronization.UIDGenerator;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.container.PopupScreen;

public class EditCategory extends PopupScreen {
	private ObjectChoiceField _choises;
	private EditField _description;
	private boolean _isCanceled;
	private int _uid;

	private static ResourceBundle _resources = ResourceBundle.getBundle(MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public EditCategory(String title) {
		super(new DialogFieldManager(), DEFAULT_MENU);
		DialogFieldManager dfm = (DialogFieldManager) getDelegate();
		dfm.setMessage(new RichTextField(title, RichTextField.NON_FOCUSABLE));

		_isCanceled = false;
		String[] types = _resources.getStringArray(MoneyResource.OPERATIONSTYPESEI);
		_choises = new ObjectChoiceField(_resources.getString(MoneyResource.CATEGORY), types);
		_description = new EditField(_resources.getString(MoneyResource.DESCRIPTION), "", 30, EditField.NO_NEWLINE);
		_uid = 0;

		dfm.addCustomField(_choises);
		dfm.addCustomField(_description);
	}

	public boolean pickCategory() {
		UiApplication.getUiApplication().pushModalScreen(this);
		return !_isCanceled;
	}

	public void setCategory(Category category) {
		_choises.setSelectedIndex(category.getType());
		_description.setText(category.getDescription());
		_uid = category.getUID();
	}

	public Category getCategory() {
		Category category = new Category();
		category.setType((short) _choises.getSelectedIndex());
		category.setDescription(_description.getText());
		if (_uid != 0)
			category.setId(_uid);
		else
			category.setId(UIDGenerator.getUID());
		return category;
	}

	protected boolean keyChar(char c, int status, int time) {
		if (c == Characters.ESCAPE) {
			_isCanceled = true;
			this.close();
			return true;
		} else if (c == Characters.ENTER) {
			this.close();
			return true;
		}

		return super.keyChar(c, status, time);
	}

	protected void makeMenu(Menu menu, int instance) {
		menu.add(_save);
		menu.add(_close);
		super.makeMenu(menu, instance);
	}

	private MenuItem _save = new MenuItem(_resources, MoneyResource.SAVE, 100, 100) {
		public void run() {
			if (CategoryPersist.getInstance().exist(getCategory())) {
				Dialog.alert(_resources.getString(MoneyResource.CATEGORYEXISTS));
				return;
			}
			close();
		}
	};

	private MenuItem _close = new MenuItem(_resources, MoneyResource.CLOSE, 110, 110) {
		public void run() {
			_isCanceled = true;
			close();
		}
	};
}
