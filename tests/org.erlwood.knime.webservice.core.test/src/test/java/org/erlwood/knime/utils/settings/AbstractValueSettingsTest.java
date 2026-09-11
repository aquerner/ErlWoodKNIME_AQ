/*
 * ------------------------------------------------------------------------
 *
 * Copyright (C) 2014 Eli Lilly and Company Limited
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettings;

public class AbstractValueSettingsTest {

	private static final String CONFIG_KEY = "threshold";

	private static final String FAIL_MESSAGE = "You must specify a threshold.";

	/** Plain implementation with no default and no additional validation. */
	private static class Simple extends AbstractValueSettings {

		@Override
		public String getValueConfigKey() {
			return CONFIG_KEY;
		}

		@Override
		protected String getValidationFailMessage() {
			return FAIL_MESSAGE;
		}

	}

	/** Implementation with a default value, a range check and optional validation skipping. */
	private static class Ranged extends AbstractValueSettings {

		private boolean ignoreValidation;

		@Override
		public String getValueConfigKey() {
			return CONFIG_KEY;
		}

		@Override
		protected String getValidationFailMessage() {
			return FAIL_MESSAGE;
		}

		@Override
		protected String getDefaultValue() {
			return "0.5";
		}

		@Override
		protected boolean isIgnoreValidation() {
			return ignoreValidation;
		}

		@Override
		protected boolean validateValue(final String v) {
			return validateDoubleValue(v, 0.0, 1.0, true);
		}

	}

	@Test
	public void defaultValueIsNullUnlessOverridden() {
		assertNull(new Simple().getValue());
		assertEquals("0.5", new Ranged().getValue());
	}

	@Test
	public void valueSurvivesSaveAndLoadRoundTrip() throws InvalidSettingsException {
		Simple saved = new Simple();
		saved.setValue("0.75");

		NodeSettings settings = new NodeSettings("test");
		saved.saveSettingsTo(settings);
		assertEquals("0.75", settings.getString(CONFIG_KEY));

		Simple loaded = new Simple();
		loaded.loadSettingsFrom(settings);
		assertEquals("0.75", loaded.getValue());
	}

	@Test
	public void loadingAbsentKeyFallsBackToTheDefaultValue() throws InvalidSettingsException {
		Ranged settingsModel = new Ranged();
		settingsModel.setValue("0.9");

		settingsModel.loadSettingsFrom(new NodeSettings("empty"));

		assertEquals("0.5", settingsModel.getValue());
	}

	@Test
	public void nullValueIsRoundTrippedAsNull() throws InvalidSettingsException {
		Simple saved = new Simple();
		saved.setValue(null);

		NodeSettings settings = new NodeSettings("test");
		saved.saveSettingsTo(settings);

		Simple loaded = new Simple();
		loaded.loadSettingsFrom(settings);
		assertNull(loaded.getValue());
	}

	@Test
	public void typedAccessorsParseTheStoredString() {
		Simple settingsModel = new Simple();

		settingsModel.setValue("12.5");
		assertEquals(12.5d, settingsModel.getDoubleValue(), 0.0d);

		settingsModel.setValue("42");
		assertEquals(42, settingsModel.getIntegerValue());
		assertEquals(42.0d, settingsModel.getDoubleValue(), 0.0d);

		settingsModel.setValue("true");
		assertTrue(settingsModel.getBoolean());

		settingsModel.setValue("TrUe");
		assertTrue(settingsModel.getBoolean());
	}

	@Test
	public void nonBooleanTextIsReadAsFalse() {
		Simple settingsModel = new Simple();

		settingsModel.setValue("yes");
		assertFalse(settingsModel.getBoolean());

		settingsModel.setValue("");
		assertFalse(settingsModel.getBoolean());
	}

	@Test(expected = NumberFormatException.class)
	public void integerAccessorRejectsMalformedText() {
		Simple settingsModel = new Simple();
		settingsModel.setValue("12.5");
		settingsModel.getIntegerValue();
	}

	@Test(expected = NumberFormatException.class)
	public void doubleAccessorRejectsMalformedText() {
		Simple settingsModel = new Simple();
		settingsModel.setValue("not-a-number");
		settingsModel.getDoubleValue();
	}

	@Test
	public void validationRejectsMissingAndEmptyValues() {
		Simple settingsModel = new Simple();

		assertValidationFails(settingsModel, new NodeSettings("empty"));

		NodeSettings empty = new NodeSettings("test");
		empty.addString(CONFIG_KEY, "");
		assertValidationFails(settingsModel, empty);
	}

	@Test
	public void validationUsesTheValueInTheSettingsRatherThanTheFieldValue()
			throws InvalidSettingsException {
		Ranged settingsModel = new Ranged();
		settingsModel.setValue("0.5");

		NodeSettings outOfRange = new NodeSettings("test");
		outOfRange.addString(CONFIG_KEY, "7.5");
		assertValidationFails(settingsModel, outOfRange);

		NodeSettings inRange = new NodeSettings("test");
		inRange.addString(CONFIG_KEY, "1.0");
		settingsModel.validateSettings(inRange);

		// validation must not have modified the held value
		assertEquals("0.5", settingsModel.getValue());
	}

	@Test
	public void ignoringValidationAcceptsAnythingIncludingAnAbsentValue()
			throws InvalidSettingsException {
		Ranged settingsModel = new Ranged();
		settingsModel.ignoreValidation = true;

		NodeSettings outOfRange = new NodeSettings("test");
		outOfRange.addString(CONFIG_KEY, "7.5");
		settingsModel.validateSettings(outOfRange);

		NodeSettings emptyValue = new NodeSettings("test");
		emptyValue.addString(CONFIG_KEY, "");
		settingsModel.validateSettings(emptyValue);
	}

	@Test
	public void doubleRangeCheckHonoursInclusivity() {
		assertTrue(AbstractValueSettings.validateDoubleValue("0.0", 0.0, 1.0, true));
		assertTrue(AbstractValueSettings.validateDoubleValue("1.0", 0.0, 1.0, true));
		assertFalse(AbstractValueSettings.validateDoubleValue("0.0", 0.0, 1.0, false));
		assertFalse(AbstractValueSettings.validateDoubleValue("1.0", 0.0, 1.0, false));
		assertTrue(AbstractValueSettings.validateDoubleValue("0.5", 0.0, 1.0, false));
		assertFalse(AbstractValueSettings.validateDoubleValue("1.0001", 0.0, 1.0, true));
		assertFalse(AbstractValueSettings.validateDoubleValue("-0.0001", 0.0, 1.0, true));
	}

	@Test
	public void integerRangeCheckHonoursInclusivity() {
		assertTrue(AbstractValueSettings.validateIntegerValue("1", 1, 10, true));
		assertTrue(AbstractValueSettings.validateIntegerValue("10", 1, 10, true));
		assertFalse(AbstractValueSettings.validateIntegerValue("1", 1, 10, false));
		assertFalse(AbstractValueSettings.validateIntegerValue("10", 1, 10, false));
		assertTrue(AbstractValueSettings.validateIntegerValue("2", 1, 10, false));
		assertFalse(AbstractValueSettings.validateIntegerValue("11", 1, 10, true));
		assertFalse(AbstractValueSettings.validateIntegerValue("0", 1, 10, true));
	}

	@Test
	public void rangeChecksRejectUnparsableAndNullInput() {
		assertFalse(AbstractValueSettings.validateDoubleValue(null, 0.0, 1.0, true));
		assertFalse(AbstractValueSettings.validateDoubleValue("", 0.0, 1.0, true));
		assertFalse(AbstractValueSettings.validateDoubleValue("abc", 0.0, 1.0, true));

		assertFalse(AbstractValueSettings.validateIntegerValue(null, 1, 10, true));
		assertFalse(AbstractValueSettings.validateIntegerValue("", 1, 10, true));
		assertFalse(AbstractValueSettings.validateIntegerValue("2.5", 1, 10, true));
	}

	@Test
	public void integerRangeCheckDoesNotAcceptDecimalRepresentationsOfIntegers() {
		assertTrue(AbstractValueSettings.validateDoubleValue("5.0", 1, 10, true));
		assertFalse(AbstractValueSettings.validateIntegerValue("5.0", 1, 10, true));
	}

	private static void assertValidationFails(final AbstractValueSettings settingsModel,
			final NodeSettings settings) {
		try {
			settingsModel.validateSettings(settings);
			fail("Expected InvalidSettingsException");
		} catch(InvalidSettingsException e) {
			assertEquals(FAIL_MESSAGE, e.getMessage());
		}
	}

}
