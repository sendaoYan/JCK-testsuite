/*
 * @test
 * @summary jdk.incubator.vector.IntVector conformance: white-box structure + black-box behavior.
 * @modules jdk.incubator.vector
 * @run main IntVectorTest
 */

import jdk.incubator.vector.*;
import java.lang.reflect.*;

public class IntVectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.IntVector");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Vector.class.isAssignableFrom(c), "extends Vector");
        check(IntVector.SPECIES_64 != null && IntVector.SPECIES_128 != null
              && IntVector.SPECIES_256 != null && IntVector.SPECIES_512 != null, "species constants");
        check(IntVector.SPECIES_64.elementType() == int.class, "element type int");
        check(hasMethod(c, "broadcast"), "broadcast");
        check(hasMethod(c, "add"), "add");
        check(hasMethod(c, "lanewise"), "lanewise");
        check(hasMethod(c, "fromArray"), "fromArray");
        check(hasMethod(c, "intoArray"), "intoArray");
    }

    static void testBlackBox() {
        VectorSpecies<Integer> sp = IntVector.SPECIES_256;
        int n = sp.length();
        check(n > 0, "species length positive");
        IntVector a = IntVector.broadcast(sp, 7);
        IntVector b = IntVector.broadcast(sp, 3);
        check(a.lane(0) == 7, "broadcast lane");
        IntVector sum = a.add(b);
        check(sum.lane(0) == 10 && sum.lane(n - 1) == 10, "add lanes");
        IntVector diff = a.sub(b);
        check(diff.lane(0) == 4, "sub lane");
        IntVector neg = a.neg();
        check(neg.lane(0) == -7, "neg lane");
        int[] arr = new int[n];
        sum.intoArray(arr, 0);
        check(arr[0] == 10 && arr[n - 1] == 10, "intoArray round-trip");
        IntVector back = IntVector.fromArray(sp, arr, 0);
        check(back.equals(sum), "fromArray round-trip equals");
        int reduced = a.reduceLanes(VectorOperators.ADD);
        check(reduced == 7 * n, "reduceLanes ADD");
        VectorMask<Integer> eq = a.eq(b);
        check(!eq.allTrue(), "eq mask not all true");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
