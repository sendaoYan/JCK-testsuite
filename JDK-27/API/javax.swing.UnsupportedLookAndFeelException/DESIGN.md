# javax.swing.UnsupportedLookAndFeelException 设计案

## 1. API 功能
`UnsupportedLookAndFeelException` 表示外观不受支持。方法分组：
- 构造：`UnsupportedLookAndFeelException(String)`。

## 2. Java 用户如何使用
用户在 `setLookAndFeel` 失败时捕获该异常：
```java
try {
    UIManager.setLookAndFeel(laf);
} catch (UnsupportedLookAndFeelException e) {
    // fallback
}
```
典型链路：`new UnsupportedLookAndFeelException(msg)` → `getMessage`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/UnsupportedLookAndFeelException.java` 定义该异常（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/UnsupportedLookAndFeelException.java`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `Exception`。
- 行为（黑盒）：`testBlackBox()` 校验 `instanceof Exception` 与 `getMessage`。
