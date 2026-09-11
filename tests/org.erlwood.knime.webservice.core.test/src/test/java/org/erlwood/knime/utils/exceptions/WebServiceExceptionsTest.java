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
package org.erlwood.knime.utils.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WebServiceExceptionsTest {

	private static final String OS_NAME = "os.name";

	private static final String BASE_MESSAGE =
			"User is not authorised to access this resource, please check username and password.";

	private static final String LINUX_MESSAGE =
			"Linux users must set workflow credentials for authenticated web services in KNIME.";

	private String originalOsName;

	@Before
	public void rememberOsName() {
		originalOsName = System.getProperty(OS_NAME);
	}

	@After
	public void restoreOsName() {
		if(originalOsName == null) {
			System.clearProperty(OS_NAME);
		} else {
			System.setProperty(OS_NAME, originalOsName);
		}
	}

	@Test
	public void webServiceExceptionKeepsItsMessageAndCause() {
		IOException cause = new IOException("connection reset");

		WebServiceException withoutCause = new WebServiceException("service unavailable");
		assertEquals("service unavailable", withoutCause.getMessage());
		assertNull(withoutCause.getCause());

		WebServiceException withCause = new WebServiceException("service unavailable", cause);
		assertEquals("service unavailable", withCause.getMessage());
		assertSame(cause, withCause.getCause());
	}

	@Test
	public void webServiceExceptionIsACheckedException() {
		assertTrue(Exception.class.isAssignableFrom(WebServiceException.class));
		assertFalse(RuntimeException.class.isAssignableFrom(WebServiceException.class));
	}

	@Test
	public void notFoundExceptionHasAFixedMessageAndIsAWebServiceException() {
		NotFoundException e = new NotFoundException();

		assertEquals("Requested resource has not been found.", e.getMessage());
		assertNull(e.getCause());
		assertTrue(e instanceof WebServiceException);
	}

	@Test
	public void notAuthorisedExceptionAddsLinuxAdviceOnLinux() {
		System.setProperty(OS_NAME, "Linux");

		assertEquals(BASE_MESSAGE + " " + LINUX_MESSAGE,
				new NotAuthorisedException().getMessage());
	}

	@Test
	public void notAuthorisedExceptionOmitsLinuxAdviceElsewhere() {
		System.setProperty(OS_NAME, "Windows 11");
		assertEquals(BASE_MESSAGE, new NotAuthorisedException().getMessage());

		System.setProperty(OS_NAME, "Mac OS X");
		assertEquals(BASE_MESSAGE, new NotAuthorisedException().getMessage());
	}

	@Test
	public void notAuthorisedExceptionDetectsLinuxRegardlessOfCase() {
		System.setProperty(OS_NAME, "GNU/LINUX 6.8");

		assertTrue(new NotAuthorisedException().getMessage().endsWith(LINUX_MESSAGE));
	}

	@Test
	public void notAuthorisedExceptionKeepsTheCauseAndTheConstructedMessage() {
		System.setProperty(OS_NAME, "Windows 11");
		IOException cause = new IOException("401");

		NotAuthorisedException e = new NotAuthorisedException(cause);

		assertEquals(BASE_MESSAGE, e.getMessage());
		assertSame(cause, e.getCause());
		assertTrue(e instanceof WebServiceException);
	}

}
