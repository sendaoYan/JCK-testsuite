/*
 * @test
 * @summary javax.management.StandardMBean conformance: DynamicMBean get/set/invoke/MBeanInfo.
 * @run main StandardMBeanTest
 */

import javax.management.*;

public class StandardMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public interface CounterMBean { int getCount(); void setCount(int v); int increment(); }
    public static class Counter implements CounterMBean {
        private int count;
        public int getCount() { return count; }
        public void setCount(int v) { count = v; }
        public int increment() { return ++count; }
    }

    public static void main(String[] args) throws Exception {
        Counter impl = new Counter();
        StandardMBean smb = new StandardMBean(impl, CounterMBean.class);
        check(smb.getImplementation() == impl, "getImplementation");
        check(smb.getMBeanInterface() == CounterMBean.class, "getMBeanInterface");
        check(smb.getImplementationClass() == Counter.class, "getImplementationClass");
        impl.setCount(3);
        check(((Integer) smb.getAttribute("Count")).intValue() == 3, "getAttribute");
        smb.setAttribute(new Attribute("Count", Integer.valueOf(9)));
        check(impl.getCount() == 9, "setAttribute");
        check(((Integer) smb.invoke("increment", new Object[0], new String[0])).intValue() == 10, "invoke");
        check(smb.getMBeanInfo().getClassName().equals(Counter.class.getName()), "MBeanInfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
