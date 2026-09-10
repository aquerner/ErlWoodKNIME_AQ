/*
 * ------------------------------------------------------------------------
 *
 * Copyright (C) 2014 Eli Lilly and Company Limited
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * ------------------------------------------------------------------------
*/
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
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.StringCell;

public class KnimenodeUtilsTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void checkFileNameRejectsNullAndEmptyNames() {
		assertEquals("No filename given.", KnimenodeUtils.checkFileName(null));
		assertEquals("No filename given.", KnimenodeUtils.checkFileName(""));
	}

	@Test
	public void checkFileNameAcceptsUrls() {
		assertNull(KnimenodeUtils.checkFileName("http://example.org/data.csv"));
	}

	@Test
	public void checkFileNameAcceptsExistingFile() throws IOException {
		File file = folder.newFile("data.csv");
		assertNull(KnimenodeUtils.checkFileName(file.getAbsolutePath()));
	}

	@Test
	public void checkFileNameReportsMissingFile() {
		File missing = new File(folder.getRoot(), "missing.csv");
		String message = KnimenodeUtils.checkFileName(missing.getAbsolutePath());
		assertNotNull(message);
		assertTrue(message.endsWith("does not exist."));
	}

	@Test
	public void checkFileNameReportsDirectory() throws IOException {
		File directory = folder.newFolder("adirectory");
		String message = KnimenodeUtils.checkFileName(directory.getAbsolutePath());
		assertNotNull(message);
		assertTrue(message.endsWith("is not a file."));
	}

	@Test
	public void clearFolderDeletesFilesButKeepsSubdirectories() throws IOException {
		File file = folder.newFile("delete-me.txt");
		File subdirectory = folder.newFolder("keep-me");
		File nested = new File(subdirectory, "nested.txt");
		Files.createFile(nested.toPath());

		KnimenodeUtils.clearFolder(folder.getRoot().getAbsolutePath());

		assertFalse(file.exists());
		assertTrue(subdirectory.isDirectory());
		assertTrue(nested.exists());
	}

	@Test
	public void clearFolderIgnoresNonDirectories() throws IOException {
		File file = folder.newFile("not-a-folder.txt");
		KnimenodeUtils.clearFolder(file.getAbsolutePath());
		assertTrue(file.exists());
	}

	@Test
	public void createColumnNameReturnsStartNameWhenUnique() {
		assertEquals("score", KnimenodeUtils.createColumnName("score", spec("smiles")));
	}

	@Test
	public void createColumnNameAppendsSuffixOnCollision() {
		assertEquals("score 2", KnimenodeUtils.createColumnName("score", spec("score")));
	}

	@Test
	public void createColumnNameIncrementsExistingSuffix() {
		assertEquals("score 3", KnimenodeUtils.createColumnName("score", spec("score", "score 2")));
	}

	private static DataTableSpec spec(final String... columnNames) {
		DataType[] types = new DataType[columnNames.length];
		for (int i = 0; i < types.length; i++) {
			types[i] = StringCell.TYPE;
		}
		return new DataTableSpec(columnNames, types);
	}

}
