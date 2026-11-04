package pitpilot;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import org.junit.Test;

public class BigIntegerArithmeticVerifierTest {

    private static final BigInteger[] VALUES = {
        BigInteger.ZERO,
        BigInteger.ONE,
        BigInteger.valueOf(-1),
        BigInteger.valueOf(42),
        BigInteger.valueOf(-100),
        BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(10))
    };

    @Test
    public void addIsCommutative() {
        for (BigInteger a : VALUES) {
            for (BigInteger b : VALUES) {
                assertTrue("add not commutative: " + a + " + " + b,
                    BigIntegerArithmeticVerifier.isCommutativeAdd(a, b));
            }
        }
    }

    @Test
    public void multiplyIsCommutative() {
        for (BigInteger a : VALUES) {
            for (BigInteger b : VALUES) {
                assertTrue("multiply not commutative: " + a + " * " + b,
                    BigIntegerArithmeticVerifier.isCommutativeMultiply(a, b));
            }
        }
    }

    @Test
    public void addIsAssociative() {
        BigInteger[] three = {BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(-7), BigInteger.valueOf(100)};
        for (BigInteger a : three) {
            for (BigInteger b : three) {
                for (BigInteger c : three) {
                    assertTrue("add not associative",
                        BigIntegerArithmeticVerifier.isAssociativeAdd(a, b, c));
                }
            }
        }
    }

    @Test
    public void multiplyDistributesOverAdd() {
        BigInteger[] three = {BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(-3), BigInteger.valueOf(25)};
        for (BigInteger a : three) {
            for (BigInteger b : three) {
                for (BigInteger c : three) {
                    assertTrue("not distributive",
                        BigIntegerArithmeticVerifier.isDistributive(a, b, c));
                }
            }
        }
    }

    @Test
    public void validModRangeIncludingZeroRemainder() {
        // 覆盖 r == 0（a 为 m 的倍数）与 r > 0、负数被除数，及 m == 0 的非法分支。
        assertTrue(BigIntegerArithmeticVerifier.isValidMod(BigInteger.valueOf(7), BigInteger.valueOf(3)));
        assertTrue(BigIntegerArithmeticVerifier.isValidMod(BigInteger.valueOf(-7), BigInteger.valueOf(3)));
        assertTrue(BigIntegerArithmeticVerifier.isValidMod(BigInteger.ZERO, BigInteger.valueOf(5)));
        assertTrue(BigIntegerArithmeticVerifier.isValidMod(BigInteger.valueOf(6), BigInteger.valueOf(3)));
        assertFalse(BigIntegerArithmeticVerifier.isValidMod(BigInteger.valueOf(6), BigInteger.ZERO));
    }
}
