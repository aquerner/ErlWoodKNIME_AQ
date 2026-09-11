package org.erlwood.knime.nodes.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;

import org.junit.Test;

public class DataPointTest {

	private static final double EPS = 1e-9;

	@Test
	public void threeDConstructorAppliesDefaults() {
		DataPoint p = new DataPoint(1.5, -2.5, 3.5);
		assertEquals(1.5, p.getRawX(), EPS);
		assertEquals(-2.5, p.getRawY(), EPS);
		assertEquals(3.5, p.getRawZ(), EPS);
		assertEquals(DataPoint.getDefsize(), p.getSize());
		assertEquals(DataPoint.DEFAULT_COLOR, p.getColor());
		assertEquals("all sizes", p.getSizeLabel());
		assertEquals("all colors", p.getColorLabel());
		assertEquals("", p.getID());
		assertEquals(0.0, p.getSizeValue(), EPS);
		assertEquals(0.0, p.getColorValue(), EPS);
		assertTrue(p.isVisible());
		assertTrue(p.isInZoomRegion());
		assertFalse(p.isSelected());
		assertFalse(p.isHilited());
	}

	@Test
	public void twoDConstructorUsesUnitZAndShortLabels() {
		DataPoint p = new DataPoint(4, 5);
		assertEquals(4.0, p.getRawX(), EPS);
		assertEquals(5.0, p.getRawY(), EPS);
		assertEquals(1.0, p.getRawZ(), EPS);
		assertEquals("size", p.getSizeLabel());
		assertEquals("color", p.getColorLabel());
		assertEquals(Color.orange, p.getColor());
	}

	@Test
	public void getPointBuildsHomogeneousColumnVector() {
		TransformMatrix m = DataPoint.getPoint(1, 2, 3);
		assertEquals(4, m.getData().length);
		assertEquals(1, m.getData()[0].length);
		assertEquals(1.0, m.getData()[0][0], EPS);
		assertEquals(2.0, m.getData()[1][0], EPS);
		assertEquals(3.0, m.getData()[2][0], EPS);
		assertEquals(1.0, m.getData()[3][0], EPS);
	}

	@Test
	public void setPointSizeClampsToTwoThroughTwelve() {
		DataPoint p = new DataPoint(0, 0, 0);
		p.setPointSize(1);
		assertEquals(2, p.getSize());
		p.setPointSize(-100);
		assertEquals(2, p.getSize());
		p.setPointSize(13);
		assertEquals(12, p.getSize());
		p.setPointSize(7);
		assertEquals(7, p.getSize());
		p.setPointSize(2);
		assertEquals(2, p.getSize());
		p.setPointSize(12);
		assertEquals(12, p.getSize());
	}

	@Test
	public void setSizeDoesNotClamp() {
		DataPoint p = new DataPoint(0, 0, 0);
		p.setSize(50);
		assertEquals(50, p.getSize());
	}

	@Test
	public void setColorDerivesHalfIntensitySelectionColour() {
		DataPoint p = new DataPoint(0, 0, 0);
		p.setColor(new Color(200, 100, 51));
		assertEquals(new Color(200, 100, 51), p.getColor());
		assertEquals(new Color(100, 50, 25), p.getSelColor());

		p.setColor(Color.BLACK);
		assertEquals(Color.BLACK, p.getSelColor());
	}

	@Test
	public void rawSettersUpdateUnderlyingPoint() {
		DataPoint p = new DataPoint(0, 0, 0);
		p.setRawX(7);
		p.setRawY(8);
		p.setRawZ(9);
		assertEquals(7.0, p.getRawX(), EPS);
		assertEquals(8.0, p.getRawY(), EPS);
		assertEquals(9.0, p.getRawZ(), EPS);
	}

	@Test
	public void transformedCoordinatesAreZeroBeforeProjection() {
		DataPoint p = new DataPoint(1, 2, 3);
		assertEquals(0.0, p.getTrX(), EPS);
		assertEquals(0.0, p.getTrY(), EPS);
		assertEquals(0.0, p.getTrZ(), EPS);
		assertEquals(0.0, p.getTrW(), EPS);
	}

	@Test
	public void projectWithIdentityMapsNormalisedCoordsToScreen() {
		DataPoint p = new DataPoint(0, 0, 0);
		p.projectPoint(new TransformMatrix(4, true), 200, 100);
		assertEquals(100f, p.getScreenX(), 1e-6);
		assertEquals(50f, p.getScreenY(), 1e-6);
		assertEquals(1.0, p.getTrW(), EPS);

		DataPoint corner = new DataPoint(1, 1, 0);
		corner.projectPoint(new TransformMatrix(4, true), 200, 100);
		assertEquals(200f, corner.getScreenX(), 1e-6);
		assertEquals(0f, corner.getScreenY(), 1e-6);

		DataPoint opposite = new DataPoint(-1, -1, 0);
		opposite.projectPoint(new TransformMatrix(4, true), 200, 100);
		assertEquals(0f, opposite.getScreenX(), 1e-6);
		assertEquals(100f, opposite.getScreenY(), 1e-6);
	}

	@Test
	public void projectAppliesTransformBeforeScreenMapping() {
		DataPoint p = new DataPoint(0, 0, 0);
		p.projectPoint(TransformMatrix.getTranslation(0.5, -0.5, 2), 200, 100);
		assertEquals(0.5, p.getTrX(), EPS);
		assertEquals(-0.5, p.getTrY(), EPS);
		assertEquals(2.0, p.getTrZ(), EPS);
		assertEquals(150f, p.getScreenX(), 1e-6);
		assertEquals(75f, p.getScreenY(), 1e-6);
	}

	@Test
	public void projectDividesByHomogeneousCoordinate() {
		DataPoint p = new DataPoint(1, 1, 0);
		p.projectPoint(TransformMatrix.getScale(1, 1, 1, 2), 200, 100);
		assertEquals(2.0, p.getTrW(), EPS);
		assertEquals(150f, p.getScreenX(), 1e-6);
		assertEquals(25f, p.getScreenY(), 1e-6);
	}

	@Test
	public void projectWithIncompatibleMatrixLeavesScreenCoordsUntouched() {
		DataPoint p = new DataPoint(1, 1, 1);
		p.setScreenX(42f);
		p.setScreenY(43f);
		p.projectPoint(new TransformMatrix(3, true), 200, 100);
		assertEquals(42f, p.getScreenX(), 1e-6);
		assertEquals(43f, p.getScreenY(), 1e-6);
	}
}
