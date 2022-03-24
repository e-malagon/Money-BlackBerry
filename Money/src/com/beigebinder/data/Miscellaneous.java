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

import net.rim.device.api.util.Persistable;

public class Miscellaneous implements Persistable {
	private String _key;
	private long _orKey;

	public Miscellaneous() {
		_orKey = System.currentTimeMillis();
	}

	public String getKey() {
		return _key;
	}

	public void setKey(String key) {
		this._key = key;
	}

	public void setOKey(long orKey) {
		_orKey = orKey;
	}

	public long getOKey() {
		return _orKey;
	}
}
