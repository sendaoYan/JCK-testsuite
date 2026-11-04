/*
 * @test
 * @summary sun.tools.attach.HotSpotVirtualMachine conformance: internal abstract class reflection.
 * @modules jdk.attach/sun.tools.attach:+open
 * @run main HotSpotVirtualMachineTest
 */
public class HotSpotVirtualMachineTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.tools.attach.HotSpotVirtualMachine", false, HotSpotVirtualMachineTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("sun.tools.attach.HotSpotVirtualMachine"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.attach.VirtualMachine"), "superclass");
        {
            java.lang.Class<?>[] arr = c.getInterfaces();
            String[] n = new String[arr.length];
            for (int i = 0; i < arr.length; i++) n[i] = arr[i].getName();
            java.util.Arrays.sort(n);
            check(String.join(";", n).equals(""), "interfaces");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(m.getName());
            }
            check(String.join(";", s).equals("checkNulls;dumpHeap;executeCommand;executeJCmd;getAgentProperties;getDefaultProps;getSystemProperties;heapHisto;isAPIv2Enabled;isStreamingEnabled;loadAgent;loadAgentLibrary;loadAgentPath;localDataDump;printFlag;remoteDataDump;setFlag;startLocalManagementAgent;startManagementAgent;writeCommand"), "methods");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(f.getName());
            }
            check(String.join(";", s).equals("VERSION_1;VERSION_2"), "fields");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
