# java.net.spi.InetAddressResolver.LookupPolicy 设计文档

## 1. API 功能

`InetAddressResolver` 的嵌套 `final class LookupPolicy`(JDK 18+,JEP 418),以位掩码
描述一次正向查找的地址族与返回顺序:

- 常量(实测精确值):`IPV4 = 1<<0 = 1`、`IPV6 = 1<<1 = 2`、`IPV4_FIRST = 1<<2 = 4`、
  `IPV6_FIRST = 1<<3 = 8`(已知位并集 `0xF`)。
- 静态工厂 `of(int characteristics)`;实例查询 `characteristics()` round-trip 原值。
- 私有构造器 `(int)`——只能经 `of()` 创建。
- **未覆写** `equals`/`hashCode`/`toString`:identity 语义,`of(1) == of(1)` 为
  `false`、`!of(1).equals(of(1))`,toString 为默认 `类名@hash`。

## 2. Java 用户如何使用

resolver 实现内部按 `lookupPolicy.characteristics()` 的位决定过滤/排序;
`of()` 的校验(javadoc + JDK-27 源码 `InetAddressResolver.java` 逐行对照):

1. 无 `IPV4` 且无 `IPV6` → `IllegalArgumentException("No address type specified")`
2. `IPV4_FIRST` 与 `IPV6_FIRST` 同设 → `IAE("Addresses order cannot be determined")`
3. `IPV4_FIRST` 设但 `IPV4` 未设 → `IAE("Addresses order and type do not match")`
4. `IPV6_FIRST` 设但 `IPV6` 未设 → 同上消息

**探针实测的宽松行为(重要)**:实现只按 4 个已知位判定,**不校验保留位**——
`of(IPV4|0x10)`、`of(0x80000001)`(符号位)等全部通过且 `characteristics()` 原样
round-trip(0x80000001 打印为 -2147483647)。javadoc 说"非法掩码抛 IAE",实测
"非法"仅指已知位的组合错误。0..15 全 16 值分类:{1,2,3,5,7,10,11} 合法,
其余 9 个非法(0/4/8/12→no type;6/9→mismatch;13/14/15→both-first)。

## 3. 参考来源

- JDK-27 源码 `/tmp/probe-J/jdksrc/.../InetAddressResolver.java`(`of()` 四段校验)。
- javadoc(JDK 18+):JEP 418。
- 探针 `/tmp/probe-J/ProbeLp.java`(常量值、0..15 矩阵、扩展位、identity)。

## 4. 测试用例设计 + 易错规范细节

文件 `LookupPolicyTest.java`(1 → 114 断言),六个部分:

- 白盒形状:final/嵌套类/私有构造器 (int)/恰好 2 个声明方法/equals 未声明
  (NoSuchMethodException 断言)/5 个字段 = 4 常量 + `private final int characteristics`。
- 常量精确值:1/2/4/8、`1<<0..1<<3`、并集 0xF。
- 合法矩阵:7 个合法组合逐个 round-trip + 非 null + toString。
- **0..15 表驱动**:每个值断言 `"OK:c"` 或 `"IAE:<精确消息>"`。
- 扩展负例:0xFFFFFFFF、-1、-2、0x7FFFFFFF→both-first 消息;0x80000000、
  Integer.MIN_VALUE、0x10/0x20/0x10000→no-type 消息;保留位宽松行为 4 例(带注释)。
- identity 语义:每次新实例、equals 非 value-based、hashCode 稳定。

易错点:① 检查顺序决定消息——both-FIRST 在 mismatch 之前,`of(13)`(IPV4+两 FIRST)
报 "cannot be determined" 而非 "do not match";② 保留位不抛;③ `of(0)` 必抛
(no type);④ equals/hashCode 是 Object 默认,不能当值对象用。

## 5. 版本差异

仅 JDK 18+ 存在。**JDK-8 反证**(temurin jdk8u372-b07):

```
$ javap -public java.net.spi.InetAddressResolver
Error: class not found: java.net.spi.InetAddressResolver
```

故本目录不做 JDK-8 镜像。
