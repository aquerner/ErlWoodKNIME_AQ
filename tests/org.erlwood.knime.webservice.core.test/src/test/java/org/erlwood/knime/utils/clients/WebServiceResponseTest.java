package org.erlwood.knime.utils.clients;

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
		r.addColumn("b", DoubleCell.TYPE);
		assertEquals(2, r.getColumns().size());
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
		assertTrue(row == r.getRows().get(0));
	}

	@Test
	public void columnEqualityIsByNameAndType() {
		Column a = new Column("x", StringCell.TYPE);
		assertEquals(a, new Column("x", StringCell.TYPE));
		assertEquals(a.hashCode(), new Column("x", StringCell.TYPE).hashCode());
		assertNotEquals(a, new Column("x", DoubleCell.TYPE));
		assertNotEquals(a, new Column("y", StringCell.TYPE));
		assertNotEquals(a, null);
		assertNotEquals(a, "x");
	}

	@Test
	public void readJSONStreamParsesODataStyleValueArray() {
		String body = "{\"@odata.context\":\"ctx\",\"value\":["
				+ "{\"Name\":\"Aspirin\",\"Weight\":180.16,\"Active\":true},"
				+ "{\"Name\":\"Caffeine\",\"Weight\":194.19,\"Active\":false}"
				+ "]}";

		WebServiceResponse r = WebServiceResponse.readJSONStream(json(body));

		List<Column> cols = r.getColumns();
		assertEquals(3, cols.size());
		assertEquals("Name", cols.get(0).getName());
		assertEquals(StringCell.TYPE, cols.get(0).getType());
		assertEquals("Weight", cols.get(1).getName());
		assertEquals(DoubleCell.TYPE, cols.get(1).getType());
		assertEquals("Active", cols.get(2).getName());
		assertEquals(BooleanCell.TYPE, cols.get(2).getType());

		List<Object[]> rows = r.getRows();
		assertEquals(2, rows.size());
		assertEquals("Aspirin", rows.get(0)[0]);
		assertEquals(180.16, (Double) rows.get(0)[1], 1e-9);
		assertEquals(Boolean.TRUE, rows.get(0)[2]);
		assertEquals("Caffeine", rows.get(1)[0]);
		assertEquals(194.19, (Double) rows.get(1)[1], 1e-9);
		assertEquals(Boolean.FALSE, rows.get(1)[2]);
	}

	@Test
	public void readJSONStreamIgnoresContentBeforeValueKey() {
		String body = "{\"meta\":{\"count\":2,\"ok\":true,\"label\":\"x\"},\"value\":[{\"Id\":1}]}";

		WebServiceResponse r = WebServiceResponse.readJSONStream(json(body));

		assertEquals(1, r.getColumns().size());
		assertEquals("Id", r.getColumns().get(0).getName());
		assertEquals(DoubleCell.TYPE, r.getColumns().get(0).getType());
		assertEquals(1, r.getRows().size());
		assertEquals(1.0, (Double) r.getRows().get(0)[0], 1e-9);
	}

	@Test
	public void readJSONStreamIntegersAreParsedAsDoubles() {
		WebServiceResponse r = WebServiceResponse.readJSONStream(json("{\"value\":[{\"n\":42},{\"n\":-7}]}"));
		assertEquals(42.0, (Double) r.getRows().get(0)[0], 1e-9);
		assertEquals(-7.0, (Double) r.getRows().get(1)[0], 1e-9);
	}

	@Test
	public void readJSONStreamWithoutValueKeyYieldsEmptyResponse() {
		WebServiceResponse r = WebServiceResponse.readJSONStream(json("{\"data\":[{\"a\":\"b\"}]}"));
		assertFalse(r.hasColumns());
		assertTrue(r.getRows().isEmpty());
	}

	@Test
	public void readJSONStreamWithEmptyValueArrayYieldsEmptyResponse() {
		WebServiceResponse r = WebServiceResponse.readJSONStream(json("{\"value\":[]}"));
		assertFalse(r.hasColumns());
		assertTrue(r.getRows().isEmpty());
	}

	@Test(expected = jakarta.json.JsonException.class)
	public void readJSONStreamRejectsMalformedJson() {
		WebServiceResponse.readJSONStream(json("{\"value\":[{\"a\":"));
	}
}
