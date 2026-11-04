/*
 * @test
 * @summary java.util.PrimitiveIterator conformance: white-box structure + black-box behavior.
 * @run main PrimitiveIteratorTest
 */

import java.util.Iterator;
import java.util.PrimitiveIterator;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

public class PrimitiveIteratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.PrimitiveIterator");
        check(c.isInterface(), "is an interface");
        check(Iterator.class.isAssignableFrom(c), "extends Iterator");
        Class<?> ofInt = Class.forName("java.util.PrimitiveIterator$OfInt");
        check(PrimitiveIterator.class.isAssignableFrom(ofInt), "OfInt extends PrimitiveIterator");
        check(ofInt.getDeclaredMethod("nextInt").getReturnType() == int.class, "nextInt returns int");
    }

    static void testBlackBox() {
        PrimitiveIterator.OfInt it = IntStream.range(0, 3).iterator();
        check(it.hasNext() && it.nextInt() == 0, "nextInt 0");
        final int[] sum = {0};
        IntConsumer add = v -> sum[0] += v;
        it.forEachRemaining(add);
        check(sum[0] == 3, "forEachRemaining consumes rest (1+2)");
        check(!it.hasNext(), "exhausted");

        PrimitiveIterator.OfInt boxed = IntStream.range(5, 7).iterator();
        check(boxed.next() == 5, "default next() boxes to Integer");
        check(boxed.next() == 6, "default next() second");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
