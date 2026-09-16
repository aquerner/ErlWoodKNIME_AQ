package org.erlwood.knime.utils.gui.documentfilters;

import static org.junit.Assert.assertEquals;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

import org.junit.Test;

public class DocumentFilterTest {

	private static AbstractDocument document(final DocumentFilter filter, final String initial)
			throws BadLocationException {
		PlainDocument doc = new PlainDocument();
		doc.insertString(0, initial, null);
		doc.setDocumentFilter(filter);
		return doc;
	}

	private static String text(final AbstractDocument doc) throws BadLocationException {
		return doc.getText(0, doc.getLength());
	}

	@Test
	public void integerFilterAcceptsIntegers() throws BadLocationException {
		AbstractDocument doc = document(new IntegerDocumentFilter(), "");
		doc.insertString(0, "42", null);
		assertEquals("42", text(doc));
		doc.insertString(0, "-", null);
		assertEquals("insertString validates only the inserted fragment", "42", text(doc));
		doc.replace(0, 0, "-", null);
		assertEquals("-42", text(doc));
	}

	@Test
	public void integerFilterRejectsNonNumericInsert() throws BadLocationException {
		AbstractDocument doc = document(new IntegerDocumentFilter(), "12");
		doc.insertString(2, "a", null);
		doc.insertString(2, "1.5", null);
		assertEquals("12", text(doc));
	}

	@Test
	public void integerFilterRejectsNegativeWhenNotAllowed() throws BadLocationException {
		AbstractDocument doc = document(new IntegerDocumentFilter(false), "");
		doc.insertString(0, "-5", null);
		assertEquals("", text(doc));
		doc.insertString(0, "5", null);
		assertEquals("5", text(doc));
	}

	@Test
	public void integerFilterReplaceValidatesWholeDocument() throws BadLocationException {
		AbstractDocument doc = document(new IntegerDocumentFilter(), "123");
		doc.replace(1, 1, "x", null);
		assertEquals("123", text(doc));
		doc.replace(1, 1, "9", null);
		assertEquals("193", text(doc));
		doc.replace(3, 0, "7", null);
		assertEquals("1937", text(doc));
	}

	@Test
	public void decimalFilterAcceptsDecimals() throws BadLocationException {
		AbstractDocument doc = document(new DecimalDocumentFilter(), "");
		doc.insertString(0, "3.14", null);
		assertEquals("3.14", text(doc));
		doc.replace(0, 0, "-", null);
		assertEquals("-3.14", text(doc));
		doc.insertString(5, "abc", null);
		assertEquals("-3.14", text(doc));
	}

	@Test
	public void decimalFilterRejectsNegativeWhenNotAllowed() throws BadLocationException {
		AbstractDocument doc = document(new DecimalDocumentFilter(false), "");
		doc.insertString(0, "-0.5", null);
		assertEquals("", text(doc));
		doc.insertString(0, "0.5", null);
		assertEquals("0.5", text(doc));
	}

	@Test
	public void multipleIntegerFilterAcceptsDelimitedIntegers() throws BadLocationException {
		AbstractDocument doc = document(new MultipleIntegerDocumentFilter(), "");
		doc.insertString(0, "1,2,3", null);
		assertEquals("1,2,3", text(doc));
		doc.insertString(5, ",x", null);
		assertEquals("1,2,3", text(doc));
		doc.replace(0, 5, "4;5", null);
		assertEquals("1,2,3", text(doc));
	}

	@Test
	public void multipleIntegerFilterHonoursCustomDelimiter() throws BadLocationException {
		AbstractDocument doc = document(new MultipleIntegerDocumentFilter(";"), "");
		doc.insertString(0, "4;5", null);
		assertEquals("4;5", text(doc));
		doc.insertString(3, ",6", null);
		assertEquals("4;5", text(doc));
	}

	@Test
	public void noWhiteSpaceFilterRejectsSpaces() throws BadLocationException {
		AbstractDocument doc = document(new NoWhiteSpaceDocumentFilter(), "");
		doc.insertString(0, "abc", null);
		doc.insertString(3, " def", null);
		assertEquals("abc", text(doc));
		doc.replace(0, 3, "a c", null);
		assertEquals("abc", text(doc));
		doc.replace(0, 3, "xyz", null);
		assertEquals("xyz", text(doc));
	}
}
