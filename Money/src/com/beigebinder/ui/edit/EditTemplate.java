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

import com.beigebinder.common.GetTextScreen;
import com.beigebinder.data.Account;
import com.beigebinder.data.Category;
import com.beigebinder.data.SavedTransaction;
import com.beigebinder.data.Transaction;
import com.beigebinder.persist.AccountPersist;
import com.beigebinder.persist.CategoryPersist;
import com.beigebinder.persist.MiscellaneousPersist;
import com.beigebinder.persist.SavedTransactionsPersist;
import com.beigebinder.persist.TemplatesPersist;
import com.beigebinder.resource.MoneyResource;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.synchronization.UIDGenerator;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;
//#ifdef stringprovider
import net.rim.device.api.util.StringProvider;

//#endif

public class EditTemplate {
	private SavedTransaction _savedTransaction;
	private Transaction _transaction;
	private EditTransactionField _editField;
	private EditField _description;
	private static ResourceBundle _resources = ResourceBundle.getBundle(MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public EditTemplate() {
		_description = new EditField(_resources.getString(MoneyResource.NAME), "", 30, EditField.NO_NEWLINE);
		_editField = new EditTransactionField();
		_editField.hideDate();

		_mainScreen.add(_description);
		_mainScreen.add(new SeparatorField());
		_mainScreen.add(_editField.getFieldManager());
		_mainScreen.setTitle(_resources.getString(MoneyResource.NEWTEMPLATE));
		_editField.suscribe();
		UiApplication.getUiApplication().pushScreen(_mainScreen);
	}

	public EditTemplate(SavedTransaction template) {
		this();
		_savedTransaction = template;
		Transaction transaction = SavedTransactionsPersist.getInstance().get(template.getTransactionId());
		Account account = AccountPersist.getInstance().get(template.getAccount());
		_transaction = transaction;
		_editField.setAccount(account);
		_editField.setTransaction(_transaction);
		if (_editField.isSplited()) {
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
		_description.setText(_savedTransaction.getDescription());
		_mainScreen.setTitle(_resources.getString(MoneyResource.EDITTEMPLATE));
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
			if (_editField.isWithMemo()) {
				menu.add(_editMemo);
				menu.add(_deleteMemo);
			} else {
				menu.add(_addMemo);
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
			Account newAccount = _editField.getNewAccount();
			Transaction transaction = _editField.getTransaction();
			if (_transaction != null) {
				transaction.setId(_transaction.getUID());
				TemplatesPersist.getInstance().update(_savedTransaction, _description.getText(), newAccount.getUID());
				SavedTransactionsPersist.getInstance().update(_transaction, transaction);
			} else {
				SavedTransaction savedTransaction = new SavedTransaction();
				savedTransaction.setId(UIDGenerator.getUID());
				savedTransaction.setDescription(_description.getText());
				savedTransaction.setAccount(newAccount.getUID());
				savedTransaction.setTransactionId(transaction.getUID());
				TemplatesPersist.getInstance().add(savedTransaction);
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

	private MenuItem _addMemo = new MenuItem(_resources, MoneyResource.ADDMEMO, 170, 170) {
		public void run() {
			GetTextScreen textPopupScreen = new GetTextScreen(_resources.getString(MoneyResource.ADDMEMO), _editField.getMemo(), 0);
			if (textPopupScreen.pickText()) {
				String text = textPopupScreen.getText();
				_editField.addMemo();
				_editField.setMemo(text);
				_editField.setDirty(true);
			}
		}
	};

	private MenuItem _deleteMemo = new MenuItem(_resources, MoneyResource.REMOVEMEMO, 180, 180) {
		public void run() {
			if (Dialog.ask(Dialog.D_DELETE, _resources.getString(MoneyResource.ASKFORDELETEMEMO), Dialog.CANCEL) == Dialog.DELETE) {
				_editField.deleteMemo();
				_editField.setDirty(true);
			}
		}
	};

	private MenuItem _editMemo = new MenuItem(_resources, MoneyResource.EDITMEMO, 190, 190) {
		public void run() {
			GetTextScreen textPopupScreen = new GetTextScreen(_resources.getString(MoneyResource.EDITMEMO), _editField.getMemo(), 0);
			if (textPopupScreen.pickText()) {
				String text = textPopupScreen.getText();
				_editField.addMemo();
				_editField.setMemo(text);
				_editField.setDirty(true);
			}
		}
	};
}
