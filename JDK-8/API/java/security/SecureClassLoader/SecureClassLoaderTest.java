/*
 * @test
 * @summary java.security.SecureClassLoader conformance: subclassable, extends ClassLoader.
 *          Appended: both protected constructors usable, parent delegation identity,
 *          default getPermissions(CodeSource) returns an EMPTY grant set (no location-based
 *          grants even for file URLs, probe-verified on JDK-8), null CodeSource tolerated,
 *          overriding subclass observes the caller's CodeSource identity, protected
 *          defineClass(String,byte[],int,int,CodeSource) surface present.
 * @run main SecureClassLoaderTest
 */

public class SecureClassLoaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // subclass exposing the protected default-permissions hook
    static class Exposing extends java.security.SecureClassLoader {
        Exposing() { super(null); }
        @Override
        public java.security.PermissionCollection getPermissions(java.security.CodeSource cs) {
            return super.getPermissions(cs);
        }
    }

    static class Permissive extends java.security.SecureClassLoader {
        java.security.CodeSource seen;
        Permissive() { super(null); }
        @Override
        public java.security.PermissionCollection getPermissions(java.security.CodeSource cs) {
            seen = cs;
            java.security.Permissions p = new java.security.Permissions();
            p.add(new java.security.AllPermission());
            return p;
        }
    }

    public static void main(String[] args) throws Exception {
        java.security.SecureClassLoader scl = new java.security.SecureClassLoader() {};
        check(scl instanceof java.lang.ClassLoader, "extends ClassLoader");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- appended contract dimensions ----
    static void testContract() throws Exception {
        // both protected constructors available to subclasses
        java.security.SecureClassLoader withParent =
                new java.security.SecureClassLoader(SecureClassLoaderTest.class.getClassLoader()) {};
        check(withParent.getParent() == SecureClassLoaderTest.class.getClassLoader(),
                "(ClassLoader) constructor sets parent");
        java.security.SecureClassLoader noParent = new java.security.SecureClassLoader(null) {};
        check(noParent.getParent() == null, "null parent gives bootstrap-parent (null)");

        // default-ctor subclass: parent is the application loader that loaded this test
        java.security.SecureClassLoader anon = new java.security.SecureClassLoader() {};
        check(anon.getParent() == SecureClassLoaderTest.class.getClassLoader(),
                "default ctor parent = defining loader of subclass");

        // delegation: parent-first class loading resolves platform classes identically
        check(anon.loadClass("java.lang.String") == String.class, "delegation resolves String");
        check(anon.loadClass("java.util.ArrayList") == java.util.ArrayList.class, "delegation resolves ArrayList");

        // default getPermissions: EMPTY for both located and null CodeSources
        java.security.CodeSource cs = new java.security.CodeSource(
                new java.net.URL("file:/tmp/jck.jar"), (java.security.CodeSigner[]) null);
        java.security.PermissionCollection dpc = new Exposing().getPermissions(cs);
        check(dpc != null, "default getPermissions non-null");
        check(!dpc.implies(new java.security.SecurityPermission("x")), "default grants no SecurityPermission");
        check(!dpc.implies(new java.io.FilePermission("/tmp/jck.jar", "read")),
                "no implicit location-based FilePermission");
        java.security.PermissionCollection dpcNull =
                new Exposing().getPermissions(new java.security.CodeSource(null, (java.security.CodeSigner[]) null));
        check(dpcNull != null && !dpcNull.implies(new java.security.AllPermission()),
                "null CodeSource: non-null empty collection");

        // overriding subclass: observes the caller's CodeSource identity, widens grants
        Permissive per = new Permissive();
        java.security.PermissionCollection wide = per.getPermissions(cs);
        check(per.seen == cs, "override receives exact CodeSource object");
        check(wide.implies(new java.security.SecurityPermission("anything")), "overridden grants all");
        check(wide.implies(new java.security.AllPermission()), "AllPermission implied");

        // structural: protected defineClass(String,byte[],int,int,CodeSource) and ByteBuffer variant exist
        int defineCount = 0;
        for (java.lang.reflect.Method m : java.security.SecureClassLoader.class.getDeclaredMethods())
            if (m.getName().equals("defineClass")
                    && java.lang.reflect.Modifier.isProtected(m.getModifiers())) defineCount++;
        check(defineCount == 2, "two protected defineClass overloads (byte[]/ByteBuffer + CodeSource)");
        // getPermissions is protected (subclass contract), not public
        java.lang.reflect.Method gp = java.security.SecureClassLoader.class
                .getDeclaredMethod("getPermissions", java.security.CodeSource.class);
        check(java.lang.reflect.Modifier.isProtected(gp.getModifiers()), "getPermissions protected");
        // concrete class (subclassable, non-final)
        check(!java.lang.reflect.Modifier.isFinal(java.security.SecureClassLoader.class.getModifiers()),
                "not final: subclassable");
    }
}
