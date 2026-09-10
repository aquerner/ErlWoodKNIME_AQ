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
package org.erlwood.knime.utils.datacell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.knime.core.data.DataCell;
import org.knime.core.data.def.StringCell;

public class MissingOrStringCellFactoryTest {

	@Test
	public void nullInputCreatesMissingCell() {
		assertTrue(MissingOrStringCellFactory.create(null).isMissing());
		assertTrue(MissingOrStringCellFactory.create(null, false).isMissing());
	}

	@Test
	public void emptyInputIsMissingOnlyWhenRequested() {
		assertTrue(MissingOrStringCellFactory.create("").isMissing());
		assertFalse(MissingOrStringCellFactory.create("", false).isMissing());
	}

	@Test
	public void valueCreatesStringCell() {
		DataCell cell = MissingOrStringCellFactory.create("CC(=O)Oc1ccccc1C(=O)O");
		assertFalse(cell.isMissing());
		assertEquals("CC(=O)Oc1ccccc1C(=O)O", ((StringCell)cell).getStringValue());
	}

	@Test
	public void factoryDelegatesToStaticCreate() {
		MissingOrStringCellFactory factory = new MissingOrStringCellFactory(true);
		assertEquals(StringCell.TYPE, factory.getDataType());
		assertTrue(factory.createCell("").isMissing());
		assertFalse(factory.createCell("aspirin").isMissing());
	}

}
