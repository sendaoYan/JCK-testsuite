# java.awt.color.CMMException 设计案

## 1. API 功能分组
- **异常类型**：`CMMException extends RuntimeException`，非受检。
- **构造**：`CMMException(String)`（仅 String 构造）。

## 2. Java 用户如何使用
当色彩管理模块（CMM）在 ICC profile 处理中出错（如 profile 数据损坏）时抛出。调用方通常不显式捕获（非受检），由框架层兜底。构造带描述：`throw new CMMException("Color management module error")`。

## 3. 开源代码实际用法调研
- JDK 内部 CMM 桥接层（`java.desktop` 的 `sun.java2d.cmm`）在 profile 转换失败时抛 `CMMException`（源码 `src/java.desktop/share/classes/sun/java2d/cmm/` 相关文件）。
- 用户代码很少直接 new 该异常，多由 `ColorConvertOp`/`ICC_ColorSpace` 转发。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`CMMExceptionTest.testBlackBox()`）：String 构造 message 保留、cause 为 null、instanceof RuntimeException（非受检）。
白盒（`testWhiteBox()`）：public class、extends RuntimeException、String 构造存在。
