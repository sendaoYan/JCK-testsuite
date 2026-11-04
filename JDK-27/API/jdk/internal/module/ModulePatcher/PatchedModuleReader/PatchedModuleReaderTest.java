/*
 * @test
 * @summary jdk.internal.module.ModulePatcher.PatchedModuleReader conformance: internal package reflection.
 * @modules java.base/jdk.internal.module:+open
 * @run main PatchedModuleReaderTest
 */
public class PatchedModuleReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.module.ModulePatcher$PatchedModuleReader", false, PatchedModuleReaderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.module.ModuleReader"), "interface java.lang.module.ModuleReader");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "find"), "method find");
        check(hasMethod(c, "findResource"), "method findResource");
        check(hasMethod(c, "list"), "method list");
        check(hasMethod(c, "open"), "method open");
        check(hasMethod(c, "read"), "method read");
        check(hasMethod(c, "release"), "method release");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "closed"), "field closed");
        check(hasField(c, "delegate"), "field delegate");
        check(hasField(c, "delegateCodeSourceURL"), "field delegateCodeSourceURL");
        check(hasField(c, "finders"), "field finders");
        check(hasField(c, "mref"), "field mref");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
