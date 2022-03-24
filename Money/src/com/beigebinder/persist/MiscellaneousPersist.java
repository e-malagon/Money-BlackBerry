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
package com.beigebinder.persist;

import java.util.Vector;

import com.beigebinder.data.Miscellaneous;
import com.beigebinder.misc.UpdateCallback;
import net.rim.device.api.system.ObjectGroup;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.RuntimeStore;

public class MiscellaneousPersist {
	private static final long MISCELLANEOUSPERSIST = 0x66fe1e2ca9d4c4f7L; // com.beigebinder.logic.MiscellaneousPersist.MISCELLANEOUSPERSIST
	private static final long MISCELLANEOUS = 0x9c33f7963881a206L; // com.beigebinder.logic.MiscellaneousPersist.MISCELLANEOUS

	private PersistentObject _miscellaneousStore;
	private Miscellaneous _miscellaneous;
	private Vector _suscriptionUpdate;

	private MiscellaneousPersist() {
		_miscellaneousStore = PersistentStore.getPersistentObject(MISCELLANEOUSPERSIST);
		_miscellaneous = (Miscellaneous) _miscellaneousStore.getContents();
		_suscriptionUpdate = new Vector();
		if (_miscellaneous == null) {
			_miscellaneous = new Miscellaneous();
			_miscellaneous.setKey("");
			_miscellaneousStore.setContents(_miscellaneous);
			_miscellaneousStore.commit();
		}
	}

	public static MiscellaneousPersist getInstance() {
		RuntimeStore runtimeStore = RuntimeStore.getRuntimeStore();
		MiscellaneousPersist optionsLogic = (MiscellaneousPersist) runtimeStore.get(MISCELLANEOUS);
		if (optionsLogic == null) {
			optionsLogic = new MiscellaneousPersist();
			runtimeStore.put(MISCELLANEOUS, optionsLogic);
		}
		return optionsLogic;
	}

	public void suscribe(UpdateCallback callback) {
		_suscriptionUpdate.addElement(callback);
	}

	public void unSuscribe(UpdateCallback callback) {
		_suscriptionUpdate.removeElement(callback);
	}

	public void update() {
		int size = _suscriptionUpdate.size();
		UpdateCallback callback;
		for (int index = 0; index < size; index++) {
			callback = (UpdateCallback) _suscriptionUpdate.elementAt(index);
			callback.update();
		}
	}

	public String getKey() {
		return _miscellaneous.getKey();
	}

	public void setKey(String key) {
		Miscellaneous ungroupedOptions = (Miscellaneous) ObjectGroup.expandGroup(_miscellaneous);
		ungroupedOptions.setKey(key);
		ObjectGroup.createGroup(ungroupedOptions);
		_miscellaneous = ungroupedOptions;
		_miscellaneousStore.setContents(_miscellaneous);
		_miscellaneousStore.commit();
	}

	public void setOK(long ok) {
		Miscellaneous ungroupedOptions = (Miscellaneous) ObjectGroup.expandGroup(_miscellaneous);
		ungroupedOptions.setOKey(ok);
		ObjectGroup.createGroup(ungroupedOptions);
		_miscellaneous = ungroupedOptions;
		_miscellaneousStore.setContents(_miscellaneous);
		_miscellaneousStore.commit();
	}
}
