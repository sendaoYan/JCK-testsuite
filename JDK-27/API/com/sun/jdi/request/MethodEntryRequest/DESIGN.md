# com.sun.jdi.request.MethodEntryRequest 行为化测试设计

## 1. API 功能
事件请求接口：EventRequest 及各请求（BreakpointRequest/ClassPrepareRequest/MethodEntryRequest 等）。

公开成员（JDK-27 javap）：
  - `public interface com.sun.jdi.request.MethodEntryRequest extends com.sun.jdi.request.EventRequest {`
  - `public abstract void addThreadFilter(com.sun.jdi.ThreadReference);`
  - `public abstract void addClassFilter(com.sun.jdi.ReferenceType);`
  - `public abstract void addClassFilter(java.lang.String);`
  - `public abstract void addClassExclusionFilter(java.lang.String);`
  - `public abstract void addInstanceFilter(com.sun.jdi.ObjectReference);`

## 2. Java 用户如何使用
通过 EventRequestManager.create*Request 建请求，设 suspendPolicy/过滤器，enable/disable，读 suspendPolicy/isEnabled。

## 3. 开源代码实际用法调研
来源：~/git/jdk25u-dev/test/jdk/com/sun/jdi/ 的 BreakpointTest.java、CountFilterTest.java、DeleteEventRequestsTest.java、InstanceFilter.java。

## 4. 测试用例设计 + 自动化实现
testBlackBox 用真实 EventRequestManager 建各类请求，验证过滤器、enable/disable、suspendPolicy 往返。

实现见同目录 `MethodEntryRequestTest.java` 的 `testWhiteBox()`（结构/继承/成员形态）与 `testBlackBox()`（组合操作行为断言）。
