/*
 * @test
 * @summary com.sun.corba.se.impl.transport.SocketOrChannelConnectionImpl conformance: type shape via reflection (headless-safe).
 * @run main SocketOrChannelConnectionImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SocketOrChannelConnectionImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.transport.SocketOrChannelConnectionImpl", false, SocketOrChannelConnectionImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.transport.EventHandlerBase"), "superclass com.sun.corba.se.impl.transport.EventHandlerBase");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.orbutil.threadpool.Work"), "interface com.sun.corba.se.spi.orbutil.threadpool.Work");
        check(in.contains("com.sun.corba.se.spi.transport.CorbaConnection"), "interface com.sun.corba.se.spi.transport.CorbaConnection");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 66, "public declared method count");
        check(mn.contains("clientReply_1_1_Get"), "method clientReply_1_1_Get");
        check(mn.contains("clientReply_1_1_Put"), "method clientReply_1_1_Put");
        check(mn.contains("clientReply_1_1_Remove"), "method clientReply_1_1_Remove");
        check(mn.contains("clientRequestMapGet"), "method clientRequestMapGet");
        check(mn.contains("close"), "method close");
        check(mn.contains("closeConnectionResources"), "method closeConnectionResources");
        check(mn.contains("createOutputObject"), "method createOutputObject");
        check(mn.contains("doWork"), "method doWork");
        check(mn.contains("dprint"), "method dprint");
        check(mn.contains("getAcceptor"), "method getAcceptor");
        check(mn.contains("getBroker"), "method getBroker");
        check(mn.contains("getChannel"), "method getChannel");
        check(mn.contains("getCodeBase"), "method getCodeBase");
        check(mn.contains("getCodeBaseIOR"), "method getCodeBaseIOR");
        check(mn.contains("getCodeSetContext"), "method getCodeSetContext");
        check(mn.contains("getConnection"), "method getConnection");
        check(mn.contains("getConnectionCache"), "method getConnectionCache");
        check(mn.contains("getContactInfo"), "method getContactInfo");
        check(mn.contains("getEnqueueTime"), "method getEnqueueTime");
        check(mn.contains("getEventHandler"), "method getEventHandler");
        check(mn.contains("getInterestOps"), "method getInterestOps");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getNextRequestId"), "method getNextRequestId");
        check(mn.contains("getResponseWaitingRoom"), "method getResponseWaitingRoom");
        check(mn.contains("getSocket"), "method getSocket");
        check(mn.contains("getSocketChannel"), "method getSocketChannel");
        check(mn.contains("getTimeStamp"), "method getTimeStamp");
        check(mn.contains("handleEvent"), "method handleEvent");
        check(mn.contains("isBusy"), "method isBusy");
        check(mn.contains("isPostInitialContexts"), "method isPostInitialContexts");
        check(mn.contains("isServer"), "method isServer");
        check(mn.contains("purgeCalls"), "method purgeCalls");
        check(mn.contains("read"), "method read");
        check(mn.contains("readFully"), "method readFully");
        check(mn.contains("registerWaiter"), "method registerWaiter");
        check(mn.contains("sendCancelRequest"), "method sendCancelRequest");
        check(mn.contains("sendCancelRequestWithLock"), "method sendCancelRequestWithLock");
        check(mn.contains("sendCloseConnection"), "method sendCloseConnection");
        check(mn.contains("sendMessageError"), "method sendMessageError");
        check(mn.contains("sendWithoutLock"), "method sendWithoutLock");
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
        check(mn.contains("setConnectionCache"), "method setConnectionCache");
        check(mn.contains("setEnqueueTime"), "method setEnqueueTime");
        check(mn.contains("setPostInitialContexts"), "method setPostInitialContexts");
        check(mn.contains("setState"), "method setState");
        check(mn.contains("setTimeStamp"), "method setTimeStamp");
        check(mn.contains("setUseSelectThreadToWait"), "method setUseSelectThreadToWait");
        check(mn.contains("shouldReadGiopHeaderOnly"), "method shouldReadGiopHeaderOnly");
        check(mn.contains("shouldRegisterReadEvent"), "method shouldRegisterReadEvent");
        check(mn.contains("shouldRegisterServerReadEvent"), "method shouldRegisterServerReadEvent");
        check(mn.contains("shouldUseDirectByteBuffers"), "method shouldUseDirectByteBuffers");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("unregisterWaiter"), "method unregisterWaiter");
        check(mn.contains("waitForResponse"), "method waitForResponse");
        check(mn.contains("write"), "method write");
        check(mn.contains("writeLock"), "method writeLock");
        check(mn.contains("writeUnlock"), "method writeUnlock");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
