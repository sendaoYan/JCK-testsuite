/*
 * @test
 * @summary javax.net.ssl.SSLEngineResult.Status conformance: enum surface.
 * @run main StatusTest
 */
import java.lang.reflect.*;
import javax.net.ssl.*;
public class StatusTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLEngineResult$Status");
        check(c.isEnum(), "is enum");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()), "public final");
        check(c.getSuperclass() == java.lang.Enum.class, "extends Enum");
        SSLEngineResult.Status[] vs = SSLEngineResult.Status.values();
        check(vs.length == 4, "4 constants, found " + vs.length);
        java.util.Set<String> names = new java.util.TreeSet<>();
        for (SSLEngineResult.Status v : vs) names.add(v.name());
        check(names.equals(new java.util.TreeSet<>(java.util.Arrays.asList(
                "BUFFER_UNDERFLOW", "BUFFER_OVERFLOW", "OK", "CLOSED"))),
                "exact constant set: " + names);
        for (SSLEngineResult.Status v : vs) {
            check(SSLEngineResult.Status.valueOf(v.name()) == v, "valueOf round trip " + v);
            check(v.name().equals(v.toString()), "name == toString for " + v);
        }
        Class<?> enclosing = c.getEnclosingClass();
        check(enclosing != null && enclosing.getName().equals("javax.net.ssl.SSLEngineResult"),
                "nested in SSLEngineResult");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
