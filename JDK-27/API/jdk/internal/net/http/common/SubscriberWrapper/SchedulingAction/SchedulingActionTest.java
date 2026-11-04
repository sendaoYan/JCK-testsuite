/*
 * @test
 * @summary jdk.internal.net.http.common.SubscriberWrapper.SchedulingAction conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.common:+open
 * @run main SchedulingActionTest
 */
import java.lang.reflect.*;
import java.util.*;
public class SchedulingActionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.common.SubscriberWrapper$SchedulingAction", false, SchedulingActionTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.common.SubscriberWrapper$SchedulingAction"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("valueOf"), "method:valueOf");
        check(methods.contains("values"), "method:values");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("CONTINUE"), "field:CONTINUE");
        check(fields.contains("RESCHEDULE"), "field:RESCHEDULE");
        check(fields.contains("RETURN"), "field:RETURN");
        Set<String> enumConsts = new HashSet<>();
        for (Field f : c.getDeclaredFields()) if (f.isEnumConstant()) enumConsts.add(f.getName());
        check(enumConsts.contains("CONTINUE"), "enum-const:CONTINUE");
        check(enumConsts.contains("RESCHEDULE"), "enum-const:RESCHEDULE");
        check(enumConsts.contains("RETURN"), "enum-const:RETURN");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
