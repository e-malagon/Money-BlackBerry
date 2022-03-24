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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import com.beigebinder.common.GetFileScreen;
import com.beigebinder.data.Account;
import com.beigebinder.data.Currency;
import com.beigebinder.data.Transaction;
import com.beigebinder.persist.AccountPersist;
import com.beigebinder.persist.CurrencyPersist;
import com.beigebinder.persist.TransactionPersist;
import com.beigebinder.resource.MoneyResource;
import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.MessageArguments;
import net.rim.blackberry.api.mail.Folder;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.Multipart;
import net.rim.blackberry.api.mail.Session;
import net.rim.blackberry.api.mail.Store;
import net.rim.blackberry.api.mail.SupportedAttachmentPart;
import net.rim.blackberry.api.mail.TextBodyPart;
import net.rim.blackberry.api.mail.BodyPart.ContentType;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.DateField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.DateTimeUtilities;

public class ExportAccountsScreen {
	private ObjectChoiceField _type;
	private CheckboxField _includeInitialBalance;
	private ObjectChoiceField _datesRangue;
	private ObjectChoiceField _selectedAccounts;
	private DateField _startDate;
	private DateField _endDate;
	private Vector _attachments;

	private static ResourceBundle _resources = ResourceBundle.getBundle(
			MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public ExportAccountsScreen() {
		_mainScreen.setTitle(_resources.getString(MoneyResource.EXPORTTITLE));

		IExport[] exportTypes = new IExport[] { new ExportTextImplementation(),
				new ExportQIFImplementation(0), new ExportQIFImplementation(1) };
		_type = new ObjectChoiceField(_resources
				.getString(MoneyResource.FORMAT), exportTypes);

		_includeInitialBalance = new CheckboxField(_resources
				.getString(MoneyResource.INCLUDEINITIALBALANCE), false);

		String[] types = _resources.getStringArray(MoneyResource.EXPORTDATES);
		_datesRangue = new ObjectChoiceField(_resources
				.getString(MoneyResource.DATES), types);

		_startDate = new DateField(_resources
				.getString(MoneyResource.STARTDATE), System.currentTimeMillis()
				- (DateTimeUtilities.ONEDAY * 7L), DateField.DATE);
		_endDate = new DateField(_resources.getString(MoneyResource.ENDDATE),
				System.currentTimeMillis(), DateField.DATE);

		types = _resources.getStringArray(MoneyResource.EXPORTACCOUNT);
		_selectedAccounts = new ObjectChoiceField(_resources
				.getString(MoneyResource.ACCOUNTS), types);

		Account[] accounts = AccountPersist.getInstance().get();
		int size = accounts.length;
		Choise[] choises = new Choise[size];
		for (int index = 0; index < size; index++) {
			choises[index] = new Choise(accounts[index]);
		}

		_accounts.set(choises);

		_type.setSelectedIndex(1);
		_mainScreen.add(_type);
		_mainScreen.add(_includeInitialBalance);
		_mainScreen.add(new SeparatorField());
		_mainScreen.add(_datesRangue);
		_mainScreen.add(_startDate);
		_mainScreen.add(_endDate);
		_mainScreen.add(new SeparatorField());
		_mainScreen.add(_selectedAccounts);
		_mainScreen.add(_accounts);
		_mainScreen.addMenuItem(_exportEmail);
		_mainScreen.addMenuItem(_exportCard);

		UiApplication.getUiApplication().pushScreen(_mainScreen);
	}

	private MainScreen _mainScreen = new MainScreen(MainScreen.DEFAULT_MENU
			| MainScreen.DEFAULT_CLOSE) {
		public boolean isDirty() {
			return false;
		}

		protected void makeMenu(Menu menu, int instance) {

			super.makeMenu(menu, instance);
		}
	};

	private MenuItem _exportEmail = new MenuItem(_resources,
			MoneyResource.EXPORTANDAEMAIL, 100, 100) {
		public void run() {
			UiApplication.getUiApplication().popScreen(_mainScreen);
			export();
			if (_attachments.size() == 0) {
				Dialog.inform(_resources
						.getString(MoneyResource.NOTRANSACTIONTOEXPORT));
				return;
			}
			send();
		}
	};

	private MenuItem _exportCard = new MenuItem(_resources,
			MoneyResource.EXPORTANDSAVE, 110, 110) {
		public void run() {

			GetFileScreen fps = new GetFileScreen(null, null, true);
			fps.pickFile();
			String path = fps.getFile();

			if (path != null) {
				UiApplication.getUiApplication().popScreen(_mainScreen);
				export();
				if (_attachments.size() == 0) {
					Dialog.inform(_resources
							.getString(MoneyResource.NOTRANSACTIONTOEXPORT));
					return;
				}
				save(path);
				Dialog
						.inform(_resources
								.getString(MoneyResource.EXPORTMESSAGE));
			}
		}
	};

	private void save(String path) {
		int size = _attachments.size();
		Atatchments atatchments;
		for (int index = 0; index < size; index++) {
			try {
				atatchments = (Atatchments) _attachments.elementAt(index);
				FileConnection fconn = (FileConnection) Connector
						.open("file:///" + path + atatchments.name);
				if (!fconn.exists())
					fconn.create();
				else
					fconn.truncate(0L);
				OutputStream ostream = fconn.openOutputStream();
				ostream.write(atatchments.buffer);
				ostream.close();
				fconn.close();
			} catch (IOException e) {
				Dialog.inform(e.toString());
			}
		}
	}

	private void send() {
		Atatchments atatchments;
		Session session = Session.getDefaultInstance();
		if (session != null) {
			Store store = session.getStore();
			Multipart mp = new Multipart();
			String messageData = "";
			SupportedAttachmentPart sap;
			TextBodyPart tbp = new TextBodyPart(mp, messageData);
			mp.addBodyPart(tbp);

			int size = _attachments.size();
			for (int index = 0; index < size; index++) {
				atatchments = (Atatchments) _attachments.elementAt(index);
				sap = new SupportedAttachmentPart(mp, ContentType.TYPE_TEXT
						+ ContentType.SUBTYPE_PLAIN, atatchments.name,
						atatchments.buffer);
				mp.addBodyPart(sap);
			}

			Folder[] folders = store.list(Folder.SENT);
			Folder sentfolder = folders[0];
			Message msg = new Message(sentfolder);
			try {

				msg.setSubject(_resources.getString(MoneyResource.MONEYREPORT));
				msg.setContent(mp);
				Invoke.invokeApplication(Invoke.APP_TYPE_MESSAGES,
						new MessageArguments(msg));
			} catch (Exception e) {
				Dialog.inform(e.toString());
			}

		}
	}

	private void export() {
		String[] accountTypes = _resources
				.getStringArray(MoneyResource.ACCOUNTTYPES);
		IExport export = (IExport) _type.getChoice(_type.getSelectedIndex());
		CurrencyPersist currencyPersist = CurrencyPersist.getInstance();
		boolean allAccounts = _selectedAccounts.getSelectedIndex() == 0;
		boolean allDates = _datesRangue.getSelectedIndex() == 0;
		long startDate = _startDate.getDate();
		long endDate = _endDate.getDate();
		int size = _accounts.getSize();
		DataBuffer buffer = null;
		Currency currency;
		Choise choise;
		Account account;
		Transaction[] transactions;
		_attachments = new Vector();
		int index;
		int size2;

		for (int choiseIndex = 0; choiseIndex < size; choiseIndex++) {
			choise = (Choise) _accounts.get(_accounts, choiseIndex);
			if (!allAccounts) {
				if (!choise.isSelected()) {
					continue;
				}
			}
			account = (Account) choise.getElement();
			currency = currencyPersist.get(account.getCurrencyID());
			transactions = TransactionPersist.getInstance().get(account);
			size2 = transactions.length;
			if (size2 == 0)
				continue;

			buffer = new DataBuffer();
			export.writeHeader(buffer, account, currency,
					_includeInitialBalance.getChecked());
			for (index = size2 - 1; 0 <= index; index--) {
				if (!allDates) {
					if (transactions[index].getDate() < startDate) {
						continue;
					} else if (endDate < transactions[index].getDate()) {
						break;
					}
				}
				export.writeTransaction(buffer, currency, transactions[index]);
			}
			export.writeFooter(buffer, currency, account);

			Atatchments atatchments = new Atatchments();
			atatchments.buffer = buffer.toArray();
			atatchments.name = accountTypes[account.getType()] + " - "
					+ account.getDescription() + export.getExtention();
			_attachments.addElement(atatchments);

		}

	}

	private ObjectListField _accounts = new ObjectListField() {
		protected boolean navigationUnclick(int status, int time) {
			int index = getSelectedIndex();
			Choise choise = (Choise) _accounts.getCallback().get(_accounts,
					index);
			choise.toggleChecked();
			_accounts.invalidate(index);
			return true;
		}
	};

	private static final class Atatchments {
		public byte[] buffer;
		public String name;
	}

	private static final class Choise {
		private Object _element;
		private boolean _selected;

		public Choise(Object element) {
			_element = element;
		}

		public Object getElement() {
			return _element;
		}

		public boolean isSelected() {
			return _selected;
		}

		public void toggleChecked() {
			this._selected = !this._selected;
		}

		public String toString() {
			StringBuffer rowString = new StringBuffer();
			if (_selected) {
				rowString.append(Characters.BALLOT_BOX_WITH_CHECK);
			} else {
				rowString.append(Characters.BALLOT_BOX);
			}
			rowString.append(Characters.SPACE);
			rowString.append(_element.toString());
			return rowString.toString();
		}
	}

}
