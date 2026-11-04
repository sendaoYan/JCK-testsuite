# java.util.concurrent.StructuredTaskScope$Subtask 设计案

## 1. API 功能
子任务句柄接口（预览，继承 Supplier）。`state()`/`get()`/`exception()` 查询结果与异常。

## 2. Java 用户如何使用
`scope.fork` 返回 Subtask，`join()` 后 `state()` 判成功/失败、`get()` 取值、`exception()` 取异常。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.StructuredTaskScope.Subtask`（JEP 480）；`test/jdk/java/util/concurrent/structured/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：接口 + extends Supplier + state/get/exception。
2. 黑盒主干：SUCCESS get / FAILED exception。
3. 契约深化：join 前访问被拒（get/exception → ISE 且消息 null；state() 可调用——值对快子任务是时序性的，
   只断可调用）（testAccessBeforeJoinRejected）；成功路径（get 返回**同一对象**、exception() → ISE）
   （testSuccessSubtask）；失败路径（exception 恒等 + 消息保留、get() → ISE）（testFailedSubtask）；
   Runnable 子任务（ran、get null、SUCCESS）（testRunnableSubtask）；被取消子任务 UNAVAILABLE
   （testCancelledSubtask）；经 Supplier 超类型取值（testSupplierTyping）。

实现：`SubtaskTest.java`（24 断言）。

## 关键规范细节（实测锁定）
- 成功子任务 exception() **抛 ISE**（"Exception is unavailable..."），不返回 null。
- 失败子任务 get() 抛 ISE。
- join 前 get()/exception() 抛 ISE 且 message == null；state() join 前可安全调用。
- 取消导致未完成的子任务 state() == UNAVAILABLE。
