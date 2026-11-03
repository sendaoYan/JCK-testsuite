/*
 * @test
 * @summary java.lang.management.MonitorInfo conformance: white-box structure + black-box behavior.
 * @run main MonitorInfoTest
 */
import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.reflect.Modifier;

public class MonitorInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.management.MonitorInfo");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass() == LockInfo.class, "extends LockInfo");
        check(c.getConstructor(String.class, int.class, int.class, StackTraceElement.class) != null,
              "constructor (String,int,int,StackTraceElement)");
        check(c.getMethod("getLockedStackDepth").getReturnType() == int.class, "getLockedStackDepth returns int");
        check(c.getMethod("getLockedStackFrame").getReturnType() == StackTraceElement.class,
              "getLockedStackFrame returns StackTraceElement");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        StackTraceElement frame = new StackTraceElement("com.example.Foo", "bar", "Foo.java", 12);
        MonitorInfo mi = new MonitorInfo("java.lang.String", 7, 3, frame);
        check(mi instanceof LockInfo, "MonitorInfo is a LockInfo");
        check("java.lang.String".equals(mi.getClassName()), "inherited getClassName");
        check(mi.getIdentityHashCode() == 7, "inherited getIdentityHashCode");
        check(mi.getLockedStackDepth() == 3, "getLockedStackDepth");
        check(mi.getLockedStackFrame() == frame, "getLockedStackFrame returns same instance");
        check("com.example.Foo".equals(mi.getLockedStackFrame().getClassName()), "frame className");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
