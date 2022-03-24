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
package com.beigebinder.data;

import net.rim.device.api.synchronization.SyncObject;
import net.rim.device.api.synchronization.UIDGenerator;
import net.rim.device.api.util.Comparator;
import net.rim.device.api.util.Persistable;

public class SavedTransaction implements Persistable, Comparator, SyncObject {
	private int _id;
	private String _description;
	private int _transactionId;
	private int _account;
	private short _used;
	private boolean _dirty;

	public boolean isDirty() {
		return _dirty;
	}

	public void setDirty(boolean dirty) {
		this._dirty = dirty;
	}

	public SavedTransaction() {
		_dirty = true;
		_used = 0;
	}

	public SavedTransaction(int id) {
		this();
		_id = id;
	}

	public SavedTransaction(Notification schedule) {
		_id = UIDGenerator.getUID();
		_description = schedule.getDescription();
		_transactionId = schedule.getTransactionId();
		_account = schedule.getAccount();
	}

	public int getUID() {
		return _id;
	}

	public void setId(int id) {
		this._id = id;
	}

	public String getDescription() {
		return _description;
	}

	public void setDescription(String description) {
		this._description = description;
	}

	public int getTransactionId() {
		return _transactionId;
	}

	public void setTransactionId(int transactionId) {
		this._transactionId = transactionId;
	}

	public int getAccount() {
		return _account;
	}

	public void setAccount(int account) {
		this._account = account;
	}

	public String toString() {
		return _description;
	}

	public void incUsed() {
		_used++;
	}

	public void setUsed(short used) {
		_used = used;
	}

	public short getUsed() {
		return _used;
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof SavedTransaction) {
			SavedTransaction other = (SavedTransaction) obj;
			if (other._id == this._id)
				return true;
		}
		return false;
	}

	public int compare(Object o1, Object o2) {
		if (!(o1 instanceof SavedTransaction && o2 instanceof SavedTransaction)) {
			throw new IllegalArgumentException();
		}
		SavedTransaction savedTransaction1 = (SavedTransaction) o1;
		SavedTransaction savedTransaction2 = (SavedTransaction) o2;
		return savedTransaction2._used - savedTransaction1._used;
	}
}
