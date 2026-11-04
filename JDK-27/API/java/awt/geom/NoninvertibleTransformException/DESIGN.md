# java.awt.geom.NoninvertibleTransformException 设计案

## 1. API 功能分组
- **异常类型**：`NoninvertibleTransformException extends Exception`，受检。
- **构造**：`NoninvertibleTransformException(String)`（仅 String 构造）。

## 2. Java 用户如何使用
对行列式为 0 的仿射变换（如 `AffineTransform.getScaleInstance(0, 1)`）调用 `createInverse()` 或 `invert()` 时抛出，调用方必须 catch（受检）。典型：`try { at.invert(); } catch (NoninvertibleTransformException e) { ... }`。

## 3. 开源代码实际用法调研
- `AffineTransform.createInverse()/invert()`（`src/java.desktop/share/classes/java/awt/geom/AffineTransform.java`）在 `getDeterminant() == 0` 时抛 `new NoninvertibleTransformException("Determinant is 0")`（源码可见）。
- `test/jdk/java/awt/geom/AffineTransform/TestInvertMethods.java` 用奇异矩阵断言 `createInverse()`/`invert()` 抛该异常（与本用例一致）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`NoninvertibleTransformExceptionTest.testBlackBox()`）：
1. String 构造 message 保留、cause 为 null、属受检异常（instanceof Exception 且非 RuntimeException）。
2. 真实触发：`getScaleInstance(0,1).createInverse()` 与 `.invert()` 均抛该异常。
白盒（`testWhiteBox()`）：public class、extends Exception、String 构造存在。
