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
import java.util.Vector;

import com.beigebinder.common.GetTextScreen;
import com.beigebinder.data.Account;
import com.beigebinder.data.Category;
import com.beigebinder.data.Currency;
import com.beigebinder.data.Expense;
import com.beigebinder.data.Item;
import com.beigebinder.data.ShopingList;
import com.beigebinder.data.TaxedShopingList;
import com.beigebinder.misc.Util;
import com.beigebinder.persist.CategoryPersist;
import com.beigebinder.persist.CurrencyPersist;
import com.beigebinder.persist.MiscellaneousPersist;
import com.beigebinder.persist.ShopingListPersist;
import com.beigebinder.resource.MoneyResource;
import com.beigebinder.ui.misc.GetAccountScreen;
import com.beigebinder.ui.misc.GetCurrencyScreen;
import com.beigebinder.ui.misc.GetExchangeRateScreen;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.synchronization.UIDGenerator;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FocusChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.util.Arrays;
//#ifdef stringprovider
import net.rim.device.api.util.StringProvider;
//#endif

public class EditShopingList {
	private EditField _description;
	private ShopingList _shopingList;
	private Currency _currency;
	private boolean _splitsFocus;
	private CategoryPersist _categoryPersist;
	private Item[] _items;
	private Item[] _filteredItems;
	private boolean _isFiltered;
	private boolean _hideChequed;
	private boolean _checklist;
	private boolean _hideNoHiPriority;
	private boolean _hidePaid;
	private long _total;
	private int _taxRate;
	private EditField _totalField;
	private int _lastCategoryUsed;

	private static ResourceBundle _resources = ResourceBundle.getBundle(MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public EditShopingList() {
		_mainScreen.setTitle(_resources.getString(MoneyResource.NEWSHOPINGLIST));
		_shopingList = null;
		_splitsFocus = false;
		_isFiltered = false;
		_hideChequed = false;
		_checklist = false;
		_hideNoHiPriority = false;
		_hidePaid = false;
		_lastCategoryUsed = -1;
		_items = new Item[0];
		_filteredItems = _items;
		_total = 0;
		_taxRate = 0;
		_categoryPersist = CategoryPersist.getInstance();
		_description = new EditField(_resources.getString(MoneyResource.DESCRIPTION), "", 30, EditField.NO_NEWLINE);

		_currency = CurrencyPersist.getInstance().getDefaulCurrency();

		_totalField = new EditField(_resources.getString(MoneyResource.TOTAL) + _currency.getSign() + " ", Util.toString(_total, _currency, true), 15, EditField.FILTER_REAL_NUMERIC | EditField.NO_NEWLINE
				| EditField.NON_FOCUSABLE);

		_description.setFocusListener(_focusChangeListener);
		_listField.setFocusListener(_focusChangeListener);
		int height = Font.getDefault().getHeight();
		height *= 2;
		_listField.setRowHeight(height);
		_listField.set(_filteredItems);

		_mainScreen.add(_description);
		_mainScreen.add(new SeparatorField());
		_mainScreen.add(_listField);
		UiApplication.getUiApplication().pushScreen(_mainScreen);
	}

	public EditShopingList(ShopingList shopingList) {
		this();
		this.setShopinglist(shopingList);
	}

	private MainScreen _mainScreen = new MainScreen(MainScreen.DEFAULT_MENU | MainScreen.DEFAULT_CLOSE) {
		public void save() throws IOException {
			saveShopinglist();
			MiscellaneousPersist.getInstance().update();
			super.save();
		}

		protected void makeMenu(Menu menu, int instance) {
			int selectedIndex = _listField.getSelectedIndex();
			menu.add(_save);
			if (_splitsFocus && selectedIndex != -1) {
				if (_checklist) {
					Item item = (Item) _listField.get(_listField, selectedIndex);
					if (item.isChequed()) {
						//#ifdef stringprovider
						_check.setText(new StringProvider(_resources.getString(MoneyResource.UNCHECK)));
						//#else
						_check.setText(_resources.getString(MoneyResource.UNCHECK));
						//#endif						
					} else {
						//#ifdef stringprovider
						_check.setText(new StringProvider(_resources.getString(MoneyResource.CHECK)));
						//#else
						_check.setText(_resources.getString(MoneyResource.CHECK));
						//#endif
					}
					menu.add(_check);
					menu.setDefault(_check);
				} else {
					menu.add(_new);
					menu.add(_edit);
					menu.add(_delete);
					menu.setDefault(_new);
				}
			} else {
				menu.add(_new);
			}
			menu.add(MenuItem.separator(141));
			menu.add(_setTaxRate);
			menu.add(_checklistMenu);
			if (_checklist) {
				menu.add(_paid);
				menu.add(MenuItem.separator(166));
				menu.add(_hideNoHiPriorityMenu);
				menu.add(_hideChequedMenu);
				menu.add(_hidePaidMenu);
			}
			menu.add(MenuItem.separator(191));
			menu.add(_cleanPaid);
			menu.add(_changeCurrency);
			super.makeMenu(menu, instance);
		}
	};

	private MenuItem _save = new MenuItem(_resources, MoneyResource.SAVE, 100, 100) {
		public void run() {
			saveShopinglist();
			MiscellaneousPersist.getInstance().update();
			UiApplication.getUiApplication().popScreen(_mainScreen);
		}
	};

	private MenuItem _check = new MenuItem(_resources, MoneyResource.CHECK, 110, 110) {
		public void run() {
			Item item = (Item) _listField.get(_listField, _listField.getSelectedIndex());
			item.setChequed(!item.isChequed());
			if (item.isChequed()) {
				ConfirmItem confirmItemScreen = new ConfirmItem(item, _currency);
				if (!confirmItemScreen.isCanceled()) {
					long tmp = item.getAmount() * item.getQuantity();
					_total += tmp;
				} else {
					item.setChequed(!item.isChequed());
				}
			} else {
				long tmp = item.getAmount() * item.getQuantity();
				_total -= tmp;
			}
			_description.setDirty(true);
			_totalField.setText(Util.toString(_total, _currency, true));
			applyFilter();
		}
	};

	private MenuItem _new = new MenuItem(_resources, MoneyResource.NEWITEM, 120, 120) {
		public void run() {
			EditItem editItemPopupScreen = new EditItem(_resources.getString(MoneyResource.NEWITEM), _taxRate != 0);
			editItemPopupScreen.setCurrency(_currency);
			if (_lastCategoryUsed != -1) {
				editItemPopupScreen.setLastCategoryUsed(_lastCategoryUsed, 0);
			}
			if (editItemPopupScreen.pickIten()) {
				Item item = editItemPopupScreen.getItem();
				Arrays.add(_items, item);
				_lastCategoryUsed = item.getCategory();
				applyFilter();
				_description.setDirty(true);
			}
		}
	};

	private MenuItem _edit = new MenuItem(_resources, MoneyResource.EDITITEM, 130, 130) {
		public void run() {
			Item item = (Item) _listField.get(_listField, _listField.getSelectedIndex());
			EditItem editItemPopupScreen = new EditItem(_resources.getString(MoneyResource.EDITITEM), _taxRate != 0);
			editItemPopupScreen.setItem(item);
			editItemPopupScreen.setCurrency(_currency);
			if (_lastCategoryUsed != -1) {
				editItemPopupScreen.setLastCategoryUsed(_lastCategoryUsed, 0);
			}
			if (editItemPopupScreen.pickIten()) {
				item = editItemPopupScreen.getItem();
				int index = Arrays.getIndex(_items, item);
				_items[index] = item;
				applyFilter();
				_description.setDirty(true);
			}
		}
	};

	private MenuItem _delete = new MenuItem(_resources, MoneyResource.DELETEITEM, 140, 140) {
		public void run() {
			if (Dialog.ask(Dialog.D_DELETE, _resources.getString(MoneyResource.ASKFORDELETEITEM), Dialog.CANCEL) == Dialog.DELETE) {
				Item item = (Item) _listField.get(_listField, _listField.getSelectedIndex());
				Arrays.remove(_items, item);
				if (_isFiltered) {
					Arrays.remove(_filteredItems, item);
				}
				_listField.set(_filteredItems);
				_description.setDirty(true);
			}
		}
	};

	private MenuItem _setTaxRate = new MenuItem(_resources, MoneyResource.ADDTAX, 150, 150) {
		public void run() {
			String tax = Double.toString(((double) _taxRate) / 100.0);
			GetTextScreen getTextScreen = new GetTextScreen(_resources.getString(MoneyResource.ADDTAX), tax, EditField.FILTER_REAL_NUMERIC | EditField.NO_NEWLINE);
			if (getTextScreen.pickText()) {
				String text = getTextScreen.getText();
				try {
					_taxRate = (int) (Double.parseDouble(text) * 100);
				} catch (Exception ex) {
					Dialog.inform(ex.toString());
					return;
				}
			}
		}
	};

	private MenuItem _checklistMenu = new MenuItem(_resources, MoneyResource.CHECKLIST, 160, 160) {
		public void run() {
			_checklist = !_checklist;
			if (_checklist) {
				//#ifdef stringprovider
				this.setText(new StringProvider(_resources.getString(MoneyResource.SHOPINGLIST)));
				//#else
				this.setText(_resources.getString(MoneyResource.SHOPINGLIST));
				//#endif						
				_mainScreen.setStatus(_totalField);
			} else {
				//#ifdef stringprovider
				this.setText(new StringProvider(_resources.getString(MoneyResource.CHECKLIST)));
				//#else
				this.setText(_resources.getString(MoneyResource.CHECKLIST));
				//#endif						
				_mainScreen.setStatus(new NullField());
			}
			applyFilter();
		}
	};

	private MenuItem _paid = new MenuItem(_resources, MoneyResource.PAY, 165, 165) {
		public void run() {
			if (Dialog.ask(Dialog.D_YES_NO, _resources.getString(MoneyResource.ASKFORPAY), Dialog.NO) == Dialog.YES) {
				Item item;
				Vector vector = new Vector();
				int len = _items.length;
				int i;
				for (i = 0; i < len; i++) {
					if (_items[i].isChequed() && !_items[i].isPaid()) {
						vector.addElement(_items[i]);
					}
				}
				len = vector.size();
				if (len == 0) {
					Dialog.inform(_resources.getString(MoneyResource.NOITEMSSELECTED));
					return;
				}

				if (Dialog.ask(Dialog.D_YES_NO, _resources.getString(MoneyResource.ASKFORCREATEEXPENSE), Dialog.NO) == Dialog.YES) {
					GetAccountScreen selectAccountScreen = new GetAccountScreen();
					if (selectAccountScreen.pickAccount()) {

						Account account = selectAccountScreen.getAccount();
						long taxableTotal = 0;
						String[] descriptions = new String[len + (_taxRate != 0 ? 1 : 0)];
						int[] accounts = new int[len + (_taxRate != 0 ? 1 : 0)];
						long[] amounts = new long[len + (_taxRate != 0 ? 1 : 0)];
						long amount = 0;

						for (i = 0; i < len; i++) {
							item = (Item) vector.elementAt(i);
							descriptions[i] = new String(item.getDescription());
							accounts[i] = item.getCategory();
							amounts[i] = item.getAmount() * item.getQuantity();
							amount += amounts[i];
							if (item.isTaxable())
								taxableTotal += amounts[i];
						}
						if (account.getCurrencyID() != _currency.getUID()) {
							Currency accountCurrency = CurrencyPersist.getInstance().get(account.getCurrencyID());
							GetExchangeRateScreen getExchangeRateScreen = new GetExchangeRateScreen(_currency, accountCurrency, amount, "1.0");

							if (!getExchangeRateScreen.pickExchangeRate())
								return;

							long exchangeRate = getExchangeRateScreen.getExchangeRate();
							Currency exchangeRateCurrency = GetExchangeRateScreen.getExchangeRateCurrency();

							long tmpAmount = 0;

							amount = Util.exchangeToAmount(amount, exchangeRate, _currency, exchangeRateCurrency, accountCurrency);

							taxableTotal = 0;

							for (i = 0; i < len; i++) {
								item = (Item) vector.elementAt(i);
								amounts[i] = Util.exchangeToAmount(amounts[i], exchangeRate, _currency, exchangeRateCurrency, accountCurrency);
								tmpAmount += amounts[i];
								if (item.isTaxable())
									taxableTotal += amounts[i];
							}

							amounts[0] -= (tmpAmount - amount);
						}

						if (_taxRate != 0) {
							descriptions[len] = _resources.getString(MoneyResource.TAXDESCRIPTION);
							accounts[len] = _categoryPersist.getTaxCategory().getUID();
							amounts[len] = taxableTotal * _taxRate / 10000;
							amount += amounts[len];
						}

						amount *= -1;

						Expense expense = new Expense();
						expense.setId(UIDGenerator.getUID());
						expense.setStatus((byte) 0);
						expense.setNumber("");
						expense.setDescription(_description.getText());
						expense.setDate(System.currentTimeMillis());
						expense.setAmount(amount);
						expense.setDescriptions(descriptions);
						expense.setAccounts(accounts);
						expense.setAmounts(amounts);

						new EditTransaction(account, expense, true);
					}
				}
				_description.setDirty(true);
				for (i = 0; i < len; i++) {
					item = (Item) vector.elementAt(i);
					item.setPaid(true);
				}
			}
		}
	};

	private MenuItem _hideNoHiPriorityMenu = new MenuItem(_resources, MoneyResource.HIDENOHIPRIORITY, 170, 170) {
		public void run() {
			_hideNoHiPriority = !_hideNoHiPriority;
			if (_hideNoHiPriority) {
				//#ifdef stringprovider
				this.setText(new StringProvider("\u2713 " + _resources.getString(MoneyResource.HIDENOHIPRIORITY)));
				//#else
				this.setText("\u2713 " + _resources.getString(MoneyResource.HIDENOHIPRIORITY));
				//#endif
			} else {
				//#ifdef stringprovider
				this.setText(new StringProvider(_resources.getString(MoneyResource.HIDENOHIPRIORITY)));
				//#else
				this.setText(_resources.getString(MoneyResource.HIDENOHIPRIORITY));
				//#endif
			}
			applyFilter();
		}
	};

	private MenuItem _hideChequedMenu = new MenuItem(_resources, MoneyResource.HIDECHEQUED, 180, 180) {
		public void run() {
			_hideChequed = !_hideChequed;
			if (_hideChequed) {
				//#ifdef stringprovider
				this.setText(new StringProvider("\u2713 " + _resources.getString(MoneyResource.HIDECHEQUED)));
				//#else
				this.setText("\u2713 " + _resources.getString(MoneyResource.HIDECHEQUED));
				//#endif
			} else {
				//#ifdef stringprovider
				this.setText(new StringProvider(_resources.getString(MoneyResource.HIDECHEQUED)));
				//#else
				this.setText(_resources.getString(MoneyResource.HIDECHEQUED));
				//#endif
			}
			applyFilter();
		}
	};

	private MenuItem _hidePaidMenu = new MenuItem(_resources, MoneyResource.HIDEPAID, 190, 190) {
		public void run() {
			_hidePaid = !_hidePaid;
			if (_hidePaid) {
				//#ifdef stringprovider
				this.setText(new StringProvider("\u2713 " + _resources.getString(MoneyResource.HIDEPAID)));
				//#else
				this.setText("\u2713 " + _resources.getString(MoneyResource.HIDEPAID));
				//#endif
			} else {
				//#ifdef stringprovider
				this.setText(new StringProvider(_resources.getString(MoneyResource.HIDEPAID)));
				//#else
				this.setText(_resources.getString(MoneyResource.HIDEPAID));
				//#endif
			}
			applyFilter();
		}
	};

	private MenuItem _cleanPaid = new MenuItem(_resources, MoneyResource.CLEAN, 200, 200) {
		public void run() {
			if (Dialog.ask(Dialog.D_YES_NO, _resources.getString(MoneyResource.ASKFORCLEAN), Dialog.NO) == Dialog.YES) {
				int len = _items.length;
				int i;
				for (i = 0; i < len; i++) {
					_items[i].setChequed(false);
					_items[i].setPaid(false);
				}
				applyFilter();
				_description.setDirty(true);
			}
		}
	};

	private MenuItem _changeCurrency = new MenuItem(_resources, MoneyResource.CHANGECURRENCY, 210, 210) {
		public void run() {
			GetCurrencyScreen selectCurrencyScreen = new GetCurrencyScreen(_currency);
			if (selectCurrencyScreen.pickCurrency()) {
				_currency = selectCurrencyScreen.getCurrency();
				_listField.invalidate();
				_description.setDirty(true);
			}
		}
	};

	private void saveShopinglist() {
		if (_shopingList == null) {
			ShopingListPersist.getInstance().add(getShopinglist());
		} else {
			ShopingList shopingList = getShopinglist();
			shopingList.setId(_shopingList.getUID());
			ShopingListPersist.getInstance().update(_shopingList, shopingList);
		}
	}

	public void setShopinglist(ShopingList shopingList) {
		boolean taxed = shopingList instanceof TaxedShopingList;
		TaxedShopingList taxedShopingList = null;
		if (taxed)
			taxedShopingList = (TaxedShopingList) shopingList;

		_mainScreen.setTitle(_resources.getString(MoneyResource.EDITSHOPINGLIST));
		_shopingList = shopingList;
		_description.setText(shopingList.getDescription());
		_currency = CurrencyPersist.getInstance().get(shopingList.getCurrency());
		if (taxed) {
			_taxRate = taxedShopingList.getTaxRate();
		}

		String[] descriptions = shopingList.getDescriptions();
		int[] categories = shopingList.getAccounts();
		long[] amounts = shopingList.getAmounts();
		boolean[] priorities = shopingList.getPriorities();
		int[] quantities = shopingList.getQuantities();
		byte[] units = shopingList.getUnits();
		String[] memos = shopingList.getMemos();
		boolean[] chequed = shopingList.getChequed();
		boolean[] checkout = shopingList.getCheckout();
		boolean[] taxable = null;
		if (taxed) {
			taxable = taxedShopingList.getTaxable();
		}

		int i;
		int len = descriptions.length;
		_items = new Item[len];
		for (i = 0; i < len; i++) {
			_items[i] = new Item();
			_items[i].setDescription(descriptions[i]);
			_items[i].setCategory(categories[i]);
			_items[i].setAmount(amounts[i]);
			_items[i].setHiPriority(priorities[i]);
			_items[i].setQuantity(quantities[i]);
			_items[i].setUnit(units[i]);
			_items[i].setMemo(memos[i]);
			_items[i].setChequed(chequed[i]);
			_items[i].setPaid(checkout[i]);
			if (taxed)
				_items[i].setTaxable(taxable[i]);
			else
				_items[i].setTaxable(false);
		}
		_filteredItems = _items;
		_listField.set(_filteredItems);
	}

	public ShopingList getShopinglist() {
		int i;
		int len = _items.length;

		String[] descriptions = new String[len];
		int[] categories = new int[len];
		long[] amounts = new long[len];
		boolean[] priorities = new boolean[len];
		int[] quantities = new int[len];
		byte[] units = new byte[len];
		String[] memos = new String[len];
		boolean[] chequed = new boolean[len];
		boolean[] checkout = new boolean[len];
		boolean[] taxable = new boolean[len];

		for (i = 0; i < len; i++) {
			descriptions[i] = _items[i].getDescription();
			categories[i] = _items[i].getCategory();
			amounts[i] = _items[i].getAmount();
			priorities[i] = _items[i].isHiPriority();
			quantities[i] = _items[i].getQuantity();
			units[i] = _items[i].getUnit();
			memos[i] = _items[i].getMemo();
			chequed[i] = _items[i].isChequed();
			checkout[i] = _items[i].isPaid();
			taxable[i] = _items[i].isTaxable();
		}
		ShopingList shopingList;
		if (_taxRate != 0) {
			TaxedShopingList taxedShopingList = new TaxedShopingList();
			taxedShopingList.setTaxRate(_taxRate);
			taxedShopingList.setTaxable(taxable);
			shopingList = taxedShopingList;
		} else {
			shopingList = new ShopingList();
		}
		shopingList.setId(UIDGenerator.getUID());
		shopingList.setDescription(_description.getText());
		shopingList.setCurrency(_currency.getUID());
		shopingList.setDescriptions(descriptions);
		shopingList.setAccounts(categories);
		shopingList.setAmounts(amounts);
		shopingList.setPriorities(priorities);
		shopingList.setQuantities(quantities);
		shopingList.setUnits(units);
		shopingList.setMemos(memos);
		shopingList.setChequed(chequed);
		shopingList.setCheckout(checkout);

		return shopingList;
	}

	private void applyFilter() {
		_isFiltered = _hideChequed || _hideNoHiPriority || _hidePaid;

		if (_isFiltered) {
			Vector vector = new Vector();
			int len = _items.length;
			long tmp;
			int i;
			_total = 0;
			for (i = 0; i < len; i++) {
				if (_items[i].isChequed() && !_items[i].isPaid()) {
					tmp = _items[i].getAmount() * _items[i].getQuantity();
					_total += tmp;
				}
				if ((_hideNoHiPriority && !_items[i].isHiPriority()) || (_hideChequed && _items[i].isChequed()) || (_hidePaid && _items[i].isPaid())) {
					continue;
				}
				vector.addElement(_items[i]);
			}
			_filteredItems = new Item[vector.size()];
			vector.copyInto(_filteredItems);
		} else {
			int len = _items.length;
			long tmp;
			int i;
			_total = 0;
			for (i = 0; i < len; i++) {
				if (_items[i].isChequed() && !_items[i].isPaid()) {
					tmp = _items[i].getAmount() * _items[i].getQuantity();
					_total += tmp;
				}
			}
			_filteredItems = _items;
		}
		_totalField.setText(Util.toString(_total, _currency, true));
		_listField.set(_filteredItems);
	}

	private FocusChangeListener _focusChangeListener = new FocusChangeListener() {
		public void focusChanged(Field field, int eventType) {
			if (field instanceof ListField) {
				if (eventType == FocusChangeListener.FOCUS_GAINED) {
					_splitsFocus = true;
				}
			} else if (field instanceof EditField) {
				if (eventType == FocusChangeListener.FOCUS_GAINED) {
					_splitsFocus = false;
				}
			}
		}
	};

	private ObjectListField _listField = new ObjectListField() {
		public void drawListRow(ListField listField, Graphics graphics, int index, int y, int width) {
			Item item = (Item) this.get(this, index);
			int _fontHeight = Font.getDefault().getHeight();
			int _fontHeight2 = (_fontHeight * 2) - 1;
			String text;
			int oldColor = graphics.getColor();
			boolean changeColor = oldColor == Color.BLACK;
			long amount = item.getAmount();
			int w2 = width / 2;
			int sum = 0;

			if (_checklist) {
				if (item.isChequed())
					text = String.valueOf(Characters.BALLOT_BOX_WITH_CHECK);
				else
					text = String.valueOf(Characters.BALLOT_BOX);
				graphics.drawText(text, 0, y, 0, _fontHeight);
				sum = _fontHeight;
			}

			text = (item.isHiPriority() ? "!" : " ") + String.valueOf(item.getQuantity()) + " - " + item.getDescription();
			graphics.drawText(text, sum, y, 0, width);

			Category category = _categoryPersist.get(item.getCategory(), 0);
			graphics.drawText(category.getDescription(), 0, y + _fontHeight, 0, w2);
			graphics.setColor(oldColor);

			text = Util.toString(amount, _currency, false);
			graphics.drawText(text, w2, y + _fontHeight, Graphics.RIGHT, w2);
			if (changeColor) {
				graphics.setColor(0xd3d3d3);
				graphics.drawLine(0, y + _fontHeight2, width, y + _fontHeight2);
			}
			graphics.setColor(oldColor);
		}
	};

}
