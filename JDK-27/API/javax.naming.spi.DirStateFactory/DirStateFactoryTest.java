/*
 * @test
 * @summary javax.naming.spi.DirStateFactory conformance: white-box structure + black-box behavior.
 * @run main DirStateFactoryTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.spi.DirStateFactory;

public class DirStateFactoryTest {
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
        Class<?> c = Class.forName("javax.naming.spi.DirStateFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");

        boolean ext = false;
        for (Class<?> itf : c.getInterfaces())
            if (itf.getName().equals("javax.naming.spi.StateFactory")) ext = true;
        check(ext, "extends StateFactory");

        check(hasMethod(c, "getStateToBind", Object.class, Name.class, Context.class,
                Hashtable.class, Attributes.class), "getStateToBind(Object,Name,Context,Hashtable,Attributes)");
        check(hasMethod(c, "getStateToBind", Object.class, Name.class, Context.class,
                Hashtable.class), "getStateToBind(Object,Name,Context,Hashtable) inherited");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final boolean[] fiveArgCalled = new boolean[1];
        final boolean[] fourArgCalled = new boolean[1];
        DirStateFactory factory = new DirStateFactory() {
            public Object getStateToBind(Object obj, Name name, Context nameCtx,
                                        Hashtable<?, ?> environment) throws javax.naming.NamingException {
                fourArgCalled[0] = true;
                return "state:" + obj;
            }
            public DirStateFactory.Result getStateToBind(Object obj, Name name, Context nameCtx,
                                        Hashtable<?, ?> environment, Attributes attrs)
                                        throws javax.naming.NamingException {
                fiveArgCalled[0] = true;
                return new DirStateFactory.Result("result:" + obj, attrs);
            }
        };

        Attributes attrs = new BasicAttributes("ou", "dev");
        DirStateFactory.Result r5 = factory.getStateToBind("x", null, null, null, attrs);
        check(fiveArgCalled[0], "5-arg getStateToBind invoked");
        check("result:x".equals(r5.getObject()), "5-arg result object");
        check(r5.getAttributes() == attrs, "5-arg result attributes");

        Object r4 = factory.getStateToBind("x", null, null, null);
        check(fourArgCalled[0], "4-arg getStateToBind invoked");
        check("state:x".equals(r4), "4-arg result");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
