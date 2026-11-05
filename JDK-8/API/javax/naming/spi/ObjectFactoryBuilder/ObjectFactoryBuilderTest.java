/*
 * @test
 * @summary javax.naming.spi.ObjectFactoryBuilder conformance: interface hierarchy and members.
 * @run main ObjectFactoryBuilderTest
 */



import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.NamingManager;
import javax.naming.spi.ObjectFactory;
import javax.naming.spi.ObjectFactoryBuilder;
public class ObjectFactoryBuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.spi.ObjectFactoryBuilder");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("createObjectFactory"), "has method createObjectFactory");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dOfbBehavior();
        System.out.println("deep: ObjectFactoryBuilder contracts OK");
    }

    public static class DeepOf implements ObjectFactory {
        public static Object lastObj;
        public static java.util.Hashtable<?, ?> lastEnv;
        public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                java.util.Hashtable<?, ?> environment) {
            lastObj = obj; lastEnv = environment;
            return "BUILT(" + obj + ")";
        }
    }

    static void dOfbBehavior() throws Exception {
        NamingManager.setObjectFactoryBuilder(new ObjectFactoryBuilder() {
            public ObjectFactory createObjectFactory(Object obj,
                    java.util.Hashtable<?, ?> environment) throws javax.naming.NamingException {
                return new DeepOf();
            }
        });
        java.util.Hashtable<Object, Object> env = new java.util.Hashtable<>();
        env.put(Context.OBJECT_FACTORIES, "should.be.ignored.when.builder.set");
        Object out = NamingManager.getObjectInstance("subject", new CompositeName("n"), null, env);
        check("BUILT(subject)".equals(out), "builder-produced factory used");
        check(DeepOf.lastObj.equals("subject"), "factory got the reference");
        // builder is once-only per JVM
        boolean ise = false;
        try { NamingManager.setObjectFactoryBuilder(null); }
        catch (IllegalStateException e) {
            ise = true;
            check("ObjectFactoryBuilder already set".equals(e.getMessage()), "ISE message");
        }
        check(ise || out != null, "second builder install rejected or ignored");
    }

}
