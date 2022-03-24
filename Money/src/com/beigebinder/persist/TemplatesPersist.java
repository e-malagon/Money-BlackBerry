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

import com.beigebinder.data.SavedTransaction;
import net.rim.device.api.system.ObjectGroup;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.util.Arrays;

public class TemplatesPersist {
	private static final long TEMPLATESPERSIST = 0xca0317e3b2a1ed41L; // com.beigebinder.logic.TemplatesPersist.TEMPLATESPERSIST
	private static final long TEMPLATES = 0x1b661c24aac84bcaL; // com.beigebinder.logic.TemplatesPersist.TEMPLATES

	private PersistentObject _templatesStore;
	private SavedTransaction[] _templates;

	private TemplatesPersist() {
		_templatesStore = PersistentStore.getPersistentObject(TEMPLATESPERSIST);
		_templates = (SavedTransaction[]) _templatesStore.getContents();
		if (_templates == null) {
			_templates = new SavedTransaction[0];
			_templatesStore.setContents(_templates);
		}
	}

	public static TemplatesPersist getInstance() {
		RuntimeStore runtimeStore = RuntimeStore.getRuntimeStore();
		TemplatesPersist templatesLogic = (TemplatesPersist) runtimeStore.get(TEMPLATES);
		if (templatesLogic == null) {
			templatesLogic = new TemplatesPersist();
			runtimeStore.put(TEMPLATES, templatesLogic);
		}
		return templatesLogic;
	}

	public void add(SavedTransaction template) {
		template.incUsed();
		this.addSingle(template);
		this.commit();
	}

	public void remove(SavedTransaction template) {
		this.removeSingle(template);
		this.commit();
	}

	public void update(SavedTransaction oldTemplate, String description, int AccountId) {
		if (oldTemplate == null) {
			throw new IllegalArgumentException();
		}
		int index = Arrays.getIndex(_templates, oldTemplate);
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		SavedTransaction ungroupedTemplate = (SavedTransaction) ObjectGroup.expandGroup(_templates[index]);
		ungroupedTemplate.setDescription(description);
		ungroupedTemplate.setAccount(AccountId);
		ObjectGroup.createGroup(ungroupedTemplate);
		_templates[index] = ungroupedTemplate;
		_templatesStore.commit();
	}

	public void updateUsed(SavedTransaction template) {
		if (template == null) {
			throw new IllegalArgumentException();
		}
		int index = Arrays.getIndex(_templates, template);
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		SavedTransaction ungroupedTemplate = (SavedTransaction) ObjectGroup.expandGroup(_templates[index]);
		ungroupedTemplate.incUsed();
		ObjectGroup.createGroup(ungroupedTemplate);
		_templates[index] = ungroupedTemplate;
		Arrays.sort(_templates, template);
		_templatesStore.commit();
	}

	public SavedTransaction get(int id) {
		SavedTransaction savedTransaction = new SavedTransaction(id);
		int index = Arrays.getIndex(_templates, savedTransaction);
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		return _templates[index];
	}

	public SavedTransaction[] get() {
		return _templates;
	}

	/***************************************************************************************/
	public void addSingle(SavedTransaction template) {
		template.incUsed();
		ObjectGroup.createGroup(template);
		Arrays.add(_templates, template);
	}

	public void removeSingle(SavedTransaction template) {
		if (template == null) {
			throw new IllegalArgumentException();
		}
		Arrays.remove(_templates, template);
	}

	public void updateSingle(SavedTransaction oldTemplate, SavedTransaction newTemplate) {
		if (oldTemplate == null || newTemplate == null) {
			throw new IllegalArgumentException();
		}
		int index = Arrays.getIndex(_templates, oldTemplate);
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		SavedTransaction ungroupedTemplate = (SavedTransaction) ObjectGroup.expandGroup(_templates[index]);
		ungroupedTemplate.setDescription(newTemplate.getDescription());
		ungroupedTemplate.setAccount(newTemplate.getAccount());
		ObjectGroup.createGroup(ungroupedTemplate);
		_templates[index] = ungroupedTemplate;
	}

	public void removeAll() {
		_templates = new SavedTransaction[0];
		_templatesStore.setContents(_templates);
	}

	public void setDirty(SavedTransaction template, boolean dirty) {
		if (template == null) {
			throw new IllegalArgumentException();
		}
		int index = Arrays.getIndex(_templates, template);
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		SavedTransaction ungroupedTemplate = (SavedTransaction) ObjectGroup.expandGroup(_templates[index]);
		ungroupedTemplate.setDirty(dirty);
		ObjectGroup.createGroup(ungroupedTemplate);
		_templates[index] = ungroupedTemplate;
	}

	public void commit() {
		_templatesStore.commit();
	}

}
