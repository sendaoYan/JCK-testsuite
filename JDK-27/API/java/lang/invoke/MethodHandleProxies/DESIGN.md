# java.lang.invoke.MethodHandleProxies — 设计方案

## 1. API 功能
方法句柄转接口代理（final 工具类）：`asInterfaceInstance(T.class, mh)`（单方法接口适配）、`wrapperInstanceTarget/wrapperInstanceType`、`isWrapperInstance`。

## 2. Java 用户如何使用
```java
StringIntFunction f = MethodHandleProxies.asInterfaceInstance(
        StringIntFunction.class, mh);
int r = f.apply("abc");            // 转发到 mh
MethodHandle back = MethodHandleProxies.wrapperInstanceTarget(f);
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/invoke/MethodHandlesProxiesTest.java`。
- 框架桥接层用 asInterfaceInstance 把句柄包装成回调接口；等价于专用 java.lang.reflect.Proxy 但分发到 invokeExact。

## 4. 测试用例设计 + 自动化实现
白盒：final、无私有访问构造、三方法存在。
黑盒：public 接口 Runnable 派发执行；equals/hashCode/toString 代理语义；wrapperInstanceTarget/type 还原；isWrapperInstance 判别；非接口→IAE、null→NPE；类型不兼容句柄→IllegalArgumentException|WMTE。
对应 `MethodHandleProxiesTest.java` 的 `testBlackBox()`。
## 深化批次（2026-11-05）

新增：Runnable 副作用计数、装箱流（null 装箱 NPE helpful 消息）、同句柄两代理恒等式 equals/hashCode、27 侧 wrapper 是 hidden class 非 Proxy（与 8 分侧断言）、default 方法保留实现、负例消息矩阵（not a public interface/not a single-method interface/cannot convert MethodHandle）。（23→42 断言）
