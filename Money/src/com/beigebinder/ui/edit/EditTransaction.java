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
//#preprocess
package com.beigebinder.ui.edit;

import java.io.IOException;

import com.beigebinder.common.GetTextScreen;
import com.beigebinder.data.Account;
import com.beigebinder.data.Category;
import com.beigebinder.data.Currency;
import com.beigebinder.data.ExpenseExchangeRate;
import com.beigebinder.data.IncomeExchangeRate;
import com.beigebinder.data.Transaction;
import com.beigebinder.data.Transfer;
import com.beigebinder.misc.Util;
import com.beigebinder.persist.AccountPersist;
import com.beigebinder.persist.CategoryPersist;
import com.beigebinder.persist.CurrencyPersist;
import com.beigebinder.persist.MiscellaneousPersist;
import com.beigebinder.persist.TransactionPersist;
import com.beigebinder.resource.MoneyResource;
import com.beigebinder.ui.misc.GetCurrencyScreen;
import com.beigebinder.ui.misc.GetExchangeRateScreen;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.synchronization.UIDGenerator;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;
//#ifdef stringprovider
import net.rim.device.api.util.StringProvider;
//#endif

public class EditTransaction {
	private Transaction _transaction;
	private Account _account;
	private EditTransactionField _editField;
	private static ResourceBundle _resources = ResourceBundle.getBundle(MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public EditTransaction(Account account, Transaction transaction) {
		this(account);
		_transaction = transaction;
		_editField.setTransaction(_transaction);
		if (_editField.isSplited()) {
			//#ifdef stringprovider
			_setSplits.setText(new StringProvider(_resources.getString(MoneyResource.UNSETSPLITS)));
			//#else
			_setSplits.setText(_resources.getString(MoneyResource.UNSETSPLITS));
			//#endif
		} else {
			//#ifdef stringprovider
			_setSplits.setText(new StringProvider(_resources.getString(MoneyResource.SETSPLITS)));
			//#else
			_setSplits.setText(_resources.getString(MoneyResource.SETSPLITS));
			//#endif
		}
		_mainScreen.setTitle(_resources.getString(MoneyResource.EDITTRANSACTION));
	}

	public EditTransaction(Account account, Transaction transaction, boolean dirty) {
		this(account, transaction);
		_editField.setDirty(dirty);
	}

	public EditTransaction(Account account) {
		this();
		_account = account;
		_editField.setAccount(_account);
	}

	public EditTransaction() {
		_account = null;
		_transaction = null;
		_editField = new EditTransactionField();
		_mainScreen.add(_editField.getFieldManager());
		_mainScreen.setTitle(_resources.getString(MoneyResource.NEWTRANSACTION));
		_editField.suscribe();
		UiApplication.getUiApplication().pushScreen(_mainScreen);
	}

	public void clearTransacton() {
		_transaction = null;
		_editField.setDirty(true);
	}

	private MainScreen _mainScreen = new MainScreen(MainScreen.DEFAULT_MENU | MainScreen.DEFAULT_CLOSE) {
		protected void makeMenu(Menu menu, int instance) {
			menu.add(_ok);
			menu.add(MenuItem.separator(101));
			if (_editField.isSplitsFocus()) {
				menu.add(_setSplits);
				menu.add(_addSplits);
				menu.add(_editSplits);
				menu.add(_removeSplits);
				menu.add(MenuItem.separator(141));
				menu.add(_changeCurrency);
				menu.add(_addAccount);
				menu.add(_addCategory);
				menu.add(MenuItem.separator(171));
				menu.setDefault(_addSplits);
			} else {
				if (_editField.getType() != 2) {
					menu.add(_setSplits);
					if (_editField.isSplited())
						menu.add(_addSplits);
					menu.add(MenuItem.separator(121));
					menu.add(_changeCurrency);
					menu.add(_addAccount);
					menu.add(_addCategory);
					menu.add(MenuItem.separator(171));
				} else {
					menu.add(_addAccount);
					menu.add(MenuItem.separator(161));
				}
			}
			if (_editField.isWithMemo()) {
				menu.add(_editMemo);
				menu.add(_deleteMemo);
			} else {
				menu.add(_addMemo);
			}
			super.makeMenu(menu, instance);
		}

		public boolean isDirty() {
			_editField.changeFocus();
			return super.isDirty();
		}

		public boolean isDataValid() {
			return _editField.isDataValid() && super.isDataValid();
		}

		public void save() throws IOException {
			TransactionPersist transactionPersist = TransactionPersist.getInstance();
			CurrencyPersist currencyPersist = CurrencyPersist.getInstance();
			long amount = _editField.getAmount();
			String exchangeRate;
			Account newAccount = _editField.getNewAccount();

			if (_editField.needExchangeRate()) {
				if (amount != 0) {
					if (_transaction instanceof ExpenseExchangeRate) {
						ExpenseExchangeRate expenseExchangeRate = (ExpenseExchangeRate) _transaction;
						exchangeRate = Util.amountToExchange(expenseExchangeRate.getDestAmount(), expenseExchangeRate.getAmount());
					} else if (_transaction instanceof IncomeExchangeRate) {
						IncomeExchangeRate incomeExchangeRate = (IncomeExchangeRate) _transaction;
						exchangeRate = Util.amountToExchange(incomeExchangeRate.getDestAmount(), incomeExchangeRate.getAmount());
					} else {
						exchangeRate = "1.0";
					}
					GetExchangeRateScreen exchangeRateScreen = new GetExchangeRateScreen(_editField.getCurreny(), currencyPersist.get(newAccount.getCurrencyID()), amount, exchangeRate);
					if (exchangeRateScreen.pickExchangeRate()) {
						amount = exchangeRateScreen.getToAmount();
						_editField.setDestAmount(amount);
					} else {
						return;
					}
				} else {
					_editField.setDestAmount(0);
				}

			}

			Transaction transaction = _editField.getTransaction();

			if (transaction instanceof Transfer) {
				Transfer transfer = (Transfer) transaction;
				Account mirrorAccount = AccountPersist.getInstance().get(transfer.getMirrorAccount());
				amount = transaction.getAmount() * -1;

				if (newAccount.getCurrencyID() != mirrorAccount.getCurrencyID()) {
					if (_transaction instanceof Transfer) {
						exchangeRate = Util.amountToExchange(transactionPersist.getMirrorTransaction(_transaction).getAmount(), _transaction.getAmount());
					} else {
						exchangeRate = "1.0";
					}

					GetExchangeRateScreen exchangeRateScreen = new GetExchangeRateScreen(currencyPersist.get(newAccount.getCurrencyID()), currencyPersist.get(mirrorAccount.getCurrencyID()), amount, exchangeRate);
					if (exchangeRateScreen.pickExchangeRate()) {
						amount = exchangeRateScreen.getToAmount();
					} else {
						return;
					}
				}
			}

			if (TransactionPersist.getInstance().exist(_account, _transaction)) {
				transaction.setId(_transaction.getUID());
				transactionPersist.update(_account, _transaction, newAccount, transaction, amount);
			} else {
				transactionPersist.add(newAccount, transaction, amount);
			}
			MiscellaneousPersist.getInstance().update();
			super.save();
		}

		public void close() {
			_editField.unSuscribe();
			super.close();
		}
	};

	private MenuItem _ok = new MenuItem(_resources, MoneyResource.SAVE, 100, 100) {
		public void run() {
			if (_transaction instanceof Transfer) {
				Transfer transfer = (Transfer) _transaction;
				Account account = AccountPersist.getInstance().get(transfer.getMirrorAccount());
				if (_account.getCurrencyID() != account.getCurrencyID())
					_editField.setDirty(true);
			}
			if (_mainScreen.isDirty()) {
				if (_mainScreen.isDataValid()) {
					try {
						_mainScreen.save();
					} catch (Exception IOException) {
						Dialog.inform(IOException.toString());
					}
					_mainScreen.close();
				}
			} else {
				_mainScreen.close();
			}
		}
	};

	private MenuItem _setSplits = new MenuItem(_resources, MoneyResource.SETSPLITS, 110, 110) {
		public void run() {
			if (_editField.switchSplits()) {
				//#ifdef stringprovider
				_setSplits.setText(new StringProvider(_resources.getString(MoneyResource.UNSETSPLITS)));
				//#else
				_setSplits.setText(_resources.getString(MoneyResource.UNSETSPLITS));
				//#endif
			} else {
				//#ifdef stringprovider
				_setSplits.setText(new StringProvider(_resources.getString(MoneyResource.SETSPLITS)));
				//#else
				_setSplits.setText(_resources.getString(MoneyResource.SETSPLITS));
				//#endif
			}
		}
	};

	private MenuItem _addSplits = new MenuItem(_resources, MoneyResource.ADDSPLIT, 120, 120) {
		public void run() {
			_editField.addSplit();
		}
	};

	private MenuItem _editSplits = new MenuItem(_resources, MoneyResource.EDITSPLIT, 130, 130) {
		public void run() {
			_editField.editFocusedSplit();
		}
	};

	private MenuItem _removeSplits = new MenuItem(_resources, MoneyResource.REMOVESPLITS, 140, 140) {
		public void run() {
			if (Dialog.ask(Dialog.D_DELETE, _resources.getString(MoneyResource.DELETESPLIT), Dialog.CANCEL) == Dialog.DELETE) {
				_editField.removeFocusedSplit();
			}
		}
	};

	private MenuItem _changeCurrency = new MenuItem(_resources, MoneyResource.CHANGECURRENCY, 150, 150) {
		public void run() {
			GetCurrencyScreen selectCurrencyScreen = new GetCurrencyScreen(_editField.getCurreny());
			if (selectCurrencyScreen.pickCurrency()) {
				Currency currency = selectCurrencyScreen.getCurrency();
				_editField.setCurrency(currency);
				_editField.setDirty(true);
			}
		}
	};

	private MenuItem _addAccount = new MenuItem(_resources, MoneyResource.NEWACCOUNT, 160, 160) {
		public void run() {
			EditAccount editAccount = new EditAccount();
			if (editAccount.pickAccount()) {
				AccountPersist.getInstance().add(editAccount.getAccount());
				MiscellaneousPersist.getInstance().update();
			}
		}
	};

	private MenuItem _addCategory = new MenuItem(_resources, MoneyResource.NEWCATEGORY, 170, 170) {
		public void run() {
			EditCategory categoryPopupScreen = new EditCategory(_resources.getString(MoneyResource.NEWCATEGORY));
			if (categoryPopupScreen.pickCategory()) {
				Category newCategory = categoryPopupScreen.getCategory();
				newCategory.setId(UIDGenerator.getUID());
				CategoryPersist.getInstance().add(newCategory);
				MiscellaneousPersist.getInstance().update();
			}
		}
	};

	private MenuItem _addMemo = new MenuItem(_resources, MoneyResource.ADDMEMO, 180, 180) {
		public void run() {
			GetTextScreen textPopupScreen = new GetTextScreen(_resources.getString(MoneyResource.ADDMEMO), _editField.getMemo(), 0);
			if (textPopupScreen.pickText()) {
				String text = textPopupScreen.getText();
				_editField.addMemo();
				_editField.setMemo(text);
				_editField.setDirty(true);
			}
		}
	};

	private MenuItem _deleteMemo = new MenuItem(_resources, MoneyResource.REMOVEMEMO, 190, 190) {
		public void run() {
			if (Dialog.ask(Dialog.D_DELETE, _resources.getString(MoneyResource.ASKFORDELETEMEMO), Dialog.CANCEL) == Dialog.DELETE) {
				_editField.deleteMemo();
				_editField.setDirty(true);
			}
		}
	};

	private MenuItem _editMemo = new MenuItem(_resources, MoneyResource.EDITMEMO, 200, 200) {
		public void run() {
			GetTextScreen textPopupScreen = new GetTextScreen(_resources.getString(MoneyResource.EDITMEMO), _editField.getMemo(), 0);
			if (textPopupScreen.pickText()) {
				String text = textPopupScreen.getText();
				_editField.addMemo();
				_editField.setMemo(text);
				_editField.setDirty(true);
			}
		}
	};
}
