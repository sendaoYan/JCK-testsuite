package pitpilot;

/**
 * 测试辅助「等价逻辑」：整数位运算/数值边界不变量校验器。
 * 把 java.lang.Integer 的 bitCount/rotate 及常见位运算抽成纯逻辑，
 * 供行为测试复用，并作为 PIT 的被测类自检这些辅助断言是否写实。
 */
public final class BitOpsHelper {

    private BitOpsHelper() {
    }

    /** 判断 value 是否为 2 的幂（> 0 且二进制恰好一个 1）。 */
    public static boolean isPowerOfTwo(int value) {
        return value > 0 && (value & (value - 1)) == 0;
    }

    /** 判断 n 二进制中 1 的个数是否等于 expected（Integer.bitCount）。 */
    public static boolean popCountEquals(int n, int expected) {
        return Integer.bitCount(n) == expected;
    }

    /** 判断 value 的第 bit 位（LSB 为 0）是否为 1。 */
    public static boolean isBitSet(int value, int bit) {
        if (bit < 0 || bit >= 32) {
            throw new IllegalArgumentException("bit must be in [0, 32)");
        }
        return (value & (1 << bit)) != 0;
    }

    /** 判断 a XOR b 再 XOR b 是否回到 a（异或对合律）。 */
    public static boolean xorInvolution(int a, int b) {
        return ((a ^ b) ^ b) == a;
    }

    /** 判断 n 先循环左移 d 位再循环右移 d 位是否回到 n。 */
    public static boolean rotateRoundTrip(int n, int distance) {
        return Integer.rotateRight(Integer.rotateLeft(n, distance), distance) == n;
    }
}
