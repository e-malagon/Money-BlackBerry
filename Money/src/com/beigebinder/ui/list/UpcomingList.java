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

import com.beigebinder.data.Account;
import com.beigebinder.data.Currency;
import com.beigebinder.data.SavedTransaction;
import com.beigebinder.data.Transaction;
import com.beigebinder.data.Transfer;
import com.beigebinder.misc.UpdateCallback;
import com.beigebinder.misc.Util;
import com.beigebinder.persist.AccountPersist;
import com.beigebinder.persist.CurrencyPersist;
import com.beigebinder.persist.MiscellaneousPersist;
import com.beigebinder.persist.PendingsPersist;
import com.beigebinder.persist.SavedTransactionsPersist;
import com.beigebinder.persist.TransactionPersist;
import com.beigebinder.resource.MoneyResource;
import com.beigebinder.ui.edit.EditUpcoming;
import com.beigebinder.ui.misc.GetExchangeRateScreen;
import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.container.MainScreen;

public final class UpcomingList implements UpdateCallback {
	private UpcomingList _miscList;
	private ObjectListField _itemsList;
	private static ResourceBundle _resources = ResourceBundle.getBundle(MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public UpcomingList() {
		int height;
		this._miscList = this;
		_itemsList = new ObjectListField() {
			private DateFormat _sdFormat = SimpleDateFormat.getInstance(SimpleDateFormat.DATE_SHORT);
			private CurrencyPersist _currencyLogic = CurrencyPersist.getInstance();
			private AccountPersist _accountLogic = AccountPersist.getInstance();
			private SavedTransactionsPersist _savedTransactionsPersist = SavedTransactionsPersist.getInstance();

			public void drawListRow(ListField listField, Graphics graphics, int index, int y, int width) {
				SavedTransaction transaction = (SavedTransaction) this.get(listField, index);
				int _fontHeight = Font.getDefault().getHeight();
				int _fontHeight2 = (_fontHeight * 2) - 1;
				String text;
				int oldColor = graphics.getColor();
				boolean changeColor = oldColor == Color.BLACK;
				int w2 = width / 2;
				int transactionId = transaction.getTransactionId();
				Transaction transaction2 = _savedTransactionsPersist.get(transactionId);
				Account account = _accountLogic.get(transaction.getAccount());
				Currency currency = _currencyLogic.get(account.getCurrencyID());

				text = transaction.getDescription();
				graphics.drawText(text, 0, y, 0, width);

				if (changeColor)
					graphics.setColor(Color.GRAY);
				text = _sdFormat.formatLocal(transaction2.getDate());
				graphics.drawText(text, 0, y + _fontHeight, 0, w2);
				graphics.setColor(oldColor);
				text = Util.toString(Math.abs(transaction2.getAmount()), currency, false);
				graphics.drawText(text, w2, y + _fontHeight, Graphics.RIGHT, w2);

				if (changeColor) {
					graphics.setColor(0xd3d3d3);
					graphics.drawLine(0, y + _fontHeight2, width, y + _fontHeight2);
				}
				graphics.setColor(oldColor);
			}
		};
		_mainScreen.setTitle(_resources.getString(MoneyResource.EXECUTEDTITLE));
		height = Font.getDefault().getHeight();
		height *= 2;
		_itemsList.setRowHeight(height);

		this.update();
		_mainScreen.add(_itemsList);
		MiscellaneousPersist.getInstance().suscribe(_miscList);
		UiApplication.getUiApplication().pushScreen(_mainScreen);
	}

	private MainScreen _mainScreen = new MainScreen(MainScreen.DEFAULT_MENU | MainScreen.DEFAULT_CLOSE) {
		protected void makeMenu(Menu menu, int instance) {
			if (UiApplication.getUiApplication().getActiveScreen().getFieldWithFocus().equals(_itemsList)) {
				int selectedIndex = _itemsList.getSelectedIndex();

				if (selectedIndex != -1) {
					menu.add(_applyItem);
					menu.add(_editItem);
					menu.add(_applyCancel);
				}
			}
			menu.add(MenuItem.separator(121));
			super.makeMenu(menu, instance);
		}

		public void close() {
			MiscellaneousPersist.getInstance().unSuscribe(_miscList);
			super.close();
		}

	};

	private MenuItem _editItem = new MenuItem(_resources, MoneyResource.EDIT, 100, 100) {
		public void run() {
			int selectedIndex = _itemsList.getSelectedIndex();
			SavedTransaction object = (SavedTransaction) _itemsList.get(_itemsList, selectedIndex);

			new EditUpcoming(object);
		}
	};

	private MenuItem _applyItem = new MenuItem(_resources, MoneyResource.APPLY, 110, 110) {
		public void run() {
			if (Dialog.ask(Dialog.D_YES_NO, _resources.getString(MoneyResource.ASKFORAPPLY), Dialog.NO) == Dialog.YES) {
				AccountPersist aLogic = AccountPersist.getInstance();
				int selectedIndex = _itemsList.getSelectedIndex();
				Object obj = _itemsList.get(_itemsList, selectedIndex);
				CurrencyPersist currencyPersist = CurrencyPersist.getInstance();
				SavedTransaction executedTransaction = (SavedTransaction) obj;
				Transaction transaction = SavedTransactionsPersist.getInstance().get(executedTransaction.getTransactionId());
				Account account = aLogic.get(executedTransaction.getAccount());
				long amount = transaction.getAmount();
				if (transaction instanceof Transfer) {
					Transfer transfer = (Transfer) transaction;
					Account mirrorAccount = AccountPersist.getInstance().get(transfer.getMirrorAccount());

					if (account.getCurrencyID() != mirrorAccount.getCurrencyID()) {
						GetExchangeRateScreen exchangeRateScreen = new GetExchangeRateScreen(currencyPersist.get(account.getCurrencyID()), currencyPersist.get(mirrorAccount.getCurrencyID()), amount, "1.0");
						if (exchangeRateScreen.pickExchangeRate()) {
							amount = exchangeRateScreen.getToAmount() * ((amount / Math.abs(amount)) * -1);
						} else {
							return;
						}
					}
				}

				TransactionPersist.getInstance().add(account, transaction, amount);
				SavedTransactionsPersist.getInstance().remove(transaction);
				PendingsPersist.getInstance().remove(executedTransaction);
				MiscellaneousPersist.getInstance().update();
			}
		}
	};

	private MenuItem _applyCancel = new MenuItem(_resources, MoneyResource.CANCEL, 120, 120) {
		public void run() {
			if (Dialog.ask(Dialog.D_YES_NO, _resources.getString(MoneyResource.ASKFORCANCEL), Dialog.NO) == Dialog.YES) {
				SavedTransactionsPersist logic = SavedTransactionsPersist.getInstance();
				int selectedIndex = _itemsList.getSelectedIndex();
				Object obj = _itemsList.get(_itemsList, selectedIndex);
				SavedTransaction executedTransaction = (SavedTransaction) obj;
				Transaction transaction = logic.get(executedTransaction.getTransactionId());
				logic.remove(transaction);
				PendingsPersist.getInstance().remove(executedTransaction);
				update();
			}
		}
	};

	public void update() {
		Object[] elements;
		elements = PendingsPersist.getInstance().get();
		Util.setIcons(false);
		_itemsList.set(elements);
	}
}
