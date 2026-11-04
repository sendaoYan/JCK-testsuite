/*
 * @test
 * @summary jdk.internal.agent.AgentConfigurationError conformance: internal class reflection.
 * @modules jdk.management.agent/jdk.internal.agent:+open
 * @run main AgentConfigurationErrorTest
 */
public class AgentConfigurationErrorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.agent.AgentConfigurationError", false, AgentConfigurationErrorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.agent.AgentConfigurationError"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Error"), "superclass");
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
            check(String.join(";", s).equals("getError;getParams"), "methods");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(f.getName());
            }
            check(String.join(";", s).equals("ACCESS_FILE_NOT_FOUND;ACCESS_FILE_NOT_READABLE;ACCESS_FILE_NOT_SET;ACCESS_FILE_READ_FAILED;AGENT_CLASS_ACCESS_DENIED;AGENT_CLASS_FAILED;AGENT_CLASS_INVALID;AGENT_CLASS_NOT_FOUND;AGENT_CLASS_PREMAIN_NOT_FOUND;AGENT_EXCEPTION;CONFIG_FILE_ACCESS_DENIED;CONFIG_FILE_CLOSE_FAILED;CONFIG_FILE_NOT_FOUND;CONFIG_FILE_OPEN_FAILED;CONNECTOR_SERVER_IO_ERROR;EXPORT_ADDRESS_FAILED;FILE_ACCESS_NOT_RESTRICTED;FILE_NOT_FOUND;FILE_NOT_READABLE;FILE_NOT_SET;FILE_READ_FAILED;INVALID_JMXREMOTE_LOCAL_PORT;INVALID_JMXREMOTE_PORT;INVALID_JMXREMOTE_RMI_PORT;INVALID_OPTION;INVALID_STATE;PASSWORD_FILE_ACCESS_NOT_RESTRICTED;PASSWORD_FILE_NOT_FOUND;PASSWORD_FILE_NOT_READABLE;PASSWORD_FILE_NOT_SET;PASSWORD_FILE_READ_FAILED"), "fields");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
