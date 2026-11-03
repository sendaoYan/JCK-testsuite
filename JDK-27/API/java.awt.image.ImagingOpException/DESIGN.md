# java.awt.image.ImagingOpException 设计案

## 1. API 功能分组
- **类型**：ImagingOpException extends RuntimeException。
- **构造**：`ImagingOpException(String)`。

## 2. Java 用户如何使用
当 BufferedImageOp/RasterOp 因图像格式不支持而失败时抛出；用户捕获该非受检异常表示图像操作失败。

## 3. 开源代码实际用法调研
- `java.awt.image.ImagingOpException` 被各类图像操作实现抛出（`src/java.desktop/share/classes/java/awt/image/`）。
- `test/jdk/java/awt/image/` 图像操作用例捕获 ImagingOpException 验证错误路径（本用例构造/继承参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`ImagingOpExceptionTest.testBlackBox()`）：
1. getMessage 精确；instanceof RuntimeException/Throwable；默认无 cause。
白盒（`testWhiteBox()`）：public、extends RuntimeException（非受检）。
