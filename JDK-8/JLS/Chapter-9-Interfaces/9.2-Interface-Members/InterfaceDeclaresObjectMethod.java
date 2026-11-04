/*
 * @test
 * @summary JLS 9.2 (Java SE 8): an interface may declare an abstract method with the
 *          same signature as a public method of Object; the class then overrides it.
 * @run main InterfaceDeclaresObjectMethod
 */

public class InterfaceDeclaresObjectMethod {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface I {
        String toString();   // redeclares Object.toString
    }

    static class Impl implements I {
        public String toString() { return "custom"; }
    }

    public static void main(String[] args) {
        I i = new Impl();
        check(i.toString().equals("custom"), "interface-declared Object method");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
