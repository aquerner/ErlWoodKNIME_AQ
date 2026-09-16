package org.erlwood.knime.utils.clients;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.erlwood.knime.utils.clients.WebServiceResponse.Column;
import org.junit.Test;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;

public class WebServiceResponseTest {

	private static InputStream json(final String s) {
		return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
	}

	@Test
	public void emptyResponseHasNoColumnsOrRows() {
		WebServiceResponse response = new WebServiceResponse();
		assertFalse(response.hasColumns());
		assertTrue(response.getColumns().isEmpty());
		assertTrue(response.getRows().isEmpty());
	}

	@Test
	public void addColumnIgnoresDuplicates() {
		WebServiceResponse response = new WebServiceResponse();
		response.addColumn("a", StringCell.TYPE);
		response.addColumn("a", StringCell.TYPE);
		response.addColumn("a", DoubleCell.TYPE);
		assertEquals(2, response.getColumns().size());
		assertTrue(response.hasColumns());
	}

	@Test
	public void createRowIsSizedToColumnsAndRegistered() {
		WebServiceResponse response = new WebServiceResponse();
		response.addColumn("a", StringCell.TYPE);
		response.addColumn("b", DoubleCell.TYPE);
		Object[] row = response.createRow();
		assertEquals(2, row.length);
		assertEquals(1, response.getRows().size());
		assertTrue(row == response.getRows().get(0));
	}

	@Test
	public void columnEqualityUsesNameAndType() {
		Column a = new Column("x", StringCell.TYPE);
		assertEquals(a, new Column("x", StringCell.TYPE));
		assertEquals(a.hashCode(), new Column("x", StringCell.TYPE).hashCode());
		assertNotEquals(a, new Column("x", DoubleCell.TYPE));
		assertNotEquals(a, new Column("y", StringCell.TYPE));
		assertNotEquals(a, "x");
	}

	@Test
	public void readJSONStreamParsesODataStyleValueArray() {
		String body = "{\"@odata.context\":\"ignored\",\"value\":["
				+ "{\"Name\":\"aspirin\",\"Weight\":180.16,\"Active\":true},"
				+ "{\"Name\":\"caffeine\",\"Weight\":194.19,\"Active\":false}"
				+ "]}";

		WebServiceResponse response = WebServiceResponse.readJSONStream(json(body));

		List<Column> columns = response.getColumns();
		assertEquals(3, columns.size());
		assertEquals(new Column("Name", StringCell.TYPE), columns.get(0));
		assertEquals(new Column("Weight", DoubleCell.TYPE), columns.get(1));
		assertEquals(new Column("Active", BooleanCell.TYPE), columns.get(2));

		List<Object[]> rows = response.getRows();
		assertEquals(2, rows.size());
		assertArrayEquals(new Object[] { "aspirin", 180.16, Boolean.TRUE }, rows.get(0));
		assertArrayEquals(new Object[] { "caffeine", 194.19, Boolean.FALSE }, rows.get(1));
	}

	@Test
	public void readJSONStreamIgnoresContentBeforeValueKey() {
		String body = "{\"meta\":{\"count\":2,\"label\":\"x\",\"flag\":true},"
				+ "\"value\":[{\"id\":1}]}";

		WebServiceResponse response = WebServiceResponse.readJSONStream(json(body));

		assertEquals(1, response.getColumns().size());
		assertEquals("id", response.getColumns().get(0).getName());
		assertEquals(1, response.getRows().size());
		assertArrayEquals(new Object[] { 1.0 }, response.getRows().get(0));
	}

	@Test
	public void readJSONStreamHandlesEmptyValueArray() {
		WebServiceResponse response = WebServiceResponse.readJSONStream(json("{\"value\":[]}"));
		assertFalse(response.hasColumns());
		assertTrue(response.getRows().isEmpty());
	}

	@Test
	public void readJSONStreamParsesIntegersAsDoubles() {
		WebServiceResponse response = WebServiceResponse.readJSONStream(
				json("{\"value\":[{\"n\":42},{\"n\":-7}]}"));
		assertEquals(DoubleCell.TYPE, response.getColumns().get(0).getType());
		assertEquals(42.0, response.getRows().get(0)[0]);
		assertEquals(-7.0, response.getRows().get(1)[0]);
	}

	@Test
	public void readJSONStreamSkipsNullValues() {
		WebServiceResponse response = WebServiceResponse.readJSONStream(
				json("{\"value\":[{\"a\":\"x\",\"b\":null}]}"));
		assertEquals(1, response.getColumns().size());
		assertEquals("a", response.getColumns().get(0).getName());
		assertArrayEquals(new Object[] { "x" }, response.getRows().get(0));
	}

	@Test
	public void readJSONStreamHandlesUnicodeAndEscapes() {
		WebServiceResponse response = WebServiceResponse.readJSONStream(
				json("{\"value\":[{\"s\":\"caf\\u00e9 \\\"quoted\\\"\"}]}"));
		assertEquals("caf\u00e9 \"quoted\"", response.getRows().get(0)[0]);
	}
}
