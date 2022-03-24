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

import com.beigebinder.data.Account;
import com.beigebinder.data.Currency;
import com.beigebinder.misc.Util;
import com.beigebinder.persist.AccountPersist;
import com.beigebinder.persist.CurrencyPersist;
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

public class EditAccount extends PopupScreen {
	private PopupScreen _popPopupScreen;
	private Account _account;
	private EditField _description;
	private EditField _initialBalance;
	private EditField _memo;
	private ObjectChoiceField _currency;
	private ObjectChoiceField _type;
	private CheckboxField _closed;
	private boolean _isCanceled;

	private static ResourceBundle _resources = ResourceBundle.getBundle(MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public EditAccount() {
		super(new DialogFieldManager(), PopupScreen.DEFAULT_MENU);
		_account = null;
		_isCanceled = false;
		_popPopupScreen = this;
		DialogFieldManager dfm = (DialogFieldManager) getDelegate();
		dfm.setMessage(new RichTextField(_resources.getString(MoneyResource.NEWACCOUNT), RichTextField.NON_FOCUSABLE));

		_description = new EditField(_resources.getString(MoneyResource.NAME), "", 30, EditField.NO_NEWLINE);
		_initialBalance = new EditField(_resources.getString(MoneyResource.BALANCEINITIAL), "0.00", 15, EditField.FILTER_REAL_NUMERIC | EditField.NO_NEWLINE);
		_memo = new EditField(_resources.getString(MoneyResource.MEMO), "", 250, EditField.NO_NEWLINE);
		Currency[] currencies = CurrencyPersist.getInstance().get();
		_currency = new ObjectChoiceField(_resources.getString(MoneyResource.CURRENCY), currencies);
		_currency.setSelectedIndex(CurrencyPersist.getInstance().getDefaulCurrency());
		_type = new ObjectChoiceField(_resources.getString(MoneyResource.TYPE), _resources.getStringArray(MoneyResource.ACCOUNTTYPES));
		_closed = new CheckboxField(_resources.getString(MoneyResource.CLOSED), false);

		dfm.addCustomField(_description);
		dfm.addCustomField(_type);
		dfm.addCustomField(_currency);
		dfm.addCustomField(_initialBalance);
		dfm.addCustomField(_closed);
		dfm.addCustomField(_memo);
	}

	public boolean pickAccount() {
		UiApplication.getUiApplication().pushModalScreen(this);
		return !_isCanceled;
	}

	public Account getAccount() {
		return _account;
	}

	public void setAccount(Account account) {
		DialogFieldManager dfm = (DialogFieldManager) getDelegate();
		dfm.setMessage(new RichTextField(_resources.getString(MoneyResource.EDITACCOUNT), RichTextField.NON_FOCUSABLE));

		_account = account;
		Currency currency = CurrencyPersist.getInstance().get(_account.getCurrencyID());
		long initialBalance = _account.getInitialBalance();
		if (6 <= _account.getType())
			initialBalance *= -1;
		_initialBalance.setText(Util.toString(initialBalance, currency, true));
		_description.setText(_account.getDescription());
		_memo.setText(_account.getMemo());
		_currency.setSelectedIndex(currency);
		_type.setSelectedIndex(_account.getType());
		_closed.setChecked(_account.isClosed());
	}

	private Account takeAccount() {
		Currency currency = (Currency) _currency.getChoice(_currency.getSelectedIndex());

		String description = _description.getText();
		short type = (short) _type.getSelectedIndex();

		long initialBalance = Util.toLong(_initialBalance.getText(), currency);
		if (6 <= type)
			initialBalance *= -1;

		Account account = new Account();
		account.setDescription(description);
		account.setType(type);
		account.setInitialBalance(initialBalance);
		account.setMemo(_memo.getText());
		account.setCurrencyID(currency.getUID());
		account.setClosed(_closed.getChecked());
		if (_account != null)
			account.setId(_account.getUID());

		if (_account != null) {
			long finalBalance = _account.getFinalBalance() - _account.getInitialBalance() + initialBalance;
			long clearedBalance = account.getClearedBalance() - _account.getInitialBalance() + initialBalance;
			long reconciledBalance = account.getReconciledBalance() - _account.getInitialBalance() + initialBalance;

			account.setId(_account.getUID());
			account.setFinalBalance(finalBalance);
			account.setClearedBalance(clearedBalance);
			account.setReconciledBalance(reconciledBalance);

		} else {
			account.setId(UIDGenerator.getUID());
			account.setFinalBalance(initialBalance);
			account.setClearedBalance(initialBalance);
			account.setReconciledBalance(initialBalance);
		}

		return account;
	}

	public boolean isDataValid() {
		if (_description.getText().length() == 0) {
			Dialog.alert(_resources.getString(MoneyResource.DESCRIPTIONINVALID));
			_description.setFocus();
			return false;
		}

		try {
			Double.parseDouble(_initialBalance.getText());
		} catch (NumberFormatException numberFormatException) {
			Dialog.alert(_resources.getString(MoneyResource.AMMOUNTINVALID));
			_initialBalance.setFocus();
			return false;
		}

		Account account = takeAccount();
		if (AccountPersist.getInstance().exist(account)) {
			Dialog.alert(_resources.getString(MoneyResource.ACCOUNTEXISTS));
			return false;
		}

		_account = account;

		return super.isDataValid();
	}

	protected void makeMenu(Menu menu, int instance) {
		menu.add(_ok);
		menu.add(_close);
		menu.setDefault(_ok);
		super.makeMenu(menu, instance);
	}

	private MenuItem _ok = new MenuItem(_resources, MoneyResource.SAVE, 100, 100) {
		public void run() {
			if (_popPopupScreen.isDirty()) {
				if (_popPopupScreen.isDataValid()) {
					UiApplication.getUiApplication().popScreen(_popPopupScreen);
				}
			} else {
				_isCanceled = true;
				UiApplication.getUiApplication().popScreen(_popPopupScreen);
			}
		}
	};

	private MenuItem _close = new MenuItem(_resources, MoneyResource.CLOSE, 110, 110) {
		public void run() {
			_isCanceled = true;
			UiApplication.getUiApplication().popScreen(_popPopupScreen);
		}
	};

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
