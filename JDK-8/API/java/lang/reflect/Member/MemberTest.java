/*
 * @test
 * @summary java.lang.reflect.Member conformance: declaring class/name/modifiers and constants,
 *          plus the deepen batch: PUBLIC/DECLARED constants driving lookup semantics,
 *          Field/Method/Constructor uniform views with exact modifier combos, interface
 *          constant modifiers, synthetic members (bridge method, this$0), inherited-member
 *          declaring class, and the JDK-8 API surface lock (no accessFlags()).
 * @run main MemberTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MemberTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static class Sample {
        public int f;
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

    static void testMember() throws Exception {
        Member m = Sample.class.getField("f");
        check(m.getDeclaringClass() == Sample.class, "getDeclaringClass");
        check("f".equals(m.getName()), "getName");
        check(m.getModifiers() == Modifier.PUBLIC, "getModifiers");
        check(!m.isSynthetic(), "isSynthetic false");
        check(Member.PUBLIC == 0, "PUBLIC == 0");
        check(Member.DECLARED == 1, "DECLARED == 1");
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
        check(Member.class.isInterface(), "Member is an interface");
    }

    static void testKindsAndDispatch() throws Exception {
        Member f = Mods.class.getField("CONST");
        Member m = Mods.class.getDeclaredMethod("prot");
        Member c = Mods.class.getDeclaredConstructor();
        check(f instanceof Field && m instanceof Method && c instanceof Constructor,
                "member kinds dispatch to Field/Method/Constructor");
        check(c.getName().equals(Mods.class.getName()),
                "constructor name is the class name");
        check(f.toString().contains("CONST") && m.toString().contains("prot"),
                "toString contains member names");
        check(Mods.class.getField("CONST").equals(f),
                "Member view equals the reflective object");
        check(Modifier.toString(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)
                .equals("public static final"), "modifier combo rendering");
    }

    static void testApiSurfaceLocked() throws Exception {
        // Member gained the default accessFlags() method only in JDK 20.
        checkThrows(NoSuchMethodException.class,
                () -> Member.class.getMethod("accessFlags"),
                "no accessFlags on JDK-8 Member");
    }

    public static void main(String[] args) throws Exception {
        testMember();
        testConstantsDriveLookups();
        testFieldViews();
        testSyntheticMembers();
        testKindsAndDispatch();
        testApiSurfaceLocked();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
