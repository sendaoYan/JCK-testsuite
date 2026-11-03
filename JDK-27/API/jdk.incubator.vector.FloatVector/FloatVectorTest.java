/*
 * @test
 * @summary jdk.incubator.vector.FloatVector conformance: white-box structure + black-box behavior.
 * @modules jdk.incubator.vector
 * @run main FloatVectorTest
 */

import jdk.incubator.vector.*;
import java.lang.reflect.*;

public class FloatVectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.FloatVector");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Vector.class.isAssignableFrom(c), "extends Vector");
        check(FloatVector.SPECIES_128 != null && FloatVector.SPECIES_256 != null, "species constants");
        check(FloatVector.SPECIES_128.elementType() == float.class, "element type float");
        check(hasMethod(c, "broadcast"), "broadcast");
        check(hasMethod(c, "add"), "add");
        check(hasMethod(c, "mul"), "mul");
        check(hasMethod(c, "fromArray"), "fromArray");
    }

    static void testBlackBox() {
        VectorSpecies<Float> sp = FloatVector.SPECIES_256;
        int n = sp.length();
        check(n > 0, "species length positive");
        FloatVector a = FloatVector.broadcast(sp, 2.5f);
        FloatVector b = FloatVector.broadcast(sp, 1.5f);
        check(a.lane(0) == 2.5f, "broadcast lane");
        FloatVector sum = a.add(b);
        check(sum.lane(0) == 4.0f && sum.lane(n - 1) == 4.0f, "add lanes");
        FloatVector prod = a.mul(b);
        check(Math.abs(prod.lane(0) - 3.75f) < 1e-6f, "mul lane");
        float[] arr = new float[n];
        sum.intoArray(arr, 0);
        check(arr[0] == 4.0f && arr[n - 1] == 4.0f, "intoArray round-trip");
        FloatVector back = FloatVector.fromArray(sp, arr, 0);
        check(back.equals(sum), "fromArray round-trip equals");
        float reduced = a.reduceLanes(VectorOperators.ADD);
        check(Math.abs(reduced - 2.5f * n) < 1e-3f, "reduceLanes ADD");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
