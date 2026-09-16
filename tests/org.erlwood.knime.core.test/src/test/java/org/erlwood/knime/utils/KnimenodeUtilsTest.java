package org.erlwood.knime.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.StringCell;

public class KnimenodeUtilsTest {

	private static DataTableSpec spec(final String... names) {
		DataColumnSpec[] specs = new DataColumnSpec[names.length];
		for (int i = 0; i < names.length; i++) {
			specs[i] = new DataColumnSpecCreator(names[i], StringCell.TYPE).createSpec();
		}
		return new DataTableSpec(specs);
	}

	@Test
	public void checkFileNameRejectsNullAndEmpty() {
		assertEquals("No filename given.", KnimenodeUtils.checkFileName(null));
		assertEquals("No filename given.", KnimenodeUtils.checkFileName(""));
	}

	@Test
	public void checkFileNameAcceptsUrls() {
		assertNull(KnimenodeUtils.checkFileName("http://example.com/data.csv"));
		assertNull(KnimenodeUtils.checkFileName("file:///tmp/does-not-need-to-exist.txt"));
	}

	@Test
	public void checkFileNameValidatesLocalFiles() throws IOException {
		File tmp = Files.createTempFile("knimenodeutils", ".txt").toFile();
		try {
			assertNull(KnimenodeUtils.checkFileName(tmp.getAbsolutePath()));
			assertTrue(KnimenodeUtils.checkFileName(tmp.getParent()).endsWith("is not a file."));
			File missing = new File(tmp.getParentFile(), "missing-" + System.nanoTime());
			assertTrue(KnimenodeUtils.checkFileName(missing.getAbsolutePath()).endsWith("does not exist."));
		} finally {
			tmp.delete();
		}
	}

	@Test
	public void clearFolderDeletesFilesButKeepsSubdirectories() throws IOException {
		File dir = Files.createTempDirectory("knimenodeutils").toFile();
		File a = new File(dir, "a.txt");
		File sub = new File(dir, "sub");
		try {
			assertTrue(a.createNewFile());
			assertTrue(sub.mkdir());
			KnimenodeUtils.clearFolder(dir.getAbsolutePath());
			assertTrue(!a.exists());
			assertTrue(sub.isDirectory());
		} finally {
			sub.delete();
			a.delete();
			dir.delete();
		}
	}

	@Test
	public void clearFolderIgnoresNonDirectories() {
		KnimenodeUtils.clearFolder(new File("does-not-exist-" + System.nanoTime()).getAbsolutePath());
	}

	@Test
	public void createColumnNameReturnsStartNameWhenUnique() {
		assertEquals("col", KnimenodeUtils.createColumnName("col", spec("other")));
	}

	@Test
	public void createColumnNameAppendsSuffixWhenTaken() {
		assertEquals("col 2", KnimenodeUtils.createColumnName("col", spec("col")));
	}

	@Test
	public void createColumnNameIncrementsExistingNumericSuffix() {
		assertEquals("col 3", KnimenodeUtils.createColumnName("col 2", spec("col 2")));
		assertEquals("col10", KnimenodeUtils.createColumnName("col9", spec("col9")));
		assertEquals("col 4", KnimenodeUtils.createColumnName("col", spec("col", "col 2", "col 3")));
	}

	@Test
	public void createColumnNameHandlesEmptySpec() {
		assertNotNull(KnimenodeUtils.createColumnName("x", new DataTableSpec()));
	}
}
