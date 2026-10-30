/*
 * @test
 * @summary JLS 12.4.1 (Example 12.4.1-1): a superclass is initialized before its
 *          subclass; a class that is not actively used is not initialized.
 * @run main SuperclassInitializedBeforeSubclass
 */

public class SuperclassInitializedBeforeSubclass {
    static final StringBuilder log = new StringBuilder();

    static class Super { static { log.append("Super "); } }
    static class One   { static { log.append("One "); } }
    static class Two extends Super { static { log.append("Two "); } }

    public static void main(String[] args) {
        One o = null;          // One is never initialized
        Two t = new Two();     // Two initialized after Super
        if (o != null || t == null) throw new AssertionError("instances");
        if (!"Super Two ".equals(log.toString()))
            throw new AssertionError("initialization order: " + log);
        System.out.println("PASS");
    }
}
