package org.erlwood.knime.utils.gui.documentfilters;

import static org.junit.Assert.assertEquals;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

import org.junit.Test;

public class DocumentFiltersTest {

	private static AbstractDocument doc(DocumentFilter filter, String initial) throws BadLocationException {
		PlainDocument d = new PlainDocument();
		d.insertString(0, initial, null);
		d.setDocumentFilter(filter);
		return d;
	}

	private static String text(AbstractDocument d) throws BadLocationException {
		return d.getText(0, d.getLength());
	}

	@Test
	public void integerFilterAcceptsDigitsAndRejectsLetters() throws BadLocationException {
		AbstractDocument d = doc(new IntegerDocumentFilter(), "");
		d.insertString(0, "12", null);
		assertEquals("12", text(d));
		d.insertString(2, "a", null);
		assertEquals("12", text(d));
		d.insertString(2, "3", null);
		assertEquals("123", text(d));
	}

	@Test
	public void integerFilterRejectsDecimalPoint() throws BadLocationException {
		AbstractDocument d = doc(new IntegerDocumentFilter(), "1");
		d.replace(1, 0, ".5", null);
		assertEquals("1", text(d));
	}

	@Test
	public void integerFilterReplaceValidatesWholeDocument() throws BadLocationException {
		AbstractDocument d = doc(new IntegerDocumentFilter(), "42");
		d.replace(0, 2, "7", null);
		assertEquals("7", text(d));
		d.replace(0, 1, "x", null);
		assertEquals("7", text(d));
		d.replace(1, 0, "8", null);
		assertEquals("78", text(d));
	}

	@Test
	public void integerFilterReplaceRejectsResultThatOverflowsInt() throws BadLocationException {
		AbstractDocument d = doc(new IntegerDocumentFilter(), "2147483647");
		d.replace(10, 0, "0", null);
		assertEquals("2147483647", text(d));
	}

	@Test
	public void integerFilterAllowsNegativeValueByDefault() throws BadLocationException {
		AbstractDocument d = doc(new IntegerDocumentFilter(), "");
		d.insertString(0, "-5", null);
		assertEquals("-5", text(d));
	}

	@Test
	public void integerFilterCanRejectNegativeValues() throws BadLocationException {
		AbstractDocument d = doc(new IntegerDocumentFilter(false), "");
		d.insertString(0, "-5", null);
		assertEquals("", text(d));
		d.insertString(0, "5", null);
		assertEquals("5", text(d));
		d.replace(0, 1, "-3", null);
		assertEquals("5", text(d));
	}

	@Test
	public void integerFilterRejectsLoneMinusSign() throws BadLocationException {
		AbstractDocument d = doc(new IntegerDocumentFilter(true), "");
		d.replace(0, 0, "-", null);
		assertEquals("", text(d));
	}

	@Test
	public void removeIsNeverFiltered() throws BadLocationException {
		AbstractDocument d = doc(new IntegerDocumentFilter(false), "123");
		d.remove(0, 3);
		assertEquals("", text(d));
	}

	@Test
	public void decimalFilterAcceptsFloatingPointForms() throws BadLocationException {
		AbstractDocument d = doc(new DecimalDocumentFilter(), "");
		d.insertString(0, "1.5", null);
		assertEquals("1.5", text(d));
		d.replace(0, 3, "1e3", null);
		assertEquals("1e3", text(d));
		d.replace(0, 3, "-0.25", null);
		assertEquals("-0.25", text(d));
		d.replace(0, 5, "3.", null);
		assertEquals("3.", text(d));
	}

	@Test
	public void decimalFilterRejectsNonNumericText() throws BadLocationException {
		AbstractDocument d = doc(new DecimalDocumentFilter(), "1.5");
		d.insertString(3, "abc", null);
		assertEquals("1.5", text(d));
		d.replace(0, 3, "1.2.3", null);
		assertEquals("1.5", text(d));
		d.replace(0, 3, "", null);
		assertEquals("1.5", text(d));
	}

	@Test
	public void decimalFilterCanRejectNegativeValues() throws BadLocationException {
		AbstractDocument d = doc(new DecimalDocumentFilter(false), "");
		d.insertString(0, "-1.5", null);
		assertEquals("", text(d));
		d.insertString(0, "1.5", null);
		assertEquals("1.5", text(d));
		d.replace(0, 0, "-", null);
		assertEquals("1.5", text(d));
	}

	@Test
	public void decimalFilterAllowsNegativeZero() throws BadLocationException {
		AbstractDocument d = doc(new DecimalDocumentFilter(false), "");
		d.insertString(0, "-0.0", null);
		assertEquals("-0.0", text(d));
	}

	@Test
	public void multipleIntegerFilterAcceptsCommaSeparatedIntegers() throws BadLocationException {
		AbstractDocument d = doc(new MultipleIntegerDocumentFilter(), "");
		d.insertString(0, "1,2,3", null);
		assertEquals("1,2,3", text(d));
		d.replace(5, 0, ",-4", null);
		assertEquals("1,2,3,-4", text(d));
	}

	@Test
	public void multipleIntegerFilterRejectsBadElementAnywhere() throws BadLocationException {
		AbstractDocument d = doc(new MultipleIntegerDocumentFilter(), "1,2");
		d.replace(3, 0, ",x", null);
		assertEquals("1,2", text(d));
		d.replace(3, 0, ",3.5", null);
		assertEquals("1,2", text(d));
		d.replace(3, 0, ", 3", null);
		assertEquals("1,2", text(d));
	}

	@Test
	public void multipleIntegerFilterRejectsEmptyElements() throws BadLocationException {
		AbstractDocument d = doc(new MultipleIntegerDocumentFilter(), "1");
		d.replace(1, 0, ",,2", null);
		assertEquals("1", text(d));
		d.replace(0, 1, ",1", null);
		assertEquals("1", text(d));
	}

	@Test
	public void multipleIntegerFilterAcceptsTrailingDelimiter() throws BadLocationException {
		AbstractDocument d = doc(new MultipleIntegerDocumentFilter(), "1");
		d.replace(1, 0, ",", null);
		assertEquals("1,", text(d));
	}

	@Test
	public void multipleIntegerFilterHonoursCustomDelimiter() throws BadLocationException {
		AbstractDocument d = doc(new MultipleIntegerDocumentFilter(";"), "");
		d.insertString(0, "1;2;3", null);
		assertEquals("1;2;3", text(d));
		d.replace(0, 5, "1,2", null);
		assertEquals("1;2;3", text(d));
	}

	@Test
	public void noWhiteSpaceFilterRejectsSpaces() throws BadLocationException {
		AbstractDocument d = doc(new NoWhiteSpaceDocumentFilter(), "");
		d.insertString(0, "abc", null);
		assertEquals("abc", text(d));
		d.insertString(3, " def", null);
		assertEquals("abc", text(d));
		d.replace(0, 3, "a b", null);
		assertEquals("abc", text(d));
		d.replace(3, 0, "123!@#", null);
		assertEquals("abc123!@#", text(d));
	}

	@Test
	public void noWhiteSpaceFilterOnlyChecksPlainSpaceCharacter() throws BadLocationException {
		AbstractDocument d = doc(new NoWhiteSpaceDocumentFilter(), "");
		d.insertString(0, "a\tb", null);
		assertEquals("a\tb", text(d));
	}

	@Test
	public void replaceBeyondDocumentEndAppends() throws BadLocationException {
		AbstractDocument d = doc(new IntegerDocumentFilter(), "12");
		d.replace(2, 0, "3", null);
		assertEquals("123", text(d));
	}
}
