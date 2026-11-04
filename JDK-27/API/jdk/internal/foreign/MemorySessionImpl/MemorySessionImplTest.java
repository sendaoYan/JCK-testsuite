/*
 * @test
 * @summary jdk.internal.foreign.MemorySessionImpl conformance: internal abstract class reflection.
 * @modules java.base/jdk.internal.foreign:+open
 * @run main MemorySessionImplTest
 */
public class MemorySessionImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.MemorySessionImpl", false, MemorySessionImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.MemorySessionImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.lang.foreign.MemorySegment$Scope"), "interface java.lang.foreign.MemorySegment$Scope");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 28, "declared methods >= 28");
        check(hasM(ms, "acquire0"), "method acquire0");
        check(hasM(ms, "addCloseAction"), "method addCloseAction");
        check(hasM(ms, "addOrCleanupIfFail"), "method addOrCleanupIfFail");
        check(hasM(ms, "asArena"), "method asArena");
        check(hasM(ms, "checkValidState"), "method checkValidState");
        check(hasM(ms, "checkValidStateRaw"), "method checkValidStateRaw");
        check(hasM(ms, "clone"), "method clone");
        check(hasM(ms, "close"), "method close");
        check(hasM(ms, "createConfined"), "method createConfined");
        check(hasM(ms, "createHeap"), "method createHeap");
        check(hasM(ms, "createImplicit"), "method createImplicit");
        check(hasM(ms, "createShared"), "method createShared");
        check(hasM(ms, "isAccessibleBy"), "method isAccessibleBy");
        check(hasM(ms, "isAlive"), "method isAlive");
        check(hasM(ms, "isCloseable"), "method isCloseable");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 13, "declared fields >= 13");
        check(hasF(fs, "GLOBAL_SESSION"), "field GLOBAL_SESSION");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
