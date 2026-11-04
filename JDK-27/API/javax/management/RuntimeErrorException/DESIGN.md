# javax.management.RuntimeErrorException 测试设计

## 1. API 功能
`RuntimeErrorException` 继承 `JMRuntimeException`，用于包装一个底层 `Error`。
构造器：`(Error)`、`(Error, String message)`；查询：`getTargetError()`（返回被包装对象）与 `getCause()`（镜像同一对象）。

## 2. Java 用户如何使用
```java
try {
    mbean.invoke(...);
} catch (Exception ex) {
    throw new RuntimeErrorException(ex, "invoke failed");
}
// 上层：
catch (RuntimeErrorException e) {
    Error root = e.getTargetError(); // 与 e.getCause() 同一对象
}
```

## 3. 开源代码实际用法调研
来源：JDK 源码 `~/git/jdk25u-dev/src/java.management/share/classes`；该包装异常在 `MBeanServer`/连接器层将目标异常包装为 JMX 异常时使用，
`test/jdk/javax/management` 用例通过 `getTargetError()`/`getCause()` 校验包装往返。

## 4. 测试用例设计
- 白盒：public/非接口/非抽象、直接父类 `JMRuntimeException`、可序列化、存在 `(Error)` 与 `(Error, String)` 构造器、`getTargetError`/`getCause` 方法。
- 黑盒：单参构造 `getMessage()` null、`getTargetError()` 与 `getCause()` 精确往返；双参构造 message 保留；独立实例互不影响；`toString()` 含类名。
- 确定性：固定 message 与独立构造的 cause 对象。
