# javax.management.RuntimeOperationsException 测试设计

## 1. API 功能
`RuntimeOperationsException` 继承 `JMRuntimeException`，用于包装一个底层 `RuntimeException`。
构造器：`(RuntimeException)`、`(RuntimeException, String message)`；查询：`getTargetException()`（返回被包装对象）与 `getCause()`（镜像同一对象）。

## 2. Java 用户如何使用
```java
try {
    mbean.invoke(...);
} catch (Exception ex) {
    throw new RuntimeOperationsException(ex, "invoke failed");
}
// 上层：
catch (RuntimeOperationsException e) {
    RuntimeException root = e.getTargetException(); // 与 e.getCause() 同一对象
}
```

## 3. 开源代码实际用法调研
来源：JDK 源码 `~/git/jdk25u-dev/src/java.management/share/classes`；该包装异常在 `MBeanServer`/连接器层将目标异常包装为 JMX 异常时使用，
`test/jdk/javax/management` 用例通过 `getTargetException()`/`getCause()` 校验包装往返。

## 4. 测试用例设计
- 白盒：public/非接口/非抽象、直接父类 `JMRuntimeException`、可序列化、存在 `(RuntimeException)` 与 `(RuntimeException, String)` 构造器、`getTargetException`/`getCause` 方法。
- 黑盒：单参构造 `getMessage()` null、`getTargetException()` 与 `getCause()` 精确往返；双参构造 message 保留；独立实例互不影响；`toString()` 含类名。
- 确定性：固定 message 与独立构造的 cause 对象。

## 深化（2026-11）
追加维度：null-target 语义、层级、serialVersionUID、序列化 round-trip（外层消息+内层 target 消息+cause==target）、真实触发。
- 构造：`(RuntimeException)` / `(RuntimeException, String)`；**null target 合法**（getTargetException/getCause 均 null，message 不受影响）。
- getCause() 与 getTargetException()/getTargetError() 恒为同一实例。
- serialVersionUID = `-8408923047489133588L`（两侧同值）。
- 真实触发：`new Attribute(null, v)` → 本异常（包装 IllegalArgumentException，外层 message null）；typed AttributeList 越界 add → 本异常（包装 IOOBE）。
- **版本差异（本目录核心）**：`registerMBean(null, name)` —— JDK-8 抛裸 `NullPointerException`；JDK-27 抛 `RuntimeOperationsException`（包装 IllegalArgumentException）。两侧各自断言各自真实行为（testDeepVersionSpecificTrigger）。

## 深化（2026-11）版本差异记录（本目录实测，JDK-8u372 vs JDK-27.0.1）
- 两侧 public API（javap -public）、serialVersionUID、异常类型与消息全部一致（见上文逐项）。
- 与本目录相关的全局版本差异：
  - `MBeanServer.registerMBean(null, name)`：JDK-8 抛 `NullPointerException`；JDK-27 抛 `RuntimeOperationsException`(包装 IllegalArgumentException)。
  - `javax.management.loading.MLet`：JDK-27 已移除（JDK-8 存在）。
