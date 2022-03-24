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

import com.beigebinder.data.Category;
import com.beigebinder.resource.MoneyResource;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.synchronization.UIDGenerator;
import net.rim.device.api.system.ObjectGroup;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.util.Arrays;

public class CategoryPersist {
	private static final long CATEGORIESPERSIST = 0x24b7cefef2f625f3L; // com.beigebinder.logic.CategoryPersist.CATEGORIESPERSIST
	private static final long CATEGORY = 0x976c427003565edeL; // com.beigebinder.logic.CategoryPersist.CATEGORY

	private static ResourceBundle _resources = ResourceBundle.getBundle(MoneyResource.BUNDLE_ID, MoneyResource.BUNDLE_NAME);

	private PersistentObject _categoriesStore;
	private Category[] _categories;

	private CategoryPersist() {
		_categoriesStore = PersistentStore.getPersistentObject(CATEGORIESPERSIST);
		_categories = (Category[]) _categoriesStore.getContents();
		if (_categories == null) {
			String[] expenses = _resources.getStringArray(MoneyResource.EXPENSES);
			String[] incomes = _resources.getStringArray(MoneyResource.INCOMES);
			int size = expenses.length + incomes.length;
			_categories = new Category[size];
			for (int index = 0; index < size; index++) {
				_categories[index] = new Category();
				if (index < expenses.length) {
					_categories[index].setId(UIDGenerator.getUID());
					_categories[index].setType((short) 0);
					_categories[index].setDescription(expenses[index]);
				} else {
					_categories[index].setId(UIDGenerator.getUID());
					_categories[index].setType((short) 1);
					_categories[index].setDescription(incomes[index - expenses.length]);
				}
			}
			_categoriesStore.setContents(_categories);
		}

	}

	public static CategoryPersist getInstance() {
		RuntimeStore runtimeStore = RuntimeStore.getRuntimeStore();
		CategoryPersist categoryPersist = (CategoryPersist) runtimeStore.get(CATEGORY);
		if (categoryPersist == null) {
			categoryPersist = new CategoryPersist();
			runtimeStore.put(CATEGORY, categoryPersist);
		}
		return categoryPersist;
	}

	public void add(Category category) {
		this.addSingle(category);
		this.commit();
	}

	public void update(Category oldCategory, Category newCategory) {
		this.updateSingle(oldCategory, newCategory);
		this.commit();
	}

	public void remove(Category category) {
		this.removeSingle(category);
		this.commit();
	}

	public Category get(int id, int type) {
		Category category = new Category(id);
		int index = Arrays.getIndex(_categories, category);
		if (index == -1) {
			category.setType((short) type);
			category.setDescription(_resources.getString(MoneyResource.CATEGORYUNDEFINED));
			index = Arrays.getIndex(_categories, category);
			if (index == -1) {
				category.setId(UIDGenerator.getUID());
				this.add(category);
				MiscellaneousPersist.getInstance().update();
				index = Arrays.getIndex(_categories, category);
			}
		}
		return _categories[index];
	}

	public Category[] get() {
		return _categories;
	}

	public boolean exist(Category category) {
		int index = Arrays.getIndex(_categories, category);
		if (index != -1) {
			if (_categories[index].getUID() == category.getUID())
				return false;
			else
				return true;
		} else
			return false;
	}

	public Category getTaxCategory() {
		Category category = new Category();
		category.setType((short) 1);
		category.setDescription(_resources.getString(MoneyResource.TAXCATEGORY));
		int index = Arrays.getIndex(_categories, category);
		if (index == -1) {
			category.setId(UIDGenerator.getUID());
			this.add(category);
			MiscellaneousPersist.getInstance().update();
			index = Arrays.getIndex(_categories, category);
		}
		return _categories[index];
	}

	/***************************************************************************************/

	public void addSingle(Category category) {
		ObjectGroup.createGroup(category);
		Arrays.add(_categories, category);
	}

	public void removeSingle(Category category) {
		if (category == null) {
			throw new IllegalArgumentException();
		}
		Arrays.remove(_categories, category);
	}

	public void updateSingle(Category oldCategory, Category newCategory) {
		if (oldCategory == null || newCategory == null) {
			throw new IllegalArgumentException();
		}
		int index = Arrays.getIndex(_categories, oldCategory);
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		Category ungroupedCategory = (Category) ObjectGroup.expandGroup(_categories[index]);

		ungroupedCategory.setDescription(newCategory.getDescription());
		ungroupedCategory.setType(newCategory.getType());
		ObjectGroup.createGroup(ungroupedCategory);
		_categories[index] = ungroupedCategory;
	}

	public void removeAll() {
		_categories = new Category[0];
		_categoriesStore.setContents(_categories);
	}

	public void setDirty(Category category, boolean dirty) {
		if (category == null) {
			throw new IllegalArgumentException();
		}
		int index = Arrays.getIndex(_categories, category);
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		Category ungroupedCategory = (Category) ObjectGroup.expandGroup(_categories[index]);
		ungroupedCategory.setDirty(dirty);
		ObjectGroup.createGroup(ungroupedCategory);
		_categories[index] = ungroupedCategory;
	}

	public void commit() {
		Arrays.sort(_categories, new Category());
		_categoriesStore.commit();
	}

	/***************************************************************************************/

}
