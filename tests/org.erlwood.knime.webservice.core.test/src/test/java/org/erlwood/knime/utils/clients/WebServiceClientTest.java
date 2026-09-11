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
package org.erlwood.knime.utils.clients;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.erlwood.knime.utils.exceptions.NotAuthorisedException;
import org.erlwood.knime.utils.exceptions.NotFoundException;
import org.erlwood.knime.utils.exceptions.WebServiceException;
import org.erlwood.knime.utils.iotiming.IOTiming;
import org.erlwood.knime.utils.settings.WebServiceSettings;
import org.junit.Before;
import org.junit.Test;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;

public class WebServiceClientTest {

	/** Test client exposing the protected helpers and a scriptable invoke(). */
	private static class TestClient extends WebServiceClient {

		private Object[] result;

		private Exception failure;

		TestClient(final IOTiming timing, final WebServiceSettings settings) {
			super(timing, settings);
		}

		@Override
		public Object[] invoke(final String method, final Object... parameters)
				throws WebServiceException {
			if(failure instanceof WebServiceException) {
				throw (WebServiceException)failure;
			} else if(failure instanceof RuntimeException) {
				throw (RuntimeException)failure;
			}
			return result;
		}

		void checkStatus(final int statusCode) throws WebServiceException {
			checkForErrors(statusCode);
		}

		WebServiceException urlException(final URI uri) {
			return createDefaultException(uri);
		}

		WebServiceException settingsException(final String message, final Throwable cause) {
			return createSettingsException(message, cause);
		}

		void recordTiming(final long timing) {
			addTiming(timing);
		}

		boolean compressionEnabled() {
			return canCompress();
		}

	}

	private IOTiming timing;

	private WebServiceSettings settings;

	private TestClient client;

	@Before
	public void createClient() {
		timing = mock(IOTiming.class);
		settings = mock(WebServiceSettings.class);
		client = new TestClient(timing, settings);
	}

	@Test
	public void constructionResetsTheTimingAndKeepsTheSettings() {
		verify(timing).setTiming(0);
		verifyNoMoreInteractions(timing);

		assertSame(settings, client.getSettings());
	}

	@Test
	public void timingIsForwardedToTheTimingCollaborator() {
		client.recordTiming(150);

		verify(timing).addTiming(150);
	}

	@Test
	public void aMissingTimingCollaboratorIsTolerated() {
		new TestClient(null, settings).recordTiming(150);
	}

	@Test
	public void headerValuesAccumulateWithoutDuplicates() {
		client.addHeader("Accept", "application/json");
		client.addHeader("Accept", "application/xml");
		client.addHeader("Accept", "application/json");
		client.addHeader("X-Request-Id", "1");

		assertEquals(List.of("application/json", "application/xml"),
				client.headers.get("Accept"));
		assertEquals(List.of("1"), client.headers.get("X-Request-Id"));
	}

	@Test
	public void replacingAHeaderDiscardsTheExistingValues() {
		client.addHeader("Accept", "application/json");
		client.addHeader("Accept", "application/xml");

		client.replaceHeader("Accept", "text/plain");

		assertEquals(List.of("text/plain"), client.headers.get("Accept"));
	}

	@Test
	public void compressionIsDisabledUntilItIsRequested() {
		assertFalse(client.compressionEnabled());

		client.canCompress(true);
		assertTrue(client.compressionEnabled());

		client.canCompress(false);
		assertFalse(client.compressionEnabled());
	}

	@Test
	public void anOkStatusIsNotAnError() throws WebServiceException {
		client.checkStatus(200);
	}

	@Test
	public void authenticationFailuresBecomeNotAuthorisedExceptions() {
		for(int statusCode : new int[] { 401, 403 }) {
			try {
				client.checkStatus(statusCode);
				fail("Expected NotAuthorisedException for " + statusCode);
			} catch(WebServiceException e) {
				assertTrue(e instanceof NotAuthorisedException);
			}
		}
	}

	@Test
	public void aMissingResourceBecomesANotFoundException() {
		try {
			client.checkStatus(404);
			fail("Expected NotFoundException");
		} catch(WebServiceException e) {
			assertTrue(e instanceof NotFoundException);
			assertEquals("Requested resource has not been found.", e.getMessage());
		}
	}

	@Test
	public void anyOtherStatusReportsTheCode() {
		for(int statusCode : new int[] { 201, 302, 500 }) {
			try {
				client.checkStatus(statusCode);
				fail("Expected WebServiceException for " + statusCode);
			} catch(WebServiceException e) {
				assertEquals("Server returned code " + statusCode, e.getMessage());
			}
		}
	}

	@Test
	public void theUrlExceptionNamesTheOffendingUri() {
		URI uri = URI.create("http://localhost:8080/service");

		assertEquals("Could not create web service client from URL '" + uri + "'.",
				client.urlException(uri).getMessage());
	}

	@Test
	public void theDefaultExceptionNamesTheMethodAndKeepsTheCause() {
		IllegalStateException cause = new IllegalStateException("boom");

		WebServiceException e = client.createDefaultException("getCompound", cause);

		assertEquals("Could not invoke 'getCompound' on web service. boom", e.getMessage());
		assertSame(cause, e.getCause());
	}

	@Test
	public void theDefaultExceptionCopesWithAnUnnamedMethodAndAMessagelessCause() {
		IllegalStateException cause = new IllegalStateException();

		assertEquals("Could not invoke web service.",
				client.createDefaultException(null, cause).getMessage());
		assertEquals("Could not invoke web service.",
				client.createDefaultException("", cause).getMessage());
		assertEquals("Could not invoke 'getCompound' on web service. ",
				client.createDefaultException("getCompound", cause).getMessage());
	}

	@Test
	public void theSettingsExceptionPointsAtTheWebServiceTab() {
		IllegalStateException cause = new IllegalStateException("timed out");

		WebServiceException e = client.settingsException("The time out was exceeded.", cause);

		assertEquals("The time out was exceeded. Try increasing it, by changing the value stored"
				+ " in the node configure dialog 'Web Service' tab.", e.getMessage());
		assertSame(cause, e.getCause());
	}

	@Test
	public void cancellableInvokeReturnsTheUnderlyingResult()
			throws CanceledExecutionException, WebServiceException {
		client.result = new Object[] { "CPD-1", 42 };

		Object[] result = client.invoke((ExecutionContext)null, "getCompound", "CPD-1");

		assertArrayEqualsAsList(client.result, result);
	}

	@Test
	public void cancellableInvokePassesThroughWebServiceExceptions()
			throws CanceledExecutionException {
		WebServiceException expected = new WebServiceException("service unavailable");
		client.failure = expected;

		try {
			client.invoke((ExecutionContext)null, "getCompound");
			fail("Expected WebServiceException");
		} catch(WebServiceException e) {
			assertSame(expected, e);
		}
	}

	@Test
	public void cancellableInvokeWrapsUnexpectedFailures() throws CanceledExecutionException {
		client.failure = new IllegalStateException("boom");

		try {
			client.invoke((ExecutionContext)null, "getCompound");
			fail("Expected WebServiceException");
		} catch(WebServiceException e) {
			assertEquals("Could not invoke 'getCompound' on web service. boom", e.getMessage());
			assertSame(client.failure, e.getCause());
		}
	}

	@Test
	public void aClientCannotBeUsedFromAnotherThread() throws InterruptedException {
		AtomicReference<Exception> thrown = new AtomicReference<>();
		Thread thread = new Thread(() -> {
			try {
				client.invoke((ExecutionContext)null, "getCompound");
			} catch(Exception e) {
				thrown.set(e);
			}
		});

		thread.start();
		thread.join();

		assertTrue(thrown.get() instanceof WebServiceException);
		assertEquals("Cannot use the same web service client in multiple threads.",
				thrown.get().getMessage());
	}

	private static void assertArrayEqualsAsList(final Object[] expected, final Object[] actual) {
		assertEquals(List.of(expected), List.of(actual));
	}

}
