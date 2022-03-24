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
package com.beigebinder.persist;

import com.beigebinder.data.ShopingList;
import com.beigebinder.data.Transaction;
import net.rim.device.api.system.ObjectGroup;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.util.Arrays;

public class ShopingListPersist {
	private static final long SHOPINGLISTPERSIST = 0xb8e2bb9c3c97b149L; // com.beigebinder.logic.ShopingListPersist.SHOPINGLISTPERSIST
	private static final long SHOPINGLIST = 0x771b8c549df0924aL; // com.beigebinder.logic.ShopingListPersist.SHOPINGLIST

	private PersistentObject _shopinglistStore;
	private ShopingList[] _shopinglists;

	private ShopingListPersist() {
		_shopinglistStore = PersistentStore.getPersistentObject(SHOPINGLISTPERSIST);
		_shopinglists = (ShopingList[]) _shopinglistStore.getContents();
		if (_shopinglists == null) {
			_shopinglists = new ShopingList[0];
			_shopinglistStore.setContents(_shopinglists);
		}
	}

	public static ShopingListPersist getInstance() {
		RuntimeStore runtimeStore = RuntimeStore.getRuntimeStore();
		ShopingListPersist savedTransactionsPersist = (ShopingListPersist) runtimeStore.get(SHOPINGLIST);
		if (savedTransactionsPersist == null) {
			savedTransactionsPersist = new ShopingListPersist();
			runtimeStore.put(SHOPINGLIST, savedTransactionsPersist);
		}
		return savedTransactionsPersist;
	}

	public void add(ShopingList shopingList) {
		this.addSingle(shopingList);
		this.commit();
	}

	public void remove(ShopingList shopingList) {
		this.removeSingle(shopingList);
		this.commit();
	}

	public void update(ShopingList oldShopingList, ShopingList newShopingList) {
		this.removeSingle(oldShopingList);
		this.addSingle(newShopingList);
		this.commit();
	}

	public ShopingList get(int id) {
		Transaction shopingList = new Transaction(id);
		int index = Arrays.getIndex(_shopinglists, shopingList);
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		return _shopinglists[index];
	}

	public ShopingList[] get() {
		return _shopinglists;
	}

	/***************************************************************************************/

	public void addSingle(ShopingList shopingList) {
		ObjectGroup.createGroup(shopingList);
		Arrays.add(_shopinglists, shopingList);
	}

	public void removeSingle(ShopingList shopingList) {
		Arrays.remove(_shopinglists, shopingList);
	}

	public void removeAll() {
		_shopinglists = new ShopingList[0];
		_shopinglistStore.setContents(_shopinglists);
	}

	public void setDirty(ShopingList shopingList, boolean dirty) {
		if (shopingList == null) {
			throw new IllegalArgumentException();
		}
		int index = Arrays.getIndex(_shopinglists, shopingList);
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		ShopingList ungroupedShopingList = (ShopingList) ObjectGroup.expandGroup(_shopinglists[index]);

		ungroupedShopingList.setDirty(dirty);
		ObjectGroup.createGroup(ungroupedShopingList);
		_shopinglists[index] = ungroupedShopingList;
	}

	public void commit() {
		_shopinglistStore.commit();
	}
}
