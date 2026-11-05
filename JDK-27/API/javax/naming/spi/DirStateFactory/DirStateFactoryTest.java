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
import javax.naming.CompositeName;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;
import javax.naming.spi.DirectoryManager;
import javax.naming.spi.StateFactory;

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
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dDsfResultValue();
        dDsfDirect();
        dDsfViaDirectoryManager();
        System.out.println("deep: DirStateFactory contracts OK");
    }

    public static class DeepDsf implements DirStateFactory {
        static Object lastObj;
        static Attributes lastAttrs;
        public Result getStateToBind(Object obj, Name name, Context nameCtx,
                java.util.Hashtable<?, ?> environment, Attributes inAttrs) {
            lastObj = obj; lastAttrs = inAttrs;
            return new Result("S(" + obj + ")", inAttrs);
        }
        public Object getStateToBind(Object obj, Name name, Context nameCtx,
                java.util.Hashtable<?, ?> environment) {
            return getStateToBind(obj, name, nameCtx, environment, null);
        }
    }

    static void dDsfResultValue() {
        // DirStateFactory.Result is a plain value holder
        BasicAttributes at = new BasicAttributes();
        at.put(new BasicAttribute("cn", "v"));
        DirStateFactory.Result r = new DirStateFactory.Result("obj", at);
        check("obj".equals(r.getObject()), "Result.getObject");
        check(r.getAttributes() == at, "Result.getAttributes identity");
        // null object and null attributes are both permitted
        DirStateFactory.Result n = new DirStateFactory.Result(null, null);
        check(n.getObject() == null, "null object allowed");
        check(n.getAttributes() == null, "null attrs allowed");
        check(r.equals(r) && !r.equals(null), "identity semantics only");
    }

    static void dDsfDirect() throws Exception {
        DeepDsf f = new DeepDsf();
        BasicAttributes at = new BasicAttributes();
        at.put(new BasicAttribute("k", "v"));
        DirStateFactory.Result r = f.getStateToBind("o", new CompositeName("n"), null, null, at);
        check("S(o)".equals(r.getObject()), "direct call result");
        check(r.getAttributes() == at, "attrs echoed");
        check(DeepDsf.lastObj.equals("o"), "object recorded");
        check(DeepDsf.lastAttrs == at, "attrs recorded");
    }

    static void dDsfViaDirectoryManager() throws Exception {
        java.util.Hashtable<Object, Object> env = new java.util.Hashtable<>();
        env.put(Context.STATE_FACTORIES, "DirStateFactoryTest$DeepDsf");
        BasicAttributes at = new BasicAttributes();
        DirStateFactory.Result r = DirectoryManager.getStateToBind("obj", null, null, env, at);
        check("S(obj)".equals(r.getObject()), "DirectoryManager uses DirStateFactory");
        check(r.getAttributes() == at, "DirectoryManager keeps factory attrs");
        // STATE_FACTORIES entry that is a plain StateFactory still applies (inherited method)
        env = new java.util.Hashtable<>();
        env.put(Context.STATE_FACTORIES, "DirStateFactoryTest$DeepDsf");
        check(DirectoryManager.getStateToBind("x", null, null, env, null).getObject() != null,
                "state path always yields a Result");
    }

}
