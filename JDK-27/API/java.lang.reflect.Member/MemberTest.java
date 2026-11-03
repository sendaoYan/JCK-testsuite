/*
 * @test
 * @summary java.lang.reflect.Member conformance: white-box structure + black-box behavior.
 * @run main MemberTest
 */

import java.lang.reflect.AccessFlag;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

public class MemberTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sample {
        public int f;
    }

    static class Biz {
        public static final int CONST = 7;
        public synchronized int compute(int x) { return x + 1; }
        private int hidden;
    }

    static class Cmp implements Comparable<Cmp> {
        public int compareTo(Cmp o) { return 0; }
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() throws Exception {
        Member m = Sample.class.getField("f");
        check(m.getDeclaringClass() == Sample.class, "getDeclaringClass");
        check("f".equals(m.getName()), "getName");
        check(m.getModifiers() == Modifier.PUBLIC, "getModifiers");
        check(!m.isSynthetic(), "isSynthetic false");
        check(Member.PUBLIC == 0, "PUBLIC == 0");
        check(Member.DECLARED == 1, "DECLARED == 1");
        check(Member.class.isInterface(), "Member is an interface");
        check(Member.class.getDeclaredMethods().length >= 4, "core abstract methods");
        boolean implByField = false;
        for (Class<?> c : Field.class.getInterfaces()) if (c == Member.class) implByField = true;
        check(implByField, "Field directly implements Member");
        check(Member.class.isAssignableFrom(Method.class)
              && Member.class.isAssignableFrom(Constructor.class),
              "Method/Constructor implement Member via Executable");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. uniform Member view over the three reflective member kinds
        Member field = Biz.class.getField("CONST");
        Member method = Biz.class.getMethod("compute", int.class);
        Member ctor = Biz.class.getDeclaredConstructor();
        check("CONST".equals(field.getName()) && "compute".equals(method.getName()),
              "names via Member view");
        check(ctor.getName().equals(Biz.class.getName()), "ctor name is class name");
        check(field.getDeclaringClass() == Biz.class && method.getDeclaringClass() == Biz.class
              && ctor.getDeclaringClass() == Biz.class, "declaring classes");
        check(field.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL),
              "field modifiers exact combo");
        check(method.getModifiers() == (Modifier.PUBLIC | Modifier.SYNCHRONIZED),
              "method modifiers exact combo");
        check(ctor.getModifiers() == 0, "package-private ctor modifiers are empty");

        // 2. isSynthetic distinguishes source and compiler members
        check(!field.isSynthetic() && !method.isSynthetic() && !ctor.isSynthetic(),
              "source members not synthetic");
        Member bridge = null;
        for (Method mm : Cmp.class.getDeclaredMethods())
            if (mm.getParameterTypes()[0] == Object.class) bridge = mm;
        check(bridge != null && bridge.isSynthetic(), "bridge member isSynthetic via Member view");

        // 3. default accessFlags() maps modifiers to flag sets
        Set<AccessFlag> ff = field.accessFlags();
        check(ff.contains(AccessFlag.PUBLIC) && ff.contains(AccessFlag.STATIC)
              && ff.contains(AccessFlag.FINAL) && ff.size() == 3, "field accessFlags exact set");
        check(method.accessFlags().contains(AccessFlag.PUBLIC)
              && method.accessFlags().contains(AccessFlag.SYNCHRONIZED), "method accessFlags");
        check(ctor.accessFlags().isEmpty(), "package-private ctor accessFlags empty");

        // 4. Member view equals underlying object and supports instanceof dispatch
        Field realField = Biz.class.getField("CONST");
        check(field.equals(realField) && field.hashCode() == realField.hashCode(),
              "Member view equals reflective object");
        check(field instanceof Field && method instanceof Method && ctor instanceof Constructor,
              "instanceof kinds");
        check(field.toString().contains("CONST"), "toString contains name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
