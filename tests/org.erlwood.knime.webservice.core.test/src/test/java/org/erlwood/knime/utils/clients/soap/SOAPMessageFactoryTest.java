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
package org.erlwood.knime.utils.clients.soap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;

public class SOAPMessageFactoryTest {

	private static final String PROTOCOL = SOAPConstants.SOAP_1_1_PROTOCOL;

	private static final String NAMESPACE = "urn:erlwood:test";

	private static final String METHOD = "getCompound";

	private static Map<String, Object> params(final Object... keysAndValues) {
		Map<String, Object> params = new LinkedHashMap<>();
		for(int i = 0; i < keysAndValues.length; i += 2) {
			params.put((String)keysAndValues[i], keysAndValues[i + 1]);
		}
		return params;
	}

	private static SOAPElement bodyElement(final SOAPMessage message) throws SOAPException {
		Iterator<jakarta.xml.soap.Node> children = message.getSOAPBody().getChildElements();
		assertTrue("no body element was added", children.hasNext());
		SOAPElement element = (SOAPElement)children.next();
		assertFalse("more than one body element was added", children.hasNext());
		return element;
	}

	private static List<SOAPElement> childElements(final SOAPElement element) {
		List<SOAPElement> children = new ArrayList<>();
		Iterator<jakarta.xml.soap.Node> iterator = element.getChildElements();
		while(iterator.hasNext()) {
			children.add((SOAPElement)iterator.next());
		}
		return children;
	}

	private static List<String> localNames(final List<SOAPElement> elements) {
		List<String> names = new ArrayList<>();
		for(SOAPElement element : elements) {
			names.add(element.getLocalName());
		}
		return names;
	}

	private static InputStream stream(final String content) {
		return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
	}

	@Test
	public void bodyElementIsNamedAfterTheMethodAndCarriesTheNamespaceAlias()
			throws SOAPException, DatatypeConfigurationException {
		SOAPMessage message = SOAPMessageFactory.createRequestMessage(PROTOCOL, NAMESPACE, "tns",
				METHOD, null);

		SOAPElement body = bodyElement(message);
		assertEquals(METHOD, body.getLocalName());
		assertEquals(NAMESPACE, body.getNamespaceURI());
		assertEquals("tns", body.getPrefix());
		assertTrue(childElements(body).isEmpty());
	}

	@Test
	public void bodyElementWithoutAnAliasStillCarriesTheNamespace()
			throws SOAPException, DatatypeConfigurationException {
		SOAPMessage message = SOAPMessageFactory.createRequestMessage(PROTOCOL, NAMESPACE, null,
				METHOD, params());

		SOAPElement body = bodyElement(message);
		assertEquals(METHOD, body.getLocalName());
		assertEquals(NAMESPACE, body.getNamespaceURI());
	}

	@Test
	public void requestMessageHasNoSoapHeader()
			throws SOAPException, DatatypeConfigurationException, Exception {
		String request = SOAPMessageFactory.createRequestString(PROTOCOL, NAMESPACE, METHOD,
				params("id", "CPD-1"));

		assertFalse(request.contains("Header"));
		assertTrue(request.contains("CPD-1"));
	}

	@Test
	public void parametersBecomeChildElementsInIterationOrder()
			throws SOAPException, DatatypeConfigurationException {
		SOAPMessage message = SOAPMessageFactory.createRequestMessage(PROTOCOL, NAMESPACE, "tns",
				METHOD, params("id", "CPD-1", "limit", 10, "exact", Boolean.TRUE));

		List<SOAPElement> children = childElements(bodyElement(message));
		assertEquals(List.of("id", "limit", "exact"), localNames(children));
		assertEquals("CPD-1", children.get(0).getValue());
		assertEquals("10", children.get(1).getValue());
		assertEquals("true", children.get(2).getValue());
	}

	@Test
	public void nestedMapsBecomeNestedElements()
			throws SOAPException, DatatypeConfigurationException {
		SOAPMessage message = SOAPMessageFactory.createRequestMessage(PROTOCOL, NAMESPACE, "tns",
				METHOD, params("filter", params("min", 1, "max", 2)));

		List<SOAPElement> children = childElements(bodyElement(message));
		assertEquals(List.of("filter"), localNames(children));

		List<SOAPElement> nested = childElements(children.get(0));
		assertEquals(List.of("min", "max"), localNames(nested));
		assertEquals("1", nested.get(0).getValue());
		assertEquals("2", nested.get(1).getValue());
	}

	@Test
	public void listsBecomeRepeatedElementsWithTheSameName()
			throws SOAPException, DatatypeConfigurationException {
		SOAPMessage message = SOAPMessageFactory.createRequestMessage(PROTOCOL, NAMESPACE, "tns",
				METHOD, params("id", List.of("CPD-1", "CPD-2", "CPD-3")));

		List<SOAPElement> children = childElements(bodyElement(message));
		assertEquals(List.of("id", "id", "id"), localNames(children));
		assertEquals("CPD-1", children.get(0).getValue());
		assertEquals("CPD-3", children.get(2).getValue());
	}

	@Test
	public void anEmptyListAddsNoElements() throws SOAPException, DatatypeConfigurationException {
		SOAPMessage message = SOAPMessageFactory.createRequestMessage(PROTOCOL, NAMESPACE, "tns",
				METHOD, params("id", Collections.emptyList()));

		assertTrue(childElements(bodyElement(message)).isEmpty());
	}

	@Test
	public void nestedListsOfMapsAreExpandedIntoRepeatedNestedElements()
			throws SOAPException, DatatypeConfigurationException {
		SOAPMessage message = SOAPMessageFactory.createRequestMessage(PROTOCOL, NAMESPACE, "tns",
				METHOD, params("compound", List.of(params("id", "CPD-1"), params("id", "CPD-2"))));

		List<SOAPElement> children = childElements(bodyElement(message));
		assertEquals(List.of("compound", "compound"), localNames(children));
		assertEquals("CPD-1", childElements(children.get(0)).get(0).getValue());
		assertEquals("CPD-2", childElements(children.get(1)).get(0).getValue());
	}

	@Test
	public void datesAreSerialisedInXmlCalendarFormat()
			throws SOAPException, DatatypeConfigurationException {
		GregorianCalendar calendar = new GregorianCalendar(2017, Calendar.MARCH, 4, 13, 45, 30);
		String expected = DatatypeFactory.newInstance()
				.newXMLGregorianCalendar(calendar)
				.toXMLFormat();

		SOAPMessage message = SOAPMessageFactory.createRequestMessage(PROTOCOL, NAMESPACE, "tns",
				METHOD, params("since", calendar.getTime()));

		List<SOAPElement> children = childElements(bodyElement(message));
		assertEquals(List.of("since"), localNames(children));
		assertEquals(expected, children.get(0).getValue());
	}

	@Test
	public void includeParametersReferenceTheMultiPartIdentifier()
			throws SOAPException, DatatypeConfigurationException {
		SOAPMessage message = SOAPMessageFactory.createRequestMessage(PROTOCOL, NAMESPACE, "tns",
				METHOD, params("data", new SOAPMessageFactory.Include("attachment-1")));

		List<SOAPElement> children = childElements(bodyElement(message));
		assertEquals(List.of("data"), localNames(children));

		List<SOAPElement> include = childElements(children.get(0));
		assertEquals(List.of("Include"), localNames(include));
		assertEquals("http://www.w3.org/2004/08/xop/include", include.get(0).getNamespaceURI());
		assertEquals("xop", include.get(0).getPrefix());
		assertEquals("cid:attachment-1", include.get(0).getAttribute("href"));
	}

	@Test
	public void includeCanBeAddedToAnExistingElement()
			throws SOAPException, DatatypeConfigurationException {
		SOAPMessage message = SOAPMessageFactory.createRequestMessage(PROTOCOL, NAMESPACE, "tns",
				METHOD, params());
		SOAPElement body = bodyElement(message);

		new SOAPMessageFactory.Include("attachment-2").addToElement(body);

		List<SOAPElement> children = childElements(body);
		assertEquals(List.of("Include"), localNames(children));
		assertEquals("cid:attachment-2", children.get(0).getAttribute("href"));
	}

	@Test
	public void nullParameterValuesFailWhileTheTextNodeIsAdded() {
		// A null parameter value is turned into a null text node, which the SAAJ implementation
		// rejects with a NullPointerException instead of producing an empty element. Pinned here
		// as current behaviour, see the pull request description.
		try {
			SOAPMessageFactory.createRequestMessage(PROTOCOL, NAMESPACE, "tns", METHOD,
					params("id", null));
			fail("Expected a null parameter value to be rejected");
		} catch(NullPointerException e) {
			assertNotNull(e);
		} catch(SOAPException | DatatypeConfigurationException e) {
			fail("Expected a NullPointerException, got " + e);
		}
	}

	@Test
	public void requestStringIsWellFormedXmlContainingTheEnvelopeAndParameters()
			throws Exception {
		String request = SOAPMessageFactory.createRequestString(PROTOCOL, NAMESPACE, "tns", METHOD,
				params("id", "CPD-1"));

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		Document document = factory.newDocumentBuilder().parse(stream(request));

		assertEquals("Envelope", document.getDocumentElement().getLocalName());
		assertEquals(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE,
				document.getDocumentElement().getNamespaceURI());
		assertEquals(1, XMLUtility.findChildren(document.getDocumentElement(), "tns:" + METHOD,
				true).size());
		Node method = XMLUtility.findChildren(document.getDocumentElement(), "tns:" + METHOD, true)
				.get(0);
		Node parameter = method.getFirstChild();
		assertEquals("id", parameter.getLocalName());
		assertEquals("CPD-1", parameter.getTextContent());
		assertNull(parameter.getNextSibling());
	}

	@Test
	public void soap12RequestsUseTheSoap12Envelope() throws Exception {
		String request = SOAPMessageFactory.createRequestString(SOAPConstants.SOAP_1_2_PROTOCOL,
				NAMESPACE, METHOD, params("id", "CPD-1"));

		assertTrue(request.contains(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE));
		assertFalse(request.contains(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE));
	}

	@Test
	public void responseStreamIsParsedIntoASoapMessage() throws IOException, SOAPException {
		SOAPMessage message = SOAPMessageFactory.createResponseMessage(PROTOCOL,
				stream(response("<tns:value xmlns:tns=\"" + NAMESPACE + "\">42</tns:value>")));

		SOAPElement body = bodyElement(message);
		assertEquals("value", body.getLocalName());
		assertEquals("42", body.getValue());
	}

	@Test
	public void responseStreamIsParsedIntoAnXmlDocument() throws IOException, SOAPException {
		Document document = SOAPMessageFactory.createResponseXML(PROTOCOL, stream(response(
				"<tns:result xmlns:tns=\"" + NAMESPACE + "\"><tns:id>CPD-1</tns:id></tns:result>"
		)));

		assertNotNull(document.getDocumentElement());
		assertEquals("result", document.getDocumentElement().getLocalName());
		assertEquals("CPD-1",
				XMLUtility.findChildren(document.getDocumentElement(), "tns:id").get(0)
						.getTextContent());
	}

	@Test
	public void anEmptyResponseBodyIsRejected() throws IOException {
		// extractContentAsDocument requires exactly one child element in the body
		try {
			SOAPMessageFactory.createResponseXML(PROTOCOL, stream(response("")));
			fail("Expected an empty response body to be rejected");
		} catch(SOAPException e) {
			assertEquals("Cannot extract Document from body", e.getMessage());
		}
	}

	@Test
	public void malformedResponseStreamIsReportedAsAFailure() {
		try {
			SOAPMessageFactory.createResponseXML(PROTOCOL, stream("this is not xml"));
			fail("Expected the malformed response to be rejected");
		} catch(IOException | SOAPException e) {
			assertNotNull(e);
		}
	}

	/** Wrap the supplied body content in a SOAP 1.1 envelope. */
	private static String response(final String body) {
		return "<soap:Envelope xmlns:soap=\"" + SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE + "\">"
				+ "<soap:Body>" + body + "</soap:Body>"
				+ "</soap:Envelope>";
	}

}
