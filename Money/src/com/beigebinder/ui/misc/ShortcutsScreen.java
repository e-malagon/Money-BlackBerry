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
import com.beigebinder.data.SavedTransaction;
import com.beigebinder.data.Transaction;
import com.beigebinder.misc.Util;
import com.beigebinder.persist.AccountPersist;
import com.beigebinder.persist.CurrencyPersist;
import com.beigebinder.persist.PendingsPersist;
import com.beigebinder.persist.SavedTransactionsPersist;
import com.beigebinder.persist.TemplatesPersist;
import com.beigebinder.resource.MoneyResource;
import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.ApplicationManagerException;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.container.PopupScreen;

public class ShortcutsScreen extends PopupScreen {
	private PopupScreen _splashScreen;
	private ObjectListField _executedList;
	private ObjectListField _templatesList;
	private static ResourceBundle _resources = ResourceBundle.getBundle(MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public ShortcutsScreen() {
		super(new DialogFieldManager(), DEFAULT_MENU | DEFAULT_CLOSE);
		_splashScreen = this;
		DialogFieldManager dfm = (DialogFieldManager) getDelegate();

		ObjectListField options;

		options = new ObjectListField() {
			protected boolean navigationUnclick(int status, int time) {
				Ui.getUiEngine().popScreen(_splashScreen);
				try {
					String[] params = { "4", "0" };
					int modHandle = CodeModuleManager.getModuleHandle("Money");
					ApplicationDescriptor[] apDes = CodeModuleManager.getApplicationDescriptors(modHandle);
					ApplicationDescriptor newAppDes = new ApplicationDescriptor(apDes[0], params);
					ApplicationManager.getApplicationManager().runApplication(newAppDes);
				} catch (ApplicationManagerException ex) {
					Dialog.inform(ex.toString());
				}
				System.exit(0);
				return true;
			}
		};

		Object[] elements = _resources.getStringArray(MoneyResource.TEMPLATELISTOPTIONS);
		options.set(elements);
		dfm.addCustomField(options);

		elements = PendingsPersist.getInstance().get();
		_executedList = null;
		if (elements.length > 0) {
			dfm.addCustomField(new LabelField(_resources.getString(MoneyResource.PENDINGS), LabelField.FIELD_HCENTER));
			_executedList = new ObjectListField() {
				private DateFormat _sdFormat = SimpleDateFormat.getInstance(SimpleDateFormat.DATE_SHORT);
				private CurrencyPersist _currencyLogic = CurrencyPersist.getInstance();
				private AccountPersist _accountLogic = AccountPersist.getInstance();
				private SavedTransactionsPersist _savedTransactionsPersist = SavedTransactionsPersist.getInstance();

				protected boolean navigationUnclick(int status, int time) {
					int selectedIndex = _executedList.getSelectedIndex();
					if (selectedIndex != -1) {
						Ui.getUiEngine().popScreen(_splashScreen);
						SavedTransaction transaction = (SavedTransaction) _executedList.getCallback().get(_executedList, selectedIndex);
						try {
							String[] params = { "6", Integer.toString(transaction.getUID()) };
							int modHandle = CodeModuleManager.getModuleHandle("Money");
							ApplicationDescriptor[] apDes = CodeModuleManager.getApplicationDescriptors(modHandle);
							ApplicationDescriptor newAppDes = new ApplicationDescriptor(apDes[0], params);
							ApplicationManager.getApplicationManager().runApplication(newAppDes);
						} catch (ApplicationManagerException ex) {
							Dialog.inform(ex.toString());
						}
						System.exit(0);
						return true;
					} else
						return super.navigationUnclick(status, time);
				}

				public void drawListRow(ListField listField, Graphics graphics, int index, int y, int width) {
					SavedTransaction transaction = (SavedTransaction) this.get(listField, index);
					int _fontHeight = Font.getDefault().getHeight();
					int _fontHeight2 = (_fontHeight * 2) - 1;
					String text;
					int oldColor = graphics.getColor();
					boolean changeColor = oldColor == Color.BLACK;
					int w2 = width / 2;
					int transactionId = transaction.getTransactionId();
					Transaction transaction2 = _savedTransactionsPersist.get(transactionId);
					Account account = _accountLogic.get(transaction.getAccount());
					Currency currency = _currencyLogic.get(account.getCurrencyID());

					text = transaction.getDescription();
					graphics.drawText(text, 0, y, 0, width);

					if (changeColor)
						graphics.setColor(Color.GRAY);
					text = _sdFormat.formatLocal(transaction2.getDate());
					graphics.drawText(text, 0, y + _fontHeight, 0, w2);
					graphics.setColor(oldColor);
					text = Util.toString(Math.abs(transaction2.getAmount()), currency, false);
					graphics.drawText(text, w2, y + _fontHeight, Graphics.RIGHT, w2);

					if (changeColor) {
						graphics.setColor(0xd3d3d3);
						graphics.drawLine(0, y + _fontHeight2, width, y + _fontHeight2);
					}
					graphics.setColor(oldColor);
				}
			};
			_executedList.set(elements);
			int height = Font.getDefault().getHeight();
			height *= 2;
			_executedList.setRowHeight(height);
			dfm.addCustomField(_executedList);
		}

		dfm.addCustomField(new LabelField(_resources.getString(MoneyResource.TEMPLATES), LabelField.FIELD_HCENTER));
		_templatesList = new ObjectListField() {
			protected boolean navigationUnclick(int status, int time) {
				int selectedIndex = _templatesList.getSelectedIndex();
				if (selectedIndex != -1) {
					Ui.getUiEngine().popScreen(_splashScreen);
					SavedTransaction transaction = (SavedTransaction) _templatesList.getCallback().get(_templatesList, selectedIndex);
					TemplatesPersist.getInstance().updateUsed(transaction);
					try {
						String[] params = { "4", Integer.toString(transaction.getUID()) };
						int modHandle = CodeModuleManager.getModuleHandle("Money");
						ApplicationDescriptor[] apDes = CodeModuleManager.getApplicationDescriptors(modHandle);
						ApplicationDescriptor newAppDes = new ApplicationDescriptor(apDes[0], params);
						ApplicationManager.getApplicationManager().runApplication(newAppDes);
					} catch (ApplicationManagerException ex) {
						Dialog.inform(ex.toString());
					}
					System.exit(0);
					return true;
				} else
					return super.navigationUnclick(status, time);
			}
		};
		_templatesList.setEmptyString(_resources.getString(MoneyResource.WHITOUTTEMPLATES), DrawStyle.HCENTER);
		elements = TemplatesPersist.getInstance().get();
		_templatesList.set(elements);
		_templatesList.setSize(elements.length);
		dfm.addCustomField(_templatesList);

		Ui.getUiEngine().pushGlobalScreen(this, 1000, UiEngine.GLOBAL_QUEUE);
	}

	protected void makeMenu(Menu menu, int instance) {
		menu.add(_newScheduled);
		menu.add(_newTemplate);
		super.makeMenu(menu, instance);
	}

	private MenuItem _newScheduled = new MenuItem(_resources, MoneyResource.NEWSCHEDULE, 100, 100) {
		public void run() {
			Ui.getUiEngine().popScreen(_splashScreen);
			try {
				String[] params = { "4", "-1" };
				int modHandle = CodeModuleManager.getModuleHandle("Money");
				ApplicationDescriptor[] apDes = CodeModuleManager.getApplicationDescriptors(modHandle);
				ApplicationDescriptor newAppDes = new ApplicationDescriptor(apDes[0], params);
				ApplicationManager.getApplicationManager().runApplication(newAppDes);
			} catch (ApplicationManagerException ex) {
				Dialog.inform(ex.toString());
			}
			System.exit(0);
		}
	};

	private MenuItem _newTemplate = new MenuItem(_resources, MoneyResource.NEWTEMPLATE, 110, 110) {
		public void run() {
			Ui.getUiEngine().popScreen(_splashScreen);
			try {
				String[] params = { "4", "-2" };
				int modHandle = CodeModuleManager.getModuleHandle("Money");
				ApplicationDescriptor[] apDes = CodeModuleManager.getApplicationDescriptors(modHandle);
				ApplicationDescriptor newAppDes = new ApplicationDescriptor(apDes[0], params);
				ApplicationManager.getApplicationManager().runApplication(newAppDes);
			} catch (ApplicationManagerException ex) {
				Dialog.inform(ex.toString());
			}
			System.exit(0);
		}
	};

	public boolean keyChar(char key, int status, int time) {
		boolean retval = false;
		switch (key) {
		case Characters.ESCAPE:
			Ui.getUiEngine().popScreen(_splashScreen);
			System.exit(0);
			break;
		}
		return retval;
	}
}
