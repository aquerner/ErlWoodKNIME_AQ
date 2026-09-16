package org.erlwood.knime.utils.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SambaUtilityTest {

	@Test
	public void fixSambaPathStripsLeadingAndTrailingSlashesAndCollapsesDoubles() {
		assertEquals("share/folder/file.txt", SambaUtility.fixSambaPath("/share//folder/file.txt/"));
		assertEquals("a/b", SambaUtility.fixSambaPath("a/b"));
		assertEquals("", SambaUtility.fixSambaPath("/"));
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
	public void makeURLLeavesOtherPathsUntouched() {
		assertEquals("C:\\data\\file.txt", SambaUtility.makeURL("C:\\data\\file.txt", true));
		assertEquals("http://example.com", SambaUtility.makeURL("http://example.com", true));
		assertEquals("smb://server/share/", SambaUtility.makeURL("smb://server/share/", true));
	}

	@Test
	public void isSambaURLAndIsUNC() {
		assertTrue(SambaUtility.isSambaURL("smb://server/share"));
		assertFalse(SambaUtility.isSambaURL("http://server/share"));
		assertTrue(SambaUtility.isUNC("\\\\server\\share"));
		assertFalse(SambaUtility.isUNC("/server/share"));
	}
}
