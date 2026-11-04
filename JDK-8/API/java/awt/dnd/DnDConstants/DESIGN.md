# java.awtdnd/DnDConstants 黑盒行为测试设计

## 1. API 功能
拖放动作常量：ACTION_NONE=0/COPY=1/MOVE=2/COPY_OR_MOVE=3(=COPY|MOVE)/LINK=0x40000000、REFERENCE 别名 LINK、位独立性。

## 2. Java 用户如何使用
```java
if ((sourceActions & DnDConstants.ACTION_MOVE) != 0) ...
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 DnDConstants.java 常量表。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：六常量精确；别名；位独立。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
