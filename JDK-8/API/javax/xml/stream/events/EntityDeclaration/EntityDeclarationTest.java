/*
 * @test
 * @summary javax.xml.stream.events.EntityDeclaration conformance: interface hierarchy and methods.
 * @run main EntityDeclarationTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class EntityDeclarationTest {
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
        Class<?> c = Class.forName("javax.xml.stream.events.EntityDeclaration");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.stream.events.XMLEvent"), "implements javax.xml.stream.events.XMLEvent");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getPublicId"), "has method getPublicId");
        check(names.contains("getSystemId"), "has method getSystemId");
        check(names.contains("getName"), "has method getName");
        check(names.contains("getNotationName"), "has method getNotationName");
        check(names.contains("getReplacementText"), "has method getReplacementText");
        check(names.contains("getBaseURI"), "has method getBaseURI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
