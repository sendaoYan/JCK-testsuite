/*
 * @test
 * @summary com.sun.corba.se.impl.interceptors.PIHandlerImpl conformance: type shape via reflection (headless-safe).
 * @run main PIHandlerImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class PIHandlerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.interceptors.PIHandlerImpl", false, PIHandlerImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.protocol.PIHandler"), "interface com.sun.corba.se.spi.protocol.PIHandler");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 27, "public declared method count");
        check(mn.contains("adapterManagerStateChanged"), "method adapterManagerStateChanged");
        check(mn.contains("adapterStateChanged"), "method adapterStateChanged");
        check(mn.contains("allocateServerRequestId"), "method allocateServerRequestId");
        check(mn.contains("cleanupClientPIRequest"), "method cleanupClientPIRequest");
        check(mn.contains("cleanupServerPIRequest"), "method cleanupServerPIRequest");
        check(mn.contains("close"), "method close");
        check(mn.contains("create_policy"), "method create_policy");
        check(mn.contains("destroyInterceptors"), "method destroyInterceptors");
        check(mn.contains("disableInterceptorsThisThread"), "method disableInterceptorsThisThread");
        check(mn.contains("enableInterceptorsThisThread"), "method enableInterceptorsThisThread");
        check(mn.contains("getPICurrent"), "method getPICurrent");
        check(mn.contains("handleClientPIEndingPoint"), "method handleClientPIEndingPoint");
        check(mn.contains("initialize"), "method initialize");
        check(mn.contains("initializeServerPIInfo"), "method initializeServerPIInfo");
        check(mn.contains("initiateClientPIRequest"), "method initiateClientPIRequest");
        check(mn.contains("invokeClientPIEndingPoint"), "method invokeClientPIEndingPoint");
        check(mn.contains("invokeClientPIStartingPoint"), "method invokeClientPIStartingPoint");
        check(mn.contains("invokeServerPIEndingPoint"), "method invokeServerPIEndingPoint");
        check(mn.contains("invokeServerPIIntermediatePoint"), "method invokeServerPIIntermediatePoint");
        check(mn.contains("invokeServerPIStartingPoint"), "method invokeServerPIStartingPoint");
        check(mn.contains("makeCompletedClientRequest"), "method makeCompletedClientRequest");
        check(mn.contains("objectAdapterCreated"), "method objectAdapterCreated");
        check(mn.contains("registerPolicyFactory"), "method registerPolicyFactory");
        check(mn.contains("register_interceptor"), "method register_interceptor");
        check(mn.contains("setClientPIInfo"), "method setClientPIInfo");
        check(mn.contains("setServerPIExceptionInfo"), "method setServerPIExceptionInfo");
        check(mn.contains("setServerPIInfo"), "method setServerPIInfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
