# java.awt.color.ProfileDataException 设计案

## 1. API 功能分组
- **异常类型**：`ProfileDataException extends RuntimeException`，非受检。
- **构造**：`ProfileDataException(String)`。

## 2. Java 用户如何使用
当 ICC profile 数据与请求形式不符时抛出：例如 TRC 是表却调用 `getGamma()`，或 TRC 是单 gamma 却调用 `getTRC()`。调用方据此选择正确的访问方法。

## 3. 开源代码实际用法调研
- `ICC_ProfileRGB.getGamma(int)`/`getTRC(int)` 在 TRC 形式不符时抛 `ProfileDataException`（`src/java.desktop/share/classes/java/awt/color/ICC_ProfileRGB.java` 注释明确；`ICC_ProfileGray.getGamma/getTRC` 同理）。
- 运行时实测：sRGB 内置 profile 的 TRC 为表，故 `getGamma(REDCOMPONENT)` 抛该异常。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`ProfileDataExceptionTest.testBlackBox()`）：String 构造 message 保留、cause 为 null、instanceof RuntimeException；真实触发 `ICC_ProfileRGB.getGamma(red)` 在表型 TRC 时抛该异常。
白盒（`testWhiteBox()`）：public class、extends RuntimeException、String 构造存在。
