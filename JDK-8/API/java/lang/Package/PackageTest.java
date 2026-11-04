/*
 * @test
 * @summary java.lang.Package conformance: white-box structure (class, not final,
 *          implements AnnotatedElement) + black-box behavior (getName, static
 *          getPackage/getPackages, isSealed false for classpath java.lang,
 *          manifest attribute getters, annotation query methods returning empty,
 *          isCompatibleWith version comparison, toString/hashCode).
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
        check(!c.isInterface(), "Package is a class");
        check(!Modifier.isFinal(c.getModifiers()), "Package is not final in JDK-8");
        check(java.lang.reflect.AnnotatedElement.class.isAssignableFrom(c),
              "Package implements AnnotatedElement");
        check(c.getConstructors().length == 0, "Package has no public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testPackageLookup() {
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
        // Classpath-loaded java.lang is not sealed (sealing is a jar-manifest feature).
        check(!p.isSealed(), "java.lang is not sealed on the classpath");
        // Standard JDK-8 rt.jar manifest attributes are present and non-null.
        check(p.getSpecificationTitle() != null, "specification title non-null");
        check(p.getSpecificationVersion() != null, "specification version non-null");
        check(p.getSpecificationVendor() != null, "specification vendor non-null");
        check(p.getImplementationTitle() != null, "implementation title non-null");
        check(p.getImplementationVersion() != null, "implementation version non-null");
        check(p.getImplementationVendor() != null, "implementation vendor non-null");
        check(p.hashCode() == p.hashCode(), "hashCode stable");
        check(p.toString() != null && p.toString().contains("java.lang"), "toString");
    }

    static void testAnnotations() {
        Package p = Object.class.getPackage();
        check(p.getAnnotation(Deprecated.class) == null, "no Deprecated annotation");
        check(p.getAnnotations().length == 0, "getAnnotations empty");
        check(p.getDeclaredAnnotations().length == 0, "getDeclaredAnnotations empty");
        check(p.getAnnotationsByType(Deprecated.class).length == 0, "getAnnotationsByType empty");
        check(p.getDeclaredAnnotationsByType(Deprecated.class).length == 0,
              "getDeclaredAnnotationsByType empty");
        check(p.getDeclaredAnnotation(Deprecated.class) == null, "getDeclaredAnnotation null");
        check(!p.isAnnotationPresent(Deprecated.class), "isAnnotationPresent false");
    }

    static void testCompatibility() {
        Package p = Object.class.getPackage();
        // JDK-8 java.lang spec version is "1.8".
        check(p.isCompatibleWith("1.8"), "isCompatibleWith(1.8)");
        check(p.isCompatibleWith("1.0"), "isCompatibleWith(1.0)");
        check(!p.isCompatibleWith("1.9"), "not compatible with 1.9");
        // A malformed version string must throw NumberFormatException.
        try {
            p.isCompatibleWith("not-a-version");
            throw new AssertionError("isCompatibleWith(malformed): expected NumberFormatException");
        } catch (NumberFormatException expected) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testPackageLookup();
        testAttributes();
        testAnnotations();
        testCompatibility();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
