package org.erlwood.knime.utils.datacell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.knime.core.data.DataCell;
import org.knime.core.data.def.StringCell;

public class MissingOrStringCellFactoryTest {

	@Test
	public void nullIsAlwaysMissing() {
		assertTrue(MissingOrStringCellFactory.create(null).isMissing());
		assertTrue(MissingOrStringCellFactory.create(null, false).isMissing());
		assertTrue(new MissingOrStringCellFactory(false).createCell(null).isMissing());
	}

	@Test
	public void emptyStringIsMissingByDefault() {
		assertTrue(MissingOrStringCellFactory.create("").isMissing());
		assertTrue(MissingOrStringCellFactory.create("", true).isMissing());
		assertTrue(new MissingOrStringCellFactory(true).createCell("").isMissing());
	}

	@Test
	public void emptyStringCanBeKeptAsStringCell() {
		DataCell cell = MissingOrStringCellFactory.create("", false);
		assertFalse(cell.isMissing());
		assertEquals(new StringCell(""), cell);
		assertEquals(new StringCell(""), new MissingOrStringCellFactory(false).createCell(""));
	}

	@Test
	public void nonEmptyStringBecomesStringCell() {
		DataCell cell = MissingOrStringCellFactory.create("hello");
		assertFalse(cell.isMissing());
		assertEquals(new StringCell("hello"), cell);
		assertEquals("hello", ((StringCell) cell).getStringValue());
		assertEquals(new StringCell(" "), MissingOrStringCellFactory.create(" "));
	}

	@Test
	public void factoryDataTypeIsString() {
		assertEquals(StringCell.TYPE, new MissingOrStringCellFactory(true).getDataType());
	}
}
