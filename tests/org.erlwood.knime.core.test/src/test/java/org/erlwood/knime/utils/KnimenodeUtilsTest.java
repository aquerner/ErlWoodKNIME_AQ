package org.erlwood.knime.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.StringCell;

public class KnimenodeUtilsTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static DataTableSpec spec(final String... names) {
		DataColumnSpec[] columns = new DataColumnSpec[names.length];
		for (int i = 0; i < names.length; i++) {
			columns[i] = new DataColumnSpecCreator(names[i], StringCell.TYPE).createSpec();
		}
		return new DataTableSpec(columns);
	}

	@Test
	public void checkFileNameRejectsEmptyName() {
		assertEquals("No filename given.", KnimenodeUtils.checkFileName(null));
		assertEquals("No filename given.", KnimenodeUtils.checkFileName(""));
	}

	@Test
	public void checkFileNameAcceptsURLs() {
		assertNull(KnimenodeUtils.checkFileName("http://example.com/data.csv"));
		assertNull(KnimenodeUtils.checkFileName("file:///does/not/matter"));
	}

	@Test
	public void checkFileNameAcceptsExistingFile() throws IOException {
		File f = tmp.newFile("input.txt");
		assertNull(KnimenodeUtils.checkFileName(f.getAbsolutePath()));
	}

	@Test
	public void checkFileNameRejectsMissingFileAndDirectories() throws IOException {
		File missing = new File(tmp.getRoot(), "missing.txt");
		String msg = KnimenodeUtils.checkFileName(missing.getAbsolutePath());
		assertNotNull(msg);
		assertTrue(msg, msg.contains("does not exist"));

		msg = KnimenodeUtils.checkFileName(tmp.getRoot().getAbsolutePath());
		assertNotNull(msg);
		assertTrue(msg, msg.contains("is not a file"));
	}

	@Test
	public void clearFolderDeletesFilesButKeepsSubfolders() throws IOException {
		File a = tmp.newFile("a.txt");
		File b = tmp.newFile("b.txt");
		File sub = tmp.newFolder("sub");
		KnimenodeUtils.clearFolder(tmp.getRoot().getAbsolutePath());
		assertTrue(!a.exists());
		assertTrue(!b.exists());
		assertTrue(sub.isDirectory());
	}

	@Test
	public void createColumnNameReturnsStartNameWhenUnique() {
		assertEquals("Result", KnimenodeUtils.createColumnName("Result", spec("A", "B")));
	}

	@Test
	public void createColumnNameAppendsCounterOnClash() {
		assertEquals("Result 2", KnimenodeUtils.createColumnName("Result", spec("Result")));
		assertEquals("Result 3", KnimenodeUtils.createColumnName("Result", spec("Result", "Result 2")));
	}

	@Test
	public void createColumnNameIncrementsExistingNumericSuffix() {
		assertEquals("Col12", KnimenodeUtils.createColumnName("Col11", spec("Col11")));
		assertEquals("Col13", KnimenodeUtils.createColumnName("Col11", spec("Col11", "Col12")));
	}
}
