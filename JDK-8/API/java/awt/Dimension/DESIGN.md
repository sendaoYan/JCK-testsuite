# java.awtDimension 黑盒行为测试设计

## 1. API 功能
尺寸值类：setSize(int|double，double 向上取 ceil)/getSize 独立副本/equals+hashCode 值语义/负宽高允许/Serializable(sUID=4723952579491349524L)/toString=`java.awt.Dimension[w=..,h=..]`。

## 2. Java 用户如何使用
```java
d.setSize(10.2,-3.7) -> (11,-3)（ceil）；d2 = d.getSize() 改 d2 不影响 d
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 Dimension.java：ceil 语义；JDK 自带测试 test/jdk/java/awt/... 大量 getSize 用法。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：ceil 边界(10.2→11,-3.7→-3)；getSize 独立性；equals/hashCode；负尺寸合法；sUID；toString 精确。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
