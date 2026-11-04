/*
 * @test
 * @summary javax.management.ObjectInstance conformance: object name + class name.
 * @run main ObjectInstanceTest
 */

import javax.management.*;
public class ObjectInstanceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    @SuppressWarnings("unchecked")
    static <T> T deepRoundTripValue(T o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        java.io.ObjectInputStream ois =
                new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        return (T) ois.readObject();
    }

    // ---- deepened (2026-11): constructor matrix / null semantics / serialization / live server ----

    static void testDeepConstructorsAndNull() throws Exception {
        ObjectName n = new ObjectName("jck.oi:type=x");
        try {
            new ObjectInstance((ObjectName) null, "c");
            check(false, "null ObjectName must throw");
        } catch (NullPointerException e) {
            passed++;
        }
        try {
            new ObjectInstance((String) null, "c");
            check(false, "null String name must throw");
        } catch (NullPointerException e) {
            passed++;
        }
        ObjectInstance s = new ObjectInstance("jck.oi2:k=v", "com.x.Y");
        check("jck.oi2".equals(s.getObjectName().getDomain()), "String ctor parses name");
        check("v".equals(s.getObjectName().getKeyProperty("k")), "String ctor key property");
        try {
            new ObjectInstance("no-colon", "c");
            check(false, "malformed String name must throw");
        } catch (MalformedObjectNameException e) {
            passed++;
        }
        // null className is accepted; equals/hashCode stay consistent
        ObjectInstance a = new ObjectInstance(n, null);
        ObjectInstance b = new ObjectInstance(new ObjectName("jck.oi:type=x"), null);
        check(a.equals(b) && b.equals(a), "null className: equals symmetric");
        check(a.hashCode() == b.hashCode(), "null className: hashCode consistent");
        check(!a.equals(new ObjectInstance(n, "c")), "null vs non-null className");
        check(!new ObjectInstance(n, "A").equals(new ObjectInstance(new ObjectName("jck.oi:type=y"), "A")),
                "different names not equal");
        check(!a.equals(null) && !a.equals("x"), "equals null / foreign type");
        check("null[jck.oi:type=x]".equals(a.toString()), "toString with null className");
        check("java.lang.String[jck.oi:type=x]".equals(new ObjectInstance(n, "java.lang.String").toString()),
                "toString format className[name]");
    }

    static void testDeepSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(ObjectInstance.class).getSerialVersionUID() == -4099952623687795850L,
                "serialVersionUID constant");
        ObjectInstance oi = new ObjectInstance(new ObjectName("jck.oi:type=ser"), "java.lang.String");
        ObjectInstance back = deepRoundTripValue(oi);
        check(oi.equals(back) && back.equals(oi), "round-trip equals");
        check("java.lang.String".equals(back.getClassName()), "round-trip class name");
        check(back.getObjectName().equals(oi.getObjectName()), "round-trip object name");
        ObjectInstance nullC = deepRoundTripValue(new ObjectInstance(new ObjectName("jck.oi:type=null"), null));
        check(nullC.getClassName() == null, "round-trip preserves null className");
    }

    public static interface ProbeMBean { int getN(); }
    public static class Probe implements ProbeMBean { public int getN() { return 7; } }

    static void testDeepLiveServer() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName n = new ObjectName("jck.oi:type=Probe");
        ObjectInstance inst = server.registerMBean(new Probe(), n);
        check(inst.getObjectName().equals(n), "registerMBean returns ObjectInstance with the given name");
        check(Probe.class.getName().equals(inst.getClassName()), "registerMBean returns exact implementation class name");
        check(server.getObjectInstance(n).equals(inst), "getObjectInstance equal");
    }

    public static void main(String[] args) throws Exception {
        ObjectInstance oi = new ObjectInstance(new ObjectName("d:type=x"), "java.lang.String");
        check("d".equals(oi.getObjectName().getDomain()), "object name");
        check("java.lang.String".equals(oi.getClassName()), "class name");
        check(oi.equals(new ObjectInstance(new ObjectName("d:type=x"), "java.lang.String")), "equals");
        testDeepConstructorsAndNull();
        testDeepSerialization();
        testDeepLiveServer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
