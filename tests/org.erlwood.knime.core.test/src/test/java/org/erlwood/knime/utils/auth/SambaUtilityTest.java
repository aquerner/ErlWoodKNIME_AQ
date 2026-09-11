package org.erlwood.knime.utils.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;

import org.junit.Test;

import jcifs.smb.SmbFile;

public class SambaUtilityTest {

	@Test
	public void fixSambaPathStripsLeadingTrailingAndDoubleSlashes() {
		assertEquals("share/folder/file.txt", SambaUtility.fixSambaPath("///share//folder///file.txt/"));
		assertEquals("share/folder", SambaUtility.fixSambaPath("share/folder"));
		assertEquals("", SambaUtility.fixSambaPath("/"));
	}

	@Test
	public void makeURLConvertsUncPath() {
		assertEquals("smb://server/share/folder", SambaUtility.makeURL("\\\\server\\share\\folder", false));
		assertEquals("smb://server/share/folder/", SambaUtility.makeURL("\\\\server\\share\\folder", true));
	}

	@Test
	public void makeURLConvertsFileProtocolPath() {
		assertEquals("smb://server/share", SambaUtility.makeURL("file:////server/share", false));
		assertEquals("smb://server/share/", SambaUtility.makeURL("file:////server/share", true));
	}

	@Test
	public void makeURLLeavesOtherPathsUntouched() {
		assertEquals("http://server/share", SambaUtility.makeURL("http://server/share", true));
		assertEquals("/local/path", SambaUtility.makeURL("/local/path", true));
		assertEquals("smb://server/share/", SambaUtility.makeURL("smb://server/share/", true));
	}

	@Test
	public void isSambaURLAndIsUNC() {
		assertTrue(SambaUtility.isSambaURL("smb://server/share"));
		assertFalse(SambaUtility.isSambaURL("\\\\server\\share"));
		assertTrue(SambaUtility.isUNC("\\\\server\\share"));
		assertFalse(SambaUtility.isUNC("smb://server/share"));
	}

	@Test
	public void stripServerRemovesServerAndShare() throws MalformedURLException {
		SmbFile file = new SmbFile("smb://server/share/folder/file.txt");
		assertEquals("folder/file.txt", SambaUtility.stripServer(file));
	}
}
