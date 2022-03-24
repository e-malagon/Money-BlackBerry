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

import java.io.InputStream;

import com.beigebinder.resource.MoneyResource;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.container.PopupScreen;

public class AboutScreen extends PopupScreen{
	private static ResourceBundle _resources = ResourceBundle.getBundle(
			MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public AboutScreen() {
		super(new DialogFieldManager(), DEFAULT_MENU);
		DialogFieldManager dfm = (DialogFieldManager) getDelegate();
		EncodedImage image = null;

		try {
			byte[] data = new byte[8191];
			InputStream input = Class.forName(
					"com.beigebinder.ui.misc.AboutScreen")
					.getResourceAsStream("/BeigeBinder.png");

			input.read(data);
			image = EncodedImage.createEncodedImage(data, 0, data.length);
		} catch (Exception ex) {
			Dialog.inform(ex.toString());
		}

		BitmapField bitmapField = new BitmapField(image.getBitmap(),
				BitmapField.FIELD_HCENTER);

		LabelField aboutMessage = new LabelField(_resources
				.getString(MoneyResource.ABOUTMESSAGE),
				LabelField.FIELD_HCENTER);

		LabelField aboutMessage2 = new LabelField(_resources
				.getString(MoneyResource.ABOUTMESSAGE2),
				LabelField.FIELD_HCENTER);

		LabelField aboutMessage3 = new LabelField("", LabelField.FIELD_HCENTER);

		ApplicationDescriptor apDes = ApplicationDescriptor
				.currentApplicationDescriptor();

		LabelField version = new LabelField(_resources
				.getString(MoneyResource.VERSION)
				+ apDes.getVersion());
		
		dfm.addCustomField(bitmapField);
		dfm.addCustomField(aboutMessage);
		dfm.addCustomField(aboutMessage2);
		dfm.addCustomField(aboutMessage3);
		dfm.addCustomField(version);
		UiApplication.getUiApplication().pushModalScreen(this);
	}

	protected boolean keyChar(char c, int status, int time) {
		if (c == Characters.ESCAPE) {
			UiApplication.getUiApplication().popScreen(this);
			return true;
		}
		return super.keyChar(c, status, time);
	}
}
