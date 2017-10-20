/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Cinnober Financial Technology AB (cinnober.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.cinnober.ciguan.datasource.getter;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.cinnober.ciguan.AsFormatIf;
import com.cinnober.ciguan.AsInitializationException;
import com.cinnober.ciguan.CwfBusinessTypeIf;
import com.cinnober.ciguan.data.AsDictionaryLanguage;
import com.cinnober.ciguan.data.AsLocale;
import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.impl.AsComponent;
import com.cinnober.ciguan.impl.CwfBusinessTypes;
import com.cinnober.ciguan.locale.impl.AsLocalePattern;

/**
 * Helper class to find formatters for business type + locale.
 */
public class AsFormat extends AsComponent implements AsFormatIf {

    /*
     * TODO: How to do?
     */
    /**
     * The Interface DIVISOR.
     */
    private static interface DIVISOR {

        /** The price. */
        int PRICE = 1000000;

        /** The qty. */
        int QTY = 1000000;

        /** The interest. */
        int INTEREST = 1000000;

        /** The decimal. */
        int DECIMAL = 1000000;

    }

    /** The Constant EMPTY_STRING. */
    protected static final String EMPTY_STRING = "";

    /*
     * Parsers for the standard Proteus date/time formats, which are not locale
     * sensitive
     */
    /** The m date parser. */
    protected SimpleDateFormat mDateParser = new SimpleDateFormat("yyyy-MM-dd");

    /** The m date time parser. */
    protected SimpleDateFormat mDateTimeParser = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS");

    /** The m time parser. */
    protected SimpleDateFormat mTimeParser = new SimpleDateFormat(
            "HH:mm:ss.SSS");

    /** The m formatters. */
    protected final Map<Locale, Map<CwfBusinessTypeIf, Format>> mFormatters = new HashMap<Locale, Map<CwfBusinessTypeIf, Format>>();

    @Override
    public int getDivisor(CwfBusinessTypeIf pBusinessType) {
        if (pBusinessType == CwfBusinessTypes.Price) {
            return DIVISOR.PRICE;
        }
        if (pBusinessType == CwfBusinessTypes.Amount
                || pBusinessType == CwfBusinessTypes.Volume) {
            return DIVISOR.QTY;
        }
        if (pBusinessType == CwfBusinessTypes.InterestRate
                || pBusinessType == CwfBusinessTypes.Percent
                || pBusinessType == CwfBusinessTypes.BasisPoint) {
            return DIVISOR.INTEREST;
        }
        if (pBusinessType == CwfBusinessTypes.Decimal) {
            return DIVISOR.DECIMAL;
        }
        return 1;
    }

    @Override
    public void startComponent() throws AsInitializationException {
        for (AsDictionaryLanguage tLanguage : As.getDictionaryHandler()
                .getDictionaryLanguages()) {
            Locale tLocale = tLanguage.getLocale();
            for (AsLocale tAsLocale : As.getConfigXmlParser().getLocales()) {
                if (tAsLocale.id.equals(tLocale.toString())) {
                    Map<CwfBusinessTypeIf, Format> tFormatters = new HashMap<CwfBusinessTypeIf, Format>();
                    addFormatters(tLocale, tAsLocale, tFormatters);
                    mFormatters.put(tLocale, tFormatters);
                }
            }
        }
    }

    /**
     * Adds the formatters.
     *
     * @param pLocale
     *            the locale
     * @param pAsLocale
     *            the as locale
     * @param pFormatters
     *            the formatters
     */
    protected void addFormatters(Locale pLocale, AsLocale pAsLocale,
            Map<CwfBusinessTypeIf, Format> pFormatters) {
        for (AsLocalePattern tPattern : pAsLocale.patterns) {
            CwfBusinessTypeIf tBusinessType = CwfBusinessTypes
                    .get(tPattern.type);
            Format tFormat = null;
            if (tBusinessType == CwfBusinessTypes.Date
                    || tBusinessType == CwfBusinessTypes.DateTime
                    || tBusinessType == CwfBusinessTypes.Time) {
                tFormat = new SimpleDateFormat(tPattern.value, pLocale);
            }
            if (tBusinessType == CwfBusinessTypes.Price
                    || tBusinessType == CwfBusinessTypes.Volume
                    || tBusinessType == CwfBusinessTypes.Amount
                    || tBusinessType == CwfBusinessTypes.Decimal
                    || tBusinessType == CwfBusinessTypes.Percent
                    || tBusinessType == CwfBusinessTypes.InterestRate
                    || tBusinessType == CwfBusinessTypes.BasisPoint) {
                DecimalFormatSymbols tSymbols = new DecimalFormatSymbols(
                        pLocale);
                tSymbols.setGroupingSeparator(pAsLocale.groupSeparator
                        .charAt(0));
                tSymbols.setDecimalSeparator(pAsLocale.decimalSeparator
                        .charAt(0));
                tFormat = new DecimalFormat(tPattern.value, tSymbols);
            }
            pFormatters.put(tBusinessType, tFormat);
        }
    }

    @Override
    public String format(Object pValue, CwfBusinessTypeIf pBusinessType,
            Locale pLocale) {
        if (pBusinessType == CwfBusinessTypes.Date) {
            return formatDate((String) pValue, pLocale);
        }
        if (pBusinessType == CwfBusinessTypes.DateTime) {
            return formatDateTime((String) pValue, pLocale);
        }
        if (pBusinessType == CwfBusinessTypes.Time) {
            return formatTime((String) pValue, pLocale);
        }
        if (pBusinessType == CwfBusinessTypes.Price) {
            return formatPrice((Number) pValue, pLocale);
        }
        if (pBusinessType == CwfBusinessTypes.Volume) {
            return formatVolume((Number) pValue, pLocale);
        }
        if (pBusinessType == CwfBusinessTypes.Amount) {
            return formatAmount((Number) pValue, pLocale);
        }
        if (pBusinessType == CwfBusinessTypes.Decimal) {
            return formatDecimal((Number) pValue, pLocale);
        }
        if (pBusinessType == CwfBusinessTypes.Percent) {
            return formatPercent((Number) pValue, pLocale);
        }
        if (pBusinessType == CwfBusinessTypes.InterestRate) {
            return formatInterestRate((Number) pValue, pLocale);
        }
        if (pBusinessType == CwfBusinessTypes.BasisPoint) {
            return formatBasisPoint((Number) pValue, pLocale);
        }
        if (pBusinessType == CwfBusinessTypes.SecondsSinceTime) {
            return formatSecondsSinceTime(pValue, pLocale);
        }
        return pValue.toString();
    }

    /**
     * Format a price value.
     *
     * @param pValue
     *            the value
     * @param pLocale
     *            the locale
     * @return the formated string
     */
    protected String formatPrice(Number pValue, Locale pLocale) {
        if (pValue == null) {
            return EMPTY_STRING;
        }
        double tPrice = asDouble(pValue) / DIVISOR.PRICE;
        Format tFormat = mFormatters.get(pLocale).get(CwfBusinessTypes.Price);
        synchronized (tFormat) {
            return tFormat.format(tPrice);
        }
    }

    /**
     * Returns the value of the specified number as a <code>double</code> or
     * zero if the number is {@code null}.
     *
     * @param pNumber
     *            the number
     * @return the numeric value represented by this object after conversion to
     *         type <code>double</code>.
     */
    private double asDouble(Number pNumber) {
        if (pNumber == null) {
            return 0;
        }
        return pNumber.doubleValue();
    }

    /**
     * Format a volume value.
     *
     * @param pValue
     *            the value
     * @param pLocale
     *            the locale
     * @return the string
     */
    protected String formatVolume(Number pValue, Locale pLocale) {
        if (pValue == null) {
            return EMPTY_STRING;
        }
        double tVolume = asDouble(pValue) / DIVISOR.QTY;
        Format tFormat = mFormatters.get(pLocale).get(CwfBusinessTypes.Volume);
        synchronized (tFormat) {
            return tFormat.format(tVolume);
        }
    }

    /**
     * Format an amount value.
     *
     * @param pValue
     *            the value
     * @param pLocale
     *            the locale
     * @return the string
     */
    protected String formatAmount(Number pValue, Locale pLocale) {
        if (pValue == null) {
            return EMPTY_STRING;
        }
        double tAmount = asDouble(pValue) / DIVISOR.PRICE;
        Format tFormat = mFormatters.get(pLocale).get(CwfBusinessTypes.Amount);
        synchronized (tFormat) {
            return tFormat.format(tAmount);
        }
    }

    /**
     * Format a date value.
     *
     * @param pDate
     *            the date
     * @param pLocale
     *            the locale
     * @return the string
     */
    protected String formatDate(String pDate, Locale pLocale) {
        if (pDate == null || pDate.isEmpty()) {
            return EMPTY_STRING;
        }
        DateFormat tFormat = (DateFormat) mFormatters.get(pLocale).get(
                CwfBusinessTypes.Date);
        synchronized (mDateParser) {
            try {
                return tFormat
                        .format(mDateParser.parse(pDate.substring(0, 10)));
            } 
            catch (ParseException e) {
                return pDate.substring(0, 10);
            }
        }
    }

    /**
     * Format a date/time value.
     *
     * @param pDateTime
     *            the date time
     * @param pLocale
     *            the locale
     * @return the string
     */
    protected String formatDateTime(String pDateTime, Locale pLocale) {
        if (pDateTime == null || pDateTime.isEmpty()) {
            return EMPTY_STRING;
        }
        DateFormat tFormat = (DateFormat) mFormatters.get(pLocale).get(
                CwfBusinessTypes.DateTime);
        synchronized (mDateTimeParser) {
            try {
                return tFormat.format(mDateTimeParser.parse(pDateTime));
            } 
            catch (ParseException e) {
                return pDateTime.replace('T', ' ');
            }
        }
    }

    /**
     * Format a time value.
     *
     * @param pTime
     *            the time
     * @param pLocale
     *            the locale
     * @return the string
     */
    protected String formatTime(String pTime, Locale pLocale) {
        if (pTime == null || pTime.isEmpty()) {
            return EMPTY_STRING;
        }
        DateFormat tFormat = (DateFormat) mFormatters.get(pLocale).get(
                CwfBusinessTypes.Time);
        synchronized (mTimeParser) {
            try {
                return tFormat.format(mTimeParser.parse(pTime.substring(11)));
            } 
            catch (ParseException e) {
                return pTime.substring(11);
            }
        }
    }

    /**
     * Format a percent value.
     * 
     * NOTE: The value is always expressed as an integral value. One percent is
     * stored as 0.01, which means 10000 as the Proteus long value
     *
     * @param pValue
     *            the value
     * @param pLocale
     *            the locale
     * @return the string
     */
    protected String formatPercent(Number pValue, Locale pLocale) {
        if (pValue == null) {
            return EMPTY_STRING;
        }
        double tPct = asDouble(pValue) / DIVISOR.INTEREST * 100;
        Format tFormat = mFormatters.get(pLocale).get(CwfBusinessTypes.Percent);
        synchronized (tFormat) {
            return tFormat.format(tPct);
        }
    }

    /**
     * Format an interest rate value.
     *
     * @param pValue
     *            the value
     * @param pLocale
     *            the locale
     * @return the string
     */
    protected String formatInterestRate(Number pValue, Locale pLocale) {
        if (pValue == null) {
            return EMPTY_STRING;
        }
        double tRate = asDouble(pValue) / DIVISOR.INTEREST * 100;
        Format tFormat = mFormatters.get(pLocale).get(
                CwfBusinessTypes.InterestRate);
        synchronized (tFormat) {
            return tFormat.format(tRate);
        }
    }

    /**
     * Format a basis point value.
     * 
     * NOTE: 1bps = 0.01%, the value is always expressed as an integral value)
     * One bps is stored as 0.0001, which means 100 as the Proteus long value
     *
     * @param pValue
     *            the value
     * @param pLocale
     *            the locale
     * @return the string
     */
    protected String formatBasisPoint(Number pValue, Locale pLocale) {
        if (pValue == null) {
            return EMPTY_STRING;
        }
        double tBps = asDouble(pValue) / DIVISOR.INTEREST * 10000;
        Format tFormat = mFormatters.get(pLocale).get(
                CwfBusinessTypes.BasisPoint);
        synchronized (tFormat) {
            return tFormat.format(tBps);
        }
    }

    /**
     * Format a decimal value.
     *
     * @param pValue
     *            the value
     * @param pLocale
     *            the locale
     * @return the string
     */
    protected String formatDecimal(Number pValue, Locale pLocale) {
        if (pValue == null) {
            return EMPTY_STRING;
        }
        double tValue = asDouble(pValue) / DIVISOR.DECIMAL;
        Format tFormat = mFormatters.get(pLocale).get(CwfBusinessTypes.Decimal);
        synchronized (tFormat) {
            return tFormat.format(tValue);
        }
    }

    /**
     * Format a seconds since time value as hh:mm:ss Note that it's not a time,
     * but rather a time offset, so 24:00:00 and upwards is ok.
     *
     * @param pValue
     *            the value
     * @param pLocale
     *            the locale
     * @return the string
     */
    protected String formatSecondsSinceTime(Object pValue, Locale pLocale) {
        if (pValue == null) {
            return EMPTY_STRING;
        }
        try {
            int tValue = Integer.parseInt(pValue.toString());
            int tHours = tValue / 3600;
            int tMinutes = (tValue - (tHours * 3600)) / 60;
            int tSeconds = tValue - (tHours * 3600) - (tMinutes * 60);
            return pad(tHours) + ":" + pad(tMinutes) + ":" + pad(tSeconds);
        } 
        catch (NumberFormatException e) {
            return pValue.toString();
        }
    }

    /**
     * Pad the value with zero if necessary.
     *
     * @param pValue
     *            the value
     * @return the string
     */
    protected String pad(int pValue) {
        return pValue < 10 ? "0" + pValue : pValue + "";
    }

}
