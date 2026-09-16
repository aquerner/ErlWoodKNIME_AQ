package org.erlwood.knime.utils.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SambaUtilityTest {

	@Test
	public void fixSambaPathStripsLeadingAndTrailingSlashesAndCollapsesDoubles() {
		assertEquals("share/folder/file.txt", SambaUtility.fixSambaPath("//share//folder///file.txt/"));
		assertEquals("share/folder", SambaUtility.fixSambaPath("share/folder"));
		assertEquals("", SambaUtility.fixSambaPath("/"));
		assertEquals("", SambaUtility.fixSambaPath(""));
	}

	@Test
	public void makeURLConvertsFileProtocolPath() {
		assertEquals("smb://server/share/dir", SambaUtility.makeURL("file:////server/share/dir", false));
		assertEquals("smb://server/share/dir/", SambaUtility.makeURL("file:////server/share/dir", true));
	}

	@Test
	public void makeURLConvertsUNCPath() {
		assertEquals("smb://server/share/dir", SambaUtility.makeURL("\\\\server\\share\\dir", false));
		assertEquals("smb://server/share/dir/", SambaUtility.makeURL("\\\\server\\share\\dir", true));
	}

	@Test
	public void makeURLDoesNotDuplicateTrailingSlash() {
		assertEquals("smb://server/share/", SambaUtility.makeURL("smb://server/share/", true));
	}

	@Test
	public void makeURLLeavesOtherPathsUntouched() {
		assertEquals("http://example.com/x", SambaUtility.makeURL("http://example.com/x", true));
		assertEquals("C:\\data\\file.txt", SambaUtility.makeURL("C:\\data\\file.txt", true));
		assertEquals("/usr/local", SambaUtility.makeURL("/usr/local", true));
	}

	@Test
	public void isSambaURL() {
		assertTrue(SambaUtility.isSambaURL("smb://server/share"));
		assertFalse(SambaUtility.isSambaURL("SMB://server/share"));
		assertFalse(SambaUtility.isSambaURL("file:////server/share"));
		assertFalse(SambaUtility.isSambaURL(""));
	}

	@Test
	public void isUNC() {
		assertTrue(SambaUtility.isUNC("\\\\server\\share"));
		assertFalse(SambaUtility.isUNC("\\server\\share"));
		assertFalse(SambaUtility.isUNC("//server/share"));
		assertFalse(SambaUtility.isUNC(""));
	}
}
