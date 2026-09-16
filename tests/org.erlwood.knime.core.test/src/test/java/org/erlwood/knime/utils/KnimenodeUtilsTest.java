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

	private static DataTableSpec spec(final String... names) {
		DataColumnSpec[] cols = new DataColumnSpec[names.length];
		for (int i = 0; i < names.length; i++) {
			cols[i] = new DataColumnSpecCreator(names[i], StringCell.TYPE).createSpec();
		}
		return new DataTableSpec(cols);
	}

	@Test
	public void checkFileNameRejectsNullOrEmpty() {
		assertEquals("No filename given.", KnimenodeUtils.checkFileName(null));
		assertEquals("No filename given.", KnimenodeUtils.checkFileName(""));
	}

	@Test
	public void checkFileNameAcceptsURLsWithoutTouchingFileSystem() {
		assertNull(KnimenodeUtils.checkFileName("http://example.com/does/not/exist.txt"));
		assertNull(KnimenodeUtils.checkFileName("file:///definitely/not/here.txt"));
	}

	@Test
	public void checkFileNameAcceptsExistingFile() throws IOException {
		File f = tmp.newFile("data.txt");
		assertNull(KnimenodeUtils.checkFileName(f.getAbsolutePath()));
	}

	@Test
	public void checkFileNameReportsMissingFile() {
		File f = new File(tmp.getRoot(), "missing.txt");
		String msg = KnimenodeUtils.checkFileName(f.getAbsolutePath());
		assertNotNull(msg);
		assertTrue(msg, msg.contains("does not exist"));
		assertTrue(msg, msg.contains(f.getAbsolutePath()));
	}

	@Test
	public void checkFileNameReportsDirectory() throws IOException {
		File dir = tmp.newFolder("dir");
		String msg = KnimenodeUtils.checkFileName(dir.getAbsolutePath());
		assertNotNull(msg);
		assertTrue(msg, msg.contains("is not a file"));
	}

	@Test
	public void clearFolderDeletesFilesButKeepsSubdirectories() throws IOException {
		File root = tmp.newFolder("root");
		File a = new File(root, "a.txt");
		File b = new File(root, "b.txt");
		File sub = new File(root, "sub");
		Files.createFile(a.toPath());
		Files.createFile(b.toPath());
		assertTrue(sub.mkdir());
		File inSub = new File(sub, "c.txt");
		Files.createFile(inSub.toPath());

		KnimenodeUtils.clearFolder(root.getAbsolutePath());

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
		KnimenodeUtils.clearFolder(new File(tmp.getRoot(), "nope").getAbsolutePath());
	}

	@Test
	public void createColumnNameReturnsStartNameWhenUnique() {
		assertEquals("Col", KnimenodeUtils.createColumnName("Col", spec("A", "B")));
		assertEquals("Col", KnimenodeUtils.createColumnName("Col", new DataTableSpec()));
	}

	@Test
	public void createColumnNameAppendsSuffixWhenNameHasNoNumber() {
		assertEquals("Col 2", KnimenodeUtils.createColumnName("Col", spec("Col")));
		assertEquals("Col 3", KnimenodeUtils.createColumnName("Col", spec("Col", "Col 2")));
	}

	@Test
	public void createColumnNameIncrementsTrailingNumber() {
		assertEquals("Col3", KnimenodeUtils.createColumnName("Col2", spec("Col2")));
		assertEquals("Col10", KnimenodeUtils.createColumnName("Col9", spec("Col9")));
		assertEquals("Col12", KnimenodeUtils.createColumnName("Col10", spec("Col10", "Col11")));
	}
}
