package org.erlwood.knime.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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

	@Test
	public void checkFileNameRejectsNullAndEmpty() {
		assertEquals("No filename given.", KnimenodeUtils.checkFileName(null));
		assertEquals("No filename given.", KnimenodeUtils.checkFileName(""));
	}

	@Test
	public void checkFileNameAcceptsUrl() {
		assertNull(KnimenodeUtils.checkFileName("http://example.org/data.sdf"));
	}

	@Test
	public void checkFileNameAcceptsExistingFile() throws IOException {
		File f = tmp.newFile("input.txt");
		assertNull(KnimenodeUtils.checkFileName(f.getAbsolutePath()));
	}

	@Test
	public void checkFileNameRejectsMissingFileAndDirectory() throws IOException {
		File missing = new File(tmp.getRoot(), "does-not-exist");
		String msg = KnimenodeUtils.checkFileName(missing.getAbsolutePath());
		assertNotNull(msg);
		assertTrue(msg.endsWith("does not exist."));

		String dirMsg = KnimenodeUtils.checkFileName(tmp.getRoot().getAbsolutePath());
		assertNotNull(dirMsg);
		assertTrue(dirMsg.endsWith("is not a file."));
	}

	@Test
	public void clearFolderDeletesFilesButKeepsSubdirectories() throws IOException {
		File a = tmp.newFile("a.txt");
		File b = tmp.newFile("b.txt");
		File sub = tmp.newFolder("sub");
		File inSub = new File(sub, "c.txt");
		Files.createFile(inSub.toPath());

		KnimenodeUtils.clearFolder(tmp.getRoot().getAbsolutePath());

		assertFalse(a.exists());
		assertFalse(b.exists());
		assertTrue(sub.isDirectory());
		assertTrue(inSub.exists());
	}

	@Test
	public void clearFolderIgnoresNonDirectories() throws IOException {
		File f = tmp.newFile("plain.txt");
		KnimenodeUtils.clearFolder(f.getAbsolutePath());
		assertTrue(f.exists());
	}

	@Test
	public void createColumnNameReturnsStartNameWhenUnique() {
		DataTableSpec spec = spec("A", "B");
		assertEquals("C", KnimenodeUtils.createColumnName("C", spec));
	}

	@Test
	public void createColumnNameAppendsCounterWhenNameHasNoNumber() {
		DataTableSpec spec = spec("Col", "Other");
		assertEquals("Col 2", KnimenodeUtils.createColumnName("Col", spec));
	}

	@Test
	public void createColumnNameIncrementsTrailingNumber() {
		DataTableSpec spec = spec("Col", "Col 2", "Col 3");
		assertEquals("Col 4", KnimenodeUtils.createColumnName("Col", spec));
		assertEquals("Col10", KnimenodeUtils.createColumnName("Col10", spec));
		DataTableSpec spec2 = spec("Col9");
		assertEquals("Col10", KnimenodeUtils.createColumnName("Col9", spec2));
	}

	private static DataTableSpec spec(final String... names) {
		DataColumnSpec[] cols = new DataColumnSpec[names.length];
		for (int i = 0; i < names.length; i++) {
			cols[i] = new DataColumnSpecCreator(names[i], StringCell.TYPE).createSpec();
		}
		return new DataTableSpec(cols);
	}
}
