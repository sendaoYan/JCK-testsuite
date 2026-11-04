/*
 * @test
 * @summary javax.activation.CommandMap conformance: abstract class hierarchy and methods.
 * @run main CommandMapTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class CommandMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.activation.CommandMap");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getDefaultCommandMap"), "has method getDefaultCommandMap");
        check(names.contains("setDefaultCommandMap"), "has method setDefaultCommandMap");
        check(names.contains("getPreferredCommands"), "has method getPreferredCommands");
        check(names.contains("getAllCommands"), "has method getAllCommands");
        check(names.contains("getCommand"), "has method getCommand");
        check(names.contains("createDataContentHandler"), "has method createDataContentHandler");
        check(names.contains("getMimeTypes"), "has method getMimeTypes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
