/*
 * @test
 * @summary java.lang.RuntimePermission API conformance: name/actions and inheritance.
 * @run main RuntimePermissionTest
 */

import java.security.BasicPermission;
import java.lang.RuntimePermission;

public class RuntimePermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRuntimePermission() {
        RuntimePermission p = new RuntimePermission("getClassLoader");
        check(p instanceof BasicPermission, "extends BasicPermission");
        check("getClassLoader".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "getActions is empty");
    }

    // ================= 契约深化（2026-11）追加 =================
    static void expectNpe(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

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
        // The two-arg constructor accepts but ignores the actions string.
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
            check("name can't be null".equals(e.getMessage()), "null name exact message [JDK8]");
        }
        try {
            new RuntimePermission("");
            throw new AssertionError("empty name: expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            check("name can't be empty".equals(e.getMessage()), "empty name exact message [JDK8]");
        }
        expectNpe(() -> new RuntimePermission(null, "read"), "two-arg null name");
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
        // Reflexivity for several names.
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
              "toString exact format [JDK8]");
        check(p.toString().contains("exitVM"), "toString contains name");
        check(new RuntimePermission("x", "read").toString().contains("java.lang.RuntimePermission"),
              "two-arg toString class part");
    }

    static void testSerialization() throws Exception {
        RuntimePermission p = new RuntimePermission("exitVM");
        check(p instanceof java.io.Serializable, "Serializable");
        // serialVersionUID locked (identical on JDK-8 and JDK-27).
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
        java.security.PermissionCollection pc = p.newPermissionCollection();
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
    }

    static void testWhiteBox() throws Exception {
        check(RuntimePermission.class.isAssignableFrom(
                  Class.forName("java.lang.RuntimePermission").asSubclass(RuntimePermission.class)),
              "loadable by name");
        check(java.lang.reflect.Modifier.isFinal(RuntimePermission.class.getModifiers()),
              "class is final");
        check(RuntimePermission.class.getSuperclass() == BasicPermission.class, "extends BasicPermission");
        check(RuntimePermission.class.getConstructor(String.class) != null, "(String) ctor present");
        check(RuntimePermission.class.getConstructor(String.class, String.class) != null,
              "(String,String) ctor present");
        check(RuntimePermission.class.getConstructors().length == 2, "exactly 2 public ctors");
    }

    public static void main(String[] args) throws Exception {
        testRuntimePermission();
        testNamesAndActions();
        testNegativeConstruction();
        testImpliesMatrix();
        testEqualsHashCode();
        testToString();
        testSerialization();
        testGuardAndCollection();
        testWhiteBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
