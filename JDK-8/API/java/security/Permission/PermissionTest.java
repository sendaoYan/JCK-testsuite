/*
 * @test
 * @summary java.security.Permission conformance: abstract class hierarchy and key methods.
 * @run main PermissionTest
 */

public class PermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.Permission");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.security.Guard"), "implements java.security.Guard");
        check(in.contains("java.io.Serializable"), "implements java.io.Serializable");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("checkGuard"), "declares checkGuard");
        check(ms.contains("equals"), "declares equals");
        check(ms.contains("getActions"), "declares getActions");
        check(ms.contains("getName"), "declares getName");
        check(ms.contains("hashCode"), "declares hashCode");
        check(ms.contains("implies"), "declares implies");
        check(ms.contains("newPermissionCollection"), "declares newPermissionCollection");
        check(ms.contains("toString"), "declares toString");

        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: behavioral matrix via a named recording subclass ----
    static class NamedPerm extends java.security.Permission {
        NamedPerm(String n) { super(n); }
        public boolean implies(java.security.Permission p) { return p instanceof NamedPerm && getName() != null && getName().equals(p.getName()); }
        public boolean equals(Object o) { return o instanceof NamedPerm && String.valueOf(getName()).equals(String.valueOf(((NamedPerm) o).getName())); }
        public int hashCode() { return String.valueOf(getName()).hashCode(); }
        public String getActions() { return "read"; }
    }

    static void testContractDeep() throws Exception {
        java.security.Permission np = new NamedPerm("a.b");
        check("a.b".equals(np.getName()), "getName exact");
        check("read".equals(np.getActions()), "getActions exact");
        // null name is legal at the base class level (only subclasses like BasicPermission validate)
        java.security.Permission nullNamed = new NamedPerm(null);
        check(nullNamed.getName() == null, "null name preserved (base ctor does not validate)");
        // toString format: ("<class>" "<name>" "<actions>")
        String ts = np.toString();
        check(ts.contains("NamedPerm") && ts.contains("a.b") && ts.contains("read"), "toString contains class/name/actions");
        check(ts.startsWith("(\""), "toString opening paren-quote form");
        // implies/equals/hashCode are entirely subclass-defined at this level
        check(np.implies(new NamedPerm("a.b")), "implies same name true");
        check(!np.implies(new NamedPerm("c.d")), "implies different name false");
        check(!np.implies(null), "implies null false (subclass logic)");
        check(np.equals(new NamedPerm("a.b")), "equals value semantics");
        check(np.hashCode() == new NamedPerm("a.b").hashCode(), "hashCode consistent with equals");
        check(np.equals(np), "equals reflexive");
        check(!np.equals("a.b"), "equals non-Permission false");
        // newPermissionCollection default returns null for plain subclasses
        check(np.newPermissionCollection() == null, "newPermissionCollection default null");
        check(new java.security.SecurityPermission("getPolicy").newPermissionCollection() != null,
                "BasicPermission-family override returns non-null collection");
        // Permission implements Guard: JDK-8 checkGuard delegates to SecurityManager; no SM -> no-op
        np.checkGuard(new Object());
        check(true, "checkGuard no-op without SecurityManager (JDK-8)");
        new java.security.SecurityPermission("setLog").checkGuard(null);
        check(true, "checkGuard SecurityPermission no-op without SM (JDK-8)");
        // serialVersionUID (stable across JDK-8 updates)
        check(java.io.ObjectStreamClass.lookup(java.security.Permission.class).getSerialVersionUID()
                == -5636570222231596674L, "serialVersionUID -5636570222231596674");
        // serialization round-trip keeps name/actions/class
        java.security.Permission rt = (java.security.Permission) roundTrip(np);
        check(rt instanceof NamedPerm, "round-trip preserves concrete class");
        check("a.b".equals(rt.getName()) && "read".equals(rt.getActions()), "round-trip preserves name/actions");
        check(rt.equals(np), "round-trip equals");
        // Permission subclasses with null actions serialize too
        java.security.Permission rtNull = (java.security.Permission) roundTrip(nullNamed);
        check(rtNull.getName() == null, "round-trip preserves null name");
    }

    static Object roundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        java.io.ObjectInputStream oin = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        return oin.readObject();
    }
}
