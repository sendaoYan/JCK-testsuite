/*
 * @test
 * @summary java.io.ObjectStreamClass API conformance: lookup and class descriptor accessors.
 * @run main ObjectStreamClassTest
 */

import java.io.ObjectStreamClass;

public class ObjectStreamClassTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testObjectStreamClass() {
        ObjectStreamClass osc = ObjectStreamClass.lookup(String.class);
        check(osc != null, "lookup returns a descriptor");
        check("java.lang.String".equals(osc.getName()), "getName");
        check(osc.forClass() == String.class, "forClass");
    }

    public static void main(String[] args) {
        testObjectStreamClass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
