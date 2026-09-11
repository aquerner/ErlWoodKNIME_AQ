package org.erlwood.knime.nodes.downloadfileswithauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.erlwood.knime.utils.auth.AuthenticationUtils;
import org.junit.Test;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettings;

public class DownloadFilesWithAuthSettingsTest {

	@Test
	public void defaultsMatchDialogExpectations() {
		DownloadFilesWithAuthSettings s = new DownloadFilesWithAuthSettings();
		assertEquals("", s.getUrlColumn());
		assertTrue(s.isDeleteTemp());
		assertFalse(s.isOutputDirectoryEnabled());
		assertEquals("", s.getOutputDirectory());
		assertTrue(s.isUseCredentials());
		assertEquals(AuthenticationUtils.getDefaultCredentialsName(), s.getCredentialsName());
	}

	@Test
	public void modelsAreBackedBySameInstances() {
		DownloadFilesWithAuthSettings s = new DownloadFilesWithAuthSettings();
		assertSame(s.getUrlColumnModel(), s.getUrlColumnModel());
		s.getUrlColumnModel().setStringValue("col");
		assertEquals("col", s.getUrlColumn());
		s.getDeleteTempModel().setBooleanValue(false);
		assertFalse(s.isDeleteTemp());
		s.getOutputDirectoryEnabledModel().setBooleanValue(true);
		assertTrue(s.isOutputDirectoryEnabled());
		s.getOutputDirectoryModel().setStringValue("/out");
		assertEquals("/out", s.getOutputDirectory());
	}

	@Test
	public void settingsKeysAreStable() throws InvalidSettingsException {
		DownloadFilesWithAuthSettings s = new DownloadFilesWithAuthSettings();
		s.getUrlColumnModel().setStringValue("u");
		s.getDeleteTempModel().setBooleanValue(false);
		s.getOutputDirectoryEnabledModel().setBooleanValue(true);
		s.getOutputDirectoryModel().setStringValue("d");
		NodeSettings ns = new NodeSettings("t");
		s.saveSettingsTo(ns);
		assertEquals("u", ns.getString("urlCol"));
		assertFalse(ns.getBoolean("deleteTemp"));
		assertTrue(ns.getBoolean("outputDirectoryEnabled"));
		assertEquals("d", ns.getString("outputDirectory"));
	}

	@Test
	public void saveThenLoadRoundTripsAllValues() throws InvalidSettingsException {
		DownloadFilesWithAuthSettings s = new DownloadFilesWithAuthSettings();
		s.getUrlColumnModel().setStringValue("url");
		s.getDeleteTempModel().setBooleanValue(false);
		s.getOutputDirectoryEnabledModel().setBooleanValue(true);
		s.getOutputDirectoryModel().setStringValue("/tmp/out");
		s.setUseCredentials(false);
		s.setCredentialsName("myCreds");

		NodeSettings ns = new NodeSettings("t");
		s.saveSettingsTo(ns);
		assertEquals("myCreds", ns.getString("credentialsName"));
		assertFalse(ns.getBoolean("useCredentials"));

		DownloadFilesWithAuthSettings loaded = new DownloadFilesWithAuthSettings();
		loaded.loadSettingsFrom(ns);
		assertEquals("url", loaded.getUrlColumn());
		assertFalse(loaded.isDeleteTemp());
		assertTrue(loaded.isOutputDirectoryEnabled());
		assertEquals("/tmp/out", loaded.getOutputDirectory());
		assertFalse(loaded.isUseCredentials());
		assertEquals("myCreds", loaded.getCredentialsName());
	}

	@Test
	public void loadFallsBackToDefaultsForOldWorkflowsWithoutOutputDirectory() throws InvalidSettingsException {
		NodeSettings ns = new NodeSettings("old");
		ns.addString("urlCol", "legacyCol");
		ns.addBoolean("deleteTemp", false);

		DownloadFilesWithAuthSettings s = new DownloadFilesWithAuthSettings();
		s.getOutputDirectoryEnabledModel().setBooleanValue(true);
		s.getOutputDirectoryModel().setStringValue("stale");
		s.loadSettingsFrom(ns);

		assertEquals("legacyCol", s.getUrlColumn());
		assertFalse(s.isDeleteTemp());
		assertFalse(s.isOutputDirectoryEnabled());
		assertEquals("", s.getOutputDirectory());
		assertTrue(s.isUseCredentials());
		assertEquals(AuthenticationUtils.getDefaultCredentialsName(), s.getCredentialsName());
	}

	@Test
	public void loadFailsWhenMandatoryUrlColumnMissing() {
		NodeSettings ns = new NodeSettings("bad");
		ns.addBoolean("deleteTemp", true);
		try {
			new DownloadFilesWithAuthSettings().loadSettingsFrom(ns);
			fail("expected InvalidSettingsException");
		} catch (InvalidSettingsException expected) {
			// ok
		}
	}

	@Test
	public void validateAcceptsOldWorkflowsButRejectsMissingMandatoryKeys() {
		NodeSettings old = new NodeSettings("old");
		old.addString("urlCol", "c");
		old.addBoolean("deleteTemp", true);
		try {
			new DownloadFilesWithAuthSettings().validateSettings(old);
		} catch (InvalidSettingsException e) {
			fail("old workflows without output directory must validate: " + e.getMessage());
		}

		NodeSettings noDelete = new NodeSettings("bad");
		noDelete.addString("urlCol", "c");
		try {
			new DownloadFilesWithAuthSettings().validateSettings(noDelete);
			fail("expected InvalidSettingsException");
		} catch (InvalidSettingsException expected) {
			// ok
		}
	}

	@Test
	public void validateIgnoresWrongTypeForOptionalOutputDirectoryKeys() {
		NodeSettings ns = new NodeSettings("bad");
		ns.addString("urlCol", "c");
		ns.addBoolean("deleteTemp", true);
		ns.addString("outputDirectoryEnabled", "notABoolean");
		try {
			new DownloadFilesWithAuthSettings().validateSettings(ns);
		} catch (InvalidSettingsException e) {
			fail("type mismatch on optional keys is deliberately ignored: " + e.getMessage());
		}
	}

	@Test
	public void credentialsNameMatchingDefaultIsStoredAsPlaceholder() throws InvalidSettingsException {
		DownloadFilesWithAuthSettings s = new DownloadFilesWithAuthSettings();
		s.setCredentialsName(AuthenticationUtils.getDefaultCredentialsName());
		NodeSettings ns = new NodeSettings("t");
		s.saveSettingsTo(ns);
		assertEquals("#default#", ns.getString("credentialsName"));
		assertEquals(AuthenticationUtils.getDefaultCredentialsName(), s.getCredentialsName());
	}

	@Test
	public void explicitCredentialsNameIsReturnedVerbatim() {
		DownloadFilesWithAuthSettings s = new DownloadFilesWithAuthSettings();
		s.setCredentialsName("explicit");
		assertEquals("explicit", s.getCredentialsName());
		s.setUseCredentials(false);
		assertEquals("explicit", s.getCredentialsName());
	}

	@Test
	public void nullCredentialsNameFallsBackToDefaultOnlyWhenUsingCredentials() {
		DownloadFilesWithAuthSettings s = new DownloadFilesWithAuthSettings();
		s.setUseCredentials(true);
		s.setCredentialsName(null);
		assertEquals(AuthenticationUtils.getDefaultCredentialsName(), s.getCredentialsName());

		s.setUseCredentials(false);
		assertEquals(null, s.getCredentialsName());
	}

	@Test
	public void loadWithoutAuthKeysUsesDefaultCredentials() throws InvalidSettingsException {
		NodeSettings ns = new NodeSettings("t");
		ns.addString("urlCol", "c");
		ns.addBoolean("deleteTemp", true);
		DownloadFilesWithAuthSettings s = new DownloadFilesWithAuthSettings();
		s.setUseCredentials(false);
		s.setCredentialsName("other");
		s.loadSettingsFrom(ns);
		assertTrue(s.isUseCredentials());
		assertEquals(AuthenticationUtils.getDefaultCredentialsName(), s.getCredentialsName());
	}
}
