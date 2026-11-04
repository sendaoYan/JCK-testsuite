/*
 * @test
 * @summary sun.management.jdp.JdpJmxPacket conformance: internal class reflection.
 * @modules jdk.management.agent/sun.management.jdp:+open
 * @run main JdpJmxPacketTest
 */
public class JdpJmxPacketTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.jdp.JdpJmxPacket", false, JdpJmxPacketTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("sun.management.jdp.JdpJmxPacket"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.management.jdp.JdpGenericPacket"), "superclass");
        {
            java.lang.Class<?>[] arr = c.getInterfaces();
            String[] n = new String[arr.length];
            for (int i = 0; i < arr.length; i++) n[i] = arr[i].getName();
            java.util.Arrays.sort(n);
            check(String.join(";", n).equals("sun.management.jdp.JdpPacket"), "interfaces");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(m.getName());
            }
            check(String.join(";", s).equals("equals;getBroadcastInterval;getId;getInstanceName;getJmxServiceUrl;getMainClass;getPacketData;getProcessId;getRmiHostname;hashCode;setBroadcastInterval;setInstanceName;setMainClass;setProcessId;setRmiHostname"), "methods");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(f.getName());
            }
            check(String.join(";", s).equals("BROADCAST_INTERVAL_KEY;INSTANCE_NAME_KEY;JMX_SERVICE_URL_KEY;MAIN_CLASS_KEY;PROCESS_ID_KEY;RMI_HOSTNAME_KEY;UUID_KEY"), "fields");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
