/*
 * @test
 * @summary java.lang.instrument.Instrumentation conformance: interface method surface.
 * @run main InstrumentationTest
 */

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class InstrumentationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInstrumentation() {
        check(Instrumentation.class.isInterface(), "is interface");
        Set<String> names = new HashSet<String>();
        for (Method m : Instrumentation.class.getMethods()) {
            names.add(m.getName());
        }
        String[] required = {
            "addTransformer", "removeTransformer", "isRetransformClassesSupported",
            "retransformClasses", "isRedefineClassesSupported", "redefineClasses",
            "isModifiableClass", "getAllLoadedClasses", "getInitiatedClasses",
            "getObjectSize", "appendToBootstrapClassLoaderSearch",
            "appendToSystemClassLoaderSearch", "isNativeMethodPrefixSupported",
            "setNativeMethodPrefix"
        };
        for (String name : required) {
            check(names.contains(name), "has method " + name);
        }
    }

    public static void main(String[] args) {
        testInstrumentation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
