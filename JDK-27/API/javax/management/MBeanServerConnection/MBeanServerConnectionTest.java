/*
 * @test
 * @summary javax.management.MBeanServerConnection conformance: white-box structure + black-box behavior.
 * @run main MBeanServerConnectionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.lang.management.*;
import javax.management.*;

public class MBeanServerConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MBeanServerConnection");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "getAttribute"), "has getAttribute");
        check(hasMethod(c, "getMBeanInfo"), "has getMBeanInfo");
        check(hasMethod(c, "queryNames"), "has queryNames");
        check(hasMethod(c, "getMBeanCount"), "has getMBeanCount");
        check(hasMethod(c, "isRegistered"), "has isRegistered");
        check(hasMethod(c, "getDomains"), "has getDomains");
        check(hasMethod(c, "getDefaultDomain"), "has getDefaultDomain");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // The platform MBeanServer is a real MBeanServerConnection.
        MBeanServerConnection mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName runtime = new ObjectName("java.lang:type=Runtime");

        Object vmName = mbs.getAttribute(runtime, "VmName");
        check(vmName != null && vmName.toString().length() > 0, "VmName attribute non-empty");

        Object specVersion = mbs.getAttribute(runtime, "SpecVersion");
        check(specVersion != null && specVersion.toString().length() > 0, "SpecVersion attribute non-empty");

        // batch getAttributes
        AttributeList attrs = mbs.getAttributes(runtime, new String[]{"VmName", "SpecVersion"});
        check(attrs.size() == 2, "getAttributes returns two");
        check(attrs.get(0) instanceof Attribute, "getAttributes elements typed");

        // getMBeanInfo
        MBeanInfo info = mbs.getMBeanInfo(runtime);
        check(info != null && info.getAttributes().length > 0, "getMBeanInfo non-empty");

        // queryNames with a wildcard and null query returns the platform MBeans
        Set<ObjectName> names = mbs.queryNames(new ObjectName("java.lang:*"), null);
        check(!names.isEmpty(), "queryNames java.lang:* non-empty");
        check(names.contains(runtime), "queryNames includes java.lang:type=Runtime");

        // queryMBeans returns ObjectInstances with the same names
        Set<ObjectInstance> mbeans = mbs.queryMBeans(new ObjectName("java.lang:type=Runtime"), null);
        check(mbeans.size() == 1, "queryMBeans one result");
        check(mbeans.iterator().next().getObjectName().equals(runtime), "queryMBeans object name");

        check(mbs.isRegistered(runtime), "isRegistered");
        check(mbs.getMBeanCount() > 0, "getMBeanCount positive");
        check(mbs.isInstanceOf(runtime, "java.lang.management.RuntimeMXBean"), "isInstanceOf RuntimeMXBean");

        String[] domains = mbs.getDomains();
        check(domains.length > 0, "getDomains non-empty");
        boolean hasJavaLang = false;
        for (String d : domains) if ("java.lang".equals(d)) hasJavaLang = true;
        check(hasJavaLang, "domains include java.lang");
        check(mbs.getDefaultDomain() != null, "getDefaultDomain non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
