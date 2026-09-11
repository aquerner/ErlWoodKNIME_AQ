/*
 * ------------------------------------------------------------------------
 *
 * Copyright (C) 2017 Eli Lilly and Company Limited
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * ------------------------------------------------------------------------
*/
package org.erlwood.knime.utils.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Test;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettings;

public class AbstractDateSettingTest {

	private static class DateSetting extends AbstractDateSetting {

		@Override
		public String getColumnConfigKey() {
			return "dateColumn";
		}

		@Override
		public String getValueConfigKey() {
			return "dateValue";
		}

		@Override
		protected String getUseColumnConfigKey() {
			return "useDateColumn";
		}

	}

	private static DateSetting dateSetting(final String value) {
		DateSetting setting = new DateSetting();
		setting.setValue(value);
		return setting;
	}

	private static Date utcDate(final String value) {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			return formatter.parse(value);
		} catch(Exception e) {
			throw new IllegalArgumentException(value, e);
		}
	}

	@Test
	public void newSettingIsInitialisedWithTodaysDate() throws InvalidSettingsException {
		DateSetting setting = new DateSetting();

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		assertEquals(formatter.format(new Date()), setting.getValue());
		assertNotNull(setting.getDate());
	}

	@Test
	public void dateIsStoredInTheIsoFormatAndReadBackUnchanged() throws InvalidSettingsException {
		DateSetting setting = new DateSetting();
		setting.setDate(utcDate("2017-03-04"));

		assertEquals("2017-03-04", setting.getValue());
		assertEquals(utcDate("2017-03-04"), setting.getDate());
	}

	@Test
	public void aNullDateIsStoredAsAnEmptyStringAndReadBackAsNull()
			throws InvalidSettingsException {
		DateSetting setting = new DateSetting();
		setting.setDate(null);

		assertEquals("", setting.getValue());
		assertNull(setting.getDate());
	}

	@Test
	public void malformedStoredDateIsReportedAsAnInvalidSetting() {
		DateSetting setting = dateSetting("04/03/2017");

		try {
			setting.getDate();
			fail("Expected InvalidSettingsException");
		} catch(InvalidSettingsException e) {
			assertEquals("String '04/03/2017' does not conform to format yyyy-MM-dd.",
					e.getMessage());
		}
	}

	@Test
	public void dateSurvivesSaveAndLoadRoundTrip() throws InvalidSettingsException {
		DateSetting saved = new DateSetting();
		saved.setDate(utcDate("2016-02-29"));

		NodeSettings settings = new NodeSettings("test");
		saved.saveSettingsTo(settings);

		DateSetting loaded = new DateSetting();
		loaded.loadSettingsFrom(settings);
		assertEquals(utcDate("2016-02-29"), loaded.getDate());
	}

	@Test
	public void validationFailureMessagesDistinguishColumnFromValue() {
		DateSetting setting = new DateSetting();

		NodeSettings columnMode = new NodeSettings("test");
		columnMode.addBoolean("useDateColumn", true);
		assertValidationFails(setting, columnMode, "You must specify a valid date column.");

		NodeSettings valueMode = new NodeSettings("test");
		valueMode.addBoolean("useDateColumn", false);
		valueMode.addString("dateValue", "");
		assertValidationFails(setting, valueMode, "You must specify a valid date value.");
	}

	@Test
	public void xmlConversionPreservesTheInstantOfTheStoredDate()
			throws InvalidSettingsException, DatatypeConfigurationException {
		XMLGregorianCalendar calendar = AbstractDateSetting.convertDateToXML("2017-03-04");

		// the stored date is parsed as UTC midnight and carried over unchanged
		assertEquals(utcDate("2017-03-04").getTime(),
				calendar.toGregorianCalendar().getTimeInMillis());
	}

	@Test
	public void xmlConversionOfAnEmptyDateIsNull()
			throws InvalidSettingsException, DatatypeConfigurationException {
		assertNull(AbstractDateSetting.convertDateToXML(""));
	}

	@Test
	public void xmlConversionRejectsAMalformedDate() throws DatatypeConfigurationException {
		try {
			AbstractDateSetting.convertDateToXML("March 2017");
			fail("Expected InvalidSettingsException");
		} catch(InvalidSettingsException e) {
			assertEquals("String 'March 2017' does not conform to format yyyy-MM-dd.",
					e.getMessage());
		}
	}

	@Test
	public void dateRangeCheckAcceptsAValidRange() throws InvalidSettingsException {
		AbstractDateSetting.checkDates(dateSetting("2017-01-01"), dateSetting("2017-06-01"));
	}

	@Test
	public void dateRangeCheckIsSkippedWhenThereIsNoEndDate() throws InvalidSettingsException {
		AbstractDateSetting.checkDates(dateSetting("2017-01-01"), null);
	}

	@Test
	public void dateRangeCheckIsSkippedWhenTheEndDateComesFromAColumn()
			throws InvalidSettingsException {
		DateSetting end = dateSetting("2000-01-01");
		end.setUseColumn(true);

		AbstractDateSetting.checkDates(dateSetting("2017-01-01"), end);
	}

	@Test
	public void endDateOnlyRangeIsAccepted() throws InvalidSettingsException {
		AbstractDateSetting.checkDates(null, dateSetting("2017-06-01"));
	}

	@Test
	public void startDateFromAColumnLeavesTheRangeUnchecked() throws InvalidSettingsException {
		DateSetting start = dateSetting("2017-06-01");
		start.setUseColumn(true);

		// the start is unknown until execution, so the inverted range is not rejected
		AbstractDateSetting.checkDates(start, dateSetting("2017-01-01"));
	}

	@Test
	public void dateRangeCheckRejectsAnInvertedRange() {
		assertCheckDatesFails(dateSetting("2017-06-01"), dateSetting("2017-01-01"),
				"End date must be after start date.");
	}

	@Test
	public void dateRangeCheckRejectsAZeroLengthRange() {
		assertCheckDatesFails(dateSetting("2017-06-01"), dateSetting("2017-06-01"),
				"End date must be after start date.");
	}

	@Test
	public void dateRangeCheckRejectsMoreThanOneYear() {
		assertCheckDatesFails(dateSetting("2017-01-01"), dateSetting("2018-01-01"),
				"Date range cannot be greater than 1 year.");
	}

	@Test
	public void dateRangeCheckAcceptsAlmostOneYear() throws InvalidSettingsException {
		AbstractDateSetting.checkDates(dateSetting("2017-01-01"), dateSetting("2017-12-31"));
	}

	@Test
	public void dateRangeCheckAllowsAnExtraDayWhenTheEndYearIsALeapYear()
			throws InvalidSettingsException {
		// 366 days inclusive, which is only allowed because 2020 is a leap year
		Date start = utcDate("2019-12-31");
		Date end = utcDate("2020-12-30");
		long days = TimeUnit.DAYS.convert(end.getTime() - start.getTime(), TimeUnit.MILLISECONDS);
		assertEquals(365, days);

		AbstractDateSetting.checkDates(dateSetting("2019-12-31"), dateSetting("2020-12-30"));
		assertCheckDatesFails(dateSetting("2019-12-31"), dateSetting("2020-12-31"),
				"Date range cannot be greater than 1 year.");
	}

	@Test
	public void dateRangeCheckReportsMalformedDates() {
		assertCheckDatesFails(dateSetting("2017-01-01"), dateSetting("tomorrow"),
				"String 'tomorrow' does not conform to format yyyy-MM-dd.");
	}

	@Test
	public void futureEndDateIsNotRejected() throws InvalidSettingsException {
		// checkDates() intends to reject a range ending in the future, but the comparison is
		// Calendar.after(Date) which is only ever true for another Calendar, so the check never
		// fires. Pinned here as current behaviour, see the pull request description.
		GregorianCalendar future = new GregorianCalendar();
		future.add(Calendar.MONTH, 1);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		String futureDate = formatter.format(future.getTime());

		GregorianCalendar start = new GregorianCalendar();
		start.add(Calendar.MONTH, -1);
		String startDate = formatter.format(start.getTime());

		AbstractDateSetting.checkDates(dateSetting(startDate), dateSetting(futureDate));

		assertFalse(new GregorianCalendar().after(new Date()));
	}

	private static void assertValidationFails(final DateSetting setting,
			final NodeSettings settings, final String expectedMessage) {
		try {
			setting.validateSettings(settings);
			fail("Expected InvalidSettingsException");
		} catch(InvalidSettingsException e) {
			assertEquals(expectedMessage, e.getMessage());
		}
	}

	private static void assertCheckDatesFails(final AbstractDateSetting start,
			final AbstractDateSetting end, final String expectedMessage) {
		try {
			AbstractDateSetting.checkDates(start, end);
			fail("Expected InvalidSettingsException");
		} catch(InvalidSettingsException e) {
			assertEquals(expectedMessage, e.getMessage());
		}
	}

}
