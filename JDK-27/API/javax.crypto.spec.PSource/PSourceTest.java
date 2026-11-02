/*
 * @test
 * @summary javax.crypto.spec.PSource conformance: protected-ctor PSource with algorithm accessor.
 * @run main PSourceTest
 */

import javax.crypto.spec.*;
import java.lang.reflect.*;

public class PSourceTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        PSource p = new PSource("MyAlg") { };
        check("MyAlg".equals(p.getAlgorithm()), "getAlgorithm");
        Class<?> c = Class.forName("javax.crypto.spec.PSource");
        check(c.getConstructors().length == 0, "no public constructor");
        check(c.getDeclaredConstructors()[0].getModifiers() == Modifier.PROTECTED, "protected constructor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
