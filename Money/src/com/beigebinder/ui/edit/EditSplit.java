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
import com.beigebinder.data.Currency;
import com.beigebinder.data.Split;
import com.beigebinder.misc.Util;
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

public class EditSplit extends PopupScreen {
	private Currency _currency;
	private EditField _description;
	private ObjectChoiceField _category;
	private EditField _amount;
	private int _uid;
	private boolean _isCancel;

	private static ResourceBundle _resources = ResourceBundle.getBundle(
			MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public EditSplit(String title) {
		super(new DialogFieldManager(), DEFAULT_MENU);
		DialogFieldManager dfm = (DialogFieldManager) getDelegate();
		dfm.setMessage(new RichTextField(title, RichTextField.NON_FOCUSABLE));

		Category[] categories = CategoryPersist.getInstance().get();
		_isCancel = false;

		_description = new EditField(_resources
				.getString(MoneyResource.DESCRIPTION), "", 30,
				EditField.NO_NEWLINE | EditField.FIELD_HCENTER);
		_category = new ObjectChoiceField(_resources
				.getString(MoneyResource.CATEGORY), categories);
		String sign = _currency != null ? _currency.getSign() : "";
		_amount = new EditField(sign + " ", "", 15,
				EditField.FILTER_REAL_NUMERIC | EditField.NO_NEWLINE);
		_uid = 0;

		dfm.addCustomField(_description);
		dfm.addCustomField(_category);
		dfm.addCustomField(_amount);
	}

	public boolean pickSplit() {
		UiApplication.getUiApplication().pushModalScreen(this);
		return !_isCancel;
	}

	public void setSplit(Split split) {
		_description.setText(split.getDescription());
		Category category = CategoryPersist.getInstance().get(
				split.getCategory(), split.getType());
		_category.setSelectedIndex(category);
		_amount.setText(Util.toString(split.getAmount(), _currency, true));
		_uid = split.getUID();
	}

	public Split getSplit() {
		Split split = new Split();
		Category category = (Category) _category.getChoice(_category
				.getSelectedIndex());
		split.setDescription(_description.getText());
		split.setType(category.getType());
		split.setCategory(category.getUID());
		String amountst = _amount.getText();
		if (amountst.length() == 0) {
			amountst = "0.00";
		}

		split.setAmount(Util.toLong(amountst, _currency));
		if (_uid != 0)
			split.setUID(_uid);
		return split;
	}

	public void setCurrency(Currency currency) {
		_currency = currency;
		String sign = _currency.getSign() + " ";
		_amount.setLabel(sign);
	}

	public void setLastCategoryUsed(int lastCategoryUsed, int type) {
		Category category = CategoryPersist.getInstance().get(lastCategoryUsed,
				type);
		_category.setSelectedIndex(category);
	}

	protected boolean keyChar(char c, int status, int time) {
		if (c == Characters.ESCAPE) {
			_isCancel = true;
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
		menu.add(_addCategory);
		menu.add(_close);
		super.makeMenu(menu, instance);
	}

	private MenuItem _save = new MenuItem(_resources, MoneyResource.SAVE, 100,
			100) {
		public void run() {
			if (isValid()) {
				if (isValid()) {
					close();
				}
			}
		}
	};

	private MenuItem _addCategory = new MenuItem(_resources,
			MoneyResource.NEWCATEGORY, 110, 110) {
		public void run() {
			EditCategory categoryPopupScreen = new EditCategory(_resources
					.getString(MoneyResource.NEWCATEGORY));
			if (categoryPopupScreen.pickCategory()) {
				Category newCategory = categoryPopupScreen.getCategory();
				newCategory.setId(UIDGenerator.getUID());
				CategoryPersist.getInstance().add(newCategory);
			}
		}
	};

	private MenuItem _close = new MenuItem(_resources, MoneyResource.CLOSE,
			120, 120) {
		public void run() {
			_isCancel = true;
			close();
		}
	};

	private boolean isValid() {
		if (_description.getText().length() == 0) {
			if (Dialog.ask(Dialog.D_YES_NO, _resources
					.getString(MoneyResource.DATAPAYEEEMPTY)) == Dialog.NO) {
				_description.setFocus();
				return false;
			}
		}
		String amountst = _amount.getText();
		if (amountst.length() > 0) {
			try {
				Double.parseDouble(amountst);
			} catch (NumberFormatException numberFormatException) {
				Dialog
						.alert(_resources
								.getString(MoneyResource.AMMOUNTINVALID));
				_amount.setFocus();
				return false;
			}
		}
		return true;
	}
}
