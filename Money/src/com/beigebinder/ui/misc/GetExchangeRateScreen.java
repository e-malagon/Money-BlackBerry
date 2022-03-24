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

import com.beigebinder.data.Currency;
import com.beigebinder.misc.Util;
import com.beigebinder.resource.MoneyResource;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.container.PopupScreen;

public class GetExchangeRateScreen extends PopupScreen {
	private PopupScreen _popPopupScreen;

	private EditField _fromAmountEditField;
	private EditField _exchangeRateEditField;
	private EditField _toAmountEditField;

	private long _toAmount;
	private long _fromAmount;
	private long _exchangeRate;

	private Currency _oldCurrency;
	private Currency _newCurrency;
	static private Currency _exCurrency = new Currency(0, "", "", "-#", "",
			"-", ".", 6, ",", 33333333);

	private boolean _isCanceled;

	private static ResourceBundle _resources = ResourceBundle.getBundle(
			MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public GetExchangeRateScreen(Currency fromCurrency, Currency toCurrency,
			long fromAmount, String exchangeRate) {
		super(new DialogFieldManager(), 0);
		_popPopupScreen = this;
		_isCanceled = false;
		DialogFieldManager dfm = (DialogFieldManager) getDelegate();

		_oldCurrency = fromCurrency;
		_newCurrency = toCurrency;

		_fromAmount = Math.abs(fromAmount);
		_exchangeRate = Util.toLong(exchangeRate, _exCurrency);
		_toAmount = Util.exchangeToAmount(_fromAmount, _exchangeRate,
				_oldCurrency, _exCurrency, _newCurrency);
		
		_fromAmountEditField = new EditField(_oldCurrency.getISOCode() + " "
				+ _oldCurrency.getSign(), Util.toString(_fromAmount,
				_oldCurrency, true), 15, EditField.FILTER_REAL_NUMERIC
				| EditField.NO_NEWLINE | EditField.NON_FOCUSABLE);
		dfm.addCustomField(_fromAmountEditField);

		LabelField field = new LabelField(_resources
				.getString(MoneyResource.EXCHANGEFACTOR));
		dfm.addCustomField(field);

		_exchangeRateEditField = new EditField("", Util.toString(_exchangeRate,
				_exCurrency, true), 17, EditField.FILTER_REAL_NUMERIC
				| EditField.NO_NEWLINE);
		dfm.addCustomField(_exchangeRateEditField);

		_toAmountEditField = new EditField(_newCurrency.getISOCode() + " "
				+ _newCurrency.getSign(), Util.toString(_toAmount,
				_newCurrency, true), 15, EditField.FILTER_REAL_NUMERIC
				| EditField.NO_NEWLINE);
		dfm.addCustomField(_toAmountEditField);

		ButtonField buttonField = new ButtonField(_resources
				.getString(MoneyResource.CALCULATE), ButtonField.FIELD_HCENTER);
		FieldChangeListener changeListener = new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (context != FieldChangeListener.PROGRAMMATIC) {

					String exrt = _exchangeRateEditField.getText();
					if (exrt.length() == 0) {
						_toAmount = Util.toLong(_toAmountEditField.getText(),
								_newCurrency);
						String extrl = Util.amountToExchange(_toAmount,
								_fromAmount);
						extrl = Util.toString(Util.toLong(extrl, _exCurrency),
								_exCurrency, true);
						_exchangeRateEditField.setText(extrl);
					} else {
						long extrl = Util.toLong(exrt, _exCurrency);
						_toAmount = Util.exchangeToAmount(_fromAmount, extrl,
								_oldCurrency, _exCurrency, _newCurrency);
						_toAmountEditField.setText(Util.toString(_toAmount,
								_newCurrency, true));
					}

				}
			}
		};
		buttonField.setChangeListener(changeListener);
		dfm.addCustomField(buttonField);

		buttonField = new ButtonField(_resources.getString(MoneyResource.SAVE),
				ButtonField.FIELD_HCENTER);
		changeListener = new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (context != FieldChangeListener.PROGRAMMATIC) {
					_exchangeRate = Util.toLong(Util.amountToExchange(
							_toAmount, _fromAmount), _exCurrency);
					UiApplication.getUiApplication().popScreen(_popPopupScreen);
				}
			}
		};
		buttonField.setChangeListener(changeListener);
		dfm.addCustomField(buttonField);

		buttonField = new ButtonField(_resources
				.getString(MoneyResource.CANCEL), ButtonField.FIELD_HCENTER);
		changeListener = new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (context != FieldChangeListener.PROGRAMMATIC) {
					_isCanceled = true;
					UiApplication.getUiApplication().popScreen(_popPopupScreen);
				}
			}
		};
		buttonField.setChangeListener(changeListener);
		dfm.addCustomField(buttonField);
	}

	public boolean pickExchangeRate() {
		UiApplication.getUiApplication().pushModalScreen(this);
		return !_isCanceled;
	}

	public long getToAmount() {
		return _toAmount;
	}

	public long getExchangeRate() {
		return _exchangeRate;
	}

	static public Currency getExchangeRateCurrency() {
		return _exCurrency;
	}

	protected boolean keyChar(char key, int status, int time) {
		boolean retval = false;
		switch (key) {
		case Characters.ESCAPE:
			retval = true;
			_isCanceled = true;
			UiApplication.getUiApplication().popScreen(_popPopupScreen);
			break;
		default:
			retval = super.keyChar(key, status, time);
			break;
		}
		return retval;
	}
}
