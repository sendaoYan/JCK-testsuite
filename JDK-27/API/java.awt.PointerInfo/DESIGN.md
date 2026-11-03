# java.awt.PointerInfo 黑盒行为测试设计

## 1. API 功能
- 值对象（final）：`getDevice()`（返回 `GraphicsDevice`）、`getLocation()`（返回 `Point`）。
- 无公开构造器，只能通过 `MouseInfo.getPointerInfo()` 获取。

## 2. Java 用户如何使用
封装鼠标指针的屏幕位置与所在显示设备：
```java
PointerInfo pi = MouseInfo.getPointerInfo();
GraphicsDevice dev = pi.getDevice();
Point loc = pi.getLocation();
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/PointerInfo.java`：由 Toolkit 构造，包内访问。
- `test/jdk/java/awt/MouseInfo/`、`test/jdk/java/awt/Mouse/` 通过 getPointerInfo 取得 PointerInfo 断言坐标/设备。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- `MouseInfo.getPointerInfo()` 返回 null（可接受）或非空时 `getDevice/getLocation` 非空且重复读取一致。
headful。
