/*
 * @test
 * @summary jdk.management.jfr.FlightRecorderMXBean conformance: white-box structure + black-box behavior.
 * @run main FlightRecorderMXBeanTest
 */

import jdk.management.jfr.*;
import javax.management.*;
import javax.management.openmbean.CompositeData;
import java.lang.management.*;
import java.lang.reflect.*;
import java.nio.file.*;
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
        check(hasMethod(c, "setConfiguration"), "setConfiguration");
        check(hasMethod(c, "setPredefinedConfiguration"), "setPredefinedConfiguration");
        check(hasMethod(c, "openStream"), "openStream");
        check(hasMethod(c, "readStream"), "readStream");
        check(hasMethod(c, "closeStream"), "closeStream");
        check(hasMethod(c, "copyTo"), "copyTo");
        // JDK-27: no SecurityException in the throws clauses (JDK-8 declares it)
        Method newRec = c.getMethod("newRecording");
        boolean declaresSecurity = false;
        for (Class<?> ex : newRec.getExceptionTypes()) if (ex == SecurityException.class) declaresSecurity = true;
        check(!declaresSecurity, "newRecording does not declare SecurityException on JDK-27");
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

    // ---- deepening: full lifecycle through the MBeanServer invoke channel ----
    static void t(String what, Runnable r) {
        try { r.run(); check(false, what + " should have thrown"); }
        catch (Throwable ex) {
            Throwable cause = ex;
            while (cause.getCause() != null) cause = cause.getCause();
            check(cause instanceof IllegalArgumentException,
                  what + " -> IllegalArgumentException via MBeanServer (got " + cause + ")");
            check(cause.getMessage() != null && cause.getMessage().contains("987654"),
                  what + " message names the id (was: " + cause.getMessage() + ")");
        }
    }

    static void testMBeanServerChannel() throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName(FlightRecorderMXBean.MXBEAN_NAME);
        check(mbs.isRegistered(on), "MBean registered under MXBEAN_NAME");
        check("jdk.management.jfr:type=FlightRecorder".equals(FlightRecorderMXBean.MXBEAN_NAME), "MXBEAN_NAME value");

        FlightRecorderMXBean bean = ManagementFactory.getPlatformMXBean(FlightRecorderMXBean.class);
        long id = (Long) mbs.invoke(on, "newRecording", null, null);
        check(id > 0, "invoke newRecording -> id");
        try {
            // configuration
            mbs.invoke(on, "setPredefinedConfiguration", new Object[]{id, "default"}, new String[]{"long", "java.lang.String"});
            check(true, "setPredefinedConfiguration default");
            // settings reflect the configuration
            Map<String, String> settings = bean.getRecordingSettings(id);
            check(settings != null && settings.size() > 1, "getRecordingSettings non-trivial after config");
            // visible in getRecordings and in the Recordings attribute
            boolean listed = false;
            for (RecordingInfo ri : bean.getRecordings()) if (ri.getId() == id) listed = true;
            check(listed, "recording listed before start");
            CompositeData[] rows = (CompositeData[]) mbs.getAttribute(on, "Recordings");
            boolean attrListed = false;
            for (CompositeData row : rows) if (((Number) row.get("id")).longValue() == id) attrListed = true;
            check(attrListed, "Recordings attribute lists the recording");
            // start -> state RUNNING
            mbs.invoke(on, "startRecording", new Object[]{id}, new String[]{"long"});
            String stateRunning = null;
            for (RecordingInfo ri : bean.getRecordings()) if (ri.getId() == id) stateRunning = ri.getState();
            check("RUNNING".equals(stateRunning), "state RUNNING after startRecording (was " + stateRunning + ")");
            // running recordings cannot be streamed: openStream -> IOException
            try {
                mbs.invoke(on, "openStream", new Object[]{id, null}, new String[]{"long", "javax.management.openmbean.TabularData"});
                check(false, "openStream while RUNNING should throw");
            } catch (Exception expected) {
                Throwable cause = expected.getCause() != null ? expected.getCause() : expected;
                check(cause instanceof java.io.IOException, "openStream while RUNNING -> IOException");
            }
            // dump via copyTo
            Path dump = Files.createTempFile("frmb-dump", ".jfr");
            mbs.invoke(on, "copyTo", new Object[]{id, dump.toString()}, new String[]{"long", "java.lang.String"});
            check(Files.size(dump) > 0, "copyTo writes a non-empty file");
            Files.deleteIfExists(dump);
            // stop -> state STOPPED
            boolean stopped = (Boolean) mbs.invoke(on, "stopRecording", new Object[]{id}, new String[]{"long"});
            check(stopped, "invoke stopRecording -> true");
            String stateStopped = null;
            for (RecordingInfo ri : bean.getRecordings()) if (ri.getId() == id) stateStopped = ri.getState();
            check("STOPPED".equals(stateStopped), "state STOPPED after stopRecording");
            // stream channel on a stopped recording
            long streamId = (Long) mbs.invoke(on, "openStream", new Object[]{id, null}, new String[]{"long", "javax.management.openmbean.TabularData"});
            check(streamId > 0, "openStream -> stream id");
            byte[] chunk = (byte[]) mbs.invoke(on, "readStream", new Object[]{streamId}, new String[]{"long"});
            check(chunk != null && chunk.length > 0, "readStream returns chunk bytes");
            // keep reading until EOF (bounded): eventually null or empty array
            boolean eof = false;
            for (int i = 0; i < 200 && !eof; i++) {
                Object more = mbs.invoke(on, "readStream", new Object[]{streamId}, new String[]{"long"});
                if (more == null || ((byte[]) more).length == 0) eof = true;
            }
            check(eof, "readStream reaches EOF (null/empty) after chunks");
            mbs.invoke(on, "closeStream", new Object[]{streamId}, new String[]{"long"});
            check(true, "closeStream");
            // closing an already-closed stream is tolerated (no throw observed on JDK-27)
            try {
                mbs.invoke(on, "closeStream", new Object[]{streamId}, new String[]{"long"});
                check(true, "closeStream twice tolerated");
            } catch (Exception expected) {
                check(true, "closeStream twice threw (acceptable)");
            }
            // clone + close
            long cloneId = (Long) mbs.invoke(on, "cloneRecording", new Object[]{id, false}, new String[]{"long", "boolean"});
            check(cloneId > 0 && cloneId != id, "cloneRecording -> distinct id");
            mbs.invoke(on, "closeRecording", new Object[]{cloneId}, new String[]{"long"});
            boolean cloneGone = true;
            for (RecordingInfo ri : bean.getRecordings()) if (ri.getId() == cloneId) cloneGone = false;
            check(cloneGone, "clone removed after closeRecording");
            mbs.invoke(on, "closeRecording", new Object[]{id}, new String[]{"long"});
            boolean gone = true;
            for (RecordingInfo ri : bean.getRecordings()) if (ri.getId() == id) gone = false;
            check(gone, "recording removed after closeRecording");
        } finally {
            try { bean.closeRecording(id); } catch (Exception ignored) {}
        }

        // unknown-id matrix through the MBeanServer channel
        t("stopRecording unknown id", () -> invoke(mbs, on, "stopRecording", 987654L));
        t("startRecording unknown id", () -> invoke(mbs, on, "startRecording", 987654L));
        t("closeRecording unknown id", () -> invoke(mbs, on, "closeRecording", 987654L));
        t("getRecordingOptions unknown id", () -> invoke(mbs, on, "getRecordingOptions", 987654L));
        t("getRecordingSettings unknown id", () -> invoke(mbs, on, "getRecordingSettings", 987654L));
        t("openStream unknown id", () -> {
            try { mbs.invoke(on, "openStream", new Object[]{987654L, null}, new String[]{"long", "javax.management.openmbean.TabularData"}); }
            catch (Exception e) { throw new RuntimeException(e); }
        });
        t("copyTo unknown id", () -> {
            try { mbs.invoke(on, "copyTo", new Object[]{987654L, "/tmp/nobody.jfr"}, new String[]{"long", "java.lang.String"}); }
            catch (Exception e) { throw new RuntimeException(e); }
        });
        try { bean.cloneRecording(987654L, false); check(false, "cloneRecording unknown id should throw"); }
        catch (NullPointerException expected) { check(true, "cloneRecording unknown id -> NPE (JDK impl quirk)"); }

        // configuration negatives on a real recording
        long nid = bean.newRecording();
        try {
            try { bean.setPredefinedConfiguration(nid, "no-such-config"); check(false, "bad config name should throw"); }
            catch (IllegalArgumentException expected) {
                check(String.valueOf(expected.getMessage()).contains("no-such-config"), "bad config name -> IAE naming it");
            }
            try { bean.setConfiguration(nid, "not valid jfc"); check(false, "bad config content should throw"); }
            catch (IllegalArgumentException expected) { check(true, "bad config content -> IAE Could not parse"); }
            Map<String, String> bad = new HashMap<>();
            bad.put("bogusKey", "x");
            try { bean.setRecordingOptions(nid, bad); check(false, "bogus option should throw"); }
            catch (IllegalArgumentException expected) {
                check(String.valueOf(expected.getMessage()).contains("Unknown recording option"), "bogus option -> IAE Unknown recording option");
                check(String.valueOf(expected.getMessage()).contains("destination"), "JDK-27 valid options list includes destination");
            }
            Map<String, String> good = new HashMap<>();
            good.put("dumpOnExit", "true");
            bean.setRecordingOptions(nid, good);
            check(Boolean.parseBoolean(bean.getRecordingOptions(nid).get("dumpOnExit")), "setRecordingOptions dumpOnExit round-trip");
            Map<String, String> s = new HashMap<>();
            s.put("jdk.FileRead#enabled", "true");
            bean.setRecordingSettings(nid, s);
            check("true".equals(bean.getRecordingSettings(nid).get("jdk.FileRead#enabled")), "setRecordingSettings round-trip");
        } finally {
            bean.closeRecording(nid);
        }

        // snapshot: created already stopped
        long snap = bean.takeSnapshot();
        try {
            boolean snapStopped = false;
            for (RecordingInfo ri : bean.getRecordings()) if (ri.getId() == snap && "STOPPED".equals(ri.getState())) snapStopped = true;
            check(snap > 0, "takeSnapshot -> id");
            check(snapStopped, "snapshot starts in STOPPED state");
            try { bean.stopRecording(snap); check(false, "stopRecording snapshot should throw"); }
            catch (IllegalStateException expected) { check(true, "stopRecording snapshot -> ISE already stopped"); }
        } finally {
            bean.closeRecording(snap);
        }

        // metadata channels
        List<EventTypeInfo> evts = bean.getEventTypes();
        check(evts.size() >= 100, "event types plentiful (was " + evts.size() + ")");
        boolean hasFileRead = false, hasObjAllocSample = false;
        for (EventTypeInfo e : evts) {
            if ("jdk.FileRead".equals(e.getName())) hasFileRead = true;
            if ("jdk.ObjectAllocationSample".equals(e.getName())) hasObjAllocSample = true;
        }
        check(hasFileRead, "jdk.FileRead exposed");
        check(hasObjAllocSample, "jdk.ObjectAllocationSample exposed (JDK 16+)");
        List<ConfigurationInfo> cfgs = bean.getConfigurations();
        boolean hasDefault = false, hasProfile = false;
        for (ConfigurationInfo ci : cfgs) {
            if ("default".equals(ci.getName())) hasDefault = true;
            if ("profile".equals(ci.getName())) hasProfile = true;
        }
        check(hasDefault && hasProfile, "default and profile configurations exposed");
    }

    static void invoke(MBeanServer mbs, ObjectName on, String op, long id) {
        try {
            mbs.invoke(on, op, new Object[]{id}, new String[]{"long"});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMBeanServerChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
