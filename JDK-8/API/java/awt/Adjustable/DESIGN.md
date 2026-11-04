# java.awtAdjustable 黑盒行为测试设计

## 1. API 功能
可调节值接口：HORIZONTAL=0/VERTICAL=1/NO_ORIENTATION=2；set/get value/min/max/unit/block/visible 往返；增删 AdjustmentListener；最小实现全 13 方法。

## 2. Java 用户如何使用
```java
class A implements Adjustable { ... }
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 Adjustable.java；Scrollbar/ScrollPaneAdjustable 实现（headful，不在本测）。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：三方向常量；最小实现全套往返；方向常量两两不同。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
