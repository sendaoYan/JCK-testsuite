/*
 * @test
 * @summary JLS 6.5.7.1 (Java SE 27): a simple method name in a method invocation
 *          refers to the visible method with that name and compatible signature.
 * @run main SimpleMethodName
 */

public class SimpleMethodName {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    int add(int a, int b) { return a + b; }
    static int twice(int a) { return a * 2; }

    public static void main(String[] args) {
        SimpleMethodName t = new SimpleMethodName();
        check(t.add(2, 3) == 5, "instance method by simple name");
        check(twice(4) == 8, "static method by simple name");
        check(Math.max(3, 7) == 7, "static method by qualified name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
