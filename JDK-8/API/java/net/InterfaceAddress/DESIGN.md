# java.net.InterfaceAddress 测试设计案（JDK-8）

> 覆盖目标：确定性契约子集。只能从 NetworkInterface.getInterfaceAddresses() 获得
> （无公开构造器）；地址非空、前缀长度 0..128（v4 ≤32）、回环接口无广播地址（探针双侧稳定）、
> 重新枚举的 equals/hashCode 一致性、toString 含地址文本。

## 1. API 功能

- `getAddress()`（接口地址）、`getBroadcast()`（v4 广播或 null）、`getNetworkPrefixLength()`
  （v4: 0-32，v6: 0-128）、equals/hashCode/toString。

## 2. 用户如何使用

- 计算网络/广播地址、判断接口的网段前缀；与 NetworkInterface 枚举配合。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/net/InterfaceAddress.html）。
- 双 JDK 探针（/tmp/probe-E）：lo 上 2 条记录（`/127.0.0.1/8 [null]` 与
  `/0:0:0:0:0:0:0:1%lo/128 [null]`）、重查 equals true、toString 形式。

## 4. 测试用例设计（映射到 InterfaceAddressTest.java，6→67 断言）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 回环接口：地址非空且为回环/any-local、前缀范围正确、**广播恒 null**、toString 含地址 | testLoopbackInterfaceAddresses |
| P0 | 重查一致性：同接口二次 getInterfaceAddresses 逐条 equals+hashCode 相等（地址/前缀同步） | testEqualsHashCode |
| P0 | 白盒：无公开构造器（JDK-8 类非 final） | testWhiteBox |
| P1 | 全系统接口扫描：每条地址非空、前缀范围、广播若非 null 必为 IPv4 | testAllInterfacesStructural |

## 易错规范细节（已锁入断言）

- 回环接口（Linux lo）的 InterfaceAddress **getBroadcast()==null**（双侧探针稳定；点对点/回环无广播）。
- equals 比较地址+前缀+广播；重新枚举产生的新对象与旧对象逐条 equals（探针 2/2）。
- toString 形如 `/127.0.0.1/8 [null]`（地址/前缀/广播三段）。
- 前缀长度环境相关：只断 v4≤32、v6≤128、≥0（本机 lo 实测 8/128，不作断言）。

## 5. 版本差异（JDK-8 vs JDK-27）

- JDK-27 的 InterfaceAddress 是 final 且构造器 private；JDK-8 非 final、构造器包私有。
  行为断言两侧一致。
