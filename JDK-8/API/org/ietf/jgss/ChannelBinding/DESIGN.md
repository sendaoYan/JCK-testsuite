# org.ietf.jgss.ChannelBinding 测试设计（JDK-8 侧）

## 1. API 功能
通道绑定：把上下文绑定到发起方/接收方地址 + 应用数据三元组。
- 构造器：`ChannelBinding(InetAddress init, InetAddress accept, byte[] appData)`、`ChannelBinding(byte[] appData)`。
- 读取：`getInitiatorAddress/getAcceptorAddress/getApplicationData`；`equals/hashCode`。

## 2. Java 用户如何使用
```java
byte[] data = "channel".getBytes("UTF-8");
ChannelBinding cb = new ChannelBinding(null, null, data);
ctx.requestChannelBinding(cb);           // 两侧配置相同的绑定
```
应用数据参与 equals/hashCode，地址为 null 表示"不参与绑定"。

## 3. 开源代码实际用法调研
- JDK-8 源码 `jdk/src/share/classes/org/ietf/jgss/*.java`（本仓 `~/git/jdk8u-dev`）：常量定义、构造器校验与 `GSSException` 错误码映射的权威实现。
- JDK 测试 `test/sun/security/krb5/`：以 `Oid`/`GSSManager` 离线构造机制对象与名称，是本包的主要真实用户。
- 本环境离线（无 KDC），黑盒用例只覆盖离线可达路径，在线路径以白盒形态锁为主。
## 4. 测试用例设计 + 自动化实现
- 白盒：public 类、两构造器 + 六方法形态。
- 黑盒（防御拷贝闭环）：传入 appData 后修改外层数组，`getApplicationData()` 不受影响；
  取出的数组再改也不影响再次取值（双向防御拷贝）；地址成员同一性转发；
  仅数据构造器地址为 null；全 null appData 合法；equals 矩阵（自等/按数据等/数据异不等/null/异类型/
  地址参与 vs 不参与/null appData vs 非 null）；hashCode 契约（相等对象哈希相等；全 null 组合的稳定值）。
- 实现见 `ChannelBindingTest.java`。
