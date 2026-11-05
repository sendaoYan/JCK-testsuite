# org.ietf.jgss.ChannelBinding 测试设计

## 1. API 功能
ChannelBinding 封装调用方提供的通道绑定信息（initiator/acceptor 地址 + 应用数据），用于在上下文建立时强化对端认证。
- 构造器：`ChannelBinding(byte[] appData)`、`ChannelBinding(InetAddress initAddr, InetAddress acceptAddr, byte[] appData)`。
- 查询：`getInitiatorAddress()`、`getAcceptorAddress()`、`getApplicationData()`。
- 比较：`equals(Object)`、`hashCode()`。
- 语义：appData 构造时防御性复制，`getApplicationData()` 返回副本；地址字段保存原引用。

## 2. Java 用户如何使用
```java
ChannelBinding cb = new ChannelBinding(InetAddress.getByName("127.0.0.1"),
                                       InetAddress.getByName("127.0.0.2"),
                                       new byte[]{1,2,3});
ctx.setChannelBinding(cb);               // 建立上下文前设置
byte[] data = cb.getApplicationData();   // 返回副本
```
组合链路：`new ChannelBinding(init, accept, data) → getInitiatorAddress/getAcceptorAddress/getApplicationData → equals/hashCode`，以及“修改源数组/返回值不影响内部状态”。

## 3. 开源代码实际用法调研
- JDK 源码 `org/ietf/jgss/ChannelBinding.java`：`getApplicationData()` 内部 `System.arraycopy` 返回副本。
- `test/jdk/sun/security/krb5/auto/IgnoreChannelBinding.java`：Kerberos 测试用 `ChannelBinding` 验证地址绑定忽略逻辑。
- JDK 源码 `sun/security/jgss/krb5/Krb5Context.java`：`setChannelBinding` 后参与上下文 token 计算。
（来源：`~/git/jdk25u-dev`。）

## 4. 测试用例设计 + 自动化实现
黑盒断言（`ChannelBindingTest.testBlackBox()`）：
- null appData 构造器：三字段均 null。
- data-only 构造器：appData 等于输入、地址为 null；修改源数组不影响内部；修改 `getApplicationData()` 返回值不影响后续读取（双重副本语义）。
- 全构造器：地址字段返回同一实例（身份相等）、appData 正确。
- equals：自反、按数据相等、不同数据不等、null/非 ChannelBinding 不等、地址+数据组合相等、不同 initiator 地址不等。
- hashCode：相等对象哈希相等；全 null=1；仅 initiator=其哈希；仅 acceptor=其哈希；仅 appData=Arrays.hashCode。
白盒（`testWhiteBox()`）：public 类、继承 Object、两个构造器 + 五个方法。

## 5. 深化补充（本轮契约深化）
新增：应用数据双向防御拷贝闭环（传入后改外层不影响、取出后改不影响再取）、地址成员同一性、
equals 全矩阵（数据/地址/空组合）、hashCode 契约与全 null 组合稳定值；注意 27 侧
`hashCode()` 与 8 侧实现一致（Arrays.hashCode 语义），断言双侧同型。
