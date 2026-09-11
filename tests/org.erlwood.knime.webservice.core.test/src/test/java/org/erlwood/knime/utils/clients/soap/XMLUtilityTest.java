package org.erlwood.knime.utils.clients.soap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XMLUtilityTest {

	private static Document parse(final String xml) throws Exception {
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setNamespaceAware(true);
		return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
	}

	private static final String XML = "<root>"
			+ "<item id=\"1\"/>"
			+ "<group><item id=\"2\"/><ITEM id=\"3\"/><other/></group>"
			+ "<item id=\"4\"/>"
			+ "</root>";

	@Test
	public void findChildrenOneLevelDeepOnlyReturnsDirectChildren() throws Exception {
		Node root = parse(XML).getDocumentElement();
		List<Node> items = XMLUtility.findChildren(root, "item");
		assertEquals(2, items.size());
		assertEquals("1", items.get(0).getAttributes().getNamedItem("id").getNodeValue());
		assertEquals("4", items.get(1).getAttributes().getNamedItem("id").getNodeValue());
	}

	@Test
	public void findChildrenRecursiveReturnsNestedMatchesCaseInsensitively() throws Exception {
		Node root = parse(XML).getDocumentElement();
		List<Node> items = XMLUtility.findChildren(root, "item", true);
		assertEquals(4, items.size());
		assertEquals("1", items.get(0).getAttributes().getNamedItem("id").getNodeValue());
		assertEquals("2", items.get(1).getAttributes().getNamedItem("id").getNodeValue());
		assertEquals("3", items.get(2).getAttributes().getNamedItem("id").getNodeValue());
		assertEquals("4", items.get(3).getAttributes().getNamedItem("id").getNodeValue());
	}

	@Test
	public void findChildrenReturnsEmptyListForNoMatchOrNullTag() throws Exception {
		Node root = parse(XML).getDocumentElement();
		assertTrue(XMLUtility.findChildren(root, "missing", true).isEmpty());
		assertTrue(XMLUtility.findChildren(root, null, true).isEmpty());
	}

	@Test
	public void findChildrenMatchesQualifiedNodeName() throws Exception {
		Node root = parse("<s:Envelope xmlns:s=\"urn:x\"><s:Body><s:Body/></s:Body></s:Envelope>").getDocumentElement();
		assertEquals(1, XMLUtility.findChildren(root, "s:Body").size());
		assertEquals(2, XMLUtility.findChildren(root, "s:Body", true).size());
	}
}
