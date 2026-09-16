package org.erlwood.knime.utils.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SambaUtilityTest {

	@Test
	public void fixSambaPathStripsLeadingTrailingAndDoubleSlashes() {
		assertEquals("share/folder/file.txt", SambaUtility.fixSambaPath("//share//folder///file.txt/"));
		assertEquals("share", SambaUtility.fixSambaPath("/share"));
		assertEquals("a/b", SambaUtility.fixSambaPath("a/b"));
		assertEquals("", SambaUtility.fixSambaPath("/"));
	}

	@Test
	public void makeURLConvertsFileURLToSamba() {
		assertEquals("smb://server/share/", SambaUtility.makeURL("file:////server/share", true));
		assertEquals("smb://server/share", SambaUtility.makeURL("file:////server/share", false));
	}

	@Test
	public void makeURLConvertsUNCToSamba() {
		assertEquals("smb://server/share/dir/", SambaUtility.makeURL("\\\\server\\share\\dir", true));
		assertEquals("smb://server/share/dir", SambaUtility.makeURL("\\\\server\\share\\dir", false));
	}

	@Test
	public void makeURLDoesNotDuplicateTrailingSlash() {
		assertEquals("smb://server/share/", SambaUtility.makeURL("smb://server/share/", true));
	}

	@Test
	public void makeURLLeavesNonSambaPathsAlone() {
		assertEquals("http://example.com/x", SambaUtility.makeURL("http://example.com/x", true));
		assertEquals("/local/path", SambaUtility.makeURL("/local/path", true));
	}

	@Test
	public void isSambaURLAndIsUNC() {
		assertTrue(SambaUtility.isSambaURL("smb://server/share"));
		assertFalse(SambaUtility.isSambaURL("http://server/share"));
		assertTrue(SambaUtility.isUNC("\\\\server\\share"));
		assertFalse(SambaUtility.isUNC("C:\\share"));
		assertFalse(SambaUtility.isUNC("smb://server/share"));
	}
}
