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

import com.beigebinder.data.Currency;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.util.Arrays;

public final class CurrencyPersist {
	private static final long CURRENCYPERSIST = 0x948f762d7b734a9dL; // com.beigebinder.logic.CurrencyPersist.CURRENCYPERSIST
	private static final long CURRENCIES = 0x502e332a855ee690L; // com.beigebinder.logic.CurrencyPersist.CURRENCIES
	private static final long DEFAULTVALUESPERSIST = 0x9e9d64a53e937529L; // com.beigebinder.logic.CurrencyPersist.DEFAULTVALUESPERSIST

	private PersistentObject _currenciesStore;
	private Currency[] _currencies;
	private PersistentObject _defaulValuesStore;
	private int[] _defaultValues;

	private CurrencyPersist() {
		_currenciesStore = PersistentStore.getPersistentObject(CURRENCYPERSIST);
		_currencies = (Currency[]) _currenciesStore.getContents();
		if (_currencies == null) {
			_currencies = getDefaultCurrencies();
			_currenciesStore.setContents(_currencies);
		}

		_defaulValuesStore = PersistentStore.getPersistentObject(DEFAULTVALUESPERSIST);
		_defaultValues = (int[]) _defaulValuesStore.getContents();
		if (_defaultValues == null) {
			_defaultValues = new int[1];
			_defaultValues[0] = 840;
			_defaulValuesStore.setContents(_defaultValues);
		}
	}

	public static CurrencyPersist getInstance() {
		RuntimeStore runtimeStore = RuntimeStore.getRuntimeStore();
		CurrencyPersist currencyLogic = (CurrencyPersist) runtimeStore.get(CURRENCIES);
		if (currencyLogic == null) {
			currencyLogic = new CurrencyPersist();
			runtimeStore.put(CURRENCIES, currencyLogic);
		}
		return currencyLogic;
	}

	public Currency get(int id) {
		Currency currency = new Currency(id);
		int index = Arrays.getIndex(_currencies, currency);
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		return _currencies[index];
	}

	public Currency[] get() {
		return _currencies;
	}

	public Currency getDefaulCurrency() {
		return this.get(_defaultValues[0]);
	}

	public void setDefaultCurrency(int currency) {
		_defaultValues[0] = currency;
		_defaulValuesStore.commit();
	}

	public void setDefaultCurrency(Currency currency) {
		_defaultValues[0] = currency.getUID();
		_defaulValuesStore.commit();
	}

	/***************************************************************************************/
	private Currency[] getDefaultCurrencies() {
		Currency[] currencies = new Currency[177];
		currencies[0] = new Currency(971, "Afghani", "AFN", "-$ #", "", "-", ".", 2, ",", 33333333);
		currencies[1] = new Currency(12, "Algerian Dinar", "DZD", "$ #-", "\u062f.\u062c.\u200f", "-", ".", 2, ",", 33333333);
		currencies[2] = new Currency(32, "Argentine Peso", "ARS", "$-#", "$", "-", ",", 2, ".", 33333333);
		currencies[3] = new Currency(51, "Armenian Dram", "AMD", "-$ #", "\u0564\u0580.", "-", ".", 2, ",", 33333333);
		currencies[4] = new Currency(533, "Aruban Guilder", "AWG", "-$ #", "\u0192", "-", ".", 2, ",", 33333333);
		currencies[5] = new Currency(36, "Australian Dollar", "AUD", "-$#", "$", "-", ".", 2, ",", 33333333);
		currencies[6] = new Currency(944, "Azerbaijanian Manat", "AZN", "-$ #", "", "-", ".", 2, ",", 33333333);
		currencies[7] = new Currency(44, "Bahamian Dollar", "BSD", "-$ #", "$", "-", ".", 2, ",", 33333333);
		currencies[8] = new Currency(48, "Bahraini Dinar", "BHD", "$ #-", "\u062f.\u0628.\u200f", "-", ".", 3, ",", 33333333);
		currencies[9] = new Currency(764, "Baht", "THB", "$-#", "\u0e3f", "-", ".", 2, ",", 33333333);
		currencies[10] = new Currency(590, "Balboa", "PAB", "-$#+", "B", "()", ".", 2, ",", 33333333);
		currencies[11] = new Currency(52, "Barbados Dollar", "BBD", "-$ #", "$", "-", ".", 2, ",", 33333333);
		currencies[12] = new Currency(974, "Belarussian Ruble", "BYR", "-$#", "\u0420\u0443\u0431", "-", ".", 0, " ", 33333333);
		currencies[13] = new Currency(84, "Belize Dollar", "BZD", "-$ #", "$", "-", ".", 2, ",", 33333333);
		currencies[14] = new Currency(60, "Bermudian Dollar", "BMD", "-$ #", "$", "-", ".", 2, ",", 33333333);
		currencies[15] = new Currency(937, "Bolivar Fuerte", "VEF", "$ -#", "BsF.", "-", ",", 2, ".", 33333333);
		currencies[16] = new Currency(68, "Boliviano", "BOB", "-$#+", "B$", "()", ".", 2, ",", 33333333);
		currencies[17] = new Currency(955, "Bond Markets Units European Composite Unit (EURCO)", "XBA", "-$ #", "", "-", ".", 0, ",", 33333333);
		currencies[18] = new Currency(986, "Brazilian Real", "BRL", "-$ #", "R$", "-", ",", 2, ".", 33333333);
		currencies[19] = new Currency(96, "Brunei Dollar", "BND", "-$ #", "$", "-", ".", 2, ",", 33333333);
		currencies[20] = new Currency(975, "Bulgarian Lev", "BGN", "-$#", "\u043B\u0432.", "-", ",", 2, " ", 33333333);
		currencies[21] = new Currency(108, "Burundi Franc", "BIF", "-$ #", "Fr", "-", ".", 0, ",", 33333333);
		currencies[22] = new Currency(124, "Canadian Dollar", "CAD", "-$#", "$", "-", ".", 2, ",", 33333333);
		currencies[23] = new Currency(132, "Cape Verde Escudo", "CVE", "-$ #", "$", "-", ".", 2, ",", 33333333);
		currencies[24] = new Currency(136, "Cayman Islands Dollar", "KYD", "-$ #", "$", "-", ".", 2, ",", 33333333);
		currencies[25] = new Currency(936, "Cedi", "GHS", "-$ #", "\u20b5", "-", ".", 2, ",", 33333333);
		currencies[26] = new Currency(952, "CFA Franc BCEAO", "XOF", "-$ #", "Fr", "-", ".", 0, ",", 33333333);
		currencies[27] = new Currency(950, "CFA Franc BEAC", "XAF", "-$ #", "Fr", "-", ".", 0, ",", 33333333);
		currencies[28] = new Currency(953, "CFP Franc", "XPF", "-$ #", "Fr", "-", ".", 0, ",", 33333333);
		currencies[29] = new Currency(152, "Chilean Peso", "CLP", "$-#", "Ch$", "-", ",", 0, ".", 33333333);
		currencies[30] = new Currency(170, "Colombian Peso", "COP", "-$#+", "$", "()", ".", 2, ",", 33333333);
		currencies[31] = new Currency(174, "Comoro Franc", "KMF", "-$ #", "Fr", "-", ".", 0, ",", 33333333);
		currencies[32] = new Currency(976, "Congolese Franc", "CDF", "-$ #", "Fr", "-", ".", 2, ",", 33333333);
		currencies[33] = new Currency(977, "Convertible Marks", "BAM", "-$ #", "\u041a\u041c.", "-", ",", 2, ".", 33333333);
		currencies[34] = new Currency(558, "Cordoba Oro", "NIO", "-$#+", "$C", "()", ".", 2, ",", 33333333);
		currencies[35] = new Currency(188, "Costa Rican Colon", "CRC", "-$#+", "C", "()", ".", 2, ",", 33333333);
		currencies[36] = new Currency(191, "Croatian Kuna", "HRK", "-$ #", "Kn", "-", ",", 2, ".", 33333333);
		currencies[37] = new Currency(192, "Cuban Peso", "CUP", "-$ #", "$", "-", ".", 2, ",", 33333333);
		currencies[38] = new Currency(203, "Czech Koruna", "CZK", "-# $", "K\u010d", "-", ",", 2, " ", 33333333);
		currencies[39] = new Currency(270, "Dalasi", "GMD", "-$ #", "D", "-", ".", 2, ",", 33333333);
		currencies[40] = new Currency(208, "Danish Krone", "DKK", "$ -#", "kr", "-", ",", 2, ".", 33333333);
		currencies[41] = new Currency(807, "Denar", "MKD", "-$ #", "Den", "-", ",", 2, ".", 33333333);
		currencies[42] = new Currency(262, "Djibouti Franc", "DJF", "-$ #", "Fr", "-", ".", 0, ",", 33333333);
		currencies[43] = new Currency(678, "Dobra", "STD", "-$ #", "Db", "-", ".", 2, ",", 33333333);
		currencies[44] = new Currency(214, "Dominican Peso", "DOP", "-$#+", "RD$", "()", ".", 2, ",", 33333333);
		currencies[45] = new Currency(704, "Dong", "VND", "-# $", "\u0111", "-", ",", 2, ".", 33333333);
		currencies[46] = new Currency(951, "East Caribbean Dollar", "XCD", "-$ #", "$", "-", ".", 2, ",", 33333333);
		currencies[47] = new Currency(818, "Egyptian Pound", "EGP", "$ #-", "\u062c.\u0645.\u200f", "-", ".", 2, ",", 33333333);
		currencies[48] = new Currency(222, "El Salvador Colon", "SVC", "-$#+", "C", "()", ".", 2, ",", 33333333);
		currencies[49] = new Currency(230, "Ethiopian Birr", "ETB", "-$ #", "", "-", ".", 2, ",", 33333333);
		currencies[50] = new Currency(978, "Euro", "EUR", "-# $", "\u20ac", "-", ",", 2, ".", 33333333);
		currencies[51] = new Currency(956, "European Monetary Unit (E.M.U.-6)", "XBB", "-$ #", "", "-", ".", 0, ",", 33333333);
		currencies[52] = new Currency(958, "European Unit of Account 17(E.U.A.-17)", "XBD", "-$ #", "", "-", ".", 0, ",", 33333333);
		currencies[53] = new Currency(957, "European Unit of Account 9(E.U.A.-9)", "XBC", "-$ #", "", "-", ".", 0, ",", 33333333);
		currencies[54] = new Currency(238, "Falkland Islands Pound", "FKP", "-$ #", "\u00a3", "-", ".", 2, ",", 33333333);
		currencies[55] = new Currency(242, "Fiji Dollar", "FJD", "-$ #", "$", "-", ".", 2, ",", 33333333);
		currencies[56] = new Currency(348, "Forint", "HUF", "-$ #", "Ft", "-", ",", 2, " ", 33333333);
		currencies[57] = new Currency(292, "Gibraltar Pound", "GIP", "-$ #", "\u00a3", "-", ".", 2, ",", 33333333);
		currencies[58] = new Currency(959, "Gold", "XAU", "-$ #", "", "-", ".", 0, ",", 33333333);
		currencies[59] = new Currency(332, "Gourde", "HTG", "-$ #", "G", "-", ".", 2, ",", 33333333);
		currencies[60] = new Currency(600, "Guarani", "PYG", "-$#+", "G", "()", ",", 0, ".", 33333333);
		currencies[61] = new Currency(324, "Guinea Franc", "GNF", "-$ #", "Fr", "-", ".", 0, ",", 33333333);
		currencies[62] = new Currency(624, "Guinea-Bissau Peso", "GWP", "-$ #", "", "-", ".", 2, ",", 33333333);
		currencies[63] = new Currency(328, "Guyana Dollar", "GYD", "-$ #", "$", "-", ".", 2, ",", 33333333);
		currencies[64] = new Currency(344, "Hong Kong Dollar", "HKD", "-$#+", "$", "()", ".", 2, ",", 33333333);
		currencies[65] = new Currency(980, "Hryvnia ", "UAH", "-# $", "\u0433\u0440\u0432.", "-", ",", 2, ".", 33333333);
		currencies[66] = new Currency(352, "Iceland Krona", "ISK", "-# $", "kr.", "-", ",", 0, ".", 33333333);
		currencies[67] = new Currency(356, "Indian Rupee", "INR", "-$#", "\u0930\u0942", "-", ".", 2, ",", 22222223);
		currencies[68] = new Currency(364, "Iranian Rial", "IRR", "-$ #", "\ufdfc", "-", ".", 2, ",", 33333333);
		currencies[69] = new Currency(368, "Iraqi Dinar", "IQD", "$ #-", "\u062f.\u0639.\u200f", "-", ".", 3, ",", 33333333);
		currencies[70] = new Currency(388, "Jamaican Dollar", "JMD", "-$ #", "$", "-", ".", 2, ",", 33333333);
		currencies[71] = new Currency(400, "Jordanian Dinar", "JOD", "$ #-", "\u062f.\u0623.\u200f", "-", ".", 3, ",", 33333333);
		currencies[72] = new Currency(404, "Kenyan Shilling", "KES", "-$ #", "Sh", "-", ".", 2, ",", 33333333);
		currencies[73] = new Currency(598, "Kina", "PGK", "-$ #", "K", "-", ".", 2, ",", 33333333);
		currencies[74] = new Currency(418, "Kip", "LAK", "-$ #", "\u20ad", "-", ".", 2, ",", 33333333);
		currencies[75] = new Currency(233, "Kroon", "EEK", "-# $", "kr", "-", ",", 2, " ", 33333333);
		currencies[76] = new Currency(414, "Kuwaiti Dinar", "KWD", "$ #-", "\u062f.\u0643.\u200f", "-", ".", 3, ",", 33333333);
		currencies[77] = new Currency(454, "Kwacha", "MWK", "-$ #", "MK", "-", ".", 2, ",", 33333333);
		currencies[78] = new Currency(973, "Kwanza", "AOA", "-$ #", "Kz", "-", ".", 2, ",", 33333333);
		currencies[79] = new Currency(104, "Kyat", "MMK", "-$ #", "K", "-", ".", 2, ",", 33333333);
		currencies[80] = new Currency(981, "Lari", "GEL", "-$ #", "\u10DA", "-", ".", 2, ",", 33333333);
		currencies[81] = new Currency(428, "Latvian Lats", "LVL", "-# $", "Ls", "-", ",", 2, " ", 33333333);
		currencies[82] = new Currency(422, "Lebanese Pound", "LBP", "$ #-", "\u0644.\u0644.\u200f", "-", ".", 2, ",", 33333333);
		currencies[83] = new Currency(8, "Lek", "ALL", "-$#", "Lek", "-", ",", 2, ".", 33333333);
		currencies[84] = new Currency(340, "Lempira", "HNL", "-$#+", "L", "()", ".", 2, ",", 33333333);
		currencies[85] = new Currency(694, "Leone", "SLL", "-$ #", "Le", "-", ".", 2, ",", 33333333);
		currencies[86] = new Currency(430, "Liberian Dollar", "LRD", "-$ #", "$", "-", ".", 2, ",", 33333333);
		currencies[87] = new Currency(434, "Libyan Dinar", "LYD", "$ #-", "\u062f.\u0644.\u200f", "-", ".", 3, ",", 33333333);
		currencies[88] = new Currency(748, "Lilangeni", "SZL", "-$ #", "L", "-", ".", 2, ",", 33333333);
		currencies[89] = new Currency(440, "Lithuanian Litas", "LTL", "-# $", "Lt", "-", ",", 2, ".", 33333333);
		currencies[90] = new Currency(426, "Loti", "LSL", "-$ #", "L", "-", ".", 2, ",", 33333333);
		currencies[91] = new Currency(969, "Malagasy Ariary", "MGA", "-$ #", "", "-", ".", 2, ",", 33333333);
		currencies[92] = new Currency(458, "Malaysian Ringgit", "MYR", "-$ #", "RM", "-", ".", 2, ",", 33333333);
		currencies[93] = new Currency(934, "Manat", "TMT", "-$ #", "", "-", ".", 2, ",", 33333333);
		currencies[94] = new Currency(480, "Mauritius Rupee", "MUR", "-$ #", "\u20A8", "-", ".", 2, ",", 33333333);
		currencies[95] = new Currency(943, "Metical", "MZN", "-$ #", "MTn", "-", ".", 2, ",", 33333333);
		currencies[96] = new Currency(484, "Mexican Peso", "MXN", "-$#", "$", "-", ".", 2, ",", 33333333);
		currencies[97] = new Currency(979, "Mexican Unidad de Inversion (UDI)", "MXV", "-$ #", "", "-", ".", 2, ",", 33333333);
		currencies[98] = new Currency(498, "Moldovan Leu", "MDL", "-$ #", "L", "-", ".", 2, ",", 33333333);
		currencies[99] = new Currency(504, "Moroccan Dirham", "MAD", "$ #-", "\u062f.\u0645.\u200f", "-", ".", 2, ",", 33333333);
		currencies[100] = new Currency(984, "Mvdol", "BOV", "-$ #", "", "-", ".", 2, ",", 33333333);
		currencies[101] = new Currency(566, "Naira", "NGN", "-$ #", "\u20A6", "-", ".", 2, ",", 33333333);
		currencies[102] = new Currency(232, "Nakfa", "ERN", "-$ #", "Nfk", "-", ".", 2, ",", 33333333);
		currencies[103] = new Currency(516, "Namibia Dollar", "NAD", "-$ #", "$", "-", ".", 2, ",", 33333333);
		currencies[104] = new Currency(524, "Nepalese Rupee", "NPR", "-$ #", "\u20a8", "-", ".", 2, ",", 33333333);
		currencies[105] = new Currency(532, "Netherlands Antillian Guilder", "ANG", "-$ #", "\u0192", "-", ".", 2, ",", 33333333);
		currencies[106] = new Currency(376, "New Israeli Sheqel", "ILS", "#- $", "\u05e9\"\u05d7", "-", ".", 2, ",", 33333333);
		currencies[107] = new Currency(946, "New Leu", "RON", "-# $", "LEI", "-", ",", 2, ".", 33333333);
		currencies[108] = new Currency(901, "New Taiwan Dollar", "TWD", "-$#", "NT$", "-", ".", 2, ",", 33333333);
		currencies[109] = new Currency(554, "New Zealand Dollar", "NZD", "-$#", "$", "-", ".", 2, ",", 33333333);
		currencies[110] = new Currency(64, "Ngultrum", "BTN", "-$ #", "", "-", ".", 2, ",", 33333333);
		currencies[111] = new Currency(408, "North Korean Won", "KPW", "-$ #", "\u20A9", "-", ".", 2, ",", 33333333);
		currencies[112] = new Currency(578, "Norwegian Krone", "NOK", "$ -#", "kr", "-", ",", 2, " ", 33333333);
		currencies[113] = new Currency(604, "Nuevo Sol", "PEN", "$-#", "S/", "-", ",", 2, ".", 33333333);
		currencies[114] = new Currency(478, "Ouguiya", "MRO", "-$ #", "UM", "-", ".", 2, ",", 33333333);
		currencies[115] = new Currency(776, "Pa'anga", "TOP", "-$ #", "T$", "-", ".", 2, ",", 33333333);
		currencies[116] = new Currency(586, "Pakistan Rupee", "PKR", "-$ #", "\u20a8", "-", ".", 2, ",", 33333333);
		currencies[117] = new Currency(964, "Palladium", "XPD", "-$ #", "", "-", ".", 0, ",", 33333333);
		currencies[118] = new Currency(446, "Pataca", "MOP", "-$ #", "P", "-", ".", 2, ",", 33333333);
		currencies[119] = new Currency(858, "Peso Uruguayo", "UYU", "-$#+", "NU$", "()", ",", 2, ".", 33333333);
		currencies[120] = new Currency(608, "Philippine Peso", "PHP", "-$ #", "Php", "-", ".", 2, ",", 33333333);
		currencies[121] = new Currency(962, "Platinum", "XPT", "-$ #", "", "-", ".", 0, ",", 33333333);
		currencies[122] = new Currency(826, "Pound Sterling", "GBP", "-$#", "\u00A3", "-", ".", 2, ",", 33333333);
		currencies[123] = new Currency(72, "Pula", "BWP", "-$ #", "P", "-", ".", 2, ",", 33333333);
		currencies[124] = new Currency(634, "Qatari Rial", "QAR", "$ #-", "\u0631.\u0642.\u200f", "-", ".", 2, ",", 33333333);
		currencies[125] = new Currency(320, "Quetzal", "GTQ", "-$#+", "Q", "()", ".", 2, ",", 33333333);
		currencies[126] = new Currency(710, "Rand", "ZAR", "$-#", "R", "-", ".", 2, ",", 33333333);
		currencies[127] = new Currency(512, "Rial Omani", "OMR", "$ #-", "\u0631.\u0639.\u200f", "-", ".", 3, ",", 33333333);
		currencies[128] = new Currency(116, "Riel", "KHR", "-$ #", "\u17db", "-", ".", 2, ",", 33333333);
		currencies[129] = new Currency(462, "Rufiyaa", "MVR", "-$ #", "\u0783.", "-", ".", 2, ",", 33333333);
		currencies[130] = new Currency(360, "Rupiah", "IDR", "-$ #", "Rp", "-", ".", 2, ",", 33333333);
		currencies[131] = new Currency(643, "Russian Ruble", "RUB", "-# $", "\u0440\u0443\u0431.", "-", ",", 2, " ", 33333333);
		currencies[132] = new Currency(646, "Rwanda Franc", "RWF", "-$ #", "Fr", "-", ".", 0, ",", 33333333);
		currencies[133] = new Currency(654, "Saint Helena Pound", "SHP", "-$ #", "\u00a3", "-", ".", 2, ",", 33333333);
		currencies[134] = new Currency(682, "Saudi Riyal", "SAR", "$ #-", "\u0631.\u0633.\u200f", "-", ".", 2, ",", 33333333);
		currencies[135] = new Currency(960, "SDR", "XDR", "-$ #", "", "-", ".", 0, ",", 33333333);
		currencies[136] = new Currency(941, "Serbian Dinar", "RSD", "-$ #", "\u0434\u0438\u043d.", "-", ".", 2, ",", 33333333);
		currencies[137] = new Currency(690, "Seychelles Rupee", "SCR", "-$ #", "\u20a8", "-", ".", 2, ",", 33333333);
		currencies[138] = new Currency(961, "Silver", "XAG", "-$ #", "", "-", ".", 0, ",", 33333333);
		currencies[139] = new Currency(702, "Singapore Dollar", "SGD", "-$ #", "$", "-", ".", 2, ",", 33333333);
		currencies[140] = new Currency(90, "Solomon Islands Dollar", "SBD", "-$ #", "$", "-", ".", 2, ",", 33333333);
		currencies[141] = new Currency(417, "Som", "KGS", "-$ #", "", "-", ".", 2, ",", 33333333);
		currencies[142] = new Currency(706, "Somali Shilling", "SOS", "-$ #", "Sh", "-", ".", 2, ",", 33333333);
		currencies[143] = new Currency(972, "Somoni", "TJS", "-$ #", "SM", "-", ".", 2, ",", 33333333);
		currencies[144] = new Currency(144, "Sri Lanka Rupee", "LKR", "-$ #", "\u20a8", "-", ".", 2, ",", 33333333);
		currencies[145] = new Currency(938, "Sudanese Pound", "SDG", "$ #-", "\u062c.\u0633.\u200f", "-", ".", 2, ",", 33333333);
		currencies[146] = new Currency(968, "Surinam Dollar", "SRD", "-$ #", "$", "-", ".", 2, ",", 33333333);
		currencies[147] = new Currency(752, "Swedish Krona", "SEK", "-# $", "kr", "-", ",", 2, " ", 33333333);
		currencies[148] = new Currency(756, "Swiss Franc", "CHF", "$-#", "SFr.", "-", ".", 2, "'", 33333333);
		currencies[149] = new Currency(760, "Syrian Pound", "SYP", "$ #-", "\u0644.\u0633.\u200f", "-", ".", 2, ",", 33333333);
		currencies[150] = new Currency(50, "Taka", "BDT", "-$ #", "\u09f3", "-", ".", 2, ",", 33333333);
		currencies[151] = new Currency(882, "Tala", "WST", "-$ #", "T", "-", ".", 2, ",", 33333333);
		currencies[152] = new Currency(834, "Tanzanian Shilling", "TZS", "-$ #", "Sh", "-", ".", 2, ",", 33333333);
		currencies[153] = new Currency(398, "Tenge", "KZT", "-$ #", "\u3012", "-", ".", 2, ",", 33333333);
		currencies[154] = new Currency(780, "Trinidad and Tobago Dollar", "TTD", "-$ #", "$", "-", ".", 2, ",", 33333333);
		currencies[155] = new Currency(496, "Tugrik", "MNT", "-$ #", "\u20ae", "-", ".", 2, ",", 33333333);
		currencies[156] = new Currency(788, "Tunisian Dinar", "TND", "$ #-", "\u062f.\u062a.\u200f", "-", ".", 3, ",", 33333333);
		currencies[157] = new Currency(949, "Turkish Lira", "TRY", "-# $", "YTL", "-", ",", 2, ".", 33333333);
		currencies[158] = new Currency(784, "UAE Dirham", "AED", "$ #-", "\u062f.\u0625.\u200f", "-", ".", 2, ",", 33333333);
		currencies[159] = new Currency(800, "Uganda Shilling", "UGX", "-$ #", "Sh", "-", ".", 2, ",", 33333333);
		currencies[160] = new Currency(970, "Unidad de Valor Real", "COU", "-$ #", "", "-", ".", 2, ",", 33333333);
		currencies[161] = new Currency(990, "Unidades de fomento", "CLF", "-$ #", "", "-", ".", 0, ",", 33333333);
		currencies[162] = new Currency(940, "Uruguay Peso en Unidades Indexadas", "UYI", "-$ #", "", "-", ".", 2, ",", 33333333);
		currencies[163] = new Currency(840, "US Dollar", "USD", "-$#+", "$", "()", ".", 2, ",", 33333333);
		currencies[164] = new Currency(997, "US Dollar (Next day)", "USN", "-$#+", "$", "()", ".", 2, ",", 33333333);
		currencies[165] = new Currency(998, "US Dollar (Same day)", "USS", "-$#+", "$", "()", ".", 2, ",", 33333333);
		currencies[166] = new Currency(860, "Uzbekistan Sum", "UZS", "-$ #", "", "-", ".", 2, ",", 33333333);
		currencies[167] = new Currency(548, "Vatu", "VUV", "-$ #", "Vt", "-", ".", 0, ",", 33333333);
		currencies[168] = new Currency(947, "WIR Euro", "CHE", "-$ #", "", "-", ".", 2, ",", 33333333);
		currencies[169] = new Currency(948, "WIR Franc", "CHW", "-$ #", "", "-", ".", 2, ",", 33333333);
		currencies[170] = new Currency(410, "Won", "KRW", "-$ #", "\uffe6", "-", ".", 0, ",", 33333333);
		currencies[171] = new Currency(886, "Yemeni Rial", "YER", "$ #-", "\u0631.\u064a.\u200f", "-", ".", 2, ",", 33333333);
		currencies[172] = new Currency(392, "Yen", "JPY", "-$ #", "\uffe5", "-", ".", 0, ",", 33333333);
		currencies[173] = new Currency(156, "Yuan Renminbi", "CNY", "-$ #", "\uffe5", "-", ".", 2, ",", 33333333);
		currencies[174] = new Currency(894, "Zambian Kwacha", "ZMK", "-$ #", "ZK", "-", ".", 2, ",", 33333333);
		currencies[175] = new Currency(935, "Zimbabwe Dollar", "ZWR", "-$ #", "$", "-", ".", 2, ",", 33333333);
		currencies[176] = new Currency(985, "Zloty", "PLN", "-# $", "z\u0142", "-", ",", 2, " ", 33333333);
		return currencies;
	}
}
