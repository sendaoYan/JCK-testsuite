/*
 * @test
 * @summary sun.net.ext.ExtendedSocketOptions conformance: internal package reflection.
 * @modules java.base/sun.net.ext:+open
 * @run main ExtendedSocketOptionsTest
 */
public class ExtendedSocketOptionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.ext.ExtendedSocketOptions", false, ExtendedSocketOptionsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "clientSocketOptions"), "method clientSocketOptions");
        check(hasMethod(c, "datagramSocketOptions"), "method datagramSocketOptions");
        check(hasMethod(c, "getInstance"), "method getInstance");
        check(hasMethod(c, "getOption"), "method getOption");
        check(hasMethod(c, "isOptionSupported"), "method isOptionSupported");
        check(hasMethod(c, "options"), "method options");
        check(hasMethod(c, "register"), "method register");
        check(hasMethod(c, "serverSocketOptions"), "method serverSocketOptions");
        check(hasMethod(c, "setOption"), "method setOption");
        check(hasMethod(c, "unixDomainSocketOptions"), "method unixDomainSocketOptions");
        check(hasField(c, "SOCK_DGRAM"), "field SOCK_DGRAM");
        check(hasField(c, "SOCK_STREAM"), "field SOCK_STREAM");
        check(hasField(c, "clientStreamOptions"), "field clientStreamOptions");
        check(hasField(c, "datagramOptions"), "field datagramOptions");
        check(hasField(c, "instance"), "field instance");
        check(hasField(c, "options"), "field options");
        check(hasField(c, "serverStreamOptions"), "field serverStreamOptions");
        check(hasField(c, "unixDomainClientOptions"), "field unixDomainClientOptions");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
