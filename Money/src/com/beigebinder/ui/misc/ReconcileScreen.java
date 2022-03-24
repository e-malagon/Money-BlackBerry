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
package com.beigebinder.ui.misc;

import java.io.IOException;
import java.util.Vector;

import com.beigebinder.data.Account;
import com.beigebinder.data.Currency;
import com.beigebinder.data.Transaction;
import com.beigebinder.misc.Util;
import com.beigebinder.persist.CurrencyPersist;
import com.beigebinder.persist.MiscellaneousPersist;
import com.beigebinder.persist.TransactionPersist;
import com.beigebinder.resource.MoneyResource;
import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
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
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class ReconcileScreen {
	private Account _account;
	private EditField _conciliedField;
	private EditField _finalField;
	private EditField _diferenceField;
	private long _finalBalance;
	private long _conciliedBalance;
	private Currency _currency;
	private DateFormat _sdFormat;
	ReconcilieTransaction[] _rt;
	private boolean _cons;
	private static ResourceBundle _resources = ResourceBundle.getBundle(
			MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public ReconcileScreen(Account account, long date) {
		this._account = account;
		_cons = false;
		_currency = CurrencyPersist.getInstance().get(account.getCurrencyID());
		int height = Font.getDefault().getHeight();
		height *= 2;
		_transactions.setRowHeight(height);
		_sdFormat = SimpleDateFormat.getInstance(SimpleDateFormat.DATE_SHORT);

		Transaction[] transactions = TransactionPersist.getInstance().get(
				_account);
		int size = transactions.length;
		Vector vector = new Vector();
		ReconcilieTransaction obj;
		int status;
		_conciliedBalance = account.getInitialBalance();
		_finalBalance = account.getInitialBalance();
		long amount;
		for (int index = size - 1; 0 <= index; index--) {
			status = transactions[index].getStatus();
			amount = transactions[index].getAmount();

			if (transactions[index].getDate() < date)
				_finalBalance += amount;

			if (status != 2) {
				obj = new ReconcilieTransaction(transactions[index],
						status == 1);
				vector.addElement(obj);
				if (status == 1) {
					_conciliedBalance += amount;
				}
			} else {
				_conciliedBalance += amount;
			}
		}
		_rt = new ReconcilieTransaction[vector.size()];
		vector.copyInto(_rt);
		_transactions.set(_rt);
		_mainScreen.add(_transactions);

		String sign = _currency.getSign() + " ";

		_finalField = new EditField(_resources
				.getString(MoneyResource.BALANCEENDING)
				+ sign, Util.toString(_finalBalance, _currency, true), 15,
				EditField.FILTER_REAL_NUMERIC | EditField.NO_NEWLINE);
		_conciliedField = new EditField(_resources
				.getString(MoneyResource.BALANCERECONCILED)
				+ sign, Util.toString(_conciliedBalance, _currency, true), 250,
				EditField.NON_FOCUSABLE | EditField.FIELD_RIGHT);
		_diferenceField = new EditField(_resources
				.getString(MoneyResource.BALANCEDIFFERENCE)
				+ sign, Util.toString(_finalBalance - _conciliedBalance,
				_currency, true), 250, EditField.NON_FOCUSABLE
				| EditField.FIELD_RIGHT);
		VerticalFieldManager vfm = new VerticalFieldManager();
		vfm.add(new SeparatorField());
		vfm.add(_finalField);
		vfm.add(_conciliedField);
		vfm.add(_diferenceField);
		_mainScreen.setStatus(vfm);
		_mainScreen.setTitle(_resources.getString(MoneyResource.RECONCILETITLE)
				+ " " + account.getDescription());
		_finalField.setFocus();

		FocusChangeListener focusChangeListener = new FocusChangeListener() {
			public void focusChanged(Field field, int eventType) {
				if (field instanceof EditField) {
					EditField editField = (EditField) field;
					String text = editField.getText();
					if (text.length() == 0) {
						text = "0.00";
					}
					try {
						Double.parseDouble(text);
					} catch (NumberFormatException numberFormatException) {
						Dialog.alert(_resources
								.getString(MoneyResource.AMMOUNTINVALID));
						editField.setText("0.00");
						editField.setFocus();
						editField.setDirty(true);
						return;
					}

					if (eventType == FocusChangeListener.FOCUS_LOST) {
						_finalBalance = Util.toLong(text, _currency);
						editField.setText(Util.toString(_finalBalance,
								_currency, true));
						editField.setDirty(true);
						_diferenceField.setText(Util.toString(_finalBalance
								- _conciliedBalance, _currency, true));
					}
				}
			}
		};
		_finalField.setFocusListener(focusChangeListener);
		UiApplication.getUiApplication().pushScreen(_mainScreen);
	}

	private MainScreen _mainScreen = new MainScreen(MainScreen.DEFAULT_MENU
			| MainScreen.DEFAULT_CLOSE) {

		protected void makeMenu(Menu menu, int instance) {
			if (_finalBalance - _conciliedBalance == 0) {
				menu.add(_reconcilie);
			}
			menu.add(_posponer);
			menu.add(MenuItem.separator(111));
			super.makeMenu(menu, instance);
		}

		public void save() throws IOException {
			TransactionPersist tLogic = TransactionPersist.getInstance();
			int size = _rt.length;
			byte value = _cons ? (byte) 2 : (byte) 1;

			for (int index = 0; index < size; index++) {
				if (_rt[index].isChequed()) {
					tLogic.updateStatus(_account, _rt[index].getTransaction(),
							value);
				} else {
					tLogic.updateStatus(_account, _rt[index].getTransaction(),
							(byte) 0);
				}
			}
			tLogic.UpdateAccountBalance(_account.getUID());
		}

		public void close() {
			super.close();
			MiscellaneousPersist.getInstance().update();
		}
	};

	MenuItem _reconcilie = new MenuItem(_resources, MoneyResource.RECONCILETITLE, 100, 100) {
		public void run() {
			if (Dialog.ask(Dialog.D_OK_CANCEL, _resources
					.getString(MoneyResource.ASKFORRECONCILIE), Dialog.CANCEL) == Dialog.OK) {
				_cons = true;
				try {
					_mainScreen.save();
				} catch (Exception IOException) {
					Dialog.inform(IOException.toString());
				}
				_mainScreen.close();
			}
		}
	};

	MenuItem _posponer = new MenuItem(_resources, MoneyResource.POSTPONE, 110, 110) {
		public void run() {
			if (Dialog.ask(Dialog.D_OK_CANCEL, _resources
					.getString(MoneyResource.ASKFORPOSPONER), Dialog.CANCEL) == Dialog.OK) {
				ReconcilieTransaction rt;
				TransactionPersist tLogic = TransactionPersist.getInstance();
				int size = _rt.length;
				for (int index = 0; index < size; index++) {
					rt = (ReconcilieTransaction) _rt[index];
					tLogic.updateStatus(_account, rt.getTransaction(), rt
							.isChequed() ? (byte) 1 : (byte) 0);
				}
				tLogic.UpdateAccountBalance(_account.getUID());
				_mainScreen.close();
			}
		}
	};

	private static final class ReconcilieTransaction {
		private boolean _chequed;
		private Transaction _transaction;

		public ReconcilieTransaction(Transaction transaction, boolean chequed) {
			this._transaction = transaction;
			this._chequed = chequed;
		}

		public void toggleChecked() {
			this._chequed = !this._chequed;
		}

		public boolean isChequed() {
			return _chequed;
		}

		public Transaction getTransaction() {
			return _transaction;
		}
	}

	private ObjectListField _transactions = new ObjectListField() {
		public void drawListRow(ListField listField, Graphics graphics,
				int index, int y, int width) {
			ReconcilieTransaction rt = (ReconcilieTransaction) _transactions
					.get(_transactions, index);
			Transaction transaction = rt.getTransaction();
			String text;
			int _fontHeight = Font.getDefault().getHeight();
			int _fontHeight2 = (_fontHeight * 2) - 1;
			int oldColor = graphics.getColor();
			boolean changeColor = oldColor == Color.BLACK;
			long amount = transaction.getAmount();
			int w2 = width / 2;
			if (rt.isChequed()) {
				text = String.valueOf(Characters.BALLOT_BOX_WITH_CHECK);
			} else {
				text = String.valueOf(Characters.BALLOT_BOX);
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

		protected boolean navigationUnclick(int status, int time) {
			int index = getSelectedIndex();
			ReconcilieTransaction rt = (ReconcilieTransaction) _transactions
					.get(_transactions, index);
			rt.toggleChecked();
			if (rt.isChequed())
				_conciliedBalance += rt.getTransaction().getAmount();
			else
				_conciliedBalance -= rt.getTransaction().getAmount();
			_conciliedField.setText(Util.toString(_conciliedBalance, _currency,
					true));
			_diferenceField.setText(Util.toString(_finalBalance
					- _conciliedBalance, _currency, true));
			_transactions.invalidate(index);
			return true;
		}
	};
}
