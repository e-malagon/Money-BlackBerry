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

import com.beigebinder.data.Account;
import com.beigebinder.data.Currency;
import com.beigebinder.data.Expense;
import com.beigebinder.data.ExpenseExchangeRate;
import com.beigebinder.data.ExpenseExchangeRateMemo;
import com.beigebinder.data.ExpenseMemo;
import com.beigebinder.data.Income;
import com.beigebinder.data.IncomeExchangeRate;
import com.beigebinder.data.IncomeExchangeRateMemo;
import com.beigebinder.data.IncomeMemo;
import com.beigebinder.data.Transaction;
import com.beigebinder.data.Transfer;
import com.beigebinder.data.TransferMemo;
import com.beigebinder.misc.Util;
import com.beigebinder.persist.AccountPersist;
import com.beigebinder.persist.CategoryPersist;
import com.beigebinder.persist.CurrencyPersist;
import com.beigebinder.resource.MoneyResource;
import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.util.DataBuffer;

public class ExportTextImplementation implements IExport {
	private AccountPersist _accountLogic;
	private CategoryPersist _categoryPersist;
	private DateFormat _sdFormat;
	private long _balance;
	private String _name;

	private static ResourceBundle _resources = ResourceBundle.getBundle(MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public ExportTextImplementation() {
		_accountLogic = AccountPersist.getInstance();
		_categoryPersist = CategoryPersist.getInstance();
		_name = _resources.getStringArray(MoneyResource.EXPORTTYPES)[0];
		_sdFormat = SimpleDateFormat.getInstance(SimpleDateFormat.DATE_SHORT);
	}

	public void writeHeader(DataBuffer buffer, Account account, Currency currency, boolean includeInitialBalance) {
		_balance = 0;
		try {
			buffer.writeShort(0xFEFF);
			buffer.write(account.getDescription().getBytes("UTF-16BE"));
			buffer.writeShort(0x000D);
			buffer.writeShort(0x000A);

			buffer.write(_resources.getString(MoneyResource.BALANCEINITIAL).getBytes("UTF-16BE"));
			buffer.write(Util.complete(Util.toString(account.getInitialBalance(), currency, false), 16, 1).getBytes("UTF-16BE"));
			buffer.writeShort(0x000D);
			buffer.writeShort(0x000A);

			String[] titles = _resources.getStringArray(MoneyResource.REPORTTITLES);
			int len = titles.length;
			for (int indx = 0; indx < len; indx++) {
				buffer.write(titles[indx].getBytes("UTF-16BE"));
			}
			buffer.writeShort(0x000D);
			buffer.writeShort(0x000A);
		} catch (Exception ex) {
			Dialog.inform(ex.toString());
		}
	}

	public void writeTransaction(DataBuffer buffer, Currency currency, Transaction transaction) {
		if (transaction instanceof ExpenseExchangeRate) {
			writeTransaction(buffer, currency, (ExpenseExchangeRate) transaction);
		} else if (transaction instanceof Expense) {
			writeTransaction(buffer, currency, (Expense) transaction);
		} else if (transaction instanceof IncomeExchangeRate) {
			writeTransaction(buffer, currency, (IncomeExchangeRate) transaction);
		} else if (transaction instanceof Income) {
			writeTransaction(buffer, currency, (Income) transaction);
		} else if (transaction instanceof Transfer) {
			writeTransaction(buffer, currency, (Transfer) transaction);
		}
	}

	private void writeTransaction(DataBuffer buffer, Currency currency, Expense expense) {
		int sizet = expense.getAccounts().length;
		long[] amounts = expense.getAmounts();
		String[] descriptions = expense.getDescriptions();
		int[] accounts = expense.getAccounts();
		boolean withMemo;
		String aux;
		_balance += expense.getAmount();

		if (expense instanceof ExpenseMemo) {
			ExpenseMemo expenseMemo = (ExpenseMemo) expense;
			aux = expenseMemo.getMemo();
			withMemo = true;
		} else {
			withMemo = false;
			aux = "";
		}
		try {
			buffer.write(Util.complete(_sdFormat.formatLocal(expense.getDate()), 11, 1).getBytes("UTF-16BE"));

			buffer.write(Util.complete(expense.getNumber(), 16, 0).getBytes("UTF-16BE"));
			buffer.write(Util.complete(expense.getDescription(), 31, 0).getBytes("UTF-16BE"));

			if (sizet == 1) {
				buffer.write(Util.complete(_categoryPersist.get(accounts[0], 0).getDescription(), 31, 0).getBytes("UTF-16BE"));
				buffer.write(Util.complete(Util.toString(expense.getAmount(), currency, false), 16, 1).getBytes("UTF-16BE"));
				if (withMemo) {
					buffer.write("  ".getBytes("UTF-16BE"));
					buffer.write(aux.replace('\n', ' ').getBytes("UTF-16BE"));
				}
				buffer.writeShort(0x000D);
				buffer.writeShort(0x000A);
			} else {
				buffer.write(Util.complete(_resources.getString(MoneyResource.SPLITMULTIPLESCATEGORIES), 31, 0).getBytes("UTF-16BE"));
				buffer.write(Util.complete(Util.toString(expense.getAmount(), currency, false), 16, 1).getBytes("UTF-16BE"));
				if (withMemo) {
					buffer.write("  ".getBytes("UTF-16BE"));
					buffer.write(aux.replace('\n', ' ').getBytes("UTF-16BE"));
				}
				buffer.writeShort(0x000D);
				buffer.writeShort(0x000A);
				for (int i = 0; i < sizet; i++) {
					buffer.write("                                                          ".getBytes("UTF-16BE"));
					if (descriptions[i].length() == 0) {
						buffer.write(Util.complete(_categoryPersist.get(accounts[i], 0).getDescription(), 31, 0).getBytes("UTF-16BE"));
					} else {
						buffer.write(Util.complete(descriptions[i], 31, 0).getBytes("UTF-16BE"));
					}
					buffer.write(Util.complete(Util.toString(amounts[i] * -1, currency, false), 16, 1).getBytes("UTF-16BE"));
					buffer.writeShort(0x000D);
					buffer.writeShort(0x000A);
				}
			}
		} catch (Exception ex) {
			Dialog.inform(ex.toString());
		}
	}

	private void writeTransaction(DataBuffer buffer, Currency currency, ExpenseExchangeRate expense) {
		int sizet = expense.getAccounts().length;
		long[] amounts = expense.getAmounts();
		String[] descriptions = expense.getDescriptions();
		int[] accounts = expense.getAccounts();
		boolean withMemo;
		String aux;
		_balance += expense.getDestAmount();

		if (expense instanceof ExpenseExchangeRateMemo) {
			ExpenseExchangeRateMemo expenseMemo = (ExpenseExchangeRateMemo) expense;
			aux = expenseMemo.getMemo();
			withMemo = true;
		} else {
			withMemo = false;
			aux = "";
		}
		try {
			buffer.write(Util.complete(_sdFormat.formatLocal(expense.getDate()), 11, 1).getBytes("UTF-16BE"));

			buffer.write(Util.complete(expense.getNumber(), 16, 0).getBytes("UTF-16BE"));
			buffer.write(Util.complete(expense.getDescription(), 31, 0).getBytes("UTF-16BE"));

			if (sizet == 1) {
				buffer.write(Util.complete(_categoryPersist.get(accounts[0], 0).getDescription(), 31, 0).getBytes("UTF-16BE"));
				buffer.write(Util.complete(Util.toString(expense.getDestAmount(), currency, false), 16, 1).getBytes("UTF-16BE"));
				if (withMemo) {
					buffer.write("  ".getBytes("UTF-16BE"));
					buffer.write(aux.replace('\n', ' ').getBytes("UTF-16BE"));
				}
				buffer.writeShort(0x000D);
				buffer.writeShort(0x000A);
			} else {
				int i;
				long tmpAmount = 0;
				long tmpTotal = 0;
				Currency exchangeRateCurrency = GetExchangeRateScreen.getExchangeRateCurrency();
				Currency destCurrency = CurrencyPersist.getInstance().get(expense.getCurrencyID());
				long exchangeRate = Util.toLong(Util.amountToExchange(expense.getDestAmount(), expense.getAmount()), exchangeRateCurrency);

				buffer.write(Util.complete(_resources.getString(MoneyResource.SPLITMULTIPLESCATEGORIES), 31, 0).getBytes("UTF-16BE"));
				buffer.write(Util.complete(Util.toString(expense.getDestAmount(), currency, false), 16, 1).getBytes("UTF-16BE"));
				if (withMemo) {
					buffer.write("  ".getBytes("UTF-16BE"));
					buffer.write(aux.replace('\n', ' ').getBytes("UTF-16BE"));
				}
				buffer.writeShort(0x000D);
				buffer.writeShort(0x000A);
				for (i = 0; i < sizet - 1; i++) {
					tmpAmount = Util.exchangeToAmount(amounts[i] * -1, exchangeRate, currency, exchangeRateCurrency, destCurrency);

					tmpTotal += tmpAmount;

					buffer.write("                                                          ".getBytes("UTF-16BE"));
					if (descriptions[i].length() == 0) {
						buffer.write(Util.complete(_categoryPersist.get(accounts[i], 0).getDescription(), 31, 0).getBytes("UTF-16BE"));
					} else {
						buffer.write(Util.complete(descriptions[i], 31, 0).getBytes("UTF-16BE"));
					}
					buffer.write(Util.complete(Util.toString(tmpAmount, currency, false), 16, 1).getBytes("UTF-16BE"));
					buffer.writeShort(0x000D);
					buffer.writeShort(0x000A);
				}
				tmpAmount = Util.exchangeToAmount(amounts[i] * -1, exchangeRate, currency, exchangeRateCurrency, destCurrency);

				tmpTotal += tmpAmount;

				buffer.write("                                                          ".getBytes("UTF-16BE"));
				if (descriptions[i].length() == 0) {
					buffer.write(Util.complete(_categoryPersist.get(accounts[i], 0).getDescription(), 31, 0).getBytes("UTF-16BE"));
				} else {
					buffer.write(Util.complete(descriptions[i], 31, 0).getBytes("UTF-16BE"));
				}
				buffer.write(Util.complete(Util.toString(tmpAmount - (Math.abs(expense.getDestAmount()) - Math.abs(tmpTotal)), currency, false), 16, 1).getBytes("UTF-16BE"));
				buffer.writeShort(0x000D);
				buffer.writeShort(0x000A);

			}
		} catch (Exception ex) {
			Dialog.inform(ex.toString());
		}
	}

	private void writeTransaction(DataBuffer buffer, Currency currency, Income income) {
		int sizet = income.getAccounts().length;
		long[] amounts = income.getAmounts();
		String[] descriptions = income.getDescriptions();
		int[] accounts = income.getAccounts();
		boolean withMemo;
		String aux;
		_balance += income.getAmount();

		if (income instanceof IncomeMemo) {
			IncomeMemo incomeMemo = (IncomeMemo) income;
			aux = incomeMemo.getMemo();
			withMemo = true;
		} else {
			aux = "";
			withMemo = false;
		}
		try {
			buffer.write(Util.complete(_sdFormat.formatLocal(income.getDate()), 11, 1).getBytes("UTF-16BE"));

			buffer.write(Util.complete(income.getNumber(), 16, 0).getBytes("UTF-16BE"));
			buffer.write(Util.complete(income.getDescription(), 31, 0).getBytes("UTF-16BE"));

			if (sizet == 1) {
				buffer.write(Util.complete(_categoryPersist.get(accounts[0], 1).getDescription(), 31, 0).getBytes("UTF-16BE"));
				buffer.write(Util.complete(Util.toString(income.getAmount(), currency, false), 16, 1).getBytes("UTF-16BE"));
				if (withMemo) {
					buffer.write("  ".getBytes("UTF-16BE"));
					buffer.write(aux.replace('\n', ' ').getBytes("UTF-16BE"));
				}
				buffer.writeShort(0x000D);
				buffer.writeShort(0x000A);
			} else {
				buffer.write(Util.complete(_resources.getString(MoneyResource.SPLITMULTIPLESCATEGORIES), 31, 0).getBytes("UTF-16BE"));
				buffer.write(Util.complete(Util.toString(income.getAmount(), currency, false), 16, 1).getBytes("UTF-16BE"));
				if (withMemo) {
					buffer.write("  ".getBytes("UTF-16BE"));
					buffer.write(aux.replace('\n', ' ').getBytes("UTF-16BE"));
				}
				buffer.writeShort(0x000D);
				buffer.writeShort(0x000A);
				for (int i = 0; i < sizet; i++) {
					buffer.write("                                                          ".getBytes("UTF-16BE"));
					if (descriptions[i].length() == 0) {
						buffer.write(Util.complete(_categoryPersist.get(accounts[i], 1).getDescription(), 31, 0).getBytes("UTF-16BE"));
					} else {
						buffer.write(Util.complete(descriptions[i], 31, 0).getBytes("UTF-16BE"));
					}
					buffer.write(Util.complete(Util.toString(amounts[i], currency, false), 16, 1).getBytes("UTF-16BE"));
					buffer.writeShort(0x000D);
					buffer.writeShort(0x000A);
				}
			}
		} catch (Exception ex) {
			Dialog.inform(ex.toString());
		}
	}

	private void writeTransaction(DataBuffer buffer, Currency currency, IncomeExchangeRate income) {
		int sizet = income.getAccounts().length;
		long[] amounts = income.getAmounts();
		String[] descriptions = income.getDescriptions();
		int[] accounts = income.getAccounts();
		boolean withMemo;
		String aux;
		_balance += income.getDestAmount();

		if (income instanceof IncomeExchangeRateMemo) {
			IncomeExchangeRateMemo incomeMemo = (IncomeExchangeRateMemo) income;
			aux = incomeMemo.getMemo();
			withMemo = true;
		} else {
			aux = "";
			withMemo = false;
		}
		try {
			buffer.write(Util.complete(_sdFormat.formatLocal(income.getDate()), 11, 1).getBytes("UTF-16BE"));

			buffer.write(Util.complete(income.getNumber(), 16, 0).getBytes("UTF-16BE"));
			buffer.write(Util.complete(income.getDescription(), 31, 0).getBytes("UTF-16BE"));

			if (sizet == 1) {
				buffer.write(Util.complete(_categoryPersist.get(accounts[0], 1).getDescription(), 31, 0).getBytes("UTF-16BE"));
				buffer.write(Util.complete(Util.toString(income.getDestAmount(), currency, false), 16, 1).getBytes("UTF-16BE"));
				if (withMemo) {
					buffer.write("  ".getBytes("UTF-16BE"));
					buffer.write(aux.replace('\n', ' ').getBytes("UTF-16BE"));
				}
				buffer.writeShort(0x000D);
				buffer.writeShort(0x000A);
			} else {
				int i;
				long tmpAmount = 0;
				long tmpTotal = 0;
				Currency exchangeRateCurrency = GetExchangeRateScreen.getExchangeRateCurrency();
				Currency destCurrency = CurrencyPersist.getInstance().get(income.getCurrencyID());
				long exchangeRate = Util.toLong(Util.amountToExchange(income.getDestAmount(), income.getAmount()), exchangeRateCurrency);

				buffer.write(Util.complete(_resources.getString(MoneyResource.SPLITMULTIPLESCATEGORIES), 31, 0).getBytes("UTF-16BE"));
				buffer.write(Util.complete(Util.toString(income.getDestAmount(), currency, false), 16, 1).getBytes("UTF-16BE"));
				if (withMemo) {
					buffer.write("  ".getBytes("UTF-16BE"));
					buffer.write(aux.replace('\n', ' ').getBytes("UTF-16BE"));
				}
				buffer.writeShort(0x000D);
				buffer.writeShort(0x000A);
				for (i = 0; i < sizet - 1; i++) {
					tmpAmount = Util.exchangeToAmount(amounts[i], exchangeRate, currency, exchangeRateCurrency, destCurrency);

					tmpTotal += tmpAmount;

					buffer.write("                                                          ".getBytes("UTF-16BE"));
					if (descriptions[i].length() == 0) {
						buffer.write(Util.complete(_categoryPersist.get(accounts[i], 1).getDescription(), 31, 0).getBytes("UTF-16BE"));
					} else {
						buffer.write(Util.complete(descriptions[i], 31, 0).getBytes("UTF-16BE"));
					}
					buffer.write(Util.complete(Util.toString(tmpAmount, currency, false), 16, 1).getBytes("UTF-16BE"));
					buffer.writeShort(0x000D);
					buffer.writeShort(0x000A);
				}
				tmpAmount = Util.exchangeToAmount(amounts[i], exchangeRate, currency, exchangeRateCurrency, destCurrency);

				tmpTotal += tmpAmount;

				buffer.write("                                                          ".getBytes("UTF-16BE"));
				if (descriptions[i].length() == 0) {
					buffer.write(Util.complete(_categoryPersist.get(accounts[i], 1).getDescription(), 31, 0).getBytes("UTF-16BE"));
				} else {
					buffer.write(Util.complete(descriptions[i], 31, 0).getBytes("UTF-16BE"));
				}
				buffer.write(Util.complete(Util.toString(tmpAmount + (Math.abs(income.getDestAmount()) - Math.abs(tmpTotal)), currency, false), 16, 1).getBytes("UTF-16BE"));
				buffer.writeShort(0x000D);
				buffer.writeShort(0x000A);

			}
		} catch (Exception ex) {
			Dialog.inform(ex.toString());
		}
	}

	private void writeTransaction(DataBuffer buffer, Currency currency, Transfer transfer) {
		String aux;
		_balance += transfer.getAmount();

		if (transfer.getAmount() < 0) {
			aux = _resources.getString(MoneyResource.TRANSFERTO);
			aux += _accountLogic.get(transfer.getMirrorAccount()).getDescription();
		} else {
			aux = _resources.getString(MoneyResource.TRANSFERFROM);
			aux += _accountLogic.get(transfer.getMirrorAccount()).getDescription();
		}

		try {
			buffer.write(Util.complete(_sdFormat.formatLocal(transfer.getDate()), 11, 1).getBytes("UTF-16BE"));

			buffer.write(Util.complete(transfer.getNumber(), 16, 0).getBytes("UTF-16BE"));
			buffer.write(Util.complete(transfer.getDescription(), 31, 0).getBytes("UTF-16BE"));

			buffer.write(Util.complete(aux, 31, 0).getBytes("UTF-16BE"));
			buffer.write(Util.complete(Util.toString(transfer.getAmount(), currency, false), 16, 1).getBytes("UTF-16BE"));
			if (transfer instanceof TransferMemo) {
				TransferMemo transferMemo = (TransferMemo) transfer;
				buffer.write("  ".getBytes("UTF-16BE"));
				buffer.write(transferMemo.getMemo().replace('\n', ' ').getBytes("UTF-16BE"));
			}

			buffer.writeShort(0x000D);
			buffer.writeShort(0x000A);
		} catch (Exception ex) {
			Dialog.inform(ex.toString());
		}
	}

	public void writeFooter(DataBuffer buffer, Currency currency, Account account) {
		buffer.writeChars(Util.complete(_resources.getString(MoneyResource.TOTAL), 89, 1));
		buffer.writeChars(Util.complete(Util.toString(_balance, currency, false), 16, 1));
		buffer.writeShort(0x000D);
		buffer.writeShort(0x000A);
		buffer.writeShort(0x000D);
		buffer.writeShort(0x000A);
	}

	public String getExtention() {
		return ".txt";
	}

	public String toString() {
		return _name;
	}
}
