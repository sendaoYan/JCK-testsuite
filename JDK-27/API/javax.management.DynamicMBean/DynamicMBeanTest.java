/*
 * @test
 * @summary javax.management.DynamicMBean conformance: white-box structure + light behavior.
 * @run main DynamicMBeanTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class DynamicMBeanTest {
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
    }
    public static class Thing implements ThingMBean {
        private int count = 7;
        public int getCount() { return count; }
        public void setCount(int c) { count = c; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.DynamicMBean");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "getAttribute"), "has getAttribute");
        check(hasMethod(c, "setAttribute"), "has setAttribute");
        check(hasMethod(c, "getAttributes"), "has getAttributes");
        check(hasMethod(c, "setAttributes"), "has setAttributes");
        check(hasMethod(c, "invoke"), "has invoke");
        check(hasMethod(c, "getMBeanInfo"), "has getMBeanInfo");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        // StandardMBean is a DynamicMBean implementation.
        DynamicMBean mb = new StandardMBean(new Thing(), ThingMBean.class);
        check(mb.getMBeanInfo().getAttributes().length == 1, "getMBeanInfo");
        check(((Integer) mb.getAttribute("Count")).intValue() == 7, "getAttribute");

        mb.setAttribute(new Attribute("Count", 21));
        check(((Integer) mb.getAttribute("Count")).intValue() == 21, "setAttribute round-trip");

        AttributeList list = mb.getAttributes(new String[]{"Count"});
        check(list.size() == 1 && list.get(0).equals(new Attribute("Count", 21)), "getAttributes");

        AttributeList toSet = new AttributeList();
        toSet.add(new Attribute("Count", 30));
        mb.setAttributes(toSet);
        check(((Integer) mb.getAttribute("Count")).intValue() == 30, "setAttributes round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
