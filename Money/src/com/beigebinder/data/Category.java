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

public final class Category implements Persistable, Comparator, SyncObject {
	private int _id;
	private short _type;
	private String _description;
	private boolean _dirty;

	public boolean isDirty() {
		return _dirty;
	}

	public void setDirty(boolean dirty) {
		this._dirty = dirty;
	}

	public Category() {
		_dirty = true;
	}

	public Category(int id) {
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

	public String toString() {
		if (_type == 0)
			return _description + " \u00bb";
		else
			return _description + " \u00ab";
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof Category) {
			Category other = (Category) obj;
			if (other._id == this._id)
				return true;
			else
				return (other._type == this._type && StringUtilities.strEqualIgnoreCase(other._description, this._description));
		}
		return false;
	}

	public int compare(Object o1, Object o2) {
		int comparacion;
		if (!(o1 instanceof Category && o2 instanceof Category)) {
			throw new IllegalArgumentException();
		}
		Category category1 = (Category) o1;
		Category category2 = (Category) o2;
		if (category1.getType() != category2.getType())
			comparacion = category1.getType() < category2.getType() ? -1 : 1;
		else
			comparacion = StringUtilities.compareToIgnoreCase(category1._description, category2._description);
		return comparacion;
	}
}
