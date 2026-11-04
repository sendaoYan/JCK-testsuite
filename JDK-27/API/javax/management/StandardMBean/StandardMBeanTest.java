/*
 * @test
 * @summary javax.management.StandardMBean conformance: white-box structure + black-box behavior.
 * @run main StandardMBeanTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.lang.management.*;
import javax.management.*;

public class StandardMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    public interface ThingMBean {
        int getCount();
        void setCount(int c);
        int reset(int base);
    }
    public static class Thing implements ThingMBean {
        private int count = 7;
        public int getCount() { return count; }
        public void setCount(int c) { count = c; }
        public int reset(int base) { count = base; return count; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.StandardMBean");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(DynamicMBean.class.isAssignableFrom(c), "implements DynamicMBean");
        check(MBeanRegistration.class.isAssignableFrom(c), "implements MBeanRegistration");
        check(hasMethod(c, "getAttribute"), "has getAttribute");
        check(hasMethod(c, "setAttribute"), "has setAttribute");
        check(hasMethod(c, "invoke"), "has invoke");
        check(hasMethod(c, "getMBeanInfo"), "has getMBeanInfo");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        StandardMBean smb = new StandardMBean(new Thing(), ThingMBean.class);
        check(smb.getMBeanInterface() == ThingMBean.class, "getMBeanInterface");
        check(smb.getImplementation() instanceof Thing, "getImplementation");
        check(smb.getImplementationClass() == Thing.class, "getImplementationClass");

        // MBeanInfo introspection
        MBeanInfo info = smb.getMBeanInfo();
        check(Thing.class.getName().equals(info.getClassName()), "getMBeanInfo className");
        check(info.getAttributes().length == 1, "one attribute");
        check("Count".equals(info.getAttributes()[0].getName()), "attribute name");
        check(info.getOperations().length == 1, "one operation");
        check("reset".equals(info.getOperations()[0].getName()), "operation name");

        // attribute get/set via DynamicMBean
        check(((Integer) smb.getAttribute("Count")).intValue() == 7, "getAttribute initial");
        smb.setAttribute(new Attribute("Count", 42));
        check(((Integer) smb.getAttribute("Count")).intValue() == 42, "setAttribute/getAttribute round-trip");

        // getAttributes / setAttributes batch
        AttributeList got = smb.getAttributes(new String[]{"Count"});
        check(got.size() == 1 && got.get(0).equals(new Attribute("Count", 42)), "getAttributes");
        AttributeList set = new AttributeList();
        set.add(new Attribute("Count", 55));
        AttributeList setResult = smb.setAttributes(set);
        check(setResult.size() == 1 && smb.getAttribute("Count").equals(55), "setAttributes");

        // invoke
        Object r = smb.invoke("reset", new Object[]{10}, new String[]{"int"});
        check(((Integer) r).intValue() == 10, "invoke returns value");
        check(smb.getAttribute("Count").equals(10), "invoke mutated state");

        // missing attribute throws AttributeNotFoundException
        boolean threw = false;
        try { smb.getAttribute("Missing"); } catch (AttributeNotFoundException ex) { threw = true; }
        check(threw, "missing attribute throws AttributeNotFoundException");

        // register/unregister against platform MBeanServer
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName("jck.test:type=StandardMBeanTest");
        ObjectInstance oi = mbs.registerMBean(smb, on);
        check(oi.getObjectName().equals(on), "registerMBean returns ObjectInstance");
        check(mbs.isRegistered(on), "isRegistered after register");
        check(mbs.getAttribute(on, "Count").equals(10), "MBeanServer getAttribute");
        mbs.setAttribute(on, new Attribute("Count", 99));
        check(mbs.getAttribute(on, "Count").equals(99), "MBeanServer setAttribute round-trip");
        check(((Integer) mbs.invoke(on, "reset", new Object[]{1}, new String[]{"int"})).intValue() == 1,
                "MBeanServer invoke");
        check(mbs.isInstanceOf(on, Thing.class.getName()), "isInstanceOf");
        mbs.unregisterMBean(on);
        check(!mbs.isRegistered(on), "unregistered");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
