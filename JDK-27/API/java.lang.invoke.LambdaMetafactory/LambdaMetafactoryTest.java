/*
 * @test
 * @summary java.lang.invoke.LambdaMetafactory conformance: flags and metafactory presence.
 * @run main LambdaMetafactoryTest
 */

import java.lang.invoke.LambdaMetafactory;
import java.lang.reflect.Method;

public class LambdaMetafactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLambdaMetafactory() throws Exception {
        check(LambdaMetafactory.FLAG_SERIALIZABLE != 0, "FLAG_SERIALIZABLE");
        check(LambdaMetafactory.FLAG_MARKERS != 0, "FLAG_MARKERS");
        check(LambdaMetafactory.FLAG_BRIDGES != 0, "FLAG_BRIDGES");
        check(LambdaMetafactory.FLAG_SERIALIZABLE != LambdaMetafactory.FLAG_MARKERS,
              "distinct flags");
        boolean hasMetafactory = false;
        boolean hasAltMetafactory = false;
        for (Method m : LambdaMetafactory.class.getMethods()) {
            if (m.getName().equals("metafactory")) hasMetafactory = true;
            if (m.getName().equals("altMetafactory")) hasAltMetafactory = true;
        }
        check(hasMetafactory, "has metafactory");
        check(hasAltMetafactory, "has altMetafactory");
    }

    public static void main(String[] args) throws Exception {
        testLambdaMetafactory();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
