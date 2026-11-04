# java.awtRenderingHints 黑盒行为测试设计

## 1. API 功能
渲染提示表：9 个 KEY 两两不同、值对象 toString 精确（Antialiased rendering mode 等）、put/get/containsKey/remove/size/clear、null-map 构造、add 合并+add(null) NPE、视图 add UnsupportedOperationException 但反映变更、equals/hashCode 对普通 Map 拷贝、clone 隔离。

## 2. Java 用户如何使用
```java
hints.put(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON); g2.setRenderingHints(hints)
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 RenderingHints.java：KEY/Value 单例；JDK-27 新增 KEY_RESOLUTION_VARIANT 等键（版本差异，不行为断言）。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：键值唯一性；toString 精确；表语义全套；视图不可加但同步；equals/hashCode 对 Map 拷贝；clone 隔离。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
