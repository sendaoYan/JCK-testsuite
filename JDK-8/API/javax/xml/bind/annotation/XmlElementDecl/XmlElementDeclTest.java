/*
 * @test
 * @summary javax.xml.bind.annotation.XmlElementDecl conformance: annotation interface and members.
 * @run main XmlElementDeclTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlElementDeclTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlElementDecl");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("scope"), "has method scope");
        check(names.contains("namespace"), "has method namespace");
        check(names.contains("name"), "has method name");
        check(names.contains("substitutionHeadNamespace"), "has method substitutionHeadNamespace");
        check(names.contains("substitutionHeadName"), "has method substitutionHeadName");
        check(names.contains("defaultValue"), "has method defaultValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
