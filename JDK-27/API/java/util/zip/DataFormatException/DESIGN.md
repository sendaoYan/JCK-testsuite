# java.util.zip.DataFormatException 测试方案

白盒结构断言在 `DataFormatExceptionTest.testWhiteBox()`，黑盒行为断言在 `DataFormatExceptionTest.testBlackBox()`。

## 1. API 功能（按方法分组）

- **构造**：`DataFormatException()`、`DataFormatException(String s)`。
- 继承：直接继承 `Exception`（不是 `IOException`），表示压缩/解压数据格式非法。

## 2. Java 用户如何使用

- 抛出侧：`Inflater.inflate(...)` 在压缩数据非法时抛 `DataFormatException`。
- 捕获侧：`try { inflater.inflate(out); } catch (DataFormatException e) { ... }`。
- 真实组合链路：`Inflater.setInput(垃圾数据)` → `inflate(...)` → 捕获 `DataFormatException`。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码，逐条标注来源。

- `src/java.base/share/classes/java/util/zip/Inflater.java:342`：`inflate(...) throws DataFormatException` —— 抛出处契约。
- `src/java.base/share/classes/java/util/zip/Inflater.java:448`：`public int inflate(byte[] output) throws DataFormatException` —— 常用重载抛出处。
- `src/java.base/share/classes/java/util/zip/DataFormatException.java`：`public class DataFormatException extends Exception` —— 继承关系。

## 4. 测试用例设计 + 自动化实现

- **正常**：无参构造 `getMessage()==null`、`getCause()==null`；`String` 构造保留 message。
- **继承**：`instanceof Exception`、`instanceof Throwable`。
- **自动化**：`DataFormatExceptionTest.testBlackBox()`；`testWhiteBox()` 校验 `getSuperclass()==Exception.class` 与两个构造器。触发真实 `inflate` 抛异常不稳定（非法数据可能返回 false），故异常类以构造器/继承为主（按任务要求）。
- **健壮性**：无副作用、headless、无 `@key`。
