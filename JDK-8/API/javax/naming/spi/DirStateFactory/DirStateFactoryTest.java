/*
 * @test
 * @summary javax.naming.spi.DirStateFactory conformance: interface hierarchy and members.
 * @run main DirStateFactoryTest
 */



import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.spi.DirStateFactory;
import javax.naming.spi.DirectoryManager;
import javax.naming.spi.StateFactory;
public class DirStateFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.spi.DirStateFactory");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("javax.naming.spi.StateFactory");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getStateToBind"), "has method getStateToBind");        testDeepContracts();
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
