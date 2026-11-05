/*
 * @test
 * @summary java.lang.Void API conformance: Void.TYPE is the primitive void class, with the
 *          name "void" and the primitive flag set.
 * @run main VoidTest
 */

import java.lang.reflect.Modifier;

public class VoidTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Void");
        check(Modifier.isFinal(c.getModifiers()), "Void is final");
        check(!c.isInterface(), "Void is a class");
        check(c.getField("TYPE") != null, "Void.TYPE field present");
        check(Modifier.isStatic(c.getField("TYPE").getModifiers())
              && Modifier.isFinal(c.getField("TYPE").getModifiers()), "TYPE is static final");
        check(c.getField("TYPE").getType() == Class.class, "TYPE field type is Class");
        check(c.getConstructors().length == 0, "Void has no public constructors");
    }

    static void testType() {
        check(Void.TYPE == void.class, "Void.TYPE == void.class");
        check(Void.TYPE.getName().equals("void"), "Void.TYPE name is \"void\"");
        check(Void.TYPE.isPrimitive(), "Void.TYPE is primitive");
        check(Void.class != Void.TYPE, "Void.class (wrapper) != Void.TYPE (primitive)");
        check(Void.class.getName().equals("java.lang.Void"), "Void.class name is java.lang.Void");
    }

    // ================= 契约深化（2026-11）追加 =================

    /** 本类自己的 void 返回方法，用于反射断言其返回类型即 Void.TYPE。 */
    static void noop() { }

    static void testStructureDeep() throws Exception {
        check(!Modifier.isAbstract(Void.class.getModifiers()),
              "Void is not abstract (final + private ctor make it uninstantiable)");
        java.lang.reflect.Constructor<?>[] ctors = Void.class.getDeclaredConstructors();
        check(ctors.length == 1, "exactly one declared constructor");
        check(Modifier.isPrivate(ctors[0].getModifiers()), "the sole constructor is private");
        check(Void.class.getSuperclass() == Object.class, "Void extends Object");
        check(!Void.class.isPrimitive(), "Void.class is not primitive");
        check(!Void.class.isArray(), "Void.class is not an array");
        check(!java.io.Serializable.class.isAssignableFrom(Void.class),
              "Void is not Serializable");
        check(Void.class.getPackage() != null
              && "java.lang".equals(Void.class.getPackage().getName()),
              "package is java.lang");
        check(Void.TYPE.getPackage() == null, "primitive void has no package");
        check(Void.TYPE.getSimpleName().equals("void"), "Void.TYPE simple name is \"void\"");
        check(Void.class.getSimpleName().equals("Void"), "Void.class simple name is Void");
        check(Void.TYPE.toString().equals("void"), "Void.TYPE toString is \"void\"");
        check(Modifier.isFinal(Void.TYPE.getModifiers()), "primitive void class is final");
        check(!Void.class.isInstance(null), "isInstance(null) == false");
        check(!Void.class.isInstance(new Object()), "isInstance(Object) == false");
        check(Void.class.getClassLoader() == null, "Void loaded by bootstrap loader");
        check(Void.TYPE.getClassLoader() == null, "void loaded by bootstrap loader");
    }

    static void testVoidReflection() throws Exception {
        check(Runnable.class.getMethod("run").getReturnType() == void.class,
              "Runnable.run() returns void.class");
        check(Runnable.class.getMethod("run").getReturnType() == Void.TYPE,
              "Runnable.run() returns Void.TYPE");
        check(VoidTest.class.getDeclaredMethod("noop").getReturnType() == Void.TYPE,
              "own void method returns Void.TYPE");
        try {
            Class.forName("void");
            check(false, "Class.forName(\"void\") should throw ClassNotFoundException");
        } catch (ClassNotFoundException expected) {
            check(true, "Class.forName(\"void\") throws ClassNotFoundException");
        }
        // TYPE 是静态单例：重复读取同一引用
        Class<?> t = Void.TYPE;
        check(t == void.class && Void.TYPE == t, "Void.TYPE singleton reference stable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testType();
        // 契约深化（2026-11）
        testStructureDeep();
        testVoidReflection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
