# java.io.InvalidClassException 测试方案 (DESIGN)

## 1. API 功能
- 2 构造器：`(String reason)`、`(String cname, String reason)`（JDK 8 无 cause 构造，JDK 9+ 才加入）；公共字段 `String classname`；getMessage 组合为 `"cname; reason"`。

## 2. Java 用户如何使用
- 反序列化类不兼容/无合法构造时捕获；读取 classname 与 reason。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/InvalidClassException.java`；`ObjectStreamClass.getExternalizableConstructor` 抛 "no valid constructor"。

## 4. 测试用例设计 + 自动化实现（InvalidClassExceptionTest.java，纯黑盒）
黑盒场景:
1. reason-only：消息原样、classname null。
2. cname+reason：classname 字段 + 组合消息。
3. JDK 8 两构造 getCause 均为 null。
4. 无 public 无参构造的 Externalizable 类反序列化抛 "no valid constructor"。
5. 作为 ObjectStreamException 捕获。
- 健壮性: 无反射白盒（JDK 8 无 ObjectInputFilter）。
