# java.awtInsets 黑盒行为测试设计

## 1. API 功能
四边内边距值类：字段 top/left/bottom/right 顺序、set() 重置、equals+hashCode（(1,2,3,4)→577）、clone 独立、负值允许、Serializable(sUID=-2272572637695466749L)、toString=`java.awt.Insets[top=..,left=..,bottom=..,right=..]`。

## 2. Java 用户如何使用
```java
i.set(1,2,3,4); Insets j=(Insets)i.clone(); j.top=99 // i 不变
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 Insets.java：hashCode=top*31^3+left*31^2+bottom*31+right 计算值。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：字段顺序；set() 重置；equals/hashCode=577；clone 独立；负值序列化往返；sUID；toString 精确。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
