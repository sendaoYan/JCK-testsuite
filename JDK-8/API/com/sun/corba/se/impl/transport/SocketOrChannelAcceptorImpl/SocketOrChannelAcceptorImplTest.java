/*
 * @test
 * @summary com.sun.corba.se.impl.transport.SocketOrChannelAcceptorImpl conformance: type shape via reflection (headless-safe).
 * @run main SocketOrChannelAcceptorImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SocketOrChannelAcceptorImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.transport.SocketOrChannelAcceptorImpl", false, SocketOrChannelAcceptorImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.transport.EventHandlerBase"), "superclass com.sun.corba.se.impl.transport.EventHandlerBase");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.legacy.connection.LegacyServerSocketEndPointInfo"), "interface com.sun.corba.se.spi.legacy.connection.LegacyServerSocketEndPointInfo");
        check(in.contains("com.sun.corba.se.spi.orbutil.threadpool.Work"), "interface com.sun.corba.se.spi.orbutil.threadpool.Work");
        check(in.contains("com.sun.corba.se.spi.transport.CorbaAcceptor"), "interface com.sun.corba.se.spi.transport.CorbaAcceptor");
        check(in.contains("com.sun.corba.se.spi.transport.SocketInfo"), "interface com.sun.corba.se.spi.transport.SocketInfo");
        check(in.contains("com.sun.corba.se.spi.transport.SocketOrChannelAcceptor"), "interface com.sun.corba.se.spi.transport.SocketOrChannelAcceptor");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 33, "public declared method count");
        check(mn.contains("accept"), "method accept");
        check(mn.contains("addToIORTemplate"), "method addToIORTemplate");
        check(mn.contains("close"), "method close");
        check(mn.contains("createInputObject"), "method createInputObject");
        check(mn.contains("createMessageMediator"), "method createMessageMediator");
        check(mn.contains("createOutputObject"), "method createOutputObject");
        check(mn.contains("doWork"), "method doWork");
        check(mn.contains("finishCreatingMessageMediator"), "method finishCreatingMessageMediator");
        check(mn.contains("getAcceptor"), "method getAcceptor");
        check(mn.contains("getChannel"), "method getChannel");
        check(mn.contains("getConnection"), "method getConnection");
        check(mn.contains("getConnectionCache"), "method getConnectionCache");
        check(mn.contains("getConnectionCacheType"), "method getConnectionCacheType");
        check(mn.contains("getEnqueueTime"), "method getEnqueueTime");
        check(mn.contains("getEventHandler"), "method getEventHandler");
        check(mn.contains("getHost"), "method getHost");
        check(mn.contains("getHostName"), "method getHostName");
        check(mn.contains("getInterestOps"), "method getInterestOps");
        check(mn.contains("getLocatorPort"), "method getLocatorPort");
        check(mn.contains("getMonitoringName"), "method getMonitoringName");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getObjectAdapterId"), "method getObjectAdapterId");
        check(mn.contains("getObjectAdapterManagerId"), "method getObjectAdapterManagerId");
        check(mn.contains("getPort"), "method getPort");
        check(mn.contains("getServerSocket"), "method getServerSocket");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("initialize"), "method initialize");
        check(mn.contains("initialized"), "method initialized");
        check(mn.contains("setConnectionCache"), "method setConnectionCache");
        check(mn.contains("setEnqueueTime"), "method setEnqueueTime");
        check(mn.contains("setLocatorPort"), "method setLocatorPort");
        check(mn.contains("shouldRegisterAcceptEvent"), "method shouldRegisterAcceptEvent");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
