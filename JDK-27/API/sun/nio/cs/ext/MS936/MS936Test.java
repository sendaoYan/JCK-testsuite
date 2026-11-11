/*
 * @test
 * @summary sun.nio.cs.ext.MS936 / sun.nio.cs.MS936 conformance: internal charset class; package is build-dependent (probed in both).
 * @modules java.base/sun.nio.cs:+open jdk.charsets/sun.nio.cs.ext:+open
 * @run main MS936Test
 */
public class MS936Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    
    static Class<?> load(String base) throws Exception {
        for (String pkg : new String[]{"sun.nio.cs.", "sun.nio.cs.ext."}) {
            try { return Class.forName(pkg + base, false, MS936Test.class.getClassLoader()); }
            catch (ClassNotFoundException ignored) { }
        }
        throw new ClassNotFoundException("sun.nio.cs." + base + " / sun.nio.cs.ext." + base);
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = load("MS936");
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == false, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.charset.Charset"), "superclass");
        check(c.getInterfaces().length == 1, "1 direct interfaces");
        check(c.getDeclaredMethods().length == 4, "4 declared methods");
        check(c.getDeclaredFields().length == 0, "0 declared fields");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
