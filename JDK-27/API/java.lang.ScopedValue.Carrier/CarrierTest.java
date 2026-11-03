/*
 * @test
 * @summary java.lang.ScopedValue.Carrier conformance: white-box structure + black-box behavior.
 * @run main CarrierTest
 */
import java.io.IOException;
import java.lang.ScopedValue;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.NoSuchElementException;

public class CarrierTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ScopedValue$Carrier");
        check(Modifier.isFinal(c.getModifiers()), "Carrier is final");
        check(!c.isInterface(), "Carrier is a class");
        check(c.getConstructors().length == 0, "no public constructors (obtained via ScopedValue.where)");
        for (String n : new String[]{"where", "get", "call", "run"}) {
            boolean found = false;
            for (Method m : c.getMethods()) if (m.getName().equals(n)) found = true;
            check(found, n + "() declared public");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBindings() {
        ScopedValue<String> a = ScopedValue.newInstance();
        ScopedValue<Integer> b = ScopedValue.newInstance();
        ScopedValue.Carrier c = ScopedValue.where(a, "x");
        check("x".equals(c.get(a)), "single binding");
        ScopedValue.Carrier two = c.where(b, 1);
        check("x".equals(two.get(a)) && Integer.valueOf(1).equals(two.get(b)), "two bindings");
        // where() on the same key in the derived carrier overrides for the new carrier.
        ScopedValue.Carrier overridden = two.where(a, "y");
        check("y".equals(overridden.get(a)), "later where() overrides the earlier binding");
        check("x".equals(two.get(a)), "the original carrier is unaffected");
    }

    static void testCallAndRun() throws Exception {
        ScopedValue<String> a = ScopedValue.newInstance();
        ScopedValue<Integer> b = ScopedValue.newInstance();
        ScopedValue.Carrier c = ScopedValue.where(a, "x").where(b, 1);
        check("x".equals(c.call(() -> a.get())), "call sees bindings");
        c.run(() -> check("x".equals(a.get()) && b.get() == 1, "run sees bindings"));
        check(!a.isBound() && !b.isBound(), "unbound outside the scope");
        // Nested rebinding inside the scope restores the outer value afterwards.
        String seen = c.call(() -> ScopedValue.where(a, "inner").call(a::get));
        check("inner".equals(seen), "nested scope overrides");
        String after = c.call(a::get);
        check("x".equals(after), "outer binding restored after nested scope");
        try {
            a.get();
            throw new AssertionError("get() outside scope: expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            passed++;
        }
    }

    static void testCheckedException() {
        ScopedValue<String> a = ScopedValue.newInstance();
        ScopedValue.Carrier c = ScopedValue.where(a, "x");
        try {
            c.call((ScopedValue.CallableOp<Void, IOException>) () -> { throw new IOException("boom"); });
            throw new AssertionError("call: expected IOException");
        } catch (IOException expected) {
            check("boom".equals(expected.getMessage()), "checked exception propagates through call");
        }
        check(!a.isBound(), "scope unwound after a throwing call");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBindings();
        testCallAndRun();
        testCheckedException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
