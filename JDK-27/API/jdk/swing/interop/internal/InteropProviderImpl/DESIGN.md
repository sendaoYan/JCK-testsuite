# jdk.swing.interop.internal.InteropProviderImpl 契约设计

## 1. API 功能
`InteropProviderImpl` 是 `sun.swing.InteropProvider` 的唯一内建实现：一个零方法的外观类。`sun.swing.InteropProvider` 本身也是空标记接口——它的存在意义是给外部框架（JavaFX SwingInterop、 SWT-AWT bridge）一个可断言的"可提供 interop 能力"类型凭据，真正的方法面在各 Wrapper 类上。

## 2. Java 用户如何使用
模块 jdk.unsupported.desktop 把它暴露在 internal 包；外部框架通过服务发现或直接 new 拿到实例后，配合 LightweightFrameWrapper 等类驱动嵌入。空实现意味着"能力声明"与"能力载体"分离。

## 3. 开源代码实际用法调研
- OpenJDK java.desktop：sun.swing.InteropProvider 声明于 jdk.unsupported.desktop 的共享导出面，供 JavaFX SwingNode 的内部通道引用。
- OpenJFX 的 SwingNode/swing 桥接模块以等价接口（com.sun.javafx.embed）做同构标记，双方通过 SWING_INTEROP 事件协议通信，不依赖本类方法调用。

## 4. 测试用例设计+自动化实现
- 零方法外观：getDeclaredMethods().length == 0；单构造器且 public；包名精确 jdk.swing.interop.internal；超类 Object。
- 标记接口链：sun.swing.InteropProvider 是空接口、isAssignableFrom 成立、impl 直接接口集合恰为它一个。
- 自动化：jtreg `run main` + `check()`，@modules 带 `jdk.unsupported.desktop/jdk.swing.interop.internal:+open`。
