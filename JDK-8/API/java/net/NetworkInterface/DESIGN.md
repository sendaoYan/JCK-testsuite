# java.net.NetworkInterface 测试设计案（JDK-8）

> 覆盖目标：确定性契约子集。查找负例（getByName/getByIndex/getByInetAddress 的 NPE/IAE/null）、
> 回环接口结构属性（name/displayName/index/MTU/硬件地址的**结构性**断言，不断言具体值）、
> 地址枚举、二次枚举 equals、经接口的 isReachable。环境相关值只断结构。

## 1. API 功能

- 静态查找：getNetworkInterfaces/getByName/getByIndex/getByInetAddress。
- 属性：getName/getDisplayName/getIndex/getParent/getSubInterfaces/getInetAddresses/getInterfaceAddresses。
- 状态：isUp/isLoopback/isPointToPoint/isVirtual/supportsMulticast/getHardwareAddress/getMTU。
- equals/hashCode/toString：按接口名（+下标）。

## 2. 用户如何使用

- 枚举本机接口、按名/下标/IP 找接口；组播/多宿主绑定前查询接口能力。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/net/NetworkInterface.html）。
- 双 JDK 探针（/tmp/probe-E）：getByName(null)→NPE、getByIndex(-1)→IAE、lo 上
  hwAddr=null、MTU=65536、toString "name:lo (lo)"、二次枚举 3/3 equals。

## 4. 测试用例设计（映射到 NetworkInterfaceTest.java，4→92 断言）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | getByName(null)→NPE；getByName("")/不存在→null；getByIndex(0)/999999→null、(-1)→IAE；getByInetAddress(null)→NPE | testLookupNegatives |
| P0 | 回环接口：isLoopback/isUp true、非 p2p、非 virtual、index>0、MTU>0、hwAddr null/全零、toString 含 name、parent null、无子接口 | testLoopbackProperties |
| P0 | getByName/getByIndex 自洽（互查一致）；equals/hashCode 重查相等；::1 与 127.0.0.1 映射同一接口 | testLoopbackProperties |
| P0 | 枚举结构：至少一个接口、名唯一非空、displayName 非空、index 正、全部布尔可调用；二次枚举全 equals | testEnumerationStructure |
| P0 | 回环的 getInetAddresses 枚举含回环地址；getInterfaceAddresses 数量一致 | testInetAddressesEnumeration |
| P1 | isReachable(lo,0,300) true；负 timeout IAE "timeout can't be negative" | testReachabilityViaInterface |

## 易错规范细节（已锁入断言）

- `getByName(null)` 抛 NPE（JDK-8 无消息）；`getByIndex(-1)` 抛 IAE；下标 0 永不分配。
- Linux 回环接口：isUp 恒 true、isPointToPoint/isVirtual false、getHardwareAddress 为 null（或全零）、
  toString 形如 `name:lo (lo)`、顶层接口 parent 为 null。
- equals 基于接口名：两次独立枚举得到的同名对象 equals 且 hashCode 相等。
- MTU/接口名/硬件地址为环境相关：只断 MTU>0、name 非空、hwAddr null/全零，绝不断言具体值。

## 5. 版本差异（JDK-8 vs JDK-27）

- JDK-27 额外有流式视图 `networkInterfaces()`/`inetAddresses()`/`subInterfaces()`（JDK-9+），
  JDK-27 侧断言流计数与枚举一致；JDK-8 无这些方法（跳过）。getIndex() 两侧都有（JDK-7+）。
