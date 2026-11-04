/*
 * @test
 * @summary JLS 5.1.6/5.1.6.3 (Java SE 8): narrowing reference conversions
 *          require a run-time test; if the actual value is not a legitimate
 *          value of the target type, ClassCastException is thrown. Examples:
 *          Object -> any type, interface -> non-final class, array covariance.
 * @run main NarrowingReferenceConversionRunTime
 */

class Base {}
class Sub extends Base {}
interface I {}
class Impl implements I {}
class FinalImpl implements I {}

public class NarrowingReferenceConversionRunTime {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Throwing { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> ex, Throwing r, String msg) throws Exception {
        try {
            r.run();
        } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + ex.getSimpleName() + " got " + t, t);
        }
        throw new AssertionError(msg + ": expected " + ex.getSimpleName() + " but nothing thrown");
    }

    public static void main(String[] args) throws Exception {
        // Object -> a class: narrowing reference conversion.
        Object o = new Sub();
        Sub s = (Sub) o;
        check(s != null, "Object -> Sub narrowing");

        // A failing narrowing reference conversion throws ClassCastException.
        Object wrong = new Object();
        checkThrows(ClassCastException.class, () -> { Sub x = (Sub) wrong; },
                    "Object -> Sub where value is not a Sub");

        // Interface -> non-final class: narrowing reference conversion.
        I i = new Impl();
        Impl impl = (Impl) i;
        check(impl != null, "interface -> non-final class narrowing");

        // Array covariance: Base[] -> Sub[] is a narrowing reference conversion
        // that fails at run time if the actual array component type is broader.
        Base[] ba = new Base[1];
        checkThrows(ClassCastException.class, () -> { Sub[] sa = (Sub[]) ba; },
                    "Base[] -> Sub[] where actual component is Base");

        Base[] realSubs = new Sub[1];
        realSubs[0] = new Sub();
        Sub[] sa = (Sub[]) realSubs;
        check(sa[0] != null, "Base[] -> Sub[] succeeds when actual component is Sub");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
