package pitpilot;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class BitOpsHelperTest {

    @Test
    public void powersOfTwoPositiveAndNonPowers() {
        assertTrue(BitOpsHelper.isPowerOfTwo(1));
        assertTrue(BitOpsHelper.isPowerOfTwo(2));
        assertTrue(BitOpsHelper.isPowerOfTwo(1024));
        assertFalse(BitOpsHelper.isPowerOfTwo(0));
        assertFalse(BitOpsHelper.isPowerOfTwo(-1));
        assertFalse(BitOpsHelper.isPowerOfTwo(3));
        assertFalse(BitOpsHelper.isPowerOfTwo(Integer.MIN_VALUE));
    }

    @Test
    public void popCountAcrossZeroPowersAndAllOnes() {
        assertTrue(BitOpsHelper.popCountEquals(0, 0));
        assertTrue(BitOpsHelper.popCountEquals(1, 1));
        assertTrue(BitOpsHelper.popCountEquals(7, 3));
        assertTrue(BitOpsHelper.popCountEquals(-1, 32));
        assertFalse(BitOpsHelper.popCountEquals(7, 2));
    }

    @Test
    public void bitSetAndUnset() {
        assertTrue(BitOpsHelper.isBitSet(0b0101, 0));
        assertTrue(BitOpsHelper.isBitSet(0b0101, 2));
        assertFalse(BitOpsHelper.isBitSet(0b0101, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void bitOutOfRangeRejected() {
        BitOpsHelper.isBitSet(1, 32);
    }

    @Test
    public void xorInvolutionHolds() {
        assertTrue(BitOpsHelper.xorInvolution(0, 0));
        assertTrue(BitOpsHelper.xorInvolution(5, 3));
        assertTrue(BitOpsHelper.xorInvolution(Integer.MIN_VALUE, Integer.MAX_VALUE));
    }

    @Test
    public void rotateRoundTripHolds() {
        assertTrue(BitOpsHelper.rotateRoundTrip(0x12345678, 4));
        assertTrue(BitOpsHelper.rotateRoundTrip(-1, 31));
        assertTrue(BitOpsHelper.rotateRoundTrip(1, 0));
    }
}
