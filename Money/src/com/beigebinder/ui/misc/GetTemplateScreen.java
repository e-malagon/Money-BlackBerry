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

import com.beigebinder.data.SavedTransaction;
import com.beigebinder.persist.TemplatesPersist;
import com.beigebinder.resource.MoneyResource;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.container.PopupScreen;

public class GetTemplateScreen extends PopupScreen {
	private PopupScreen _splashScreen;	
	private ObjectListField _templatesList;
	private boolean _isCanceled;

	private static ResourceBundle _resources = ResourceBundle.getBundle(
			MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public GetTemplateScreen() {
		super(new DialogFieldManager(), DEFAULT_MENU);
		_splashScreen = this;
		DialogFieldManager dfm = (DialogFieldManager) getDelegate();
		dfm.setMessage(new RichTextField(_resources
				.getString(MoneyResource.TEMPLATES), RichTextField.NON_FOCUSABLE | RichTextField.FIELD_HCENTER));

		_templatesList = new ObjectListField() {
			protected boolean navigationUnclick(int status, int time) {
				int selectedIndex = _templatesList.getSelectedIndex();
				if (selectedIndex != -1) {
					UiApplication.getUiApplication().popScreen(_splashScreen);
					return true;
				} else
					return super.navigationUnclick(status, time);
			}
		};
		_templatesList.setEmptyString(_resources
				.getString(MoneyResource.WHITOUTTEMPLATES), DrawStyle.HCENTER);
		Object[] elements = TemplatesPersist.getInstance().get();
		_templatesList.set(elements);
		_templatesList.setSize(elements.length);
		dfm.addCustomField(_templatesList);
		
		_isCanceled = false;
		
	}

	public boolean pickTemplate() {
		UiApplication.getUiApplication().pushModalScreen(this);
		return !_isCanceled;
	}

	public SavedTransaction getTemplate() {
		int selectedIndex = _templatesList.getSelectedIndex();
		return (SavedTransaction) _templatesList
		.getCallback().get(_templatesList, selectedIndex);
	}

	protected void makeMenu(Menu menu, int instance) {
		menu.add(_save);
		menu.add(_close);
		super.makeMenu(menu, instance);
	}

	private MenuItem _close = new MenuItem(_resources, MoneyResource.CLOSE, 100, 100) {
		public void run() {
			_isCanceled = true;
			close();
		}
	};

	private MenuItem _save = new MenuItem(_resources, MoneyResource.SAVE, 110, 110) {
		public void run() {
			close();
		}
	};
	
	protected boolean keyChar(char c, int status, int time) {
		if (c == Characters.ESCAPE) {
			_isCanceled = true;
			this.close();
			return true;
		} else if (c == Characters.ENTER) {
			this.close();
			return true;
		}

		return super.keyChar(c, status, time);
	}
}
