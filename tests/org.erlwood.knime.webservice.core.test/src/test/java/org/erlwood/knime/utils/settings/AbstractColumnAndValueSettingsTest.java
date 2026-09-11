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

public class AbstractColumnAndValueSettingsTest {

	private static final String COLUMN_KEY = "concentrationColumn";

	private static final String VALUE_KEY = "concentrationValue";

	private static final String USE_COLUMN_KEY = "useConcentrationColumn";

	private static final String COLUMN_FAIL_MESSAGE = "You must specify a concentration column.";

	private static final String VALUE_FAIL_MESSAGE = "You must specify a concentration value.";

	private static class Concentration extends AbstractColumnAndValueSettings {

		private boolean useColumnByDefault;

		private boolean ignoreValidation;

		@Override
		public String getColumnConfigKey() {
			return COLUMN_KEY;
		}

		@Override
		public String getValueConfigKey() {
			return VALUE_KEY;
		}

		@Override
		protected String getUseColumnConfigKey() {
			return USE_COLUMN_KEY;
		}

		@Override
		protected String getColumnValidationFailMessage() {
			return COLUMN_FAIL_MESSAGE;
		}

		@Override
		protected String getValueValidationFailMessage() {
			return VALUE_FAIL_MESSAGE;
		}

		@Override
		protected boolean useColumnByDefault() {
			return useColumnByDefault;
		}

		@Override
		protected boolean isIgnoreValidation() {
			return ignoreValidation;
		}

		@Override
		protected boolean validateValue(final String v) {
			return AbstractValueSettings.validateDoubleValue(v, 0.0, 100.0, true);
		}

	}

	@Test
	public void columnAndValueStartUnsetAndValueModeIsTheDefault() {
		Concentration settingsModel = new Concentration();

		assertNull(settingsModel.getColumnName());
		assertNull(settingsModel.getValue());
		assertFalse(settingsModel.isUseColumn());
	}

	@Test
	public void allThreeFieldsSurviveSaveAndLoadRoundTrip() throws InvalidSettingsException {
		Concentration saved = new Concentration();
		saved.setColumnName("Concentration");
		saved.setValue("10.0");
		saved.setUseColumn(true);

		NodeSettings settings = new NodeSettings("test");
		saved.saveSettingsTo(settings);
		assertEquals("Concentration", settings.getString(COLUMN_KEY));
		assertEquals("10.0", settings.getString(VALUE_KEY));
		assertTrue(settings.getBoolean(USE_COLUMN_KEY));

		Concentration loaded = new Concentration();
		loaded.loadSettingsFrom(settings);
		assertEquals("Concentration", loaded.getColumnName());
		assertEquals("10.0", loaded.getValue());
		assertTrue(loaded.isUseColumn());
	}

	@Test
	public void loadingAbsentSettingsAppliesTheUseColumnDefault() throws InvalidSettingsException {
		Concentration useValue = new Concentration();
		useValue.setUseColumn(true);
		useValue.loadSettingsFrom(new NodeSettings("empty"));
		assertFalse(useValue.isUseColumn());

		Concentration useColumn = new Concentration();
		useColumn.useColumnByDefault = true;
		useColumn.loadSettingsFrom(new NodeSettings("empty"));
		assertTrue(useColumn.isUseColumn());
	}

	@Test
	public void validationChecksTheColumnWhenTheColumnIsInUse() {
		Concentration settingsModel = new Concentration();

		NodeSettings noColumn = new NodeSettings("test");
		noColumn.addBoolean(USE_COLUMN_KEY, true);
		noColumn.addString(VALUE_KEY, "10.0");
		assertValidationFails(settingsModel, noColumn, COLUMN_FAIL_MESSAGE);
	}

	@Test
	public void validationIgnoresAnInvalidValueWhenTheColumnIsInUse()
			throws InvalidSettingsException {
		Concentration settingsModel = new Concentration();

		NodeSettings settings = new NodeSettings("test");
		settings.addBoolean(USE_COLUMN_KEY, true);
		settings.addString(COLUMN_KEY, "Concentration");
		settings.addString(VALUE_KEY, "9999.0");

		settingsModel.validateSettings(settings);
	}

	@Test
	public void validationChecksTheValueWhenTheColumnIsNotInUse() {
		Concentration settingsModel = new Concentration();

		NodeSettings noValue = new NodeSettings("test");
		noValue.addBoolean(USE_COLUMN_KEY, false);
		noValue.addString(COLUMN_KEY, "Concentration");
		assertValidationFails(settingsModel, noValue, VALUE_FAIL_MESSAGE);

		NodeSettings outOfRange = new NodeSettings("test");
		outOfRange.addBoolean(USE_COLUMN_KEY, false);
		outOfRange.addString(VALUE_KEY, "9999.0");
		assertValidationFails(settingsModel, outOfRange, VALUE_FAIL_MESSAGE);
	}

	@Test
	public void validationTreatsAnAbsentUseColumnFlagAsValueMode() {
		Concentration settingsModel = new Concentration();
		settingsModel.useColumnByDefault = true;

		NodeSettings columnOnly = new NodeSettings("test");
		columnOnly.addString(COLUMN_KEY, "Concentration");

		// the use column default only applies when loading, validation defaults to the value
		assertValidationFails(settingsModel, columnOnly, VALUE_FAIL_MESSAGE);
	}

	@Test
	public void ignoringValidationSkipsTheValueCheckButNotTheColumnCheck() {
		Concentration settingsModel = new Concentration();
		settingsModel.ignoreValidation = true;

		NodeSettings valueMode = new NodeSettings("test");
		valueMode.addBoolean(USE_COLUMN_KEY, false);
		try {
			settingsModel.validateSettings(valueMode);
		} catch(InvalidSettingsException e) {
			fail("Value validation should have been skipped: " + e.getMessage());
		}

		NodeSettings columnMode = new NodeSettings("test");
		columnMode.addBoolean(USE_COLUMN_KEY, true);
		assertValidationFails(settingsModel, columnMode, COLUMN_FAIL_MESSAGE);
	}

	@Test
	public void validationAcceptsAValueAtTheInclusiveRangeBoundaries()
			throws InvalidSettingsException {
		Concentration settingsModel = new Concentration();

		for(String value : new String[] { "0.0", "50.0", "100.0" }) {
			NodeSettings settings = new NodeSettings("test");
			settings.addBoolean(USE_COLUMN_KEY, false);
			settings.addString(VALUE_KEY, value);
			settingsModel.validateSettings(settings);
		}
	}

	private static void assertValidationFails(final Concentration settingsModel,
			final NodeSettings settings, final String expectedMessage) {
		try {
			settingsModel.validateSettings(settings);
			fail("Expected InvalidSettingsException");
		} catch(InvalidSettingsException e) {
			assertEquals(expectedMessage, e.getMessage());
		}
	}

}
