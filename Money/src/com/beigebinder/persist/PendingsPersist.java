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

import com.beigebinder.data.SavedTransaction;
import net.rim.device.api.system.ObjectGroup;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.util.Arrays;

public class PendingsPersist {
	private static final long PENDINGPERSIST = 0x7e42afd501b84b8cL; // com.beigebinder.logic.PendingsPersist.PENDINGPERSIST
	private static final long PENDINGS = 0x659529f5aa622077L; // com.beigebinder.logic.PendingsPersist.PENDINGS

	private PersistentObject _pendingsStore;
	private SavedTransaction[] _pendings;

	private PendingsPersist() {
		_pendingsStore = PersistentStore.getPersistentObject(PENDINGPERSIST);
		_pendings = (SavedTransaction[]) _pendingsStore.getContents();
		if (_pendings == null) {
			_pendings = new SavedTransaction[0];
			_pendingsStore.setContents(_pendings);
		}
	}

	public static PendingsPersist getInstance() {
		RuntimeStore runtimeStore = RuntimeStore.getRuntimeStore();
		PendingsPersist pendingsLogic = (PendingsPersist) runtimeStore.get(PENDINGS);
		if (pendingsLogic == null) {
			pendingsLogic = new PendingsPersist();
			runtimeStore.put(PENDINGS, pendingsLogic);
		}
		return pendingsLogic;
	}

	public void add(SavedTransaction pending) {
		this.addSingle(pending);
		this.commit();
	}

	public void remove(SavedTransaction pending) {
		this.removeSingle(pending);
		this.commit();
	}

	public void updateAccount(SavedTransaction pending, int AccountId) {
		if (pending == null) {
			throw new IllegalArgumentException();
		}
		int index = Arrays.getIndex(_pendings, pending);
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		SavedTransaction ungroupedSchedule = (SavedTransaction) ObjectGroup.expandGroup(_pendings[index]);
		ungroupedSchedule.setAccount(AccountId);
		ObjectGroup.createGroup(ungroupedSchedule);
		_pendings[index] = ungroupedSchedule;
		_pendingsStore.commit();
	}

	public SavedTransaction get(int id) {
		SavedTransaction savedTransaction = new SavedTransaction(id);
		int index = Arrays.getIndex(_pendings, savedTransaction);
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		return _pendings[index];
	}

	public SavedTransaction[] get() {
		return _pendings;
	}

	/***************************************************************************************/

	public void addSingle(SavedTransaction pending) {
		ObjectGroup.createGroup(pending);
		Arrays.add(_pendings, pending);
	}

	public void removeSingle(SavedTransaction pending) {
		if (pending == null) {
			throw new IllegalArgumentException();
		}
		Arrays.remove(_pendings, pending);
	}

	public void updateSingle(SavedTransaction oldPending, SavedTransaction newPending) {
		if (oldPending == null || newPending == null) {
			throw new IllegalArgumentException();
		}
		int index = Arrays.getIndex(_pendings, oldPending);
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		SavedTransaction ungroupedSchedule = (SavedTransaction) ObjectGroup.expandGroup(_pendings[index]);
		ungroupedSchedule.setDescription(newPending.getDescription());
		ungroupedSchedule.setAccount(newPending.getAccount());
		ObjectGroup.createGroup(ungroupedSchedule);
		_pendings[index] = ungroupedSchedule;
	}

	public void removeAll() {
		_pendings = new SavedTransaction[0];
		_pendingsStore.setContents(_pendings);
	}

	public void setDirty(SavedTransaction pending, boolean dirty) {
		if (pending == null) {
			throw new IllegalArgumentException();
		}
		int index = Arrays.getIndex(_pendings, pending);
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		SavedTransaction ungroupedSchedule = (SavedTransaction) ObjectGroup.expandGroup(_pendings[index]);
		ungroupedSchedule.setDirty(dirty);
		ObjectGroup.createGroup(ungroupedSchedule);
		_pendings[index] = ungroupedSchedule;
	}

	public void commit() {
		_pendingsStore.commit();
	}

	/***************************************************************************************/
}
