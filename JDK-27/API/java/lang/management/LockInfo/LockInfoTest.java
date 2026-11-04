/*
 * @test
 * @summary java.lang.management.LockInfo conformance: white-box structure + black-box behavior.
 * @run main LockInfoTest
 */
import java.lang.management.LockInfo;
import java.lang.reflect.Modifier;

public class LockInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.management.LockInfo");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(c.getConstructor(String.class, int.class) != null, "constructor (String,int)");
        check(c.getMethod("getClassName").getReturnType() == String.class, "getClassName returns String");
        check(c.getMethod("getIdentityHashCode").getReturnType() == int.class, "getIdentityHashCode returns int");
        check(c.getMethod("from", javax.management.openmbean.CompositeData.class).getReturnType() == LockInfo.class,
              "from returns LockInfo");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        LockInfo li = new LockInfo("java.lang.String", 42);
        check("java.lang.String".equals(li.getClassName()), "getClassName");
        check(li.getIdentityHashCode() == 42, "getIdentityHashCode");
        check(li.toString().contains("java.lang.String"), "toString contains className");

        LockInfo zero = new LockInfo("java.util.List", 0);
        check(zero.getIdentityHashCode() == 0, "identity hash 0");
        check("java.util.List".equals(zero.getClassName()), "getClassName second");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
