/*
 * @test
 * @summary sun.net.httpserver.HttpConnection.State conformance: enum constants and members.
 * @run main StateTest
 */

public class StateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.httpserver.HttpConnection$State", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.httpserver.HttpConnection$State"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("valueOf"), "declares method valueOf");
        check(mnames.contains("values"), "declares method values");
        java.util.Set<String> consts = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) consts.add(f.getName());
        check(consts.contains("IDLE"), "enum constant IDLE");
        check(consts.contains("REQUEST"), "enum constant REQUEST");
        check(consts.contains("RESPONSE"), "enum constant RESPONSE");
        check(consts.contains("NEWLY_ACCEPTED"), "enum constant NEWLY_ACCEPTED");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
