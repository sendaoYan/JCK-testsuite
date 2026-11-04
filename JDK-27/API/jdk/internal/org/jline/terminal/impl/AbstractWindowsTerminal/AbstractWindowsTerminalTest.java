/*
 * @test
 * @summary jdk.internal.org.jline.terminal.impl.AbstractWindowsTerminal conformance: internal package reflection.
 * @modules jdk.internal.le/jdk.internal.org.jline.terminal.impl:+open
 * @run main AbstractWindowsTerminalTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AbstractWindowsTerminalTest {
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
        Class<?> c = Class.forName("jdk.internal.org.jline.terminal.impl.AbstractWindowsTerminal", false, AbstractWindowsTerminalTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.org.jline.terminal.impl.AbstractWindowsTerminal"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass().getName().equals("jdk.internal.org.jline.terminal.impl.AbstractTerminal"), "superclass");
        check(Arrays.equals(interfaces(c), new String[]{}), "interfaces");
        check(Arrays.equals(methods(c), new String[]{"canPauseResume","ctrl","doClose","getAttributes","getConsoleMode","getEscapeSequence","getProvider","getRawSequence","getSystemStream","handle","hasFocusSupport","input","output","pause","pause","paused","processConsoleInput","processInputChar","processKeyEvent","pump","reader","resume","setAttributes","setConsoleMode","setSize","trackFocus","trackMouse","updateConsoleMode","writer"}), "methods");
        check(Arrays.equals(fields(c), new String[]{"ENABLE_ECHO_INPUT","ENABLE_EXTENDED_FLAGS","ENABLE_INSERT_MODE","ENABLE_LINE_INPUT","ENABLE_MOUSE_INPUT","ENABLE_PROCESSED_INPUT","ENABLE_QUICK_EDIT_MODE","ENABLE_VIRTUAL_TERMINAL_PROCESSING","ENABLE_WINDOW_INPUT","TYPE_WINDOWS","TYPE_WINDOWS_256_COLOR","TYPE_WINDOWS_CONEMU","TYPE_WINDOWS_VTP","attributes","closer","focusTracking","inConsole","input","lock","nativeHandlers","originalInConsoleMode","originalOutConsoleMode","outConsole","output","paused","pump","reader","skipNextLf","slaveInputPipe","tracking","writer"}), "fields");
        check(Arrays.equals(enumConstants(c), new String[]{}), "enumConstants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
