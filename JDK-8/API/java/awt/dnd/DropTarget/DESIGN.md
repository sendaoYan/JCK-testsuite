# java.awtdnd/DropTarget 黑盒行为测试设计

## 1. API 功能
放目标：headless 下全部构造器 HeadlessException；headful 分支（@key headful）测 active/getComponent/setDefaultActions/listener/FlavorMap/setFlavorMap 同实例/getDropTargetContext。

## 2. Java 用户如何使用
```java
new DropTarget() // headless 抛 HeadlessException
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 DropTarget.java。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：headless 构造器异常；headful 契约全套；DropTargetAdapter 需实现 drop。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
