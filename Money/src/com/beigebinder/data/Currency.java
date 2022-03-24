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

public final class Currency implements Persistable, Comparator, SyncObject {
	private int _id;
	private String _description;
	private String _ISOCode;
	private char[] _mask;
	private String _sign;
	private char[] _negativeSymbol;
	private String _decimalSymbol;
	private byte _decimals;
	private String _groupSymbol;
	private int _grouping;

	public Currency(int id, String description, String ISOCode, String mask, String symbol, String negativeSymbol, String decimalSymbol, int decimals, String groupSymbol, int grouping) {
		this._id = id;
		this._description = description;
		this._ISOCode = ISOCode;
		this._mask = mask.toCharArray();
		this._sign = symbol;
		this._negativeSymbol = negativeSymbol.toCharArray();
		this._decimalSymbol = decimalSymbol;
		this._decimals = (byte) decimals;
		this._groupSymbol = groupSymbol;
		this._grouping = grouping;
	}

	public Currency(int id) {
		_id = id;
	}

	public int getUID() {
		return _id;
	}

	public String getDescription() {
		return _description;
	}

	public String getISOCode() {
		return _ISOCode;
	}

	public char[] getMask() {
		return _mask;
	}

	public String getSign() {
		if (_sign.length() == 0)
			return _ISOCode;
		else
			return _sign;
	}

	public char[] getNegativeSymbol() {
		return _negativeSymbol;
	}

	public String getDecimalSymbol() {
		return _decimalSymbol;
	}

	public byte getDecimals() {
		return _decimals;
	}

	public String getGroupSymbol() {
		return _groupSymbol;
	}

	public int getGrouping() {
		return _grouping;
	}

	public String toString() {
		return _description;
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof Currency) {
			Currency other = (Currency) obj;
			if (other._id == this._id)
				return true;
			else
				return StringUtilities.strEqualIgnoreCase(other._description, this._description);
		}
		return false;
	}

	public int compare(Object o1, Object o2) {
		if (!(o1 instanceof Currency && o2 instanceof Currency)) {
			throw new IllegalArgumentException();
		}
		Currency currency1 = (Currency) o1;
		Currency currency2 = (Currency) o2;

		return StringUtilities.compareToIgnoreCase(currency1._description, currency2._description);
	}
}
