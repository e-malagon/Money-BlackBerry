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
package com.beigebinder.money;

import com.beigebinder.misc.SyncCollectionImpl;
import com.beigebinder.misc.Util;
import net.rim.device.api.synchronization.SyncManager;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.util.DateTimeUtilities;

public class MoneyStartup extends Application {
	public MoneyStartup() {
		invokeLater(new Runnable() {
			public void run() {
				ApplicationManager myApp = ApplicationManager.getApplicationManager();
				while (myApp.inStartup()) {
					try {
						Thread.sleep(1000);
					} catch (Exception ex) {
					}
				}

				long time = System.currentTimeMillis() + (DateTimeUtilities.ONEMINUTE * 5);

				String[] params = { "2" };
				ApplicationDescriptor apDes = ApplicationDescriptor.currentApplicationDescriptor();
				ApplicationDescriptor newAppDes = new ApplicationDescriptor(apDes, params);
				ApplicationManager.getApplicationManager().scheduleApplication(newAppDes, time, true);
				Util.setIcons(true);

				SyncManager.getInstance().enableSynchronization(new SyncCollectionImpl());

				System.exit(0);
			}
		});
	}
}
