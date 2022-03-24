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

public class Transaction implements Persistable, Comparator, SyncObject {
	protected int _id;
	protected int _parentAccount;
	protected byte _status;
	protected String _number;
	protected String _description;
	protected long _date;
	protected long _amount;
	protected boolean _dirty;

	public boolean isDirty() {
		return _dirty;
	}

	public void setDirty(boolean dirty) {
		this._dirty = dirty;
	}

	public Transaction() {
		_dirty = true;
	}

	public Transaction(int id) {
		this();
		_id = id;
	}

	public int getUID() {
		return _id;
	}

	public void setId(int id) {
		this._id = id;
	}

	public int getParentAccount() {
		return _parentAccount;
	}

	public void setParentAccount(int parentAccount) {
		this._parentAccount = parentAccount;
	}

	public byte getStatus() {
		return _status;
	}

	public void setStatus(byte status) {
		this._status = status;
	}

	public String getNumber() {
		return _number;
	}

	public void setNumber(String number) {
		this._number = number;
	}

	public String getDescription() {
		return _description;
	}

	public void setDescription(String description) {
		this._description = description;
	}

	public long getDate() {
		return _date;
	}

	public void setDate(long date) {
		this._date = date;
	}

	public long getAmount() {
		return _amount;
	}

	public void setAmount(long amount) {
		this._amount = amount;
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof Transaction) {
			Transaction other = (Transaction) obj;
			return other._id == this._id;
		}
		return false;
	}

	public int compare(Object o1, Object o2) {
		if (!(o1 instanceof Transaction && o2 instanceof Transaction)) {
			throw new IllegalArgumentException();
		}
		Transaction transaction1 = (Transaction) o1;
		Transaction transaction2 = (Transaction) o2;

		return transaction1._date == transaction2._date ? 0 : transaction1._date > transaction2._date ? -1 : 1;
	}

	public Object clone() {
		Transaction transaction = new Transaction();
		transaction._id = _id;
		transaction._parentAccount = _parentAccount;
		transaction._status = _status;
		transaction._number = _number;
		transaction._description = new String(_description);
		transaction._date = _date;
		transaction._amount = _amount;
		transaction._dirty = _dirty;
		return transaction;
	}
}
