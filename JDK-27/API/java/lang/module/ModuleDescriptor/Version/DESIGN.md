# java.lang.module.ModuleDescriptor.Version — 设计方案

## 1. API 功能
模块版本号（final、无 public 构造器）：`static parse(String)`（形如 `[0-9]+(\.[0-9]+)*(-[a-z0-9]+)?...`，必须以数字开头）、`compareTo`（数字分量逐位比较，非字典序）、`toString`（保留原串）。

## 2. Java 用户如何使用
```java
Version v = Version.parse("27.0.1");
if (v.compareTo(Version.parse("27")) > 0) ...
newModule("m").version(v).build();
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/module/ModuleDescriptor.java`：Version 为 JDK 版本方案（JEP 223）的子集。
- JDK `test/jdk/java/lang/module/VersionTest.java`：数字语义（1.9 < 1.10）、尾零等价、非法串。
- jdeps/jlink 比较模块版本决定覆盖。

## 4. 测试用例设计 + 自动化实现
白盒：final、Comparable、无 public 构造器、static parse 返回 Version。
黑盒：5 个版本串往返；equals/hashCode（含异类型）；分量式比较（1.9<1.10、2<10、1.2.3.4>1.2.3、1.2==1.2.0、27<27.0.1）；空串/非数字开头/null 抛 IAE。
对应 `VersionTest.java` 的 `testBlackBox()`。
