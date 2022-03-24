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

import java.util.Vector;

import com.beigebinder.data.Account;
import com.beigebinder.data.Category;
import com.beigebinder.data.Notification;
import com.beigebinder.data.SavedTransaction;
import com.beigebinder.data.ShopingList;
import com.beigebinder.data.Transaction;
import com.beigebinder.persist.AccountPersist;
import com.beigebinder.persist.CategoryPersist;
import com.beigebinder.persist.MiscellaneousPersist;
import com.beigebinder.persist.NotificationPersist;
import com.beigebinder.persist.PendingsPersist;
import com.beigebinder.persist.SavedTransactionsPersist;
import com.beigebinder.persist.ShopingListPersist;
import com.beigebinder.persist.TemplatesPersist;
import com.beigebinder.persist.TransactionPersist;
import com.beigebinder.resource.MoneyResource;
import net.rim.device.api.i18n.Locale;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.synchronization.SyncCollection;
import net.rim.device.api.synchronization.SyncConverter;
import net.rim.device.api.synchronization.SyncObject;
import net.rim.device.api.util.Arrays;

public class SyncCollectionImpl implements SyncCollection {
	private static ResourceBundle _resources = ResourceBundle.getBundle(MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);
	private SyncConverter _syncConverter;
	private boolean isInTransaction;
	private SyncObject[] _objects;
	private AccountPersist _accountPersist;
	private CategoryPersist _categoryPersist;
	private NotificationPersist _notificationPersist;
	private PendingsPersist _pendingsPersist;
	private SavedTransactionsPersist _savedTransactionsPersist;
	private TemplatesPersist _templatesPersist;
	private TransactionPersist _transactionPersist;
	private ShopingListPersist _shopingListPersist;

	public SyncCollectionImpl() {
		_syncConverter = new SyncConverterImpl();
		isInTransaction = false;
		_objects = null;
		_accountPersist = AccountPersist.getInstance();
		_categoryPersist = CategoryPersist.getInstance();
		_notificationPersist = NotificationPersist.getInstance();
		_pendingsPersist = PendingsPersist.getInstance();
		_savedTransactionsPersist = SavedTransactionsPersist.getInstance();
		_templatesPersist = TemplatesPersist.getInstance();
		_transactionPersist = TransactionPersist.getInstance();
		_shopingListPersist = ShopingListPersist.getInstance();

	}

	public boolean addSyncObject(SyncObject object) {
		if (!isInTransaction)
			return false;

		if (object instanceof Account) {
			Account account = (Account) object;
			_accountPersist.addSingle(account);
		} else if (object instanceof Category) {
			Category category = (Category) object;
			_categoryPersist.addSingle(category);
		} else if (object instanceof Notification) {
			Notification notification = (Notification) object;
			_notificationPersist.addSingle(notification);
		} else if (object instanceof SavedTransaction) {
			SavedTransaction savedTransaction = (SavedTransaction) object;
			if (savedTransaction.getUsed() == 0)
				_pendingsPersist.addSingle(savedTransaction);
			else
				_templatesPersist.addSingle(savedTransaction);
		} else if (object instanceof Transaction) {
			Transaction transaction = (Transaction) object;
			if (transaction.getParentAccount() == 0)
				_savedTransactionsPersist.addSingle(transaction);
			else
				_transactionPersist.addSingle(transaction.getParentAccount(), transaction);
		} else if (object instanceof ShopingList) {
			ShopingList shopingList = (ShopingList) object;
			_shopingListPersist.add(shopingList);
		}
		return true;
	}

	public void beginTransaction() {
		isInTransaction = true;
	}

	public void clearSyncObjectDirty(SyncObject object) {
		if (!isInTransaction)
			return;
		boolean dirty = false;
		if (object instanceof Account) {
			Account account = (Account) object;
			_accountPersist.setDirty(account, dirty);
		} else if (object instanceof Category) {
			Category category = (Category) object;
			_categoryPersist.setDirty(category, dirty);
		} else if (object instanceof Notification) {
			Notification notification = (Notification) object;
			_notificationPersist.setDirty(notification, dirty);
		} else if (object instanceof SavedTransaction) {
			SavedTransaction savedTransaction = (SavedTransaction) object;
			if (savedTransaction.getUsed() == 0)
				_pendingsPersist.setDirty(savedTransaction, dirty);
			else
				_templatesPersist.setDirty(savedTransaction, dirty);
		} else if (object instanceof Transaction) {
			Transaction transaction = (Transaction) object;
			if (transaction.getParentAccount() == 0)
				_savedTransactionsPersist.setDirty(transaction, dirty);
			else
				_transactionPersist.setDirty(transaction.getParentAccount(), transaction, dirty);
		} else if (object instanceof ShopingList) {
			ShopingList shopingList = (ShopingList) object;
			_shopingListPersist.setDirty(shopingList, dirty);
		}
	}

	public void endTransaction() {
		isInTransaction = false;
		MiscellaneousPersist.getInstance().setOK(0);
		_transactionPersist.validate();
		_accountPersist.commit();
		_categoryPersist.commit();
		_notificationPersist.commit();
		_pendingsPersist.commit();
		_savedTransactionsPersist.commit();
		_templatesPersist.commit();
		_transactionPersist.commit();
		_shopingListPersist.commit();
	}

	public SyncConverter getSyncConverter() {
		return _syncConverter;
	}

	public String getSyncName() {
		return "Money for BlackBerry by BeigeBinder";
	}

	public String getSyncName(Locale locale) {
		return _resources.getString(MoneyResource.ABOUTMESSAGE);
	}

	public SyncObject getSyncObject(int uid) {
		int index;

		Account account = new Account(uid);
		Account[] accounts = _accountPersist.get();
		index = Arrays.getIndex(accounts, account);
		if (index != -1)
			return accounts[index];

		Category category = new Category(uid);
		Category[] categories = _categoryPersist.get();
		index = Arrays.getIndex(categories, category);
		if (index != -1)
			return categories[index];

		Notification notification = new Notification(uid);
		Notification[] notifications = _notificationPersist.get();
		index = Arrays.getIndex(notifications, notification);
		if (index != -1)
			return notifications[index];

		SavedTransaction savedTransaction = new SavedTransaction(uid);
		SavedTransaction[] pendings = _pendingsPersist.get();
		index = Arrays.getIndex(pendings, savedTransaction);
		if (index != -1)
			return pendings[index];

		SavedTransaction[] templates = _templatesPersist.get();
		index = Arrays.getIndex(templates, savedTransaction);
		if (index != -1)
			return templates[index];

		Transaction transaction = new Transaction(uid);
		Transaction[] transactions = _savedTransactionsPersist.get();
		index = Arrays.getIndex(transactions, transaction);
		if (index != -1)
			return transactions[index];

		int size = accounts.length;
		for (int index2 = 0; index2 < size; index2++) {
			transactions = _transactionPersist.get(accounts[index2]);
			index = Arrays.getIndex(transactions, transaction);
			if (index != -1)
				return transactions[index];
		}

		ShopingList shopingList = new ShopingList(uid);
		ShopingList[] shopingLists = _shopingListPersist.get();
		index = Arrays.getIndex(shopingLists, shopingList);
		if (index != -1)
			return shopingLists[index];

		return null;
	}

	public int getSyncObjectCount() {
		_objects = null;
		this.getSyncObjects();
		return _objects.length;
	}

	public SyncObject[] getSyncObjects() {
		if (_objects != null)
			return _objects;

		Vector vector = new Vector();
		int index;
		int size;
		int index2;
		int size2;

		Account[] accounts = _accountPersist.get();
		size = accounts.length;
		vector.ensureCapacity(vector.size() + accounts.length);
		for (index = 0; index < size; index++)
			vector.addElement(accounts[index]);

		Category[] categories = _categoryPersist.get();
		size = categories.length;
		vector.ensureCapacity(vector.size() + categories.length);
		for (index = 0; index < size; index++)
			vector.addElement(categories[index]);

		Notification[] notifications = _notificationPersist.get();
		size = notifications.length;
		vector.ensureCapacity(vector.size() + notifications.length);
		for (index = 0; index < size; index++)
			vector.addElement(notifications[index]);

		SavedTransaction[] pendings = _pendingsPersist.get();
		size = pendings.length;
		vector.ensureCapacity(vector.size() + pendings.length);
		for (index = 0; index < size; index++)
			vector.addElement(pendings[index]);

		SavedTransaction[] templates = _templatesPersist.get();
		size = templates.length;
		vector.ensureCapacity(vector.size() + templates.length);
		for (index = 0; index < size; index++)
			vector.addElement(templates[index]);

		Transaction[] transactions = _savedTransactionsPersist.get();
		size = transactions.length;
		vector.ensureCapacity(vector.size() + transactions.length);
		for (index = 0; index < size; index++)
			vector.addElement(transactions[index]);

		size = accounts.length;
		for (index = 0; index < size; index++) {
			transactions = _transactionPersist.get(accounts[index]);
			size2 = transactions.length;
			vector.ensureCapacity(vector.size() + transactions.length);
			for (index2 = 0; index2 < size2; index2++)
				vector.addElement(transactions[index2]);
		}

		ShopingList[] shopingLists = _shopingListPersist.get();
		size = shopingLists.length;
		vector.ensureCapacity(vector.size() + shopingLists.length);
		for (index = 0; index < size; index++)
			vector.addElement(shopingLists[index]);

		_objects = new SyncObject[vector.size()];
		vector.copyInto(_objects);
		return _objects;
	}

	public int getSyncVersion() {
		return 2;
	}

	public boolean isSyncObjectDirty(SyncObject object) {
		if (object instanceof Account) {
			Account account = (Account) object;
			return account.isDirty();
		} else if (object instanceof Category) {
			Category category = (Category) object;
			return category.isDirty();
		} else if (object instanceof Notification) {
			Notification notification = (Notification) object;
			return notification.isDirty();
		} else if (object instanceof SavedTransaction) {
			SavedTransaction savedTransaction = (SavedTransaction) object;
			return savedTransaction.isDirty();
		} else if (object instanceof Transaction) {
			Transaction transaction = (Transaction) object;
			return transaction.isDirty();
		} else if (object instanceof ShopingList) {
			ShopingList shopingList = (ShopingList) object;
			return shopingList.isDirty();
		}
		return false;
	}

	public boolean removeAllSyncObjects() {
		if (!isInTransaction)
			return false;
		_accountPersist.removeAll();
		_categoryPersist.removeAll();
		_notificationPersist.removeAll();
		_pendingsPersist.removeAll();
		_savedTransactionsPersist.removeAll();
		_templatesPersist.removeAll();
		_transactionPersist.removeAll();
		_shopingListPersist.removeAll();
		return true;
	}

	public boolean removeSyncObject(SyncObject object) {
		if (!isInTransaction)
			return false;
		if (object instanceof Account) {
			Account account = (Account) object;
			_accountPersist.removeSingle(account);
		} else if (object instanceof Category) {
			Category category = (Category) object;
			_categoryPersist.removeSingle(category);
		} else if (object instanceof Notification) {
			Notification notification = (Notification) object;
			_notificationPersist.removeSingle(notification);
		} else if (object instanceof SavedTransaction) {
			SavedTransaction savedTransaction = (SavedTransaction) object;
			if (savedTransaction.getUsed() == 0)
				_pendingsPersist.removeSingle(savedTransaction);
			else
				_templatesPersist.removeSingle(savedTransaction);
		} else if (object instanceof Transaction) {
			Transaction transaction = (Transaction) object;
			if (transaction.getParentAccount() == 0)
				_savedTransactionsPersist.removeSingle(transaction);
			else
				_transactionPersist.removeSinlge(transaction.getParentAccount(), transaction);
		} else if (object instanceof ShopingList) {
			ShopingList shopingList = (ShopingList) object;
			_shopingListPersist.removeSingle(shopingList);
		}
		return true;
	}

	public void setSyncObjectDirty(SyncObject object) {
		if (!isInTransaction)
			return;
		boolean dirty = true;
		if (object instanceof Account) {
			Account account = (Account) object;
			_accountPersist.setDirty(account, dirty);
		} else if (object instanceof Category) {
			Category category = (Category) object;
			_categoryPersist.setDirty(category, dirty);
		} else if (object instanceof Notification) {
			Notification notification = (Notification) object;
			_notificationPersist.setDirty(notification, dirty);
		} else if (object instanceof SavedTransaction) {
			SavedTransaction savedTransaction = (SavedTransaction) object;
			if (savedTransaction.getUsed() == 0)
				_pendingsPersist.setDirty(savedTransaction, dirty);
			else
				_templatesPersist.setDirty(savedTransaction, dirty);
		} else if (object instanceof Transaction) {
			Transaction transaction = (Transaction) object;
			if (transaction.getParentAccount() == 0)
				_savedTransactionsPersist.setDirty(transaction, dirty);
			else
				_transactionPersist.setDirty(transaction.getParentAccount(), transaction, dirty);
		} else if (object instanceof ShopingList) {
			ShopingList shopingList = (ShopingList) object;
			_shopingListPersist.setDirty(shopingList, dirty);
		}
	}

	public boolean updateSyncObject(SyncObject oldObject, SyncObject newObject) {
		if (!isInTransaction)
			return false;
		if (oldObject instanceof Account) {
			Account oldAccount = (Account) oldObject;
			Account newAccount = (Account) newObject;
			_accountPersist.updateSingle(oldAccount, newAccount);
		} else if (oldObject instanceof Category) {
			Category oldCategory = (Category) oldObject;
			Category newCategory = (Category) newObject;
			_categoryPersist.updateSingle(oldCategory, newCategory);
		} else if (oldObject instanceof Notification) {
			Notification oldNotification = (Notification) oldObject;
			Notification newNotification = (Notification) newObject;
			_notificationPersist.updateSingle(oldNotification, newNotification);
		} else if (oldObject instanceof SavedTransaction) {
			SavedTransaction oldSavedTransaction = (SavedTransaction) oldObject;
			SavedTransaction newSavedTransaction = (SavedTransaction) newObject;
			if (oldSavedTransaction.getUsed() == 0)
				_pendingsPersist.updateSingle(oldSavedTransaction, newSavedTransaction);
			else
				_templatesPersist.updateSingle(oldSavedTransaction, newSavedTransaction);
		} else if (oldObject instanceof Transaction) {
			Transaction oldTransaction = (Transaction) oldObject;
			Transaction newTransaction = (Transaction) newObject;
			if (oldTransaction.getParentAccount() == 0)
				_savedTransactionsPersist.updateSingle(oldTransaction, newTransaction);
			else
				_transactionPersist.updateSinlge(oldTransaction.getParentAccount(), oldTransaction, newTransaction);
		} else if (oldObject instanceof ShopingList) {
			ShopingList oldShopingList = (ShopingList) oldObject;
			ShopingList newShopingList = (ShopingList) newObject;
			_shopingListPersist.removeSingle(oldShopingList);
			_shopingListPersist.addSingle(newShopingList);
		}
		return true;
	}
}
