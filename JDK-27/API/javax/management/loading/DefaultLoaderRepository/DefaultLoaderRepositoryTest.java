/*
 * @test
 * @summary javax.management.loading.DefaultLoaderRepository conformance: white-box structure + black-box behavior.
 * @run main DefaultLoaderRepositoryTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.loading.DefaultLoaderRepository;

@SuppressWarnings("deprecation")
public class DefaultLoaderRepositoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean has(Method[] mm, String n) {
        for (Method m : mm) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.loading.DefaultLoaderRepository");
        check(!c.isInterface(), "not interface");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(c.getAnnotation(Deprecated.class) != null, "has @Deprecated");
        check(c.getConstructors().length >= 1, "has public constructor");
        Method[] mm = c.getMethods();
        check(has(mm, "loadClass"), "has loadClass");
        check(has(mm, "loadClassWithout"), "has loadClassWithout");
        // static methods
        for (Method m : mm) {
            if (m.getName().equals("loadClass"))
                check(Modifier.isStatic(m.getModifiers()), "loadClass is static");
            if (m.getName().equals("loadClassWithout"))
                check(Modifier.isStatic(m.getModifiers()), "loadClassWithout is static");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        try {
            // A new empty MBeanServer contributes its own classloader repository.
            check(DefaultLoaderRepository.loadClass("java.lang.String") == String.class,
                    "loadClass java.lang.String");
            check(DefaultLoaderRepository.loadClassWithout(null, "java.lang.Integer") == Integer.class,
                    "loadClassWithout null exclude");

            boolean cnfe = false;
            try {
                DefaultLoaderRepository.loadClass("no.such.Class.Name.Here");
            } catch (ClassNotFoundException e) {
                cnfe = true;
            }
            check(cnfe, "loadClass ClassNotFoundException for unknown class");
        } finally {
            MBeanServerFactory.releaseMBeanServer(mbs);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
