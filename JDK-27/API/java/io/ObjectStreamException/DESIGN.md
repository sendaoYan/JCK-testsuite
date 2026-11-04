# java.io.ObjectStreamException 测试方案 (DESIGN)

## 1. API 功能
- 抽象类，所有序列化机制异常的父类；四个 protected 构造（()/String/Throwable/String+Throwable）。

## 2. Java 用户如何使用
- `catch (ObjectStreamException e)` 一次性捕获全部序列化异常（InvalidClass/InvalidObject/NotActive/NotSerializable/OptionalData/StreamCorrupted/WriteAborted）。

## 3. 开源代码实际用法调研
- 类型定义 — `jdk25u-dev/src/java.base/share/classes/java/io/ObjectStreamException.java`（protected 构造族）。
- 各具体子类 — 同目录下 InvalidClassException.java 等 7 个文件。

## 4. 测试用例设计 + 自动化实现（ObjectStreamExceptionTest.java）
- 白盒: abstract、extends IOException、全部构造 protected、7 个具体子类均 assignable。
- 黑盒场景:
  1. 反射实例化被拒。
  2. 子类经 4 个 protected 构造的 message/cause 行为。
  3. 子类异常被 `catch (ObjectStreamException)` 捕获。
  4. toString/stackTrace 行为。
- 健壮性: 全内存。

产出：`ObjectStreamExceptionTest.java`（白盒 13 + 黑盒 9 断言）。
