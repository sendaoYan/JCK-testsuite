/*
 * @test
 * @summary jdk.internal.net.http.common.DebugLogger.LoggerConfig conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.common:+open
 * @run main LoggerConfigTest
 */
import java.lang.reflect.*;
import java.util.*;
public class LoggerConfigTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.common.DebugLogger$LoggerConfig", false, LoggerConfigTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.common.DebugLogger$LoggerConfig"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Record"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("errLevel"), "method:errLevel");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("levelEnabledFor"), "method:levelEnabledFor");
        check(methods.contains("logLevel"), "method:logLevel");
        check(methods.contains("minSeverity"), "method:minSeverity");
        check(methods.contains("of"), "method:of");
        check(methods.contains("on"), "method:on");
        check(methods.contains("outLevel"), "method:outLevel");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("withErrLevel"), "method:withErrLevel");
        check(methods.contains("withLogLevel"), "method:withLogLevel");
        check(methods.contains("withOutLevel"), "method:withOutLevel");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("ERRLOG"), "field:ERRLOG");
        check(fields.contains("ERROUT"), "field:ERROUT");
        check(fields.contains("LOG"), "field:LOG");
        check(fields.contains("OFF"), "field:OFF");
        check(fields.contains("STDERR"), "field:STDERR");
        check(fields.contains("STDOUT"), "field:STDOUT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
