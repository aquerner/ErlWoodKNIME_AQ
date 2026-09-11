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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettings;

public class AbstractColumnNameSettingsTest {

	private static final String CONFIG_KEY = "smilesColumn";

	private static final String FAIL_MESSAGE = "You must specify a valid SMILES column.";

	private static class ColumnSettings extends AbstractColumnNameSettings {

		private boolean ignoreValidation;

		@Override
		public String getColumnConfigKey() {
			return CONFIG_KEY;
		}

		@Override
		protected String getValidationFailMessage() {
			return FAIL_MESSAGE;
		}

		@Override
		protected boolean isIgnoreValidation() {
			return ignoreValidation;
		}

	}

	@Test
	public void columnNameIsNullBeforeAnythingIsSet() {
		assertNull(new ColumnSettings().getColumnName());
	}

	@Test
	public void columnNameSurvivesSaveAndLoadRoundTrip() throws InvalidSettingsException {
		ColumnSettings saved = new ColumnSettings();
		saved.setColumnName("Structure");

		NodeSettings settings = new NodeSettings("test");
		saved.saveSettingsTo(settings);
		assertEquals("Structure", settings.getString(CONFIG_KEY));

		ColumnSettings loaded = new ColumnSettings();
		loaded.loadSettingsFrom(settings);
		assertEquals("Structure", loaded.getColumnName());
	}

	@Test
	public void loadingAbsentKeyResetsTheColumnNameToNull() throws InvalidSettingsException {
		ColumnSettings settingsModel = new ColumnSettings();
		settingsModel.setColumnName("Structure");

		settingsModel.loadSettingsFrom(new NodeSettings("empty"));

		assertNull(settingsModel.getColumnName());
	}

	@Test
	public void validationRejectsAbsentNullAndEmptyColumnNames() {
		ColumnSettings settingsModel = new ColumnSettings();

		assertValidationFails(settingsModel, new NodeSettings("empty"));

		NodeSettings nullName = new NodeSettings("test");
		nullName.addString(CONFIG_KEY, null);
		assertValidationFails(settingsModel, nullName);

		NodeSettings emptyName = new NodeSettings("test");
		emptyName.addString(CONFIG_KEY, "");
		assertValidationFails(settingsModel, emptyName);
	}

	@Test
	public void validationAcceptsAColumnNameInTheSettingsEvenWhenTheFieldIsUnset()
			throws InvalidSettingsException {
		ColumnSettings settingsModel = new ColumnSettings();

		NodeSettings settings = new NodeSettings("test");
		settings.addString(CONFIG_KEY, "Structure");
		settingsModel.validateSettings(settings);

		// validation reads the settings only, it does not populate the model
		assertNull(settingsModel.getColumnName());
	}

	@Test
	public void ignoringValidationAcceptsAnAbsentColumnName() throws InvalidSettingsException {
		ColumnSettings settingsModel = new ColumnSettings();
		settingsModel.ignoreValidation = true;

		settingsModel.validateSettings(new NodeSettings("empty"));
	}

	private static void assertValidationFails(final ColumnSettings settingsModel,
			final NodeSettings settings) {
		try {
			settingsModel.validateSettings(settings);
			fail("Expected InvalidSettingsException");
		} catch(InvalidSettingsException e) {
			assertEquals(FAIL_MESSAGE, e.getMessage());
		}
	}

}
