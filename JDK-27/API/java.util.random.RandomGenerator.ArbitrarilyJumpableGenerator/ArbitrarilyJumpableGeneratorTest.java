/*
 * @test
 * @summary java.util.random.RandomGenerator.ArbitrarilyJumpableGenerator
 *          conformance: hierarchy, of() rejection, and declared methods.
 *          (No standard algorithm is arbitrarily-jumpable, so behaviour is
 *          verified through of() and reflection rather than a concrete impl.)
 * @run main ArbitrarilyJumpableGeneratorTest
 */

import java.util.random.RandomGenerator;

public class ArbitrarilyJumpableGeneratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void checkThrowsIAE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    public static void main(String[] args) {
        Class<?> aj = RandomGenerator.ArbitrarilyJumpableGenerator.class;
        check(aj.isInterface(), "ArbitrarilyJumpableGenerator is an interface");
        check(RandomGenerator.LeapableGenerator.class.isAssignableFrom(aj),
              "ArbitrarilyJumpableGenerator extends LeapableGenerator");
        check(RandomGenerator.JumpableGenerator.class.isAssignableFrom(aj),
              "ArbitrarilyJumpableGenerator extends JumpableGenerator");

        // of(): a splittable (non arbitrarily-jumpable) algorithm is rejected;
        // an unknown algorithm is rejected.
        checkThrowsIAE(() -> RandomGenerator.ArbitrarilyJumpableGenerator
                .of("L32X64MixRandom"),
            "of(splittable algorithm)");
        checkThrowsIAE(() -> RandomGenerator.ArbitrarilyJumpableGenerator
                .of("NoSuchAlgorithm"),
            "of(unknown algorithm)");

        // declared methods exist with the documented parameter signatures
        for (String[] m : new String[][] {
                {"copy"},
                {"jumpPowerOfTwo", "int"},
                {"jump", "double"},
                {"jump"},
                {"jumps", "double"},
                {"jumps", "long", "double"},
                {"leap"},
                {"copyAndJump", "double"},
            }) {
            String name = m[0];
            java.util.List<Class<?>> params = new java.util.ArrayList<>();
            for (int i = 1; i < m.length; i++) {
                if (!m[i].isEmpty()) {
                    params.add(paramClass(m[i]));
                }
            }
            try {
                java.lang.reflect.Method meth = aj.getMethod(name,
                        params.toArray(new Class<?>[0]));
                check(meth != null, "method " + name + "/" + params.size());
            } catch (NoSuchMethodException e) {
                throw new AssertionError("missing method " + name, e);
            }
        }

        System.out.println("PASS (" + passed + " assertions)");
    }

    static Class<?> paramClass(String sig) {
        switch (sig) {
            case "int": return int.class;
            case "long": return long.class;
            case "double": return double.class;
            case "": return null;
            default:
                try {
                    return Class.forName(sig);
                } catch (ClassNotFoundException e) {
                    throw new AssertionError("bad sig " + sig, e);
                }
        }
    }
}
