package org.erlwood.knime.nodes.graph;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TransformMatrixTest {

	private static final double EPS = 1e-9;

	private static void assertMatrix(double[][] expected, TransformMatrix actual) {
		assertEquals(expected.length, actual.getData().length);
		for (int i = 0; i < expected.length; i++) {
			assertArrayEquals("row " + i, expected[i], actual.getData()[i], EPS);
		}
	}

	private static TransformMatrix column(double x, double y, double z) {
		return new TransformMatrix(new double[][] { { x }, { y }, { z }, { 1.0 } });
	}

	@Test
	public void sizeConstructorCreatesZeroMatrix() {
		TransformMatrix m = new TransformMatrix(2, 3);
		assertEquals(2, m.getData().length);
		assertEquals(3, m.getData()[0].length);
		assertMatrix(new double[][] { { 0, 0, 0 }, { 0, 0, 0 } }, m);
	}

	@Test
	public void identityConstructorPutsOnesOnDiagonalOnly() {
		TransformMatrix m = new TransformMatrix(3, true);
		assertMatrix(new double[][] { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } }, m);
		assertEquals(3.0, m.trace(), EPS);

		TransformMatrix z = new TransformMatrix(3, false);
		assertMatrix(new double[][] { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 } }, z);
		assertEquals(0.0, z.trace(), EPS);
	}

	@Test
	public void arrayConstructorCopiesInput() {
		double[][] src = { { 1, 2 }, { 3, 4 } };
		TransformMatrix m = new TransformMatrix(src);
		assertNotSame(src, m.getData());
		src[0][0] = 99;
		assertEquals(1.0, m.getData()[0][0], EPS);
	}

	@Test
	public void translationMovesPoint() throws Exception {
		TransformMatrix t = TransformMatrix.getTranslation(1, 2, 3);
		TransformMatrix p = TransformMatrix.multiply(t, column(10, 20, 30));
		assertMatrix(new double[][] { { 11 }, { 22 }, { 33 }, { 1 } }, p);
	}

	@Test
	public void scaleMultipliesEachAxis() throws Exception {
		TransformMatrix s = TransformMatrix.getScale(2, 3, 4);
		TransformMatrix p = TransformMatrix.multiply(s, column(1, 1, 1));
		assertMatrix(new double[][] { { 2 }, { 3 }, { 4 }, { 1 } }, p);
		assertEquals(1.0, s.getData()[3][3], EPS);

		TransformMatrix sw = TransformMatrix.getScale(2, 3, 4, 5);
		assertEquals(5.0, sw.getData()[3][3], EPS);
	}

	@Test
	public void rotationAboutZByQuarterTurnMapsXToY() throws Exception {
		TransformMatrix r = TransformMatrix.getRotation("z", Math.PI / 2);
		TransformMatrix p = TransformMatrix.multiply(r, column(1, 0, 0));
		assertMatrix(new double[][] { { 0 }, { 1 }, { 0 }, { 1 } }, p);
	}

	@Test
	public void rotationAboutXByQuarterTurnMapsYToZ() throws Exception {
		TransformMatrix r = TransformMatrix.getRotation("x", Math.PI / 2);
		TransformMatrix p = TransformMatrix.multiply(r, column(0, 1, 0));
		assertMatrix(new double[][] { { 0 }, { 0 }, { 1 }, { 1 } }, p);
	}

	@Test
	public void rotationAboutYByQuarterTurnMapsZToX() throws Exception {
		TransformMatrix r = TransformMatrix.getRotation("y", Math.PI / 2);
		TransformMatrix p = TransformMatrix.multiply(r, column(0, 0, 1));
		assertMatrix(new double[][] { { 1 }, { 0 }, { 0 }, { 1 } }, p);
	}

	@Test
	public void unknownAxisFallsBackToZRotation() {
		TransformMatrix z = TransformMatrix.getRotation("z", 0.7);
		TransformMatrix other = TransformMatrix.getRotation("bogus", 0.7);
		assertMatrix(z.getData(), other);
	}

	@Test
	public void rotationLeavesUnitLengthUnchanged() throws Exception {
		TransformMatrix r = TransformMatrix.getRotation("x", 0.3);
		TransformMatrix p = TransformMatrix.multiply(r, column(0.6, 0.0, 0.8));
		double len = Math.sqrt(p.getData()[0][0] * p.getData()[0][0] + p.getData()[1][0] * p.getData()[1][0]
				+ p.getData()[2][0] * p.getData()[2][0]);
		assertEquals(1.0, len, EPS);
	}

	@Test
	public void perspectiveSetsHomogeneousCoordinateFromZ() throws Exception {
		TransformMatrix persp = TransformMatrix.getPerspective(0, 0, 4);
		TransformMatrix p = TransformMatrix.multiply(persp, column(1, 2, 8));
		assertEquals(1.0, p.getData()[0][0], EPS);
		assertEquals(2.0, p.getData()[1][0], EPS);
		assertEquals(2.0, p.getData()[3][0], EPS); // w = z / 4
		assertEquals(0.0, persp.getData()[3][3], EPS);
	}

	@Test
	public void staticMultiplyRejectsDimensionMismatch() {
		try {
			TransformMatrix.multiply(new TransformMatrix(2, 3), new TransformMatrix(2, 3));
			fail("expected mismatch");
		} catch (Exception e) {
			assertEquals("row column mismatch", e.getMessage());
		}
	}

	@Test
	public void staticMultiplyWithResultRejectsWrongResultShape() {
		try {
			TransformMatrix.multiply(new TransformMatrix(4, true), column(1, 2, 3), new TransformMatrix(4, 4));
			fail("expected mismatch");
		} catch (Exception e) {
			assertEquals("row column mismatch", e.getMessage());
		}
	}

	@Test
	public void staticMultiplyWritesIntoResult() throws Exception {
		TransformMatrix result = new TransformMatrix(4, 1);
		TransformMatrix.multiply(TransformMatrix.getTranslation(1, 1, 1), column(1, 2, 3), result);
		assertMatrix(new double[][] { { 2 }, { 3 }, { 4 }, { 1 } }, result);
	}

	@Test
	public void multiplyMatrixAndPreMultiplyDifferInOrder() throws Exception {
		TransformMatrix a = new TransformMatrix(new double[][] { { 1, 2 }, { 3, 4 } });
		TransformMatrix b = new TransformMatrix(new double[][] { { 0, 1 }, { 1, 0 } });

		TransformMatrix ab = new TransformMatrix(a.getData());
		ab.multiplyMatrix(b);
		assertMatrix(new double[][] { { 2, 1 }, { 4, 3 } }, ab);

		TransformMatrix ba = new TransformMatrix(a.getData());
		ba.preMultiplyMatrix(b);
		assertMatrix(new double[][] { { 3, 4 }, { 1, 2 } }, ba);
	}

	@Test
	public void multiplyMatrixRejectsNonSquareResult() {
		TransformMatrix a = new TransformMatrix(2, 2);
		try {
			a.multiplyMatrix(new TransformMatrix(2, 3));
			fail("expected mismatch");
		} catch (Exception e) {
			assertEquals("row column mismatch - output different size to input", e.getMessage());
		}
		try {
			a.multiplyMatrix(new TransformMatrix(3, 2));
			fail("expected mismatch");
		} catch (Exception e) {
			assertEquals("row column mismatch", e.getMessage());
		}
	}

	@Test
	public void scalarMultiplyAndAddMatrix() throws Exception {
		TransformMatrix a = new TransformMatrix(new double[][] { { 1, 2 }, { 3, 4 } });
		a.multiply(2);
		assertMatrix(new double[][] { { 2, 4 }, { 6, 8 } }, a);

		a.addMatrix(new TransformMatrix(new double[][] { { 1, 1 }, { 1, 1 } }));
		assertMatrix(new double[][] { { 3, 5 }, { 7, 9 } }, a);
	}

	@Test
	public void addMatrixRejectsDifferentShape() {
		try {
			new TransformMatrix(2, 2).addMatrix(new TransformMatrix(3, 3));
			fail("expected mismatch");
		} catch (Exception e) {
			assertEquals("row column mismatch", e.getMessage());
		}
	}

	@Test
	public void transposeSwapsOffDiagonalEntries() {
		TransformMatrix a = new TransformMatrix(new double[][] { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } });
		a.transpose();
		assertMatrix(new double[][] { { 1, 4, 7 }, { 2, 5, 8 }, { 3, 6, 9 } }, a);
		a.transpose();
		assertMatrix(new double[][] { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } }, a);
	}

	@Test
	public void toStringFormatsRowsWithFiveDecimalsAndTabs() {
		TransformMatrix a = new TransformMatrix(new double[][] { { 1, 0.5 } });
		String[] lines = a.toString().split("\n");
		assertEquals(1, lines.length);
		String[] cells = lines[0].split("\t");
		assertEquals(2, cells.length);
		assertEquals("1.00000", cells[0].replace(',', '.'));
		assertEquals("0.50000", cells[1].replace(',', '.'));
	}
}
