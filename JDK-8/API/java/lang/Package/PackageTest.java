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
        testPackageIdentity();
        testManifestAttributesExact();
        testDefinedPackageContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===== 契约深化（2026-11）新增 fixtures 与断言 =====
    static class PkgLoader extends ClassLoader {
        PkgLoader() { super(PackageTest.class.getClassLoader()); }
        Package def(String name, String st, String sv, String svc,
                    String it, String iv, String ivc, java.net.URL seal) {
            return definePackage(name, st, sv, svc, it, iv, ivc, seal);
        }
    }

    static void testPackageIdentity() throws Exception {
        Package p = String.class.getPackage();
        check(p == Integer.class.getPackage(), "java.lang Package is shared by its classes");
        check(p == Object.class.getPackage(), "Object shares the java.lang Package");
        check(p == Runnable.class.getPackage(), "Runnable shares the java.lang Package");
        check(p == Class.forName("java.lang.StringBuilder").getPackage(),
              "newly loaded bootstrap class resolves to the same Package");
        // NOTE (locked JDK-8 behavior): Package objects are value-like; after the
        // earlier getPackages() enumeration, Package.getPackage may hand out a
        // fresh-but-equal instance, so only content identity is asserted here.
        Package g = Package.getPackage("java.lang");
        check(g != null && "java.lang".equals(g.getName()), "static lookup resolves java.lang");
        check(g.toString().equals(p.toString()), "static lookup is content-equal");
        check(g.isSealed() == p.isSealed(), "static lookup agrees on sealed flag");
        check(g.hashCode() == p.hashCode(), "hashCodes agree (both are name-based)");
        Package util = Package.getPackage("java.util");
        check(util != null && "java.util".equals(util.getName()), "java.util package visible");
        check(util != p, "different packages are different instances");
        check(Package.getPackage("jck.no.such.pkg") == null, "unknown package is null");
        try {
            Package.getPackage(null);
            throw new AssertionError("getPackage(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
        boolean hasLang = false, hasUtil = false;
        for (Package q : Package.getPackages()) {
            if ("java.lang".equals(q.getName())) hasLang = true;
            if ("java.util".equals(q.getName())) hasUtil = true;
        }
        check(hasLang, "getPackages includes java.lang");
        check(hasUtil, "getPackages includes java.util");
        check(p.hashCode() == "java.lang".hashCode(), "package hashCode is the name hashCode");
        check(p.hashCode() == p.hashCode(), "hashCode stable across calls");
        check(p.equals(p), "equals reflexive");
        check(!p.equals(util), "different packages not equal");
        check(!p.equals(null), "equals(null) false");
        check(!p.equals("java.lang"), "Package never equals a String");
        check("package java.lang, Java Platform API Specification, version 1.8".equals(p.toString()),
              "java.lang toString exact on JDK-8: " + p.toString());
        check(util.toString().startsWith("package java.util"), "java.util toString prefix");
    }

    static void testManifestAttributesExact() {
        Package p = String.class.getPackage();
        // JDK-8 rt.jar carries the standard manifest attributes (locked for this toolchain).
        check("Java Platform API Specification".equals(p.getSpecificationTitle()),
              "specification title exact");
        check("1.8".equals(p.getSpecificationVersion()), "specification version is 1.8");
        check("Oracle Corporation".equals(p.getSpecificationVendor()),
              "specification vendor exact");
        check("Java Runtime Environment".equals(p.getImplementationTitle()),
              "implementation title exact");
        check(p.getImplementationVersion() != null, "implementation version present");
        check(p.getImplementationVendor() != null, "implementation vendor present");
        check(!p.isSealed(), "classpath java.lang is not sealed");
        check(p.isCompatibleWith("1.8"), "compatible with 1.8");
        check(p.isCompatibleWith("1.7"), "compatible with 1.7");
        check(p.isCompatibleWith("1"), "compatible with 1");
        check(p.isCompatibleWith("1.0"), "compatible with 1.0");
        check(p.isCompatibleWith("0.9"), "compatible with 0.9");
        check(p.isCompatibleWith("1.8.0"), "compatible with 1.8.0");
        check(!p.isCompatibleWith("1.9"), "not compatible with 1.9");
        check(!p.isCompatibleWith("1.10"), "numeric (not lexicographic) 1.10 comparison");
        check(!p.isCompatibleWith("2"), "not compatible with 2");
        try {
            p.isCompatibleWith("");
            throw new AssertionError("isCompatibleWith(\"\"): expected NumberFormatException");
        } catch (NumberFormatException e) {
            check("For input string: \"\"".equals(e.getMessage()),
                  "empty version NFE message exact: " + e.getMessage());
        }
        try {
            p.isCompatibleWith("not-a-version");
            throw new AssertionError("malformed version: expected NumberFormatException");
        } catch (NumberFormatException expected) {
            passed++;
        }
        try {
            p.isCompatibleWith(null);
            throw new AssertionError("isCompatibleWith(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void testDefinedPackageContract() throws Exception {
        PkgLoader ldr = new PkgLoader();
        Package own = ldr.def("jck.pkg.contract", "cst", "3.2", "csv", "cit", "2.1", "civ", null);
        check(own != null, "definePackage returns the new Package");
        check("jck.pkg.contract".equals(own.getName()), "defined package name round-trip");
        check("cst".equals(own.getSpecificationTitle()), "defined spec title");
        check("3.2".equals(own.getSpecificationVersion()), "defined spec version");
        check("csv".equals(own.getSpecificationVendor()), "defined spec vendor");
        check("cit".equals(own.getImplementationTitle()), "defined impl title");
        check("2.1".equals(own.getImplementationVersion()), "defined impl version");
        check("civ".equals(own.getImplementationVendor()), "defined impl vendor");
        check(!own.isSealed(), "null sealBase defines an unsealed package");
        check(own.isCompatibleWith("3.2"), "compatible with own spec version");
        check(own.isCompatibleWith("3.1"), "compatible with older spec");
        check(own.isCompatibleWith("2.99"), "compatible with much older spec");
        check(!own.isCompatibleWith("3.2.1"), "not compatible with newer spec");
        check(!own.isCompatibleWith("4"), "not compatible with major newer spec");
        check("package jck.pkg.contract, cst, version 3.2".equals(own.toString()),
              "defined package toString exact: " + own.toString());
        check(own.hashCode() == "jck.pkg.contract".hashCode(), "defined package hash is name hash");
        check(own.equals(own) && !own.equals(Package.getPackage("java.lang")),
              "defined package equality");
        check(own.getAnnotation(Deprecated.class) == null, "defined package has no annotations");
        check(own.getAnnotations().length == 0, "defined package annotations empty");
        check(!own.isAnnotationPresent(Deprecated.class), "defined package annotation present false");
        Package sealed = ldr.def("jck.pkg.sealed", null, null, null, null, null, null,
                                 new java.net.URL("file:/tmp/jck-seal.jar"));
        check(sealed != null && sealed.isSealed(), "sealBase URL yields a sealed package");
        check(sealed.getSpecificationTitle() == null && sealed.getImplementationTitle() == null,
              "sealed package carries no manifest attributes");
        try {
            sealed.isCompatibleWith("1.0");
            throw new AssertionError("null specVersion: expected NumberFormatException");
        } catch (NumberFormatException expected) {
            passed++;
        }
        Package dup = null;
        try {
            dup = ldr.def("jck.pkg.contract", "x", "x", "x", "x", "x", "x", null);
            throw new AssertionError("duplicate definePackage: expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
        check(dup == null, "duplicate definition returns nothing");
        check(Package.getPackage("jck.pkg.contract") == null,
              "child-loader packages are invisible to the system loader namespace");
    }
}
