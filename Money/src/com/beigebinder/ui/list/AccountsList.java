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
package com.beigebinder.ui.list;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;

import com.beigebinder.common.GetDateScreen;
import com.beigebinder.data.Account;
import com.beigebinder.data.Currency;
import com.beigebinder.misc.UpdateCallback;
import com.beigebinder.misc.Util;
import com.beigebinder.money.Money;
import com.beigebinder.persist.AccountPersist;
import com.beigebinder.persist.CurrencyPersist;
import com.beigebinder.persist.MiscellaneousPersist;
import com.beigebinder.resource.MoneyResource;
import com.beigebinder.ui.edit.EditAccount;
import com.beigebinder.ui.misc.AboutScreen;
import com.beigebinder.ui.misc.ExportAccountsScreen;
import com.beigebinder.ui.misc.GetCurrencyScreen;
import com.beigebinder.ui.misc.RegistrationScreen;
import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.util.DateTimeUtilities;
//#ifdef stringprovider
import net.rim.device.api.util.StringProvider;

//#endif

public final class AccountsList implements UpdateCallback {
	private AccountsList _accountsList;
	private CurrencyPersist _currencyLogic;
	private String[] _accountTypes;
	private static ResourceBundle _resources = ResourceBundle.getBundle(MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public AccountsList() {
		this._accountsList = this;
		_mainScreen.setTitle(_resources.getString(MoneyResource.ACCOUNTSTITLE));
		_currencyLogic = CurrencyPersist.getInstance();
		int height = Font.getDefault().getHeight();
		height *= 2;
		_accounts.setRowHeight(height);
		_accountTypes = _resources.getStringArray(MoneyResource.ACCOUNTTYPES);
		this.update();
		_mainScreen.add(_accounts);
		_mainScreen.addMenuItem(_export);
		_mainScreen.addMenuItem(_purgue);
		_mainScreen.addMenuItem(MenuItem.separator(161));
		_mainScreen.addMenuItem(_showSchedules);
		_mainScreen.addMenuItem(_showExecuted);
		_mainScreen.addMenuItem(MenuItem.separator(181));
		_mainScreen.addMenuItem(_showShopingLists);
		_mainScreen.addMenuItem(_showTemplates);
		_mainScreen.addMenuItem(MenuItem.separator(201));
		_mainScreen.addMenuItem(_showCategories);
		_mainScreen.addMenuItem(_defaultCurrency);
		_mainScreen.addMenuItem(MenuItem.separator(221));
		_mainScreen.addMenuItem(_help);
		_mainScreen.addMenuItem(_about);
		MiscellaneousPersist.getInstance().suscribe(_accountsList);
		Ui.getUiEngine().pushScreen(_mainScreen);

		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				try {
					AccountPersist.getInstance().findAccount();
				} catch (NumberFormatException ex) {
					_mainScreen.removeMenuItem(_about);
					_mainScreen.addMenuItem(_registration);
					_mainScreen.addMenuItem(_about);
				} catch (IllegalArgumentException ex) {
					new RegistrationScreen(_mainScreen, false);
				}
			}
		});
	}

	private MainScreen _mainScreen = new MainScreen(MainScreen.DEFAULT_MENU | MainScreen.DEFAULT_CLOSE) {
		protected void makeMenu(Menu menu, int instance) {
			if (UiApplication.getUiApplication().getActiveScreen().getFieldWithFocus().equals(_accounts)) {
				int selectedIndex = _accounts.getSelectedIndex();

				if (selectedIndex != -1) {
					Object obj = _accounts.get(_accounts, selectedIndex);
					if (obj instanceof Account) {
						Account account = (Account) obj;
						if (account.isClosed()) {
							//#ifdef stringprovider
							_closeAccount.setText(new StringProvider(_resources.getString(MoneyResource.MARKASOPEN)));
							//#else
							_closeAccount.setText(_resources.getString(MoneyResource.MARKASOPEN));
							//#endif
						} else {
							//#ifdef stringprovider
							_closeAccount.setText(new StringProvider(_resources.getString(MoneyResource.MARKASCLOSED)));
							//#else
							_closeAccount.setText(_resources.getString(MoneyResource.MARKASCLOSED));
							//#endif
						}
					}

					menu.add(_openAccount);
					menu.add(MenuItem.separator(101));
					menu.add(_newAccount);
					menu.add(_editAccount);
					menu.add(_closeAccount);
					menu.add(_deleteAccount);
				} else {
					menu.add(_newAccount);
				}
				menu.add(MenuItem.separator(141));
			}
			super.makeMenu(menu, instance);
		}

		public void close() {
			RuntimeStore appReg = RuntimeStore.getRuntimeStore();
			synchronized (appReg) {
				appReg.remove(Money.ID);
			}
			MiscellaneousPersist.getInstance().unSuscribe(_accountsList);
			super.close();
		}

		public boolean isDirty() {
			return false;
		}
	};

	private ObjectListField _accounts = new ObjectListField() {
		public void drawListRow(ListField listField, Graphics graphics, int index, int y, int width) {
			Account account = (Account) _accounts.get(_accounts, index);
			Font newFont = Font.getDefault().derive(Font.ITALIC);
			int fontHeight = Font.getDefault().getHeight();
			int fontHeight2 = (fontHeight * 2) - 1;
			String text;
			int accountType = account.getType();
			long finalBalance = account.getFinalBalance();
			int oldColor = graphics.getColor();
			boolean changeColor = oldColor == Color.BLACK;

			Currency currency = _currencyLogic.get(account.getCurrencyID());

			int w2 = width / 2;
			if (account.isClosed()) {
				if (changeColor)
					graphics.setColor(Color.GRAY);
				graphics.setFont(newFont);
				text = "- " + account.getDescription() + " (" + currency.getISOCode() + ") - " + _resources.getString(MoneyResource.CLOSED);
			} else {
				text = account.getDescription() + " (" + currency.getISOCode() + ")";
			}

			graphics.drawText(text, 0, y, 0, width);
			if (changeColor)
				graphics.setColor(Color.GRAY);
			graphics.setFont(newFont);
			text = _accountTypes[accountType];
			graphics.drawText(text, 0, y + fontHeight, 0, w2);
			if (finalBalance < 0) {
				graphics.setColor(Color.RED);
			}

			graphics.setColor(oldColor);
			graphics.setFont(Font.getDefault());
			text = Util.toString(finalBalance, currency, false);
			graphics.drawText(text, w2, y + fontHeight, Graphics.RIGHT, w2);
			if (changeColor) {
				graphics.setColor(0xd3d3d3);
				graphics.drawLine(0, y + fontHeight2, width, y + fontHeight2);
			}
			graphics.setColor(oldColor);
		}
	};

	private MenuItem _openAccount = new MenuItem(_resources, MoneyResource.TRANSACTIONS, 100, 100) {
		public void run() {
			int selectedIndex = _accounts.getSelectedIndex();
			Object obj = _accounts.get(_accounts, selectedIndex);
			if (obj instanceof Account) {
				Account account = (Account) obj;
				new TransactionList(account);
			}
		}
	};

	private MenuItem _newAccount = new MenuItem(_resources, MoneyResource.NEWACCOUNT, 110, 110) {
		public void run() {
			EditAccount editAccount = new EditAccount();
			if (editAccount.pickAccount()) {
				AccountPersist.getInstance().add(editAccount.getAccount());
				MiscellaneousPersist.getInstance().update();
			}
		}
	};

	private MenuItem _editAccount = new MenuItem(_resources, MoneyResource.EDITACCOUNT, 120, 120) {
		public void run() {
			int selectedIndex = _accounts.getSelectedIndex();
			Object obj = _accounts.get(_accounts, selectedIndex);
			Account account = (Account) obj;
			EditAccount editAccount = new EditAccount();
			editAccount.setAccount(account);
			if (editAccount.pickAccount()) {
				AccountPersist.getInstance().update(account, editAccount.getAccount());
				MiscellaneousPersist.getInstance().update();
			}
		}
	};

	private MenuItem _closeAccount = new MenuItem(_resources, MoneyResource.CLOSE, 130, 130) {
		public void run() {
			String msg;
			int selectedIndex = _accounts.getSelectedIndex();
			Object obj = _accounts.get(_accounts, selectedIndex);
			if (obj instanceof Account) {
				Account account = (Account) obj;
				if (account.isClosed())
					msg = _resources.getString(MoneyResource.ASKFOROPENACCOUNT);
				else
					msg = _resources.getString(MoneyResource.ASKFORCLOSEACCOUNT);
				if (Dialog.ask(Dialog.D_OK_CANCEL, msg, Dialog.CANCEL) == Dialog.OK) {
					AccountPersist.getInstance().updateClosed(account, !account.isClosed());
					update();
				}

			}
		}
	};

	private MenuItem _deleteAccount = new MenuItem(_resources, MoneyResource.DELETEACCOUNT, 140, 140) {
		public void run() {
			if (Dialog.ask(Dialog.D_DELETE, _resources.getString(MoneyResource.ASKFORDELETEACCOUNT), Dialog.CANCEL) == Dialog.DELETE) {
				int selectedIndex = _accounts.getSelectedIndex();
				Object obj = _accounts.get(_accounts, selectedIndex);
				if (obj instanceof Account) {
					Account account = (Account) obj;
					AccountPersist.getInstance().remove(account);
					update();
				}
			}
		}
	};

	private MenuItem _export = new MenuItem(_resources, MoneyResource.EXPORTTITLE, 150, 150) {
		public void run() {
			new ExportAccountsScreen();
		}
	};

	private MenuItem _purgue = new MenuItem(_resources, MoneyResource.PURGUE, 160, 160) {
		public void run() {
			Calendar calendar = DateTimeUtilities.getDate(0);
			int month = calendar.get(Calendar.MONTH);
			int year = calendar.get(Calendar.YEAR);
			year = month == 0 ? year - 1 : year;
			month = month == 0 ? 11 : month - 1;
			calendar.set(Calendar.MONTH, month);
			calendar.set(Calendar.YEAR, year);
			long time = calendar.getTime().getTime();

			GetDateScreen dateScreen = new GetDateScreen(_resources.getString(MoneyResource.DATE), time);
			if (dateScreen.pickDate()) {
				boolean restartAll = false;
				long date = dateScreen.getDate();

				DateFormat sdFormat = SimpleDateFormat.getInstance(SimpleDateFormat.DATE_DEFAULT);
				String message = _resources.getString(MoneyResource.ASKFORPURGUE) + sdFormat.formatLocal(date);
				if (Dialog.ask(Dialog.D_DELETE, message, Dialog.CANCEL) == Dialog.DELETE) {
					message = _resources.getString(MoneyResource.ASKFORZEROBALANCES);
					if (Dialog.ask(Dialog.D_YES_NO, message, Dialog.NO) == Dialog.YES) {
						restartAll = true;
					}
					AccountPersist.getInstance().purgueAccounts(date, restartAll);
					MiscellaneousPersist.getInstance().update();
				}

			}

		}
	};

	private MenuItem _showSchedules = new MenuItem(_resources, MoneyResource.SCHEDULES, 170, 170) {
		public void run() {
			new ScheduledList();
		}
	};

	private MenuItem _showExecuted = new MenuItem(_resources, MoneyResource.EXECUTEDTITLE, 180, 180) {
		public void run() {
			new UpcomingList();
		}
	};

	private MenuItem _showShopingLists = new MenuItem(_resources, MoneyResource.SHOPINGLISTS, 190, 190) {
		public void run() {
			new ShopingListScreen();
		}
	};

	private MenuItem _showTemplates = new MenuItem(_resources, MoneyResource.TEMPLATES, 200, 200) {
		public void run() {
			new TemplatesList();
		}
	};

	private MenuItem _showCategories = new MenuItem(_resources, MoneyResource.CATEGORIESTITLE, 210, 210) {
		public void run() {
			new CategoryList();
		}
	};

	private MenuItem _defaultCurrency = new MenuItem(_resources, MoneyResource.DEFAULTCURRENCY, 220, 220) {
		public void run() {
			GetCurrencyScreen selectCurrencyScreen = new GetCurrencyScreen(CurrencyPersist.getInstance().getDefaulCurrency());
			if (selectCurrencyScreen.pickCurrency()) {
				Currency currency = selectCurrencyScreen.getCurrency();
				CurrencyPersist.getInstance().setDefaultCurrency(currency.getUID());
			}
		}
	};

	private MenuItem _help = new MenuItem(_resources, MoneyResource.HELP, 230, 230) {
		public void run() {
			BrowserSession browserSession = Browser.getDefaultSession();
			browserSession.displayPage("https://www.beigebinder.com/help/Money/index.html");
		}
	};

	private MenuItem _registration = new MenuItem(_resources, MoneyResource.REGISTRATION, 240, 240) {
		public void run() {
			new RegistrationScreen(_mainScreen, true);
		}
	};

	private MenuItem _about = new MenuItem(_resources, MoneyResource.ABOUT, 250, 250) {
		public void run() {
			new AboutScreen();
		}
	};

	public void update() {
		Account[] accounts = AccountPersist.getInstance().get();
		_accounts.set(accounts);
		if (accounts.length == 0) {
			_mainScreen.setStatus(new NullField());
		} else {
			int i;
			int len = accounts.length;
			Hashtable hash = new Hashtable();
			Integer key;
			Long total;
			for (i = 0; i < len; i++) {
				key = new Integer(accounts[i].getCurrencyID());
				if (hash.containsKey(key)) {
					total = (Long) hash.get(key);
					total = new Long(total.longValue() + accounts[i].getFinalBalance());
					hash.put(key, total);
				} else {
					total = new Long(accounts[i].getFinalBalance());
					hash.put(key, total);
				}
			}
			Enumeration keys = hash.keys();
			Currency currency;
			String[] totals = new String[hash.size()];
			i = 0;
			while (keys.hasMoreElements()) {
				key = (Integer) keys.nextElement();
				currency = _currencyLogic.get(key.intValue());
				total = (Long) hash.get(key);
				totals[i++] = currency.getISOCode() + "  " + Util.toString(total.longValue(), currency, false);
			}
			ObjectChoiceField totalsChoiceField = new ObjectChoiceField(_resources.getString(MoneyResource.BALANCE), totals);
			_mainScreen.setStatus(totalsChoiceField);
		}
	}
}
