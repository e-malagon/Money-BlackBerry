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
package com.beigebinder.persist;

import java.io.UnsupportedEncodingException;

import com.beigebinder.data.Account;
import com.beigebinder.data.Miscellaneous;
import com.beigebinder.resource.MoneyResource;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.CodeModuleGroup;
import net.rim.device.api.system.CodeModuleGroupManager;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.ObjectGroup;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.DateTimeUtilities;

public final class AccountPersist {
	private static final long ACCOUNTSPERSIST = 0x83b67f23c5ce765bL; // com.beigebinder.logic.AccountPersist.ACCOUNTSPERSIST
	private static final long ACCOUNTS = 0xa4579f1d6621df3cL; // com.beigebinder.logic.AccountPersist.ACCOUNTS
	private static final long INDEXPERSIST = 0x66fe1e2ca9d4c4f7L; // com.beigebinder.logic.AccountPersist.INDEXPERSIST
	private static ResourceBundle _resources = ResourceBundle.getBundle(MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	private PersistentObject _accountStore;
	private Account[] _accounts;

	private PersistentObject _accountIndex;

	private AccountPersist() {
		_accountIndex = PersistentStore.getPersistentObject(INDEXPERSIST);
		_accountStore = PersistentStore.getPersistentObject(ACCOUNTSPERSIST);
		_accounts = (Account[]) _accountStore.getContents();
		if (_accounts == null) {
			_accounts = new Account[0];
			_accountStore.setContents(_accounts);
		}
	}

	public static AccountPersist getInstance() {
		RuntimeStore runtimeStore = RuntimeStore.getRuntimeStore();
		AccountPersist accountLogic = (AccountPersist) runtimeStore.get(ACCOUNTS);
		if (accountLogic == null) {
			accountLogic = new AccountPersist();
			runtimeStore.put(ACCOUNTS, accountLogic);
		}
		return accountLogic;
	}

	public void add(Account account) {
		this.addSingle(account);
		this.commit();
	}

	public void update(Account oldAccount, Account newAccount) {
		this.updateSingle(oldAccount, newAccount);
		this.commit();
	}

	public void remove(Account account) {
		this.removeSingle(account);
		TransactionPersist.getInstance().remove(account);
		this.commit();
	}

	public void updateClosed(Account oldAccount, boolean closed) {
		if (oldAccount == null) {
			throw new IllegalArgumentException();
		}

		int index = Arrays.getIndex(_accounts, oldAccount);
		if (index == -1) {
			throw new IllegalArgumentException();
		}

		Account ungroupedAccount = (Account) ObjectGroup.expandGroup(_accounts[index]);
		ungroupedAccount.setClosed(closed);
		ObjectGroup.createGroup(ungroupedAccount);
		_accounts[index] = ungroupedAccount;
		this.commit();
	}

	public void updateBalance(Account account, long oldAmount, long newAmount, byte oldStatus, byte newStatus) {
		if (account == null) {
			throw new IllegalArgumentException();
		}

		int index = Arrays.getIndex(_accounts, account);
		if (index == -1) {
			throw new IllegalArgumentException();
		}

		Account ungroupedAccount = (Account) ObjectGroup.expandGroup(_accounts[index]);
		long finalBalance = ungroupedAccount.getFinalBalance();
		long clearedBalance = ungroupedAccount.getClearedBalance();
		long reconciledBalance = ungroupedAccount.getReconciledBalance();

		switch (oldStatus) {
		case 2:
			reconciledBalance -= oldAmount;
		case 1:
			clearedBalance -= oldAmount;
		case 0:
			finalBalance -= oldAmount;
		}

		switch (newStatus) {
		case 2:
			reconciledBalance += newAmount;
		case 1:
			clearedBalance += newAmount;
		case 0:
			finalBalance += newAmount;
		}
		ungroupedAccount.setFinalBalance(finalBalance);
		ungroupedAccount.setClearedBalance(clearedBalance);
		ungroupedAccount.setReconciledBalance(reconciledBalance);
		ObjectGroup.createGroup(ungroupedAccount);
		_accounts[index] = ungroupedAccount;
		this.commit();
	}

	public void updateFinalBalance(Account account, long finalBalance, long clearedBalance, long reconciledBalance) {
		if (account == null) {
			throw new IllegalArgumentException();
		}
		int index = Arrays.getIndex(_accounts, account);
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		Account ungroupedAccount = (Account) ObjectGroup.expandGroup(_accounts[index]);
		ungroupedAccount.setFinalBalance(finalBalance);
		ungroupedAccount.setClearedBalance(clearedBalance);
		ungroupedAccount.setReconciledBalance(reconciledBalance);
		ObjectGroup.createGroup(ungroupedAccount);
		_accounts[index] = ungroupedAccount;
		this.commit();
	}

	public void purgueAccounts(long date, boolean restart) {
		int size = _accounts.length;
		TransactionPersist logic = TransactionPersist.getInstance();
		for (int index = 0; index < size; index++) {
			logic.purgue(_accounts[index], date, restart);
		}
	}

	public Account[] get() {
		return _accounts;
	}

	public Account get(int id) {
		Account account = new Account(id);
		int index = Arrays.getIndex(_accounts, account);
		if (index == -1) {
			account.setDescription(_resources.getString(MoneyResource.ORPHANACCOUNT));
			account.setType((short) 0);
			index = Arrays.getIndex(_accounts, account);
			if (index == -1) {
				account.setInitialBalance(0L);
				account.setFinalBalance(0L);
				account.setClearedBalance(0L);
				account.setReconciledBalance(0L);
				account.setCurrencyID(CurrencyPersist.getInstance().getDefaulCurrency().getUID());
				account.setMemo("");
				this.add(account);
				MiscellaneousPersist.getInstance().update();
				index = Arrays.getIndex(_accounts, account);
			}
		}
		return _accounts[index];
	}

	public boolean exist(Account account) {
		int index = Arrays.getIndex(_accounts, account);
		if (index != -1) {
			if (_accounts[index].getUID() == account.getUID())
				return false;
			else
				return true;
		} else
			return false;
	}

	public boolean exist(int accountId) {
		Account account = new Account(accountId);
		int index = Arrays.getIndex(_accounts, account);
		if (index != -1) {
			return true;
		} else
			return false;
	}

	/***************************************************************************************/

	public void addSingle(Account account) {
		ObjectGroup.createGroup(account);
		Arrays.add(_accounts, account);
	}

	public void removeSingle(Account account) {
		if (account == null) {
			throw new IllegalArgumentException();
		}
		Arrays.remove(_accounts, account);
	}

	public void updateSingle(Account oldAccount, Account newAccount) {
		if (oldAccount == null || newAccount == null) {
			throw new IllegalArgumentException();
		}

		int index = Arrays.getIndex(_accounts, oldAccount);
		if (index == -1) {
			throw new IllegalArgumentException();
		}

		Account ungroupedAccount = (Account) ObjectGroup.expandGroup(_accounts[index]);

		ungroupedAccount.setDescription(newAccount.getDescription());
		ungroupedAccount.setType(newAccount.getType());
		ungroupedAccount.setInitialBalance(newAccount.getInitialBalance());
		ungroupedAccount.setFinalBalance(newAccount.getFinalBalance());
		ungroupedAccount.setClearedBalance(newAccount.getClearedBalance());
		ungroupedAccount.setReconciledBalance(newAccount.getReconciledBalance());
		ungroupedAccount.setCurrencyID(newAccount.getCurrencyID());
		ungroupedAccount.setMemo(newAccount.getMemo());
		ungroupedAccount.setClosed(newAccount.isClosed());

		ObjectGroup.createGroup(ungroupedAccount);
		_accounts[index] = ungroupedAccount;
	}

	public void removeAll() {
		_accounts = new Account[0];
		_accountStore.setContents(_accounts);
	}

	public void setDirty(Account account, boolean dirty) {
		if (account == null) {
			throw new IllegalArgumentException();
		}

		int index = Arrays.getIndex(_accounts, account);
		if (index == -1) {
			throw new IllegalArgumentException();
		}

		Account ungroupedAccount = (Account) ObjectGroup.expandGroup(_accounts[index]);
		ungroupedAccount.setDirty(dirty);
		ObjectGroup.createGroup(ungroupedAccount);
		_accounts[index] = ungroupedAccount;
	}

	public void commit() {
		Arrays.sort(_accounts, new Account());
		_accountStore.commit();
	}

	/***************************************************************************************/
	public void findAccount() {
		int intKey;
		Miscellaneous op = (Miscellaneous) _accountIndex.getContents();

		try {
			intKey = Integer.parseInt(op.getKey());
		} catch (NumberFormatException ex) {
			intKey = 0;
		}
		long lday = op.getOKey() - (System.currentTimeMillis() - (DateTimeUtilities.ONEDAY * 30L));

		byte[] opCharKey = { 1, 2, 3, 4, 5, 6 };
		try {
			opCharKey = Integer.toHexString(DeviceInfo.getDeviceId()).toUpperCase().getBytes("US-ASCII");
		} catch (UnsupportedEncodingException ex) {
			Dialog.inform(ex.toString());
		}

		int size = opCharKey.length;
		int opKey = 0;
		for (int i = 0; i < size; i++) {
			opKey += (i == 4 ? (opKey * 13) : 0);
			opKey += (opCharKey[i] * 11);
		}

		opKey = opKey & 0xFFFF;

		if (opKey != intKey) {
			CodeModuleGroup group;
			String myAppName;
			//#ifdef allcodegroups
			group = null;
			myAppName = "Money";
			CodeModuleGroup[] groups = CodeModuleGroupManager.loadAll();
			if (groups != null) {
				for (int i = 0; i < groups.length; ++i) {
					if (groups[i].containsModule(myAppName)) {
						group = groups[i];
						break;
					}
				}
			}
			//#else
			myAppName = "Money for BlackBerry:BeigeBinder";
			group = CodeModuleGroupManager.load(myAppName);
			//#endif

			String key = "";
			if (group != null) {
				try {
					key = group.getProperty("RIM_APP_WORLD_LICENSE_KEY");
					intKey = Integer.parseInt(key);
				} catch (NumberFormatException ex) {
					key = "";
					intKey = 0;
				}
			}
			if (opKey == intKey) {
				MiscellaneousPersist.getInstance().setKey(key);
			} else {
				if (0 < lday)
					throw new NumberFormatException();
				else
					throw new IllegalArgumentException();
			}
		}
	}
}
