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
package com.beigebinder.misc;

import java.io.EOFException;
import java.io.UnsupportedEncodingException;

import com.beigebinder.data.Account;
import com.beigebinder.data.Category;
import com.beigebinder.data.Expense;
import com.beigebinder.data.ExpenseExchangeRate;
import com.beigebinder.data.ExpenseExchangeRateMemo;
import com.beigebinder.data.ExpenseMemo;
import com.beigebinder.data.Income;
import com.beigebinder.data.IncomeExchangeRate;
import com.beigebinder.data.IncomeExchangeRateMemo;
import com.beigebinder.data.IncomeMemo;
import com.beigebinder.data.Notification;
import com.beigebinder.data.SavedTransaction;
import com.beigebinder.data.ShopingList;
import com.beigebinder.data.TaxedShopingList;
import com.beigebinder.data.Transaction;
import com.beigebinder.data.Transfer;
import com.beigebinder.data.TransferMemo;
import net.rim.device.api.synchronization.SyncConverter;
import net.rim.device.api.synchronization.SyncObject;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.util.DataBuffer;

public class SyncConverterImpl implements SyncConverter {
	private byte _idtype;
	private int _id;
	private String _description;
	private boolean _dirty;
	private short _type;
	private int _currencyID;
	private long _initialBalance;
	private long _finalBalance;
	private long _reconciledBalance;
	private long _clearedBalance;
	private String _memo;
	private boolean _closed;
	private long _nextExecutionDate;
	private int _daysForAlert;
	private int _notificationsLeft;
	private short _used;
	private int _transactionId;
	private int _account;
	private int _parentAccount;
	private byte _status;
	private String _number;
	private long _date;
	private long _amount;
	private int _mirrorAccount;
	protected long _destAmount;
	private int _taxRate;
	private String[] _descriptions;
	private int[] _accounts;
	private long[] _amounts;

	protected boolean[] _taxables;
	protected boolean[] _priorities;
	protected int[] _quantities;
	protected byte[] _units;
	protected String[] _memos;
	protected boolean[] _checked;
	protected boolean[] _checkout;

	public boolean convert(SyncObject object, DataBuffer buffer, int version) {
		try {
			if (object instanceof Account) {
				Account account = (Account) object;
				return this.convert(account, buffer, version);
			} else if (object instanceof Category) {
				Category category = (Category) object;
				return this.convert(category, buffer, version);
			} else if (object instanceof ExpenseExchangeRateMemo) {
				ExpenseExchangeRateMemo expenseExchangeRateMemo = (ExpenseExchangeRateMemo) object;
				return this.convert(expenseExchangeRateMemo, buffer, version);
			} else if (object instanceof ExpenseExchangeRate) {
				ExpenseExchangeRate expenseExchangeRate = (ExpenseExchangeRate) object;
				return this.convert(expenseExchangeRate, buffer, version);
			} else if (object instanceof ExpenseMemo) {
				ExpenseMemo expenseMemo = (ExpenseMemo) object;
				return this.convert(expenseMemo, buffer, version);
			} else if (object instanceof Expense) {
				Expense expense = (Expense) object;
				return this.convert(expense, buffer, version);
			} else if (object instanceof IncomeExchangeRateMemo) {
				IncomeExchangeRateMemo incomeExchangeRateMemo = (IncomeExchangeRateMemo) object;
				return this.convert(incomeExchangeRateMemo, buffer, version);
			} else if (object instanceof IncomeExchangeRate) {
				IncomeExchangeRate incomeExchangeRate = (IncomeExchangeRate) object;
				return this.convert(incomeExchangeRate, buffer, version);
			} else if (object instanceof IncomeMemo) {
				IncomeMemo incomeMemo = (IncomeMemo) object;
				return this.convert(incomeMemo, buffer, version);
			} else if (object instanceof Income) {
				Income income = (Income) object;
				return this.convert(income, buffer, version);
			} else if (object instanceof Notification) {
				Notification notification = (Notification) object;
				return this.convert(notification, buffer, version);
			} else if (object instanceof SavedTransaction) {
				SavedTransaction savedTransaction = (SavedTransaction) object;
				return this.convert(savedTransaction, buffer, version);
			} else if (object instanceof Transfer) {
				Transfer transfer = (Transfer) object;
				return this.convert(transfer, buffer, version);
			} else if (object instanceof TransferMemo) {
				TransferMemo transferMemo = (TransferMemo) object;
				return this.convert(transferMemo, buffer, version);
			} else if (object instanceof Transaction) {
				Transaction transaction = (Transaction) object;
				return this.convert(transaction, buffer, version);
			} else if (object instanceof TaxedShopingList) {
				TaxedShopingList taxedShopingList = (TaxedShopingList) object;
				return this.convert(taxedShopingList, buffer, version);
			} else if (object instanceof ShopingList) {
				ShopingList shopingList = (ShopingList) object;
				return this.convert(shopingList, buffer, version);
			}
		} catch (UnsupportedEncodingException e) {
		}
		return false;
	}

	public SyncObject convert(DataBuffer data, int version, int UID) {
		byte[] bytes;
		int length;
		try {
			while (data.available() > 0) {
				length = data.readShort();
				switch (data.readByte()) {
				case TYPE_CLASS:
					_idtype = data.readByte();
					break;
				case TYPE_ID:
					_id = data.readInt();
					break;
				case TYPE_DESCRIPTION:
					bytes = new byte[length];
					data.readFully(bytes);
					if (version == 1) {
						_description = new String(bytes).trim();
					} else {
						_description = new String(bytes, "UTF-8").trim();
					}
					break;
				case TYPE_DIRTY:
					_dirty = data.readBoolean();
					break;
				case TYPE_TYPE:
					_type = data.readShort();
					break;
				case TYPE_CURRENCYID:
					_currencyID = data.readInt();
					break;
				case TYPE_INITIALBALANCE:
					_initialBalance = data.readLong();
					break;
				case TYPE_FINALBALANCE:
					_finalBalance = data.readLong();
					break;
				case TYPE_RECONCILEDBALANCE:
					_reconciledBalance = data.readLong();
					break;
				case TYPE_CLEAREDBALANCE:
					_clearedBalance = data.readLong();
					break;
				case TYPE_MEMO:
					bytes = new byte[length];
					data.readFully(bytes);
					if (version == 1) {
						_memo = new String(bytes).trim();
					} else {
						_memo = new String(bytes, "UTF-8").trim();
					}
					break;
				case TYPE_CLOSED:
					_closed = data.readBoolean();
					break;
				case TYPE_NEXTEXECUTIONDATE:
					_nextExecutionDate = data.readLong();
					break;
				case TYPE_DAYSFORALERT:
					_daysForAlert = data.readInt();
					break;
				case TYPE_NOTIFICATIONSLEFT:
					_notificationsLeft = data.readInt();
					break;
				case TYPE_USED:
					_used = data.readShort();
					break;
				case TYPE_TRANSACTIONID:
					_transactionId = data.readInt();
					break;
				case TYPE_ACCOUNT:
					_account = data.readInt();
					break;
				case TYPE_PARENTACCOUNT:
					_parentAccount = data.readInt();
					break;
				case TYPE_STATUS:
					_status = data.readByte();
					break;
				case TYPE_NUMBER:
					bytes = new byte[length];
					data.readFully(bytes);
					if (version == 1) {
						_number = new String(bytes).trim();
					} else {
						_number = new String(bytes, "UTF-8").trim();
					}
					break;
				case TYPE_DATE:
					_date = data.readLong();
					break;
				case TYPE_AMOUNT:
					_amount = data.readLong();
					break;
				case TYPE_MIRRORACCOUNT:
					_mirrorAccount = data.readInt();
					break;
				case TYPE_DESCRIPTIONS:
					_descriptions = toStringArray(data);
					break;
				case TYPE_ACCOUNTS:
					_accounts = toIntArray(data);
					break;
				case TYPE_AMOUNTS:
					_amounts = toLongArray(data);
					break;
				case TYPE_PRIORITIES:
					_priorities = toBooleanArray(data);
					break;
				case TYPE_QUANTITIES:
					_quantities = toIntArray(data);
					break;
				case TYPE_UNITS:
					_units = toByteArray(data);
					break;
				case TYPE_MEMOS:
					_memos = toStringArray(data);
					break;
				case TYPE_CHECKED:
					_checked = toBooleanArray(data);
					break;
				case TYPE_CHECKOUT:
					_checkout = toBooleanArray(data);
					break;
				case TYPE_DESTAMOUNT:
					_destAmount = data.readLong();
					break;
				case TYPE_TAXRATE:
					_taxRate = data.readInt();
					break;
				case TYPE_TAXABLES:
					_taxables = toBooleanArray(data);
					break;
				default:
					bytes = new byte[length];
					data.readFully(bytes);
					break;
				}
			}
		} catch (EOFException e) {
			Dialog.inform(e.toString());
		} catch (UnsupportedEncodingException e) {
			Dialog.inform(e.toString());
		}

		switch (_idtype) {
		case CLASS_ACCOUT:
			return getAccount(version);
		case CLASS_CATEGORY:
			return getCategory(version);
		case CLASS_EXPENSE:
			return getExpense(version);
		case CLASS_EXPENSEMEMO:
			return getExpenseMemo(version);
		case CLASS_INCOME:
			return getIncome(version);
		case CLASS_INCOMEMEMO:
			return getIncomeMemo(version);
		case CLASS_NOTIFICATION:
			return getNotification(version);
		case CLASS_SAVEDTRANSACTION:
			return getSavedTransaction(version);
		case CLASS_TRANSACTION:
			return getTransaction(version);
		case CLASS_TRANSFER:
			return getTransfer(version);
		case CLASS_TRANSFERMEMO:
			return getTransferMemo(version);
		case CLASS_SHOPINGLIST:
			return getShopingList(version);
		case CLASS_TAXABLESHOPINGLIST:
			return getTaxedShopingList(version);
		case CLASS_EXPENSEEXCHANGERATE:
			return getExpenseExchangeRate(version);
		case CLASS_INCOMEEXCHANGERATE:
			return getExpenseExchangeRateMemo(version);
		case CLASS_EXPENSEEXCHANGERATEMEMO:
			return getIncomeExchangeRate(version);
		case CLASS_INCOMEEXCHANGERATEMEMO:
			return getIncomeExchangeRateMemo(version);
		}
		return null;
	}

	private Account getAccount(int version) {
		Account account = new Account();
		account.setId(_id);
		account.setType(_type);
		account.setDescription(_description);
		account.setCurrencyID(_currencyID);
		account.setInitialBalance(_initialBalance);
		account.setFinalBalance(_finalBalance);
		account.setReconciledBalance(_reconciledBalance);
		account.setClearedBalance(_clearedBalance);
		account.setMemo(_memo);
		account.setClosed(_closed);
		account.setDirty(_dirty);
		return account;
	}

	private boolean convert(Account account, DataBuffer buffer, int version) throws UnsupportedEncodingException {
		byte[] bytes;

		buffer.writeShort(1);
		buffer.writeByte(TYPE_CLASS);
		buffer.write(CLASS_ACCOUT);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_ID);
		buffer.writeInt(account.getUID());

		buffer.writeShort(2);
		buffer.writeByte(TYPE_TYPE);
		buffer.writeShort(account.getType());

		if (version == 1) {
			bytes = account.getDescription().getBytes();
		} else {
			bytes = account.getDescription().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_DESCRIPTION);
		buffer.write(bytes);
		buffer.writeByte(0);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_CURRENCYID);
		buffer.writeInt(account.getCurrencyID());

		buffer.writeShort(8);
		buffer.writeByte(TYPE_INITIALBALANCE);
		buffer.writeLong(account.getInitialBalance());

		buffer.writeShort(8);
		buffer.writeByte(TYPE_FINALBALANCE);
		buffer.writeLong(account.getFinalBalance());

		buffer.writeShort(8);
		buffer.writeByte(TYPE_RECONCILEDBALANCE);
		buffer.writeLong(account.getReconciledBalance());

		buffer.writeShort(8);
		buffer.writeByte(TYPE_CLEAREDBALANCE);
		buffer.writeLong(account.getClearedBalance());

		if (version == 1) {
			bytes = account.getMemo().getBytes();
		} else {
			bytes = account.getMemo().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_MEMO);
		buffer.write(bytes);
		buffer.writeByte(0);

		buffer.writeShort(1);
		buffer.writeByte(TYPE_CLOSED);
		buffer.writeBoolean(account.isClosed());

		buffer.writeShort(1);
		buffer.writeByte(TYPE_DIRTY);
		buffer.writeBoolean(account.isDirty());

		buffer.trim(true);
		return true;
	}

	private Category getCategory(int version) {
		Category category = new Category();
		category.setId(_id);
		category.setType(_type);
		category.setDescription(_description);
		category.setDirty(_dirty);
		return category;
	}

	private boolean convert(Category category, DataBuffer buffer, int version) throws UnsupportedEncodingException {
		byte[] bytes;

		buffer.writeShort(1);
		buffer.writeByte(TYPE_CLASS);
		buffer.write(CLASS_CATEGORY);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_ID);
		buffer.writeInt(category.getUID());

		buffer.writeShort(2);
		buffer.writeByte(TYPE_TYPE);
		buffer.writeShort(category.getType());

		if (version == 1) {
			bytes = category.getDescription().getBytes();
		} else {
			bytes = category.getDescription().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_DESCRIPTION);
		buffer.write(bytes);
		buffer.writeByte(0);

		buffer.writeShort(1);
		buffer.writeByte(TYPE_DIRTY);
		buffer.writeBoolean(category.isDirty());
		return true;
	}

	private Expense getExpense(int version) {
		Expense expense = new Expense();
		expense.setId(_id);
		expense.setParentAccount(_parentAccount);
		expense.setStatus(_status);
		expense.setNumber(_number);
		expense.setDescription(_description);
		expense.setDate(_date);
		expense.setAmount(_amount);
		expense.setDirty(_dirty);
		expense.setDescriptions(_descriptions);
		expense.setAccounts(_accounts);
		expense.setAmounts(_amounts);
		return expense;
	}

	private boolean convert(Expense expense, DataBuffer buffer, int version) throws UnsupportedEncodingException {
		byte[] bytes;

		buffer.writeShort(1);
		buffer.writeByte(TYPE_CLASS);
		buffer.write(CLASS_EXPENSE);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_ID);
		buffer.writeInt(expense.getUID());

		buffer.writeShort(4);
		buffer.writeByte(TYPE_PARENTACCOUNT);
		buffer.writeInt(expense.getParentAccount());

		buffer.writeShort(1);
		buffer.writeByte(TYPE_STATUS);
		buffer.write(expense.getStatus());

		if (version == 1) {
			bytes = expense.getNumber().getBytes();
		} else {
			bytes = expense.getNumber().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_NUMBER);
		buffer.write(bytes);
		buffer.writeByte(0);

		if (version == 1) {
			bytes = expense.getDescription().getBytes();
		} else {
			bytes = expense.getDescription().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_DESCRIPTION);
		buffer.write(bytes);
		buffer.writeByte(0);

		buffer.writeShort(8);
		buffer.writeByte(TYPE_DATE);
		buffer.writeLong(expense.getDate());

		buffer.writeShort(8);
		buffer.writeByte(TYPE_AMOUNT);
		buffer.writeLong(expense.getAmount());

		bytes = toByteArray(expense.getDescriptions(), buffer.isBigEndian(), version);
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_DESCRIPTIONS);
		buffer.write(bytes);

		bytes = toByteArray(expense.getAccounts(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_ACCOUNTS);
		buffer.write(bytes);

		bytes = toByteArray(expense.getAmounts(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_AMOUNTS);
		buffer.write(bytes);

		buffer.writeShort(1);
		buffer.writeByte(TYPE_DIRTY);
		buffer.writeBoolean(expense.isDirty());
		return true;
	}

	private Expense getExpenseExchangeRate(int version) {
		ExpenseExchangeRate expenseExchangeRate = new ExpenseExchangeRate();
		expenseExchangeRate.setId(_id);
		expenseExchangeRate.setParentAccount(_parentAccount);
		expenseExchangeRate.setStatus(_status);
		expenseExchangeRate.setNumber(_number);
		expenseExchangeRate.setDescription(_description);
		expenseExchangeRate.setDate(_date);
		expenseExchangeRate.setAmount(_amount);
		expenseExchangeRate.setDirty(_dirty);
		expenseExchangeRate.setDescriptions(_descriptions);
		expenseExchangeRate.setAccounts(_accounts);
		expenseExchangeRate.setAmounts(_amounts);
		expenseExchangeRate.setCuRrencyID(_currencyID);
		expenseExchangeRate.setDestAmount(_destAmount);
		return expenseExchangeRate;
	}

	private boolean convert(ExpenseExchangeRate expenseExchangeRate, DataBuffer buffer, int version) throws UnsupportedEncodingException {
		byte[] bytes;

		buffer.writeShort(1);
		buffer.writeByte(TYPE_CLASS);
		buffer.write(CLASS_EXPENSE);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_ID);
		buffer.writeInt(expenseExchangeRate.getUID());

		buffer.writeShort(4);
		buffer.writeByte(TYPE_PARENTACCOUNT);
		buffer.writeInt(expenseExchangeRate.getParentAccount());

		buffer.writeShort(1);
		buffer.writeByte(TYPE_STATUS);
		buffer.write(expenseExchangeRate.getStatus());

		if (version == 1) {
			bytes = expenseExchangeRate.getNumber().getBytes();
		} else {
			bytes = expenseExchangeRate.getNumber().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_NUMBER);
		buffer.write(bytes);
		buffer.writeByte(0);

		if (version == 1) {
			bytes = expenseExchangeRate.getDescription().getBytes();
		} else {
			bytes = expenseExchangeRate.getDescription().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_DESCRIPTION);
		buffer.write(bytes);
		buffer.writeByte(0);

		buffer.writeShort(8);
		buffer.writeByte(TYPE_DATE);
		buffer.writeLong(expenseExchangeRate.getDate());

		buffer.writeShort(8);
		buffer.writeByte(TYPE_AMOUNT);
		buffer.writeLong(expenseExchangeRate.getAmount());

		bytes = toByteArray(expenseExchangeRate.getDescriptions(), buffer.isBigEndian(), version);
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_DESCRIPTIONS);
		buffer.write(bytes);

		bytes = toByteArray(expenseExchangeRate.getAccounts(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_ACCOUNTS);
		buffer.write(bytes);

		bytes = toByteArray(expenseExchangeRate.getAmounts(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_AMOUNTS);
		buffer.write(bytes);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_CURRENCYID);
		buffer.writeInt(expenseExchangeRate.getCurrencyID());

		buffer.writeShort(8);
		buffer.writeByte(TYPE_DESTAMOUNT);
		buffer.writeLong(expenseExchangeRate.getDestAmount());

		buffer.writeShort(1);
		buffer.writeByte(TYPE_DIRTY);
		buffer.writeBoolean(expenseExchangeRate.isDirty());
		return true;
	}

	private Expense getExpenseExchangeRateMemo(int version) {
		ExpenseExchangeRateMemo expenseExchangeRateMemo = new ExpenseExchangeRateMemo();
		expenseExchangeRateMemo.setId(_id);
		expenseExchangeRateMemo.setParentAccount(_parentAccount);
		expenseExchangeRateMemo.setStatus(_status);
		expenseExchangeRateMemo.setNumber(_number);
		expenseExchangeRateMemo.setDescription(_description);
		expenseExchangeRateMemo.setDate(_date);
		expenseExchangeRateMemo.setAmount(_amount);
		expenseExchangeRateMemo.setDirty(_dirty);
		expenseExchangeRateMemo.setDescriptions(_descriptions);
		expenseExchangeRateMemo.setAccounts(_accounts);
		expenseExchangeRateMemo.setAmounts(_amounts);
		expenseExchangeRateMemo.setCuRrencyID(_currencyID);
		expenseExchangeRateMemo.setDestAmount(_destAmount);
		expenseExchangeRateMemo.setMemo(_memo);
		return expenseExchangeRateMemo;
	}

	private boolean convert(ExpenseExchangeRateMemo expenseExchangeRateMemo, DataBuffer buffer, int version) throws UnsupportedEncodingException {
		byte[] bytes;

		buffer.writeShort(1);
		buffer.writeByte(TYPE_CLASS);
		buffer.write(CLASS_EXPENSE);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_ID);
		buffer.writeInt(expenseExchangeRateMemo.getUID());

		buffer.writeShort(4);
		buffer.writeByte(TYPE_PARENTACCOUNT);
		buffer.writeInt(expenseExchangeRateMemo.getParentAccount());

		buffer.writeShort(1);
		buffer.writeByte(TYPE_STATUS);
		buffer.write(expenseExchangeRateMemo.getStatus());

		if (version == 1) {
			bytes = expenseExchangeRateMemo.getNumber().getBytes();
		} else {
			bytes = expenseExchangeRateMemo.getNumber().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_NUMBER);
		buffer.write(bytes);
		buffer.writeByte(0);

		if (version == 1) {
			bytes = expenseExchangeRateMemo.getDescription().getBytes();
		} else {
			bytes = expenseExchangeRateMemo.getDescription().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_DESCRIPTION);
		buffer.write(bytes);
		buffer.writeByte(0);

		buffer.writeShort(8);
		buffer.writeByte(TYPE_DATE);
		buffer.writeLong(expenseExchangeRateMemo.getDate());

		buffer.writeShort(8);
		buffer.writeByte(TYPE_AMOUNT);
		buffer.writeLong(expenseExchangeRateMemo.getAmount());

		bytes = toByteArray(expenseExchangeRateMemo.getDescriptions(), buffer.isBigEndian(), version);
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_DESCRIPTIONS);
		buffer.write(bytes);

		bytes = toByteArray(expenseExchangeRateMemo.getAccounts(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_ACCOUNTS);
		buffer.write(bytes);

		bytes = toByteArray(expenseExchangeRateMemo.getAmounts(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_AMOUNTS);
		buffer.write(bytes);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_CURRENCYID);
		buffer.writeInt(expenseExchangeRateMemo.getCurrencyID());

		buffer.writeShort(8);
		buffer.writeByte(TYPE_DESTAMOUNT);
		buffer.writeLong(expenseExchangeRateMemo.getDestAmount());

		if (version == 1) {
			bytes = expenseExchangeRateMemo.getMemo().getBytes();
		} else {
			bytes = expenseExchangeRateMemo.getMemo().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_MEMO);
		buffer.write(bytes);
		buffer.writeByte(0);

		buffer.writeShort(1);
		buffer.writeByte(TYPE_DIRTY);
		buffer.writeBoolean(expenseExchangeRateMemo.isDirty());
		return true;
	}

	private ExpenseMemo getExpenseMemo(int version) {
		ExpenseMemo expenseMemo = new ExpenseMemo();
		expenseMemo.setId(_id);
		expenseMemo.setParentAccount(_parentAccount);
		expenseMemo.setStatus(_status);
		expenseMemo.setNumber(_number);
		expenseMemo.setDescription(_description);
		expenseMemo.setDate(_date);
		expenseMemo.setAmount(_amount);
		expenseMemo.setDirty(_dirty);
		expenseMemo.setDescriptions(_descriptions);
		expenseMemo.setAccounts(_accounts);
		expenseMemo.setAmounts(_amounts);
		expenseMemo.setMemo(_memo);
		return expenseMemo;
	}

	private boolean convert(ExpenseMemo expenseMemo, DataBuffer buffer, int version) throws UnsupportedEncodingException {
		byte[] bytes;

		buffer.writeShort(1);
		buffer.writeByte(TYPE_CLASS);
		buffer.write(CLASS_EXPENSEMEMO);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_ID);
		buffer.writeInt(expenseMemo.getUID());

		buffer.writeShort(4);
		buffer.writeByte(TYPE_PARENTACCOUNT);
		buffer.writeInt(expenseMemo.getParentAccount());

		buffer.writeShort(1);
		buffer.writeByte(TYPE_STATUS);
		buffer.write(expenseMemo.getStatus());

		if (version == 1) {
			bytes = expenseMemo.getNumber().getBytes();
		} else {
			bytes = expenseMemo.getNumber().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_NUMBER);
		buffer.write(bytes);
		buffer.writeByte(0);

		if (version == 1) {
			bytes = expenseMemo.getDescription().getBytes();
		} else {
			bytes = expenseMemo.getDescription().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_DESCRIPTION);
		buffer.write(bytes);
		buffer.writeByte(0);

		buffer.writeShort(8);
		buffer.writeByte(TYPE_DATE);
		buffer.writeLong(expenseMemo.getDate());

		buffer.writeShort(8);
		buffer.writeByte(TYPE_AMOUNT);
		buffer.writeLong(expenseMemo.getAmount());

		bytes = toByteArray(expenseMemo.getDescriptions(), buffer.isBigEndian(), version);
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_DESCRIPTIONS);
		buffer.write(bytes);

		bytes = toByteArray(expenseMemo.getAccounts(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_ACCOUNTS);
		buffer.write(bytes);

		bytes = toByteArray(expenseMemo.getAmounts(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_AMOUNTS);
		buffer.write(bytes);

		if (version == 1) {
			bytes = expenseMemo.getMemo().getBytes();
		} else {
			bytes = expenseMemo.getMemo().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_MEMO);
		buffer.write(bytes);
		buffer.writeByte(0);

		buffer.writeShort(1);
		buffer.writeByte(TYPE_DIRTY);
		buffer.writeBoolean(expenseMemo.isDirty());
		return true;
	}

	private Income getIncome(int version) {
		Income income = new Income();
		income.setId(_id);
		income.setParentAccount(_parentAccount);
		income.setStatus(_status);
		income.setNumber(_number);
		income.setDescription(_description);
		income.setDate(_date);
		income.setAmount(_amount);
		income.setDirty(_dirty);
		income.setDescriptions(_descriptions);
		income.setAccounts(_accounts);
		income.setAmounts(_amounts);
		return income;
	}

	private boolean convert(Income income, DataBuffer buffer, int version) throws UnsupportedEncodingException {
		byte[] bytes;

		buffer.writeShort(1);
		buffer.writeByte(TYPE_CLASS);
		buffer.write(CLASS_INCOME);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_ID);
		buffer.writeInt(income.getUID());

		buffer.writeShort(4);
		buffer.writeByte(TYPE_PARENTACCOUNT);
		buffer.writeInt(income.getParentAccount());

		buffer.writeShort(1);
		buffer.writeByte(TYPE_STATUS);
		buffer.write(income.getStatus());

		if (version == 1) {
			bytes = income.getNumber().getBytes();
		} else {
			bytes = income.getNumber().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_NUMBER);
		buffer.write(bytes);
		buffer.writeByte(0);

		if (version == 1) {
			bytes = income.getDescription().getBytes();
		} else {
			bytes = income.getDescription().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_DESCRIPTION);
		buffer.write(bytes);
		buffer.writeByte(0);

		buffer.writeShort(8);
		buffer.writeByte(TYPE_DATE);
		buffer.writeLong(income.getDate());

		buffer.writeShort(8);
		buffer.writeByte(TYPE_AMOUNT);
		buffer.writeLong(income.getAmount());

		bytes = toByteArray(income.getDescriptions(), buffer.isBigEndian(), version);
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_DESCRIPTIONS);
		buffer.write(bytes);

		bytes = toByteArray(income.getAccounts(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_ACCOUNTS);
		buffer.write(bytes);

		bytes = toByteArray(income.getAmounts(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_AMOUNTS);
		buffer.write(bytes);

		buffer.writeShort(1);
		buffer.writeByte(TYPE_DIRTY);
		buffer.writeBoolean(income.isDirty());
		return true;
	}

	private IncomeMemo getIncomeMemo(int version) {
		IncomeMemo incomeMemo = new IncomeMemo();
		incomeMemo.setId(_id);
		incomeMemo.setParentAccount(_parentAccount);
		incomeMemo.setStatus(_status);
		incomeMemo.setNumber(_number);
		incomeMemo.setDescription(_description);
		incomeMemo.setDate(_date);
		incomeMemo.setAmount(_amount);
		incomeMemo.setDirty(_dirty);
		incomeMemo.setDescriptions(_descriptions);
		incomeMemo.setAccounts(_accounts);
		incomeMemo.setAmounts(_amounts);
		incomeMemo.setMemo(_memo);
		return incomeMemo;
	}

	private boolean convert(IncomeMemo incomeMemo, DataBuffer buffer, int version) throws UnsupportedEncodingException {
		byte[] bytes;

		buffer.writeShort(1);
		buffer.writeByte(TYPE_CLASS);
		buffer.write(CLASS_INCOMEMEMO);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_ID);
		buffer.writeInt(incomeMemo.getUID());

		buffer.writeShort(4);
		buffer.writeByte(TYPE_PARENTACCOUNT);
		buffer.writeInt(incomeMemo.getParentAccount());

		buffer.writeShort(1);
		buffer.writeByte(TYPE_STATUS);
		buffer.write(incomeMemo.getStatus());

		if (version == 1) {
			bytes = incomeMemo.getNumber().getBytes();
		} else {
			bytes = incomeMemo.getNumber().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_NUMBER);
		buffer.write(bytes);
		buffer.writeByte(0);

		if (version == 1) {
			bytes = incomeMemo.getDescription().getBytes();
		} else {
			bytes = incomeMemo.getDescription().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_DESCRIPTION);
		buffer.write(bytes);
		buffer.writeByte(0);

		buffer.writeShort(8);
		buffer.writeByte(TYPE_DATE);
		buffer.writeLong(incomeMemo.getDate());

		buffer.writeShort(8);
		buffer.writeByte(TYPE_AMOUNT);
		buffer.writeLong(incomeMemo.getAmount());

		bytes = toByteArray(incomeMemo.getDescriptions(), buffer.isBigEndian(), version);
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_DESCRIPTIONS);
		buffer.write(bytes);

		bytes = toByteArray(incomeMemo.getAccounts(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_ACCOUNTS);
		buffer.write(bytes);

		bytes = toByteArray(incomeMemo.getAmounts(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_AMOUNTS);
		buffer.write(bytes);

		if (version == 1) {
			bytes = incomeMemo.getMemo().getBytes();
		} else {
			bytes = incomeMemo.getMemo().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_MEMO);
		buffer.write(bytes);
		buffer.writeByte(0);

		buffer.writeShort(1);
		buffer.writeByte(TYPE_DIRTY);
		buffer.writeBoolean(incomeMemo.isDirty());
		return true;
	}

	private Income getIncomeExchangeRate(int version) {
		IncomeExchangeRate incomeExchangeRate = new IncomeExchangeRate();
		incomeExchangeRate.setId(_id);
		incomeExchangeRate.setParentAccount(_parentAccount);
		incomeExchangeRate.setStatus(_status);
		incomeExchangeRate.setNumber(_number);
		incomeExchangeRate.setDescription(_description);
		incomeExchangeRate.setDate(_date);
		incomeExchangeRate.setAmount(_amount);
		incomeExchangeRate.setDirty(_dirty);
		incomeExchangeRate.setDescriptions(_descriptions);
		incomeExchangeRate.setAccounts(_accounts);
		incomeExchangeRate.setAmounts(_amounts);
		incomeExchangeRate.setCurrencyID(_currencyID);
		incomeExchangeRate.setDestAmount(_destAmount);
		return incomeExchangeRate;
	}

	private boolean convert(IncomeExchangeRate incomeExchangeRate, DataBuffer buffer, int version) throws UnsupportedEncodingException {
		byte[] bytes;

		buffer.writeShort(1);
		buffer.writeByte(TYPE_CLASS);
		buffer.write(CLASS_INCOME);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_ID);
		buffer.writeInt(incomeExchangeRate.getUID());

		buffer.writeShort(4);
		buffer.writeByte(TYPE_PARENTACCOUNT);
		buffer.writeInt(incomeExchangeRate.getParentAccount());

		buffer.writeShort(1);
		buffer.writeByte(TYPE_STATUS);
		buffer.write(incomeExchangeRate.getStatus());

		if (version == 1) {
			bytes = incomeExchangeRate.getNumber().getBytes();
		} else {
			bytes = incomeExchangeRate.getNumber().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_NUMBER);
		buffer.write(bytes);
		buffer.writeByte(0);

		if (version == 1) {
			bytes = incomeExchangeRate.getDescription().getBytes();
		} else {
			bytes = incomeExchangeRate.getDescription().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_DESCRIPTION);
		buffer.write(bytes);
		buffer.writeByte(0);

		buffer.writeShort(8);
		buffer.writeByte(TYPE_DATE);
		buffer.writeLong(incomeExchangeRate.getDate());

		buffer.writeShort(8);
		buffer.writeByte(TYPE_AMOUNT);
		buffer.writeLong(incomeExchangeRate.getAmount());

		bytes = toByteArray(incomeExchangeRate.getDescriptions(), buffer.isBigEndian(), version);
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_DESCRIPTIONS);
		buffer.write(bytes);

		bytes = toByteArray(incomeExchangeRate.getAccounts(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_ACCOUNTS);
		buffer.write(bytes);

		bytes = toByteArray(incomeExchangeRate.getAmounts(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_AMOUNTS);
		buffer.write(bytes);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_CURRENCYID);
		buffer.writeInt(incomeExchangeRate.getCurrencyID());

		buffer.writeShort(8);
		buffer.writeByte(TYPE_DESTAMOUNT);
		buffer.writeLong(incomeExchangeRate.getDestAmount());

		buffer.writeShort(1);
		buffer.writeByte(TYPE_DIRTY);
		buffer.writeBoolean(incomeExchangeRate.isDirty());
		return true;
	}

	private Income getIncomeExchangeRateMemo(int version) {
		IncomeExchangeRateMemo incomeExchangeRateMemo = new IncomeExchangeRateMemo();
		incomeExchangeRateMemo.setId(_id);
		incomeExchangeRateMemo.setParentAccount(_parentAccount);
		incomeExchangeRateMemo.setStatus(_status);
		incomeExchangeRateMemo.setNumber(_number);
		incomeExchangeRateMemo.setDescription(_description);
		incomeExchangeRateMemo.setDate(_date);
		incomeExchangeRateMemo.setAmount(_amount);
		incomeExchangeRateMemo.setDirty(_dirty);
		incomeExchangeRateMemo.setDescriptions(_descriptions);
		incomeExchangeRateMemo.setAccounts(_accounts);
		incomeExchangeRateMemo.setAmounts(_amounts);
		incomeExchangeRateMemo.setCurrencyID(_currencyID);
		incomeExchangeRateMemo.setDestAmount(_destAmount);
		incomeExchangeRateMemo.setMemo(_memo);
		return incomeExchangeRateMemo;
	}

	private boolean convert(IncomeExchangeRateMemo incomeExchangeRateMemo, DataBuffer buffer, int version) throws UnsupportedEncodingException {
		byte[] bytes;

		buffer.writeShort(1);
		buffer.writeByte(TYPE_CLASS);
		buffer.write(CLASS_INCOME);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_ID);
		buffer.writeInt(incomeExchangeRateMemo.getUID());

		buffer.writeShort(4);
		buffer.writeByte(TYPE_PARENTACCOUNT);
		buffer.writeInt(incomeExchangeRateMemo.getParentAccount());

		buffer.writeShort(1);
		buffer.writeByte(TYPE_STATUS);
		buffer.write(incomeExchangeRateMemo.getStatus());

		if (version == 1) {
			bytes = incomeExchangeRateMemo.getNumber().getBytes();
		} else {
			bytes = incomeExchangeRateMemo.getNumber().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_NUMBER);
		buffer.write(bytes);
		buffer.writeByte(0);

		if (version == 1) {
			bytes = incomeExchangeRateMemo.getDescription().getBytes();
		} else {
			bytes = incomeExchangeRateMemo.getDescription().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_DESCRIPTION);
		buffer.write(bytes);
		buffer.writeByte(0);

		buffer.writeShort(8);
		buffer.writeByte(TYPE_DATE);
		buffer.writeLong(incomeExchangeRateMemo.getDate());

		buffer.writeShort(8);
		buffer.writeByte(TYPE_AMOUNT);
		buffer.writeLong(incomeExchangeRateMemo.getAmount());

		bytes = toByteArray(incomeExchangeRateMemo.getDescriptions(), buffer.isBigEndian(), version);
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_DESCRIPTIONS);
		buffer.write(bytes);

		bytes = toByteArray(incomeExchangeRateMemo.getAccounts(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_ACCOUNTS);
		buffer.write(bytes);

		bytes = toByteArray(incomeExchangeRateMemo.getAmounts(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_AMOUNTS);
		buffer.write(bytes);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_CURRENCYID);
		buffer.writeInt(incomeExchangeRateMemo.getCurrencyID());

		buffer.writeShort(8);
		buffer.writeByte(TYPE_DESTAMOUNT);
		buffer.writeLong(incomeExchangeRateMemo.getDestAmount());

		if (version == 1) {
			bytes = incomeExchangeRateMemo.getMemo().getBytes();
		} else {
			bytes = incomeExchangeRateMemo.getMemo().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_MEMO);
		buffer.write(bytes);
		buffer.writeByte(0);

		buffer.writeShort(1);
		buffer.writeByte(TYPE_DIRTY);
		buffer.writeBoolean(incomeExchangeRateMemo.isDirty());
		return true;
	}

	private Notification getNotification(int version) {
		Notification notification = new Notification();
		notification.setId(_id);
		notification.setDescription(_description);
		notification.setType(_type);
		notification.setNextExecutionDate(_nextExecutionDate);
		notification.setDaysForAlert(_daysForAlert);
		notification.setNotificationsLeft(_notificationsLeft);
		notification.setAccount(_account);
		notification.setTransactionId(_transactionId);
		notification.setDirty(_dirty);
		return notification;
	}

	private boolean convert(Notification notification, DataBuffer buffer, int version) throws UnsupportedEncodingException {
		byte[] bytes;

		buffer.writeShort(1);
		buffer.writeByte(TYPE_CLASS);
		buffer.write(CLASS_NOTIFICATION);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_ID);
		buffer.writeInt(notification.getUID());

		if (version == 1) {
			bytes = notification.getDescription().getBytes();
		} else {
			bytes = notification.getDescription().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_DESCRIPTION);
		buffer.write(bytes);
		buffer.writeByte(0);

		buffer.writeShort(2);
		buffer.writeByte(TYPE_TYPE);
		buffer.writeShort(notification.getType());

		buffer.writeShort(8);
		buffer.writeByte(TYPE_NEXTEXECUTIONDATE);
		buffer.writeLong(notification.getNextExecutionDate());

		buffer.writeShort(4);
		buffer.writeByte(TYPE_DAYSFORALERT);
		buffer.writeInt(notification.getDaysForAlert());

		buffer.writeShort(4);
		buffer.writeByte(TYPE_NOTIFICATIONSLEFT);
		buffer.writeInt(notification.getNotificationsLeft());

		buffer.writeShort(4);
		buffer.writeByte(TYPE_ACCOUNT);
		buffer.writeInt(notification.getAccount());

		buffer.writeShort(4);
		buffer.writeByte(TYPE_TRANSACTIONID);
		buffer.writeInt(notification.getTransactionId());

		buffer.writeShort(1);
		buffer.writeByte(TYPE_DIRTY);
		buffer.writeBoolean(notification.isDirty());
		return true;
	}

	private SavedTransaction getSavedTransaction(int version) {
		SavedTransaction savedTransaction = new SavedTransaction();
		savedTransaction.setId(_id);
		savedTransaction.setDescription(_description);
		savedTransaction.setTransactionId(_transactionId);
		savedTransaction.setAccount(_account);
		savedTransaction.setUsed(_used);
		savedTransaction.setDirty(_dirty);
		return savedTransaction;
	}

	private boolean convert(SavedTransaction savedTransaction, DataBuffer buffer, int version) throws UnsupportedEncodingException {
		byte[] bytes;

		buffer.writeShort(1);
		buffer.writeByte(TYPE_CLASS);
		buffer.write(CLASS_SAVEDTRANSACTION);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_ID);
		buffer.writeInt(savedTransaction.getUID());

		if (version == 1) {
			bytes = savedTransaction.getDescription().getBytes();
		} else {
			bytes = savedTransaction.getDescription().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_DESCRIPTION);
		buffer.write(bytes);
		buffer.writeByte(0);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_ACCOUNT);
		buffer.writeInt(savedTransaction.getAccount());

		buffer.writeShort(4);
		buffer.writeByte(TYPE_TRANSACTIONID);
		buffer.writeInt(savedTransaction.getTransactionId());

		buffer.writeShort(2);
		buffer.writeByte(TYPE_USED);
		buffer.writeShort(savedTransaction.getUsed());

		buffer.writeShort(1);
		buffer.writeByte(TYPE_DIRTY);
		buffer.writeBoolean(savedTransaction.isDirty());
		return true;
	}

	private Transaction getTransaction(int version) {
		Transaction transaction = new Transaction();
		transaction.setId(_id);
		transaction.setParentAccount(_parentAccount);
		transaction.setStatus(_status);
		transaction.setNumber(_number);
		transaction.setDescription(_description);
		transaction.setDate(_date);
		transaction.setAmount(_amount);
		transaction.setDirty(_dirty);
		return transaction;
	}

	private boolean convert(Transaction transaction, DataBuffer buffer, int version) throws UnsupportedEncodingException {
		byte[] bytes;

		buffer.writeShort(1);
		buffer.writeByte(TYPE_CLASS);
		buffer.write(CLASS_TRANSACTION);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_ID);
		buffer.writeInt(transaction.getUID());

		buffer.writeShort(4);
		buffer.writeByte(TYPE_PARENTACCOUNT);
		buffer.writeInt(transaction.getParentAccount());

		buffer.writeShort(1);
		buffer.writeByte(TYPE_STATUS);
		buffer.write(transaction.getStatus());

		if (version == 1) {
			bytes = transaction.getNumber().getBytes();
		} else {
			bytes = transaction.getNumber().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_NUMBER);
		buffer.write(bytes);
		buffer.writeByte(0);

		if (version == 1) {
			bytes = transaction.getDescription().getBytes();
		} else {
			bytes = transaction.getDescription().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_DESCRIPTION);
		buffer.write(bytes);
		buffer.writeByte(0);

		buffer.writeShort(8);
		buffer.writeByte(TYPE_DATE);
		buffer.writeLong(transaction.getDate());

		buffer.writeShort(8);
		buffer.writeByte(TYPE_AMOUNT);
		buffer.writeLong(transaction.getAmount());

		buffer.writeShort(1);
		buffer.writeByte(TYPE_DIRTY);
		buffer.writeBoolean(transaction.isDirty());
		return true;
	}

	private Transfer getTransfer(int version) {
		Transfer transfer = new Transfer();
		transfer.setId(_id);
		transfer.setParentAccount(_parentAccount);
		transfer.setStatus(_status);
		transfer.setNumber(_number);
		transfer.setDescription(_description);
		transfer.setDate(_date);
		transfer.setAmount(_amount);
		transfer.setDirty(_dirty);
		transfer.setMirrorAccount(_mirrorAccount);
		return transfer;
	}

	private boolean convert(Transfer transfer, DataBuffer buffer, int version) throws UnsupportedEncodingException {
		byte[] bytes;

		buffer.writeShort(1);
		buffer.writeByte(TYPE_CLASS);
		buffer.write(CLASS_TRANSFER);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_ID);
		buffer.writeInt(transfer.getUID());

		buffer.writeShort(4);
		buffer.writeByte(TYPE_PARENTACCOUNT);
		buffer.writeInt(transfer.getParentAccount());

		buffer.writeShort(1);
		buffer.writeByte(TYPE_STATUS);
		buffer.write(transfer.getStatus());

		if (version == 1) {
			bytes = transfer.getNumber().getBytes();
		} else {
			bytes = transfer.getNumber().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_NUMBER);
		buffer.write(bytes);
		buffer.writeByte(0);

		if (version == 1) {
			bytes = transfer.getDescription().getBytes();
		} else {
			bytes = transfer.getDescription().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_DESCRIPTION);
		buffer.write(bytes);
		buffer.writeByte(0);

		buffer.writeShort(8);
		buffer.writeByte(TYPE_DATE);
		buffer.writeLong(transfer.getDate());

		buffer.writeShort(8);
		buffer.writeByte(TYPE_AMOUNT);
		buffer.writeLong(transfer.getAmount());

		buffer.writeShort(4);
		buffer.writeByte(TYPE_MIRRORACCOUNT);
		buffer.writeInt(transfer.getMirrorAccount());

		buffer.writeShort(1);
		buffer.writeByte(TYPE_DIRTY);
		buffer.writeBoolean(transfer.isDirty());
		return true;
	}

	private TransferMemo getTransferMemo(int version) {
		TransferMemo transferMemo = new TransferMemo();
		transferMemo.setId(_id);
		transferMemo.setParentAccount(_parentAccount);
		transferMemo.setStatus(_status);
		transferMemo.setNumber(_number);
		transferMemo.setDescription(_description);
		transferMemo.setDate(_date);
		transferMemo.setAmount(_amount);
		transferMemo.setDirty(_dirty);
		transferMemo.setMirrorAccount(_mirrorAccount);
		transferMemo.setMemo(_memo);
		return transferMemo;
	}

	private boolean convert(TransferMemo transferMemo, DataBuffer buffer, int version) throws UnsupportedEncodingException {
		byte[] bytes;

		buffer.writeShort(1);
		buffer.writeByte(TYPE_CLASS);
		buffer.write(CLASS_TRANSFERMEMO);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_ID);
		buffer.writeInt(transferMemo.getUID());

		buffer.writeShort(4);
		buffer.writeByte(TYPE_PARENTACCOUNT);
		buffer.writeInt(transferMemo.getParentAccount());

		buffer.writeShort(1);
		buffer.writeByte(TYPE_STATUS);
		buffer.write(transferMemo.getStatus());

		if (version == 1) {
			bytes = transferMemo.getNumber().getBytes();
		} else {
			bytes = transferMemo.getNumber().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_NUMBER);
		buffer.write(bytes);
		buffer.writeByte(0);

		if (version == 1) {
			bytes = transferMemo.getDescription().getBytes();
		} else {
			bytes = transferMemo.getDescription().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_DESCRIPTION);
		buffer.write(bytes);
		buffer.writeByte(0);

		buffer.writeShort(8);
		buffer.writeByte(TYPE_DATE);
		buffer.writeLong(transferMemo.getDate());

		buffer.writeShort(8);
		buffer.writeByte(TYPE_AMOUNT);
		buffer.writeLong(transferMemo.getAmount());

		buffer.writeShort(4);
		buffer.writeByte(TYPE_MIRRORACCOUNT);
		buffer.writeInt(transferMemo.getMirrorAccount());

		if (version == 1) {
			bytes = transferMemo.getMemo().getBytes();
		} else {
			bytes = transferMemo.getMemo().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_MEMO);
		buffer.write(bytes);
		buffer.writeByte(0);

		buffer.writeShort(1);
		buffer.writeByte(TYPE_DIRTY);
		buffer.writeBoolean(transferMemo.isDirty());
		return true;
	}

	private ShopingList getShopingList(int version) {
		ShopingList shopinglsit = new ShopingList();
		shopinglsit.setId(_id);
		shopinglsit.setDescription(_description);
		shopinglsit.setCurrency(_currencyID);
		shopinglsit.setDirty(_dirty);
		shopinglsit.setDescriptions(_descriptions);
		shopinglsit.setAccounts(_accounts);
		shopinglsit.setAmounts(_amounts);
		shopinglsit.setPriorities(_priorities);
		shopinglsit.setQuantities(_quantities);
		shopinglsit.setUnits(_units);
		shopinglsit.setMemos(_memos);
		shopinglsit.setChequed(_checked);
		shopinglsit.setCheckout(_checkout);

		return shopinglsit;
	}

	private boolean convert(ShopingList shopinglist, DataBuffer buffer, int version) throws UnsupportedEncodingException {
		byte[] bytes;

		buffer.writeShort(1);
		buffer.writeByte(TYPE_CLASS);
		buffer.write(CLASS_SHOPINGLIST);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_ID);
		buffer.writeInt(shopinglist.getUID());

		if (version == 1) {
			bytes = shopinglist.getDescription().getBytes();
		} else {
			bytes = shopinglist.getDescription().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_DESCRIPTION);
		buffer.write(bytes);
		buffer.writeByte(0);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_CURRENCYID);
		buffer.writeInt(shopinglist.getCurrency());

		bytes = toByteArray(shopinglist.getDescriptions(), buffer.isBigEndian(), version);
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_DESCRIPTIONS);
		buffer.write(bytes);

		bytes = toByteArray(shopinglist.getAccounts(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_ACCOUNTS);
		buffer.write(bytes);

		bytes = toByteArray(shopinglist.getAmounts(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_AMOUNTS);
		buffer.write(bytes);

		buffer.writeShort(1);
		buffer.writeByte(TYPE_DIRTY);
		buffer.writeBoolean(shopinglist.isDirty());

		bytes = toByteArray(shopinglist.getPriorities(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_PRIORITIES);
		buffer.write(bytes);

		bytes = toByteArray(shopinglist.getQuantities(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_QUANTITIES);
		buffer.write(bytes);

		bytes = toByteArray(shopinglist.getUnits(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_UNITS);
		buffer.write(bytes);

		bytes = toByteArray(shopinglist.getMemos(), buffer.isBigEndian(), version);
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_MEMOS);
		buffer.write(bytes);

		bytes = toByteArray(shopinglist.getChequed(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_CHECKED);
		buffer.write(bytes);

		bytes = toByteArray(shopinglist.getCheckout(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_CHECKOUT);
		buffer.write(bytes);

		return true;
	}

	private TaxedShopingList getTaxedShopingList(int version) {
		TaxedShopingList shopinglsit = new TaxedShopingList();
		shopinglsit.setId(_id);
		shopinglsit.setDescription(_description);
		shopinglsit.setCurrency(_currencyID);
		shopinglsit.setDirty(_dirty);
		shopinglsit.setDescriptions(_descriptions);
		shopinglsit.setAccounts(_accounts);
		shopinglsit.setAmounts(_amounts);
		shopinglsit.setPriorities(_priorities);
		shopinglsit.setQuantities(_quantities);
		shopinglsit.setUnits(_units);
		shopinglsit.setMemos(_memos);
		shopinglsit.setChequed(_checked);
		shopinglsit.setCheckout(_checkout);
		shopinglsit.setTaxRate(_taxRate);
		shopinglsit.setTaxable(_taxables);

		return shopinglsit;
	}

	private boolean convert(TaxedShopingList shopinglist, DataBuffer buffer, int version) throws UnsupportedEncodingException {
		byte[] bytes;

		buffer.writeShort(1);
		buffer.writeByte(TYPE_CLASS);
		buffer.write(CLASS_SHOPINGLIST);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_ID);
		buffer.writeInt(shopinglist.getUID());

		if (version == 1) {
			bytes = shopinglist.getDescription().getBytes();
		} else {
			bytes = shopinglist.getDescription().getBytes("UTF-8");
		}
		buffer.writeShort(bytes.length + 1);
		buffer.writeByte(TYPE_DESCRIPTION);
		buffer.write(bytes);
		buffer.writeByte(0);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_CURRENCYID);
		buffer.writeInt(shopinglist.getCurrency());

		bytes = toByteArray(shopinglist.getDescriptions(), buffer.isBigEndian(), version);
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_DESCRIPTIONS);
		buffer.write(bytes);

		bytes = toByteArray(shopinglist.getAccounts(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_ACCOUNTS);
		buffer.write(bytes);

		bytes = toByteArray(shopinglist.getAmounts(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_AMOUNTS);
		buffer.write(bytes);

		buffer.writeShort(1);
		buffer.writeByte(TYPE_DIRTY);
		buffer.writeBoolean(shopinglist.isDirty());

		bytes = toByteArray(shopinglist.getPriorities(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_PRIORITIES);
		buffer.write(bytes);

		bytes = toByteArray(shopinglist.getQuantities(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_QUANTITIES);
		buffer.write(bytes);

		bytes = toByteArray(shopinglist.getUnits(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_UNITS);
		buffer.write(bytes);

		bytes = toByteArray(shopinglist.getMemos(), buffer.isBigEndian(), version);
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_MEMOS);
		buffer.write(bytes);

		bytes = toByteArray(shopinglist.getChequed(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_CHECKED);
		buffer.write(bytes);

		bytes = toByteArray(shopinglist.getCheckout(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_CHECKOUT);
		buffer.write(bytes);

		buffer.writeShort(4);
		buffer.writeByte(TYPE_TAXRATE);
		buffer.writeInt(shopinglist.getTaxRate());

		bytes = toByteArray(shopinglist.getTaxable(), buffer.isBigEndian());
		buffer.writeShort(bytes.length);
		buffer.writeByte(TYPE_TAXABLES);
		buffer.write(bytes);

		return true;
	}

	private String[] toStringArray(DataBuffer data) {
		String[] strings = null;
		byte length;
		byte[] bytes;
		try {
			int size = data.readInt();
			strings = new String[size];
			for (int index = 0; index < size; index++) {
				length = data.readByte();
				bytes = new byte[length];
				data.readFully(bytes);
				strings[index] = new String(bytes).trim();
			}
		} catch (EOFException e) {
			Dialog.inform(e.toString());
		}
		return strings;
	}

	private byte[] toByteArray(String[] strings, boolean bigEndianFlag, int version) throws UnsupportedEncodingException {
		DataBuffer buffer = new DataBuffer(bigEndianFlag);
		byte[] bytes;
		int size = strings.length;
		buffer.writeInt(size);
		for (int index = 0; index < size; index++) {
			if (version == 1) {
				bytes = strings[index].getBytes();
			} else {
				bytes = strings[index].getBytes("UTF-8");
			}
			buffer.write(bytes.length + 1);
			buffer.write(bytes);
			buffer.writeByte(0);
		}
		buffer.trim(true);
		return buffer.getArray();
	}

	private boolean[] toBooleanArray(DataBuffer data) {
		boolean[] booleans = null;
		try {
			int size = data.readInt();
			booleans = new boolean[size];
			for (int index = 0; index < size; index++) {
				booleans[index] = data.readBoolean();
			}
		} catch (EOFException e) {
			Dialog.inform(e.toString());
		}
		return booleans;
	}

	private byte[] toByteArray(boolean[] booleans, boolean bigEndianFlag) {
		DataBuffer buffer = new DataBuffer(bigEndianFlag);
		int size = booleans.length;
		buffer.writeInt(size);
		for (int index = 0; index < size; index++) {
			buffer.writeBoolean(booleans[index]);
		}
		buffer.trim(true);
		return buffer.getArray();
	}

	private byte[] toByteArray(DataBuffer data) {
		byte[] bytes = null;
		try {
			int size = data.readInt();
			bytes = new byte[size];
			for (int index = 0; index < size; index++) {
				bytes[index] = data.readByte();
			}
		} catch (EOFException e) {
			Dialog.inform(e.toString());
		}
		return bytes;
	}

	private byte[] toByteArray(byte[] bytes, boolean bigEndianFlag) {
		DataBuffer buffer = new DataBuffer(bigEndianFlag);
		int size = bytes.length;
		buffer.writeInt(size);
		for (int index = 0; index < size; index++) {
			buffer.writeByte(bytes[index]);
		}
		buffer.trim(true);
		return buffer.getArray();
	}

	private int[] toIntArray(DataBuffer data) {
		int[] ints = null;
		try {
			int size = data.readInt();
			ints = new int[size];
			for (int index = 0; index < size; index++) {
				ints[index] = data.readInt();
			}
		} catch (EOFException e) {
			Dialog.inform(e.toString());
		}
		return ints;
	}

	private byte[] toByteArray(int[] integers, boolean bigEndianFlag) {
		DataBuffer buffer = new DataBuffer(bigEndianFlag);
		int size = integers.length;
		buffer.writeInt(size);
		for (int index = 0; index < size; index++) {
			buffer.writeInt(integers[index]);
		}
		buffer.trim(true);
		return buffer.getArray();
	}

	private long[] toLongArray(DataBuffer data) {
		long[] longs = null;
		try {
			int size = data.readInt();
			longs = new long[size];
			for (int index = 0; index < size; index++) {
				longs[index] = data.readLong();
			}
		} catch (EOFException e) {
			Dialog.inform(e.toString());
		}
		return longs;
	}

	private byte[] toByteArray(long[] longs, boolean bigEndianFlag) {
		DataBuffer buffer = new DataBuffer(bigEndianFlag);
		int size = longs.length;
		buffer.writeInt(size);
		for (int index = 0; index < size; index++) {
			buffer.writeLong(longs[index]);
		}
		buffer.trim(true);
		return buffer.getArray();
	}

	private static final byte CLASS_ACCOUT = 0;
	private static final byte CLASS_CATEGORY = 1;
	private static final byte CLASS_EXPENSE = 3;
	private static final byte CLASS_INCOME = 4;
	private static final byte CLASS_NOTIFICATION = 5;
	private static final byte CLASS_SAVEDTRANSACTION = 6;
	private static final byte CLASS_TRANSACTION = 7;
	private static final byte CLASS_TRANSFER = 8;
	private static final byte CLASS_EXPENSEMEMO = 9;
	private static final byte CLASS_INCOMEMEMO = 10;
	private static final byte CLASS_TRANSFERMEMO = 11;
	private static final byte CLASS_SHOPINGLIST = 12;
	private static final byte CLASS_EXPENSEEXCHANGERATE = 13;
	private static final byte CLASS_INCOMEEXCHANGERATE = 14;
	private static final byte CLASS_EXPENSEEXCHANGERATEMEMO = 15;
	private static final byte CLASS_INCOMEEXCHANGERATEMEMO = 16;
	private static final byte CLASS_TAXABLESHOPINGLIST = 17;

	private static final byte TYPE_CLASS = 1;
	private static final byte TYPE_ID = 2;
	private static final byte TYPE_DESCRIPTION = 3;
	private static final byte TYPE_DIRTY = 4;
	private static final byte TYPE_TYPE = 5;
	private static final byte TYPE_CURRENCYID = 6;
	private static final byte TYPE_INITIALBALANCE = 7;
	private static final byte TYPE_FINALBALANCE = 8;
	private static final byte TYPE_MEMO = 9;
	private static final byte TYPE_CLOSED = 10;
	private static final byte TYPE_NEXTEXECUTIONDATE = 11;
	private static final byte TYPE_DAYSFORALERT = 12;
	private static final byte TYPE_NOTIFICATIONSLEFT = 13;
	private static final byte TYPE_USED = 15;
	private static final byte TYPE_TRANSACTIONID = 16;
	private static final byte TYPE_ACCOUNT = 17;
	private static final byte TYPE_PARENTACCOUNT = 18;
	private static final byte TYPE_STATUS = 19;
	private static final byte TYPE_NUMBER = 20;
	private static final byte TYPE_DATE = 21;
	private static final byte TYPE_AMOUNT = 22;
	private static final byte TYPE_MIRRORACCOUNT = 23;
	private static final byte TYPE_DESCRIPTIONS = 24;
	private static final byte TYPE_ACCOUNTS = 25;
	private static final byte TYPE_AMOUNTS = 26;
	private static final byte TYPE_RECONCILEDBALANCE = 27;
	private static final byte TYPE_CLEAREDBALANCE = 28;
	private static final byte TYPE_PRIORITIES = 29;
	private static final byte TYPE_QUANTITIES = 30;
	private static final byte TYPE_UNITS = 31;
	private static final byte TYPE_MEMOS = 32;
	private static final byte TYPE_CHECKED = 33;
	private static final byte TYPE_CHECKOUT = 34;
	private static final byte TYPE_DESTAMOUNT = 35;
	private static final byte TYPE_TAXRATE = 36;
	private static final byte TYPE_TAXABLES = 37;

}
