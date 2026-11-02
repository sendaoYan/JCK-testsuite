/*
 * @test
 * @summary jdk.internal.classfile.impl.SignaturesImpl.UnboundedTypeArgImpl conformance: internal enum reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main UnboundedTypeArgImplTest
 */
public class UnboundedTypeArgImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.SignaturesImpl$UnboundedTypeArgImpl", false, UnboundedTypeArgImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.SignaturesImpl$UnboundedTypeArgImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.lang.classfile.Signature$TypeArg$Unbounded"), "interface java.lang.classfile.Signature$TypeArg$Unbounded");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 3, "declared methods >= 3");
        check(hasM(ms, "valueOf"), "method valueOf");
        check(hasM(ms, "values"), "method values");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 2, "declared fields >= 2");
        check(hasF(fs, "INSTANCE"), "field INSTANCE");
        check(hasEnum(fs, "INSTANCE"), "enum constant INSTANCE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
