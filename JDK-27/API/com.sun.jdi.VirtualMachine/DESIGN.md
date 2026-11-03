# com.sun.jdi.VirtualMachine 行为化测试设计

## 1. API 功能
VirtualMachine 是目标 VM 的镜像：查询类/线程/模块、mirrorOf 造值、事件与请求入口、dispose/exit 生命周期。

公开成员（JDK-27 javap）：
  - `public interface com.sun.jdi.VirtualMachine extends com.sun.jdi.Mirror {`
  - `public static final int TRACE_NONE;`
  - `public static final int TRACE_SENDS;`
  - `public static final int TRACE_RECEIVES;`
  - `public static final int TRACE_EVENTS;`
  - `public static final int TRACE_REFTYPES;`
  - `public static final int TRACE_OBJREFS;`
  - `public static final int TRACE_ALL;`
  - `public default java.util.List<com.sun.jdi.ModuleReference> allModules();`
  - `public abstract java.util.List<com.sun.jdi.ReferenceType> classesByName(java.lang.String);`
  - `public abstract java.util.List<com.sun.jdi.ReferenceType> allClasses();`
  - `public abstract void redefineClasses(java.util.Map<? extends com.sun.jdi.ReferenceType, byte[]>);`
  - `public abstract java.util.List<com.sun.jdi.ThreadReference> allThreads();`
  - `public abstract void suspend();`
  - `public abstract void resume();`
  - `public abstract java.util.List<com.sun.jdi.ThreadGroupReference> topLevelThreadGroups();`
  - `public abstract com.sun.jdi.event.EventQueue eventQueue();`
  - `public abstract com.sun.jdi.request.EventRequestManager eventRequestManager();`
  - `public abstract com.sun.jdi.BooleanValue mirrorOf(boolean);`
  - `public abstract com.sun.jdi.ByteValue mirrorOf(byte);`
  - `public abstract com.sun.jdi.CharValue mirrorOf(char);`
  - `public abstract com.sun.jdi.ShortValue mirrorOf(short);`
  - `public abstract com.sun.jdi.IntegerValue mirrorOf(int);`
  - `public abstract com.sun.jdi.LongValue mirrorOf(long);`
  - `public abstract com.sun.jdi.FloatValue mirrorOf(float);`
  - `public abstract com.sun.jdi.DoubleValue mirrorOf(double);`
  - `public abstract com.sun.jdi.StringReference mirrorOf(java.lang.String);`
  - `public abstract com.sun.jdi.VoidValue mirrorOfVoid();`
  - `public abstract java.lang.Process process();`
  - `public abstract void dispose();`
  - `public abstract void exit(int);`
  - `public abstract boolean canWatchFieldModification();`
  - `public abstract boolean canWatchFieldAccess();`
  - `public abstract boolean canGetBytecodes();`
  - `public abstract boolean canGetSyntheticAttribute();`
  - `public abstract boolean canGetOwnedMonitorInfo();`
  - `public abstract boolean canGetCurrentContendedMonitor();`
  - `public abstract boolean canGetMonitorInfo();`
  - `public abstract boolean canUseInstanceFilters();`
  - `public abstract boolean canRedefineClasses();`
  - `public abstract boolean canAddMethod();`
  - `public abstract boolean canUnrestrictedlyRedefineClasses();`
  - `public abstract boolean canPopFrames();`
  - `public abstract boolean canGetSourceDebugExtension();`
  - `public abstract boolean canRequestVMDeathEvent();`
  - `public abstract boolean canGetMethodReturnValues();`
  - `public abstract boolean canGetInstanceInfo();`
  - `public abstract boolean canUseSourceNameFilters();`
  - `public abstract boolean canForceEarlyReturn();`
  - `public abstract boolean canBeModified();`
  - `public abstract boolean canRequestMonitorEvents();`
  - `public abstract boolean canGetMonitorFrameInfo();`
  - `public abstract boolean canGetClassFileVersion();`
  - `public abstract boolean canGetConstantPool();`
  - `public default boolean canGetModuleInfo();`
  - `public abstract void setDefaultStratum(java.lang.String);`
  - `public abstract java.lang.String getDefaultStratum();`
  - `public abstract long[] instanceCounts(java.util.List<? extends com.sun.jdi.ReferenceType>);`
  - `public abstract java.lang.String description();`
  - `public abstract java.lang.String version();`
  - `public abstract java.lang.String name();`
  - `public abstract void setDebugTraceMode(int);`

## 2. Java 用户如何使用
从 connector.launch() 得到 vm 后，`allClasses()`/`classesByName()`/`allThreads()`/`mirrorOf()` 查询，`eventRequestManager()`/`eventQueue()` 建请求收事件，最后 `dispose()`。

## 3. 开源代码实际用法调研
来源：~/git/jdk25u-dev/test/jdk/com/sun/jdi/ 的 ClassesByName.java、DataDumpTest.java、HomeTest.java、RunToExit.java。

## 4. 测试用例设计 + 自动化实现
testBlackBox 用 launchTarget 启动子 JVM，断言 name/version/mirrorOf/allClasses/allModules/instanceCounts 等。

实现见同目录 `VirtualMachineTest.java` 的 `testWhiteBox()`（结构/继承/成员形态）与 `testBlackBox()`（组合操作行为断言）。
