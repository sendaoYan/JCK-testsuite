/*
 * @test
 * @summary com.sun.xml.internal.stream.events.CharacterEvent conformance: value class behavior.
 * @run main CharacterEventTest
 */

public class CharacterEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.xml.internal.stream.events.CharacterEvent v = new com.sun.xml.internal.stream.events.CharacterEvent("hello");
        check("hello".equals(v.getData()), "getData");
        check(!v.isCData(), "not CDATA");
        check(new com.sun.xml.internal.stream.events.CharacterEvent("cd", true).isCData(), "CDATA flag");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
