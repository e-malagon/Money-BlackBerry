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

import com.beigebinder.data.Notification;
import net.rim.device.api.system.ObjectGroup;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.util.Arrays;

public class NotificationPersist {
	private static final long NOTIFICATIONSPERSIST = 0xcb22d5a40da35480L; // com.beigebinder.logic.NotificationPersist.NOTIFICATIONSPERSIST
	private static final long NOTIFICATIONS = 0x1c297155f38314ebL; // com.beigebinder.logic.NotificationPersist.NOTIFICATIONS

	private PersistentObject _notificationsStore;
	private Notification[] _notifications;

	private NotificationPersist() {
		_notificationsStore = PersistentStore.getPersistentObject(NOTIFICATIONSPERSIST);
		_notifications = (Notification[]) _notificationsStore.getContents();
		if (_notifications == null) {
			_notifications = new Notification[0];
			_notificationsStore.setContents(_notifications);
		}
	}

	public static NotificationPersist getInstance() {
		RuntimeStore runtimeStore = RuntimeStore.getRuntimeStore();
		NotificationPersist notificationLogic = (NotificationPersist) runtimeStore.get(NOTIFICATIONS);
		if (notificationLogic == null) {
			notificationLogic = new NotificationPersist();
			runtimeStore.put(NOTIFICATIONS, notificationLogic);
		}
		return notificationLogic;
	}

	public void add(Notification notification) {
		this.addSingle(notification);
		this.commit();
	}

	public void remove(Notification notification) {
		this.removeSingle(notification);
		this.commit();
	}

	public void update(Notification oldNotification, Notification newNotification) {
		this.updateSingle(oldNotification, newNotification);
		this.commit();
	}

	public void updateExecutionDate(Notification notification, long date) {
		if (notification == null) {
			throw new IllegalArgumentException();
		}
		int index = Arrays.getIndex(_notifications, notification);
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		Notification ungroupednotification = (Notification) ObjectGroup.expandGroup(_notifications[index]);
		ungroupednotification.setNextExecutionDate(date);
		ungroupednotification.setNotificationsLeft(notification.getNotificationsLeft() - 1);
		ObjectGroup.createGroup(ungroupednotification);
		_notifications[index] = ungroupednotification;
		_notificationsStore.commit();
	}

	public Notification[] get() {
		return _notifications;
	}

	/***************************************************************************************/
	public void addSingle(Notification notification) {
		ObjectGroup.createGroup(notification);
		Arrays.add(_notifications, notification);
	}

	public void removeSingle(Notification notification) {
		if (notification == null) {
			throw new IllegalArgumentException();
		}
		Arrays.remove(_notifications, notification);
	}

	public void updateSingle(Notification oldNotification, Notification newNotification) {
		if (oldNotification == null || newNotification == null) {
			throw new IllegalArgumentException();
		}
		int index = Arrays.getIndex(_notifications, oldNotification);
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		Notification ungroupednotification = (Notification) ObjectGroup.expandGroup(_notifications[index]);
		ungroupednotification.setDescription(newNotification.getDescription());
		ungroupednotification.setType(newNotification.getType());
		ungroupednotification.setNextExecutionDate(newNotification.getNextExecutionDate());
		ungroupednotification.setDaysForAlert(newNotification.getDaysForAlert());
		ungroupednotification.setNotificationsLeft(newNotification.getNotificationsLeft());
		ungroupednotification.setAccount(newNotification.getAccount());
		ObjectGroup.createGroup(ungroupednotification);
		_notifications[index] = ungroupednotification;
	}

	public void removeAll() {
		_notifications = new Notification[0];
		_notificationsStore.setContents(_notifications);
	}

	public void setDirty(Notification notification, boolean dirty) {
		if (notification == null) {
			throw new IllegalArgumentException();
		}
		int index = Arrays.getIndex(_notifications, notification);
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		Notification ungroupednotification = (Notification) ObjectGroup.expandGroup(_notifications[index]);
		ungroupednotification.setDirty(dirty);
		ObjectGroup.createGroup(ungroupednotification);
		_notifications[index] = ungroupednotification;
	}

	public void commit() {
		_notificationsStore.commit();
	}

	/***************************************************************************************/
}
