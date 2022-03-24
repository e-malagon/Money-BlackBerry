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

public class TaxedShopingList extends ShopingList implements Persistable, SyncObject {
	protected int _taxRate;
	protected boolean[] _taxables;

	public int getTaxRate() {
		return _taxRate;
	}

	public void setTaxRate(int taxRate) {
		this._taxRate = taxRate;
	}

	public boolean[] getTaxable() {
		return _taxables;
	}

	public void setTaxable(boolean[] taxable) {
		this._taxables = taxable;
	}
}
