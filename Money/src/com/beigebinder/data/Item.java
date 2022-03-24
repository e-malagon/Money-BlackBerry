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

import net.rim.device.api.synchronization.UIDGenerator;

public class Item {
	protected int _uid;
	protected String _description;
	protected int _category;
	protected long _amount;
	protected long _oldAmount;
	protected boolean _hiPriority;
	protected int _quantity;
	protected byte _unit;
	protected String _memo;
	protected boolean _chequed;
	protected boolean _paid;
	protected boolean _taxable;

	public Item() {
		_uid = UIDGenerator.getUID();
		_oldAmount = 0;
		_amount = 0;
	}

	public int getUID() {
		return _uid;
	}

	public void setUID(int uid) {
		_uid = uid;
	}

	public String getDescription() {
		return _description;
	}

	public void setDescription(String description) {
		this._description = description;
	}

	public int getCategory() {
		return _category;
	}

	public void setCategory(int category) {
		this._category = category;
	}

	public long getAmount() {
		return _amount;
	}

	public void setAmount(long amount) {
		this._oldAmount = this._amount;
		this._amount = amount;
	}

	public long getOldAmount() {
		return _oldAmount;
	}

	public boolean isHiPriority() {
		return _hiPriority;
	}

	public void setHiPriority(boolean priority) {
		this._hiPriority = priority;
	}

	public int getQuantity() {
		return _quantity;
	}

	public void setQuantity(int quantity) {
		this._quantity = quantity;
	}

	public byte getUnit() {
		return _unit;
	}

	public void setUnit(byte unit) {
		this._unit = unit;
	}

	public String getMemo() {
		return _memo;
	}

	public void setMemo(String memo) {
		this._memo = memo;
	}

	public boolean isChequed() {
		return _chequed;
	}

	public void setChequed(boolean chequed) {
		this._chequed = chequed;
	}

	public boolean isPaid() {
		return _paid;
	}

	public void setPaid(boolean paid) {
		this._paid = paid;
	}

	public boolean isTaxable() {
		return _taxable;
	}

	public void setTaxable(boolean taxable) {
		this._taxable = taxable;
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof Item) {
			Item item = (Item) obj;
			return this._uid == item._uid;
		}
		return false;
	}
}
