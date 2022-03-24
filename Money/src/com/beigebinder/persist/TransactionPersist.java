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
package com.beigebinder.persist;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.beigebinder.data.Account;
import com.beigebinder.data.ExpenseExchangeRate;
import com.beigebinder.data.IncomeExchangeRate;
import com.beigebinder.data.Transaction;
import com.beigebinder.data.Transfer;
import com.beigebinder.misc.Util;
import net.rim.device.api.system.ObjectGroup;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.StringUtilities;

public final class TransactionPersist {
	private static final long TRANSACTIONS = 0x89a2b7d005fec373L; // com.beigebinder.logic.TransactionPersist.TRANSACTIONS
	private static final String TRANSACTIONSSTRING = "com.beigebinder.logic.TransactionPersist.TRANSACTIONS.";

	private PersistentObject _transactionsStore;
	private Transaction[] _transactions;
	private Hashtable _transactionStoreHashtable;
	private Hashtable _transactionsHashtable;

	private TransactionPersist() {
		_transactionStoreHashtable = new Hashtable();
		_transactionsHashtable = new Hashtable();
	}

	public static TransactionPersist getInstance() {
		RuntimeStore runtimeStore = RuntimeStore.getRuntimeStore();
		TransactionPersist transactionLogic = (TransactionPersist) runtimeStore.get(TRANSACTIONS);
		if (transactionLogic == null) {
			transactionLogic = new TransactionPersist();
			runtimeStore.put(TRANSACTIONS, transactionLogic);
		}
		return transactionLogic;
	}

	private void setStore(int accountId) {
		Integer id = new Integer(accountId);
		if (_transactionStoreHashtable.containsKey(id)) {
			_transactionsStore = (PersistentObject) _transactionStoreHashtable.get(id);
			_transactions = (Transaction[]) _transactionsHashtable.get(id);

		} else {
			long storeID = StringUtilities.stringHashToLong(TRANSACTIONSSTRING + id.toString());
			_transactionsStore = PersistentStore.getPersistentObject(storeID);
			_transactionStoreHashtable.put(id, _transactionsStore);

			_transactions = (Transaction[]) _transactionsStore.getContents();
			if (_transactions == null) {
				_transactions = new Transaction[0];
				_transactionsStore.setContents(_transactions);
			}
			_transactionsHashtable.put(id, _transactions);
		}
	}

	private void addSingle(Account account, Transaction transaction) {
		long amount = transaction.getAmount();
		if (transaction instanceof ExpenseExchangeRate) {
			ExpenseExchangeRate exchangeRate = (ExpenseExchangeRate) transaction;
			amount = exchangeRate.getDestAmount();
		} else if (transaction instanceof IncomeExchangeRate) {
			IncomeExchangeRate exchangeRate = (IncomeExchangeRate) transaction;
			amount = exchangeRate.getDestAmount();
		}

		this.addSingle(account.getUID(), transaction);
		Arrays.sort(_transactions, transaction);
		AccountPersist.getInstance().updateBalance(account, 0, amount, (byte) 3, transaction.getStatus());
		_transactionsStore.commit();
	}

	private void removeSinlge(Account account, Transaction transaction) {
		long amount = transaction.getAmount();
		if (transaction instanceof ExpenseExchangeRate) {
			ExpenseExchangeRate exchangeRate = (ExpenseExchangeRate) transaction;
			amount = exchangeRate.getDestAmount();
		} else if (transaction instanceof IncomeExchangeRate) {
			IncomeExchangeRate exchangeRate = (IncomeExchangeRate) transaction;
			amount = exchangeRate.getDestAmount();
		}

		this.removeSinlge(account.getUID(), transaction);
		AccountPersist.getInstance().updateBalance(account, amount, 0, transaction.getStatus(), (byte) 3);
		_transactionsStore.commit();
	}

	public void add(Account account, Transaction transaction, long mirrorAmount) {
		this.addSingle(account, transaction);
		if (transaction instanceof Transfer) {
			Transfer newTransfer = (Transfer) transaction.clone();
			int idta = newTransfer.getMirrorAccount();
			newTransfer.setAmount(mirrorAmount);
			newTransfer.setMirrorAccount(account.getUID());
			this.addSingle(new Account(idta), newTransfer);
		}
	}

	public void update(Account oldAccount, Transaction oldTransaction, Account newAccount, Transaction newTransaction, long mirrorAmount) {
		this.remove(oldAccount, oldTransaction);
		this.add(newAccount, newTransaction, mirrorAmount);
	}

	public void remove(Account account, Transaction transaction) {
		removeSinlge(account, transaction);
		if (transaction instanceof Transfer) {
			Transfer transfer = (Transfer) transaction;
			Account transferAccount = new Account(transfer.getMirrorAccount());
			transfer = (Transfer) getMirrorTransaction(transaction);
			this.removeSinlge(transferAccount, transfer);
		}
	}

	public Transaction getMirrorTransaction(Transaction transaction) {
		Transfer transfer = (Transfer) transaction;
		this.setStore(transfer.getMirrorAccount());
		int index = Arrays.getIndex(_transactions, transaction);
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		return _transactions[index];
	}

	public void remove(Account account) {
		int accountID = account.getUID();
		int mirrorAccountID;
		int index;
		int[] ids = new int[0];
		setStore(accountID);
		int size = _transactions.length;
		Transfer transfer;
		for (index = 0; index < size; index++) {
			if (_transactions[index] instanceof Transfer) {
				transfer = (Transfer) _transactions[index];
				mirrorAccountID = transfer.getMirrorAccount();
				setStore(mirrorAccountID);
				Arrays.remove(_transactions, transfer);
				if (!Util.contains(ids, mirrorAccountID))
					Arrays.add(ids, mirrorAccountID);
				setStore(accountID);
			}
		}
		size = ids.length;
		for (index = 0; index < size; index++) {
			this.setStore(ids[index]);
			_transactionsStore.commit();
			UpdateAccountBalance(ids[index]);
		}

		Integer id = new Integer(accountID);
		long storeID = StringUtilities.stringHashToLong(TRANSACTIONSSTRING + id.toString());
		PersistentStore.destroyPersistentObject(storeID);

		if (_transactionStoreHashtable.containsKey(id)) {
			_transactionStoreHashtable.remove(id);
			_transactionsHashtable.remove(id);
		}
	}

	public void updateStatus(Account account, Transaction oldTransaction, byte status) {
		this.setStore(account.getUID());
		int index = Arrays.getIndex(_transactions, oldTransaction);
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		Transaction ungroupedTransaction = (Transaction) ObjectGroup.expandGroup(_transactions[index]);
		ungroupedTransaction.setStatus(status);
		ObjectGroup.createGroup(ungroupedTransaction);
		_transactions[index] = ungroupedTransaction;
		_transactionsStore.commit();
	}

	public void purgue(Account account, long date, boolean restart) {
		int accountID = account.getUID();
		setStore(accountID);
		int size = _transactions.length;
		long initialBalance = account.getInitialBalance();
		long finalBalance = 0;
		long reconciledBalance = 0;
		long clearedBalance = 0;
		long amount;
		int index;
		for (index = 0; index < size;) {
			if (date < _transactions[index].getDate()) {
				amount = _transactions[index].getAmount();
				switch (_transactions[index].getStatus()) {
				case 2:
					reconciledBalance += amount;
				case 1:
					clearedBalance += amount;
				case 0:
					finalBalance += amount;
				}
				index++;
			} else {
				size--;
				initialBalance += _transactions[index].getAmount();
				Arrays.remove(_transactions, _transactions[index]);
			}
		}
		_transactionsStore.commit();
		if (restart)
			initialBalance = 0;
		finalBalance += initialBalance;
		clearedBalance += initialBalance;
		reconciledBalance += initialBalance;

		Account newAccount = new Account();
		newAccount.setDescription(account.getDescription());
		newAccount.setType(account.getType());
		newAccount.setInitialBalance(initialBalance);
		newAccount.setFinalBalance(finalBalance);
		newAccount.setClearedBalance(clearedBalance);
		newAccount.setReconciledBalance(reconciledBalance);
		newAccount.setCurrencyID(account.getCurrencyID());
		newAccount.setMemo(account.getMemo());
		AccountPersist.getInstance().update(account, newAccount);
	}

	public void UpdateAccountBalance(int accountID) {
		setStore(accountID);
		AccountPersist accountLogic = AccountPersist.getInstance();
		Account account;
		long finalBalance = 0;
		long reconciledBalance = 0;
		long clearedBalance = 0;
		long amount;
		int size = _transactions.length;
		for (int index = 0; index < size; index++) {
			amount = _transactions[index].getAmount();
			switch (_transactions[index].getStatus()) {
			case 2:
				reconciledBalance += amount;
			case 1:
				clearedBalance += amount;
			case 0:
				finalBalance += amount;
			}
		}
		account = accountLogic.get(accountID);
		finalBalance += account.getInitialBalance();
		clearedBalance += account.getInitialBalance();
		reconciledBalance += account.getInitialBalance();
		accountLogic.updateFinalBalance(account, finalBalance, clearedBalance, reconciledBalance);
	}

	public Transaction[] get(Account account) {
		this.setStore(account.getUID());
		return _transactions;
	}

	public Transaction get(Account account, int id) {
		this.setStore(account.getUID());
		Transaction transaction = new Transaction(id);
		int index = Arrays.getIndex(_transactions, transaction);
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		return _transactions[index];
	}

	public boolean exist(Account account, Transaction transaction) {
		if (transaction == null)
			return false;
		this.setStore(account.getUID());
		int index = Arrays.getIndex(_transactions, transaction);
		return index != -1;
	}

	/***************************************************************************************/

	public void addSingle(int accountId, Transaction transaction) {
		this.setStore(accountId);
		if (ObjectGroup.isInGroup(transaction))
			transaction = (Transaction) ObjectGroup.expandGroup(transaction);
		transaction.setParentAccount(accountId);
		ObjectGroup.createGroup(transaction);
		Arrays.add(_transactions, transaction);
	}

	public void removeSinlge(int accountId, Transaction transaction) {
		this.setStore(accountId);
		Arrays.remove(_transactions, transaction);
	}

	public void updateSinlge(int accountId, Transaction oldTransaction, Transaction newTransaction) {
		this.setStore(accountId);
		Arrays.remove(_transactions, oldTransaction);
		if (ObjectGroup.isInGroup(newTransaction))
			newTransaction = (Transaction) ObjectGroup.expandGroup(newTransaction);
		newTransaction.setParentAccount(accountId);
		ObjectGroup.createGroup(newTransaction);
		Arrays.add(_transactions, newTransaction);
	}

	public void setDirty(int accountId, Transaction transaction, boolean dirty) {
		this.setStore(accountId);
		if (transaction == null) {
			throw new IllegalArgumentException();
		}
		int index = Arrays.getIndex(_transactions, transaction);
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		Transaction ungroupedTransaction = (Transaction) ObjectGroup.expandGroup(_transactions[index]);
		ungroupedTransaction.setDirty(dirty);
		ObjectGroup.createGroup(ungroupedTransaction);
		_transactions[index] = ungroupedTransaction;
	}

	public void removeSingle(int accountId) {
		Integer id = new Integer(accountId);
		long storeID = StringUtilities.stringHashToLong(TRANSACTIONSSTRING + id.toString());
		PersistentStore.destroyPersistentObject(storeID);

		if (_transactionStoreHashtable.containsKey(id)) {
			_transactionStoreHashtable.remove(id);
			_transactionsHashtable.remove(id);
		}
	}

	public void removeAll() {
		Integer id;
		long storeID;
		Enumeration enumeration = _transactionStoreHashtable.keys();
		while (enumeration.hasMoreElements()) {
			id = (Integer) enumeration.nextElement();
			storeID = StringUtilities.stringHashToLong(TRANSACTIONSSTRING + id.toString());
			PersistentStore.destroyPersistentObject(storeID);
			_transactionStoreHashtable.remove(id);
			_transactionsHashtable.remove(id);
		}
	}

	public void validate() {
		AccountPersist accountPersist = AccountPersist.getInstance();
		Integer id;
		long storeID;
		int size;
		int index;
		Enumeration enumeration = _transactionStoreHashtable.keys();
		while (enumeration.hasMoreElements()) {
			id = (Integer) enumeration.nextElement();
			if (!accountPersist.exist(id.intValue())) {
				storeID = StringUtilities.stringHashToLong(TRANSACTIONSSTRING + id.toString());
				PersistentStore.destroyPersistentObject(storeID);
				_transactionStoreHashtable.remove(id);
				_transactionsHashtable.remove(id);
			}
		}
		Vector vector;
		Transfer transfer;
		Transaction[] transactions;
		Transaction transaction;
		enumeration = _transactionStoreHashtable.keys();
		while (enumeration.hasMoreElements()) {
			vector = new Vector();
			id = (Integer) enumeration.nextElement();
			this.setStore(id.intValue());
			size = _transactions.length;
			for (index = 0; index < size; index++) {
				if (_transactions[index] instanceof Transfer) {
					transfer = (Transfer) _transactions[index];
					transactions = (Transaction[]) _transactionsHashtable.get(new Integer(transfer.getMirrorAccount()));
					if (!Arrays.contains(transactions, _transactions[index])) {
						vector.addElement(_transactions[index]);
					}
				}
			}
			size = vector.size();
			for (index = 0; index < size; index++) {
				transaction = (Transaction) vector.elementAt(index);
				Arrays.remove(_transactions, transaction);
			}
		}
	}

	public void commit() {
		Integer id;
		Transaction transaction = new Transaction();
		Enumeration enumeration = _transactionStoreHashtable.keys();
		while (enumeration.hasMoreElements()) {
			id = (Integer) enumeration.nextElement();
			this.setStore(id.intValue());
			this.UpdateAccountBalance(id.intValue());
			Arrays.sort(_transactions, transaction);
			_transactionsStore.commit();
		}
	}

}
