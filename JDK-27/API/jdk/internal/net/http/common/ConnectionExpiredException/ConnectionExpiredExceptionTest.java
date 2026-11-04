/*
 * @test
 * @summary jdk.internal.net.http.common.ConnectionExpiredException conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.common:+open
 * @run main ConnectionExpiredExceptionTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ConnectionExpiredExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.common.ConnectionExpiredException", false, ConnectionExpiredExceptionTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.common.ConnectionExpiredException"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.io.IOException"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
