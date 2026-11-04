# java.awtdnd/DragSource 黑盒行为测试设计

## 1. API 功能
拖源：headless 下 getDefaultDragSource()/构造器 HeadlessException、静态光标字段 null（headless 观察）、getDragThreshold()>0 系统属性驱动、isDragImageSupported 稳定布尔、适配器 no-op 可调；headful 分支（@key headful）测监听器增删。

## 2. Java 用户如何使用
```java
DragSource.getDefaultDragSource() // headless 抛 HeadlessException
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 DragSource.java；JDK-27 有 @key headful 用例。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：headless 异常矩阵；光标 null；threshold 正；适配器 no-op；DnD 常量联测。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
