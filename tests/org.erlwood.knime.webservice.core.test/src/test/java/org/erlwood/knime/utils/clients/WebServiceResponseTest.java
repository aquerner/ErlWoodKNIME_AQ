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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

	@Test
	public void newResponseHasNoColumns() {
		WebServiceResponse response = new WebServiceResponse();
		assertFalse(response.hasColumns());
		assertTrue(response.getColumns().isEmpty());
		assertTrue(response.getRows().isEmpty());
	}

	@Test
	public void duplicateColumnsAreAddedOnce() {
		WebServiceResponse response = new WebServiceResponse();
		response.addColumn("compound", StringCell.TYPE);
		response.addColumn("compound", StringCell.TYPE);
		response.addColumn("compound", DoubleCell.TYPE);

		assertTrue(response.hasColumns());
		assertEquals(2, response.getColumns().size());
	}

	@Test
	public void createdRowMatchesColumnCount() {
		WebServiceResponse response = new WebServiceResponse();
		response.addColumn("compound", StringCell.TYPE);
		response.addColumn("ic50", DoubleCell.TYPE);

		Object[] row = response.createRow();

		assertEquals(2, row.length);
		assertEquals(1, response.getRows().size());
		assertTrue(row == response.getRows().get(0));
	}

	@Test
	public void jsonStreamIsParsedIntoTypedColumnsAndRows() {
		String json = "{\"value\":["
				+ "{\"compound\":\"aspirin\",\"ic50\":12.5,\"active\":true},"
				+ "{\"compound\":\"ibuprofen\",\"ic50\":3,\"active\":false}"
				+ "]}";

		WebServiceResponse response = WebServiceResponse
				.readJSONStream(stream(json));

		List<Column> columns = response.getColumns();
		assertEquals(3, columns.size());
		assertEquals("compound", columns.get(0).getName());
		assertEquals(StringCell.TYPE, columns.get(0).getType());
		assertEquals("ic50", columns.get(1).getName());
		assertEquals(DoubleCell.TYPE, columns.get(1).getType());
		assertEquals("active", columns.get(2).getName());
		assertEquals(BooleanCell.TYPE, columns.get(2).getType());

		assertEquals(2, response.getRows().size());
		assertArrayEquals(new Object[] { "aspirin", 12.5d, Boolean.TRUE }, response.getRows().get(0));
		assertArrayEquals(new Object[] { "ibuprofen", 3.0d, Boolean.FALSE }, response.getRows().get(1));
	}

	@Test
	public void jsonWithoutValueKeysProducesEmptyResponse() {
		WebServiceResponse response = WebServiceResponse
				.readJSONStream(stream("{\"metadata\":{\"count\":0}}"));

		assertFalse(response.hasColumns());
		assertTrue(response.getRows().isEmpty());
	}

	private static InputStream stream(final String json) {
		return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
	}

}
