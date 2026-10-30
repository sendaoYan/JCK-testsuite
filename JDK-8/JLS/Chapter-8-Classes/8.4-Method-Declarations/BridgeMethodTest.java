/*
 * @test
 * @summary JLS 8.4.8.3/15.12.4.5 (Java SE 8): when a method overrides a generic
 *          method and its erasure differs, the compiler generates a synthetic
 *          bridge method with the erased return type; the bridge delegates to the
 *          real method and both dispatch to the same implementation.
 * @run main BridgeMethodTest
 */
import java.lang.reflect.*;

interface Node<T> { T data(); }
class StringNode implements Node<String> {
    public String data() { return "x"; }
}

public class BridgeMethodTest {
    public static void main(String[] args) throws Exception {
        Method real = null, bridge = null;
        for (Method m : StringNode.class.getDeclaredMethods()) {
            if (!m.getName().equals("data")) continue;
            if (m.getReturnType() == Object.class && m.isBridge()) bridge = m;
            else if (m.getReturnType() == String.class && !m.isBridge()) real = m;
        }
        if (real == null) throw new AssertionError("real data() returning String not found");
        if (bridge == null) throw new AssertionError("bridge data() returning Object not found");
        if (!bridge.isSynthetic()) throw new AssertionError("bridge method must be synthetic");

        StringNode node = new StringNode();
        if (!"x".equals(real.invoke(node))) throw new AssertionError("real method invoke");
        if (!"x".equals(bridge.invoke(node))) throw new AssertionError("bridge method invoke");

        // Invocation through the generic interface type dispatches to the override.
        Node<String> n = new StringNode();
        if (!"x".equals(n.data())) throw new AssertionError("interface dispatch");

        System.out.println("PASS");
    }
}
