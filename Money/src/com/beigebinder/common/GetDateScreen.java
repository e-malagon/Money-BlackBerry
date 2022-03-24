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
package com.beigebinder.common;

import com.beigebinder.resource.MoneyResource;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.DateField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.util.DateTimeUtilities;

public class GetDateScreen extends PopupScreen {
	private PopupScreen _popPopupScreen;
	private DateField _date;
	private boolean _isCanceled;

	private static ResourceBundle _resources = ResourceBundle.getBundle(MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public GetDateScreen(String title, long date) {
		super(new DialogFieldManager(), 0);
		_popPopupScreen = this;
		DialogFieldManager dfm = (DialogFieldManager) getDelegate();
		dfm.setMessage(new RichTextField(title, RichTextField.NON_FOCUSABLE));

		_date = new DateField(_resources.getString(MoneyResource.DATE), date, DateField.DATE);
		dfm.addCustomField(_date);

		ButtonField buttonField = new ButtonField(_resources.getString(MoneyResource.SAVE), ButtonField.FIELD_HCENTER);
		FieldChangeListener changeListener = new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (context != FieldChangeListener.PROGRAMMATIC) {
					UiApplication.getUiApplication().popScreen(_popPopupScreen);
				}
			}
		};
		buttonField.setChangeListener(changeListener);
		dfm.addCustomField(buttonField);

		buttonField = new ButtonField(_resources.getString(MoneyResource.CANCEL), ButtonField.FIELD_HCENTER);
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
		_isCanceled = false;
	}

	public GetDateScreen(String title) {
		this(title, DateTimeUtilities.getDate(0).getTime().getTime());
	}

	public boolean pickDate() {
		UiApplication.getUiApplication().pushModalScreen(this);
		return !_isCanceled;
	}

	public long getDate() {
		return _date.getDate();
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
