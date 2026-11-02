/*
 * @test
 * @summary sun.net.www.protocol.http.AuthenticationHeader conformance: class and members.
 * @run main AuthenticationHeaderTest
 */

public class AuthenticationHeaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.http.AuthenticationHeader", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.protocol.http.AuthenticationHeader"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getHttpCallerInfo"), "declares method getHttpCallerInfo");
        check(mnames.contains("headerParser"), "declares method headerParser");
        check(mnames.contains("isPresent"), "declares method isPresent");
        check(mnames.contains("parse"), "declares method parse");
        check(mnames.contains("raw"), "declares method raw");
        check(mnames.contains("scheme"), "declares method scheme");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
