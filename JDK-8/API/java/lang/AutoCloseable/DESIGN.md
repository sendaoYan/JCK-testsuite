# java.lang.AutoCloseable 测试设计案（JDK-8）

> 覆盖目标：**TWR 招牌闭环**。重点补：白盒（恰 1 方法 close，abstract、void、throws Exception
> 宽签名）、双资源 open/close 全时序日志 [open:r1, open:r2, body, close:r2, close:r1]、
> 三资源严格逆序、异常抑制闭环（body E1 + close E2 → catch E1 同实例 + suppressed[0]==E2 同
> 实例）、双 close 抛（后声明资源的异常为主、先声明的被抑制）、嵌套 TWR 抑制链 [E2,E3]、
> body 抛 Error 仍 close 并抑制、close 恰好一次（正常/抛 body 两档）、签名缩窄（实现不抛受检
> 合法 + 接口宽引用须 catch）、构造器失败补偿语义（后资源 ctor 抛 → 先资源仍 close）、
> return/continue/break 仍 close、受检 close 抑制类型、库类型事实表。

## 1. API 提供了哪些功能

- **接口** `AutoCloseable`（Java 7+）：唯一方法 `void close() throws Exception`；
  try-with-resources（JLS 14.20.3）对每个资源按**声明逆序**调 close；
  body 主异常优先传播，后续 close 异常**抑制**（getSuppressed）追加到主异常；
  close 抛异常而 body 正常时该异常直接传播（无抑制）。

## 2. Java 用户如何使用

- 实现类可**缩窄** close 的 throws（不抛受检异常）→ 调用方无需 catch；
- 接口引用（`AutoCloseable a = ...`）调用 close 必须处理 Exception（宽签名真实存在）。

```java
try (Res a = new Res("a"); Res b = new Res("b")) { ... }
// close 顺序：b 后 a
```

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/AutoCloseable.html`）：缩窄 throws 与异常抑制的说明。
- **JLS §14.20.3** try-with-resources：资源按逆序关闭、抑制语义、资源 ctor 失败的补偿 close。
- **OpenJDK 8u-dev** `javac` 对 TWR 的 desugar（addSuppressed 路径）。

## 4. 测试用例设计（映射到 AutoCloseableTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：1 方法、abstract、throws Exception | testWhiteBox |
| P0 | 双资源 open/close 全时序日志精确 | testOpenCloseLogSequence |
| P0 | 三资源严格逆序 + 所有 open 先于所有 close | testThreeResourceReverseOrder |
| P0 | 空 body 仍 close | testEmptyBodyStillCloses |
| P0 | 抑制闭环：E1 主 + E2 抑制（同实例） | testSuppressedExceptionLoop |
| P0 | close 抛而 body 正常 → 直接传播、无 suppressed | testCloseThrowsBodyNormal |
| P0 | 双 close 抛：后者主、前者抑制 | testBothClosesThrow |
| P0 | body 抛 + 双 close 抑制按 close 逆序 | testBodyThrowsTwoClosesSuppress |
| P0 | 嵌套 TWR 抑制链 [E2,E3] | testNestedTwrSuppressionChain |
| P1 | body 抛 Error 仍 close 并抑制 | testErrorBodyStillCloses |
| P1 | close 恰好一次（两档） | testCloseExactlyOnce |
| P1 | 签名缩窄 + 接口宽引用双路径 | testNarrowedSignature |
| P1 | ctor 失败：后资源失败仍 close 先资源；首资源失败无 close | testCtorThrowsClosesEarlierResources |
| P1 | return/continue/labeled break 仍 close | testControlFlowStillCloses |
| P2 | 受检 close（IOException）抑制类型与消息 | testCheckedCloseSuppressedOnUncheckedBody |
| P2 | 子类继承 close / 匿名实现 / 同实例两资源 close 两次 | testSubclassAndAnonymousResources |
| P2 | 库类型事实（FIS/BAIS/Scanner） | testLibraryImplementsAutoCloseable |
| P0 | 双资源逆序（原有） | testCloseOrderAndOnce |
| P0 | close 异常传播（原有） | testCloseThrows |

## 易错规范细节

- **主异常选择**：body 异常永远优先；两个 close 都抛时，**后声明资源**（先 close）的异常为主，
  先声明资源的异常抑制其上；抑制顺序 == close 顺序（逆声明序）。
- **close() throws Exception 是宽签名**：实现可缩窄到不抛；经接口引用调用仍须 catch Exception。
- **资源 ctor 失败**：该资源自身不 close；已构造成功的**先前资源仍按 TWR 语义 close**（补偿）。
- **每资源 close 恰一次**——同实例声明为两个资源则 close 两次（本批实证）。
- 控制流离开（return/continue/break/异常）都会 close——desugar 保证。
