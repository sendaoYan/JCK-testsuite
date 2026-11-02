/*
 * @test
 * @summary com.sun.corba.se.spi.transport.CorbaConnection conformance: type shape via reflection (headless-safe).
 * @run main CorbaConnectionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CorbaConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.transport.CorbaConnection", false, CorbaConnectionTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.pept.transport.Connection"), "interface com.sun.corba.se.pept.transport.Connection");
        check(in.contains("com.sun.corba.se.spi.legacy.connection.Connection"), "interface com.sun.corba.se.spi.legacy.connection.Connection");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 34, "public declared method count");
        check(mn.contains("clientReply_1_1_Get"), "method clientReply_1_1_Get");
        check(mn.contains("clientReply_1_1_Put"), "method clientReply_1_1_Put");
        check(mn.contains("clientReply_1_1_Remove"), "method clientReply_1_1_Remove");
        check(mn.contains("clientRequestMapGet"), "method clientRequestMapGet");
        check(mn.contains("closeConnectionResources"), "method closeConnectionResources");
        check(mn.contains("dprint"), "method dprint");
        check(mn.contains("getBroker"), "method getBroker");
        check(mn.contains("getCodeBase"), "method getCodeBase");
        check(mn.contains("getCodeBaseIOR"), "method getCodeBaseIOR");
        check(mn.contains("getCodeSetContext"), "method getCodeSetContext");
        check(mn.contains("getNextRequestId"), "method getNextRequestId");
        check(mn.contains("getResponseWaitingRoom"), "method getResponseWaitingRoom");
        check(mn.contains("getSocketChannel"), "method getSocketChannel");
        check(mn.contains("isPostInitialContexts"), "method isPostInitialContexts");
        check(mn.contains("purgeCalls"), "method purgeCalls");
        check(mn.contains("read"), "method read");
        check(mn.contains("sendCancelRequest"), "method sendCancelRequest");
        check(mn.contains("sendCancelRequestWithLock"), "method sendCancelRequestWithLock");
        check(mn.contains("sendCloseConnection"), "method sendCloseConnection");
        check(mn.contains("sendMessageError"), "method sendMessageError");
        check(mn.contains("serverRequestMapGet"), "method serverRequestMapGet");
        check(mn.contains("serverRequestMapPut"), "method serverRequestMapPut");
        check(mn.contains("serverRequestMapRemove"), "method serverRequestMapRemove");
        check(mn.contains("serverRequestProcessingBegins"), "method serverRequestProcessingBegins");
        check(mn.contains("serverRequestProcessingEnds"), "method serverRequestProcessingEnds");
        check(mn.contains("serverRequest_1_1_Get"), "method serverRequest_1_1_Get");
        check(mn.contains("serverRequest_1_1_Put"), "method serverRequest_1_1_Put");
        check(mn.contains("serverRequest_1_1_Remove"), "method serverRequest_1_1_Remove");
        check(mn.contains("setCodeBaseIOR"), "method setCodeBaseIOR");
        check(mn.contains("setCodeSetContext"), "method setCodeSetContext");
        check(mn.contains("setPostInitialContexts"), "method setPostInitialContexts");
        check(mn.contains("shouldReadGiopHeaderOnly"), "method shouldReadGiopHeaderOnly");
        check(mn.contains("shouldUseDirectByteBuffers"), "method shouldUseDirectByteBuffers");
        check(mn.contains("write"), "method write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
