/*
 * @test
 * @summary java.lang.reflect.Member conformance: white-box structure + black-box behavior,
 *          plus the deepen batch: PUBLIC/DECLARED constants driving lookup semantics,
 *          Field/Method/Constructor uniform views with exact modifier combos, interface
 *          constant modifiers and value, synthetic members (bridge method, this$0),
 *          inherited-member declaring class and the accessFlags() default method (JDK 20+).
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

    // ---- fixtures added by the deepen batch ----

    static class BaseM {
        public int pubF;

        public void pubM() {
        }
    }

    static class DerivedM extends BaseM {
        @SuppressWarnings("unused")
        private int privF;
    }

    interface ConstIface {
        String K = "kv";
    }

    static class Mods {
        public static final int CONST = 7;
        private int hid;

        protected void prot() {
        }
    }

    static class BridgeBase<T> {
        public T id(T t) {
            return t;
        }
    }

    static class BridgeStr extends BridgeBase<String> {
        public String id(String s) {
            return s;
        }
    }

    static class MOuter {
        private int anchor = 1;

        class MIn {
            int use() {
                return anchor;
            }
        }
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

    // ---- tests added by the deepen batch ----

    static void testConstantsDriveLookups() throws Exception {
        check(Member.PUBLIC == 0, "PUBLIC == 0");
        check(Member.DECLARED == 1, "DECLARED == 1");
        // PUBLIC lookups see inherited public members; DECLARED lookups are
        // class-local and include private members.
        boolean pubFViaPublic = false, pubFViaDeclared = false, privViaDeclared = false;
        for (Field f : DerivedM.class.getFields()) {
            if (f.getName().equals("pubF")) pubFViaPublic = true;
        }
        for (Field f : DerivedM.class.getDeclaredFields()) {
            if (f.getName().equals("pubF")) pubFViaDeclared = true;
            if (f.getName().equals("privF")) privViaDeclared = true;
        }
        check(pubFViaPublic, "PUBLIC lookup sees the inherited public field");
        check(!pubFViaDeclared, "DECLARED lookup excludes inherited fields");
        check(privViaDeclared, "DECLARED lookup includes private fields");
        boolean pubM = false;
        for (Method m : DerivedM.class.getMethods()) {
            if (m.getName().equals("pubM")) pubM = true;
        }
        check(pubM, "PUBLIC lookup sees the inherited public method");
        check(DerivedM.class.getMethod("pubM").getDeclaringClass() == BaseM.class,
                "inherited member declaring class is the base class");
    }

    static void testFieldViews() throws Exception {
        Member f = Mods.class.getField("CONST");
        check("CONST".equals(f.getName()), "field name");
        check(f.getDeclaringClass() == Mods.class, "field declaring class");
        check(f.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL),
                "CONST modifiers are PUBLIC|STATIC|FINAL");
        check(!f.isSynthetic(), "source field not synthetic");
        Member i = ConstIface.class.getField("K");
        check(i.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL),
                "interface constant implicitly public static final");
        check("K".equals(i.getName()), "interface constant name");
        check("kv".equals(((Field) i).get(null)), "interface constant value readable");
        Member p = Mods.class.getDeclaredField("hid");
        check(p.getModifiers() == Modifier.PRIVATE, "private field modifiers");
        Member pr = Mods.class.getDeclaredMethod("prot");
        check(pr.getModifiers() == Modifier.PROTECTED, "protected method modifiers");
    }

    static void testSyntheticMembers() throws Exception {
        Member bridge = null;
        for (Method m : BridgeStr.class.getDeclaredMethods()) {
            if (m.getName().equals("id") && m.getParameterTypes()[0] == Object.class) {
                bridge = m;
            }
        }
        check(bridge != null && bridge.isSynthetic(),
                "bridge member isSynthetic via Member view");
        Member real = BridgeStr.class.getMethod("id", String.class);
        check(!real.isSynthetic(), "source member not synthetic");
        boolean this0 = false;
        for (Field f : MOuter.MIn.class.getDeclaredFields()) {
            if (f.getName().equals("this$0") && f.isSynthetic()) this0 = true;
        }
        check(this0, "inner class this$0 is a synthetic member");
    }

    static void testAccessFlagsDefault() throws Exception {
        // the accessFlags() default method (JDK 20+) is reachable through the
        // Member interface view and mirrors getModifiers().
        Member f = Mods.class.getField("CONST");
        Set<AccessFlag> af = f.accessFlags();
        check(af.contains(AccessFlag.PUBLIC) && af.contains(AccessFlag.STATIC)
                && af.contains(AccessFlag.FINAL) && af.size() == 3,
                "accessFlags via Member view exact set");
        Member pr = Mods.class.getDeclaredMethod("prot");
        check(pr.accessFlags().size() == 1
                && pr.accessFlags().contains(AccessFlag.PROTECTED),
                "protected method accessFlags via Member view");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantsDriveLookups();
        testFieldViews();
        testSyntheticMembers();
        testAccessFlagsDefault();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
