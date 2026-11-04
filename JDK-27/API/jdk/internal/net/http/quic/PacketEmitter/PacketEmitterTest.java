/*
 * @test
 * @summary jdk.internal.net.http.quic.PacketEmitter conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main PacketEmitterTest
 */
import java.lang.reflect.*;
import java.util.*;
public class PacketEmitterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.PacketEmitter", false, PacketEmitterTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.PacketEmitter"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass-null");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("acknowledged"), "method:acknowledged");
        check(methods.contains("checkAbort"), "method:checkAbort");
        check(methods.contains("emitAckPacket"), "method:emitAckPacket");
        check(methods.contains("executor"), "method:executor");
        check(methods.contains("isOpen"), "method:isOpen");
        check(methods.contains("logTag"), "method:logTag");
        check(methods.contains("ptoBackoffIncreased"), "method:ptoBackoffIncreased");
        check(methods.contains("reschedule"), "method:reschedule");
        check(methods.contains("retransmit"), "method:retransmit");
        check(methods.contains("sendData"), "method:sendData");
        check(methods.contains("timer"), "method:timer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
