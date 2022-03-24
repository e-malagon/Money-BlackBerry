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
//#preprocess 
package com.beigebinder.misc;

import java.util.Calendar;
import java.util.Date;

import com.beigebinder.data.Currency;
import com.beigebinder.data.Notification;
import com.beigebinder.data.SavedTransaction;
import com.beigebinder.data.Transaction;
import com.beigebinder.persist.NotificationPersist;
import com.beigebinder.persist.PendingsPersist;
import com.beigebinder.persist.SavedTransactionsPersist;
import net.rim.blackberry.api.homescreen.HomeScreen;
//#ifdef Indicators
import net.rim.blackberry.api.messagelist.ApplicationIcon;
import net.rim.blackberry.api.messagelist.ApplicationIndicator;
import net.rim.blackberry.api.messagelist.ApplicationIndicatorRegistry;
import net.rim.device.api.system.EncodedImage;
//#endif
import net.rim.device.api.synchronization.UIDGenerator;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.DateTimeUtilities;

public class Util {

	public static String toString(long value, Currency currency, boolean decimal) {
		char[] result = new char[30];
		int indexResult = result.length - 1;
		char[] amount = String.valueOf(Math.abs(value)).toCharArray();
		int indexAmount = amount.length - 1;
		Arrays.fill(result, ' ');
		char[] mask = currency.getMask();
		int indexMask = mask.length - 1;
		int iaux;
		int yaux;
		int kaux;
		char[] caux;
		if (decimal) {
			iaux = currency.getDecimals() - 1;
			while (0 <= iaux) {
				if (0 <= indexAmount) {
					result[indexResult] = amount[indexAmount];
				} else {
					result[indexResult] = '0';
				}
				indexResult--;
				indexAmount--;
				iaux--;
			}

			result[indexResult] = '.';
			indexResult--;

			if (0 <= indexAmount) {
				while (0 <= indexAmount) {
					result[indexResult] = amount[indexAmount];
					indexResult--;
					indexAmount--;
				}
			} else {
				result[indexResult] = '0';
				indexResult--;
			}
			if (value < 0) {
				result[indexResult] = '-';
				indexResult--;
			}
		} else {
			while (0 <= indexMask) {
				switch (mask[indexMask]) {
				case '+':
					if (value < 0) {
						result[indexResult] = currency.getNegativeSymbol()[1];
						indexResult--;
					}
					break;
				case ' ':
					result[indexResult] = ' ';
					indexResult--;
					break;
				case '-':
					if (value < 0) {
						result[indexResult] = currency.getNegativeSymbol()[0];
						indexResult--;
					}
					break;
				case '$':
					caux = currency.getSign().toCharArray();
					iaux = caux.length - 1;
					while (0 <= iaux) {
						result[indexResult] = caux[iaux];
						indexResult--;
						iaux--;
					}
					break;
				case '#':
					iaux = currency.getDecimals() - 1;
					while (0 <= iaux) {
						if (0 <= indexAmount) {
							result[indexResult] = amount[indexAmount];
						} else {
							result[indexResult] = '0';
						}
						indexResult--;
						indexAmount--;
						iaux--;
					}
					caux = currency.getDecimalSymbol().toCharArray();
					iaux = caux.length - 1;
					while (0 <= iaux) {
						result[indexResult] = caux[iaux];
						indexResult--;
						iaux--;
					}
					if (0 <= indexAmount) {
						yaux = currency.getGrouping();
						while (0 <= indexAmount) {
							kaux = yaux % 10;
							yaux /= 10;
							while (0 < kaux) {
								if (indexAmount < 0)
									break;
								result[indexResult] = amount[indexAmount];
								indexResult--;
								indexAmount--;
								kaux--;
							}
							if (0 <= indexAmount) {
								caux = currency.getGroupSymbol().toCharArray();
								iaux = caux.length - 1;
								while (0 <= iaux) {
									result[indexResult] = caux[iaux];
									indexResult--;
									iaux--;
								}
							}
						}
					} else {
						result[indexResult] = '0';
						indexResult--;
					}
					break;
				}
				indexMask--;
			}
		}

		return new String(result).trim();
	}

	public static long toLong(String value, Currency currency) {
		char[] valueChar = value.toCharArray();
		int len = valueChar.length;
		int ptr = value.indexOf('.');
		ptr = ptr != -1 ? ptr + 1 : len;
		int ptr2 = ptr - 1;
		int len2 = ptr + currency.getDecimals();
		char[] result = new char[35];
		Arrays.fill(result, ' ');
		int index = 25;

		while (ptr < len2) {
			if (ptr < len) {
				if (Character.isDigit(valueChar[ptr])) {
					result[index] = valueChar[ptr];
					index++;
				}
			} else {
				result[index] = '0';
				index++;
			}
			ptr++;
		}
		index = 24;

		while (0 <= ptr2) {
			if (Character.isDigit(valueChar[ptr2])) {
				result[index] = valueChar[ptr2];
				index--;
			}
			ptr2--;
		}
		if (value.indexOf('-') != -1)
			result[index] = '-';

		return Long.parseLong(new String(result).trim());
	}

	public static long exchangeToAmount(long value, long exrt, Currency valueCurrency, Currency exchangeCurrency, Currency returnCurrency) {
		byte[] valueArray = new byte[25];
		byte[] exrtArray = new byte[25];
		byte[] returnArray = new byte[50];
		int lvalue = 0;
		int lextr = 0;
		int j;
		int hv;
		long sign = value / Math.abs(value);
		value = Math.abs(value);
		exrt = Math.abs(exrt);
		Arrays.fill(valueArray, (byte) 0);
		Arrays.fill(exrtArray, (byte) 0);
		Arrays.fill(returnArray, (byte) 0);

		while (value > 0) {
			valueArray[lvalue++] = (byte) (value % 10);
			value /= 10;
		}

		while (exrt > 0) {
			exrtArray[lextr++] = (byte) (exrt % 10);
			exrt /= 10;
		}

		for (byte i = 0; i < lextr; i++) {
			for (j = 0; j < lvalue; j++) {
				hv = exrtArray[i] * valueArray[j];
				returnArray[i + j] += (hv % 10);
				if (hv > 9)
					returnArray[i + j + 1] += (hv / 10);
				if (returnArray[i + j] > 9) {
					returnArray[i + j + 1] += (returnArray[i + j] / 10);
					returnArray[i + j] %= 10;
				}
			}
		}

		int pvalue = valueCurrency.getDecimals();
		int pextr = exchangeCurrency.getDecimals();
		int preturn = returnCurrency.getDecimals();

		int ik = (pvalue + pextr) - preturn;
		long lreturn = 0;
		lvalue += (lextr + 3);
		while (ik <= lvalue) {
			lreturn *= 10;
			if (0 <= lvalue) {
				lreturn += returnArray[lvalue];
			}
			lvalue--;
		}
		return lreturn * sign;
	}

	public static String amountToExchange(long value1, long value2) {
		double valor = Math.abs((double) value1 / (double) value2);
		if (valor < 0.0000001 || 1000000 < valor)
			valor = 1;
		return String.valueOf(valor);
	}

	public static String[] copy(String[] array) {
		int size = array.length;
		String[] newArray = new String[size];
		for (int index = 0; index < size; index++) {
			newArray[index] = new String(array[index]);
		}
		return newArray;
	}

	public static boolean contains(int[] array, int value) {
		int len = array.length;
		for (int index = 0; index < len; index++) {
			if (array[index] == value)
				return true;
		}
		return false;
	}

	public static void RecurringCheck() {
		NotificationPersist scheduleLogic = NotificationPersist.getInstance();
		Notification[] schedules = scheduleLogic.get();
		SavedTransaction executedTransaction;
		Transaction transaction;
		int size = schedules.length;
		long currentDate = DateTimeUtilities.getNextDate(0).getTime().getTime();
		long date;
		long nextDate = 0;
		for (int index = 0; index < size; index++) {
			if (schedules[index].getNotificationsLeft() == 0)
				continue;

			nextDate = ((long) schedules[index].getDaysForAlert()) * DateTimeUtilities.ONEDAY;

			date = schedules[index].getNextExecutionDate() - nextDate;
			if (date < currentDate) {
				executedTransaction = new SavedTransaction(schedules[index]);
				transaction = (Transaction) SavedTransactionsPersist.getInstance().get(executedTransaction.getTransactionId()).clone();
				transaction.setDate(schedules[index].getNextExecutionDate());
				transaction.setId(UIDGenerator.getUID());
				executedTransaction.setTransactionId(transaction.getUID());
				PendingsPersist.getInstance().add(executedTransaction);
				SavedTransactionsPersist.getInstance().add(transaction);
				nextDate = getNextExecutionDate(schedules[index]);
				scheduleLogic.updateExecutionDate(schedules[index], nextDate);
				if (nextDate < currentDate)
					index--;
			}

		}
		long time = DateTimeUtilities.getNextDate(0).getTime().getTime();

		String[] params = { "2" };
		ApplicationDescriptor apDes = ApplicationDescriptor.currentApplicationDescriptor();
		ApplicationDescriptor newAppDes = new ApplicationDescriptor(apDes, params);
		ApplicationManager.getApplicationManager().scheduleApplication(newAppDes, time, true);

	}

	private static long getNextExecutionDate(Notification notification) {
		long date = notification.getNextExecutionDate();
		long add = 0;
		switch (notification.getType()) {
		case 1:
			add = DateTimeUtilities.ONEDAY * 7;
			break;
		case 2:
			add = DateTimeUtilities.ONEDAY * 14;
			break;
		case 3:
			add = DateTimeUtilities.ONEDAY * 21;
			break;
		case 4:
			add = DateTimeUtilities.ONEDAY * getMonthsDays(date, 1);
			break;
		case 5:
			add = DateTimeUtilities.ONEDAY * getMonthsDays(date, 2);
			break;
		case 6:
			add = DateTimeUtilities.ONEDAY * getMonthsDays(date, 3);
			break;
		case 7:
			add = DateTimeUtilities.ONEDAY * getMonthsDays(date, 6);
			break;
		}
		date += add;
		return date;
	}

	private static long getMonthsDays(long date, int months) {
		long days = 0;
		int month;
		int year;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(date));
		month = calendar.get(Calendar.MONTH);
		year = calendar.get(Calendar.YEAR);
		while (months != 0) {
			days += DateTimeUtilities.getNumberOfDaysInMonth(month, year);
			if (month < 11) {
				month++;
			} else {
				month = 0;
				year++;
			}
			months--;
		}
		return days;
	}

	public static void setIcons(boolean isStartup) {
		final Bitmap icon = Bitmap.getBitmapResource("Money.png");
		final Bitmap icon2 = Bitmap.getBitmapResource("MoneyRollover.png");
		final Bitmap iconT = Bitmap.getBitmapResource("MoneyT.png");
		final Bitmap icon2T = Bitmap.getBitmapResource("MoneyRolloverT.png");
		final Bitmap iconA = Bitmap.getBitmapResource("MoneyPay.png");
		final Bitmap icon2A = Bitmap.getBitmapResource("MoneyRolloverPay.png");

		int numberofPendigs = PendingsPersist.getInstance().get().length;
		int cont = 0;
		boolean salir = false;
		while (!salir && cont < 60) {
			salir = true;
			try {
				if (numberofPendigs != 0) {
					HomeScreen.updateIcon(iconA, 2);
					HomeScreen.setRolloverIcon(icon2A, 2);
				} else {
					HomeScreen.updateIcon(iconT, 2);
					HomeScreen.setRolloverIcon(icon2T, 2);
				}
				HomeScreen.updateIcon(icon, 0);
				HomeScreen.setRolloverIcon(icon2, 0);
			} catch (Exception ex) {
				salir = false;
				try {
					Thread.sleep(1000);
				} catch (Exception ex1) {
				}
			}
			cont++;
		}

		//#ifdef Indicators
		ApplicationIndicator indicator;
		if (isStartup) {
			EncodedImage image = EncodedImage.getEncodedImageResource("MoneyIndicator.png");
			ApplicationIcon iconI = new ApplicationIcon(image);
			indicator = ApplicationIndicatorRegistry.getInstance().register(iconI, false, false);
		} else {
			indicator = ApplicationIndicatorRegistry.getInstance().getApplicationIndicator();
		}

		if (numberofPendigs != 0) {
			indicator.setValue(numberofPendigs);
			indicator.setVisible(true);
		} else {
			indicator.setVisible(false);
		}
		//#endif	
	}

	static public String complete(String str, int len, int alin) {
		char[] array = new char[len];
		Arrays.fill(array, ' ');
		if (str != null) {
			char[] array2 = str.toCharArray();
			int start = 0;
			int sterLen = str.length();
			switch (alin) {
			case 1:
				start = len - sterLen - 1;
				break;
			}
			for (int index = 0; index < sterLen && index < len; index++, start++) {
				array[start] = array2[index];
			}
		}
		return new String(array);
	}
}
