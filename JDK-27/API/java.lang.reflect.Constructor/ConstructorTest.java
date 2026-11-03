/*
 * @test
 * @summary java.lang.reflect.Constructor conformance: white-box structure + black-box behavior.
 * @run main ConstructorTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

public class ConstructorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sample {
        public int x;

        public Sample() {
            x = 0;
        }

        public Sample(int v) {
            x = v;
        }

        private Sample(String s) {
            x = s.length();
        }
    }

    static class Widget {
        final String tag;
        final int n;

        public Widget() { this("none", 0); }
        public Widget(String tag, int n) { this.tag = tag; this.n = n; }
        public Widget(String... parts) { this(String.join("-", parts), parts.length); }
        private Widget(boolean boom) {
            if (boom) throw new IllegalArgumentException("ctor-boom");
            this.tag = "b";
            this.n = -1;
        }
    }

    static class Gadget<T> {
        public final T payload;
        public Gadget(T payload) { this.payload = payload; }
    }

    abstract static class AbstractThing {
    }

    interface Marker {
    }

    enum Color {
        RED, GREEN
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() throws Exception {
        Constructor<Sample> c = Sample.class.getConstructor(int.class);
        check(c.getParameterTypes().length == 1, "getParameterTypes length");
        check(c.getParameterCount() == 1, "getParameterCount");
        check(c.getModifiers() == Modifier.PUBLIC, "public modifier");
        check(c.getName().equals(Sample.class.getName()), "getName");
        check(c.getDeclaringClass() == Sample.class, "getDeclaringClass");
        Sample s = c.newInstance(42);
        check(s.x == 42, "newInstance with args");
        Sample s0 = Sample.class.getConstructor().newInstance();
        check(s0.x == 0, "newInstance no-arg");
        check(Sample.class.getConstructors().length == 2, "getConstructors length");
        check(Sample.class.getDeclaredConstructors().length == 3, "getDeclaredConstructors length");
        check(c.toString() != null, "toString");
        check(c.toGenericString() != null, "toGenericString");
        check(Modifier.isFinal(Constructor.class.getModifiers()), "Constructor is final");
        check(Constructor.class.getSuperclass().getName().equals("java.lang.reflect.Executable"),
              "Constructor extends Executable");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. exact-signature construction and state verification
        Constructor<Widget> twoArg = Widget.class.getConstructor(String.class, int.class);
        Widget w = twoArg.newInstance("a", 2);
        check("a".equals(w.tag) && w.n == 2, "newInstance(String,int) state");
        check(twoArg.getParameterCount() == 2, "twoArg param count");
        check(twoArg.getParameterTypes()[0] == String.class
              && twoArg.getParameterTypes()[1] == int.class, "twoArg param types");
        check(twoArg.getModifiers() == Modifier.PUBLIC, "twoArg public");
        check(twoArg.getExceptionTypes().length == 0, "no declared exceptions");
        check(Widget.class.getConstructor().newInstance().tag.equals("none"), "no-arg chaining");

        // 2. varargs constructor
        Constructor<Widget> va = Widget.class.getConstructor(String[].class);
        check(va.isVarArgs(), "varargs ctor isVarArgs");
        check(va.getParameterTypes()[0] == String[].class, "varargs ctor param type");
        Widget w2 = va.newInstance(new Object[]{new String[]{"x", "y"}});
        check("x-y".equals(w2.tag) && w2.n == 2, "varargs packed newInstance");
        Widget w3 = va.newInstance((Object) new String[]{"single"});
        check("single".equals(w3.tag) && w3.n == 1, "varargs single array");

        // 3. constructor that throws: wrapped in InvocationTargetException
        Constructor<Widget> boom = Widget.class.getDeclaredConstructor(boolean.class);
        try {
            boom.newInstance(true);
            check(false, "throwing ctor must wrap");
        } catch (InvocationTargetException e) {
            check(e.getCause() instanceof IllegalArgumentException, "cause exact type");
            check("ctor-boom".equals(e.getCause().getMessage()), "cause message");
        }
        boom.setAccessible(true);
        Widget quiet = boom.newInstance(false);
        check("b".equals(quiet.tag) && quiet.n == -1, "private ctor after setAccessible");

        // 4. private constructor access control (different nest)
        Constructor<CtorSeparate> priv = (Constructor<CtorSeparate>) CtorSeparate.class.getDeclaredConstructor();
        check(Modifier.isPrivate(priv.getModifiers()), "ctor private modifier");
        try {
            priv.newInstance();
            check(false, "private ctor must be denied");
        } catch (IllegalAccessException e) {
            check(true, "IllegalAccessException private ctor");
        }
        priv.setAccessible(true);
        check(priv.newInstance() != null, "private ctor after setAccessible");

        // 5. abstract class cannot be instantiated; interfaces have no ctors; enums reject newInstance
        Constructor<?> abs = AbstractThing.class.getDeclaredConstructor();
        try { abs.newInstance(); check(false, "abstract must fail"); }
        catch (InstantiationException e) { check(true, "InstantiationException abstract class"); }
        check(Marker.class.getDeclaredConstructors().length == 0, "interface has no constructors");
        Constructor<Color> ec = (Constructor<Color>) (Constructor<?>) Color.class.getDeclaredConstructor(String.class, int.class);
        try { ec.newInstance("r", 1); check(false, "enum ctor must fail"); }
        catch (IllegalArgumentException e) { check(true, "IllegalArgumentException enum newInstance"); }

        // 6. argument mismatches
        try { twoArg.newInstance("only-one"); check(false, "arity mismatch must fail"); }
        catch (IllegalArgumentException e) { check(true, "IllegalArgumentException arity"); }
        try { twoArg.newInstance("a", "not-an-int"); check(false, "type mismatch must fail"); }
        catch (IllegalArgumentException e) { check(true, "IllegalArgumentException type"); }

        // 7. generic constructor / annotated return
        Constructor<Gadget<String>> g = (Constructor<Gadget<String>>) (Constructor<?>) Gadget.class.getConstructor(Object.class);
        check(g.getParameterTypes()[0] == Object.class, "generic ctor erased param");
        check(g.newInstance("p").payload.equals("p"), "generic ctor newInstance");
        check(g.getTypeParameters().length == 0, "ctor own type parameters empty");
        check(g.getAnnotatedReturnType().getType() == Gadget.class, "annotated return declaring type");

        // 8. equality and string forms
        Constructor<Widget> twoArg2 = Widget.class.getConstructor(String.class, int.class);
        check(twoArg.equals(twoArg2), "Constructor equals by value");
        check(twoArg.hashCode() == twoArg2.hashCode(), "Constructor hashCode consistent");
        check(!twoArg.equals(va), "different ctors not equal");
        check(twoArg.toString().contains("Widget"), "toString contains class name");
        check(va.toGenericString().contains("Widget"), "toGenericString contains class name");
        check(Widget.class.getDeclaredConstructors().length == 4, "declared ctor count");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

// second top-level class = a different nest: its private constructor is not
// accessible to ConstructorTest, exercising the access-control paths.
class CtorSeparate {
    private CtorSeparate() { }
}
