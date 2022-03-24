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

import com.beigebinder.data.Account;
import com.beigebinder.data.Category;
import com.beigebinder.data.Notification;
import com.beigebinder.data.Transaction;
import com.beigebinder.persist.AccountPersist;
import com.beigebinder.persist.CategoryPersist;
import com.beigebinder.persist.MiscellaneousPersist;
import com.beigebinder.persist.NotificationPersist;
import com.beigebinder.persist.SavedTransactionsPersist;
import com.beigebinder.resource.MoneyResource;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.synchronization.UIDGenerator;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.DateField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;
//#ifdef stringprovider
import net.rim.device.api.util.StringProvider;
//#endif

public class EditScheduled {
	private Notification _schedule;
	private Transaction _transaction;

	private EditField _notificationDescription;
	private ObjectChoiceField _typeSchedule;
	private DateField _date;
	private EditField _daysForAlert;
	private EditField _notificationsLeft;
	private EditTransactionField _editField;

	private static ResourceBundle _resources = ResourceBundle.getBundle(MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public EditScheduled() {
		_schedule = null;
		_mainScreen.setTitle(_resources.getString(MoneyResource.NEWSCHEDULE));

		String[] types = _resources.getStringArray(MoneyResource.SCHEDULETYPES);
		_typeSchedule = new ObjectChoiceField(_resources.getString(MoneyResource.SCHEDULETYPE), types);
		_date = new DateField(_resources.getString(MoneyResource.NEXTDATE), System.currentTimeMillis(), DateField.DATE);
		_notificationDescription = new EditField(_resources.getString(MoneyResource.DESCRIPTION), "", 30, EditField.NO_NEWLINE);
		_daysForAlert = new EditField(_resources.getString(MoneyResource.DAYSFORALERT), "1", 3, EditField.FILTER_REAL_NUMERIC | EditField.NO_NEWLINE);
		_notificationsLeft = new EditField(_resources.getString(MoneyResource.NOTIFICATIONSLEFT), "", 3, EditField.FILTER_REAL_NUMERIC | EditField.NO_NEWLINE);
		_editField = new EditTransactionField();
		_editField.hideDate();

		_mainScreen.add(_notificationDescription);
		_mainScreen.add(_typeSchedule);
		_mainScreen.add(_date);
		_mainScreen.add(_daysForAlert);
		_mainScreen.add(_notificationsLeft);
		_mainScreen.add(new SeparatorField());
		_mainScreen.add(_editField.getFieldManager());

		_editField.suscribe();
		UiApplication.getUiApplication().pushScreen(_mainScreen);
	}

	public EditScheduled(Notification schedule) {
		this();
		this._schedule = schedule;
		this.setSchedule(_schedule);
		_mainScreen.setTitle(_resources.getString(MoneyResource.EDITSCHEDULE));
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
				menu.add(_addAccount);
				menu.add(_addCategory);
				menu.setDefault(_addSplits);
			} else {
				if (_editField.getType() != 2) {
					menu.add(_setSplits);
					if (_editField.isSplited())
						menu.add(_addSplits);
					menu.add(MenuItem.separator(121));
					menu.add(_addAccount);
					menu.add(_addCategory);
					menu.add(MenuItem.separator(161));
				} else {
					menu.add(_addAccount);
					menu.add(MenuItem.separator(151));
				}
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
			Notification schedule = getSchedule();
			Transaction transaction = _editField.getTransaction();

			if (_schedule != null) {
				transaction.setId(_transaction.getUID());
				schedule.setTransactionId(transaction.getUID());
				NotificationPersist.getInstance().update(_schedule, schedule);
				SavedTransactionsPersist.getInstance().update(_transaction, transaction);
			} else {
				schedule.setTransactionId(transaction.getUID());
				NotificationPersist.getInstance().add(schedule);
				SavedTransactionsPersist.getInstance().add(transaction);
			}
			super.save();
			MiscellaneousPersist.getInstance().update();
		}

		public void close() {
			_editField.unSuscribe();
			super.close();
		}
	};

	private MenuItem _ok = new MenuItem(_resources, MoneyResource.SAVE, 100, 100) {
		public void run() {
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

	private MenuItem _addAccount = new MenuItem(_resources, MoneyResource.NEWACCOUNT, 150, 150) {
		public void run() {
			EditAccount editAccount = new EditAccount();
			if (editAccount.pickAccount()) {
				AccountPersist.getInstance().add(editAccount.getAccount());
				MiscellaneousPersist.getInstance().update();
			}
		}
	};

	private MenuItem _addCategory = new MenuItem(_resources, MoneyResource.NEWCATEGORY, 160, 160) {
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

	private void setSchedule(Notification schedule) {
		Account account = AccountPersist.getInstance().get(schedule.getAccount());
		this._typeSchedule.setSelectedIndex(schedule.getType());
		this._notificationDescription.setText(schedule.getDescription());
		_editField.setAccount(account);
		this._daysForAlert.setText(String.valueOf(schedule.getDaysForAlert()));
		this._date.setDate(schedule.getNextExecutionDate());
		if (schedule.getNotificationsLeft() <= 999)
			_notificationsLeft.setText(String.valueOf(schedule.getNotificationsLeft()));

		Transaction transaction = SavedTransactionsPersist.getInstance().get(schedule.getTransactionId());
		this._transaction = transaction;
		_editField.setTransaction(transaction);
	}

	private Notification getSchedule() {
		Notification schedule = new Notification();
		schedule.setId(UIDGenerator.getUID());
		schedule.setType((short) _typeSchedule.getSelectedIndex());
		schedule.setDescription(_notificationDescription.getText());
		schedule.setNextExecutionDate(_date.getDate());
		int n = 1;
		try {
			n = Integer.parseInt(_daysForAlert.getText());
		} catch (NumberFormatException e) {
			n = 1;
		}
		schedule.setDaysForAlert(n);
		n = 1;
		if (_typeSchedule.getSelectedIndex() != 0) {
			try {
				n = Integer.parseInt(_notificationsLeft.getText());
			} catch (NumberFormatException e) {
				n = 100000;
			}
		}
		schedule.setNotificationsLeft(n);
		Account account = _editField.getNewAccount();
		schedule.setAccount(account.getUID());
		return schedule;
	}
}
