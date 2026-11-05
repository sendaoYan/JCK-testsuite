/*
 * @test
 * @summary jdk.jshell.execution.JdiDefaultExecutionControl conformance: jdi default execution control.
 * @modules jdk.jshell
 * @run main JdiDefaultExecutionControlTest
 */

import java.lang.reflect.*;

public class JdiDefaultExecutionControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("jdk.jshell.execution.JdiDefaultExecutionControl");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getSuperclass().getName().equals("jdk.jshell.execution.JdiExecutionControl"), "extends JdiExecutionControl");
    }

    static void testBlackBox() throws Exception {
        // White-box degradation: JdiDefaultExecutionControl exposes no
        // public/protected constructor (only a private one plus the package-private
        // static create() factory), and a live instance requires launching a real
        // remote debuggee JVM over JDI — unsafe/non-headless. Assert method presence
        // and constructor shape instead of behavior.
        Class<?> c = Class.forName("jdk.jshell.execution.JdiDefaultExecutionControl");
        check(hasMethod(c, "close"), "declares close()");
        check(hasMethod(c, "invoke"), "declares invoke()");
        check(hasMethod(c, "stop"), "declares stop()");
        check(hasMethod(c, "vm"), "declares vm()");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1 && !Modifier.isPublic(ctors[0].getModifiers()),
              "no public constructor (factory/private only)");
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }


    // ---- deep contract: factory wiring surface ----
    static void testDeepShape() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.JdiDefaultExecutionControl");
        check(hasMethod(c, "create"), "declares static create() factory");
        // the nested JdiStarter SPI is reachable from the provider
        Class<?> starter = Class.forName("jdk.jshell.execution.JdiDefaultExecutionControl$JdiStarter");
        check(starter.isInterface(), "JdiStarter is an interface nested here");
        Class<?> provider = Class.forName("jdk.jshell.execution.JdiExecutionControlProvider");
        check(provider.getConstructor(starter) != null, "provider can be built on a custom JdiStarter");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepShape();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
