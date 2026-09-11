package org.erlwood.knime.nodes.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PlotScaleTest {

	private static final double TOL = 1e-4;

	@Test
	public void constructorStoresLimitsLabelAndType() {
		PlotScale s = new PlotScale(-1, 9, "conc", PlotScale.Y_AXIS);
		assertEquals(-1.0, s.getLowLimit(), 0);
		assertEquals(9.0, s.getHighLimit(), 0);
		assertEquals("conc", s.getLabel());
		assertEquals(PlotScale.Y_AXIS, s.getType());
		assertNotNull(s.getNf());
	}

	@Test
	public void axisConstantsAreDistinct() {
		assertEquals(0, PlotScale.X_AXIS);
		assertEquals(1, PlotScale.Y_AXIS);
		assertEquals(2, PlotScale.Z_AXIS);
	}

	@Test
	public void rangeOfTenUsesTickOfTwo() {
		PlotScale s = new PlotScale(0, 10, "x", PlotScale.X_AXIS);
		assertEquals(2.0, s.getTick(), TOL);
		assertEquals(0.0, s.getFirstTick(), TOL);
	}

	@Test
	public void rangeOfHundredScalesTickByTen() {
		PlotScale s = new PlotScale(0, 100, "x", PlotScale.X_AXIS);
		assertEquals(20.0, s.getTick(), TOL);
	}

	@Test
	public void rangeBelowFiveUsesTickOfOne() {
		PlotScale s = new PlotScale(0, 4, "x", PlotScale.X_AXIS);
		assertEquals(1.0, s.getTick(), TOL);
	}

	@Test
	public void rangeBelowTwoAndHalfUsesTickOfHalf() {
		PlotScale s = new PlotScale(0, 2, "x", PlotScale.X_AXIS);
		assertEquals(0.5, s.getTick(), TOL);
	}

	@Test
	public void rangeBelowOneAndQuarterUsesTickOfFifth() {
		PlotScale s = new PlotScale(0, 1, "x", PlotScale.X_AXIS);
		assertEquals(0.2, s.getTick(), TOL);
	}

	@Test
	public void smallRangeScalesTickDown() {
		PlotScale s = new PlotScale(0, 0.01, "x", PlotScale.X_AXIS);
		assertEquals(0.002, s.getTick(), 1e-6);
		assertEquals(3, s.getNf().getMinimumFractionDigits());
		assertEquals(3, s.getNf().getMaximumFractionDigits());
	}

	@Test
	public void largeRangeUsesNoFractionDigits() {
		PlotScale s = new PlotScale(0, 5000, "x", PlotScale.X_AXIS);
		assertEquals(2000.0, s.getTick(), 2000 * 1e-5);
		assertEquals(0, s.getNf().getMinimumFractionDigits());
		assertEquals(0, s.getNf().getMaximumFractionDigits());
	}

	@Test
	public void firstTickIsFirstMultipleAtOrAboveLowLimit() {
		PlotScale s = new PlotScale(3, 13, "x", PlotScale.X_AXIS);
		assertEquals(2.0, s.getTick(), TOL);
		assertEquals(4.0, s.getFirstTick(), TOL);
		assertTrue(s.getFirstTick() >= s.getLowLimit());
	}

	@Test
	public void firstTickWithNegativeLowLimit() {
		PlotScale s = new PlotScale(-5, 5, "x", PlotScale.X_AXIS);
		assertEquals(2.0, s.getTick(), TOL);
		assertEquals(-4.0, s.getFirstTick(), TOL);
	}

	@Test
	public void firstTickEqualsLowLimitWhenAlreadyOnGrid() {
		PlotScale s = new PlotScale(4, 14, "x", PlotScale.X_AXIS);
		assertEquals(4.0, s.getFirstTick(), TOL);
	}

	@Test
	public void reversedLimitsUseAbsoluteRange() {
		PlotScale s = new PlotScale(10, 0, "x", PlotScale.X_AXIS);
		assertEquals(2.0, s.getTick(), TOL);
	}

	@Test
	public void zeroRangeFallsBackToPercentOfLowLimit() {
		PlotScale s = new PlotScale(0.3, 0.3, "x", PlotScale.X_AXIS);
		assertEquals(0.001, s.getTick(), 1e-7);
	}

	@Test
	public void updateScaleRecomputesTicks() {
		PlotScale s = new PlotScale(0, 10, "x", PlotScale.X_AXIS);
		assertEquals(2.0, s.getTick(), TOL);
		s.updateScale(0, 1);
		assertEquals(0.0, s.getLowLimit(), 0);
		assertEquals(1.0, s.getHighLimit(), 0);
		assertEquals(0.2, s.getTick(), TOL);
	}
}
