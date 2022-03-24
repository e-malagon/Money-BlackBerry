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

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

import com.beigebinder.resource.MoneyResource;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.container.PopupScreen;

public class GetFileScreen extends PopupScreen {

	private String _currentPath;
	private String[] _extensions;
	private ObjectListField _olf;
	private EncodedImage _image = null;
	private boolean _directory;
	private LabelField _path;

	private static ResourceBundle _resources = ResourceBundle.getBundle(MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	public GetFileScreen() {
		this(null, null, false);
	}

	public GetFileScreen(String startPath, String[] extensions, boolean directory) {
		super(new DialogFieldManager(), DEFAULT_MENU);
		_extensions = extensions;
		_directory = directory;
		prepScreen(startPath);
	}

	public void pickFile() {
		UiApplication.getUiApplication().pushModalScreen(this);
	}

	public String getFile() {
		return _currentPath;
	}

	private void prepScreen(String path) {
		String message;
		if (_directory)
			message = _resources.getString(MoneyResource.SELECTAFOLDER);
		else
			message = _resources.getString(MoneyResource.SELECTAFILE);
		DialogFieldManager dfm = (DialogFieldManager) getDelegate();
		dfm.setMessage(new RichTextField(message, RichTextField.NON_FOCUSABLE));

		try {
			byte[] data = new byte[8191];
			InputStream input = Class.forName("com.beigebinder.common.GetFileScreen").getResourceAsStream("/Folder.png");

			input.read(data);
			_image = EncodedImage.createEncodedImage(data, 0, data.length);
		} catch (Exception ex) {
			Dialog.inform(ex.toString());
		}

		_olf = new ObjectListField() {
			public void drawListRow(ListField listField, Graphics graphics, int index, int y, int width) {
				graphics.drawImage(0, y + ((Font.getDefault().getHeight() - _image.getHeight()) >> 1), _image.getWidth(), _image.getHeight(), _image, 0, 0, 0);
				graphics.drawText((String) _olf.get(listField, index), _image.getWidth(), y, 0, width - _image.getWidth());
			}
		};
		_path = new LabelField(_currentPath, LabelField.NON_FOCUSABLE | LabelField.USE_ALL_WIDTH);
		dfm.addCustomField(_path);
		dfm.addCustomField(new SeparatorField());
		dfm.addCustomField(_olf);
		updateList(path);
	}

	private Vector readFiles(String path) {
		Enumeration fileEnum;
		Vector filesVector = new Vector();

		_currentPath = path;
		_path.setText(_currentPath);

		if (path == null) {
			fileEnum = FileSystemRegistry.listRoots();

			while (fileEnum.hasMoreElements()) {
				filesVector.addElement((Object) fileEnum.nextElement());
			}
		} else {
			try {
				FileConnection fc = (FileConnection) Connector.open("file:///" + path);
				fileEnum = fc.list();
				String currentFile;

				while (fileEnum.hasMoreElements()) {
					currentFile = ((String) fileEnum.nextElement());

					if (currentFile.lastIndexOf('/') == (currentFile.length() - 1)) {
						filesVector.addElement((Object) currentFile);
					} else {
						if (!_directory) {
							if (_extensions == null) {
								filesVector.addElement((Object) currentFile);
							} else {
								for (int count = _extensions.length - 1; count >= 0; --count) {
									if (currentFile.indexOf(_extensions[count]) != -1) {
										filesVector.addElement((Object) currentFile);
										break;
									}
								}
							}
						}
					}

				}
			} catch (Exception ex) {
				Dialog.alert("Unable to open folder. " + ex.toString());
			}
		}
		return filesVector;
	}

	private void openFileorFolder(boolean back) {
		String thePath = buildPath(back);

		if (thePath == null) {
			updateList(thePath);
		} else if (!thePath.equals("*?*")) {
			updateList(thePath);
		} else {
			this.close();
		}
	}

	private void updateList(String path) {
		Vector fileList = readFiles(path);
		Object fileArray[] = vectorToArray(fileList);
		_olf.set(fileArray);
	}

	private String buildPath(boolean back) {

		String newPath;
		int selectedIndex = _olf.getSelectedIndex();
		if (selectedIndex != -1)
			newPath = (String) _olf.get(_olf, selectedIndex);
		else
			newPath = "";

		if (back) {
			newPath = _currentPath.substring(0, _currentPath.length() - 2);
			int lastSlash = newPath.lastIndexOf('/');

			if (lastSlash == -1) {
				newPath = null;
			} else {
				newPath = newPath.substring(0, lastSlash + 1);
			}
		} else if (newPath.lastIndexOf('/') == (newPath.length() - 1)) {
			if (_currentPath != null) {
				newPath = _currentPath + newPath;
			}
		} else {
			_currentPath += newPath;
			newPath = "*?*";
		}

		return newPath;
	}

	private Object[] vectorToArray(Vector filesVector) {
		int filesCount = filesVector.size();
		Object[] files;
		files = new Object[(filesCount)];
		filesVector.copyInto(files);
		return files;
	}

	protected boolean keyChar(char c, int status, int time) {
		if (c == Characters.ESCAPE) {
			_currentPath = null;
			this.close();
			return true;
		} else if (c == Characters.ENTER) {
			openFileorFolder(false);
			return true;
		}

		return super.keyChar(c, status, time);
	}

	protected void makeMenu(Menu menu, int instance) {
		if (_currentPath != null)
			menu.add(_select);
		menu.add(_open);
		if (_directory && _currentPath != null) {
			menu.add(_back);
			menu.add(_newFolder);
		}
		menu.add(_close);
		menu.setDefault(_open);
		super.makeMenu(menu, instance);
	}

	private MenuItem _select = new MenuItem(_resources, MoneyResource.SELECT, 100, 100) {
		public void run() {
			if (_directory)
				close();
			else
				openFileorFolder(false);

		}
	};

	private MenuItem _open = new MenuItem(_resources, MoneyResource.OPEN, 110, 110) {
		public void run() {
			openFileorFolder(false);
		}
	};

	private MenuItem _back = new MenuItem(_resources, MoneyResource.BACKFOLDER, 120, 120) {
		public void run() {
			openFileorFolder(true);
		}
	};

	private MenuItem _newFolder = new MenuItem(_resources, MoneyResource.NEWFOLDER, 130, 130) {
		public void run() {
			GetTextScreen popupScreen = new GetTextScreen(_resources.getString(MoneyResource.NAME));
			if (popupScreen.pickText()) {
				String directoryName = popupScreen.getText();
				try {
					FileConnection fc = (FileConnection) Connector.open("file:///" + _currentPath + directoryName + "/", Connector.READ_WRITE);
					if (!fc.exists()) {
						fc.mkdir();
					}
					updateList(_currentPath);
					fc.close();
				} catch (Exception e) {
					Dialog.inform(e.getMessage());
				}
			}
		}
	};

	private MenuItem _close = new MenuItem(_resources, MoneyResource.CLOSE, 140, 140) {
		public void run() {
			_currentPath = null;
			close();
		}
	};
}
