/*
 * @test
 * @summary org.omg.CORBA.Environment conformance: abstract class shape.
 * @run main EnvironmentTest
 */

public class EnvironmentTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.Environment");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");

        // real instance from the offline ORB factory
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.Environment env = orb.create_environment();
        check(env != null, "create_environment works offline");

        // ---- exception slot lifecycle ----
        check(env.exception() == null, "fresh Environment holds null exception");
        RuntimeException first = new RuntimeException("first");
        env.exception(first);
        check(env.exception() == first, "exception() returns the exact instance set");
        check("first".equals(env.exception().getMessage()), "message intact");

        org.omg.CORBA.BAD_PARAM second = new org.omg.CORBA.BAD_PARAM("second", 7,
                org.omg.CORBA.CompletionStatus.COMPLETED_YES);
        env.exception(second);
        check(env.exception() == second, "second set replaces the first");
        check(((org.omg.CORBA.SystemException) env.exception()).minor == 7, "replaced exception fields intact");

        env.exception(null);
        check(env.exception() == null, "exception(null) clears the slot");

        env.exception(first);
        env.clear();
        check(env.exception() == null, "clear() resets the slot to null");

        // set/clear cycles stay independent across two environments
        org.omg.CORBA.Environment env2 = orb.create_environment();
        env.exception(first);
        check(env2.exception() == null, "two environments have independent slots");
        env2.exception(second);
        check(env.exception() == first && env2.exception() == second, "no cross-talk between environments");
        env.clear();
        check(env2.exception() == second, "clearing one does not touch the other");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
