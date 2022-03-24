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
import com.beigebinder.data.Category;
import com.beigebinder.data.Currency;
import com.beigebinder.data.Expense;
import com.beigebinder.data.ExpenseExchangeRate;
import com.beigebinder.data.ExpenseExchangeRateMemo;
import com.beigebinder.data.ExpenseMemo;
import com.beigebinder.data.Income;
import com.beigebinder.data.IncomeExchangeRate;
import com.beigebinder.data.IncomeExchangeRateMemo;
import com.beigebinder.data.IncomeMemo;
import com.beigebinder.data.Split;
import com.beigebinder.data.Transaction;
import com.beigebinder.data.Transfer;
import com.beigebinder.data.TransferMemo;
import com.beigebinder.misc.UpdateCallback;
import com.beigebinder.misc.Util;
import com.beigebinder.persist.AccountPersist;
import com.beigebinder.persist.CategoryPersist;
import com.beigebinder.persist.CurrencyPersist;
import com.beigebinder.persist.MiscellaneousPersist;
import com.beigebinder.resource.MoneyResource;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.synchronization.UIDGenerator;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.FocusChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.DateField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.util.Arrays;

public class EditTransactionField implements UpdateCallback, MoneyResource {
	private Account _account;
	private Currency _currency;
	private VerticalFieldManager _allFieldManager;
	private long _balance;

	private ObjectChoiceField _type;
	private ObjectChoiceField _accounts;
	private EditField _number;
	private EditField _description;
	private DateField _date;
	private ObjectChoiceField _status;
	private ObjectChoiceField _categories;
	private ObjectChoiceField _accountsDest;
	private EditField _amount;
	private SeparatorField _separator;
	private String _memo;
	private boolean _splited;
	private boolean _splitsFocus;
	private boolean _whitMemo;
	private long _destAmount;
	private int _lastUsedCategory;
	private int _userControl;
	private Split[] _splits;
	private CategoryPersist _categoryLogic;
	private static ResourceBundle _resources = ResourceBundle.getBundle(
			MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public EditTransactionField() {
		_categoryLogic = CategoryPersist.getInstance();
		_memo = "";
		_splited = false;
		_splitsFocus = false;
		_balance = 0;
		_destAmount = 0;
		_lastUsedCategory = -1;
		String[] types = _resources.getStringArray(OPERATIONSTYPES);
		_type = new ObjectChoiceField(_resources.getString(TYPE), types);
		FieldChangeListener changeListener = new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (context != FieldChangeListener.PROGRAMMATIC)
					setSplits();
			}
		};
		_type.setChangeListener(changeListener);
		Account[] accounts = AccountPersist.getInstance().get();
		_accounts = new ObjectChoiceField(_resources.getString(ACCOUNT),
				accounts);
		FieldChangeListener listener = new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				int sIndex = _accounts.getSelectedIndex();
				if (sIndex != -1) {
					Account account = (Account) _accounts.getChoice(sIndex);
					_currency = CurrencyPersist.getInstance().get(
							account.getCurrencyID());
					_splitsList.invalidate();
				} else {
					_currency = CurrencyPersist.getInstance()
							.getDefaulCurrency();
				}
				String sign = _currency.getSign() + " ";
				_amount.setLabel(sign);
			}
		};
		_accounts.setChangeListener(listener);
		int sIndex = _accounts.getSelectedIndex();
		if (sIndex != -1) {
			_account = (Account) _accounts.getChoice(sIndex);
			_currency = CurrencyPersist.getInstance().get(
					_account.getCurrencyID());
		} else {
			_currency = CurrencyPersist.getInstance().getDefaulCurrency();
		}

		_number = new EditField(_resources.getString(TRANSACTIONNUMBER), "",
				15, EditField.NO_NEWLINE);
		_description = new EditField(_resources.getString(PAYEE), "", 30,
				EditField.NO_NEWLINE);
		_date = new DateField(_resources.getString(DATE), System
				.currentTimeMillis(), DateField.DATE);

		String[] statusStrings = _resources.getStringArray(STATUSSTRINGS);
		_status = new ObjectChoiceField(_resources.getString(STATUS),
				statusStrings);

		Category[] categories = CategoryPersist.getInstance().get();
		_categories = new ObjectChoiceField(_resources.getString(INTO),
				categories);
		_accountsDest = new ObjectChoiceField(_resources.getString(TO),
				accounts);
		String sign = _currency.getSign();
		_amount = new EditField(sign + " ", "", 15,
				EditField.FILTER_REAL_NUMERIC | EditField.NO_NEWLINE);
		_amount.setFocusListener(_focusChangeListener);
		_separator = new SeparatorField();

		_splits = new Split[0];
		_splitsList.set(_splits);
		_splitsList.setRowHeight(Font.getDefault().getHeight() * 2);
		_splitsList.setFocusListener(_focusChangeListener);

		_userControl = 0;
		_allFieldManager = new VerticalFieldManager();
		_allFieldManager.add(_type);
		_allFieldManager.add(_accounts);
		_allFieldManager.add(_number);
		_allFieldManager.add(_description);
		_allFieldManager.add(_date);
		_allFieldManager.add(_status);
		_allFieldManager.add(_categories);
		_allFieldManager.add(_amount);
	}

	public EditTransactionField(Account account) {
		this();
		this._account = account;
		_accounts.setSelectedIndex(_account);
	}

	public VerticalFieldManager getFieldManager() {
		return _allFieldManager;
	}

	public boolean isDataValid() {
		Account account;
		int size;

		size = _accounts.getSize();
		if (size == 0) {
			Dialog.alert(_resources.getString(ADDACOUNT));
			_accounts.setFocus();
			return false;
		}
		account = (Account) _accounts.getChoice(_accounts.getSelectedIndex());
		if (account.isClosed()) {
			Dialog.alert(_resources.getString(CLOSEDACCOUNTALERT));
			_accounts.setFocus();
			return false;
		}
		
		size = _accountsDest.getSize();
		if (size == 0) {
			Dialog.alert(_resources.getString(ADDACOUNT));
			_accounts.setFocus();
			return false;
		}
		
		size = _categories.getSize();
		if (size == 0) {
			Dialog.alert(_resources.getString(ADDCATEGORY));
			_categories.setFocus();
			return false;
		}

		if (_type.getSelectedIndex() == 2) {
			Account account2 = (Account) _accountsDest.getChoice(_accountsDest
					.getSelectedIndex());
			if (account.equals(account2)) {
				Dialog.alert(_resources.getString(SAMEACCOUNT));
				_accountsDest.setFocus();
				return false;
			}

			if (account2.isClosed()) {
				Dialog.alert(_resources.getString(CLOSEDACCOUNTALERT));
				_accountsDest.setFocus();
				return false;
			}
		}
		if (_description.getText().length() == 0) {
			if (Dialog.ask(Dialog.D_YES_NO, _resources
					.getString(DATAPAYEEEMPTY)) == Dialog.NO) {
				_description.setFocus();
				return false;
			}
		}
		String amount = _amount.getText();
		if (amount.length() > 0) {
			try {
				Double.parseDouble(amount);
			} catch (NumberFormatException numberFormatException) {
				Dialog.alert(_resources.getString(AMMOUNTINVALID));
				_amount.setFocus();
				return false;
			}
		}
		return true;
	}

	public void hideDate() {
		_allFieldManager.delete(_date);
	}

	public void suscribe() {
		MiscellaneousPersist.getInstance().suscribe(this);
	}

	public void unSuscribe() {
		MiscellaneousPersist.getInstance().unSuscribe(this);
	}

	public boolean isSplitsFocus() {
		return _splitsFocus && _splitsList.getSelectedIndex() != -1;
	}

	public Account getNewAccount() {
		return (Account) _accounts.getChoice(_accounts.getSelectedIndex());
	}

	public void setAccount(Account account) {
		this._account = account;
		this._accounts.setSelectedIndex(_account);
	}

	public void setCurrency(Currency currency) {
		_currency = currency;
		_splitsList.invalidate();
		String sign = _currency.getSign() + " ";
		_amount.setLabel(sign);
	}

	public Currency getCurreny() {
		return _currency;
	}

	public boolean switchSplits() {
		setDirty(true);
		_splited = !_splited;
		setSplits();
		return _splited;
	}

	public int getType() {
		return _type.getSelectedIndex();
	}

	public void changeFocus() {
		_description.setFocus();
	}

	public boolean needExchangeRate() {
		return (_type.getSelectedIndex() != 2)
				&& (_currency.getUID() != getNewAccount().getCurrencyID());
	}

	public void setDestAmount(long amount) {
		_destAmount = amount;
	}

	public long getAmount() {
		String amount = _amount.getText();
		if (amount.length() == 0) {
			amount = "0.00";
		}
		return Util.toLong(amount, _currency);
	}

	public void addSplit() {
		EditSplit editSplitPopupScreen = new EditSplit(_resources
				.getString(MoneyResource.ADDSPLIT));
		editSplitPopupScreen.setCurrency(_currency);
		if (_lastUsedCategory != -1) {
			editSplitPopupScreen.setLastCategoryUsed(_lastUsedCategory, _type
					.getSelectedIndex());
		}

		if (editSplitPopupScreen.pickSplit()) {
			Split split = editSplitPopupScreen.getSplit();
			int index = Arrays.getIndex(_splits, split);
			if (index != -1)
				_splits[index] = split;
			else {
				Arrays.add(_splits, split);
				_lastUsedCategory = split.getCategory();
			}
			_splitsList.set(_splits);

			_balance += split.getAmount();
			_amount.setText(Util.toString(_balance, _currency, true));
			_amount.setDirty(true);
		}
	}

	public void editFocusedSplit() {
		int selectedIndex = _splitsList.getSelectedIndex();
		if (selectedIndex != -1) {
			Split oldSplit = _splits[selectedIndex];

			EditSplit editSplitPopupScreen = new EditSplit(_resources
					.getString(MoneyResource.ADDSPLIT));
			editSplitPopupScreen.setCurrency(_currency);
			editSplitPopupScreen.setSplit(oldSplit);
			if (_lastUsedCategory != -1) {
				editSplitPopupScreen.setLastCategoryUsed(_lastUsedCategory,
						_type.getSelectedIndex());
			}

			if (editSplitPopupScreen.pickSplit()) {
				Split split = editSplitPopupScreen.getSplit();
				int index = Arrays.getIndex(_splits, split);
				if (index != -1)
					_splits[index] = split;
				else {
					Arrays.add(_splits, split);
					_lastUsedCategory = split.getCategory();
				}
				_splitsList.set(_splits);

				_balance -= oldSplit.getAmount();
				_balance += split.getAmount();
				_amount.setText(Util.toString(_balance, _currency, true));
				_amount.setDirty(true);
			}

		}
	}

	public void setDirty(boolean dirty) {
		_description.setDirty(dirty);
	}

	public boolean isSplited() {
		return _splited;
	}

	public boolean isWithMemo() {
		return _whitMemo;
	}

	public String getMemo() {
		return _memo;
	}

	public void deleteMemo() {
		_whitMemo = false;
	}

	public void addMemo() {
		_whitMemo = true;
	}

	public void setMemo(String memo) {
		_memo = memo;
	}

	public void removeFocusedSplit() {
		int selectedIndex = _splitsList.getSelectedIndex();
		if (selectedIndex != -1) {
			_balance -= _splits[selectedIndex].getAmount();
			Arrays.remove(_splits, _splits[selectedIndex]);
			_splitsList.set(_splits);
			_amount.setText(Util.toString(_balance, _currency, true));
			_amount.setDirty(true);
		}
	}

	public void setTransaction(Transaction transaction) {
		AccountPersist accountLogic = AccountPersist.getInstance();
		CategoryPersist categoryPersist = CategoryPersist.getInstance();
		_balance = transaction.getAmount();

		int type;
		if (transaction instanceof Transfer)
			type = 2;
		else if (transaction instanceof Income)
			type = 1;
		else
			type = 0;

		_type.setSelectedIndex(type);
		_number.setText(transaction.getNumber());
		_description.setText(transaction.getDescription());
		_date.setDate(transaction.getDate());
		_status.setSelectedIndex(transaction.getStatus());

		_destAmount = transaction.getAmount();

		switch (type) {
		case 0:
		case 1:
			int[] splitAccounts;
			String[] spliStrings;
			long[] splitAmounts;
			Category category;
			int index;
			int sizeAccounts;

			if (type == 0) {
				Expense expense = (Expense) transaction;
				splitAccounts = expense.getAccounts();
				spliStrings = expense.getDescriptions();
				splitAmounts = expense.getAmounts();
				_balance *= -1;
				if (transaction instanceof ExpenseMemo) {
					ExpenseMemo expenseMemo = (ExpenseMemo) transaction;
					_memo = expenseMemo.getMemo();
					_whitMemo = true;
				} else if (transaction instanceof ExpenseExchangeRateMemo) {
					ExpenseExchangeRateMemo expenseMemo = (ExpenseExchangeRateMemo) transaction;
					_currency = CurrencyPersist.getInstance().get(
							expenseMemo.getCurrencyID());
					_memo = expenseMemo.getMemo();
					_whitMemo = true;
				} else if (transaction instanceof ExpenseExchangeRate) {
					ExpenseExchangeRate expenseMemo = (ExpenseExchangeRate) transaction;
					_currency = CurrencyPersist.getInstance().get(
							expenseMemo.getCurrencyID());
					_destAmount = expenseMemo.getDestAmount();
					_whitMemo = false;
				} else {
					_whitMemo = false;
				}
			} else {
				Income income = (Income) transaction;
				splitAccounts = income.getAccounts();
				spliStrings = income.getDescriptions();
				splitAmounts = income.getAmounts();
				if (transaction instanceof IncomeMemo) {
					IncomeMemo incomeMemo = (IncomeMemo) transaction;
					_memo = incomeMemo.getMemo();
					_whitMemo = true;
				} else if (transaction instanceof IncomeExchangeRateMemo) {
					IncomeExchangeRateMemo incomeMemo = (IncomeExchangeRateMemo) transaction;
					_currency = CurrencyPersist.getInstance().get(
							incomeMemo.getCurrencyID());
					_memo = incomeMemo.getMemo();
					_whitMemo = true;
				} else if (transaction instanceof IncomeExchangeRate) {
					IncomeExchangeRate incomeMemo = (IncomeExchangeRate) transaction;
					_currency = CurrencyPersist.getInstance().get(
							incomeMemo.getCurrencyID());
					_destAmount = incomeMemo.getDestAmount();
					_whitMemo = false;
				} else {
					_whitMemo = false;
				}
			}
			sizeAccounts = splitAccounts.length;

			if (1 < sizeAccounts || 0 < spliStrings[0].length()) {
				_splited = true;
			} else {
				_splited = false;
			}
			setSplits();

			category = categoryPersist.get(splitAccounts[0], type);
			_lastUsedCategory = splitAccounts[0];
			_categories.setSelectedIndex(category);
			_amount.setText(Util.toString(_balance, _currency, true));
			_amount.setLabel(_currency.getSign() + " ");

			_splits = new Split[sizeAccounts];
			for (index = 0; index < sizeAccounts; index++) {
				_splits[index] = new Split();
				_splits[index].setCategory(splitAccounts[index]);
				_splits[index].setType((short) type);
				_splits[index].setDescription(spliStrings[index]);
				_splits[index].setAmount(splitAmounts[index]);
			}
			_splitsList.set(_splits);
			break;
		case 2:
			Transfer transfer = (Transfer) transaction;
			if (transaction instanceof TransferMemo) {
				TransferMemo transferMemo = (TransferMemo) transaction;
				_memo = transferMemo.getMemo();
				_whitMemo = true;
			} else {
				_whitMemo = false;
			}
			Account account = accountLogic.get(transfer.getMirrorAccount());
			_splited = false;
			setSplits();
			_accountsDest.setSelectedIndex(account);

			if (_balance < 0) {
				_balance *= -1;
			}
			_amount.setText(Util.toString(_balance, _currency, true));

			_splits = new Split[0];
			_splitsList.set(_splits);
			break;
		}
	}

	public Transaction getTransaction() {
		Transaction transaction;
		int[] categories;
		long[] amounts;
		String[] descriptions;
		Category category;
		long amount;

		String amountst = _amount.getText();
		if (amountst.length() == 0) {
			amountst = "0.00";
		}
		int type = _type.getSelectedIndex();
		if (_splited) {
			int size = _splits.length;
			categories = new int[size];
			amounts = new long[size];
			descriptions = new String[size];
			for (int index = 0; index < size; index++) {
				descriptions[index] = _splits[index].getDescription();
				categories[index] = _splits[index].getCategory();
				amounts[index] = _splits[index].getAmount();
			}
		} else {
			categories = new int[1];
			amounts = new long[1];
			descriptions = new String[1];
			if (type != 2) {
				String description;
				category = (Category) _categories.getChoice(_categories
						.getSelectedIndex());
				if (_splits.length > 0)
					description = _splits[0].getDescription();
				else
					description = "";

				descriptions[0] = description;
				categories[0] = category.getUID();
				amounts[0] = Util.toLong(amountst, _currency);
			}
		}
		amount = Util.toLong(amountst, _currency);
		switch (type) {
		case 0:
			Expense expense;
			_destAmount *= -1;
			if (_whitMemo) {
				if (_currency.getUID() != getNewAccount().getCurrencyID()) {
					ExpenseExchangeRateMemo expenseExchangeRateMemo = new ExpenseExchangeRateMemo();
					expenseExchangeRateMemo.setCuRrencyID(_currency.getUID());
					expenseExchangeRateMemo.setDestAmount(_destAmount);
					expenseExchangeRateMemo.setMemo(_memo);
					expense = expenseExchangeRateMemo;
				} else {
					ExpenseMemo expenseMemo = new ExpenseMemo();
					expenseMemo.setMemo(_memo);
					expense = expenseMemo;
				}
			} else {
				if (_currency.getUID() != getNewAccount().getCurrencyID()) {
					ExpenseExchangeRate expenseExchangeRate = new ExpenseExchangeRate();
					expenseExchangeRate.setCuRrencyID(_currency.getUID());
					expenseExchangeRate.setDestAmount(_destAmount);
					expense = expenseExchangeRate;
				} else {
					expense = new Expense();
				}
			}
			transaction = expense;
			expense.setAccounts(categories);
			expense.setDescriptions(descriptions);
			expense.setAmounts(amounts);
			amount *= -1;
			break;
		case 1:
			Income income;
			if (_whitMemo) {
				if (_currency.getUID() != getNewAccount().getCurrencyID()) {
					IncomeExchangeRateMemo incomeExchangeRateMemo = new IncomeExchangeRateMemo();
					incomeExchangeRateMemo.setCurrencyID(_currency.getUID());
					incomeExchangeRateMemo.setDestAmount(_destAmount);
					incomeExchangeRateMemo.setMemo(_memo);
					income = incomeExchangeRateMemo;
				} else {
					IncomeMemo incomeMemo = new IncomeMemo();
					incomeMemo.setMemo(_memo);
					income = incomeMemo;
				}
			} else {
				if (_currency.getUID() != getNewAccount().getCurrencyID()) {
					IncomeExchangeRate incomeExchangeRate = new IncomeExchangeRate();
					incomeExchangeRate.setCurrencyID(_currency.getUID());
					incomeExchangeRate.setDestAmount(_destAmount);
					income = incomeExchangeRate;
				} else {
					income = new Income();
				}

			}
			transaction = income;
			income.setAccounts(categories);
			income.setDescriptions(descriptions);
			income.setAmounts(amounts);
			break;
		case 2:
			Transfer transfer;
			if (_whitMemo) {
				TransferMemo transferMemo = new TransferMemo();
				transferMemo.setMemo(_memo);
				transfer = transferMemo;
			} else
				transfer = new Transfer();

			transaction = transfer;
			Account accountDest = (Account) _accountsDest
					.getChoice(_accountsDest.getSelectedIndex());
			transfer.setMirrorAccount(accountDest.getUID());
			amount *= -1;
			break;
		default:
			transaction = new Transaction();
			break;
		}
		transaction.setId(UIDGenerator.getUID());
		transaction.setAmount(amount);
		transaction.setDate(_date.getDate());
		transaction.setNumber(_number.getText());
		transaction.setDescription(_description.getText());
		transaction.setStatus((byte) _status.getSelectedIndex());
		return transaction;
	}

	private void setSplitsDest() {
		int type = _type.getSelectedIndex();
		if (type != 2) {
			if (type == 0) {
				_categories.setLabel(_resources.getString(TO));
			} else {
				_categories.setLabel(_resources.getString(FROM));
			}

			if (_lastUsedCategory == -1) {
				Category[] categories = CategoryPersist.getInstance().get();
				int len = categories.length;
				for (int index = 0; index < len; index++) {
					if (categories[index].getType() == type) {
						_accountsDest.setSelectedIndex(categories[index]);
						break;
					}
				}
			}

		}
	}

	private void setSplits() {
		int type = _type.getSelectedIndex();
		if (_splited && type != 2) {
			if (_userControl != 4) {
				_amount.setEditable(false);
				_amount.setText(Util.toString(_balance, _currency, true));
				switch (_userControl) {
				case 0:
				case 1:
					_allFieldManager.delete(_categories);
					_allFieldManager.delete(_amount);
					break;
				case 2:
					_allFieldManager.delete(_accountsDest);
					_allFieldManager.delete(_amount);
					break;
				}
				_allFieldManager.add(_amount);
				_allFieldManager.add(_separator);
				_allFieldManager.add(_splitsList);
				_userControl = 4;
			}
			if (_lastUsedCategory == -1 && _categories.getSelectedIndex() != -1) {
				Category category = (Category) _categories
						.getChoice(_categories.getSelectedIndex());
				_lastUsedCategory = category.getUID();
			}
		} else {
			switch (_userControl) {
			case 0:
			case 1:
				_allFieldManager.delete(_categories);
				_allFieldManager.delete(_amount);
				break;
			case 2:
				_allFieldManager.delete(_accountsDest);
				_allFieldManager.delete(_amount);
				break;
			case 4:
				_allFieldManager.delete(_splitsList);
				_allFieldManager.delete(_separator);
				_allFieldManager.delete(_amount);
				break;
			}
			_userControl = type;
			_amount.setEditable(true);
			if (type != 2)
				_allFieldManager.add(_categories);
			else
				_allFieldManager.add(_accountsDest);

			_allFieldManager.add(_amount);
		}
		this.setSplitsDest();
	}

	public void update() {
		Object selectedItem = null;
		int selectedIndex;

		selectedIndex = _accounts.getSelectedIndex();
		if (selectedIndex != -1)
			selectedItem = _accounts.getChoice(selectedIndex);
		_accounts.setChoices(AccountPersist.getInstance().get());
		if (selectedIndex != -1)
			_accounts.setSelectedIndex(selectedItem);

		selectedIndex = _accountsDest.getSelectedIndex();
		if (selectedIndex != -1)
			selectedItem = _accountsDest.getChoice(selectedIndex);
		_accountsDest.setChoices(AccountPersist.getInstance().get());
		if (selectedIndex != -1)
			_accountsDest.setSelectedIndex(selectedItem);

		selectedIndex = _categories.getSelectedIndex();
		if (selectedIndex != -1)
			selectedItem = _categories.getChoice(selectedIndex);
		_categories.setChoices(CategoryPersist.getInstance().get());
		if (selectedIndex != -1)
			_categories.setSelectedIndex(selectedItem);
	}

	private FocusChangeListener _focusChangeListener = new FocusChangeListener() {
		public void focusChanged(Field field, int eventType) {
			if (field instanceof ListField) {
				if (eventType == FocusChangeListener.FOCUS_GAINED) {
					_splitsFocus = true;
				}
			} else if (field instanceof EditField) {
				if (eventType == FocusChangeListener.FOCUS_GAINED) {
					_splitsFocus = false;
				}
			}
		}
	};

	private ObjectListField _splitsList = new ObjectListField() {
		public void drawListRow(ListField listField, Graphics graphics,
				int index, int y, int width) {
			Split split = (Split) this.get(listField, index);
			String text;
			int oldColor = graphics.getColor();
			boolean changeColor = oldColor == Color.BLACK;
			long amount = split.getAmount();
			int w2 = width / 2;
			int fontHeight = Font.getDefault().getHeight();
			int fontHeight2 = (fontHeight * 2) - 1;

			graphics.drawText(split.getDescription(), 0, y, 0, width);

			Category category = _categoryLogic.get(split.getCategory(), split
					.getType());
			graphics.drawText(category.getDescription(), 0, y + fontHeight, 0,
					w2);
			graphics.setColor(oldColor);

			text = Util.toString(amount, _currency, false);
			graphics.drawText(text, w2, y + fontHeight, Graphics.RIGHT, w2);
			if (changeColor) {
				graphics.setColor(0xd3d3d3);
				graphics.drawLine(0, y + fontHeight2, width, y + fontHeight2);
			}
			graphics.setColor(oldColor);
		};

	};

}
