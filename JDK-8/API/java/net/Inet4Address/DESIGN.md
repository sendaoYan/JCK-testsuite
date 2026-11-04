# java.net.Inet4Address 测试设计案（JDK-8）

> 覆盖目标：完整契约。final 工厂类（无公开构造器）、IPv4 字面量→Inet4Address 类型断言、
> 点分十进制与字节往返、D 类组播边界（224-239/4）、MC 范围矩阵、equals/hashCode 按字节、
> 防御性拷贝、序列化具体类型保持。全部行为先经双 JDK 探针实证。

## 1. API 功能

- 继承 InetAddress 的 final 类；无公开构造器（只能经 InetAddress.getByAddress/getByName 获得）。
- 地址表示（getAddress/getHostAddress）、分类（isLoopback/isAnyLocal/isLinkLocal/isSiteLocal/isMulticast）、
  MC 范围（isMCGlobal/isMCNodeLocal/isMCLinkLocal/isMCSiteLocal/isMCOrgLocal）。
- equals/hashCode：仅按 4 字节地址。

## 2. 用户如何使用

- `InetAddress.getByName("1.2.3.4")` 得到 Inet4Address，`instanceof Inet4Address` 区分地址族。
- D 类地址判断用 isMulticastAddress + isMC* 系列确定范围。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/net/Inet4Address.html）。
- OpenJDK `~/git/jdk8u-dev/jdk/test/java/net/InetAddress/`（IPv4Formats、B6206512 等）。
- 双 JDK 探针（/tmp/probe-E）：字面量类型、MC 范围矩阵、hashCode 精确值。

## 4. 测试用例设计（映射到 Inet4AddressTest.java，4→119 断言）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 白盒：final、无公开构造器、Serializable | testWhiteBox |
| P0 | 字面量→Inet4Address 精确类型 + hostAddress 往返（10 字面量） | testLiteralTyping |
| P0 | 字节往返矩阵（含高位字节 255 无符号处理）+ 防御性拷贝 | testBytesRoundTrip |
| P0 | 分类边界（127/8、169.254/16、10/8、172.16/12、192.168/16） | testClassification |
| P0 | D 类组播 224/4 两端 + MC 范围矩阵 + 单播无范围 | testMulticastBoundaries |
| P1 | equals/hashCode（精确值 16909060/2130706433/0、忽略 hostname、v4≠v6） | testEqualsHashCode |
| P1 | toString 形式 + 序列化具体类型保持 | testToStringAndSerialization |

## 易错规范细节（已锁入断言）

- 223.255.255.255 非组播、224.0.0.0 组播、239.255.255.255 组播、240.0.0.0 非组播（D 类恰好 224/4）。
- 255.255.255.255（受限广播）**不是**组播地址。
- MC 范围：224.0.0.x link-local、224.0.1.x global、239.192.0.0/16 org、239.255.0.0/16 site；IPv4 恒无 node-local。
- hashCode 即地址 int：1.2.3.4→0x01020304=16909060、127.0.0.1→0x7F000001=2130706433。
- getAddress() 返回克隆；序列化后仍是 Inet4Address（具体类型保持）。

## 5. 版本差异（JDK-8 vs JDK-27）

- JDK-27 额外有 `Inet4Address.ofLiteral`/`ofPosixLiteral`（静态工厂，JDK-27 侧覆盖 POSIX 八进制/短形式）；
  行为断言两侧一致，无差异。
