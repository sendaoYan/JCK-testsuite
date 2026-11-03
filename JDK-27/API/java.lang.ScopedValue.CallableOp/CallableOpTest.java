/*
 * @test
 * @summary java.lang.ScopedValue.CallableOp conformance: white-box structure + black-box behavior.
 * @run main CallableOpTest
 */
import java.io.IOException;
import java.lang.ScopedValue;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class CallableOpTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ScopedValue$CallableOp");
        check(c.isInterface(), "CallableOp is an interface");
        check(c.getTypeParameters().length == 2, "two type parameters <T, X extends Throwable>");
        Method call = c.getMethod("call");
        check(call.getReturnType() == Object.class, "call() returns T (erased Object)");
        check(call.getGenericExceptionTypes().length == 1
              && call.getGenericExceptionTypes()[0].getTypeName().contains("X"),
              "call() throws X (the type variable)");
        check(Modifier.isPublic(call.getModifiers()) && Modifier.isAbstract(call.getModifiers()),
              "call() is public abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testLambda() throws Exception {
        ScopedValue.CallableOp<String, RuntimeException> op = () -> "x";
        check("x".equals(op.call()), "call() returns the lambda value");
        ScopedValue.CallableOp<Integer, RuntimeException> fortyTwo = () -> 42;
        check(fortyTwo.call() == 42, "call() returns the boxed int");
    }

    static void testCheckedPropagation() {
        ScopedValue.CallableOp<Void, IOException> thrower = () -> { throw new IOException("boom"); };
        try {
            thrower.call();
            throw new AssertionError("call(): expected the checked IOException to propagate");
        } catch (IOException expected) {
            check("boom".equals(expected.getMessage()), "checked exception propagates with message");
        }
    }

    static void testUsedByCarrier() {
        ScopedValue<String> key = ScopedValue.newInstance();
        String result = ScopedValue.where(key, "v").call((ScopedValue.CallableOp<String, RuntimeException>) key::get);
        check("v".equals(result), "CallableOp is the callback type of Carrier.call");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testLambda();
        testCheckedPropagation();
        testUsedByCarrier();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
