/*
 * @test
 * @summary jdk.incubator.vector.ShortVector conformance: white-box structure + black-box behavior.
 * @modules jdk.incubator.vector
 * @run main ShortVectorTest
 */

import jdk.incubator.vector.*;
import java.lang.reflect.*;

public class ShortVectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.ShortVector");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Vector.class.isAssignableFrom(c), "extends Vector");
        check(ShortVector.SPECIES_64 != null && ShortVector.SPECIES_128 != null, "species constants");
        check(ShortVector.SPECIES_64.elementType() == short.class, "element type short");
        check(hasMethod(c, "broadcast"), "broadcast");
        check(hasMethod(c, "add"), "add");
        check(hasMethod(c, "fromArray"), "fromArray");
    }

    static void testBlackBox() {
        VectorSpecies<Short> sp = ShortVector.SPECIES_256;
        int n = sp.length();
        check(n > 0, "species length positive");
        ShortVector a = ShortVector.broadcast(sp, (short) 9);
        ShortVector b = ShortVector.broadcast(sp, (short) 4);
        check(a.lane(0) == (short) 9, "broadcast lane");
        ShortVector sum = a.add(b);
        check(sum.lane(0) == (short) 13 && sum.lane(n - 1) == (short) 13, "add lanes");
        short[] arr = new short[n];
        sum.intoArray(arr, 0);
        check(arr[0] == (short) 13 && arr[n - 1] == (short) 13, "intoArray round-trip");
        ShortVector back = ShortVector.fromArray(sp, arr, 0);
        check(back.equals(sum), "fromArray round-trip equals");
        int reduced = a.reduceLanes(VectorOperators.ADD);
        check(reduced == 9 * n, "reduceLanes ADD");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
