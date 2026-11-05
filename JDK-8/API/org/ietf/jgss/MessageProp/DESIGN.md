# org.ietf.jgss.MessageProp 测试设计（JDK-8 侧）

## 1. API 功能
逐消息的 QOP/隐私标志与补充标志载体。
- 构造器：`MessageProp(boolean privacy)`（QOP=0）、`MessageProp(int qop, boolean privacy)`。
- 读写：`getQOP/setQOP`、`getPrivacy/setPrivacy`；补充标志只读：`isDuplicate/isOld/isUnseq/isGapTag/isWrapped`；
  `getMinorStatus/getMinorString`（错误通道）。

## 2. Java 用户如何使用
```java
MessageProp p = new MessageProp(0, true);   // 请求隐私
ctx.wrap(inp, 0, inp.length, p);            // wrap 后 p 携带实际 QOP 与补充标志
if (p.isDuplicate() || p.isUnseq()) { ... }
```
补充标志是 wrap MIC 返回的"每消息真实状态"，与构造时的请求值是两条通道。

## 3. 开源代码实际用法调研
- JDK-8 源码 `jdk/src/share/classes/org/ietf/jgss/*.java`（本仓 `~/git/jdk8u-dev`）：常量定义、构造器校验与 `GSSException` 错误码映射的权威实现。
- JDK 测试 `test/sun/security/krb5/`：以 `Oid`/`GSSManager` 离线构造机制对象与名称，是本包的主要真实用户。
- 本环境离线（无 KDC），黑盒用例只覆盖离线可达路径，在线路径以白盒形态锁为主。
## 4. 测试用例设计 + 自动化实现
- 白盒：public 类、两个构造器、全部 12 个方法的精确签名与 boolean/int 返回型。
- 黑盒：构造矩阵（qop 0/1/5 × privacy true/false）往返；setter 覆盖（qop 重设、privacy 翻转）；
  补充标志五元组默认全 false 且离线场景下不可写（无 setter）；`getMinorStatus` 默认 0、
  `getMinorString` 默认 null；equals/hashCode 继承 Object（同一性）。
- 实现见 `MessagePropTest.java`。
