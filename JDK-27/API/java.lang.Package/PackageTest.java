/*
 * @test
 * @summary java.lang.Package conformance: white-box structure + black-box behavior.
 * @run main PackageTest
 */

import java.lang.reflect.Modifier;

public class PackageTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Package");
        check(Modifier.isFinal(c.getModifiers()), "Package is final");
        check(!c.isInterface(), "Package is a class");
        check(java.lang.reflect.AnnotatedElement.class.isAssignableFrom(c),
              "Package implements AnnotatedElement");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testPackage() {
        Package p = Object.class.getPackage();
        check(p != null, "Object.class.getPackage() non-null");
        check("java.lang".equals(p.getName()), "Object's package is java.lang");
        check(Object.class.getPackage() == Object.class.getPackage(),
              "getPackage is stable for the same class");
        check(Package.getPackage("java.lang") == p, "Package.getPackage(\"java.lang\")");
        check(Package.getPackages().length > 0, "Package.getPackages() non-empty");
    }

    static void testAttributes() {
        Package p = Object.class.getPackage();
        check(p.isSealed(), "java.lang is sealed (jrt-fs/jimage manifest)");
        check(p.getAnnotation(Deprecated.class) == null, "no Deprecated annotation");
        check(p.getAnnotations().length == 0, "no annotations");
        check(p.toString() != null && p.toString().contains("java.lang"), "toString");
        // Manifest attributes are absent for the bootstrap java.lang package.
        check(p.getSpecificationTitle() == null, "specification title null");
        check(p.getImplementationVendor() == null, "implementation vendor null");
        check(p.hashCode() == p.hashCode(), "hashCode stable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testPackage();
        testAttributes();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
