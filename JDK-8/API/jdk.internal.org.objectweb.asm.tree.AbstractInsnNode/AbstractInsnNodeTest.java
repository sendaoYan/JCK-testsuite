/*
 * @test
 * @summary jdk.internal.org.objectweb.asm.tree.AbstractInsnNode conformance: type shape via reflection (headless-safe).
 * @run main AbstractInsnNodeTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class AbstractInsnNodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.objectweb.asm.tree.AbstractInsnNode", false, AbstractInsnNodeTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "accept"), "declares accept");
        check(hasMethod(c, "clone"), "declares clone");
        check(hasMethod(c, "getNext"), "declares getNext");
        check(hasMethod(c, "getOpcode"), "declares getOpcode");
        check(hasMethod(c, "getPrevious"), "declares getPrevious");
        check(hasMethod(c, "getType"), "declares getType");
        check(hasField(c, "FIELD_INSN"), "declares field FIELD_INSN");
        check(hasField(c, "FRAME"), "declares field FRAME");
        check(hasField(c, "IINC_INSN"), "declares field IINC_INSN");
        check(hasField(c, "INSN"), "declares field INSN");
        check(hasField(c, "INT_INSN"), "declares field INT_INSN");
        check(hasField(c, "INVOKE_DYNAMIC_INSN"), "declares field INVOKE_DYNAMIC_INSN");
        check(hasField(c, "JUMP_INSN"), "declares field JUMP_INSN");
        check(hasField(c, "LABEL"), "declares field LABEL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
