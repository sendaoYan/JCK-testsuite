/*
 * @test
 * @summary jdk.internal.classfile.impl.Util conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main UtilTest
 */
public class UtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.Util", false, UtilTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.Util"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 43, "declared methods >= 43");
        check(hasM(ms, "badOpcodeKindException"), "method badOpcodeKindException");
        check(hasM(ms, "buildingCode"), "method buildingCode");
        check(hasM(ms, "buildingFlags"), "method buildingFlags");
        check(hasM(ms, "checkFlags"), "method checkFlags");
        check(hasM(ms, "checkKind"), "method checkKind");
        check(hasM(ms, "checkU1"), "method checkU1");
        check(hasM(ms, "checkU2"), "method checkU2");
        check(hasM(ms, "descriptorStringHash"), "method descriptorStringHash");
        check(hasM(ms, "dumpBytesHex"), "method dumpBytesHex");
        check(hasM(ms, "dumpMethod"), "method dumpMethod");
        check(hasM(ms, "entryList"), "method entryList");
        check(hasM(ms, "fieldTypeSymbol"), "method fieldTypeSymbol");
        check(hasM(ms, "flagsToBits"), "method flagsToBits");
        check(hasM(ms, "has"), "method has");
        check(hasM(ms, "isAttributeAllowed"), "method isAttributeAllowed");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 5, "declared fields >= 5");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
