/*
 * @test
 * @summary java.nio.ByteOrder API conformance: endianness constants and buffer order.
 * @run main ByteOrderTest
 */

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteOrderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testByteOrder() {
        check("BIG_ENDIAN".equals(ByteOrder.BIG_ENDIAN.toString()), "BIG_ENDIAN");
        check("LITTLE_ENDIAN".equals(ByteOrder.LITTLE_ENDIAN.toString()), "LITTLE_ENDIAN");
        check(ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN
                  || ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN,
              "native order is endian");
        ByteBuffer bb = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        check(bb.order() == ByteOrder.LITTLE_ENDIAN, "buffer order");
    }

    public static void main(String[] args) {
        testByteOrder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
