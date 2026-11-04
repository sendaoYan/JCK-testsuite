# java.net.Inet6Address 测试设计案（JDK-8）

> 覆盖目标：完整契约。final 工厂类（无公开构造器）、IPv6 文本→全展开形式归一化矩阵、
> scope id 语义（数字 scope、NetworkInterface 变体、**equals 忽略 scope**——探针实证）、
> isIPv4CompatibleAddress 字节规则、组播范围矩阵、序列化 scope 保持。
> 全部行为先经双 JDK 探针实证（两侧一致）。

## 1. API 功能

- 静态工厂：`Inet6Address.getByAddress(String, byte[], int)` / `(String, byte[], NetworkInterface)`。
- 访问：`getScopeId`/`getScopedInterface`/`getHostAddress`（含 `%scope` 后缀）。
- 分类：isIPv4CompatibleAddress + 继承的 is* / MC* 系列。

## 2. 用户如何使用

- `InetAddress.getByName("::1")` 得到 Inet6Address；链路本地地址配 scope id 用
  `Inet6Address.getByAddress(host, fe80bytes, nif_index)` 构造。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/net/Inet6Address.html）。
- OpenJDK `~/git/jdk8u-dev/jdk/test/java/net/InetAddress/`（B6206512 scope 相关）。
- 双 JDK 探针（/tmp/probe-E）：文本归一化、scope 渲染、equals 忽略 scope。

## 4. 测试用例设计（映射到 Inet6AddressTest.java，4→81 断言）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 白盒：final、无公开构造器、Serializable | testWhiteBox |
| P0 | 文本归一化矩阵（10 组：压缩/全写/大小写/尾零压缩 → 全展开小写） | testTextFormNormalisation |
| P0 | 组播范围 ff01/ff02/ff05/ff08/ff0e/ff0f 矩阵 + ff00::/8 两端 | testMulticastScopes |
| P0 | scope id：数字 scope 保留与 `%n` 渲染（**0 也渲染**）、null nif 变体无后缀、equals 忽略 scope | testScopeId |
| P0 | isIPv4CompatibleAddress 字节规则（前 10 字节全零）+ IPv4-mapped 折叠为 Inet4Address | testIPv4CompatibleAddress |
| P1 | equals/hashCode（`::`→0、`::1`→1、v6≠v4）+ 序列化类型/scope/hostname 保持 + 坏 scope 接口名→UHE | testEqualsHashCodeSerialization |

## 易错规范细节（已锁入断言）

- `getHostAddress()` 恒为全展开形式（`::1`→`0:0:0:0:0:0:0:1`），hex 小写；**不输出压缩形式**。
- 数字 scope（含 0）经 getByAddress 设置后渲染为 `%n` 后缀；NetworkInterface=null 变体不设 scope → 无后缀。
- **equals/hashCode 忽略 scope id**（fe80::1%5 equals fe80::1%7，探针双侧一致）。
- isIPv4CompatibleAddress = 字节 0-9 全零（`::1`/`::`/`::1:0` true；byte10 非零 false）。
- `getByName("::ffff:1.2.3.4")` 折叠为 Inet4Address（4 字节），不产生 Inet6Address。
- `getByName("fe80::1%<不存在的接口名>")` → UnknownHostException。
- getByAddress(String,byte[],int) 对 null/4 字节/5 字节 → UHE。

## 5. 版本差异（JDK-8 vs JDK-27）

- JDK-27 有 `Inet6Address.ofLiteral`（返回 InetAddress 声明类型；拒绝 IPv4 字面量→IAE）。
  文本归一化、scope、equals 语义两侧完全一致。
