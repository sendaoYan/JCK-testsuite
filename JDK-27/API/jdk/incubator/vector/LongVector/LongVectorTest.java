/*
 * @test
 * @summary jdk.incubator.vector.LongVector conformance: white-box structure + black-box behavior.
 * @modules jdk.incubator.vector
 * @run main LongVectorTest
 */

import jdk.incubator.vector.*;
import java.lang.reflect.*;

public class LongVectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.LongVector");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Vector.class.isAssignableFrom(c), "extends Vector");
        check(LongVector.SPECIES_64 != null && LongVector.SPECIES_128 != null, "species constants");
        check(LongVector.SPECIES_64.elementType() == long.class, "element type long");
        check(hasMethod(c, "broadcast"), "broadcast");
        check(hasMethod(c, "add"), "add");
        check(hasMethod(c, "fromArray"), "fromArray");
    }

    static void testBlackBox() {
        VectorSpecies<Long> sp = LongVector.SPECIES_256;
        int n = sp.length();
        check(n > 0, "species length positive");
        LongVector a = LongVector.broadcast(sp, 99L);
        LongVector b = LongVector.broadcast(sp, 1L);
        check(a.lane(0) == 99L, "broadcast lane");
        LongVector sum = a.add(b);
        check(sum.lane(0) == 100L && sum.lane(n - 1) == 100L, "add lanes");
        long[] arr = new long[n];
        sum.intoArray(arr, 0);
        check(arr[0] == 100L && arr[n - 1] == 100L, "intoArray round-trip");
        LongVector back = LongVector.fromArray(sp, arr, 0);
        check(back.equals(sum), "fromArray round-trip equals");
        long reduced = a.reduceLanes(VectorOperators.ADD);
        check(reduced == 99L * n, "reduceLanes ADD");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
