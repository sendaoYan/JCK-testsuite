package pitpilot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.RoundingMode;
import org.junit.Test;

public class DecimalFormatHelperTest {

    @Test
    public void formatCurrencySplitsSignAndCents() {
        assertEquals("-1234", DecimalFormatHelper.formatCurrency(-12.34));
        assertEquals("1234", DecimalFormatHelper.formatCurrency(12.34));
        assertEquals("0", DecimalFormatHelper.formatCurrency(0.0));
        assertEquals("0", DecimalFormatHelper.formatCurrency(-0.0));
    }

    @Test
    public void withinToleranceExactAndStrictBoundary() {
        assertTrue(DecimalFormatHelper.withinTolerance(1.0, 1.0001, 0.001));
        // exact equality with epsilon == 0：区分 <= 与 <
        assertTrue(DecimalFormatHelper.withinTolerance(1.0, 1.0, 0.0));
        assertFalse(DecimalFormatHelper.withinTolerance(1.0, 1.1, 0.001));
    }

    @Test(expected = IllegalArgumentException.class)
    public void withinToleranceRejectsNegativeEpsilon() {
        DecimalFormatHelper.withinTolerance(1.0, 1.0, -0.1);
    }

    @Test
    public void roundToScaleHalfUp() {
        assertEquals(1.24, DecimalFormatHelper.roundToScale(1.235, 2, RoundingMode.HALF_UP), 0.0);
        assertEquals(2.0, DecimalFormatHelper.roundToScale(2.0, 1, RoundingMode.DOWN), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void roundToScaleRejectsNegativeScale() {
        DecimalFormatHelper.roundToScale(1.2, -1, RoundingMode.HALF_UP);
    }

    /**
     * 弱断言演示位：只断言「非 null」，不断言内容。
     * 对 formatPercent 内部的乘法、正负分支、前缀字符串等变异体都无法杀死。
     */
    @Test
    public void formatPercentWeakAssertionOnlyChecksNonNull() {
        assertNotNull(DecimalFormatHelper.formatPercent(12.5));
    }
}
