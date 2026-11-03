# com.sun.jdi.request.EventRequestManager 行为化测试设计

## 1. API 功能
事件请求接口：EventRequest 及各请求（BreakpointRequest/ClassPrepareRequest/MethodEntryRequest 等）。

公开成员（JDK-27 javap）：
  - `public interface com.sun.jdi.request.EventRequestManager extends com.sun.jdi.Mirror {`
  - `public abstract com.sun.jdi.request.ClassPrepareRequest createClassPrepareRequest();`
  - `public abstract com.sun.jdi.request.ClassUnloadRequest createClassUnloadRequest();`
  - `public abstract com.sun.jdi.request.ThreadStartRequest createThreadStartRequest();`
  - `public abstract com.sun.jdi.request.ThreadDeathRequest createThreadDeathRequest();`
  - `public abstract com.sun.jdi.request.ExceptionRequest createExceptionRequest(com.sun.jdi.ReferenceType, boolean, boolean);`
  - `public abstract com.sun.jdi.request.MethodEntryRequest createMethodEntryRequest();`
  - `public abstract com.sun.jdi.request.MethodExitRequest createMethodExitRequest();`
  - `public abstract com.sun.jdi.request.MonitorContendedEnterRequest createMonitorContendedEnterRequest();`
  - `public abstract com.sun.jdi.request.MonitorContendedEnteredRequest createMonitorContendedEnteredRequest();`
  - `public abstract com.sun.jdi.request.MonitorWaitRequest createMonitorWaitRequest();`
  - `public abstract com.sun.jdi.request.MonitorWaitedRequest createMonitorWaitedRequest();`
  - `public abstract com.sun.jdi.request.StepRequest createStepRequest(com.sun.jdi.ThreadReference, int, int);`
  - `public abstract com.sun.jdi.request.BreakpointRequest createBreakpointRequest(com.sun.jdi.Location);`
  - `public abstract com.sun.jdi.request.AccessWatchpointRequest createAccessWatchpointRequest(com.sun.jdi.Field);`
  - `public abstract com.sun.jdi.request.ModificationWatchpointRequest createModificationWatchpointRequest(com.sun.jdi.Field);`
  - `public abstract com.sun.jdi.request.VMDeathRequest createVMDeathRequest();`
  - `public abstract void deleteEventRequest(com.sun.jdi.request.EventRequest);`
  - `public abstract void deleteEventRequests(java.util.List<? extends com.sun.jdi.request.EventRequest>);`
  - `public abstract void deleteAllBreakpoints();`
  - `public abstract java.util.List<com.sun.jdi.request.StepRequest> stepRequests();`
  - `public abstract java.util.List<com.sun.jdi.request.ClassPrepareRequest> classPrepareRequests();`
  - `public abstract java.util.List<com.sun.jdi.request.ClassUnloadRequest> classUnloadRequests();`
  - `public abstract java.util.List<com.sun.jdi.request.ThreadStartRequest> threadStartRequests();`
  - `public abstract java.util.List<com.sun.jdi.request.ThreadDeathRequest> threadDeathRequests();`
  - `public abstract java.util.List<com.sun.jdi.request.ExceptionRequest> exceptionRequests();`
  - `public abstract java.util.List<com.sun.jdi.request.BreakpointRequest> breakpointRequests();`
  - `public abstract java.util.List<com.sun.jdi.request.AccessWatchpointRequest> accessWatchpointRequests();`
  - `public abstract java.util.List<com.sun.jdi.request.ModificationWatchpointRequest> modificationWatchpointRequests();`
  - `public abstract java.util.List<com.sun.jdi.request.MethodEntryRequest> methodEntryRequests();`
  - `public abstract java.util.List<com.sun.jdi.request.MethodExitRequest> methodExitRequests();`
  - `public abstract java.util.List<com.sun.jdi.request.MonitorContendedEnterRequest> monitorContendedEnterRequests();`
  - `public abstract java.util.List<com.sun.jdi.request.MonitorContendedEnteredRequest> monitorContendedEnteredRequests();`
  - `public abstract java.util.List<com.sun.jdi.request.MonitorWaitRequest> monitorWaitRequests();`
  - `public abstract java.util.List<com.sun.jdi.request.MonitorWaitedRequest> monitorWaitedRequests();`
  - `public abstract java.util.List<com.sun.jdi.request.VMDeathRequest> vmDeathRequests();`

## 2. Java 用户如何使用
通过 EventRequestManager.create*Request 建请求，设 suspendPolicy/过滤器，enable/disable，读 suspendPolicy/isEnabled。

## 3. 开源代码实际用法调研
来源：~/git/jdk25u-dev/test/jdk/com/sun/jdi/ 的 BreakpointTest.java、CountFilterTest.java、DeleteEventRequestsTest.java、InstanceFilter.java。

## 4. 测试用例设计 + 自动化实现
testBlackBox 用真实 EventRequestManager 建各类请求，验证过滤器、enable/disable、suspendPolicy 往返。

实现见同目录 `EventRequestManagerTest.java` 的 `testWhiteBox()`（结构/继承/成员形态）与 `testBlackBox()`（组合操作行为断言）。
