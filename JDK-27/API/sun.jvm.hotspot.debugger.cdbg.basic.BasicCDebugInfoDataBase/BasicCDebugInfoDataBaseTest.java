/*
 * @test
 * @summary sun.jvm.hotspot.debugger.cdbg.basic.BasicCDebugInfoDataBase conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.cdbg.basic:+open
 * @run main BasicCDebugInfoDataBaseTest
 */
import java.lang.reflect.*;

public class BasicCDebugInfoDataBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.cdbg.basic.BasicCDebugInfoDataBase", false, BasicCDebugInfoDataBaseTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.cdbg.CDebugInfoDataBase"), "interface sun.jvm.hotspot.debugger.cdbg.CDebugInfoDataBase");
        check(hasMethod(c, "resolve"), "method resolve");
        check(hasMethod(c, "iterate"), "method iterate");
        check(hasMethod(c, "lookupType"), "method lookupType");
        check(hasMethod(c, "addType"), "method addType");
        check(hasMethod(c, "debugInfoForPC"), "method debugInfoForPC");
        check(hasMethod(c, "lineNumberForPC"), "method lineNumberForPC");
        check(hasMethod(c, "lookupSym"), "method lookupSym");
        check(hasMethod(c, "resolveType"), "method resolveType");
        check(hasMethod(c, "addLineNumberInfo"), "method addLineNumberInfo");
        check(hasMethod(c, "beginConstruction"), "method beginConstruction");
        check(hasMethod(c, "endConstruction"), "method endConstruction");
        check(hasMethod(c, "addBlock"), "method addBlock");
        check(hasMethod(c, "addGlobalSym"), "method addGlobalSym");
        check(hasMethod(c, "resolveSym"), "method resolveSym");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
