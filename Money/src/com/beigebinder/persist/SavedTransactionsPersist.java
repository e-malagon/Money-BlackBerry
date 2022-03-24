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

import com.beigebinder.data.Transaction;
import net.rim.device.api.system.ObjectGroup;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.util.Arrays;

public class SavedTransactionsPersist {
	private static final long TRANSACTIONPERSIST = 0x31bd827f35b7ddedL; // com.beigebinder.logic.SavedTransactionsPersist.TRANSACTIONPERSIST
	private static final long TRANSACTIONS = 0x867c6fba4158c166L; // com.beigebinder.logic.SavedTransactionsPersist.TRANSACTIONS

	private PersistentObject _transactionsStore;
	private Transaction[] _transactions;

	private SavedTransactionsPersist() {
		_transactionsStore = PersistentStore.getPersistentObject(TRANSACTIONPERSIST);
		_transactions = (Transaction[]) _transactionsStore.getContents();
		if (_transactions == null) {
			_transactions = new Transaction[0];
			_transactionsStore.setContents(_transactions);
		}
	}

	public static SavedTransactionsPersist getInstance() {
		RuntimeStore runtimeStore = RuntimeStore.getRuntimeStore();
		SavedTransactionsPersist savedTransactionsPersist = (SavedTransactionsPersist) runtimeStore.get(TRANSACTIONS);
		if (savedTransactionsPersist == null) {
			savedTransactionsPersist = new SavedTransactionsPersist();
			runtimeStore.put(TRANSACTIONS, savedTransactionsPersist);
		}
		return savedTransactionsPersist;
	}

	public void add(Transaction transaction) {
		this.addSingle(transaction);
		this.commit();
	}

	public void remove(Transaction transaction) {
		this.removeSingle(transaction);
		this.commit();
	}

	public void update(Transaction oldTransaction, Transaction newTransaction) {
		this.updateSingle(oldTransaction, newTransaction);
		this.commit();
	}

	public Transaction get(int id) {
		Transaction transaction = new Transaction(id);
		int index = Arrays.getIndex(_transactions, transaction);
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		return _transactions[index];
	}

	public Transaction[] get() {
		return _transactions;
	}

	/***************************************************************************************/

	public void addSingle(Transaction transaction) {
		ObjectGroup.createGroup(transaction);
		Arrays.add(_transactions, transaction);
	}

	public void removeSingle(Transaction transaction) {
		Arrays.remove(_transactions, transaction);
	}

	public void updateSingle(Transaction oldTransaction, Transaction newTransaction) {
		Arrays.remove(_transactions, oldTransaction);
		ObjectGroup.createGroup(newTransaction);
		Arrays.add(_transactions, newTransaction);
	}

	public void removeAll() {
		_transactions = new Transaction[0];
		_transactionsStore.setContents(_transactions);
	}

	public void setDirty(Transaction transaction, boolean dirty) {
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

	public void commit() {
		_transactionsStore.commit();
	}
}
