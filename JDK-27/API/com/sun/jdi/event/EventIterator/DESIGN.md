# com.sun.jdi.event.EventIterator 行为化测试设计

## 1. API 功能
JDI 事件接口：Event/EventSet/EventQueue/EventIterator 及各具体事件（ClassPrepare/ThreadStart/Monitor 等）。

公开成员（JDK-27 javap）：
  - `public interface com.sun.jdi.event.EventIterator extends java.util.Iterator<com.sun.jdi.event.Event> {`
  - `public abstract com.sun.jdi.event.Event nextEvent();`

## 2. Java 用户如何使用
调试器用 EventRequestManager 建请求，从 EventQueue.remove() 拿 EventSet，遍历 EventIterator 按 instanceof 分派具体事件。

## 3. 开源代码实际用法调研
来源：~/git/jdk25u-dev/test/jdk/com/sun/jdi/ 的 CountEvent.java、CountFilterTest.java、DeleteEventRequestsTest.java、MonitorWaitTest.java（若存在）。

## 4. 测试用例设计 + 自动化实现
testBlackBox 用真实 ClassPrepare 事件（waitPrepared）或匿名实现，断言事件集/迭代器/具体 getter。

实现见同目录 `EventIteratorTest.java` 的 `testWhiteBox()`（结构/继承/成员形态）与 `testBlackBox()`（组合操作行为断言）。
