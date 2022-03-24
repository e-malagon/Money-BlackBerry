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
package com.beigebinder.money;

import com.beigebinder.data.Account;
import com.beigebinder.data.SavedTransaction;
import com.beigebinder.data.Transaction;
import com.beigebinder.persist.AccountPersist;
import com.beigebinder.persist.PendingsPersist;
import com.beigebinder.persist.SavedTransactionsPersist;
import com.beigebinder.persist.TemplatesPersist;
import com.beigebinder.ui.edit.EditScheduled;
import com.beigebinder.ui.edit.EditTemplate;
import com.beigebinder.ui.edit.EditTransaction;
import com.beigebinder.ui.edit.EditUpcoming;
import com.beigebinder.ui.list.AccountsList;
import com.beigebinder.ui.list.UpcomingList;
import net.rim.device.api.system.ObjectGroup;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.UiApplication;

public final class Money extends UiApplication {
	public static final long ID = 0xe99b9d5bbccd0338L; // com.beigebinder.money.Money
	private int _show;
	private int _templateId;

	public Money() {
		_show = 0;
	}

	public void showAccountList() {
		new AccountsList();
	}

	public void showMiscList() {
		new UpcomingList();
	}

	public void showEditTransaction() {
		switch (_templateId) {
		case -2:
			new EditTemplate();
			break;
		case -1:
			new EditScheduled();
			break;
		case 0:
			new EditTransaction();
			break;
		default:
			SavedTransaction savedTransaction = TemplatesPersist.getInstance().get(_templateId);
			Account account = AccountPersist.getInstance().get(savedTransaction.getAccount());
			Transaction transaction = (Transaction) ObjectGroup.expandGroup(SavedTransactionsPersist.getInstance().get(savedTransaction.getTransactionId()));
			transaction.setDate(System.currentTimeMillis());
			EditTransaction editTransaction = new EditTransaction(account, transaction);
			editTransaction.clearTransacton();

			break;
		}
	}

	public void showEditPrevTransaction() {
		if (_templateId != 0) {
			SavedTransaction savedTransaction = PendingsPersist.getInstance().get(_templateId);
			new EditUpcoming(savedTransaction);
		}
	}

	public void setShow(int show) {
		_show = show;
	}

	public void setTemplateTransaction(int id) {
		_templateId = id;
	}

	public void activate() {
		super.activate();
		switch (_show) {
		case 3:
			this.showMiscList();
			break;
		case 4:
			this.showEditTransaction();
			break;
		case 6:
			this.showEditPrevTransaction();
			break;
		}
		_show = 0;
	}

	public static void main(String[] args) {
		if (args != null && args.length > 0) {
			int value = Integer.parseInt(args[0]);
			switch (value) {
			case 0:
				MoneyStartup moneyStartup = new MoneyStartup();
				moneyStartup.enterEventDispatcher();
				break;
			case 1:
			case 3:
			case 4:
			case 6:
				RuntimeStore appReg = RuntimeStore.getRuntimeStore();
				Money money;
				boolean npi;
				synchronized (appReg) {
					npi = appReg.get(ID) == null;
					if (npi) {
						appReg.put(ID, new Money());
					}
					money = (Money) appReg.waitFor(ID);
				}
				if (value == 4 || value == 6) {
					int id = Integer.parseInt(args[1]);
					money.setTemplateTransaction(id);
				} else {
					money.setTemplateTransaction(0);
				}
				if (npi) {
					money.showAccountList();
					if (value == 3) {
						money.showMiscList();
					}
					if (value == 4) {
						money.showEditTransaction();
					}
					if (value == 6) {
						money.showEditPrevTransaction();
					}
					money.enterEventDispatcher();
				} else {
					money.setShow(value);
					money.requestForeground();
				}
				break;
			case 2:
				MoneyAlert moneyAlert = new MoneyAlert();
				moneyAlert.enterEventDispatcher();
				break;
			case 5:
				MoneyShortcuts moneyShortcuts = new MoneyShortcuts();
				moneyShortcuts.enterEventDispatcher();
				break;
			default:
				System.exit(0);
			}
		}
	}
}
