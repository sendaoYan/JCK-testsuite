/*
 * @test
 * @summary JVMS 5.5: a class is initialized at first active use; its superclass is
 *          initialized first, and each class is initialized at most once.
 * @run main InitializationOrder
 */

public class InitializationOrder {
    static StringBuilder log = new StringBuilder();
    static int init(String s) { log.append(s); return 1; }

    static class Parent { static int p = init("P"); }
    static class Child extends Parent { static int c = init("C"); }

    public static void main(String[] args) {
        int v = Child.c;          // first active use: Parent then Child
        if (!"PC".equals(log.toString()))
            throw new AssertionError("initialization order: " + log);

        log.setLength(0);
        v = Child.c;              // already initialized: no re-initialization
        if (!"".equals(log.toString()))
            throw new AssertionError("re-initialized: " + log);

        System.out.println("PASS");
    }
}
