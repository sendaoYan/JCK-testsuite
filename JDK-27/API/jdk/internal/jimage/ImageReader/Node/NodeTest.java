/*
 * @test
 * @summary jdk.internal.jimage.ImageReader.Node conformance: internal package reflection.
 * @modules java.base/jdk.internal.jimage:+open
 * @run main NodeTest
 */
public class NodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.jimage.ImageReader$Node", false, NodeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "compressedSize"), "method compressedSize");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "extension"), "method extension");
        check(hasMethod(c, "getChildNames"), "method getChildNames");
        check(hasMethod(c, "getFileAttributes"), "method getFileAttributes");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "isDirectory"), "method isDirectory");
        check(hasMethod(c, "isLink"), "method isLink");
        check(hasMethod(c, "isResource"), "method isResource");
        check(hasMethod(c, "resolveLink"), "method resolveLink");
        check(hasMethod(c, "size"), "method size");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "fileAttrs"), "field fileAttrs");
        check(hasField(c, "name"), "field name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
