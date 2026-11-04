# java.awt.BufferCapabilities 黑盒行为测试设计

## 1. API 功能
- 构造：`BufferCapabilities(frontCaps, backCaps, flipContents)`。
- 查询：`getFrontBufferCapabilities/getBackBufferCapabilities/getFlipContents`、`isPageFlipping/isFullScreenRequired/isMultiBufferAvailable`。
- `clone()`（浅拷贝）。

## 2. Java 用户如何使用
描述图形缓冲能力（`GraphicsConfiguration.getBufferCapabilities` 返回）：
```java
BufferCapabilities bc = gc.getBufferCapabilities();
boolean flip = bc.isPageFlipping();
BufferCapabilities.FlipContents fc = bc.getFlipContents();
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/BufferCapabilities.java`：`isPageFlipping()==(getFlipContents()!=null)`；`isFullScreenRequired/isMultiBufferAvailable` 恒 false；`clone` 浅拷贝。
- `src/java.desktop/share/classes/java/awt/GraphicsConfiguration.java`、`sun/awt/` 使用它描述缓冲能力。
- `test/jdk/java/awt/GraphicsDevice/`、`test/jdk/java/awt/image/VolatileImage/` 查询 BufferCapabilities。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 构造后 front/back/flip 引用一致性；isPageFlipping 由 flipContents 决定（null 与非 null 两路）。
- isFullScreenRequired/isMultiBufferAvailable 恒 false。
- `clone()` 浅拷贝（共享组件引用、容器不同、状态一致）。
headless-safe。
