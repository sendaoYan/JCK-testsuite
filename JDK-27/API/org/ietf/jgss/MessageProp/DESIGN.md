# org.ietf.jgss.MessageProp 测试设计

## 1. API 功能
MessageProp 在 GSSContext 的每条消息方法（wrap/unwrap/getMIC/verifyMIC）间传递 QOP 与机密性状态，并携带补充状态（重复/过期/乱序/缺口令牌）与机制 minor 状态。
- 构造器：`MessageProp(boolean privState)`（QOP 默认 0）、`MessageProp(int qop, boolean privState)`。
- 查询/设置：`getQOP()/setQOP(int)`、`getPrivacy()/setPrivacy(boolean)`。
- 补充状态查询：`isDuplicateToken()`、`isOldToken()`、`isUnseqToken()`、`isGapToken()`。
- minor：`getMinorStatus()`、`getMinorString()`。
- 组合设置：`setSupplementaryStates(boolean dup, boolean old, boolean unseq, boolean gap, int minorStatus, String minorString)`。

## 2. Java 用户如何使用
```java
MessageProp p = new MessageProp(0, true); // 请求 QOP 0 + 机密性
byte[] out = ctx.wrap(data, 0, data.length, p);
boolean priv = p.getPrivacy();           // wrap 返回后读取实际机密性
if (p.isDuplicateToken()) { ... }        // unwrap/verifyMIC 返回后读取补充状态
```
组合链路：`new MessageProp(0,true) → wrap → getPrivacy → setQOP/setPrivacy → setSupplementaryStates → isDuplicateToken/isOldToken/isUnseqToken/isGapToken/getMinorStatus/getMinorString`。

## 3. 开源代码实际用法调研
- `test/jdk/sun/security/krb5/auto/Context.java`：`MessageProp p1 = new MessageProp(0, privacy); ... me.x.wrap(input, 0, input.length, p1);` 并在返回后读取 `p1.getPrivacy()`。
- 同上：`MessageProp p1 = new MessageProp(0, true); me.x.unwrap(...)` 后校验 `p1.getPrivacy()`。
- JDK 源码 `sun/security/jgss/GSSContextImpl.java`：per-message 方法用 `setSupplementaryStates(...)` 回填状态。
（来源：`~/git/jdk25u-dev`。）

## 4. 测试用例设计 + 自动化实现
黑盒断言（`MessagePropTest.testBlackBox()`）：
- boolean 构造器：QOP=0、privacy 正确、四个补充状态 false、minorStatus=0、minorString=null。
- int+boolean 构造器：QOP/privacy 取值。
- setQOP/setPrivacy 往返两轮。
- `setSupplementaryStates(true,false,true,false,3,"minor")`：逐个标志 + minor 状态精确断言。
- 重置为全 false/0/null。
- 四个补充标志同时为 true 的组合断言。
白盒（`testWhiteBox()`）：public 类、继承 Object、两个构造器 + 11 个方法。

## 5. 深化补充（本轮契约深化）
新增：构造矩阵 qop×privacy 全枚举往返、setter 覆盖链（qop/privacy 重设）、补充标志五元组
默认值与只读性、minor 通道默认值（0/null）、白盒 12 方法精确签名面与返回型锁。
