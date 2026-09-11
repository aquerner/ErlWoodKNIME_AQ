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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XMLUtilityTest {

	private static final String XML = "<results>"
			+ "<result id=\"1\"><compound>aspirin</compound></result>"
			+ "<RESULT id=\"2\"><compound>ibuprofen</compound></RESULT>"
			+ "<summary><result id=\"3\"><compound>caffeine</compound></result></summary>"
			+ "</results>";

	private static Element parse(final String xml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		Document document = factory.newDocumentBuilder().parse(
				new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))
		);
		return document.getDocumentElement();
	}

	private static List<String> ids(final List<Node> nodes) {
		List<String> ids = new ArrayList<>();
		for(Node node : nodes) {
			ids.add(node.getAttributes().getNamedItem("id").getNodeValue());
		}
		return ids;
	}

	@Test
	public void searchIsLimitedToDirectChildrenByDefault() throws Exception {
		List<Node> nodes = XMLUtility.findChildren(parse(XML), "result");

		assertEquals(2, nodes.size());
		assertEquals(List.of("1", "2"), ids(nodes));
	}

	@Test
	public void recursiveSearchFindsNestedNodesInDocumentOrder() throws Exception {
		List<Node> nodes = XMLUtility.findChildren(parse(XML), "result", true);

		assertEquals(3, nodes.size());
		assertEquals(List.of("1", "2", "3"), ids(nodes));
	}

	@Test
	public void tagMatchingIgnoresCase() throws Exception {
		assertEquals(2, XMLUtility.findChildren(parse(XML), "RESULT").size());
		assertEquals(2, XMLUtility.findChildren(parse(XML), "ReSuLt").size());
	}

	@Test
	public void anUnknownTagYieldsAnEmptyList() throws Exception {
		assertTrue(XMLUtility.findChildren(parse(XML), "missing", true).isEmpty());
	}

	@Test
	public void aNullTagYieldsAnEmptyList() throws Exception {
		assertTrue(XMLUtility.findChildren(parse(XML), null, true).isEmpty());
	}

	@Test
	public void aLeafNodeHasNoElementChildrenButDoesHaveATextChild() throws Exception {
		Node compound = XMLUtility.findChildren(parse(XML), "compound", true).get(0);

		assertTrue(XMLUtility.findChildren(compound, "compound", true).isEmpty());
		assertEquals(1, XMLUtility.findChildren(compound, "#text").size());
		assertEquals("aspirin", XMLUtility.findChildren(compound, "#text").get(0).getNodeValue());
	}

	@Test
	public void searchingAnEmptyElementYieldsAnEmptyList() throws Exception {
		assertTrue(XMLUtility.findChildren(parse("<results/>"), "result", true).isEmpty());
	}

	@Test
	public void namespacePrefixesArePartOfTheMatchedName() throws Exception {
		Element root = parse("<ns:results xmlns:ns=\"urn:test\">"
				+ "<ns:result id=\"1\"/>"
				+ "</ns:results>");

		assertTrue(XMLUtility.findChildren(root, "result").isEmpty());
		assertEquals(1, XMLUtility.findChildren(root, "ns:result").size());
	}

	@Test
	public void eachCallReturnsAnIndependentList() throws Exception {
		Element root = parse(XML);

		List<Node> first = XMLUtility.findChildren(root, "result");
		first.clear();

		assertEquals(2, XMLUtility.findChildren(root, "result").size());
	}

}
