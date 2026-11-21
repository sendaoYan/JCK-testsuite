/*
 * @test
 * @summary JVMS 4.10.1.9 / 5.4.4: invokevirtual on a private method of an
 *          unrelated class passes verification but fails access control at
 *          resolution with IllegalAccessError.
 * @compile PrivateMethodAccess.jasm
 * @run main PrivateMethodAccessTest
 */

public class PrivateMethodAccessTest {
    static boolean causedBy(Throwable t, Class<? extends Throwable> cls) {
        for (Throwable x = t; x != null; x = x.getCause())
            if (cls.isInstance(x)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> victim = Class.forName("PrivateMethodVictim");
        Class<?> attacker = Class.forName("PrivateMethodAttacker");
        Object v = victim.getDeclaredConstructor().newInstance();
        Object at = attacker.getDeclaredConstructor().newInstance();

        boolean threw = false;
        try {
            attacker.getMethod("steal", victim).invoke(at, v);
        } catch (Throwable t) {
            threw = causedBy(t, IllegalAccessError.class);
        }
        if (!threw) {
            throw new AssertionError("private method across classes not rejected with IllegalAccessError");
        }

        System.out.println("PASS");
    }
}
