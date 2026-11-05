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
import javax.naming.CompositeName;
import javax.naming.directory.BasicAttribute;
import javax.naming.spi.DirectoryManager;
import javax.naming.spi.ObjectFactory;

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
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dDofBehavior();
        dDofViaDirectoryManager();
        System.out.println("deep: DirObjectFactory contracts OK");
    }

    public static class DeepDof implements DirObjectFactory {
        static Object lastObj;
        static Name lastName;
        static Context lastCtx;
        static java.util.Hashtable<?, ?> lastEnv;
        static Attributes lastAttrs;
        public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                java.util.Hashtable<?, ?> environment, Attributes attrs) {
            lastObj = obj; lastName = name; lastCtx = nameCtx;
            lastEnv = environment; lastAttrs = attrs;
            return "DO(" + obj + ")";
        }
        public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                java.util.Hashtable<?, ?> environment) {
            return getObjectInstance(obj, name, nameCtx, environment, null);
        }
    }

    static class DeepDofNull implements DirObjectFactory {
        public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                java.util.Hashtable<?, ?> environment, Attributes attrs) {
            return null;
        }
        public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                java.util.Hashtable<?, ?> environment) {
            return getObjectInstance(obj, name, nameCtx, environment, null);
        }
    }

    static void dDofBehavior() throws Exception {
        DeepDof f = new DeepDof();
        Name n = new CompositeName("a/b");
        java.util.Hashtable<Object, Object> env = new java.util.Hashtable<>();
        BasicAttributes at = new BasicAttributes();
        at.put(new BasicAttribute("cn", "x"));
        check("DO(t)".equals(f.getObjectInstance("t", n, null, env, at)), "direct call");
        check(DeepDof.lastName.equals(n), "name recorded");
        check(DeepDof.lastEnv == env, "environment recorded");
        check(DeepDof.lastAttrs == at, "attributes recorded");
        // null attributes are legal input
        f.getObjectInstance("t2", null, null, null, null);
        check(DeepDof.lastAttrs == null, "null attrs legal");
        // null return means "not handled"
        check(new DeepDofNull().getObjectInstance("x", null, null, null, null) == null,
                "null return allowed");
    }

    static void dDofViaDirectoryManager() throws Exception {
        // OBJECT_FACTORIES routes through DirectoryManager to the DirObjectFactory
        java.util.Hashtable<Object, Object> env = new java.util.Hashtable<>();
        env.put(Context.OBJECT_FACTORIES, "DirObjectFactoryTest$DeepDof");
        BasicAttributes at = new BasicAttributes();
        at.put(new BasicAttribute("k", "v"));
        Object out = DirectoryManager.getObjectInstance("subj", new CompositeName("n"), null, env, at);
        check("DO(subj)".equals(out), "DirectoryManager delegation");
        check(DeepDof.lastAttrs == at, "attrs forwarded via DirectoryManager");
    }

}
