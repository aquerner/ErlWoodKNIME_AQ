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
package org.erlwood.knime.utils.settings;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WebServiceSettingsOptionsTest {

	/** Subclass used to check the factory class refuses to be instantiated. */
	private static class Instantiated extends WebServiceSettingsOptions {

	}

	@Test
	public void noneEnablesNothing() {
		int options = WebServiceSettingsOptions.NONE;

		assertFalse(WebServiceSettingsOptions.useCredentials(options));
		assertFalse(WebServiceSettingsOptions.useOptionalCredentials(options));
		assertFalse(WebServiceSettingsOptions.useTimeOut(options));
		assertFalse(WebServiceSettingsOptions.useMaxChildElements(options));
	}

	@Test
	public void allEnablesCredentialsTimeOutAndMaxChildElements() {
		int options = WebServiceSettingsOptions.ALL;

		assertTrue(WebServiceSettingsOptions.useCredentials(options));
		assertTrue(WebServiceSettingsOptions.useTimeOut(options));
		assertTrue(WebServiceSettingsOptions.useMaxChildElements(options));

		// credentials are mandatory rather than optional
		assertFalse(WebServiceSettingsOptions.useOptionalCredentials(options));
	}

	@Test
	public void restOptionsExcludeMaxChildElements() {
		int options = WebServiceSettingsOptions.REST;

		assertTrue(WebServiceSettingsOptions.useCredentials(options));
		assertTrue(WebServiceSettingsOptions.useTimeOut(options));
		assertFalse(WebServiceSettingsOptions.useMaxChildElements(options));
	}

	@Test
	public void soapOptionsMatchAllOptions() {
		assertTrue(WebServiceSettingsOptions.useCredentials(WebServiceSettingsOptions.SOAP));
		assertTrue(WebServiceSettingsOptions.useTimeOut(WebServiceSettingsOptions.SOAP));
		assertTrue(WebServiceSettingsOptions.useMaxChildElements(WebServiceSettingsOptions.SOAP));
	}

	@Test
	public void noCredentialsVariantsDisableCredentialsOnly() {
		int soap = WebServiceSettingsOptions.SOAP_NO_CREDENTIALS;
		assertFalse(WebServiceSettingsOptions.useCredentials(soap));
		assertTrue(WebServiceSettingsOptions.useTimeOut(soap));
		assertTrue(WebServiceSettingsOptions.useMaxChildElements(soap));

		int rest = WebServiceSettingsOptions.REST_NO_CREDENTIALS;
		assertFalse(WebServiceSettingsOptions.useCredentials(rest));
		assertTrue(WebServiceSettingsOptions.useTimeOut(rest));
		assertFalse(WebServiceSettingsOptions.useMaxChildElements(rest));
	}

	@Test
	public void optionalCredentialsAlsoCountAsCredentials() {
		int options = WebServiceSettingsOptions.USE_OPTIONAL_CREDENTIALS;

		assertTrue(WebServiceSettingsOptions.useCredentials(options));
		assertTrue(WebServiceSettingsOptions.useOptionalCredentials(options));
		assertFalse(WebServiceSettingsOptions.useTimeOut(options));
	}

	@Test
	public void mandatoryCredentialsAreNotReportedAsOptional() {
		int options = WebServiceSettingsOptions.USE_CREDENTIALS;

		assertTrue(WebServiceSettingsOptions.useCredentials(options));
		assertFalse(WebServiceSettingsOptions.useOptionalCredentials(options));
	}

	@Test
	public void eachFlagIsIndependentOfTheOthers() {
		assertTrue(WebServiceSettingsOptions.useTimeOut(WebServiceSettingsOptions.USE_TIME_OUT));
		assertFalse(WebServiceSettingsOptions.useMaxChildElements(
				WebServiceSettingsOptions.USE_TIME_OUT));

		assertTrue(WebServiceSettingsOptions.useMaxChildElements(
				WebServiceSettingsOptions.USE_MAX_CHILD_ELEMENTS));
		assertFalse(WebServiceSettingsOptions.useTimeOut(
				WebServiceSettingsOptions.USE_MAX_CHILD_ELEMENTS));
	}

	@Test
	public void unknownFlagsAreIgnored() {
		int options = WebServiceSettingsOptions.USE_TIME_OUT | 1024;

		assertTrue(WebServiceSettingsOptions.useTimeOut(options));
		assertFalse(WebServiceSettingsOptions.useCredentials(options));
		assertFalse(WebServiceSettingsOptions.useMaxChildElements(options));
	}

	@Test(expected = IllegalStateException.class)
	public void theOptionsHolderCannotBeInstantiated() {
		new Instantiated();
	}

}
