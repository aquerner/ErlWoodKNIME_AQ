package org.erlwood.knime.nodes.xlsenumeratenamedranges;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettings;

public class XLSEnumerateNamedRangesSettingsTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void fileLocationDefaultsToNull() {
		assertNull(new XLSEnumerateNamedRangesSettings().getFileLocation());
	}

	@Test
	public void saveAndLoadRoundTripsFileLocation() throws InvalidSettingsException {
		XLSEnumerateNamedRangesSettings s = new XLSEnumerateNamedRangesSettings();
		s.setFileLocation("/data/book.xlsx");
		NodeSettings ns = new NodeSettings("t");
		s.save(ns);
		assertEquals("/data/book.xlsx", ns.getString("XLS_LOCATION"));

		XLSEnumerateNamedRangesSettings loaded = XLSEnumerateNamedRangesSettings.load(ns);
		assertEquals("/data/book.xlsx", loaded.getFileLocation());
	}

	@Test
	public void saveAndLoadRoundTripsNullLocation() throws InvalidSettingsException {
		NodeSettings ns = new NodeSettings("t");
		new XLSEnumerateNamedRangesSettings().save(ns);
		assertNull(XLSEnumerateNamedRangesSettings.load(ns).getFileLocation());
	}

	@Test
	public void loadFailsWhenKeyMissing() {
		try {
			XLSEnumerateNamedRangesSettings.load(new NodeSettings("empty"));
			fail("expected InvalidSettingsException");
		} catch (InvalidSettingsException expected) {
			// ok
		}
	}

	@Test
	public void statusReportsMissingLocationRegardlessOfExistenceCheck() {
		XLSEnumerateNamedRangesSettings s = new XLSEnumerateNamedRangesSettings();
		assertEquals("No file location specified", s.getStatus(false));
		assertEquals("No file location specified", s.getStatus(true));
		s.setFileLocation("");
		assertEquals("No file location specified", s.getStatus(false));
		assertEquals("No file location specified", s.getStatus(true));
	}

	@Test
	public void statusIsOkWithoutExistenceCheckForAnyNonEmptyLocation() {
		XLSEnumerateNamedRangesSettings s = new XLSEnumerateNamedRangesSettings();
		s.setFileLocation("/definitely/not/here.xlsx");
		assertNull(s.getStatus(false));
	}

	@Test
	public void statusIsOkForExistingReadableFile() throws IOException {
		File f = folder.newFile("book.xlsx");
		XLSEnumerateNamedRangesSettings s = new XLSEnumerateNamedRangesSettings();
		s.setFileLocation(f.getAbsolutePath());
		assertNull(s.getStatus(true));
	}

	@Test
	public void statusIsOkForExistingFileUrl() throws IOException {
		File f = folder.newFile("book.xlsx");
		XLSEnumerateNamedRangesSettings s = new XLSEnumerateNamedRangesSettings();
		s.setFileLocation(f.toURI().toURL().toString());
		assertNull(s.getStatus(true));
	}

	@Test
	public void statusReportsMissingFile() {
		File f = new File(folder.getRoot(), "missing.xlsx");
		XLSEnumerateNamedRangesSettings s = new XLSEnumerateNamedRangesSettings();
		s.setFileLocation(f.getAbsolutePath());
		assertEquals("Specified file doesn't exist (" + f.getAbsolutePath() + ")", s.getStatus(true));
	}

	@Test
	public void statusReportsDirectoryIsNotAFile() throws IOException {
		File dir = folder.newFolder("sub");
		XLSEnumerateNamedRangesSettings s = new XLSEnumerateNamedRangesSettings();
		s.setFileLocation(dir.getAbsolutePath());
		assertEquals("Specified location is not a file (" + dir.getAbsolutePath() + ")", s.getStatus(true));
	}

	@Test
	public void statusReportsUnopenableUrl() {
		String url = new File(folder.getRoot(), "missing.xlsx").toURI().toString();
		XLSEnumerateNamedRangesSettings s = new XLSEnumerateNamedRangesSettings();
		s.setFileLocation(url);
		String status = s.getStatus(true);
		assertTrue(String.valueOf(status), status != null && status.startsWith("Can't open specified location ("));
		assertTrue(status.contains(url));
	}
}
