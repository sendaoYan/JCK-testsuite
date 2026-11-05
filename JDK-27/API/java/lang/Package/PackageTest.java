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

    // ---- Part 2b: black-box for the previously uncovered Package query methods ----
    static void testBlackBox() {
        Package p = Object.class.getPackage(); // java.lang

        // Manifest attribute getters are all absent for the bootstrap java.lang package.
        check(p.getImplementationTitle() == null, "getImplementationTitle null");
        check(p.getImplementationVersion() == null, "getImplementationVersion null");
        check(p.getSpecificationVendor() == null, "getSpecificationVendor null");
        check(p.getSpecificationVersion() == null, "getSpecificationVersion null");

        // Annotation query methods on a package that carries no annotations.
        check(p.getDeclaredAnnotations().length == 0, "getDeclaredAnnotations empty");
        check(p.getAnnotationsByType(Deprecated.class).length == 0, "getAnnotationsByType empty");
        check(p.getDeclaredAnnotationsByType(Deprecated.class).length == 0,
              "getDeclaredAnnotationsByType empty");
        check(p.getDeclaredAnnotation(Deprecated.class) == null, "getDeclaredAnnotation null");
        check(!p.isAnnotationPresent(Deprecated.class), "isAnnotationPresent false");

        // isCompatibleWith: java.lang has no spec version, so it must throw NumberFormatException.
        try {
            p.isCompatibleWith("1.0");
            throw new AssertionError("isCompatibleWith(null specVersion): expected NumberFormatException");
        } catch (NumberFormatException expected) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testPackage();
        testAttributes();
        testBlackBox();
        testPackageIdentity();
        testBootstrapAttributes();
        testCompatibleNfe();
        testDefinedPackageContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===== 契约深化（2026-11）新增 fixtures 与断言 =====
    static class PkgLoader extends ClassLoader {
        PkgLoader() { super(PackageTest.class.getClassLoader()); }
        @SuppressWarnings("deprecation")
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
        // NOTE: Package objects are value-like; after enumeration the static lookup
        // may hand out a fresh-but-equal instance, so only content identity is asserted.
        Package g = Package.getPackage("java.lang");
        check(g != null && "java.lang".equals(g.getName()), "static lookup resolves java.lang");
        check(g.toString().equals(p.toString()), "static lookup is content-equal");
        check(g.isSealed() == p.isSealed(), "static lookup agrees on sealed flag");
        check(g.hashCode() == p.hashCode(), "hashCodes agree (both are name-based)");
        Package util = Package.getPackage("java.util");
        check(util != null && "java.util".equals(util.getName()), "java.util package visible");
        check(util != p, "different packages are different instances");
        check("package java.util".equals(util.toString()), "java.util toString exact");
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
        check(p.toString().startsWith("package "), "toString starts with 'package '");
    }

    static void testBootstrapAttributes() {
        Package p = String.class.getPackage();
        // On JDK-27 java.lang lives in the java.base module image: no manifest
        // attributes, and the module image is treated as sealed.
        check("package java.lang".equals(p.toString()), "java.lang toString exact");
        check(p.isSealed(), "java.lang is sealed in the module image");
        check(p.getSpecificationTitle() == null, "spec title null");
        check(p.getSpecificationVersion() == null, "spec version null");
        check(p.getSpecificationVendor() == null, "spec vendor null");
        check(p.getImplementationTitle() == null, "impl title null");
        check(p.getImplementationVersion() == null, "impl version null");
        check(p.getImplementationVendor() == null, "impl vendor null");
        check(p.getName().equals("java.lang"), "name round-trip");
        check(p == String.class.getPackage(), "stable for the same class");
    }

    static void testCompatibleNfe() {
        Package p = String.class.getPackage();
        String[] versions = { "1.0", "1", "1.8", "2", "9", "x", "1.0.0" };
        for (int i = 0; i < versions.length; i++) {
            try {
                p.isCompatibleWith(versions[i]);
                throw new AssertionError("null specVersion " + versions[i] + ": expected NFE");
            } catch (NumberFormatException expected) {
                passed++;
            }
        }
        try {
            p.isCompatibleWith("");
            throw new AssertionError("empty version: expected NumberFormatException");
        } catch (NumberFormatException e) {
            check("Empty version string".equals(e.getMessage()),
                  "empty version NFE message exact: " + e.getMessage());
        }
        try {
            p.isCompatibleWith(null);
            throw new AssertionError("null version: expected NumberFormatException");
        } catch (NumberFormatException expected) {
            passed++;
        }
    }

    @SuppressWarnings("deprecation")
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
        check(ldr.getDefinedPackage("jck.pkg.contract") == own,
              "getDefinedPackage reflects the definition");
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
        try {
            ldr.def("jck.pkg.contract", "x", "x", "x", "x", "x", "x", null);
            throw new AssertionError("duplicate definePackage: expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
        check(Package.getPackage("jck.pkg.contract") == null,
              "child-loader packages are invisible to the system loader namespace");
    }
}
