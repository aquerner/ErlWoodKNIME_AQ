package org.erlwood.knime.utils.clients;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
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

import jakarta.json.Json;
import jakarta.json.stream.JsonParser;

/** Tests for {@link WebServiceResponse}, in particular the Jakarta JSON based
 * {@link WebServiceResponse#readJSONStream(InputStream)}. */
public class WebServiceResponseTest {

	private static InputStream json(final String s) {
		return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
	}

	@Test
	public void jakartaJsonParserIsAvailableFromPlatform() {
		try (JsonParser parser = Json.createParser(json("{\"a\":1}"))) {
			assertEquals(JsonParser.Event.START_OBJECT, parser.next());
			assertEquals(JsonParser.Event.KEY_NAME, parser.next());
			assertEquals("a", parser.getString());
			assertEquals(JsonParser.Event.VALUE_NUMBER, parser.next());
		}
	}

	@Test
	public void emptyResponseHasNoColumnsOrRows() {
		WebServiceResponse r = new WebServiceResponse();
		assertFalse(r.hasColumns());
		assertTrue(r.getColumns().isEmpty());
		assertTrue(r.getRows().isEmpty());
	}

	@Test
	public void addColumnIgnoresDuplicates() {
		WebServiceResponse r = new WebServiceResponse();
		r.addColumn("a", StringCell.TYPE);
		r.addColumn("a", StringCell.TYPE);
		r.addColumn("a", DoubleCell.TYPE);
		r.addColumn("b", StringCell.TYPE);
		assertEquals(3, r.getColumns().size());
		assertTrue(r.hasColumns());
	}

	@Test
	public void createRowIsSizedToColumnsAndRegistered() {
		WebServiceResponse r = new WebServiceResponse();
		r.addColumn("a", StringCell.TYPE);
		r.addColumn("b", DoubleCell.TYPE);
		Object[] row = r.createRow();
		assertEquals(2, row.length);
		assertEquals(1, r.getRows().size());
		assertSame(row, r.getRows().get(0));
	}

	@Test
	public void columnEqualityAndHashCode() {
		Column a = new Column("x", StringCell.TYPE);
		Column b = new Column("x", StringCell.TYPE);
		Column c = new Column("x", DoubleCell.TYPE);
		Column d = new Column("y", StringCell.TYPE);
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
		assertNotEquals(a, c);
		assertNotEquals(a, d);
		assertNotEquals(a, "x");
		assertEquals("x", a.getName());
		assertEquals(StringCell.TYPE, a.getType());
	}

	@Test
	public void readJSONStreamIgnoresEverythingBeforeValueKey() {
		WebServiceResponse r = WebServiceResponse.readJSONStream(json(
				"{\"odata.metadata\":\"http://example/$metadata\",\"count\":42,"
				+ "\"value\":[{\"Name\":\"Aspirin\"}]}"));
		assertEquals(1, r.getColumns().size());
		assertEquals("Name", r.getColumns().get(0).getName());
		assertEquals(StringCell.TYPE, r.getColumns().get(0).getType());
		assertEquals(1, r.getRows().size());
		assertArrayEquals(new Object[] { "Aspirin" }, r.getRows().get(0));
	}

	@Test
	public void readJSONStreamMapsJsonTypesToKnimeTypes() {
		WebServiceResponse r = WebServiceResponse.readJSONStream(json(
				"{\"value\":[{\"Name\":\"Aspirin\",\"Mass\":180.16,\"Approved\":true,\"Count\":3}]}"));
		List<Column> cols = r.getColumns();
		assertEquals(4, cols.size());
		assertEquals(new Column("Name", StringCell.TYPE), cols.get(0));
		assertEquals(new Column("Mass", DoubleCell.TYPE), cols.get(1));
		assertEquals(new Column("Approved", BooleanCell.TYPE), cols.get(2));
		assertEquals(new Column("Count", DoubleCell.TYPE), cols.get(3));

		assertEquals(1, r.getRows().size());
		Object[] row = r.getRows().get(0);
		assertEquals("Aspirin", row[0]);
		assertEquals(180.16, (Double) row[1], 1e-9);
		assertEquals(Boolean.TRUE, row[2]);
		assertEquals(3.0, (Double) row[3], 1e-9);
	}

	@Test
	public void readJSONStreamProducesOneRowPerObjectAndHandlesFalse() {
		WebServiceResponse r = WebServiceResponse.readJSONStream(json(
				"{\"value\":[{\"Name\":\"A\",\"Active\":true},{\"Name\":\"B\",\"Active\":false}]}"));
		assertEquals(2, r.getColumns().size());
		assertEquals(2, r.getRows().size());
		assertArrayEquals(new Object[] { "A", Boolean.TRUE }, r.getRows().get(0));
		assertArrayEquals(new Object[] { "B", Boolean.FALSE }, r.getRows().get(1));
	}

	@Test
	public void readJSONStreamSkipsNullValues() {
		WebServiceResponse r = WebServiceResponse.readJSONStream(json(
				"{\"value\":[{\"Name\":\"A\",\"Mass\":null},{\"Name\":\"B\",\"Mass\":1.5}]}"));
		// null carries no type information so no column is created for it in the first object
		assertEquals(2, r.getColumns().size());
		assertEquals("Name", r.getColumns().get(0).getName());
		assertEquals("Mass", r.getColumns().get(1).getName());
		assertEquals(2, r.getRows().size());
		assertArrayEquals(new Object[] { "A" }, r.getRows().get(0));
		assertArrayEquals(new Object[] { "B", 1.5 }, r.getRows().get(1));
	}

	@Test
	public void readJSONStreamWithEmptyValueArrayHasNoRows() {
		WebServiceResponse r = WebServiceResponse.readJSONStream(json("{\"value\":[]}"));
		assertFalse(r.hasColumns());
		assertTrue(r.getRows().isEmpty());
	}

	@Test
	public void readJSONStreamWithoutValueKeyStoresNothing() {
		WebServiceResponse r = WebServiceResponse.readJSONStream(json(
				"{\"results\":[{\"Name\":\"Aspirin\",\"Mass\":180.16}]}"));
		assertFalse(r.hasColumns());
		assertTrue(r.getRows().isEmpty());
	}

	@Test
	public void readJSONStreamHandlesUnicodeAndEscapes() {
		WebServiceResponse r = WebServiceResponse.readJSONStream(json(
				"{\"value\":[{\"Name\":\"caf\\u00e9 \\\"quoted\\\"\"}]}"));
		assertArrayEquals(new Object[] { "caf\u00e9 \"quoted\"" }, r.getRows().get(0));
	}

	@Test(expected = jakarta.json.JsonException.class)
	public void readJSONStreamRejectsMalformedJson() {
		WebServiceResponse.readJSONStream(json("{\"value\":[{\"Name\":"));
	}
}
