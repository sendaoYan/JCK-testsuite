# java.lang.invoke.MutableCallSite — 设计方案

## 1. API 功能
可变调用点（CallSite 子类）：`setTarget` 可反复替换，`syncAll(CallSite[])` 全局可见性栅栏（跨线程发布新 target）。

## 2. Java 用户如何使用
```java
MutableCallSite cs = new MutableCallSite(mt);
cs.setTarget(a); ... cs.setTarget(b);
MutableCallSite.syncAll(new CallSite[]{ cs });
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/invoke/MutableCallSiteTest.java`（含 syncAll 内存语义）。
- 多态内联缓存（PIC）实现、JRuby 调用点缓存升级用它做分层 target 替换。

## 4. 测试用例设计 + 自动化实现
白盒：public 非 final、构造 (MethodType)/(MethodHandle)、syncAll 静态。
黑盒：占位 target 类型正确；换 target 立即可见；syncAll 空数组/单元素/多元素均可用；setTarget 错类型→ClassCastException；dynamicInvoker 反复调用跟随最新 target。
对应 `MutableCallSiteTest.java` 的 `testBlackBox()`。