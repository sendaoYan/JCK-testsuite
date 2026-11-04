# java.io.ObjectStreamException 测试方案 (DESIGN)

## 1. API 功能
- 抽象类，所有序列化机制异常的父类；JDK 8 仅两个 protected 构造：`()` 与 `(String)`（`(Throwable)`/`(String,Throwable)` 为后续版本新增）。

## 2. Java 用户如何使用
- `catch (ObjectStreamException e)` 一次性捕获全部序列化异常（InvalidClass/InvalidObject/NotActive/NotSerializable/OptionalData/StreamCorrupted/WriteAborted）。

## 3. 开源代码实际用法调研
- 类型定义 — `jdk8u-dev/jdk/src/share/classes/java/io/ObjectStreamException.java`（protected 构造族）。
- 各具体子类 — 同目录下 InvalidClassException.java 等 7 个文件。

## 4. 测试用例设计 + 自动化实现（ObjectStreamExceptionTest.java，纯黑盒）
黑盒场景:
1. 抽象类反射实例化被拒。
2. 子类经 `()` 与 `(String)` 两个 protected 构造的 message 行为。
3. 子类异常被 `catch (ObjectStreamException)` 捕获。
4. 具体 JDK 子类均属于 ObjectStreamException/IOException 家族（OptionalDataException 无公共构造，用 isAssignableFrom 校验）。
5. toString/stackTrace 行为。
- 健壮性: 全内存。
