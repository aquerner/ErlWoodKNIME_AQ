package org.erlwood.knime.utils.datacell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.knime.core.data.DataCell;
import org.knime.core.data.def.StringCell;

public class MissingOrStringCellFactoryTest {

	@Test
	public void nullBecomesMissingCell() {
		assertTrue(MissingOrStringCellFactory.create(null).isMissing());
		assertTrue(MissingOrStringCellFactory.create(null, false).isMissing());
	}

	@Test
	public void emptyStringIsMissingByDefault() {
		assertTrue(MissingOrStringCellFactory.create("").isMissing());
	}

	@Test
	public void emptyStringCanBeKeptAsStringCell() {
		DataCell cell = MissingOrStringCellFactory.create("", false);
		assertEquals(new StringCell(""), cell);
	}

	@Test
	public void nonEmptyStringBecomesStringCell() {
		assertEquals(new StringCell("value"), MissingOrStringCellFactory.create("value"));
	}

	@Test
	public void instanceHonoursEmptyIsMissingFlag() {
		assertTrue(new MissingOrStringCellFactory(true).createCell("").isMissing());
		assertEquals(new StringCell(""), new MissingOrStringCellFactory(false).createCell(""));
		assertEquals(StringCell.TYPE, new MissingOrStringCellFactory(true).getDataType());
	}
}
