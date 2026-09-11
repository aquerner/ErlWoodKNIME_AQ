package org.erlwood.knime.utils.gui.layout;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TableLayoutConstraintsTest implements TableLayoutConstants {

	private static void assertCells(TableLayoutConstraints c, int col1, int row1, int col2, int row2) {
		assertEquals("col1", col1, c.getCol1());
		assertEquals("row1", row1, c.getRow1());
		assertEquals("col2", col2, c.getCol2());
		assertEquals("row2", row2, c.getRow2());
	}

	private static void assertAlign(TableLayoutConstraints c, int h, int v) {
		assertEquals("hAlign", h, c.gethAlign());
		assertEquals("vAlign", v, c.getvAlign());
	}

	@Test
	public void defaultConstructorIsSingleCellFullyJustified() {
		TableLayoutConstraints c = new TableLayoutConstraints();
		assertCells(c, 0, 0, 0, 0);
		assertAlign(c, FULL, FULL);
	}

	@Test
	public void parsesSingleCellColumnAndRow() {
		TableLayoutConstraints c = new TableLayoutConstraints("3, 5");
		assertCells(c, 3, 5, 3, 5);
		assertAlign(c, FULL, FULL);
	}

	@Test
	public void parsesSpanningCellRange() {
		TableLayoutConstraints c = new TableLayoutConstraints("1, 2, 4, 6");
		assertCells(c, 1, 2, 4, 6);
		assertAlign(c, FULL, FULL);
	}

	@Test
	public void acceptsSpacesInsteadOfCommas() {
		assertCells(new TableLayoutConstraints("1 2 3 4"), 1, 2, 3, 4);
		assertCells(new TableLayoutConstraints("1,2,3,4"), 1, 2, 3, 4);
		assertCells(new TableLayoutConstraints("  1 ,  2 "), 1, 2, 1, 2);
	}

	@Test
	public void parsesAlignmentLettersAfterSingleCell() {
		TableLayoutConstraints c = new TableLayoutConstraints("2, 3, L, T");
		assertCells(c, 2, 3, 2, 3);
		assertAlign(c, LEFT, TOP);

		assertAlign(new TableLayoutConstraints("0, 0, C, C"), CENTER, CENTER);
		assertAlign(new TableLayoutConstraints("0, 0, R, B"), RIGHT, BOTTOM);
		assertAlign(new TableLayoutConstraints("0, 0, F, F"), FULL, FULL);
	}

	@Test
	public void alignmentLettersAreCaseInsensitive() {
		assertAlign(new TableLayoutConstraints("0, 0, l, b"), LEFT, BOTTOM);
		assertAlign(new TableLayoutConstraints("0, 0, r, t"), RIGHT, TOP);
	}

	@Test
	public void horizontalAlignmentAloneLeavesVerticalFull() {
		TableLayoutConstraints c = new TableLayoutConstraints("0, 0, R");
		assertAlign(c, RIGHT, FULL);
	}

	@Test
	public void unknownAlignmentLetterKeepsFull() {
		TableLayoutConstraints c = new TableLayoutConstraints("0, 0, X, Y");
		assertAlign(c, FULL, FULL);
		assertCells(c, 0, 0, 0, 0);
	}

	@Test
	public void unknownHorizontalLetterStillParsesVertical() {
		assertAlign(new TableLayoutConstraints("0, 0, X, B"), FULL, BOTTOM);
	}

	@Test
	public void emptyStringGivesDefaults() {
		TableLayoutConstraints c = new TableLayoutConstraints("");
		assertCells(c, 0, 0, 0, 0);
		assertAlign(c, FULL, FULL);
	}

	@Test
	public void nonNumericFirstTokenGivesDefaultCell() {
		TableLayoutConstraints c = new TableLayoutConstraints("abc");
		assertCells(c, 0, 0, 0, 0);
		assertAlign(c, FULL, FULL);
	}

	@Test
	public void invalidSecondColumnAbandonsRangeParsing() {
		TableLayoutConstraints c = new TableLayoutConstraints("1, 2, zz, 9");
		assertCells(c, 1, 2, 1, 2);
		assertAlign(c, FULL, FULL);
	}

	@Test
	public void reversedRangeIsNormalised() {
		TableLayoutConstraints c = new TableLayoutConstraints("5, 6, 1, 2");
		assertCells(c, 5, 6, 5, 6);
	}

	@Test
	public void explicitConstructorStoresValues() {
		TableLayoutConstraints c = new TableLayoutConstraints(1, 2, 3, 4, LEFT, BOTTOM);
		assertCells(c, 1, 2, 3, 4);
		assertAlign(c, LEFT, BOTTOM);
	}

	@Test
	public void explicitConstructorReplacesOutOfRangeAlignmentWithFull() {
		assertAlign(new TableLayoutConstraints(0, 0, 0, 0, MIN_ALIGN - 1, MAX_ALIGN + 1), FULL, FULL);
		assertAlign(new TableLayoutConstraints(0, 0, 0, 0, 99, -99), FULL, FULL);
		assertAlign(new TableLayoutConstraints(0, 0, 0, 0, MIN_ALIGN, MAX_ALIGN), LEFT, BOTTOM);
	}

	@Test
	public void explicitConstructorDoesNotNormaliseReversedRange() {
		TableLayoutConstraints c = new TableLayoutConstraints(5, 6, 1, 2, FULL, FULL);
		assertCells(c, 5, 6, 1, 2);
	}

	@Test
	public void settersUpdateFields() {
		TableLayoutConstraints c = new TableLayoutConstraints();
		c.setCol1(1);
		c.setRow1(2);
		c.setCol2(3);
		c.setRow2(4);
		c.sethAlign(RIGHT);
		c.setvAlign(TOP);
		assertCells(c, 1, 2, 3, 4);
		assertAlign(c, RIGHT, TOP);
	}

	@Test
	public void toStringOfSingleCellShowsAlignmentLetters() {
		TableLayoutConstraints c = new TableLayoutConstraints(2, 3, 2, 3, LEFT, BOTTOM);
		assertEquals("3, 2, L, B", c.toString());
		assertEquals("0, 0, F, F", new TableLayoutConstraints().toString());
		assertEquals("0, 0, C, C", new TableLayoutConstraints(0, 0, 0, 0, CENTER, CENTER).toString());
		assertEquals("0, 0, R, T", new TableLayoutConstraints(0, 0, 0, 0, RIGHT, TOP).toString());
	}

	@Test
	public void toStringOfSpanningCellShowsSecondCorner() {
		TableLayoutConstraints c = new TableLayoutConstraints(1, 2, 3, 4, LEFT, BOTTOM);
		assertEquals("2, 1, 4, 3", c.toString());
	}
}
