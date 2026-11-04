/*
 * @test
 * @summary jdk.internal.classfile.impl.AbstractPseudoInstruction.UnboundCharacterRange conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main UnboundCharacterRangeTest
 */
public class UnboundCharacterRangeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.AbstractPseudoInstruction$UnboundCharacterRange", false, UnboundCharacterRangeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.AbstractPseudoInstruction$UnboundCharacterRange"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.classfile.impl.AbstractPseudoInstruction"), "superclass jdk.internal.classfile.impl.AbstractPseudoInstruction");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.lang.classfile.instruction.CharacterRange"), "interface java.lang.classfile.instruction.CharacterRange");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 6, "declared methods >= 6");
        check(hasM(ms, "characterRangeEnd"), "method characterRangeEnd");
        check(hasM(ms, "characterRangeStart"), "method characterRangeStart");
        check(hasM(ms, "endScope"), "method endScope");
        check(hasM(ms, "flags"), "method flags");
        check(hasM(ms, "startScope"), "method startScope");
        check(hasM(ms, "writeTo"), "method writeTo");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 5, "declared fields >= 5");
        check(hasF(fs, "characterRangeEnd"), "field characterRangeEnd");
        check(hasF(fs, "characterRangeStart"), "field characterRangeStart");
        check(hasF(fs, "endScope"), "field endScope");
        check(hasF(fs, "flags"), "field flags");
        check(hasF(fs, "startScope"), "field startScope");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
