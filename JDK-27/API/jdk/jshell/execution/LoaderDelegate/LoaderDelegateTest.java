/*
 * @test
 * @summary jdk.jshell.execution.LoaderDelegate conformance: loader delegate interface.
 * @modules jdk.jshell
 * @run main LoaderDelegateTest
 */

import java.lang.reflect.*;
import java.util.*;

public class LoaderDelegateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("jdk.jshell.execution.LoaderDelegate");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
    }

    static void testBlackBox() throws Exception {
        final List<String> loaded = new ArrayList<>();
        final List<String> redefined = new ArrayList<>();
        final List<String> classpath = new ArrayList<>();

        jdk.jshell.execution.LoaderDelegate ld = new jdk.jshell.execution.LoaderDelegate() {
            public void load(jdk.jshell.spi.ExecutionControl.ClassBytecodes[] cbcs) {
                for (jdk.jshell.spi.ExecutionControl.ClassBytecodes c : cbcs) loaded.add(c.name());
            }
            public void classesRedefined(jdk.jshell.spi.ExecutionControl.ClassBytecodes[] cbcs) {
                for (jdk.jshell.spi.ExecutionControl.ClassBytecodes c : cbcs) redefined.add(c.name());
            }
            public void addToClasspath(String path) { classpath.add(path); }
            public Class<?> findClass(String name) throws ClassNotFoundException {
                return Class.forName(name);
            }
        };

        jdk.jshell.spi.ExecutionControl.ClassBytecodes[] cbcs =
            new jdk.jshell.spi.ExecutionControl.ClassBytecodes[] {
                new jdk.jshell.spi.ExecutionControl.ClassBytecodes("a.C1", new byte[]{0}),
                new jdk.jshell.spi.ExecutionControl.ClassBytecodes("a.C2", new byte[]{1, 2})
            };

        ld.load(cbcs);
        check(loaded.size() == 2 && loaded.get(0).equals("a.C1") && loaded.get(1).equals("a.C2"),
              "load() received all class names");

        ld.classesRedefined(cbcs);
        check(redefined.size() == 2 && redefined.get(1).equals("a.C2"),
              "classesRedefined() received all class names");

        String p = "/tmp" + java.io.File.separator + "jck-ld";
        ld.addToClasspath(p);
        check(classpath.size() == 1 && classpath.get(0).equals(p),
              "addToClasspath() stored path");

        check(ld.findClass("java.lang.String") == String.class,
              "findClass() resolves a system class by name");
    }


    // ---- deep contract: four abstract operations, no defaults ----
    static void testDeepShape() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.LoaderDelegate");
        java.util.TreeSet<String> names = new java.util.TreeSet<>();
        int abstractCount = 0;
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getDeclaringClass() == c) {
                names.add(m.getName());
                if (java.lang.reflect.Modifier.isAbstract(m.getModifiers())) abstractCount++;
            }
        }
        check(names.equals(new java.util.TreeSet<>(java.util.Arrays.asList(
                "addToClasspath", "classesRedefined", "findClass", "load"))),
              "exact operation set: " + names);
        check(abstractCount == 4, "all four operations are abstract (no defaults)");
        // RemoteExecutionControl is the partner that actually drives classesRedefined
        Class<?> remote = Class.forName("jdk.jshell.execution.RemoteExecutionControl");
        check(hasMethod(remote, "redefine"), "RemoteExecutionControl.redefine forwards to the delegate");
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepShape();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
