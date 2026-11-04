/*
 * @test
 * @summary sun.net.ProgressSource.State conformance: enum constants and members.
 * @run main StateTest
 */

public class StateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.ProgressSource$State", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.ProgressSource$State"), "binary name");
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
        check(consts.contains("NEW"), "enum constant NEW");
        check(consts.contains("CONNECTED"), "enum constant CONNECTED");
        check(consts.contains("UPDATE"), "enum constant UPDATE");
        check(consts.contains("DELETE"), "enum constant DELETE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
