/*
 * @test
 * @summary java.security.Provider.Service conformance: white-box structure + black-box behavior.
 * @run main ServiceTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.Provider;
import java.security.Security;
import java.util.List;
import java.util.Map;

public class ServiceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.Provider$Service");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        checkMethods(c, new String[]{"getType", "getAlgorithm", "getProvider", "getClassName", "getAttribute", "newInstance", "supportsParameter", "toString"});
    }

    static void testBlackBox() throws Exception {
        Provider sun = Security.getProvider("SUN");
        check(sun != null, "SUN provider present");
        Provider.Service s = sun.getService("MessageDigest", "SHA-256");
        check(s != null, "MessageDigest SHA-256 service");
        check("MessageDigest".equals(s.getType()), "getType");
        check("SHA-256".equals(s.getAlgorithm()), "getAlgorithm");
        check(s.getClassName() != null, "getClassName");
        check(sun.equals(s.getProvider()), "getProvider");
        check(s.getAttribute("NotAnAttribute") == null, "getAttribute unknown null");
        check(s.toString() != null && s.toString().contains("SHA-256"), "toString");

        Provider.Service custom = new Provider.Service(sun, "MessageDigest", "MyAlg", "com.example.MyDigest", (List<String>) null, (Map<String, String>) null);
        check("MessageDigest".equals(custom.getType()), "constructed getType");
        check("MyAlg".equals(custom.getAlgorithm()), "constructed getAlgorithm");
        check("com.example.MyDigest".equals(custom.getClassName()), "constructed getClassName");
        check(sun.equals(custom.getProvider()), "constructed getProvider");
        check(custom.getAttribute("x") == null, "constructed getAttribute null");
        check(custom.toString() != null && custom.toString().contains("MyAlg"), "constructed toString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static void checkEnumConstants(Class<?> c, String[] names) { Object[] cs = c.getEnumConstants(); check(cs != null, "enum constants"); check(cs.length == names.length, "enum constant count"); for (String n : names) check(hasConst(cs, n), "constant " + n); }
    static boolean hasConst(Object[] cs, String name) { for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true; return false; }
}
