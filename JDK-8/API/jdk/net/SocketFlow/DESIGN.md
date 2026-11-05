# jdk.net.SocketFlow 设计案（JDK-8，含 Status 子目录）

## 1. API 功能
`jdk.net.SocketFlow`（public class，包私有构造）是 SO_FLOW_SLA 选项的值载体：
- 常量：`NORMAL_PRIORITY=1`、`HIGH_PRIORITY=2`。
- `static SocketFlow create()`：工厂，默认 priority=NORMAL_PRIORITY(1)、bandwidth=-1（UNSET）、status=NO_STATUS。
- `SocketFlow priority(int)`：**合法域恰为 {1,2}**，其余值抛 IllegalArgumentException("invalid priority")。
- `SocketFlow bandwidth(long)`：合法域 >=0，负数抛 IllegalArgumentException("invalid bandwidth")；-1 是内部 UNSET 哨兵。
- `int priority()` / `long bandwidth()` / `Status status()`：读回。
- `SocketFlow.Status` 枚举（8 常量，声明序）：NO_STATUS, OK, NO_PERMISSION, NOT_CONNECTED, NOT_SUPPORTED, ALREADY_CREATED, IN_PROGRESS, OTHER。

JDK-8 独有：JDK-9 起 SO_FLOW_SLA 与 SocketFlow 一并删除（Solaris 专属），JDK-27 无此类。

## 2. 使用方式
`SocketFlow f = SocketFlow.create().priority(SocketFlow.HIGH_PRIORITY).bandwidth(1024);` 链式 setter 均返回 this；随后 `Sockets.setOption(socket, ExtendedSocketOptions.SO_FLOW_SLA, f)`（仅 Solaris 支持；Linux 直接 UnsupportedOperationException）。status 由实现随选项生效情况更新，用户代码只读。

## 3. 参考来源
- Oracle javadoc（Java SE 8 jdk.net.SocketFlow / .SocketFlow.Status）。
- OpenJDK 源码：~/git/jdk8u-dev/jdk/src/share/classes/jdk/net/SocketFlow.java（priority 校验 `!= NORMAL && != HIGH` 即抛；bandwidth 校验 `<0` 即抛；UNSET=-1）。
- 实测记录（/tmp/probe-dnet/ProbeFlow.java，jdk8u372-b07 @ Linux）：
  - priority(-1/0/3/5/10/11/100) 全部 IllegalArgumentException("invalid priority")；只有 1/2 通过——任务书猜测的「1-10 合法」是错的，实测法定域 {1,2}。
  - bandwidth(-1/-2/MIN_VALUE) → IAE("invalid bandwidth")；0/100/2^40/Long.MAX_VALUE 通过。
  - 非法 setter 后值保持不变（先写后读验证）。
  - Status.valueOf("bogus") → IAE；valueOf(null) → NPE；values() 顺序即上列声明序。
  - SO_FLOW_SLA 经 Sockets.setOption 于未连接 socket → UnsupportedOperationException("SO_FLOW_SLA")。

## 4. 测试设计 + 易错细节
**测试设计**（SocketFlowTest 11→45；StatusTest 9→65）：
1. create() 默认三元组（priority/bandwidth/status）+ 两次 create 独立实例互不干扰。
2. priority 域表：7 个非法值逐个 IAE + 消息精确串；NORMAL/HIGH 往返；非法后值不变。
3. bandwidth 域表：3 个负值 IAE + 消息；0/MAX 合法；覆盖语义（后值生效）；链式组合。
4. status 恒 NO_STATUS（setter 不影响）；独立实例隔离。
5. SO_FLOW_SLA 真实通道异常（Linux UOE）。
6. 反射面：无 public 构造器；create/priority(int)/bandwidth(long) 签名存在；非枚举。
7. StatusTest：8 常量 ordinal 0..7 全表 + name/toString==name + getDeclaringClass；values() 每次新克隆数组且元素同一；valueOf(name()) 同一性（==）；valueOf bogus IAE / null NPE；compareTo 按 ordinal（raw 化绕开通配符捕获）；EnumSet.allOf 大小 8；final 修饰；SocketFlow.create().status() 联动回 NO_STATUS。

**易错细节**：
- priority 合法域只有 {1,2}：javadoc 明说「Must be either NORMAL_PRIORITY or HIGH_PRIORITY」，5/10 都是非法——写「1-10」断言必挂。
- bandwidth 的 -1 语义双重：内部默认值（可读回）与用户非法值（不可设）——「默认回读 -1」与「set(-1) 抛异常」要分开断言。
- `Enum<?> a.compareTo(Enum<?> b)` 编译不过（通配符捕获不一致），须 `((Enum) a).compareTo(b)` raw 化。
- values() 返回新克隆：两次调用数组引用不同（`!=`），但元素是同一常量（`==`）。
- StatusTest 原有 9 断言只查集合相等；深化后加了顺序（ordinal）锁定，防常量重排。

**8/27 差异表**

| 维度 | JDK-8 | JDK-27 |
|---|---|---|
| jdk.net.SocketFlow 类 | 有 | 删除 |
| ExtendedSocketOptions.SO_FLOW_SLA | 有（Linux 不支持 set） | 字段删除 |
| NetworkPermission target setOption/getOption.SO_FLOW_SLA | 文档化 | 文档删除 |
