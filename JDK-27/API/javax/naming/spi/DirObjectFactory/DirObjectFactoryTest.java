/*
 * @test
 * @summary javax.naming.spi.DirObjectFactory conformance: white-box structure + black-box behavior.
 * @run main DirObjectFactoryTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.spi.DirObjectFactory;

public class DirObjectFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.spi.DirObjectFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");

        boolean ext = false;
        for (Class<?> itf : c.getInterfaces())
            if (itf.getName().equals("javax.naming.spi.ObjectFactory")) ext = true;
        check(ext, "extends ObjectFactory");

        check(hasMethod(c, "getObjectInstance", Object.class, Name.class, Context.class,
                Hashtable.class, Attributes.class), "getObjectInstance(Object,Name,Context,Hashtable,Attributes)");
        check(hasMethod(c, "getObjectInstance", Object.class, Name.class, Context.class,
                Hashtable.class), "getObjectInstance(Object,Name,Context,Hashtable) inherited");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Anonymous implementation proves both overloads are independently callable.
        final boolean[] fiveArgCalled = new boolean[1];
        final boolean[] fourArgCalled = new boolean[1];
        DirObjectFactory factory = new DirObjectFactory() {
            public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                           Hashtable<?, ?> environment) throws Exception {
                fourArgCalled[0] = true;
                return "four:" + obj;
            }
            public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                           Hashtable<?, ?> environment, Attributes attrs) throws Exception {
                fiveArgCalled[0] = true;
                return "five:" + obj;
            }
        };

        Attributes attrs = new BasicAttributes("cn", "v");
        Object r5 = factory.getObjectInstance("x", null, null, null, attrs);
        check(fiveArgCalled[0], "5-arg getObjectInstance invoked");
        check("five:x".equals(r5), "5-arg result");

        Object r4 = factory.getObjectInstance("x", null, null, null);
        check(fourArgCalled[0], "4-arg getObjectInstance invoked");
        check("four:x".equals(r4), "4-arg result");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
