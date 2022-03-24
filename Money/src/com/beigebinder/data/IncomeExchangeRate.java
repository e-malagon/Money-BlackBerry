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

public class IncomeExchangeRate extends Income implements Persistable, SyncObject {
	protected long _destAmount;
	protected int _currencyID;

	public long getDestAmount() {
		return _destAmount;
	}

	public void setDestAmount(long destAmount) {
		this._destAmount = destAmount;
	}

	public int getCurrencyID() {
		return _currencyID;
	}

	public void setCurrencyID(int currencyID) {
		this._currencyID = currencyID;
	}
}
