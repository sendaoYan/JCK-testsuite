/*
 * @test
 * @summary com.sun.jdi.VirtualMachine conformance: interface hierarchy and methods.
 * @run main VirtualMachineTest
 */

import java.lang.reflect.*;
import java.util.*;

public class VirtualMachineTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.VirtualMachine");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("com.sun.jdi.Mirror"), "extends com.sun.jdi.Mirror");
        check(si.size() == 1, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("allModules"), "method allModules");
        check(dm.contains("classesByName"), "method classesByName");
        check(dm.contains("allClasses"), "method allClasses");
        check(dm.contains("redefineClasses"), "method redefineClasses");
        check(dm.contains("allThreads"), "method allThreads");
        check(dm.contains("suspend"), "method suspend");
        check(dm.contains("resume"), "method resume");
        check(dm.contains("topLevelThreadGroups"), "method topLevelThreadGroups");
        check(dm.contains("eventQueue"), "method eventQueue");
        check(dm.contains("eventRequestManager"), "method eventRequestManager");
        check(dm.contains("mirrorOf"), "method mirrorOf");
        check(dm.contains("mirrorOfVoid"), "method mirrorOfVoid");
        check(dm.contains("process"), "method process");
        check(dm.contains("dispose"), "method dispose");
        check(dm.contains("exit"), "method exit");
        check(dm.contains("canWatchFieldModification"), "method canWatchFieldModification");
        check(dm.contains("canWatchFieldAccess"), "method canWatchFieldAccess");
        check(dm.contains("canGetBytecodes"), "method canGetBytecodes");
        check(dm.contains("canGetSyntheticAttribute"), "method canGetSyntheticAttribute");
        check(dm.contains("canGetOwnedMonitorInfo"), "method canGetOwnedMonitorInfo");
        check(dm.contains("canGetCurrentContendedMonitor"), "method canGetCurrentContendedMonitor");
        check(dm.contains("canGetMonitorInfo"), "method canGetMonitorInfo");
        check(dm.contains("canUseInstanceFilters"), "method canUseInstanceFilters");
        check(dm.contains("canRedefineClasses"), "method canRedefineClasses");
        check(dm.contains("canAddMethod"), "method canAddMethod");
        check(dm.contains("canUnrestrictedlyRedefineClasses"), "method canUnrestrictedlyRedefineClasses");
        check(dm.contains("canPopFrames"), "method canPopFrames");
        check(dm.contains("canGetSourceDebugExtension"), "method canGetSourceDebugExtension");
        check(dm.contains("canRequestVMDeathEvent"), "method canRequestVMDeathEvent");
        check(dm.contains("canGetMethodReturnValues"), "method canGetMethodReturnValues");
        check(dm.contains("canGetInstanceInfo"), "method canGetInstanceInfo");
        check(dm.contains("canUseSourceNameFilters"), "method canUseSourceNameFilters");
        check(dm.contains("canForceEarlyReturn"), "method canForceEarlyReturn");
        check(dm.contains("canBeModified"), "method canBeModified");
        check(dm.contains("canRequestMonitorEvents"), "method canRequestMonitorEvents");
        check(dm.contains("canGetMonitorFrameInfo"), "method canGetMonitorFrameInfo");
        check(dm.contains("canGetClassFileVersion"), "method canGetClassFileVersion");
        check(dm.contains("canGetConstantPool"), "method canGetConstantPool");
        check(dm.contains("canGetModuleInfo"), "method canGetModuleInfo");
        check(dm.contains("setDefaultStratum"), "method setDefaultStratum");
        check(dm.contains("getDefaultStratum"), "method getDefaultStratum");
        check(dm.contains("instanceCounts"), "method instanceCounts");
        check(dm.contains("description"), "method description");
        check(dm.contains("version"), "method version");
        check(dm.contains("name"), "method name");
        check(dm.contains("setDebugTraceMode"), "method setDebugTraceMode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
