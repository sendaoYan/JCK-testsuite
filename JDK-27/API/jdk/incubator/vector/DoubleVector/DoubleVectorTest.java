/*
 * @test
 * @summary jdk.incubator.vector.DoubleVector conformance: white-box structure + black-box behavior.
 * @modules jdk.incubator.vector
 * @run main DoubleVectorTest
 */

import jdk.incubator.vector.*;
import java.lang.reflect.*;

public class DoubleVectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.DoubleVector");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Vector.class.isAssignableFrom(c), "extends Vector");
        check(DoubleVector.SPECIES_128 != null && DoubleVector.SPECIES_256 != null, "species constants");
        check(DoubleVector.SPECIES_128.elementType() == double.class, "element type double");
        check(hasMethod(c, "broadcast"), "broadcast");
        check(hasMethod(c, "add"), "add");
        check(hasMethod(c, "sub"), "sub");
        check(hasMethod(c, "fromArray"), "fromArray");
    }

    static void testBlackBox() {
        VectorSpecies<Double> sp = DoubleVector.SPECIES_256;
        int n = sp.length();
        check(n > 0, "species length positive");
        DoubleVector a = DoubleVector.broadcast(sp, 6.0);
        DoubleVector b = DoubleVector.broadcast(sp, 2.0);
        check(a.lane(0) == 6.0, "broadcast lane");
        DoubleVector sum = a.add(b);
        check(sum.lane(0) == 8.0 && sum.lane(n - 1) == 8.0, "add lanes");
        DoubleVector diff = a.sub(b);
        check(diff.lane(0) == 4.0, "sub lane");
        double[] arr = new double[n];
        sum.intoArray(arr, 0);
        check(arr[0] == 8.0 && arr[n - 1] == 8.0, "intoArray round-trip");
        DoubleVector back = DoubleVector.fromArray(sp, arr, 0);
        check(back.equals(sum), "fromArray round-trip equals");
        double reduced = a.reduceLanes(VectorOperators.ADD);
        check(Math.abs(reduced - 6.0 * n) < 1e-9, "reduceLanes ADD");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
