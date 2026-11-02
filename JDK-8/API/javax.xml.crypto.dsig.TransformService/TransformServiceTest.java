/*
 * @test
 * @summary javax.xml.crypto.dsig.TransformService conformance: abstract class hierarchy and methods.
 * @run main TransformServiceTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class TransformServiceTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.TransformService");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.crypto.dsig.Transform"), "implements javax.xml.crypto.dsig.Transform");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getInstance"), "has method getInstance");
        check(names.contains("getMechanismType"), "has method getMechanismType");
        check(names.contains("getAlgorithm"), "has method getAlgorithm");
        check(names.contains("getProvider"), "has method getProvider");
        check(names.contains("init"), "has method init");
        check(names.contains("marshalParams"), "has method marshalParams");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
