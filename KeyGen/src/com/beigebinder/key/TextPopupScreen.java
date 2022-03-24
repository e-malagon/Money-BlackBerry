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
package com.beigebinder.key;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.container.PopupScreen;

public class TextPopupScreen extends PopupScreen {
	private EditField _text;
	private boolean _cancel;

	public TextPopupScreen(String title, String text, long flags) {
		super(new DialogFieldManager(), DEFAULT_MENU);
		DialogFieldManager dfm = (DialogFieldManager) getDelegate();
		dfm.setMessage(new RichTextField(title, RichTextField.NON_FOCUSABLE));

		_text = new EditField(flags);
		_text.setText(text);
		_cancel = false;
		dfm.addCustomField(_text);
	}

	public TextPopupScreen(String title) {
		this(title, "", EditField.NO_NEWLINE | EditField.NO_COMPLEX_INPUT);
	}

	public boolean pickText() {
		UiApplication.getUiApplication().pushModalScreen(this);
		return !_cancel;
	}

	public String getText() {
		return _text.getText();
	}

	protected boolean keyChar(char c, int status, int time) {
		// Close this screen if escape is selected.
		if (c == Characters.ESCAPE) {
			_text.setText("");
			_cancel = true;
			this.close();
			return true;
		} else if (c == Characters.ENTER) {
			this.close();
			return true;
		}

		return super.keyChar(c, status, time);
	}

	protected void makeMenu(Menu menu, int instance) {
		menu.add(_save);
		menu.add(_close);
		super.makeMenu(menu, instance);
	}

	private MenuItem _close = new MenuItem("Close", 40, 40) {
		public void run() {
			_text.setText("");
			close();
		}
	};

	private MenuItem _save = new MenuItem("Save", 40, 40) {
		public void run() {
			close();
		}
	};
}
