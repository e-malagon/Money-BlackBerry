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

import com.beigebinder.common.GetDateScreen;
import com.beigebinder.data.Account;
import com.beigebinder.data.Currency;
import com.beigebinder.data.ExpenseExchangeRate;
import com.beigebinder.data.IncomeExchangeRate;
import com.beigebinder.data.SavedTransaction;
import com.beigebinder.data.Transaction;
import com.beigebinder.data.Transfer;
import com.beigebinder.misc.UpdateCallback;
import com.beigebinder.misc.Util;
import com.beigebinder.persist.AccountPersist;
import com.beigebinder.persist.CurrencyPersist;
import com.beigebinder.persist.MiscellaneousPersist;
import com.beigebinder.persist.SavedTransactionsPersist;
import com.beigebinder.persist.TemplatesPersist;
import com.beigebinder.persist.TransactionPersist;
import com.beigebinder.resource.MoneyResource;
import com.beigebinder.ui.edit.EditTransaction;
import com.beigebinder.ui.misc.GetTemplateScreen;
import com.beigebinder.ui.misc.ReconcileScreen;
import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.ObjectGroup;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.FocusChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.util.DateTimeUtilities;

public class TransactionList implements UpdateCallback {
	private TransactionList _transactionList;
	private Transaction[] _transactions;
	private Account _account;
	private EditField _balanceField;
	private EditField _clearedBalanceField;
	private EditField _reconciledBalanceField;
	private EditField _currentBalanceField;
	private Currency _currency;
	private DateFormat _sdFormat;
	private long _currentBalance;
	private int _view;
	private int _index;
	private static ResourceBundle _resources = ResourceBundle.getBundle(MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public TransactionList(Account account) {
		this._account = account;
		this._transactionList = this;
		this._transactions = null;
		_sdFormat = SimpleDateFormat.getInstance(SimpleDateFormat.DATE_SHORT);
		_currency = CurrencyPersist.getInstance().get(account.getCurrencyID());
		_balanceField = new EditField(_resources.getString(MoneyResource.BALANCE) + " ", Util.toString(_account.getFinalBalance(), _currency, false), 250, EditField.NON_FOCUSABLE | EditField.FIELD_RIGHT);
		_clearedBalanceField = new EditField(_resources.getString(MoneyResource.BALANCECLEARED) + " ", Util.toString(_account.getClearedBalance(), _currency, false), 250, EditField.NON_FOCUSABLE | EditField.FIELD_RIGHT);
		_reconciledBalanceField = new EditField(_resources.getString(MoneyResource.BALANCERECONCILED) + " ", Util.toString(_account.getReconciledBalance(), _currency, false), 250, EditField.NON_FOCUSABLE
				| EditField.FIELD_RIGHT);
		_currentBalanceField = new EditField(_resources.getString(MoneyResource.CURRENTBALANCE) + " ", Util.toString(_account.getReconciledBalance(), _currency, false), 250, EditField.NON_FOCUSABLE
				| EditField.FIELD_RIGHT);
		_mainScreen.setStatus(_currentBalanceField);
		_view = 3;
		_mainScreen.setTitle(account.getDescription() + " (" + _currency.getISOCode() + ")");
		int height = Font.getDefault().getHeight();
		height *= 2;
		update();
		_transactionsObjectField.setRowHeight(height);
		_transactionsObjectField.setFocusListener(_focusChangeListener);
		_mainScreen.add(_transactionsObjectField);
		MiscellaneousPersist.getInstance().suscribe(_transactionList);
		UiApplication.getUiApplication().pushScreen(_mainScreen);
	}

	private MainScreen _mainScreen = new MainScreen(MainScreen.DEFAULT_MENU | MainScreen.DEFAULT_CLOSE) {

		protected void makeMenu(Menu menu, int instance) {
			int selectedIndex = _transactionsObjectField.getSelectedIndex();
			if (selectedIndex != -1) {
				menu.add(_newTransaction);
				menu.add(_editTransaction);
				menu.add(_deleteTransaction);
				menu.add(_useTemplate);
				menu.add(_goto);
				menu.add(MenuItem.separator(141));
				if (_view != 0)
					menu.add(_viewBalance);
				if (_view != 1)
					menu.add(_viewCleared);
				if (_view != 2)
					menu.add(_viewReconciled);
				if (_view != 3)
					menu.add(_viewCurrent);
				menu.add(_reconcile);
			} else {
				menu.add(_newTransaction);
				menu.add(_useTemplate);
				menu.add(MenuItem.separator(131));
			}
			super.makeMenu(menu, instance);
		}

		public void close() {
			super.close();
			MiscellaneousPersist.getInstance().unSuscribe(_transactionList);
			MiscellaneousPersist.getInstance().update();
		}
	};

	MenuItem _newTransaction = new MenuItem(_resources, MoneyResource.NEWTRANSACTION, 100, 100) {
		public void run() {
			new EditTransaction(_account);
		}
	};

	MenuItem _editTransaction = new MenuItem(_resources, MoneyResource.EDITTRANSACTION, 110, 110) {
		public void run() {
			int selectedIndex = _transactionsObjectField.getSelectedIndex();
			Object obj = _transactionsObjectField.get(_transactionsObjectField, selectedIndex);
			if (obj instanceof Transaction) {
				Account account = _account;
				Transaction transaction = (Transaction) obj;
				if (transaction instanceof Transfer && 0 < transaction.getAmount()) {
					Transfer transfer = (Transfer) transaction;
					account = AccountPersist.getInstance().get(transfer.getMirrorAccount());
					transaction = TransactionPersist.getInstance().getMirrorTransaction(transaction);
				}
				new EditTransaction(account, transaction);
			}
		}
	};

	MenuItem _deleteTransaction = new MenuItem(_resources, MoneyResource.DELETETRANSACTION, 120, 120) {
		public void run() {
			if (Dialog.ask(Dialog.D_DELETE, _resources.getString(MoneyResource.ASKFORDELETETRANSACTION), Dialog.CANCEL) == Dialog.DELETE) {
				int selectedIndex = _transactionsObjectField.getSelectedIndex();
				Object obj = _transactionsObjectField.get(_transactionsObjectField, selectedIndex);
				if (obj instanceof Transaction) {
					Transaction transaction = (Transaction) obj;
					TransactionPersist.getInstance().remove(_account, transaction);
					update();
				}
			}
		}
	};

	MenuItem _useTemplate = new MenuItem(_resources, MoneyResource.USETEMPLATE, 130, 130) {
		public void run() {
			GetTemplateScreen getTemplateScreen = new GetTemplateScreen();
			if (getTemplateScreen.pickTemplate()) {
				SavedTransaction savedTransaction = getTemplateScreen.getTemplate();
				TemplatesPersist.getInstance().updateUsed(savedTransaction);
				Transaction transaction = (Transaction) ObjectGroup.expandGroup(SavedTransactionsPersist.getInstance().get(savedTransaction.getTransactionId()));
				transaction.setDate(System.currentTimeMillis());
				EditTransaction editTransaction = new EditTransaction(_account, transaction);
				editTransaction.clearTransacton();
			}

		}
	};

	MenuItem _goto = new MenuItem(_resources, MoneyResource.GOTODATE, 140, 140) {
		public void run() {
			GetDateScreen dateScreen = new GetDateScreen(_resources.getString(MoneyResource.GOTODATE));
			if (dateScreen.pickDate()) {
				long date = dateScreen.getDate();
				gotoDate(date);
			}
		}
	};

	MenuItem _viewBalance = new MenuItem(_resources, MoneyResource.VIEWBALANCE, 150, 150) {
		public void run() {
			_view = 0;
			_mainScreen.setStatus(_balanceField);
		}
	};

	MenuItem _viewCleared = new MenuItem(_resources, MoneyResource.VIEWCLEARED, 160, 160) {
		public void run() {
			_view = 1;
			_mainScreen.setStatus(_clearedBalanceField);
		}
	};

	MenuItem _viewReconciled = new MenuItem(_resources, MoneyResource.VIEWRECONCILED, 170, 170) {
		public void run() {
			_view = 2;
			_mainScreen.setStatus(_reconciledBalanceField);
		}
	};

	MenuItem _viewCurrent = new MenuItem(_resources, MoneyResource.VIEWCURRENT, 180, 180) {
		public void run() {
			_view = 3;
			_mainScreen.setStatus(_currentBalanceField);
		}
	};

	MenuItem _reconcile = new MenuItem(_resources, MoneyResource.RECONCILETITLE, 190, 190) {
		public void run() {
			GetDateScreen dateScreen = new GetDateScreen(_resources.getString(MoneyResource.RECONCILETITLE));
			if (dateScreen.pickDate()) {
				long date = dateScreen.getDate();
				new ReconcileScreen(_account, date + DateTimeUtilities.ONEDAY);
			}
		}
	};

	public void gotoDate(long date) {
		Transaction[] transactions = TransactionPersist.getInstance().get(_account);
		int index = transactions.length - 1;
		while (0 <= index) {
			if (date < transactions[index].getDate())
				break;
			index--;
		}
		_transactionsObjectField.setSelectedIndex(index);
		valitateCurrent();
	}

	private void valitateCurrent() {
		if (_transactions == null)
			return;
		int currentIndex = _transactionsObjectField.getSelectedIndex();
		if (currentIndex < 0 || _index == currentIndex)
			return;
		if (currentIndex < _index) {
			for (int i = _index - 1; currentIndex <= i; i--) {
				_currentBalance += _transactions[i].getAmount();
			}
		} else {
			for (int i = _index; i < currentIndex; i++) {
				_currentBalance -= _transactions[i].getAmount();
			}
		}
		_index = currentIndex;
		_currentBalanceField.setText(Util.toString(_currentBalance, _currency, false));
	}

	private FocusChangeListener _focusChangeListener = new FocusChangeListener() {
		public void focusChanged(net.rim.device.api.ui.Field field, int eventType) {
			valitateCurrent();
		};
	};

	private ObjectListField _transactionsObjectField = new ObjectListField() {

		public void drawListRow(ListField listField, Graphics graphics, int index, int y, int width) {
			Transaction transaction = (Transaction) _transactionsObjectField.get(_transactionsObjectField, index);
			String text;
			int _fontHeight = Font.getDefault().getHeight();
			int _fontHeight2 = (_fontHeight * 2) - 1;
			int oldColor = graphics.getColor();
			boolean changeColor = oldColor == Color.BLACK;
			long amount = transaction.getAmount();
			if (transaction instanceof ExpenseExchangeRate) {
				ExpenseExchangeRate exchangeRate = (ExpenseExchangeRate) transaction;
				amount = exchangeRate.getDestAmount();
			} else if (transaction instanceof IncomeExchangeRate) {
				IncomeExchangeRate exchangeRate = (IncomeExchangeRate) transaction;
				amount = exchangeRate.getDestAmount();
			}

			int w2 = width / 2;

			text = "";
			switch (transaction.getStatus()) {
			case 0:
				break;
			case 1:
				text = "*";
				break;
			case 2:
				text = "\u2713";
				break;
			}
			graphics.drawText(text, 0, y, 0, _fontHeight);

			text = transaction.getDescription();
			graphics.drawText(text, _fontHeight, y, 0, width - _fontHeight);

			if (changeColor)
				graphics.setColor(Color.GRAY);
			text = _sdFormat.formatLocal(transaction.getDate());
			graphics.drawText(text, 0, y + _fontHeight, 0, w2);
			graphics.setColor(oldColor);
			text = Util.toString(amount, _currency, false);
			graphics.drawText(text, w2, y + _fontHeight, Graphics.RIGHT, w2);
			if (changeColor) {
				graphics.setColor(0xd3d3d3);
				graphics.drawLine(0, y + _fontHeight2, width, y + _fontHeight2);
			}
			graphics.setColor(oldColor);

		}
	};

	public void update() {
		long finalBalance = 0;
		long clearedBalance = 0;
		long reconciledBalance = 0;
		_index = 0;

		_transactions = TransactionPersist.getInstance().get(_account);
		Account account = AccountPersist.getInstance().get(_account.getUID());

		finalBalance = account.getFinalBalance();
		clearedBalance = account.getClearedBalance();
		reconciledBalance = account.getReconciledBalance();
		_currentBalance = finalBalance;

		_transactionsObjectField.set(_transactions);
		_transactionsObjectField.setSelectedIndex(0);
		_balanceField.setText(Util.toString(finalBalance, _currency, false));
		_clearedBalanceField.setText(Util.toString(clearedBalance, _currency, false));
		_reconciledBalanceField.setText(Util.toString(reconciledBalance, _currency, false));
		_currentBalanceField.setText(Util.toString(_currentBalance, _currency, false));
	}
}
