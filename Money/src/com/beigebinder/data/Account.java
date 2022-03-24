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
import net.rim.device.api.util.StringUtilities;

public final class Account implements Persistable, Comparator, SyncObject {
	private int _id;
	private short _type;
	private String _description;
	private int _currencyID;
	private long _initialBalance;
	private long _finalBalance;
	private long _reconciledBalance;
	private long _clearedBalance;
	private String _memo;
	private boolean _closed;
	private boolean _dirty;

	public Account() {
		_dirty = true;
	}

	public Account(int id) {
		this();
		_id = id;
	}

	public long getInitialBalance() {
		return _initialBalance;
	}

	public void setInitialBalance(long initialBalance) {
		_initialBalance = initialBalance;
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

	public short getType() {
		return _type;
	}

	public void setType(short type) {
		this._type = type;
	}

	public String toString() {
		if (_closed)
			return "- " + _description + " -";
		else
			return _description;
	}

	public int getCurrencyID() {
		return _currencyID;
	}

	public void setCurrencyID(int currencyID) {
		this._currencyID = currencyID;
	}

	public String getMemo() {
		return _memo;
	}

	public void setMemo(String memo) {
		this._memo = memo;
	}

	public long getFinalBalance() {
		return _finalBalance;
	}

	public void setFinalBalance(long balance) {
		_finalBalance = balance;
	}

	public long getReconciledBalance() {
		return _reconciledBalance;
	}

	public void setReconciledBalance(long reconciledBalance) {
		this._reconciledBalance = reconciledBalance;
	}

	public long getClearedBalance() {
		return _clearedBalance;
	}

	public void setClearedBalance(long clearedBalance) {
		this._clearedBalance = clearedBalance;
	}

	public boolean isClosed() {
		return _closed;
	}

	public void setClosed(boolean closed) {
		this._closed = closed;
	}

	public boolean isDirty() {
		return _dirty;
	}

	public void setDirty(boolean dirty) {
		this._dirty = dirty;
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof Account) {
			Account other = (Account) obj;
			if (other._id == this._id)
				return true;
			else
				return (other._type == this._type && StringUtilities.strEqualIgnoreCase(other._description, this._description));
		}
		return false;
	}

	public int compare(Object o1, Object o2) {
		int comparacion;
		if (!(o1 instanceof Account && o2 instanceof Account)) {
			throw new IllegalArgumentException();
		}
		Account account1 = (Account) o1;
		Account account2 = (Account) o2;
		if (account1.getType() != account2.getType())
			comparacion = account1.getType() < account2.getType() ? -1 : 1;
		else
			comparacion = StringUtilities.compareToIgnoreCase(account1._description, account2._description);
		return comparacion;
	}
}
