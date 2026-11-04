/*
 * @test
 * @summary com.sun.security.ntlm.Client conformance: internal package reflection.
 * @modules java.base/com.sun.security.ntlm:+open
 * @run main ClientTest
 */
public class ClientTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.security.ntlm.Client", false, ClientTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.security.ntlm.NTLM"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "debug"), "method debug");
        check(hasMethod(c, "dispose"), "method dispose");
        check(hasMethod(c, "getDomain"), "method getDomain");
        check(hasMethod(c, "type1"), "method type1");
        check(hasMethod(c, "type3"), "method type3");
        check(hasField(c, "domain"), "field domain");
        check(hasField(c, "hostname"), "field hostname");
        check(hasField(c, "pw1"), "field pw1");
        check(hasField(c, "pw2"), "field pw2");
        check(hasField(c, "username"), "field username");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
