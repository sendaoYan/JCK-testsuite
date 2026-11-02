/*
 * @test
 * @summary jdk.internal.org.jline.reader.impl.history.DefaultHistory conformance: internal package reflection.
 * @modules jdk.internal.le/jdk.internal.org.jline.reader.impl.history:+open
 * @run main DefaultHistoryTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultHistoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static String[] methods(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) l.add(m.getName());
        }
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    static String[] fields(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) l.add(f.getName());
        }
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    static String[] enumConstants(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) l.add(f.getName());
        }
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    static String[] interfaces(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Class<?> i : c.getInterfaces()) l.add(i.getName());
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.jline.reader.impl.history.DefaultHistory", false, DefaultHistoryTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.org.jline.reader.impl.history.DefaultHistory"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Arrays.equals(interfaces(c), new String[]{"jdk.internal.org.jline.reader.History"}), "interfaces");
        check(Arrays.equals(methods(c), new String[]{"add","addHistoryLine","addHistoryLine","append","attach","createEntry","current","first","get","index","internalAdd","internalAdd","isEmpty","iterator","last","load","matchPatterns","moveTo","moveToEnd","moveToFirst","moveToLast","next","previous","purge","read","resetIndex","save","size","spliterator","toString","trimHistory","write"}), "methods");
        check(Arrays.equals(fields(c), new String[]{"DEFAULT_HISTORY_FILE_SIZE","DEFAULT_HISTORY_SIZE"}), "fields");
        check(Arrays.equals(enumConstants(c), new String[]{}), "enumConstants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
