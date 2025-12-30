package com.aevrontech.finevo.domain.model

/**
 * Currency data class representing a currency with code, name, symbol, and decimal digits. Data
 * sourced from currencies.json with symbol_native, decimal_digits, and name fields.
 */
data class CurrencyInfo(
    val code: String,
    val displayName: String,
    val symbol: String,
    val decimalDigits: Int = 2
) {
    /**
     * Check if this currency matches a search query. Matches against code, symbol, or display name
     * (case-insensitive).
     */
    fun matchesSearch(query: String): Boolean {
        if (query.isBlank()) return true
        val lowerQuery = query.lowercase().trim()
        return code.lowercase().contains(lowerQuery) ||
            symbol.lowercase().contains(lowerQuery) ||
            displayName.lowercase().contains(lowerQuery)
    }

    /** Format an amount for this currency. */
    fun formatAmount(amount: Double): String {
        return "$symbol ${String.format("%.${decimalDigits}f", kotlin.math.abs(amount))}"
    }
}

/** Provider for getting available currencies. Uses a curated list from currencies.json. */
object CurrencyProvider {

    /**
     * All currencies from currencies.json, using symbol_native, name, and decimal_digits. Sorted
     * alphabetically by name.
     */
    private val allCurrencies: List<CurrencyInfo> =
        listOf(
            CurrencyInfo("USD", "US Dollar", "$", 2),
            CurrencyInfo("CAD", "Canadian Dollar", "$", 2),
            CurrencyInfo("EUR", "Euro", "€", 2),
            CurrencyInfo("AED", "United Arab Emirates Dirham", "د.إ.‏", 2),
            CurrencyInfo("AFN", "Afghan Afghani", "؋", 0),
            CurrencyInfo("ALL", "Albanian Lek", "Lek", 0),
            CurrencyInfo("AMD", "Armenian Dram", "դdelays.", 0),
            CurrencyInfo("ARS", "Argentine Peso", "$", 2),
            CurrencyInfo("AUD", "Australian Dollar", "$", 2),
            CurrencyInfo("AZN", "Azerbaijani Manat", "ман.", 2),
            CurrencyInfo("BAM", "Bosnia-Herzegovina Convertible Mark", "KM", 2),
            CurrencyInfo("BDT", "Bangladeshi Taka", "৳", 2),
            CurrencyInfo("BGN", "Bulgarian Lev", "лв.", 2),
            CurrencyInfo("BHD", "Bahraini Dinar", "د.ب.‏", 3),
            CurrencyInfo("BIF", "Burundian Franc", "FBu", 0),
            CurrencyInfo("BND", "Brunei Dollar", "$", 2),
            CurrencyInfo("BOB", "Bolivian Boliviano", "Bs", 2),
            CurrencyInfo("BRL", "Brazilian Real", "R$", 2),
            CurrencyInfo("BWP", "Botswanan Pula", "P", 2),
            CurrencyInfo("BYR", "Belarusian Ruble", "BYR", 0),
            CurrencyInfo("BZD", "Belize Dollar", "$", 2),
            CurrencyInfo("CDF", "Congolese Franc", "FrCD", 2),
            CurrencyInfo("CHF", "Swiss Franc", "CHF", 2),
            CurrencyInfo("CLP", "Chilean Peso", "$", 0),
            CurrencyInfo("CNY", "Chinese Yuan", "CN¥", 2),
            CurrencyInfo("COP", "Colombian Peso", "$", 0),
            CurrencyInfo("CRC", "Costa Rican Colón", "₡", 0),
            CurrencyInfo("CVE", "Cape Verdean Escudo", "CV$", 2),
            CurrencyInfo("CZK", "Czech Republic Koruna", "Kč", 2),
            CurrencyInfo("DJF", "Djiboutian Franc", "Fdj", 0),
            CurrencyInfo("DKK", "Danish Krone", "kr", 2),
            CurrencyInfo("DOP", "Dominican Peso", "RD$", 2),
            CurrencyInfo("DZD", "Algerian Dinar", "د.ج.‏", 2),
            CurrencyInfo("EGP", "Egyptian Pound", "ج.م.‏", 2),
            CurrencyInfo("ERN", "Eritrean Nakfa", "Nfk", 2),
            CurrencyInfo("ETB", "Ethiopian Birr", "Br", 2),
            CurrencyInfo("GBP", "British Pound Sterling", "£", 2),
            CurrencyInfo("GEL", "Georgian Lari", "GEL", 2),
            CurrencyInfo("GHS", "Ghanaian Cedi", "GH₵", 2),
            CurrencyInfo("GNF", "Guinean Franc", "FG", 0),
            CurrencyInfo("GTQ", "Guatemalan Quetzal", "Q", 2),
            CurrencyInfo("HKD", "Hong Kong Dollar", "$", 2),
            CurrencyInfo("HNL", "Honduran Lempira", "L", 2),
            CurrencyInfo("HRK", "Croatian Kuna", "kn", 2),
            CurrencyInfo("HUF", "Hungarian Forint", "Ft", 0),
            CurrencyInfo("IDR", "Indonesian Rupiah", "Rp", 0),
            CurrencyInfo("ILS", "Israeli New Sheqel", "₪", 2),
            CurrencyInfo("INR", "Indian Rupee", "₹", 2),
            CurrencyInfo("IQD", "Iraqi Dinar", "د.ع.‏", 0),
            CurrencyInfo("IRR", "Iranian Rial", "﷼", 0),
            CurrencyInfo("ISK", "Icelandic Króna", "kr", 0),
            CurrencyInfo("JMD", "Jamaican Dollar", "$", 2),
            CurrencyInfo("JOD", "Jordanian Dinar", "د.أ.‏", 3),
            CurrencyInfo("JPY", "Japanese Yen", "￥", 0),
            CurrencyInfo("KES", "Kenyan Shilling", "Ksh", 2),
            CurrencyInfo("KHR", "Cambodian Riel", "៛", 2),
            CurrencyInfo("KMF", "Comorian Franc", "FC", 0),
            CurrencyInfo("KRW", "South Korean Won", "₩", 0),
            CurrencyInfo("KWD", "Kuwaiti Dinar", "د.ك.‏", 3),
            CurrencyInfo("KZT", "Kazakhstani Tenge", "тңг.", 2),
            CurrencyInfo("LBP", "Lebanese Pound", "ل.ل.‏", 0),
            CurrencyInfo("LKR", "Sri Lankan Rupee", "SL Re", 2),
            CurrencyInfo("LYD", "Libyan Dinar", "د.ل.‏", 3),
            CurrencyInfo("MAD", "Moroccan Dirham", "د.م.‏", 2),
            CurrencyInfo("MDL", "Moldovan Leu", "MDL", 2),
            CurrencyInfo("MGA", "Malagasy Ariary", "MGA", 0),
            CurrencyInfo("MKD", "Macedonian Denar", "MKD", 2),
            CurrencyInfo("MMK", "Myanma Kyat", "K", 0),
            CurrencyInfo("MOP", "Macanese Pataca", "MOP$", 2),
            CurrencyInfo("MUR", "Mauritian Rupee", "MURs", 0),
            CurrencyInfo("MXN", "Mexican Peso", "$", 2),
            CurrencyInfo("MYR", "Malaysian Ringgit", "RM", 2),
            CurrencyInfo("MZN", "Mozambican Metical", "MTn", 2),
            CurrencyInfo("NAD", "Namibian Dollar", "N$", 2),
            CurrencyInfo("NGN", "Nigerian Naira", "₦", 2),
            CurrencyInfo("NIO", "Nicaraguan Córdoba", "C$", 2),
            CurrencyInfo("NOK", "Norwegian Krone", "kr", 2),
            CurrencyInfo("NPR", "Nepalese Rupee", "नेरू", 2),
            CurrencyInfo("NZD", "New Zealand Dollar", "$", 2),
            CurrencyInfo("OMR", "Omani Rial", "ر.ع.‏", 3),
            CurrencyInfo("PAB", "Panamanian Balboa", "B/.", 2),
            CurrencyInfo("PEN", "Peruvian Nuevo Sol", "S/.", 2),
            CurrencyInfo("PHP", "Philippine Peso", "₱", 2),
            CurrencyInfo("PKR", "Pakistani Rupee", "₨", 0),
            CurrencyInfo("PLN", "Polish Zloty", "zł", 2),
            CurrencyInfo("PYG", "Paraguayan Guarani", "₲", 0),
            CurrencyInfo("QAR", "Qatari Rial", "ر.ق.‏", 2),
            CurrencyInfo("RON", "Romanian Leu", "RON", 2),
            CurrencyInfo("RSD", "Serbian Dinar", "дин.", 0),
            CurrencyInfo("RUB", "Russian Ruble", "руб.", 2),
            CurrencyInfo("RWF", "Rwandan Franc", "FR", 0),
            CurrencyInfo("SAR", "Saudi Riyal", "ر.س.‏", 2),
            CurrencyInfo("SDG", "Sudanese Pound", "SDG", 2),
            CurrencyInfo("SEK", "Swedish Krona", "kr", 2),
            CurrencyInfo("SGD", "Singapore Dollar", "$", 2),
            CurrencyInfo("SOS", "Somali Shilling", "Ssh", 0),
            CurrencyInfo("SYP", "Syrian Pound", "ل.س.‏", 0),
            CurrencyInfo("THB", "Thai Baht", "฿", 2),
            CurrencyInfo("TND", "Tunisian Dinar", "د.ت.‏", 3),
            CurrencyInfo("TOP", "Tongan Paʻanga", "T$", 2),
            CurrencyInfo("TRY", "Turkish Lira", "TL", 2),
            CurrencyInfo("TTD", "Trinidad and Tobago Dollar", "$", 2),
            CurrencyInfo("TWD", "New Taiwan Dollar", "NT$", 2),
            CurrencyInfo("TZS", "Tanzanian Shilling", "TSh", 0),
            CurrencyInfo("UAH", "Ukrainian Hryvnia", "₴", 2),
            CurrencyInfo("UGX", "Ugandan Shilling", "USh", 0),
            CurrencyInfo("UYU", "Uruguayan Peso", "$", 2),
            CurrencyInfo("UZS", "Uzbekistan Som", "UZS", 0),
            CurrencyInfo("VEF", "Venezuelan Bolívar", "Bs.F.", 2),
            CurrencyInfo("VND", "Vietnamese Dong", "₫", 0),
            CurrencyInfo("XAF", "CFA Franc BEAC", "FCFA", 0),
            CurrencyInfo("XOF", "CFA Franc BCEAO", "CFA", 0),
            CurrencyInfo("YER", "Yemeni Rial", "ر.ي.‏", 0),
            CurrencyInfo("ZAR", "South African Rand", "R", 2),
            CurrencyInfo("ZMK", "Zambian Kwacha", "ZK", 0)
        )
            .sortedBy { it.displayName }

    /** Get all available currencies. */
    fun getAllCurrencies(): List<CurrencyInfo> = allCurrencies

    /** Get a specific currency by code. */
    fun getCurrency(code: String): CurrencyInfo? =
        allCurrencies.find { it.code.equals(code, ignoreCase = true) }

    /** Get commonly used currencies (empty - not showing popular section). */
    fun getPopularCurrencies(): List<CurrencyInfo> = emptyList()
}

/** Helper to get currency symbol from code. */
fun getCurrencySymbol(code: String): String {
    return CurrencyProvider.getCurrency(code)?.symbol ?: code
}
