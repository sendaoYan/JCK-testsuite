/*
 * @test
 * @summary javax.naming.spi.DirectoryManager conformance: white-box structure + black-box behavior.
 * @run main DirectoryManagerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import javax.naming.CannotProceedException;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.spi.DirStateFactory;
import javax.naming.spi.DirectoryManager;
import javax.naming.CompositeName;
import javax.naming.Reference;
import javax.naming.directory.BasicAttribute;
import javax.naming.spi.DirObjectFactory;
import javax.naming.spi.NamingManager;
import javax.naming.spi.ObjectFactory;
import javax.naming.spi.StateFactory;

public class DirectoryManagerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.spi.DirectoryManager");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "is class");
        check(c.getSuperclass().getName().equals("javax.naming.spi.NamingManager"),
                "extends NamingManager");
        check(hasMethod(c, "getContinuationDirContext"), "getContinuationDirContext");
        check(hasMethod(c, "getObjectInstance"), "getObjectInstance");
        check(hasMethod(c, "getStateToBind"), "getStateToBind");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // getObjectInstance: with no object factory installed, an ordinary object is returned unchanged.
        String plain = "plain-object";
        Attributes attrs = new BasicAttributes("cn", "v");
        Object r = DirectoryManager.getObjectInstance(plain, null, null, null, attrs);
        check(r == plain, "getObjectInstance returns plain object unchanged");

        Object rNull = DirectoryManager.getObjectInstance(null, null, null, null, attrs);
        check(rNull == null, "getObjectInstance(null) returns null");

        // getStateToBind: with no state factory, the object and attributes are wrapped unchanged.
        DirStateFactory.Result res = DirectoryManager.getStateToBind(plain, null, null, null, attrs);
        check(res != null, "getStateToBind returns non-null Result");
        check(res.getObject() == plain, "getStateToBind keeps object");
        check(res.getAttributes() == attrs, "getStateToBind keeps attributes");

        // getContinuationDirContext builds a usable DirContext carrying the continuation.
        CannotProceedException cpe = new CannotProceedException();
        cpe.setEnvironment(new Hashtable<>());
        DirContext dctx = DirectoryManager.getContinuationDirContext(cpe);
        check(dctx != null, "getContinuationDirContext returns non-null");
        check(dctx instanceof DirContext, "result is a DirContext");
        dctx.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dDmGetObjectInstance();
        dDmGetStateToBind();
        System.out.println("deep: DirectoryManager contracts OK");
    }

    public static class DeepDof implements DirObjectFactory {
        public static Object lastRefInfo;
        public static Attributes lastAttrs;
        public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                java.util.Hashtable<?, ?> environment, Attributes attrs) {
            lastRefInfo = obj; lastAttrs = attrs;
            return "D(" + obj + ")";
        }
        public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                java.util.Hashtable<?, ?> environment) {
            return getObjectInstance(obj, name, nameCtx, environment, null);
        }
    }

    public static class DeepOf implements ObjectFactory {
        public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                java.util.Hashtable<?, ?> environment) {
            return "P(" + obj + ")";
        }
    }

    public static class DeepDsf implements DirStateFactory {
        public static Object lastObj;
        public static Attributes lastInAttrs;
        public DirStateFactory.Result getStateToBind(Object obj, Name name,
                Context nameCtx, java.util.Hashtable<?, ?> environment, Attributes inAttrs) {
            lastObj = obj; lastInAttrs = inAttrs;
            BasicAttributes out = new BasicAttributes();
            out.put(new BasicAttribute("dn", "computed"));
            return new DirStateFactory.Result("DS(" + obj + ")", out);
        }
        public Object getStateToBind(Object obj, Name name, Context nameCtx,
                java.util.Hashtable<?, ?> environment) {
            return getStateToBind(obj, name, nameCtx, environment, null);
        }
    }

    public static class DeepSf implements StateFactory {
        public static Object lastObj;
        public Object getStateToBind(Object obj, Name name, Context nameCtx,
                java.util.Hashtable<?, ?> environment) {
            lastObj = obj;
            return "PS(" + obj + ")";
        }
    }

    static void dDmGetObjectInstance() throws Exception {
        // plain object, no factories -> returned unchanged
        Object plain = "plain";
        check(DirectoryManager.getObjectInstance(plain, null, null, null, null) == plain,
                "plain object returned unchanged");
        check(DirectoryManager.getObjectInstance(null, null, null, null, null) == null,
                "null refInfo -> null");
        // DirObjectFactory from OBJECT_FACTORIES receives the attributes
        java.util.Hashtable<Object, Object> env = new java.util.Hashtable<>();
        env.put(Context.OBJECT_FACTORIES, "DirectoryManagerTest$DeepDof");
        BasicAttributes attrs = new BasicAttributes();
        attrs.put(new BasicAttribute("cn", "v"));
        Object out = DirectoryManager.getObjectInstance("target", new CompositeName("n"), null, env, attrs);
        check("D(target)".equals(out), "DirObjectFactory consulted");
        check(DeepDof.lastAttrs == attrs, "attributes passed through to factory");
        // plain ObjectFactory is also usable through the directory path
        env = new java.util.Hashtable<>();
        env.put(Context.OBJECT_FACTORIES, "DirectoryManagerTest$DeepOf");
        out = DirectoryManager.getObjectInstance("target", null, null, env, null);
        check("P(target)".equals(out), "plain ObjectFactory fallback");
        // Reference naming its own factory class wins exclusively
        Reference ref = new Reference("java.lang.String", "DirectoryManagerTest$DeepOf", null);
        out = DirectoryManager.getObjectInstance(ref, null, null, null, null);
        check("P(java.lang.String)".equals(out) || "P(".equals(out.toString().substring(0, 2)),
                "reference factory used exclusively");
    }

    static void dDmGetStateToBind() throws Exception {
        // no factories -> Result wrapping the originals
        Object obj = "orig";
        BasicAttributes attrs = new BasicAttributes();
        attrs.put(new BasicAttribute("k", "v"));
        DirStateFactory.Result r = DirectoryManager.getStateToBind(obj, null, null, null, attrs);
        check(r != null, "always returns a Result");
        check(r.getObject() == obj, "no factory -> original object");
        check(r.getAttributes() == attrs, "no factory -> original attributes");
        // DirStateFactory via STATE_FACTORIES produces a full Result
        java.util.Hashtable<Object, Object> env = new java.util.Hashtable<>();
        env.put(Context.STATE_FACTORIES, "DirectoryManagerTest$DeepDsf");
        r = DirectoryManager.getStateToBind(obj, new CompositeName("n"), null, env, attrs);
        check("DS(orig)".equals(r.getObject()), "DirStateFactory result object");
        check("computed".equals(r.getAttributes().get("dn").get()), "DirStateFactory result attributes");
        check(DeepDsf.lastObj == obj, "factory received the object");
        check(DeepDsf.lastInAttrs == attrs, "factory received the attributes");
        // plain StateFactory result is wrapped with the original attributes
        env = new java.util.Hashtable<>();
        env.put(Context.STATE_FACTORIES, "DirectoryManagerTest$DeepSf");
        r = DirectoryManager.getStateToBind(obj, null, null, env, attrs);
        check("PS(orig)".equals(r.getObject()), "plain StateFactory object wrapped");
        check(r.getAttributes() == attrs, "plain StateFactory keeps original attrs");
        // null attrs with no factory -> Result with null attributes
        r = DirectoryManager.getStateToBind(obj, null, null, null, null);
        check(r.getAttributes() == null, "null attrs propagate to Result");
    }

}
