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

public final class Split {
	protected int _uid;
	protected String _description;
	protected int _category;
	protected long _amount;
	protected short _type;

	public Split() {
		_uid = UIDGenerator.getUID();
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
		this._amount = amount;
	}

	public void setType(short type) {
		this._type = type;
	}

	public short getType() {
		return _type;
	}

	public boolean equals(Object obj) {
		if (obj instanceof Split) {
			Split split = (Split) obj;
			return this._uid == split._uid;
		} else
			return false;
	}
}
