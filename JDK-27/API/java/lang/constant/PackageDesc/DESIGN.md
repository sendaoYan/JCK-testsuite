# java.lang.constant.PackageDesc — 设计方案

## 1. API 功能
包名义描述（interface）：`of(二进制名)`、`ofInternalName(内部名)`；`internalName()`（抽象）、`name()`（default，由 internalName 推导）；值相等。

## 2. Java 用户如何使用
```java
PackageDesc p = PackageDesc.of("java.lang");
p.internalName(); // "java/lang"
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/constant/PackageDesc.java`。
- ClassFile API（ClassFile.CodeBuilder/模块属性）以 internalName 形式消费。

## 4. 测试用例设计 + 自动化实现
白盒：接口、5 方法、of/ofInternalName 静态、name 为 default。
黑盒：dotted/slashed 双向映射与互换等价；根包双空串；值相等（含跨工厂形式 HashSet 去重）；of("java/lang") IAE、分号 IAE、两个工厂 null NPE。
对应 `PackageDescTest.java` 的 `testBlackBox()`。

## 5. 契约深化（JDK-27 实测锁定）
- **点分名校验矩阵（IAE）**：`java..lang`（空段）、`.a`/`a.`（首尾点）、`a.b;.c`；内部形式 `a//b` → IAE。段内非标识符字符（如 `a.b-c`）**不**被拒绝（只校验分隔符位置，27 实测）。
- `of("")` 即根包，与 `ofInternalName("")` equals；name()/internalName() 互逆一致。

## 版本差异
JDK-8 无此包（java.lang.constant 是 12+、java.lang.IO 是 25+（JEP 495）、java.lang.foreign 是 19+ 预览 / 22+ 正式）；本目录测试仅 JDK-27，不镜像 JDK-8 侧。
