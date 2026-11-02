/*
 * @test
 * @summary com.sun.corba.se.impl.logging.UtilSystemException conformance: type shape via reflection (headless-safe).
 * @run main UtilSystemExceptionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class UtilSystemExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.logging.UtilSystemException", false, UtilSystemExceptionTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.spi.logging.LogWrapperBase"), "superclass com.sun.corba.se.spi.logging.LogWrapperBase");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 30, "public declared method count");
        check(mn.contains("badBeginUnmarshalCustomValue"), "method badBeginUnmarshalCustomValue");
        check(mn.contains("badHexDigit"), "method badHexDigit");
        check(mn.contains("classCastExceptionInLoadStub"), "method classCastExceptionInLoadStub");
        check(mn.contains("classNotFound"), "method classNotFound");
        check(mn.contains("connectNoTie"), "method connectNoTie");
        check(mn.contains("connectTieNoServant"), "method connectTieNoServant");
        check(mn.contains("connectTieWrongOrb"), "method connectTieWrongOrb");
        check(mn.contains("connectWrongOrb"), "method connectWrongOrb");
        check(mn.contains("couldNotLoadStub"), "method couldNotLoadStub");
        check(mn.contains("errorInMakeStubFromRepositoryId"), "method errorInMakeStubFromRepositoryId");
        check(mn.contains("errorSetBooleanField"), "method errorSetBooleanField");
        check(mn.contains("errorSetByteField"), "method errorSetByteField");
        check(mn.contains("errorSetCharField"), "method errorSetCharField");
        check(mn.contains("errorSetDoubleField"), "method errorSetDoubleField");
        check(mn.contains("errorSetFloatField"), "method errorSetFloatField");
        check(mn.contains("errorSetIntField"), "method errorSetIntField");
        check(mn.contains("errorSetLongField"), "method errorSetLongField");
        check(mn.contains("errorSetObjectField"), "method errorSetObjectField");
        check(mn.contains("errorSetShortField"), "method errorSetShortField");
        check(mn.contains("exceptionInLoadStub"), "method exceptionInLoadStub");
        check(mn.contains("get"), "method get");
        check(mn.contains("illegalFieldAccess"), "method illegalFieldAccess");
        check(mn.contains("invalidIndirection"), "method invalidIndirection");
        check(mn.contains("loadTieFailed"), "method loadTieFailed");
        check(mn.contains("noPoa"), "method noPoa");
        check(mn.contains("objectNotConnected"), "method objectNotConnected");
        check(mn.contains("objectNotExported"), "method objectNotExported");
        check(mn.contains("stubFactoryCouldNotMakeStub"), "method stubFactoryCouldNotMakeStub");
        check(mn.contains("unableLocateValueHelper"), "method unableLocateValueHelper");
        check(mn.contains("unknownSysex"), "method unknownSysex");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
