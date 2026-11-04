/*
 * @test
 * @summary sun.invoke.util.Wrapper conformance: enum constants and members.
 * @run main WrapperTest
 */

public class WrapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.invoke.util.Wrapper", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.invoke.util.Wrapper"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("arrayType"), "declares method arrayType");
        check(mnames.contains("asPrimitiveType"), "declares method asPrimitiveType");
        check(mnames.contains("asWrapperType"), "declares method asWrapperType");
        check(mnames.contains("basicTypeChar"), "declares method basicTypeChar");
        check(mnames.contains("bitWidth"), "declares method bitWidth");
        check(mnames.contains("boolValue"), "declares method boolValue");
        java.util.Set<String> consts = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) consts.add(f.getName());
        check(consts.contains("BOOLEAN"), "enum constant BOOLEAN");
        check(consts.contains("BYTE"), "enum constant BYTE");
        check(consts.contains("SHORT"), "enum constant SHORT");
        check(consts.contains("CHAR"), "enum constant CHAR");
        check(consts.contains("INT"), "enum constant INT");
        check(consts.contains("LONG"), "enum constant LONG");
        check(consts.contains("FLOAT"), "enum constant FLOAT");
        check(consts.contains("DOUBLE"), "enum constant DOUBLE");
        check(consts.contains("OBJECT"), "enum constant OBJECT");
        check(consts.contains("VOID"), "enum constant VOID");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
