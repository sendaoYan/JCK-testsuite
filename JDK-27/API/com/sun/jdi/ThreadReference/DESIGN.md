# com.sun.jdi.ThreadReference 行为化测试设计

## 1. API 功能
类型成员与调用栈：Field/Method/TypeComponent/Location/LocalVariable/StackFrame/ThreadReference/ThreadGroupReference 等，描述成员与线程栈帧。

公开成员（JDK-27 javap）：
  - `public interface com.sun.jdi.ThreadReference extends com.sun.jdi.ObjectReference {`
  - `public static final int THREAD_STATUS_UNKNOWN;`
  - `public static final int THREAD_STATUS_ZOMBIE;`
  - `public static final int THREAD_STATUS_RUNNING;`
  - `public static final int THREAD_STATUS_SLEEPING;`
  - `public static final int THREAD_STATUS_MONITOR;`
  - `public static final int THREAD_STATUS_WAIT;`
  - `public static final int THREAD_STATUS_NOT_STARTED;`
  - `public abstract java.lang.String name();`
  - `public abstract void suspend();`
  - `public abstract void resume();`
  - `public abstract int suspendCount();`
  - `public abstract void stop(com.sun.jdi.ObjectReference) throws com.sun.jdi.InvalidTypeException;`
  - `public abstract void interrupt();`
  - `public abstract int status();`
  - `public abstract boolean isSuspended();`
  - `public abstract boolean isAtBreakpoint();`
  - `public abstract com.sun.jdi.ThreadGroupReference threadGroup();`
  - `public abstract int frameCount() throws com.sun.jdi.IncompatibleThreadStateException;`
  - `public abstract java.util.List<com.sun.jdi.StackFrame> frames() throws com.sun.jdi.IncompatibleThreadStateException;`
  - `public abstract com.sun.jdi.StackFrame frame(int) throws com.sun.jdi.IncompatibleThreadStateException;`
  - `public abstract java.util.List<com.sun.jdi.StackFrame> frames(int, int) throws com.sun.jdi.IncompatibleThreadStateException;`
  - `public abstract java.util.List<com.sun.jdi.ObjectReference> ownedMonitors() throws com.sun.jdi.IncompatibleThreadStateException;`
  - `public abstract java.util.List<com.sun.jdi.MonitorInfo> ownedMonitorsAndFrames() throws com.sun.jdi.IncompatibleThreadStateException;`
  - `public abstract com.sun.jdi.ObjectReference currentContendedMonitor() throws com.sun.jdi.IncompatibleThreadStateException;`
  - `public abstract void popFrames(com.sun.jdi.StackFrame) throws com.sun.jdi.IncompatibleThreadStateException;`
  - `public abstract void forceEarlyReturn(com.sun.jdi.Value) throws com.sun.jdi.InvalidTypeException, com.sun.jdi.ClassNotLoadedException, com.sun.jdi.IncompatibleThreadStateException;`
  - `public default boolean isVirtual();`

## 2. Java 用户如何使用
从 ReferenceType 拿 Field/Method，读 name/signature/type/修饰符；挂起线程后 `thread.frames()`/`frame.visibleVariables()` 取 LocalVariable 与值。

## 3. 开源代码实际用法调研
来源：~/git/jdk25u-dev/test/jdk/com/sun/jdi/ 的 FramesTest.java、Frames2Test.java、GetLocalVariables.java、FetchLocals.java、AccessSpecifierTest.java。

## 4. 测试用例设计 + 自动化实现
testBlackBox 挂起 debuggee 主线程，遍历 frames 找 main 帧，验证 location/visibleVariables/getValue 等。

实现见同目录 `ThreadReferenceTest.java` 的 `testWhiteBox()`（结构/继承/成员形态）与 `testBlackBox()`（组合操作行为断言）。
