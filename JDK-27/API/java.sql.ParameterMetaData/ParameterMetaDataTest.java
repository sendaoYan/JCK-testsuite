/*
 * @test
 * @summary java.sql.ParameterMetaData conformance: white-box structure + black-box behavior.
 * @run main ParameterMetaDataTest
 */
public class ParameterMetaDataTest {
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
        Class<?> c = Class.forName("java.sql.ParameterMetaData");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("java.sql.Wrapper"), "superinterface 0");
        check(c.getDeclaredMethods().length == 9, "declared method count 9");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> called = new java.util.ArrayList<String>();
        java.sql.ParameterMetaData pmd = (java.sql.ParameterMetaData) java.lang.reflect.Proxy.newProxyInstance(
            java.sql.ParameterMetaData.class.getClassLoader(),
            new Class<?>[] { java.sql.ParameterMetaData.class },
            new java.lang.reflect.InvocationHandler() {
                public Object invoke(Object proxy, java.lang.reflect.Method m, Object[] args) {
                    called.add(m.getName() + ":" + ((args == null || args.length == 0) ? "-" : args[0]));
                    String n = m.getName();
                    if (n.equals("getParameterCount")) return Integer.valueOf(3);
                    if (n.equals("getParameterClassName")) return "java.lang.String";
                    if (n.equals("getParameterMode")) return Integer.valueOf(java.sql.ParameterMetaData.parameterModeIn);
                    if (n.equals("getParameterType")) return Integer.valueOf(java.sql.Types.VARCHAR);
                    if (n.equals("getParameterTypeName")) return "VARCHAR";
                    if (n.equals("getPrecision")) return Integer.valueOf(20);
                    if (n.equals("getScale")) return Integer.valueOf(2);
                    if (n.equals("isNullable")) return Integer.valueOf(java.sql.ParameterMetaData.parameterNullable);
                    if (n.equals("isSigned")) return Boolean.TRUE;
                    throw new AssertionError("unexpected method " + m);
                }
            });

        check(pmd.getParameterCount() == 3, "getParameterCount");
        check("java.lang.String".equals(pmd.getParameterClassName(1)), "getParameterClassName");
        check(pmd.getParameterMode(1) == java.sql.ParameterMetaData.parameterModeIn, "getParameterMode");
        check(pmd.getParameterType(1) == java.sql.Types.VARCHAR, "getParameterType");
        check("VARCHAR".equals(pmd.getParameterTypeName(1)), "getParameterTypeName");
        check(pmd.getPrecision(1) == 20, "getPrecision");
        check(pmd.getScale(1) == 2, "getScale");
        check(pmd.isNullable(1) == java.sql.ParameterMetaData.parameterNullable, "isNullable");
        check(pmd.isSigned(1), "isSigned");

        check(called.size() == 9, "all 9 interface methods dispatched");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
