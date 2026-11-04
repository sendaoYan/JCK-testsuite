/*
 * @test
 * @summary jdk.internal.org.jline.terminal.impl.AbstractTerminal conformance: internal package reflection.
 * @modules jdk.internal.le/jdk.internal.org.jline.terminal.impl:+open
 * @run main AbstractTerminalTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AbstractTerminalTest {
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
        Class<?> c = Class.forName("jdk.internal.org.jline.terminal.impl.AbstractTerminal", false, AbstractTerminalTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.org.jline.terminal.impl.AbstractTerminal"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Arrays.equals(interfaces(c), new String[]{"jdk.internal.org.jline.terminal.spi.TerminalExt"}), "interfaces");
        check(Arrays.equals(methods(c), new String[]{"canPauseResume","checkInterrupted","close","doClose","echo","echo","echoSignal","encoding","enterRawMode","flush","getBooleanCapability","getCursorPosition","getKind","getName","getNumericCapability","getPalette","getStatus","getStatus","getStringCapability","getType","handle","hasFocusSupport","hasMouseSupport","parseInfoCmp","pause","pause","paused","puts","raise","readMouseEvent","readMouseEvent","resume","setOnClose","toString","trackFocus","trackMouse"}), "methods");
        check(Arrays.equals(fields(c), new String[]{"bools","encoding","handlers","ints","name","onClose","palette","status","strings","type"}), "fields");
        check(Arrays.equals(enumConstants(c), new String[]{}), "enumConstants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
