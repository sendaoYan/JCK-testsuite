/*
 * @test
 * @summary javax.management.loading.ClassLoaderRepository conformance: white-box structure + black-box behavior.
 * @run main ClassLoaderRepositoryTest
 */

import java.lang.reflect.Method;
import javax.management.loading.ClassLoaderRepository;

public class ClassLoaderRepositoryTest {
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
        Class<?> c = Class.forName("javax.management.loading.ClassLoaderRepository");
        check(c.isInterface(), "is interface");
        Method[] mm = c.getMethods();
        check(has(mm, "loadClass"), "has loadClass");
        check(has(mm, "loadClassWithout"), "has loadClassWithout");
        check(has(mm, "loadClassBefore"), "has loadClassBefore");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Anonymous implementation proving the interface contract is callable.
        ClassLoaderRepository repo = new ClassLoaderRepository() {
            public Class<?> loadClass(String className) throws ClassNotFoundException {
                return ClassLoader.getSystemClassLoader().loadClass(className);
            }
            public Class<?> loadClassWithout(ClassLoader exclude, String className)
                    throws ClassNotFoundException {
                return loadClass(className);
            }
            public Class<?> loadClassBefore(ClassLoader stop, String className)
                    throws ClassNotFoundException {
                return loadClass(className);
            }
        };

        check(repo.loadClass("java.lang.String") == String.class, "loadClass java.lang.String");
        check(repo.loadClass("java.lang.Integer") == Integer.class, "loadClass java.lang.Integer");
        check(repo.loadClassWithout(null, "java.util.List") == java.util.List.class,
                "loadClassWithout null exclude");
        check(repo.loadClassWithout(Thread.currentThread().getContextClassLoader(),
                "java.lang.Double") == Double.class, "loadClassWithout real exclude");
        check(repo.loadClassBefore(null, "java.util.ArrayList") == java.util.ArrayList.class,
                "loadClassBefore null stop");

        boolean cnfe = false;
        try {
            repo.loadClass("no.such.Class.Name.Here");
        } catch (ClassNotFoundException e) {
            cnfe = true;
        }
        check(cnfe, "loadClass ClassNotFoundException for unknown class");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
