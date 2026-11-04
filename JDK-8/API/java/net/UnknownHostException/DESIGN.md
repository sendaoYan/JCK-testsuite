# java.net.UnknownHostException 测试设计案（JDK-8）

> 覆盖目标：完整契约。构造器×2、getMessage/toString 精确文本、IOException 层级、
> null 消息语义、cause 默认 null、序列化 round-trip、由 InetAddress API 真实触发。

## 1. API 功能

- `UnknownHostException()` / `UnknownHostException(String message)`；继承 IOException（受检异常）。
- 语义：主机名/IP 字面量无法解析时由 InetAddress/Socket 等 API 抛出。

## 2. 用户如何使用

- `catch (UnknownHostException e)` 在 DNS/字面量解析失败分支处理；`e.getMessage()` 拿到主机名线索。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/net/UnknownHostException.html）。
- 双 JDK 探针（/tmp/probe-E）：getByName 非法字面量的消息文本。

## 4. 测试用例设计（映射到 UnknownHostExceptionTest.java，3→17 断言）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 两个构造器 + getMessage 精确值 / null | main 直排（原 3 条）+ 深化 |
| P0 | toString 精确 `java.net.UnknownHostException: boom` | main |
| P0 | 继承 IOException、Serializable；cause 默认 null | main |
| P1 | 序列化 round-trip（类与消息保持、no-arg 消息仍 null） | main |
| P1 | 真实触发：`getByName("999.888.777.666")` → UHE 且消息含输入字面量 | main |

## 易错规范细节（已锁入断言）

- `new UnknownHostException(null).getMessage() == null`（null 消息保持 null，不串 "null"）。
- 非法点分字面量（超 255 八位组）不查 DNS 直接 UHE，消息包含原始字面量。

## 5. 版本差异（JDK-8 vs JDK-27）

- 无行为差异。JDK-27 侧额外断言 `InetAddress.ofLiteral` 对同样输入抛 IAE（而非 UHE），
  消息 `Invalid IP address literal: ...`（严格字面量 API 与宽松 getByName 的语义分界）。
