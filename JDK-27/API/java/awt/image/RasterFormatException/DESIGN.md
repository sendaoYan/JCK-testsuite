# java.awt.image.RasterFormatException 设计案

## 1. API 功能分组
- **类型**：RasterFormatException extends RuntimeException。
- **构造**：`RasterFormatException(String)`。

## 2. Java 用户如何使用
当栅格格式不合法（如非法宽度/高度/波段数）时抛出，用户捕获以识别栅格操作参数错误。

## 3. 开源代码实际用法调研
- `java.awt.image.RasterFormatException` 由 Raster/SampleModel 校验失败抛出（`src/java.desktop/share/classes/java/awt/image/`）。
- `test/jdk/java/awt/image/` 栅格操作用例捕获该异常验证参数校验（本用例构造/继承参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`RasterFormatExceptionTest.testBlackBox()`）：
1. getMessage 精确；instanceof RuntimeException；默认无 cause。
白盒（`testWhiteBox()`）：public、extends RuntimeException。
