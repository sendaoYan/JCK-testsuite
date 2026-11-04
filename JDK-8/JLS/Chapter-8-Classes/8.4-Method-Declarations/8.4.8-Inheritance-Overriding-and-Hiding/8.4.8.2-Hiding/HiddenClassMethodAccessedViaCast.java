/*
 * @test
 * @summary JLS 8.4.8.2 (Java SE 8): a hidden class method is invoked based on the compile-
 *          time type of the reference; a qualified name or a cast to the
 *          superclass type accesses the hidden method.
 * @run main HiddenClassMethodAccessedViaCast
 */
class Super { static String greeting() { return "Goodnight"; } }
class Sub extends Super { static String greeting() { return "Hello"; } }
public class HiddenClassMethodAccessedViaCast {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) {
        Super s = new Sub();
        check(s.greeting().equals("Goodnight"), "hidden class method invoked via reference type");
        check(((Super) s).greeting().equals("Goodnight"), "cast to superclass accesses hidden method");
        check(Sub.greeting().equals("Hello"), "qualified name accesses hiding method");
        check(Super.greeting().equals("Goodnight"), "qualified name accesses hidden method");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
