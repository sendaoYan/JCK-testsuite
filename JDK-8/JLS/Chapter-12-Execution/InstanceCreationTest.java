/*
 * @test
 * @summary JLS 12.5 (Java SE 8): new instance creation runs the superclass
 *          constructor (including its instance initializers) before the subclass
 *          field initializers and constructor body.
 * @run main InstanceCreationTest
 */

public class InstanceCreationTest {
    static StringBuilder log = new StringBuilder();

    static class A {
        { log.append("A-init"); }
        A() { log.append("A-ctor"); }
    }
    static class B extends A {
        int f = initF();
        int initF() { log.append("B-init"); return 1; }
        B() { log.append("B-ctor"); }
    }

    public static void main(String[] args) {
        B b = new B();
        if (!log.toString().equals("A-initA-ctorB-initB-ctor"))
            throw new AssertionError("init order: " + log);
        if (b.f != 1) throw new AssertionError("field initialized");
        System.out.println("PASS");
    }
}
