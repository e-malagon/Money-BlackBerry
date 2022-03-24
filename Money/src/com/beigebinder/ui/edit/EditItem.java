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
import com.beigebinder.data.Item;
import com.beigebinder.misc.UpdateCallback;
import com.beigebinder.misc.Util;
import com.beigebinder.persist.CategoryPersist;
import com.beigebinder.persist.CurrencyPersist;
import com.beigebinder.persist.MiscellaneousPersist;
import com.beigebinder.resource.MoneyResource;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.synchronization.UIDGenerator;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.container.PopupScreen;

public class EditItem extends PopupScreen implements UpdateCallback {

	private EditField _description;
	private ObjectChoiceField _category;
	private EditField _amount;
	private CheckboxField _priority;
	private EditField _quantity;
	private CheckboxField _taxable;
	private EditField _memo;
	private Currency _currency;
	private int _uid;
	private boolean _cancel;

	private static ResourceBundle _resources = ResourceBundle.getBundle(MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public EditItem(String title, boolean taxlable) {
		super(new DialogFieldManager(), DEFAULT_MENU);
		DialogFieldManager dfm = (DialogFieldManager) getDelegate();
		dfm.setMessage(new RichTextField(title, RichTextField.NON_FOCUSABLE));

		_cancel = false;

		_currency = CurrencyPersist.getInstance().getDefaulCurrency();

		_description = new EditField(_resources.getString(MoneyResource.DESCRIPTION), "", 30, EditField.NO_NEWLINE);
		Category[] categories = CategoryPersist.getInstance().get();
		_category = new ObjectChoiceField(_resources.getString(MoneyResource.CATEGORY), categories);
		_amount = new EditField(_resources.getString(MoneyResource.VALUE) + _currency.getSign() + " ", "", 15, EditField.FILTER_REAL_NUMERIC | EditField.NO_NEWLINE);
		_priority = new CheckboxField(_resources.getString(MoneyResource.PRIORITY), false);
		_quantity = new EditField(_resources.getString(MoneyResource.QUANTITY), "1", 3, EditField.FILTER_REAL_NUMERIC | EditField.NO_NEWLINE);
		_taxable = new CheckboxField(_resources.getString(MoneyResource.TAXABLE), false);
		_memo = new EditField(_resources.getString(MoneyResource.MEMO), "", 250, 0);
		_uid = 0;

		dfm.addCustomField(_description);
		dfm.addCustomField(_category);
		dfm.addCustomField(_amount);
		dfm.addCustomField(_quantity);
		dfm.addCustomField(_priority);
		if (taxlable)
			dfm.addCustomField(_taxable);
		dfm.addCustomField(_memo);

		MiscellaneousPersist.getInstance().suscribe(this);
	}

	public boolean pickIten() {
		UiApplication.getUiApplication().pushModalScreen(this);
		return !_cancel;
	}

	public void setItem(Item item) {
		Category category = CategoryPersist.getInstance().get(item.getCategory(), 0);
		_description.setText(item.getDescription());
		_category.setSelectedIndex(category);
		_amount.setText(Util.toString(item.getAmount(), _currency, true));
		_quantity.setText(String.valueOf(item.getQuantity()));
		_priority.setChecked(item.isHiPriority());
		_memo.setText(item.getMemo());
		_taxable.setChecked(item.isTaxable());
		_uid = item.getUID();
	}

	public Item getItem() {
		Item item = new Item();
		int selectedIndex = _category.getSelectedIndex();
		if (selectedIndex != -1) {
			Category category = (Category) _category.getChoice(selectedIndex);
			item.setCategory(category.getUID());
		}
		item.setDescription(_description.getText());
		String amountst = _amount.getText();
		if (amountst.length() == 0) {
			amountst = "0.00";
		}

		item.setAmount(Util.toLong(amountst, _currency));
		item.setQuantity(Integer.parseInt((_quantity.getText())));
		item.setHiPriority(_priority.getChecked());
		item.setTaxable(_taxable.getChecked());
		item.setMemo(_memo.getText());
		if (_uid != 0)
			item.setUID(_uid);
		return item;
	}

	public void setCurrency(Currency currency) {
		_currency = currency;
		String sign = _currency.getSign() + " ";
		_amount.setLabel(sign);
	}

	public void setLastCategoryUsed(int lastCategoryUsed, int type) {
		Category category = CategoryPersist.getInstance().get(lastCategoryUsed, type);
		_category.setSelectedIndex(category);
	}

	private boolean isValid() {
		if (_description.getText().length() == 0) {
			if (Dialog.ask(Dialog.D_YES_NO, _resources.getString(MoneyResource.DATAEMPTY)) == Dialog.NO) {
				_description.setFocus();
				return false;
			}
		}
		String amountst = _amount.getText();
		if (amountst.length() > 0) {
			try {
				Double.parseDouble(amountst);
			} catch (NumberFormatException numberFormatException) {
				Dialog.alert(_resources.getString(MoneyResource.AMMOUNTINVALID));
				_amount.setFocus();
				return false;
			}
		}
		try {
			Integer.parseInt((_quantity.getText()));
		} catch (NumberFormatException numberFormatException) {
			Dialog.alert(_resources.getString(MoneyResource.AMMOUNTINVALID));
			_quantity.setFocus();
			return false;
		}
		return true;
	}

	public void update() {
		Object selectedItem = null;
		int selectedIndex;
		Object[] choices = CategoryPersist.getInstance().get();

		selectedIndex = _category.getSelectedIndex();
		if (selectedIndex != -1)
			selectedItem = _category.getChoice(selectedIndex);
		_category.setChoices(choices);
		if (selectedIndex != -1)
			_category.setSelectedIndex(selectedItem);
	}

	protected boolean keyChar(char c, int status, int time) {
		if (c == Characters.ESCAPE) {
			_cancel = true;
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
		menu.setDefault(_save);
		super.makeMenu(menu, instance);
	}

	private MenuItem _save = new MenuItem(_resources, MoneyResource.SAVE, 100, 100) {
		public void run() {
			if (isValid()) {
				close();
			}
		}
	};

	private MenuItem _addCategory = new MenuItem(_resources, MoneyResource.NEWCATEGORY, 110, 110) {
		public void run() {
			EditCategory categoryPopupScreen = new EditCategory(_resources.getString(MoneyResource.NEWCATEGORY));
			if (categoryPopupScreen.pickCategory()) {
				Category newCategory = categoryPopupScreen.getCategory();
				newCategory.setId(UIDGenerator.getUID());
				CategoryPersist.getInstance().add(newCategory);
			}
		}
	};

	private MenuItem _close = new MenuItem(_resources, MoneyResource.CLOSE, 120, 120) {
		public void run() {
			_cancel = true;
			close();
		}
	};
}
