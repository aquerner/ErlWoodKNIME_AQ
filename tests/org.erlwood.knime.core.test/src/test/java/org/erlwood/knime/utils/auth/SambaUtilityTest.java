package org.erlwood.knime.utils.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;

import org.junit.Test;

import jcifs.smb.SmbFile;

public class SambaUtilityTest {

	@Test
	public void fixSambaPathStripsLeadingSlashes() {
		assertEquals("server/share", SambaUtility.fixSambaPath("/server/share"));
		assertEquals("server/share", SambaUtility.fixSambaPath("///server/share"));
	}

	@Test
	public void fixSambaPathCollapsesRepeatedSlashes() {
		assertEquals("server/share/dir", SambaUtility.fixSambaPath("server//share///dir"));
	}

	@Test
	public void fixSambaPathStripsSingleTrailingSlash() {
		assertEquals("server/share", SambaUtility.fixSambaPath("server/share/"));
		assertEquals("server/share", SambaUtility.fixSambaPath("server/share//"));
	}

	@Test
	public void fixSambaPathLeavesCleanPathAlone() {
		assertEquals("server/share/dir/file.txt", SambaUtility.fixSambaPath("server/share/dir/file.txt"));
		assertEquals("", SambaUtility.fixSambaPath(""));
		assertEquals("", SambaUtility.fixSambaPath("/"));
		assertEquals("", SambaUtility.fixSambaPath("///"));
	}

	@Test
	public void fixSambaPathDoesNotTouchBackslashes() {
		assertEquals("server\\\\share", SambaUtility.fixSambaPath("server\\\\share"));
	}

	@Test
	public void makeURLConvertsQuadrupleSlashFileUrlToSmb() {
		assertEquals("smb://server/share/dir", SambaUtility.makeURL("file:////server/share/dir", false));
		assertEquals("smb://server/share/dir/", SambaUtility.makeURL("file:////server/share/dir", true));
	}

	@Test
	public void makeURLConvertsUncPathToSmb() {
		assertEquals("smb://server/share/dir", SambaUtility.makeURL("\\\\server\\share\\dir", false));
		assertEquals("smb://server/share/dir/", SambaUtility.makeURL("\\\\server\\share\\dir", true));
	}

	@Test
	public void makeURLKeepsExistingTrailingSlash() {
		assertEquals("smb://server/share/", SambaUtility.makeURL("smb://server/share/", true));
		assertEquals("smb://server/share/", SambaUtility.makeURL("\\\\server\\share\\", true));
	}

	@Test
	public void makeURLLeavesLocalPathsAndOrdinaryFileUrlsAlone() {
		assertEquals("file:///C:/data", SambaUtility.makeURL("file:///C:/data", true));
		assertEquals("/tmp/data", SambaUtility.makeURL("/tmp/data", true));
		assertEquals("C:\\data", SambaUtility.makeURL("C:\\data", true));
		assertEquals("http://example.org/x", SambaUtility.makeURL("http://example.org/x", true));
		assertEquals("", SambaUtility.makeURL("", true));
	}

	@Test
	public void makeURLOnlyAppendsSlashToSmbUrls() {
		assertEquals("smb://server/share/", SambaUtility.makeURL("smb://server/share", true));
		assertEquals("smb://server/share", SambaUtility.makeURL("smb://server/share", false));
		assertEquals("/tmp/data", SambaUtility.makeURL("/tmp/data", true));
	}

	@Test
	public void makeURLIsCaseSensitiveAboutScheme() {
		assertEquals("FILE:////server/share", SambaUtility.makeURL("FILE:////server/share", false));
	}

	@Test
	public void isSambaURLRequiresSmbScheme() {
		assertTrue(SambaUtility.isSambaURL("smb://server/share"));
		assertFalse(SambaUtility.isSambaURL("SMB://server/share"));
		assertFalse(SambaUtility.isSambaURL("file:////server/share"));
		assertFalse(SambaUtility.isSambaURL("\\\\server\\share"));
		assertFalse(SambaUtility.isSambaURL(""));
	}

	@Test
	public void isUNCRequiresLeadingDoubleBackslash() {
		assertTrue(SambaUtility.isUNC("\\\\server\\share"));
		assertFalse(SambaUtility.isUNC("\\server\\share"));
		assertFalse(SambaUtility.isUNC("//server/share"));
		assertFalse(SambaUtility.isUNC("smb://server/share"));
		assertFalse(SambaUtility.isUNC(""));
	}

	@Test
	public void stripServerRemovesServerAndSharePrefix() throws MalformedURLException {
		SmbFile file = new SmbFile("smb://server/share/dir/file.txt");
		assertEquals("dir/file.txt", SambaUtility.stripServer(file));
	}

	@Test
	public void stripServerOfShareRootIsEmpty() throws MalformedURLException {
		SmbFile file = new SmbFile("smb://server/share/");
		assertEquals("", SambaUtility.stripServer(file));
	}
}
