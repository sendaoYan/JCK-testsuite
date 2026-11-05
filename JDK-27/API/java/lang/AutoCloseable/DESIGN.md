# AutoCloseable — 设计方案

## 1. API 功能
`java.lang.AutoCloseable`（接口）是 try-with-resources 的资源抽象，声明唯一抽象方法 `void close() throws Exception`，语义：资源在离开 try 块时按声明逆序自动关闭，且每个资源恰好关闭一次。

## 2. Java 用户如何使用
- 自定义资源实现 `AutoCloseable` 后放入 try-with-resources，JDK 自动调用 `close()`。
```java
try (Resource a = new Resource("a"); Resource b = new Resource("b")) { ... }
```
- `close()` 抛出的异常会从 try 块传播。

## 3. 开源代码实际用法调研
- JDK 内 `java.io`/`java.sql` 大量类型实现 `AutoCloseable`；`test/jdk/java/lang/AutoCloseable` 验证逆序关闭、单次关闭与异常传播。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.AutoCloseable")` 验证为接口、`close()` 返回 void 且声明 `throws Exception`。
黑盒：两个内嵌资源类记录关闭顺序，验证逆序 + 恰好一次；抛异常资源验证异常传播。
对应 `<Simple>Test.java` 白盒 + 黑盒部分。

## 契约深化（2026-11）

### 1. API 功能（新增维度）
恰 1 个声明方法（abstract、public、void、throws Exception 宽签名）；非泛型。

### 2. 使用方式（新增）
open/close 双向日志全时序（[open:r1, open:r2, body, close:r2, close:r1] 精确）+ 三资源严格
逆序；异常抑制闭环（body E1 + close E2 → catch E1 同实例 + suppressed[0]==E2 同实例）；双
close 抛（后声明资源主异常、先声明抑制）；body 抛 + 双 close 抑制按 close 逆序 [EB,EA]；
嵌套 TWR 抑制链（内层恢复后外层 close E3 逃逸；未捕获形态 suppressed [E2,E3]）；body 抛
Error 仍 close 并抑制；close 恰好一次（正常/抛 body 两档）；**签名缩窄**（Silent 无 throws
裸调 + 接口宽引用须 catch + AutoCloseable 型 TWR 资源）；ctor 失败补偿（后资源 ctor 抛 →
先资源仍 close；首资源失败无 close）；return/continue/labeled break 仍 close；受检 close
（IOException）抑制类型与消息精确；同实例两资源 close 两次；匿名/子类资源；
**JDK-9+ effectively-final 资源形态 try (r)**（27 侧新增：re-entry 仅记 close）。

### 3. 参考来源
JLS §14.20.3（逆序 close、抑制、ctor 补偿）；Oracle javadoc AutoCloseable（throws 缩窄）。

### 4. 测试用例设计 + 易错规范细节（新增方法）
testWhiteBoxShape；testOpenCloseLogSequence；testThreeResourceReverseOrder；
testEmptyBodyStillCloses；testSuppressedExceptionLoop；testCloseThrowsBodyNormal；
testBothClosesThrow；testBodyThrowsTwoClosesSuppress；testNestedTwrSuppressionChain；
testErrorBodyStillCloses；testCloseExactlyOnce；testNarrowedSignature；
testCtorThrowsClosesEarlierResources；testControlFlowStillCloses；
testCheckedCloseSuppressedOnUncheckedBody；testSubclassAndAnonymousResources；
testLibraryImplementsAutoCloseable；testEffectivelyFinalResource（27 专属，8 无此语法）。

易错：主异常优先级 body > 后声明资源 close > 先声明资源 close；嵌套 TWR 内层 catch 恢复后
外层 close 的异常会**逃逸**（不再是抑制）。
