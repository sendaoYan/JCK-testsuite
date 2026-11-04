# java.awt.image.BufferStrategy 设计案

## 1. API 功能分组
- **类型**：abstract class。
- **抽象方法**：`getCapabilities()/getDrawGraphics()/contentsLost()/contentsRestored()/show()`。
- **具体方法**：`dispose()`。

## 2. Java 用户如何使用
`Component.createBufferStrategy(2)` 返回 BufferStrategy，用户 `getDrawGraphics()` 绘图、`show()` 翻转后缓冲区、`contentsLost()/contentsRestored()` 判断加速内存丢失。

## 3. 开源代码实际用法调研
- `java.awt.image.BufferStrategy` 是双缓冲抽象（`src/java.desktop/share/classes/java/awt/image/BufferStrategy.java`），`ComponentPeer` 内部提供实现。
- `test/jdk/java/awt/Component/` 缓冲策略用例依赖 show/getDrawGraphics（本用例抽象契约参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`BufferStrategyTest.testBlackBox()`，匿名实现）：
1. getCapabilities/contentsLost/contentsRestored/show 调用；dispose 无异常。
白盒（`testWhiteBox()`）：public abstract、5 个抽象方法、dispose 具体。
