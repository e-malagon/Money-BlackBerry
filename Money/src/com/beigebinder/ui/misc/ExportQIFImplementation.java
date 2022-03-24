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
import com.beigebinder.persist.TransactionPersist;
import com.beigebinder.resource.MoneyResource;
import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.util.DataBuffer;

public class ExportQIFImplementation implements IExport {
	private CategoryPersist categoryPersist;
	private AccountPersist accountLogic;
	private DateFormat sdFormat;
	private String _name;

	private static ResourceBundle _resources = ResourceBundle.getBundle(MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public ExportQIFImplementation(int dateFormat) {
		accountLogic = AccountPersist.getInstance();
		categoryPersist = CategoryPersist.getInstance();
		if (dateFormat == 0) {
			sdFormat = new SimpleDateFormat("MM/dd''yyyy");
			_name = _resources.getStringArray(MoneyResource.EXPORTTYPES)[1];
		} else {
			sdFormat = new SimpleDateFormat("dd/MM''yyyy");
			_name = _resources.getStringArray(MoneyResource.EXPORTTYPES)[2];
		}
	}

	public void writeHeader(DataBuffer buffer, Account account, Currency currency, boolean includeInitialBalance) {
		try {
			switch (account.getType()) {
			case 0:// Activo
				buffer.write("!Type:Oth A".getBytes("UTF-8"));
				break;
			case 1:// Efectivo
				buffer.write("!Type:Cash".getBytes("UTF-8"));
				break;
			case 2:// Cheques
			case 3:// Banco
			case 4:// Ahorro
				buffer.write("!Type:Bank".getBytes("UTF-8"));
				break;
			case 5:// Inversiones
				buffer.write("!Type:Invst".getBytes("UTF-8"));
				break;
			case 6:// Pasivo
			case 7:// Linea de Credito
			case 9:// Prestamo
				buffer.write("!Type:Oth L".getBytes("UTF-8"));
				break;
			case 8:// Tarjeta de Credito
				buffer.write("!Type:CCard".getBytes("UTF-8"));
				break;
			}
			buffer.writeByte(0x0D);
			buffer.writeByte(0x0A);
			if (includeInitialBalance) {
				long date;
				Transaction[] transactions = TransactionPersist.getInstance().get(account);
				if (transactions.length > 0) {
					date = transactions[transactions.length - 1].getDate();
				} else {
					date = System.currentTimeMillis();
				}
				buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_D);
				buffer.write(sdFormat.formatLocal(date).getBytes("UTF-8"));
				buffer.writeByte(0x0D);
				buffer.writeByte(0x0A);
				buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_T);
				buffer.write(Util.toString(account.getInitialBalance(), currency, true).getBytes("UTF-8"));
				buffer.writeByte(0x0D);
				buffer.writeByte(0x0A);
				buffer.write("CX".getBytes("UTF-8"));
				buffer.writeByte(0x0D);
				buffer.writeByte(0x0A);
				buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_P);
				buffer.write(_resources.getString(MoneyResource.OPENINGBALANCE).getBytes("UTF-8"));
				buffer.writeByte(0x0D);
				buffer.writeByte(0x0A);
				buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_L);
				buffer.write(account.getDescription().getBytes("UTF-8"));
				buffer.writeByte(0x0D);
				buffer.writeByte(0x0A);
				buffer.writeByte(Characters.CIRCUMFLEX_ACCENT);
				buffer.writeByte(0x0D);
				buffer.writeByte(0x0A);
			}
		} catch (Exception ex) {
			Dialog.inform(ex.toString());
		}
	}

	public void writeTransaction(DataBuffer buffer, Currency currency, Transaction transaction) {
		try {
			buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_D);
			buffer.write(sdFormat.formatLocal(transaction.getDate()).getBytes("UTF-8"));
			buffer.writeByte(0x0D);
			buffer.writeByte(0x0A);
			switch (transaction.getStatus()) {
			case 1:
				buffer.write("C*".getBytes("UTF-8"));
				buffer.writeByte(0x0D);
				buffer.writeByte(0x0A);
				break;
			case 2:
				buffer.write("CX".getBytes("UTF-8"));
				buffer.writeByte(0x0D);
				buffer.writeByte(0x0A);
				break;
			}
		} catch (Exception ex) {
			Dialog.inform(ex.toString());
		}
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
		buffer.writeByte(Characters.CIRCUMFLEX_ACCENT);
		buffer.writeByte(0x0D);
		buffer.writeByte(0x0A);
	}

	private void writeTransaction(DataBuffer buffer, Currency currency, Expense expense) {
		int sizet = expense.getAccounts().length;
		long[] amounts = expense.getAmounts();
		String[] descriptions = expense.getDescriptions();
		int[] accounts = expense.getAccounts();
		try {
			buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_T);
			buffer.write(Util.toString(expense.getAmount(), currency, true).getBytes("UTF-8"));
			buffer.writeByte(0x0D);
			buffer.writeByte(0x0A);
			buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_N);
			buffer.write(expense.getNumber().getBytes("UTF-8"));
			buffer.writeByte(0x0D);
			buffer.writeByte(0x0A);
			buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_P);
			buffer.write(expense.getDescription().getBytes("UTF-8"));
			buffer.writeByte(0x0D);
			buffer.writeByte(0x0A);
			if (expense instanceof ExpenseMemo) {
				ExpenseMemo expenseMemo = (ExpenseMemo) expense;
				buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_M);
				buffer.write(expenseMemo.getMemo().replace('\n', ' ').getBytes("UTF-8"));
				buffer.writeByte(0x0D);
				buffer.writeByte(0x0A);
			}
			buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_L);
			buffer.write(categoryPersist.get(accounts[0], 0).getDescription().getBytes("UTF-8"));
			buffer.writeByte(0x0D);
			buffer.writeByte(0x0A);
			if (sizet > 1) {
				for (int i = 0; i < sizet; i++) {
					buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_S);
					buffer.write(categoryPersist.get(accounts[i], 0).getDescription().getBytes("UTF-8"));
					buffer.writeByte(0x0D);
					buffer.writeByte(0x0A);
					buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_E);
					buffer.write(descriptions[i].getBytes("UTF-8"));
					buffer.writeByte(0x0D);
					buffer.writeByte(0x0A);
					buffer.writeByte(Characters.DOLLAR_SIGN);
					buffer.write(Util.toString(amounts[i] * -1, currency, true).getBytes("UTF-8"));
					buffer.writeByte(0x0D);
					buffer.writeByte(0x0A);
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
		long tmpAmount = 0;
		long tmpTotal = 0;
		Currency exchangeRateCurrency = GetExchangeRateScreen.getExchangeRateCurrency();
		Currency destCurrency = CurrencyPersist.getInstance().get(expense.getCurrencyID());
		long exchangeRate = Util.toLong(Util.amountToExchange(expense.getDestAmount(), expense.getAmount()), exchangeRateCurrency);
		try {
			buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_T);
			buffer.write(Util.toString(expense.getDestAmount(), currency, true).getBytes("UTF-8"));
			buffer.writeByte(0x0D);
			buffer.writeByte(0x0A);
			buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_N);
			buffer.write(expense.getNumber().getBytes("UTF-8"));
			buffer.writeByte(0x0D);
			buffer.writeByte(0x0A);
			buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_P);
			buffer.write(expense.getDescription().getBytes("UTF-8"));
			buffer.writeByte(0x0D);
			buffer.writeByte(0x0A);
			if (expense instanceof ExpenseExchangeRateMemo) {
				ExpenseExchangeRateMemo expenseMemo = (ExpenseExchangeRateMemo) expense;
				buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_M);
				buffer.write(expenseMemo.getMemo().replace('\n', ' ').getBytes("UTF-8"));
				buffer.writeByte(0x0D);
				buffer.writeByte(0x0A);
			}
			buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_L);
			buffer.write(categoryPersist.get(accounts[0], 0).getDescription().getBytes("UTF-8"));
			buffer.writeByte(0x0D);
			buffer.writeByte(0x0A);

			if (sizet > 1) {
				int i;
				for (i = 0; i < sizet - 1; i++) {
					tmpAmount = Util.exchangeToAmount(amounts[i] * -1, exchangeRate, currency, exchangeRateCurrency, destCurrency);
					tmpTotal += tmpAmount;
					buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_S);
					buffer.write(categoryPersist.get(accounts[i], 0).getDescription().getBytes("UTF-8"));
					buffer.writeByte(0x0D);
					buffer.writeByte(0x0A);
					buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_E);
					buffer.write(descriptions[i].getBytes("UTF-8"));
					buffer.writeByte(0x0D);
					buffer.writeByte(0x0A);
					buffer.writeByte(Characters.DOLLAR_SIGN);
					buffer.write(Util.toString(tmpAmount, destCurrency, true).getBytes("UTF-8"));
					buffer.writeByte(0x0D);
					buffer.writeByte(0x0A);
				}
				tmpAmount = Util.exchangeToAmount(amounts[i] * -1, exchangeRate, currency, exchangeRateCurrency, destCurrency);
				tmpTotal += tmpAmount;
				buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_S);
				buffer.write(categoryPersist.get(accounts[i], 0).getDescription().getBytes("UTF-8"));
				buffer.writeByte(0x0D);
				buffer.writeByte(0x0A);
				buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_E);
				buffer.write(descriptions[i].getBytes("UTF-8"));
				buffer.writeByte(0x0D);
				buffer.writeByte(0x0A);
				buffer.writeByte(Characters.DOLLAR_SIGN);
				buffer.write(Util.toString(tmpAmount - (Math.abs(expense.getDestAmount()) - Math.abs(tmpTotal)), currency, true).getBytes("UTF-8"));
				buffer.writeByte(0x0D);
				buffer.writeByte(0x0A);
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

		try {
			buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_T);
			buffer.write(Util.toString(income.getAmount(), currency, true).getBytes("UTF-8"));
			buffer.writeByte(0x0D);
			buffer.writeByte(0x0A);
			buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_N);
			buffer.write(income.getNumber().getBytes("UTF-8"));
			buffer.writeByte(0x0D);
			buffer.writeByte(0x0A);
			buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_P);
			buffer.write(income.getDescription().getBytes("UTF-8"));
			buffer.writeByte(0x0D);
			buffer.writeByte(0x0A);

			if (income instanceof IncomeMemo) {
				IncomeMemo incomeMemo = (IncomeMemo) income;
				buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_M);
				buffer.write(incomeMemo.getMemo().replace('\n', ' ').getBytes("UTF-8"));
				buffer.writeByte(0x0D);
				buffer.writeByte(0x0A);
			}

			buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_L);
			buffer.write(categoryPersist.get(accounts[0], 1).getDescription().getBytes("UTF-8"));
			buffer.writeByte(0x0D);
			buffer.writeByte(0x0A);

			if (sizet > 1) {
				for (int i = 0; i < sizet; i++) {
					buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_S);
					buffer.write(categoryPersist.get(accounts[i], 1).getDescription().getBytes("UTF-8"));
					buffer.writeByte(0x0D);
					buffer.writeByte(0x0A);
					buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_E);
					buffer.write(descriptions[i].getBytes("UTF-8"));
					buffer.writeByte(0x0D);
					buffer.writeByte(0x0A);
					buffer.writeByte(Characters.DOLLAR_SIGN);
					buffer.write(Util.toString(amounts[i], currency, true).getBytes("UTF-8"));
					buffer.writeByte(0x0D);
					buffer.writeByte(0x0A);
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
		long tmpAmount = 0;
		long tmpTotal = 0;
		Currency exchangeRateCurrency = GetExchangeRateScreen.getExchangeRateCurrency();
		Currency destCurrency = CurrencyPersist.getInstance().get(income.getCurrencyID());
		long exchangeRate = Util.toLong(Util.amountToExchange(income.getDestAmount(), income.getAmount()), exchangeRateCurrency);

		try {
			buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_T);
			buffer.write(Util.toString(income.getDestAmount(), currency, true).getBytes("UTF-8"));
			buffer.writeByte(0x0D);
			buffer.writeByte(0x0A);
			buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_N);
			buffer.write(income.getNumber().getBytes("UTF-8"));
			buffer.writeByte(0x0D);
			buffer.writeByte(0x0A);
			buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_P);
			buffer.write(income.getDescription().getBytes("UTF-8"));
			buffer.writeByte(0x0D);
			buffer.writeByte(0x0A);

			if (income instanceof IncomeExchangeRateMemo) {
				IncomeExchangeRateMemo incomeMemo = (IncomeExchangeRateMemo) income;
				buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_M);
				buffer.write(incomeMemo.getMemo().replace('\n', ' ').getBytes("UTF-8"));
				buffer.writeByte(0x0D);
				buffer.writeByte(0x0A);
			}

			buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_L);
			buffer.write(categoryPersist.get(accounts[0], 1).getDescription().getBytes("UTF-8"));
			buffer.writeByte(0x0D);
			buffer.writeByte(0x0A);

			if (sizet > 1) {
				int i;
				for (i = 0; i < sizet - 1; i++) {
					tmpAmount = Util.exchangeToAmount(amounts[i], exchangeRate, currency, exchangeRateCurrency, destCurrency);
					tmpTotal += tmpAmount;
					buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_S);
					buffer.write(categoryPersist.get(accounts[i], 1).getDescription().getBytes("UTF-8"));
					buffer.writeByte(0x0D);
					buffer.writeByte(0x0A);
					buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_E);
					buffer.write(descriptions[i].getBytes("UTF-8"));
					buffer.writeByte(0x0D);
					buffer.writeByte(0x0A);
					buffer.writeByte(Characters.DOLLAR_SIGN);
					buffer.write(Util.toString(tmpAmount, currency, true).getBytes("UTF-8"));
					buffer.writeByte(0x0D);
					buffer.writeByte(0x0A);
				}
				tmpAmount = Util.exchangeToAmount(amounts[i], exchangeRate, currency, exchangeRateCurrency, destCurrency);
				tmpTotal += tmpAmount;
				buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_S);
				buffer.write(categoryPersist.get(accounts[i], 1).getDescription().getBytes("UTF-8"));
				buffer.writeByte(0x0D);
				buffer.writeByte(0x0A);
				buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_E);
				buffer.write(descriptions[i].getBytes("UTF-8"));
				buffer.writeByte(0x0D);
				buffer.writeByte(0x0A);
				buffer.writeByte(Characters.DOLLAR_SIGN);
				buffer.write(Util.toString(tmpAmount + (Math.abs(income.getDestAmount()) - Math.abs(tmpTotal)), currency, true).getBytes("UTF-8"));
				buffer.writeByte(0x0D);
				buffer.writeByte(0x0A);
			}
		} catch (Exception ex) {
			Dialog.inform(ex.toString());
		}
	}

	private void writeTransaction(DataBuffer buffer, Currency currency, Transfer transfer) {
		try {
			buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_T);
			buffer.write(Util.toString(transfer.getAmount(), currency, true).getBytes("UTF-8"));
			buffer.writeByte(0x0D);
			buffer.writeByte(0x0A);
			buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_N);
			buffer.write(transfer.getNumber().getBytes("UTF-8"));
			buffer.writeByte(0x0D);
			buffer.writeByte(0x0A);
			buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_P);
			buffer.write(transfer.getDescription().getBytes("UTF-8"));
			buffer.writeByte(0x0D);
			buffer.writeByte(0x0A);

			if (transfer instanceof TransferMemo) {
				TransferMemo transferMemo = (TransferMemo) transfer;
				buffer.writeByte(Characters.LATIN_CAPITAL_LETTER_M);
				buffer.write(transferMemo.getMemo().replace('\n', ' ').getBytes("UTF-8"));
				buffer.writeByte(0x0D);
				buffer.writeByte(0x0A);
			}

			transfer = (Transfer) transfer;
			buffer.write("L[".getBytes("UTF-8"));
			buffer.write(accountLogic.get(transfer.getMirrorAccount()).getDescription().getBytes("UTF-8"));
			buffer.write("]".getBytes("UTF-8"));
			buffer.writeByte(0x0D);
			buffer.writeByte(0x0A);
		} catch (Exception ex) {
			Dialog.inform(ex.toString());
		}
	}

	public void writeFooter(DataBuffer buffer, Currency currency, Account account) {
	}

	public String getExtention() {
		return ".qif";
	}

	public String toString() {
		return _name;
	}
}
