/*
 * @test
 * @summary javax.xml.bind.JAXBPermission conformance: BasicPermission semantics, implies matrix and name handling.
 * @run main JAXBPermissionTest
 */


public class JAXBPermissionTest {
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
        javax.xml.bind.JAXBPermission p = new javax.xml.bind.JAXBPermission("setDatatypeConverter");
        check("setDatatypeConverter".equals(p.getName()), "name");
        check(p instanceof java.security.BasicPermission, "is BasicPermission");
        testStructure();
        testImplies();
        testEquality();
        testNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure() throws Exception {
        check(java.lang.reflect.Modifier.isFinal(javax.xml.bind.JAXBPermission.class.getModifiers()), "class is final");
        check(javax.xml.bind.JAXBPermission.class.getSuperclass() == java.security.BasicPermission.class, "direct superclass BasicPermission");
        check(p1().newPermissionCollection() != null, "newPermissionCollection non-null");
        check(javax.xml.bind.JAXBPermission.class.getConstructors().length == 1, "exactly one public constructor (String)");
        check(javax.xml.bind.JAXBPermission.class.getDeclaredConstructors().length == 1, "no hidden extra constructors");
        // actions are not part of this permission type: BasicPermission ignores them
        check("".equals(p1().getActions()), "getActions() == \"\" (locked)");
        check("(\"javax.xml.bind.JAXBPermission\" \"setDatatypeConverter\")".equals(p1().toString()),
                "toString format locked");
    }

    static javax.xml.bind.JAXBPermission p1() {
        return new javax.xml.bind.JAXBPermission("setDatatypeConverter");
    }

    static void testImplies() {
        javax.xml.bind.JAXBPermission p = p1();
        check(p.implies(p), "permission implies itself");
        javax.xml.bind.JAXBPermission other = new javax.xml.bind.JAXBPermission("otherName");
        check(!p.implies(other), "does not imply different name");
        check(!other.implies(p), "different name does not imply this one");
        javax.xml.bind.JAXBPermission wildcard = new javax.xml.bind.JAXBPermission("*");
        check(wildcard.implies(p), "wildcard \"*\" implies specific name (BasicPermission)");
        check(!p.implies(wildcard), "specific name does not imply wildcard");
        check(wildcard.implies(other), "wildcard implies any other name");
    }

    static void testEquality() {
        javax.xml.bind.JAXBPermission a = p1();
        javax.xml.bind.JAXBPermission b = new javax.xml.bind.JAXBPermission("setDatatypeConverter");
        check(a.equals(b), "value equality for same name");
        check(b.equals(a), "equals symmetric");
        check(!a.equals(new javax.xml.bind.JAXBPermission("zzz")), "not equal to different name");
        check(!a.equals(null), "not equal to null");
        check(!a.equals("setDatatypeConverter"), "not equal to non-permission object");
        check(a.hashCode() == b.hashCode(), "hashCode equal for equal permissions");
        check(a.hashCode() != new javax.xml.bind.JAXBPermission("zzz").hashCode(), "hashCode differs for different names");
    }

    static void testNegative() {
        boolean npe = false;
        try { new javax.xml.bind.JAXBPermission(null); }
        catch (NullPointerException x) { npe = true; }
        check(npe, "constructor(null) throws NullPointerException (locked)");
        boolean iae = false;
        try { new javax.xml.bind.JAXBPermission(""); }
        catch (IllegalArgumentException x) { iae = true; }
        check(iae, "constructor(\"\") throws IllegalArgumentException (locked)");
        // no no-arg constructor
        boolean noNoArg = false;
        try { javax.xml.bind.JAXBPermission.class.getConstructor(); }
        catch (NoSuchMethodException x) { noNoArg = true; }
        check(noNoArg, "no no-arg constructor");
    }
}
