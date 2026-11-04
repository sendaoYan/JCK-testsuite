/*
 * @test
 * @summary jdk.internal.net.quic.QuicVersion conformance: internal package reflection.
 * @modules java.base/jdk.internal.net.quic:+open
 * @run main QuicVersionTest
 */
public class QuicVersionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.quic.QuicVersion", false, QuicVersionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "firstFlightVersion"), "method firstFlightVersion");
        check(hasMethod(c, "of"), "method of");
        check(hasMethod(c, "valueOf"), "method valueOf");
        check(hasMethod(c, "values"), "method values");
        check(hasMethod(c, "versionNumber"), "method versionNumber");
        check(hasField(c, "$VALUES"), "field $VALUES");
        check(hasField(c, "QUIC_V1"), "field QUIC_V1");
        check(hasField(c, "QUIC_V2"), "field QUIC_V2");
        check(hasField(c, "versionNumber"), "field versionNumber");
        check(hasEnumConstant(c, "QUIC_V1"), "enum QUIC_V1");
        check(hasEnumConstant(c, "QUIC_V2"), "enum QUIC_V2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
