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
	public void parsesTypedColumnsAndRowsFromValueArray() {
		WebServiceResponse response = WebServiceResponse.readJSONStream(json(
				"{\"odata.metadata\":\"ignored\",\"value\":["
				+ "{\"Name\":\"aspirin\",\"Weight\":180.16,\"Active\":true},"
				+ "{\"Name\":\"caffeine\",\"Weight\":194.19,\"Active\":false}"
				+ "]}"));

		assertTrue(response.hasColumns());
		List<Column> columns = response.getColumns();
		assertEquals(3, columns.size());
		assertEquals("Name", columns.get(0).getName());
		assertEquals(StringCell.TYPE, columns.get(0).getType());
		assertEquals("Weight", columns.get(1).getName());
		assertEquals(DoubleCell.TYPE, columns.get(1).getType());
		assertEquals("Active", columns.get(2).getName());
		assertEquals(BooleanCell.TYPE, columns.get(2).getType());

		List<Object[]> rows = response.getRows();
		assertEquals(2, rows.size());
		assertArrayEquals(new Object[] { "aspirin", 180.16, Boolean.TRUE }, rows.get(0));
		assertArrayEquals(new Object[] { "caffeine", 194.19, Boolean.FALSE }, rows.get(1));
	}

	@Test
	public void ignoresEverythingBeforeValueKey() {
		WebServiceResponse response = WebServiceResponse.readJSONStream(json(
				"{\"meta\":{\"count\":2,\"ok\":true,\"label\":\"x\"},\"value\":[{\"Id\":1}]}"));

		assertEquals(1, response.getColumns().size());
		assertEquals("Id", response.getColumns().get(0).getName());
		assertEquals(DoubleCell.TYPE, response.getColumns().get(0).getType());
		assertEquals(1, response.getRows().size());
		assertArrayEquals(new Object[] { 1.0 }, response.getRows().get(0));
	}

	@Test
	public void emptyValueArrayProducesNoColumnsOrRows() {
		WebServiceResponse response = WebServiceResponse.readJSONStream(json("{\"value\":[]}"));
		assertFalse(response.hasColumns());
		assertTrue(response.getColumns().isEmpty());
		assertTrue(response.getRows().isEmpty());
	}

	@Test
	public void nullValuesAreSkipped() {
		WebServiceResponse response = WebServiceResponse.readJSONStream(json(
				"{\"value\":[{\"A\":\"x\",\"B\":null,\"C\":2}]}"));

		assertEquals(2, response.getColumns().size());
		assertEquals("A", response.getColumns().get(0).getName());
		assertEquals("C", response.getColumns().get(1).getName());
		assertArrayEquals(new Object[] { "x", 2.0 }, response.getRows().get(0));
	}

	@Test
	public void integerAndExponentNumbersAreParsedAsDoubles() {
		WebServiceResponse response = WebServiceResponse.readJSONStream(json(
				"{\"value\":[{\"A\":7,\"B\":-2.5,\"C\":1e3}]}"));
		assertArrayEquals(new Object[] { 7.0, -2.5, 1000.0 }, response.getRows().get(0));
		for (Column c : response.getColumns()) {
			assertEquals(DoubleCell.TYPE, c.getType());
		}
	}

	@Test
	public void addColumnDeduplicatesByNameAndType() {
		WebServiceResponse response = new WebServiceResponse();
		response.addColumn("A", StringCell.TYPE);
		response.addColumn("A", StringCell.TYPE);
		response.addColumn("A", DoubleCell.TYPE);
		assertEquals(2, response.getColumns().size());
	}

	@Test
	public void createRowIsSizedToColumnsAndRegistered() {
		WebServiceResponse response = new WebServiceResponse();
		response.addColumn("A", StringCell.TYPE);
		response.addColumn("B", DoubleCell.TYPE);
		Object[] row = response.createRow();
		assertEquals(2, row.length);
		assertEquals(1, response.getRows().size());
		assertTrue(row == response.getRows().get(0));
	}

	@Test
	public void columnEqualityAndHashCode() {
		Column a = new Column("A", StringCell.TYPE);
		Column b = new Column("A", StringCell.TYPE);
		Column c = new Column("A", DoubleCell.TYPE);
		Column d = new Column("B", StringCell.TYPE);
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
		assertNotEquals(a, c);
		assertNotEquals(a, d);
		assertNotEquals(a, "A");
	}
}
