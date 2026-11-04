/*
 * @test
 * @summary sun.jvm.hotspot.debugger.DataSource conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger:+open
 * @run main DataSourceTest
 */
import java.lang.reflect.*;

public class DataSourceTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.DataSource", false, DataSourceTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
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
