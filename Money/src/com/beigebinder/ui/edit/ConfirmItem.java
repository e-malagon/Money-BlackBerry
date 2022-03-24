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

import com.beigebinder.data.Currency;
import com.beigebinder.data.Item;
import com.beigebinder.misc.Util;
import com.beigebinder.resource.MoneyResource;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.FocusChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.container.PopupScreen;

public class ConfirmItem extends PopupScreen {
	private PopupScreen _popPopupScreen;
	private Currency _currency;
	private boolean _isCanceled;
	private EditField _quantity;
	private EditField _amount;
	private EditField _total;
	private Item _item;

	private static ResourceBundle _resources = ResourceBundle.getBundle(MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public ConfirmItem(Item item, Currency currency) {
		super(new DialogFieldManager(), 0);
		_popPopupScreen = this;
		_isCanceled = false;
		_currency = currency;
		_item = item;

		DialogFieldManager dfm = (DialogFieldManager) getDelegate();
		dfm.setMessage(new RichTextField(_resources.getString(MoneyResource.CONFIRMAMOUNT), RichTextField.NON_FOCUSABLE));

		_quantity = new EditField(_resources.getString(MoneyResource.QUANTITY), String.valueOf(item.getQuantity()), 3, EditField.FILTER_REAL_NUMERIC | EditField.NO_NEWLINE);
		dfm.addCustomField(_quantity);

		_amount = new EditField(_resources.getString(MoneyResource.VALUE) + _currency.getSign() + " ", Util.toString(item.getAmount(), _currency, true), 15, EditField.FILTER_REAL_NUMERIC | EditField.NO_NEWLINE);
		dfm.addCustomField(_amount);

		long total = item.getAmount() * item.getQuantity();

		_total = new EditField(_resources.getString(MoneyResource.TOTAL) + _currency.getSign() + " ", Util.toString(total, _currency, true), 15, EditField.FILTER_REAL_NUMERIC | EditField.NO_NEWLINE
				| EditField.NON_FOCUSABLE);
		dfm.addCustomField(_total);

		FocusChangeListener focusChangeListener = new FocusChangeListener() {
			public void focusChanged(Field field, int eventType) {
				if (eventType == FocusChangeListener.FOCUS_LOST) {
					if (field.equals(_amount)) {
						try {
							Double.parseDouble(_amount.getText());
						} catch (NumberFormatException numberFormatException) {
							Dialog.alert(_resources.getString(MoneyResource.AMMOUNTINVALID));
							_amount.setFocus();
							return;
						}
					}
					if (field.equals(_quantity)) {
						try {
							Integer.parseInt((_quantity.getText()));
						} catch (NumberFormatException numberFormatException) {
							Dialog.alert(_resources.getString(MoneyResource.AMMOUNTINVALID));
							_quantity.setFocus();
							return;
						}
					}
					long total = Util.toLong(_amount.getText(), _currency) * Integer.parseInt((_quantity.getText()));

					_total.setText(Util.toString(total, _currency, true));
				}
			}
		};

		_quantity.setFocusListener(focusChangeListener);
		_amount.setFocusListener(focusChangeListener);

		ButtonField buttonField = new ButtonField(_resources.getString(MoneyResource.SAVE), ButtonField.FIELD_HCENTER);
		FieldChangeListener changeListener = new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (context != FieldChangeListener.PROGRAMMATIC) {
					_item.setAmount(Util.toLong(_amount.getText(), _currency));
					_item.setQuantity(Integer.parseInt((_quantity.getText())));
					UiApplication.getUiApplication().popScreen(_popPopupScreen);
				}
			}
		};
		buttonField.setChangeListener(changeListener);
		dfm.addCustomField(buttonField);
		buttonField.setFocus();

		buttonField = new ButtonField(_resources.getString(MoneyResource.CANCEL), ButtonField.FIELD_HCENTER);
		changeListener = new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (context != FieldChangeListener.PROGRAMMATIC) {
					_isCanceled = true;
					UiApplication.getUiApplication().popScreen(_popPopupScreen);
				}
			}
		};
		buttonField.setChangeListener(changeListener);
		dfm.addCustomField(buttonField);

		UiApplication.getUiApplication().pushModalScreen(this);
	}

	public boolean isCanceled() {
		return _isCanceled;
	}

	protected boolean keyChar(char key, int status, int time) {
		boolean retval = false;
		switch (key) {
		case Characters.ESCAPE:
			retval = true;
			_isCanceled = true;
			UiApplication.getUiApplication().popScreen(_popPopupScreen);
			break;
		default:
			retval = super.keyChar(key, status, time);
			break;
		}
		return retval;
	}
}
