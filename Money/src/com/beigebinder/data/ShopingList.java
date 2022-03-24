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
import net.rim.device.api.util.Persistable;

public class ShopingList implements Persistable, SyncObject {
	protected int _id;
	protected String _description;
	protected int _currency;
	private String[] _descriptions;
	private int[] _accounts;
	private long[] _amounts;
	protected boolean[] _priorities;
	protected int[] _quantities;
	protected byte[] _units;
	protected String[] _memos;
	protected boolean[] _checked;
	protected boolean[] _checkout;
	protected boolean _dirty;

	public ShopingList() {
		_dirty = true;
	}

	public ShopingList(int id) {
		this();
		_id = id;
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

	public int getCurrency() {
		return _currency;
	}

	public void setCurrency(int currency) {
		this._currency = currency;
	}

	public boolean[] getPriorities() {
		return _priorities;
	}

	public void setPriorities(boolean[] priorities) {
		this._priorities = priorities;
	}

	public int[] getQuantities() {
		return _quantities;
	}

	public void setQuantities(int[] quantities) {
		this._quantities = quantities;
	}

	public byte[] getUnits() {
		return _units;
	}

	public void setUnits(byte[] units) {
		this._units = units;
	}

	public String[] getMemos() {
		return _memos;
	}

	public void setMemos(String[] memos) {
		this._memos = memos;
	}

	public String[] getDescriptions() {
		return _descriptions;
	}

	public void setDescriptions(String[] descriptions) {
		this._descriptions = descriptions;
	}

	public int[] getAccounts() {
		return _accounts;
	}

	public void setAccounts(int[] accounts) {
		this._accounts = accounts;
	}

	public long[] getAmounts() {
		return _amounts;
	}

	public void setAmounts(long[] amounts) {
		this._amounts = amounts;
	}

	public boolean[] getChequed() {
		return _checked;
	}

	public void setChequed(boolean[] checked) {
		this._checked = checked;
	}

	public boolean[] getCheckout() {
		return _checkout;
	}

	public void setCheckout(boolean[] checkout) {
		this._checkout = checkout;
	}

	public boolean isDirty() {
		return _dirty;
	}

	public void setDirty(boolean dirty) {
		this._dirty = dirty;
	}

	public String toString() {
		return this._description;
	}

	public boolean equals(Object obj) {
		if (obj instanceof ShopingList) {
			ShopingList shopingList = (ShopingList) obj;
			if (this._id == shopingList._id)
				return true;
		}
		return false;
	}

}
