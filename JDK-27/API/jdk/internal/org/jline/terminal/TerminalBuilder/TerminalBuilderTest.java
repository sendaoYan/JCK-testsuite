/*
 * @test
 * @summary jdk.internal.org.jline.terminal.TerminalBuilder conformance: internal package reflection.
 * @modules jdk.internal.le/jdk.internal.org.jline.terminal:+open
 * @run main TerminalBuilderTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TerminalBuilderTest {
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
        Class<?> c = Class.forName("jdk.internal.org.jline.terminal.TerminalBuilder", false, TerminalBuilderTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.org.jline.terminal.TerminalBuilder"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Arrays.equals(interfaces(c), new String[]{}), "interfaces");
        check(Arrays.equals(methods(c), new String[]{"attributes","build","builder","codepage","color","computeEncoding","computeSystemOutput","computeType","dumb","encoding","encoding","exec","ffm","getProviders","inputStreamWrapper","jansi","jna","jni","name","nativeSignals","paused","provider","providers","setTerminalOverride","signalHandler","size","streams","system","systemOutput","terminal","type"}), "methods");
        check(Arrays.equals(fields(c), new String[]{"DEPRECATED_PROVIDERS","PROP_CODEPAGE","PROP_COLOR_DISTANCE","PROP_DISABLE_ALTERNATE_CHARSET","PROP_DISABLE_DEPRECATED_PROVIDER_WARNING","PROP_DUMB","PROP_DUMB_COLOR","PROP_ENCODING","PROP_EXEC","PROP_FFM","PROP_FILE_DESCRIPTOR_CREATION_MODE","PROP_FILE_DESCRIPTOR_CREATION_MODE_DEFAULT","PROP_FILE_DESCRIPTOR_CREATION_MODE_NATIVE","PROP_FILE_DESCRIPTOR_CREATION_MODE_REFLECTION","PROP_JANSI","PROP_JNA","PROP_JNI","PROP_NON_BLOCKING_READS","PROP_OUTPUT","PROP_OUTPUT_ERR","PROP_OUTPUT_ERR_OUT","PROP_OUTPUT_FORCED_ERR","PROP_OUTPUT_FORCED_OUT","PROP_OUTPUT_OUT","PROP_OUTPUT_OUT_ERR","PROP_PROVIDER","PROP_PROVIDERS","PROP_PROVIDERS_DEFAULT","PROP_PROVIDER_DUMB","PROP_PROVIDER_EXEC","PROP_PROVIDER_FFM","PROP_PROVIDER_JANSI","PROP_PROVIDER_JNA","PROP_PROVIDER_JNI","PROP_REDIRECT_PIPE_CREATION_MODE","PROP_REDIRECT_PIPE_CREATION_MODE_DEFAULT","PROP_REDIRECT_PIPE_CREATION_MODE_NATIVE","PROP_REDIRECT_PIPE_CREATION_MODE_REFLECTION","PROP_TYPE"}), "fields");
        check(Arrays.equals(enumConstants(c), new String[]{}), "enumConstants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
