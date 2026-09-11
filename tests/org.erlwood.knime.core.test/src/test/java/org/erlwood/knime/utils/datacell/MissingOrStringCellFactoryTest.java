package org.erlwood.knime.utils.datacell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.knime.core.data.DataCell;
import org.knime.core.data.def.StringCell;

public class MissingOrStringCellFactoryTest {

	@Test
	public void nullIsAlwaysMissing() {
		assertTrue(MissingOrStringCellFactory.create(null).isMissing());
		assertTrue(MissingOrStringCellFactory.create(null, false).isMissing());
	}

	@Test
	public void emptyStringIsMissingByDefault() {
		assertTrue(MissingOrStringCellFactory.create("").isMissing());
	}

	@Test
	public void emptyStringIsStringCellWhenEmptyIsNotMissing() {
		DataCell cell = MissingOrStringCellFactory.create("", false);
		assertEquals(new StringCell(""), cell);
	}

	@Test
	public void nonEmptyStringIsStringCell() {
		assertEquals(new StringCell("abc"), MissingOrStringCellFactory.create("abc"));
	}

	@Test
	public void instanceHonoursEmptyIsMissingFlag() {
		MissingOrStringCellFactory missing = new MissingOrStringCellFactory(true);
		MissingOrStringCellFactory keep = new MissingOrStringCellFactory(false);
		assertTrue(missing.createCell("").isMissing());
		assertEquals(new StringCell(""), keep.createCell(""));
		assertEquals(StringCell.TYPE, missing.getDataType());
	}
}
