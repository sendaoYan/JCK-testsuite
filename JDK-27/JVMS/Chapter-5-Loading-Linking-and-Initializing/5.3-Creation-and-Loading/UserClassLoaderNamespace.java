/*
 * @test
 * @summary JVMS 5.3.2: each user-defined class loader defines its own
 *          namespace, so two loaders loading the same bytes produce distinct
 *          Class objects that are not mutually assignable, while a child
 *          loader delegates to its parent for already-visible classes.
 * @run main UserClassLoaderNamespace
 */

public class UserClassLoaderNamespace {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static class Probe {
        public static int value = 42;
    }

    static class ByteLoader extends ClassLoader {
        ByteLoader() { super(null); }  // bootstrap parent, independent namespace
        Class<?> define(String name, byte[] bytes) {
            return defineClass(name, bytes, 0, bytes.length);
        }
    }

    static byte[] readAll(java.io.InputStream in) throws java.io.IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        return out.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        byte[] bytes = readAll(UserClassLoaderNamespace.class
                .getResourceAsStream("UserClassLoaderNamespace$Probe.class"));

        ByteLoader l1 = new ByteLoader();
        ByteLoader l2 = new ByteLoader();
        Class<?> c1 = l1.define("UserClassLoaderNamespace$Probe", bytes);
        Class<?> c2 = l2.define("UserClassLoaderNamespace$Probe", bytes);

        check(c1 != c2, "two loaders produce distinct Class objects");
        check(c1.getClassLoader() == l1 && c2.getClassLoader() == l2,
                "each Class is bound to its defining loader");

        Object o = c1.getDeclaredConstructor().newInstance();
        check(!c2.isInstance(o),
                "an object is not an instance of the other loader's class");

        check(c1.getField("value").getInt(null) == 42,
                "the field defined from the bytes is intact");

        // Parent delegation: a child of the system loader resolves platform
        // classes through its parent.
        ClassLoader child = new ClassLoader(
                UserClassLoaderNamespace.class.getClassLoader()) { };
        check(child.loadClass("java.lang.String") == String.class,
                "child loader delegates to its parent");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
