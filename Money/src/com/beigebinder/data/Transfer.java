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

public class Transfer extends Transaction implements Persistable, SyncObject {
	private int _mirrorAccount;

	public int getMirrorAccount() {
		return _mirrorAccount;
	}

	public void setMirrorAccount(int mirrorAccount) {
		this._mirrorAccount = mirrorAccount;
	}

	public Object clone() {
		Transfer transfer = new Transfer();
		transfer._id = _id;
		transfer._parentAccount = _parentAccount;
		transfer._status = _status;
		transfer._number = _number;
		transfer._description = new String(_description);
		transfer._date = _date;
		transfer._amount = _amount;
		transfer._mirrorAccount = _mirrorAccount;
		transfer._dirty = _dirty;
		return transfer;
	}
}
