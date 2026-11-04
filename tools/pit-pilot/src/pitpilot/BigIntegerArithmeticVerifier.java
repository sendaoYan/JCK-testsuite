package pitpilot;

import java.math.BigInteger;

/**
 * 测试辅助「等价逻辑」：把对 java.math.BigInteger 代数性质的校验抽成纯逻辑，
 * 供行为测试复用。这里作为 PIT 的被测类，自检这些辅助断言本身是否写实。
 */
public final class BigIntegerArithmeticVerifier {

    private BigIntegerArithmeticVerifier() {
    }

    public static boolean isCommutativeAdd(BigInteger a, BigInteger b) {
        return a.add(b).equals(b.add(a));
    }

    public static boolean isCommutativeMultiply(BigInteger a, BigInteger b) {
        return a.multiply(b).equals(b.multiply(a));
    }

    public static boolean isAssociativeAdd(BigInteger a, BigInteger b, BigInteger c) {
        return a.add(b).add(c).equals(a.add(b.add(c)));
    }

    public static boolean isDistributive(BigInteger a, BigInteger b, BigInteger c) {
        return a.multiply(b.add(c)).equals(a.multiply(b).add(a.multiply(c)));
    }

    /** 校验 a mod m 的结果落在 [0, |m|) 区间内；m == 0 时非法。 */
    public static boolean isValidMod(BigInteger a, BigInteger m) {
        if (m.signum() == 0) {
            return false;
        }
        BigInteger r = a.mod(m);
        BigInteger absM = m.abs();
        return r.signum() >= 0 && r.compareTo(absM) < 0;
    }
}
