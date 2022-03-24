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

import net.rim.device.api.system.ObjectGroup;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.RuntimeStore;

public final class KeyPersist {
	private static final long KEYERSIST = 0x7de5a979299379a2L;
	private static final long KEY = 0x9ce8bea8ae752398L;

	private PersistentObject _keyStore;
	private KeyCode _keyCode;

	private KeyPersist() {
		_keyStore = PersistentStore.getPersistentObject(KEYERSIST);
		_keyCode = (KeyCode) _keyStore.getContents();
		if (_keyCode == null) {
			_keyCode = new KeyCode();
			_keyCode.setKey(0L);
			_keyStore.setContents(_keyCode);
			_keyStore.commit();
		}
	}

	public static KeyPersist getInstance() {
		RuntimeStore runtimeStore = RuntimeStore.getRuntimeStore();
		KeyPersist optionsLogic = (KeyPersist) runtimeStore.get(KEY);
		if (optionsLogic == null) {
			optionsLogic = new KeyPersist();
			runtimeStore.put(KEY, optionsLogic);
		}
		return optionsLogic;
	}

	public void setKey(long key) {
		KeyCode ungroupedOptions = (KeyCode) ObjectGroup.expandGroup(_keyCode);
		ungroupedOptions.setKey(key);
		ObjectGroup.createGroup(ungroupedOptions);
		_keyCode = ungroupedOptions;
		_keyStore.setContents(_keyCode);
		_keyStore.commit();
	}

	/***************************************************************************************/
	public boolean isValidKey() {
		long opKey = 3977112509L;
		opKey -= _keyCode.getKey();

		return opKey == 0;
	}
}
