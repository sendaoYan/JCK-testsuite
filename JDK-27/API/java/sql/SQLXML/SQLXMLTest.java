/*
 * @test
 * @summary java.sql.SQLXML conformance: white-box structure + black-box behavior.
 * @run main SQLXMLTest
 */
public class SQLXMLTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface Throwing { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> t, String label, Throwing r) {
        try { r.run(); } catch (Throwable x) { check(t.isInstance(x), label + " throws " + t.getSimpleName()); return; }
        throw new AssertionError(label + ": expected " + t.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.sql.SQLXML");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("java.lang.AutoCloseable"), "superinterface 0");
        check(c.getDeclaredMethods().length == 10, "declared method count 10");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> called = new java.util.ArrayList<String>();
        java.sql.SQLXML sx = (java.sql.SQLXML) java.lang.reflect.Proxy.newProxyInstance(
            java.sql.SQLXML.class.getClassLoader(),
            new Class<?>[] { java.sql.SQLXML.class },
            new java.lang.reflect.InvocationHandler() {
                public Object invoke(Object proxy, java.lang.reflect.Method m, Object[] args) {
                    called.add(m.getName() + "/" + m.getParameterCount());
                    if (m.getName().equals("getString")) return "<a>xml</a>";
                    return null; // void methods and reference-returning streams/sources are null-shaped
                }
            });

        check("<a>xml</a>".equals(sx.getString()), "getString");
        check(sx.getBinaryStream() == null, "getBinaryStream shape null");
        check(sx.getCharacterStream() == null, "getCharacterStream shape null");
        check(sx.getSource(javax.xml.transform.Source.class) == null, "getSource shape null");
        check(sx.setBinaryStream() == null, "setBinaryStream shape null");
        check(sx.setCharacterStream() == null, "setCharacterStream shape null");
        check(sx.setResult(javax.xml.transform.Result.class) == null, "setResult shape null");
        sx.setString("<b/>");
        sx.free();
        sx.close();
        check(true, "setString/free/close callable");

        check(called.size() == 10, "all 10 interface methods dispatched");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
