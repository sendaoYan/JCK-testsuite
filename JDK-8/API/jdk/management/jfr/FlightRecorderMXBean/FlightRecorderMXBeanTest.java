/*
 * @test
 * @summary jdk.management.jfr.FlightRecorderMXBean conformance: reflection shape + dual-channel lifecycle.
 * @run main FlightRecorderMXBeanTest
 */

import jdk.management.jfr.*;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;

import javax.management.*;

public class FlightRecorderMXBeanTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }
    /** unwrap reflection/JSR-282 wrappers until the root cause is found */
    static Throwable root(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        return t;
    }

    public static void main(String[] args) throws Exception {
        // ---------- original shape checks (kept verbatim) ----------
        Class<?> c = Class.forName("jdk.management.jfr.FlightRecorderMXBean");
        check(c != null, "jdk.management.jfr.FlightRecorderMXBean loads");
        check(c.getName().equals("jdk.management.jfr.FlightRecorderMXBean"), "binary name is jdk.management.jfr.FlightRecorderMXBean");
        check(Modifier.isPublic(c.getModifiers()), "jdk.management.jfr.FlightRecorderMXBean is public");
        check(c.isInterface(), "jdk.management.jfr.FlightRecorderMXBean is an interface");
        check(ifaceNames(c).equals(set("java.lang.management.PlatformManagedObject")), "direct superinterfaces");
        check(declares(c, "newRecording"), "declares newRecording");
        check(declares(c, "takeSnapshot"), "declares takeSnapshot");
        check(declares(c, "cloneRecording"), "declares cloneRecording");
        check(declares(c, "startRecording"), "declares startRecording");
        check(declares(c, "stopRecording"), "declares stopRecording");
        check(declares(c, "closeRecording"), "declares closeRecording");

        // ---------- JDK-8 specific surface ----------
        // 8u262 backport declares SecurityException on the mutating operations (JDK-27 dropped it)
        for (String n : new String[]{"newRecording", "cloneRecording", "startRecording", "stopRecording", "copyTo"}) {
            boolean sawSec = false;
            for (Method m : c.getMethods()) {
                if (!m.getName().equals(n)) continue;
                for (Class<?> ex : m.getExceptionTypes()) if (ex == SecurityException.class) sawSec = true;
            }
            check(sawSec, n + " declares SecurityException (JDK-8 surface)");
        }
        // stream operations throw IOException (verified signature)
        for (String n : new String[]{"openStream", "readStream", "closeStream", "closeRecording", "copyTo"}) {
            boolean sawIO = false;
            for (Method m : c.getMethods()) {
                if (!m.getName().equals(n)) continue;
                for (Class<?> ex : m.getExceptionTypes()) if (ex == java.io.IOException.class) sawIO = true;
            }
            check(sawIO, n + " declares IOException");
        }
        check("jdk.management.jfr:type=FlightRecorder".equals(c.getField("MXBEAN_NAME").get(null)), "MXBEAN_NAME constant");

        // ---------- direct-channel lifecycle ----------
        FlightRecorderMXBean bean = ManagementFactory.getPlatformMXBean(FlightRecorderMXBean.class);
        check(bean != null, "obtainable via ManagementFactory");
        javax.management.ObjectName on = bean.getObjectName();
        check(on != null && "jdk.management.jfr:type=FlightRecorder".equals(on.toString()), "getObjectName");

        long id = bean.newRecording();
        try {
            // default options map (JDK-8: option key is "disk", no "destination" option exists)
            Map<String, String> opts = bean.getRecordingOptions(id);
            check(opts.size() == 6, "default options map has 6 entries, got " + opts.size());
            check("true".equals(opts.get("disk")), "disk=true default");
            check("false".equals(opts.get("dumpOnExit")), "dumpOnExit=false default");
            check("0".equals(opts.get("maxAge")) && "0".equals(opts.get("maxSize")) && "0".equals(opts.get("duration")), "zero duration/maxAge/maxSize defaults");
            check(opts.get("name") != null && !opts.get("name").isEmpty(), "default name non-empty");

            bean.setPredefinedConfiguration(id, "default");
            Map<String, String> settings = bean.getRecordingSettings(id);
            check(settings.size() > 100, "default configuration yields full settings map (" + settings.size() + ")");
            // setRecordingSettings replaces the whole map (subset semantics, verified)
            Map<String, String> sub = new HashMap<String, String>();
            sub.put("jdk.CPULoad#enabled", "true");
            sub.put("jdk.CPULoad#period", "1 s");
            bean.setRecordingSettings(id, sub);
            Map<String, String> after = bean.getRecordingSettings(id);
            check(after.size() == 2 && "true".equals(after.get("jdk.CPULoad#enabled")) && "1 s".equals(after.get("jdk.CPULoad#period")),
                  "setRecordingSettings replaces settings map");

            bean.startRecording(id);
            RecordingInfo ri = null;
            for (RecordingInfo r : bean.getRecordings()) if (r.getId() == id) ri = r;
            check(ri != null && "RUNNING".equals(ri.getState()), "RUNNING listed while started");
            check(ri.getStartTime() > 0, "startTime > 0 while running");

            Path dump = Files.createTempFile("frmxbean8", ".jfr");
            Files.deleteIfExists(dump);
            bean.copyTo(id, dump.toString());
            check(Files.size(dump) > 0, "copyTo wrote non-empty file while running");
            Files.deleteIfExists(dump);

            check(bean.stopRecording(id) == true, "stopRecording -> true");
            for (RecordingInfo r : bean.getRecordings()) if (r.getId() == id) ri = r;
            check("STOPPED".equals(ri.getState()), "STOPPED after stop");
            // verified: JDK-8 throws ISE on re-stop of a STOPPED recording
            try { bean.stopRecording(id); check(false, "stopRecording twice should throw ISE"); }
            catch (IllegalStateException e) { check("Can't stop an already stopped recording.".equals(e.getMessage()), "re-stop ISE message"); }
        } finally {
            bean.closeRecording(id);
        }
        boolean gone = true;
        for (RecordingInfo r : bean.getRecordings()) if (r.getId() == id) gone = false;
        check(gone, "recording removed after closeRecording");

        // ---------- unknown recording id matrix (direct channel) ----------
        final long ghost = 987654L;
        String want = "No recording available with id 987654";
        try { bean.getRecordingOptions(ghost); check(false, "getRecordingOptions(ghost) should throw"); }
        catch (IllegalArgumentException e) { check(want.equals(e.getMessage()), "getRecordingOptions(ghost) IAE msg"); }
        try { bean.startRecording(ghost); check(false, "startRecording(ghost) should throw"); }
        catch (IllegalArgumentException e) { check(want.equals(e.getMessage()), "startRecording(ghost) IAE msg"); }
        try { bean.stopRecording(ghost); check(false, "stopRecording(ghost) should throw"); }
        catch (IllegalArgumentException e) { check(want.equals(e.getMessage()), "stopRecording(ghost) IAE msg"); }
        try { bean.closeRecording(ghost); check(false, "closeRecording(ghost) should throw"); }
        catch (IllegalArgumentException e) { check(want.equals(e.getMessage()), "closeRecording(ghost) IAE msg"); }
        try { bean.openStream(ghost, null); check(false, "openStream(ghost) should throw"); }
        catch (IllegalArgumentException e) { check(want.equals(e.getMessage()), "openStream(ghost) IAE msg"); }
        try { bean.copyTo(ghost, "/tmp/never.jfr"); check(false, "copyTo(ghost) should throw"); }
        catch (IllegalArgumentException e) { check(want.equals(e.getMessage()), "copyTo(ghost) IAE msg"); }

        // configuration negatives
        try { bean.setPredefinedConfiguration(ghost, "default"); check(false, "setPredefinedConfiguration(ghost) should throw"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("987654"), "setPredefinedConfiguration(ghost) IAE mentions id"); }
        long tmp = bean.newRecording();
        try {
            try { bean.setPredefinedConfiguration(tmp, "no-such-config"); check(false, "unknown config name should throw"); }
            catch (IllegalArgumentException e) { check(e.getMessage() != null, "unknown config IAE has message"); }
            try { bean.setConfiguration(tmp, "this is not xml"); check(false, "bad config text should throw"); }
            catch (IllegalArgumentException e) { check("Could not parse configuration".equals(e.getMessage()), "setConfiguration bad text IAE msg"); }
        } finally { bean.closeRecording(tmp); }
        // option negatives (JDK-8 valid options: dumpOnExit, duration, name, maxAge, maxSize, disk)
        tmp = bean.newRecording();
        try {
            try {
                Map<String, String> bad = new HashMap<String, String>(); bad.put("nosuchoption", "1");
                bean.setRecordingOptions(tmp, bad); check(false, "bogus option should throw");
            } catch (IllegalArgumentException e) {
                check(e.getMessage().startsWith("Unknown recording option: nosuchoption"), "bogus option IAE prefix");
            }
            try {
                Map<String, String> dest = new HashMap<String, String>(); dest.put("destination", "/tmp/x.jfr");
                bean.setRecordingOptions(tmp, dest); check(false, "destination should be unknown on JDK-8");
            } catch (IllegalArgumentException e) {
                check(e.getMessage().startsWith("Unknown recording option: destination"), "destination unknown on JDK-8");
            }
        } finally { bean.closeRecording(tmp); }

        // ---------- MBeanServer channel: full sequence with open signature ----------
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        check(mbs.isRegistered(on), "registered in platform MBeanServer");
        // openStream MBean signature is (long, javax.management.openmbean.TabularData)
        MBeanInfo info = mbs.getMBeanInfo(on);
        boolean sawOpenStreamTabular = false, sawNewRecording = false;
        for (MBeanOperationInfo op : info.getOperations()) {
            if ("openStream".equals(op.getName()) && op.getSignature().length == 2
                && "long".equals(op.getSignature()[0].getType())
                && "javax.management.openmbean.TabularData".equals(op.getSignature()[1].getType())) sawOpenStreamTabular = true;
            if ("newRecording".equals(op.getName())) sawNewRecording = true;
        }
        check(sawOpenStreamTabular, "openStream MBean signature (long, TabularData)");
        check(sawNewRecording, "newRecording exposed as operation");

        Long mid = (Long) mbs.invoke(on, "newRecording", null, null);
        check(mid != null && mid.longValue() > 0, "newRecording via MBeanServer");
        try {
            mbs.invoke(on, "setPredefinedConfiguration", new Object[]{mid, "default"}, new String[]{"long", "java.lang.String"});
            mbs.invoke(on, "startRecording", new Object[]{mid}, new String[]{"long"});
            check(true, "startRecording via MBeanServer");
            Path dump = Files.createTempFile("frmxbean8-mbs", ".jfr");
            Files.deleteIfExists(dump);
            mbs.invoke(on, "copyTo", new Object[]{mid, dump.toString()}, new String[]{"long", "java.lang.String"});
            check(Files.size(dump) > 0, "copyTo via MBeanServer");
            mbs.invoke(on, "stopRecording", new Object[]{mid}, new String[]{"long"});
            check(true, "stopRecording via MBeanServer");
            // stream a stopped recording to EOF (bounded loop)
            Long sid = (Long) mbs.invoke(on, "openStream", new Object[]{mid, null}, new String[]{"long", "javax.management.openmbean.TabularData"});
            check(sid != null && sid.longValue() > 0, "openStream via MBeanServer");
            long total = 0; int emptyRound = 0;
            while (emptyRound < 100) {
                byte[] chunk = (byte[]) mbs.invoke(on, "readStream", new Object[]{sid}, new String[]{"long"});
                if (chunk == null) break;
                if (chunk.length == 0) { emptyRound++; continue; }
                total += chunk.length; emptyRound = 0;
            }
            check(total > 0, "readStream via MBeanServer returned " + total + " bytes before EOF");
            mbs.invoke(on, "closeStream", new Object[]{sid}, new String[]{"long"});
            check(true, "closeStream via MBeanServer");
            Files.deleteIfExists(dump);
        } finally {
            mbs.invoke(on, "closeRecording", new Object[]{mid}, new String[]{"long"});
        }
        // unknown id through MBeanServer wraps the IAE
        try {
            mbs.invoke(on, "startRecording", new Object[]{ghost}, new String[]{"long"});
            check(false, "MBeanServer startRecording(ghost) should throw");
        } catch (Exception e) {
            Throwable r = root(e);
            check(r instanceof IllegalArgumentException && r.getMessage().contains("987654"),
                  "MBeanServer ghost id -> IAE mentioning id (got " + r + ")");
        }

        // ---------- snapshot ----------
        long snap = bean.takeSnapshot();
        try {
            check(snap > 0, "takeSnapshot returns positive id");
            RecordingInfo sr = null;
            for (RecordingInfo r : bean.getRecordings()) if (r.getId() == snap) sr = r;
            check(sr != null, "snapshot listed");
            check("Snapshot".equals(sr.getName()), "snapshot named Snapshot");
            check("STOPPED".equals(sr.getState()), "snapshot state STOPPED on JDK-8");
            try { bean.stopRecording(snap); check(false, "stopping a snapshot should throw ISE"); }
            catch (IllegalStateException e) { check(e.getMessage() != null, "stopRecording(snapshot) ISE"); }
        } finally { bean.closeRecording(snap); }

        // ---------- event type surface ----------
        List<EventTypeInfo> types = bean.getEventTypes();
        check(types.size() > 100, "getEventTypes size (" + types.size() + ")");
        boolean sawFileRead = false, sawObjectAllocationSample = false;
        for (EventTypeInfo t : types) {
            if ("jdk.FileRead".equals(t.getName())) sawFileRead = true;
            if ("jdk.ObjectAllocationSample".equals(t.getName())) sawObjectAllocationSample = true;
        }
        check(sawFileRead, "jdk.FileRead present");
        check(!sawObjectAllocationSample, "jdk.ObjectAllocationSample absent on JDK-8");
        EventTypeInfo fileRead = null;
        for (EventTypeInfo t : types) if ("jdk.FileRead".equals(t.getName())) fileRead = t;
        List<String> names = new ArrayList<String>();
        for (SettingDescriptorInfo s : fileRead.getSettingDescriptors()) names.add(s.getName());
        check(names.size() == 3, "FileRead has 3 settings on JDK-8 (" + names + ")");
        check(names.equals(Arrays.asList("enabled", "threshold", "stackTrace")), "FileRead settings order [enabled, threshold, stackTrace]");

        // configurations surface
        List<ConfigurationInfo> confs = bean.getConfigurations();
        check(confs.size() >= 2, "at least default+profile configurations");
        boolean sawDefault = false, sawProfile = false;
        for (ConfigurationInfo cf : confs) {
            if ("default".equals(cf.getName())) sawDefault = true;
            if ("profile".equals(cf.getName())) sawProfile = true;
        }
        check(sawDefault && sawProfile, "default and profile configurations present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
