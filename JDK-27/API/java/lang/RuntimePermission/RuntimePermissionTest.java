/*
 * @test
 * @summary java.lang.RuntimePermission conformance: white-box structure + black-box behavior.
 * @run main RuntimePermissionTest
 */

import java.lang.reflect.Modifier;
import java.security.BasicPermission;
import java.security.PermissionCollection;

public class RuntimePermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.RuntimePermission");
        check(Modifier.isFinal(c.getModifiers()), "RuntimePermission is final");
        check(c.getSuperclass() == BasicPermission.class, "extends BasicPermission");
        check(c.getConstructor(String.class) != null, "(String) constructor present");
        check(c.getConstructor(String.class, String.class) != null, "(String,String) constructor present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testRuntimePermission() {
        RuntimePermission p = new RuntimePermission("getClassLoader");
        check(p instanceof BasicPermission, "extends BasicPermission");
        check("getClassLoader".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "getActions is empty for one-arg ctor");

        RuntimePermission withActions = new RuntimePermission("setSecurityManager", "read,write");
        check("setSecurityManager".equals(withActions.getName()), "two-arg getName");
        check("".equals(withActions.getActions()), "BasicPermission ignores actions: getActions empty");

        check(p.implies(new RuntimePermission("getClassLoader")), "implies same name");
        check(!p.implies(new RuntimePermission("setSecurityManager")), "!implies different name");
        check(p.equals(new RuntimePermission("getClassLoader")), "equals same name");
        check(p.hashCode() == new RuntimePermission("getClassLoader").hashCode(), "hashCode");
    }

    
    static void testBlackBox() throws Exception {
        testRuntimePermission();
    }

    // ================= 契约深化（2026-11）追加 =================
    static void expectIae(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    static void testNamesAndActions() {
        String[] names = {"exitVM", "getClassLoader", "setContextClassLoader",
                          "createClassLoader", "readFileDescriptor", "loadLibrary.foo"};
        for (String n : names) {
            check(n.equals(new RuntimePermission(n).getName()), "getName round-trip " + n);
            check("".equals(new RuntimePermission(n).getActions()), "getActions empty for " + n);
        }
        RuntimePermission two = new RuntimePermission("exitVM", "read,write");
        check("exitVM".equals(two.getName()), "two-arg getName");
        check("".equals(two.getActions()), "two-arg actions ignored");
        check("".equals(new RuntimePermission("x", null).getActions()), "two-arg null actions ok");
        check("exitVM".equals(new RuntimePermission("exitVM").getName()),
              "exitVM name not canonicalized in getName");
    }

    static void testNegativeConstruction() {
        try {
            new RuntimePermission(null);
            throw new AssertionError("null name: expected NullPointerException");
        } catch (NullPointerException e) {
            check("name can't be null".equals(e.getMessage()), "null name exact message [JDK27]");
        }
        try {
            new RuntimePermission("");
            throw new AssertionError("empty name: expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            check("name can't be empty".equals(e.getMessage()), "empty name exact message [JDK27]");
        }
        try {
            new RuntimePermission(null, "read");
            throw new AssertionError("two-arg null name: expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
        expectIae(() -> new RuntimePermission("", "read"), "two-arg empty name");
    }

    static void testImpliesMatrix() {
        RuntimePermission exitVm = new RuntimePermission("exitVM");
        check(exitVm.implies(new RuntimePermission("exitVM")), "implies self");
        check(new RuntimePermission("getClassLoader").implies(new RuntimePermission("getClassLoader")),
              "implies same name different instance");
        check(!exitVm.implies(new RuntimePermission("getClassLoader")), "!implies different name");
        // exitVM is internally canonicalized to the exitVM.* hierarchy.
        check(exitVm.implies(new RuntimePermission("exitVM.5")), "exitVM implies exitVM.5");
        check(exitVm.implies(new RuntimePermission("exitVM.42")), "exitVM implies exitVM.42");
        check(!exitVm.implies(new RuntimePermission("exitVMX")), "!implies name sharing a prefix");
        check(!new RuntimePermission("exitVM.5").implies(exitVm), "exitVM.5 !implies exitVM");
        // Wildcard hierarchy semantics.
        check(new RuntimePermission("a.*").implies(new RuntimePermission("a.b")), "a.* implies a.b");
        check(new RuntimePermission("a.*").implies(new RuntimePermission("a.b.c")), "a.* implies a.b.c");
        check(!new RuntimePermission("a.*").implies(new RuntimePermission("a")), "a.* !implies a");
        check(!new RuntimePermission("a").implies(new RuntimePermission("a.*")), "a !implies a.*");
        check(!new RuntimePermission("a.b").implies(new RuntimePermission("a.c")), "sibling not implied");
        check(new RuntimePermission("*").implies(new RuntimePermission("anything")),
              "bare * implies any name");
        // Non-BasicPermission and null arguments are simply not implied.
        check(!exitVm.implies(null), "implies(null) is false");
        check(!exitVm.implies(new java.util.PropertyPermission("exitVM", "read")),
              "implies other Permission type is false");
        for (String n : new String[] {"setIO", "modifyThread", "getenv.*"}) {
            check(new RuntimePermission(n).implies(new RuntimePermission(n)), "reflexive " + n);
        }
    }

    static void testEqualsHashCode() {
        RuntimePermission a = new RuntimePermission("exitVM");
        RuntimePermission b = new RuntimePermission("exitVM");
        check(a.equals(b), "equals same name");
        check(b.equals(a), "equals symmetric");
        check(!a.equals(new RuntimePermission("getClassLoader")), "!equals different name");
        check(!a.equals(null), "!equals null");
        check(!a.equals("exitVM"), "!equals non-permission object");
        check(!a.equals(new java.util.PropertyPermission("exitVM", "read")), "!equals other type");
        check(a.hashCode() == b.hashCode(), "hashCode equal for equal names");
        check(a.hashCode() == a.hashCode(), "hashCode stable");
        check(a != b, "distinct instances despite equality");
    }

    static void testToString() {
        RuntimePermission p = new RuntimePermission("exitVM");
        check("(\"java.lang.RuntimePermission\" \"exitVM\")".equals(p.toString()),
              "toString exact format [JDK27]");
        check(p.toString().contains("exitVM"), "toString contains name");
        check(new RuntimePermission("x", "read").toString().contains("java.lang.RuntimePermission"),
              "two-arg toString class part");
    }

    static void testSerialization() throws Exception {
        RuntimePermission p = new RuntimePermission("exitVM");
        check(p instanceof java.io.Serializable, "Serializable");
        long svuid = java.io.ObjectStreamClass.lookup(RuntimePermission.class).getSerialVersionUID();
        check(svuid == 7399184964622342223L, "serialVersionUID locked 7399184964622342223");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(p);
        }
        Object back = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof RuntimePermission, "round-trip type");
        check(back.equals(p), "round-trip equals original");
        check("exitVM".equals(((RuntimePermission) back).getName()), "round-trip name");
        check(((RuntimePermission) back).implies(new RuntimePermission("exitVM.1")),
              "round-trip keeps exitVM.* canonicalization");
        check("".equals(((RuntimePermission) back).getActions()), "round-trip actions empty");
    }

    static void testGuardAndCollection() {
        RuntimePermission p = new RuntimePermission("exitVM");
        check(p instanceof java.security.Guard, "Permission implements Guard");
        check(p.newPermissionCollection() != null, "newPermissionCollection non-null");
        PermissionCollection pc = p.newPermissionCollection();
        pc.add(p);
        check(pc.implies(new RuntimePermission("exitVM.7")),
              "collection implies after add (exitVM hierarchy)");
        pc.setReadOnly();
        check(pc.isReadOnly(), "collection read-only flag");
        try {
            pc.add(new RuntimePermission("x"));
            throw new AssertionError("add to read-only collection: expected SecurityException");
        } catch (SecurityException expected) {
            passed++;
        }
        check(RuntimePermission.class.getConstructors().length == 2, "exactly 2 public ctors");
    }

public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNamesAndActions();
        testNegativeConstruction();
        testImpliesMatrix();
        testEqualsHashCode();
        testToString();
        testSerialization();
        testGuardAndCollection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
