/*
 * @test
 * @summary java.lang.reflect.Modifier conformance: white-box structure + black-box behavior.
 * @run main ModifierTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ModifierTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // fixed targets whose reflected modifiers are asserted exactly
    static class Targets {
        private static final int LOCK = 1;
        public transient int tmp;
        protected volatile long shared;
        public synchronized int compute() { return 1; }
        public native int external(); // declared native (not implemented)
        public abstract static class Abs { }
        public interface Contract { }
        @FunctionalInterface
        public interface Fn { void go(); }
    }

    abstract static class AbstractTarget {
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() {
        check(Modifier.PUBLIC == 0x00000001, "PUBLIC value");
        check(Modifier.PRIVATE == 0x00000002, "PRIVATE value");
        check(Modifier.PROTECTED == 0x00000004, "PROTECTED value");
        check(Modifier.STATIC == 0x00000008, "STATIC value");
        check(Modifier.FINAL == 0x00000010, "FINAL value");
        check(Modifier.ABSTRACT == 0x00000400, "ABSTRACT value");
        check(Modifier.INTERFACE == 0x00000200, "INTERFACE value");

        check(Modifier.isPublic(Modifier.PUBLIC), "isPublic");
        check(Modifier.isPrivate(Modifier.PRIVATE), "isPrivate");
        check(Modifier.isProtected(Modifier.PROTECTED), "isProtected");
        check(Modifier.isStatic(Modifier.STATIC), "isStatic");
        check(Modifier.isFinal(Modifier.FINAL), "isFinal");
        check(Modifier.isSynchronized(Modifier.SYNCHRONIZED), "isSynchronized");
        check(Modifier.isVolatile(Modifier.VOLATILE), "isVolatile");
        check(Modifier.isTransient(Modifier.TRANSIENT), "isTransient");
        check(Modifier.isNative(Modifier.NATIVE), "isNative");
        check(Modifier.isInterface(Modifier.INTERFACE), "isInterface");
        check(Modifier.isAbstract(Modifier.ABSTRACT), "isAbstract");
        check(Modifier.isStrict(Modifier.STRICT), "isStrict");

        String s = Modifier.toString(Modifier.PUBLIC | Modifier.STATIC);
        check(s.contains("public") && s.contains("static"), "toString");
        check(Modifier.class.getMethods().length >= 16, "helper method count");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. exact modifier combos from real reflected targets
        Field lock = Targets.class.getDeclaredField("LOCK");
        int lm = lock.getModifiers();
        check(Modifier.isPrivate(lm) && Modifier.isStatic(lm) && Modifier.isFinal(lm)
              && !Modifier.isPublic(lm) && !Modifier.isTransient(lm),
              "private static final combo");
        check("private static final".equals(Modifier.toString(lm)), "toString canonical order");

        Field tmp = Targets.class.getDeclaredField("tmp");
        check(Modifier.isPublic(tmp.getModifiers()) && Modifier.isTransient(tmp.getModifiers())
              && !Modifier.isVolatile(tmp.getModifiers()), "public transient combo");
        check("public transient".equals(Modifier.toString(tmp.getModifiers())), "public transient string");

        Field shared = Targets.class.getDeclaredField("shared");
        check(Modifier.isProtected(shared.getModifiers()) && Modifier.isVolatile(shared.getModifiers()),
              "protected volatile combo");
        check("protected volatile".equals(Modifier.toString(shared.getModifiers())),
              "protected volatile string");

        Method compute = Targets.class.getMethod("compute");
        check(Modifier.isPublic(compute.getModifiers())
              && Modifier.isSynchronized(compute.getModifiers())
              && !Modifier.isNative(compute.getModifiers()), "public synchronized combo");
        check("public synchronized".equals(Modifier.toString(compute.getModifiers())),
              "public synchronized string");

        Method external = Targets.class.getMethod("external");
        check(Modifier.isNative(external.getModifiers()) && Modifier.isPublic(external.getModifiers()),
              "public native combo");

        // 2. class/interface-level combos
        int am = AbstractTarget.class.getModifiers();
        check(Modifier.isAbstract(am) && !Modifier.isInterface(am), "abstract class flags");
        check("abstract static".equals(Modifier.toString(am)), "nested abstract class string");
        int im = Targets.Contract.class.getModifiers();
        check(Modifier.isInterface(im) && Modifier.isAbstract(im), "interface flags");
        check("public abstract static interface".equals(Modifier.toString(im)),
              "nested interface canonical string");
        int fm = Targets.Fn.class.getModifiers();
        check(Modifier.isInterface(fm) && Modifier.isAbstract(fm), "annotation-type interface flags");

        // 3. toString canonical ordering and degenerate cases
        check("public static final".equals(Modifier.toString(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)),
              "canonical order public static final");
        check("abstract interface".equals(Modifier.toString(Modifier.ABSTRACT | Modifier.INTERFACE)),
              "canonical order abstract interface");
        check("".equals(Modifier.toString(0)), "empty modifiers string");
        check("public".equals(Modifier.toString(Modifier.PUBLIC)), "single flag string");
        check("protected transient".equals(Modifier.toString(Modifier.PROTECTED | Modifier.TRANSIENT)),
              "protected transient canonical");

        // 4. allow-mask helpers: membership of every source modifier
        int cls = Modifier.classModifiers();
        check(Modifier.isPublic(cls) && Modifier.isProtected(cls) && Modifier.isPrivate(cls)
              && Modifier.isAbstract(cls) && Modifier.isStatic(cls) && Modifier.isFinal(cls),
              "classModifiers contains source class flags");
        check(!Modifier.isSynchronized(cls) && !Modifier.isVolatile(cls)
              && !Modifier.isTransient(cls) && !Modifier.isNative(cls),
              "classModifiers excludes member flags");
        int ifc = Modifier.interfaceModifiers();
        check(Modifier.isPublic(ifc) && Modifier.isAbstract(ifc) && Modifier.isStatic(ifc),
              "interfaceModifiers contains source class-level flags");
        check(!Modifier.isFinal(ifc) && !Modifier.isSynchronized(ifc) && !Modifier.isVolatile(ifc),
              "interfaceModifiers excludes final and member flags");
        int mth = Modifier.methodModifiers();
        check(Modifier.isPublic(mth) && Modifier.isAbstract(mth) && Modifier.isStatic(mth)
              && Modifier.isSynchronized(mth) && Modifier.isNative(mth) && Modifier.isFinal(mth)
              && Modifier.isPrivate(mth) && Modifier.isProtected(mth) && Modifier.isStrict(mth),
              "methodModifiers full membership");
        check(!Modifier.isVolatile(mth) && !Modifier.isTransient(mth), "methodModifiers excludes field flags");
        int fld = Modifier.fieldModifiers();
        check(Modifier.isVolatile(fld) && Modifier.isTransient(fld) && Modifier.isFinal(fld)
              && Modifier.isPublic(fld) && Modifier.isStatic(fld), "fieldModifiers membership");
        check(!Modifier.isSynchronized(fld) && !Modifier.isNative(fld) && !Modifier.isAbstract(fld),
              "fieldModifiers excludes method flags");
        int ctor = Modifier.constructorModifiers();
        check(Modifier.isPublic(ctor) && Modifier.isProtected(ctor) && Modifier.isPrivate(ctor),
              "constructorModifiers only access flags");
        check(!Modifier.isStatic(ctor) && !Modifier.isAbstract(ctor) && !Modifier.isFinal(ctor)
              && !Modifier.isNative(ctor), "constructorModifiers excludes others");
        int par = Modifier.parameterModifiers();
        check(Modifier.isFinal(par), "parameterModifiers contains final");
        check(!Modifier.isPublic(par) && !Modifier.isStatic(par) && !Modifier.isVolatile(par),
              "parameterModifiers minimal");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
