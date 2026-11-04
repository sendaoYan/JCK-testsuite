/*
 * @test
 * @summary sun.jvm.hotspot.debugger.windbg.AddressDataSource conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.windbg:+open
 * @run main AddressDataSourceTest
 */
import java.lang.reflect.*;

public class AddressDataSourceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.windbg.AddressDataSource", false, AddressDataSourceTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.DataSource"), "interface sun.jvm.hotspot.debugger.DataSource");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "readInt"), "method readInt");
        check(hasMethod(c, "read"), "method read");
        check(hasMethod(c, "readLong"), "method readLong");
        check(hasMethod(c, "readByte"), "method readByte");
        check(hasMethod(c, "readShort"), "method readShort");
        check(hasMethod(c, "getFilePointer"), "method getFilePointer");
        check(hasMethod(c, "seek"), "method seek");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
