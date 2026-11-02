/*
 * @test
 * @summary jdk.internal.io.JdkConsoleImpl conformance: internal package reflection.
 * @modules java.base/jdk.internal.io:+open
 * @run main JdkConsoleImplTest
 */
public class JdkConsoleImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.io.JdkConsoleImpl", false, JdkConsoleImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "jdk.internal.io.JdkConsole"), "interface jdk.internal.io.JdkConsole");
        check(hasMethod(c, "charset"), "method charset");
        check(hasMethod(c, "flush"), "method flush");
        check(hasMethod(c, "format"), "method format");
        check(hasMethod(c, "passwordConsole"), "method passwordConsole");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "println"), "method println");
        check(hasMethod(c, "readLine"), "method readLine");
        check(hasMethod(c, "readPassword"), "method readPassword");
        check(hasMethod(c, "readPasswordNoNewLine"), "method readPasswordNoNewLine");
        check(hasMethod(c, "reader"), "method reader");
        check(hasMethod(c, "writer"), "method writer");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "PASSWORD_CONSOLE"), "field PASSWORD_CONSOLE");
        check(hasField(c, "formatter"), "field formatter");
        check(hasField(c, "outCharset"), "field outCharset");
        check(hasField(c, "pw"), "field pw");
        check(hasField(c, "rcb"), "field rcb");
        check(hasField(c, "readLock"), "field readLock");
        check(hasField(c, "reader"), "field reader");
        check(hasField(c, "restoreEcho"), "field restoreEcho");
        check(hasField(c, "restoreEchoLock"), "field restoreEchoLock");
        check(hasField(c, "shutdownHookInstalled"), "field shutdownHookInstalled");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
