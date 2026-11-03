/*
 * @test
 * @summary jdk.management.jfr.FlightRecorderMXBean conformance: white-box structure + black-box behavior.
 * @run main FlightRecorderMXBeanTest
 */

import jdk.management.jfr.*;
import javax.management.*;
import java.lang.management.*;
import java.lang.reflect.*;
import java.util.*;
public class FlightRecorderMXBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.management.jfr.FlightRecorderMXBean");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(PlatformManagedObject.class.isAssignableFrom(c), "extends PlatformManagedObject");
        check(hasMethod(c, "newRecording"), "newRecording");
        check(hasMethod(c, "takeSnapshot"), "takeSnapshot");
        check(hasMethod(c, "cloneRecording"), "cloneRecording");
        check(hasMethod(c, "startRecording"), "startRecording");
        check(hasMethod(c, "stopRecording"), "stopRecording");
        check(hasMethod(c, "closeRecording"), "closeRecording");
        check(hasMethod(c, "getRecordings"), "getRecordings");
        check(hasMethod(c, "getConfigurations"), "getConfigurations");
        check(hasMethod(c, "getEventTypes"), "getEventTypes");
        check(hasMethod(c, "setRecordingOptions"), "setRecordingOptions");
        check(hasMethod(c, "getRecordingOptions"), "getRecordingOptions");

    }

    static void testBlackBox() throws Exception {
        FlightRecorderMXBean bean = ManagementFactory.getPlatformMXBean(FlightRecorderMXBean.class);
        check(bean != null, "bean present");
        check(FlightRecorderMXBean.MXBEAN_NAME != null, "MXBEAN_NAME");
        check(bean.getRecordings() != null, "getRecordings");
        check(bean.getConfigurations() != null, "getConfigurations");
        check(bean.getEventTypes() != null, "getEventTypes");
        long id = bean.newRecording();
        check(id > 0, "newRecording id");
        bean.startRecording(id);
        check(bean.stopRecording(id), "stopRecording");
        bean.closeRecording(id);

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
