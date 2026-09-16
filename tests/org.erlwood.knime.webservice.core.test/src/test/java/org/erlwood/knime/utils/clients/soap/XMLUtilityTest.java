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
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(false);
		return factory.newDocumentBuilder()
				.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
	}

	private static final String XML =
			"<root>"
			+ "<item id=\"1\"><item id=\"nested\"/></item>"
			+ "<other><item id=\"2\"/></other>"
			+ "<ITEM id=\"3\"/>"
			+ "text"
			+ "</root>";

	@Test
	public void findChildrenReturnsOnlyDirectChildrenByDefault() throws Exception {
		Document doc = parse(XML);
		List<Node> items = XMLUtility.findChildren(doc.getDocumentElement(), "item");
		assertEquals(2, items.size());
		assertEquals("1", items.get(0).getAttributes().getNamedItem("id").getNodeValue());
		assertEquals("3", items.get(1).getAttributes().getNamedItem("id").getNodeValue());
	}

	@Test
	public void findChildrenRecursesWhenRequested() throws Exception {
		Document doc = parse(XML);
		List<Node> items = XMLUtility.findChildren(doc.getDocumentElement(), "item", true);
		assertEquals(4, items.size());
		assertEquals("1", items.get(0).getAttributes().getNamedItem("id").getNodeValue());
		assertEquals("nested", items.get(1).getAttributes().getNamedItem("id").getNodeValue());
		assertEquals("2", items.get(2).getAttributes().getNamedItem("id").getNodeValue());
		assertEquals("3", items.get(3).getAttributes().getNamedItem("id").getNodeValue());
	}

	@Test
	public void findChildrenMatchesTagCaseInsensitively() throws Exception {
		Document doc = parse(XML);
		List<Node> items = XMLUtility.findChildren(doc.getDocumentElement(), "Item");
		assertEquals(2, items.size());
	}

	@Test
	public void findChildrenReturnsEmptyListForUnknownOrNullTag() throws Exception {
		Document doc = parse(XML);
		assertTrue(XMLUtility.findChildren(doc.getDocumentElement(), "missing", true).isEmpty());
		assertTrue(XMLUtility.findChildren(doc.getDocumentElement(), null, true).isEmpty());
	}

	@Test
	public void findChildrenOnLeafReturnsEmptyList() throws Exception {
		Document doc = parse("<root><leaf/></root>");
		Node leaf = doc.getDocumentElement().getFirstChild();
		assertTrue(XMLUtility.findChildren(leaf, "leaf", true).isEmpty());
	}
}
