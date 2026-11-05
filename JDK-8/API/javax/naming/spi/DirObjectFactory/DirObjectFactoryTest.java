/*
 * @test
 * @summary javax.naming.spi.DirObjectFactory conformance: interface hierarchy and members.
 * @run main DirObjectFactoryTest
 */



import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.spi.DirObjectFactory;
import javax.naming.spi.DirectoryManager;
import javax.naming.spi.ObjectFactory;
public class DirObjectFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.spi.DirObjectFactory");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("javax.naming.spi.ObjectFactory");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getObjectInstance"), "has method getObjectInstance");        testDeepContracts();
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
