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
import net.rim.device.api.util.Comparator;
import net.rim.device.api.util.Persistable;

public class Notification implements Persistable, Comparator, SyncObject {
	private int _id;
	private String _description;
	private short _type;
	private long _nextExecutionDate;
	private int _daysForAlert;
	private int _notificationsLeft;
	private int _account;
	private int _transactionId;
	private boolean _dirty;

	public boolean isDirty() {
		return _dirty;
	}

	public void setDirty(boolean dirty) {
		this._dirty = dirty;
	}

	public Notification() {
		_dirty = true;
	}

	public Notification(int id) {
		this();
		_id = id;
	}

	public int getUID() {
		return _id;
	}

	public void setId(int id) {
		this._id = id;
	}

	public short getType() {
		return _type;
	}

	public void setType(short type) {
		this._type = type;
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

	public long getNextExecutionDate() {
		return _nextExecutionDate;
	}

	public void setNextExecutionDate(long lastExecutionDate) {
		this._nextExecutionDate = lastExecutionDate;
	}

	public int getDaysForAlert() {
		return _daysForAlert;
	}

	public void setDaysForAlert(int daysForAlert) {
		this._daysForAlert = daysForAlert;
	}

	public int getNotificationsLeft() {
		return _notificationsLeft;
	}

	public void setNotificationsLeft(int notificationsLeft) {
		this._notificationsLeft = notificationsLeft;
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

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof Notification) {
			Notification other = (Notification) obj;
			return other._id == this._id;
		}
		return false;
	}

	public int compare(Object o1, Object o2) {
		if (!(o1 instanceof Notification && o2 instanceof Notification)) {
			throw new IllegalArgumentException();
		}
		Notification schedule1 = (Notification) o1;
		Notification schedule2 = (Notification) o2;
		return schedule1._id - schedule2._id;
	}
}
