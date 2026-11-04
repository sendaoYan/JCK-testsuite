# javax.swing.plaf.nimbus.Effect.ArrayCache — DESIGN

## 1. API 功能
`Effect.ArrayCache` 是包私有 `Effect` 的 protected 静态嵌套类，作为 AppContext 级别的临时数组缓存，方法 `getTmpIntArray(int)`/`getTmpByteArray1/2/3(int)`（均 protected）。来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/nimbus/Effect.java`（Effect=37、getArrayCache=84、ArrayCache=93、getTmpIntArray=99、getTmpByteArray1=109）。

## 2. Java 用户如何使用
对外不可达（`Effect` 包私有 + `ArrayCache` protected），仅 JDK 内部 `Effect.getArrayCache()` 使用；外部用户无公开入口。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/nimbus/Effect.java:84`：`protected static ArrayCache getArrayCache()`，AppContext 单例。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/nimbus/Effect.java:93`：`protected static class ArrayCache`。

## 4. 测试用例设计（降级：纯白盒）
黑盒部分为空（无公开/包可见 API 可从默认包测试访问），仅保留 `ArrayCacheTest.testWhiteBox` 的结构反射断言（protected、非抽象、extends Object、无接口）。
