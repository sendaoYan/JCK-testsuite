/*
 * @test
 * @summary sun.security.jgss.GSSToken conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.jgss:+open
 * @run main GSSTokenTest
 */
import java.lang.reflect.*;
import java.util.*;
public class GSSTokenTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.GSSToken", false, GSSTokenTest.class.getClassLoader());
        check(c.getName().equals("sun.security.jgss.GSSToken"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getHexBytes"), "method:getHexBytes");
        check(methods.contains("readBigEndian"), "method:readBigEndian");
        check(methods.contains("readFully"), "method:readFully");
        check(methods.contains("readInt"), "method:readInt");
        check(methods.contains("readLittleEndian"), "method:readLittleEndian");
        check(methods.contains("writeBigEndian"), "method:writeBigEndian");
        check(methods.contains("writeInt"), "method:writeInt");
        check(methods.contains("writeLittleEndian"), "method:writeLittleEndian");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
