# javax.management.JMException 测试设计

## 1. API 功能
`JMException` 是 JMX 受检异常，继承 `Exception`。构造器：无参、`(String message)`。
作为 `JMException` 族成员，被 `MBeanServer`/`MBeanServerConnection` 各操作在失败时抛出，承载可读 detail message。

## 2. Java 用户如何使用
```java
try {
    mbeanServer.getAttribute(name, "Missing");
} catch (JMException e) {
    String why = e.getMessage(); // 详情消息，可能为 null（无参构造）
}
```
构造组合：`new JMException("detail")` 保留消息；`new JMException()` 消息为 null；`new JMException((String)null)` 消息同样为 null。

## 3. 开源代码实际用法调研
来源：JDK 源码 `~/git/jdk25u-dev/src/java.management/share/classes`（检索该类型出现位置）与 Javadoc 契约；不依赖外部 GitHub 项目。
该类在 JMX 实现中被大量抛出（如 `MBeanServer.getAttribute`/`setAttribute` 在属性缺失/非法时、`registerMBean` 重复注册时），
JDK 自带测试 `~/git/jdk25u-dev/test/jdk/javax/management` 亦通过 `catch (JMException e)` 校验这些失败路径。

## 4. 测试用例设计
- 白盒：`Class.forName` 校验 public/非接口/非抽象、直接父类为 `Exception`、可序列化、存在无参与 `(String)` 构造器。
- 黑盒：`new JMException("fixed-message")` 的 `getMessage()` 精确往返；无参与 `(String)null` 构造 `getMessage()` 为 null；`toString()` 含类名；`instanceof` 继承链校验。
- 确定性：固定输入字符串；只断言规范/实现稳定行为，不断言运行时生成的消息文案。

## 深化（2026-11）新建
### 1. API 功能
- javax.management 受检异常层次根（直接父类 java.lang.Exception）：`() ` / `(String)`。
- 归属表（本批范围）：ReflectionException、MBeanException、MBeanRegistrationException、OperationsException 及其 9 个子类。

### 2. Java 用户如何使用
所有 JMX API 受检异常的公共祖先；方法签名 `throws JMException` 常见。

### 3. 参考来源
- Oracle javadoc javax.management.JMException；javap 双侧对照；实现权威 `JMException.java`。

### 4. 测试用例设计 + 易错规范细节
- 构造链 + getCause null；serialVersionUID = `350520924977331825L`（两侧同值）。
- 序列化 round-trip：消息保真、cause null。
- 子类归属表：13 个类逐一断言 instanceof JMException；明确 InvalidApplicationException **不在**该层次（它直接 extends java.lang.Exception）。

## 深化（2026-11）版本差异记录（本目录实测，JDK-8u372 vs JDK-27.0.1）
- 两侧 public API（javap -public）、serialVersionUID、异常类型与消息全部一致（见上文逐项）。
- 与本目录相关的全局版本差异：
  - `MBeanServer.registerMBean(null, name)`：JDK-8 抛 `NullPointerException`；JDK-27 抛 `RuntimeOperationsException`(包装 IllegalArgumentException)。
  - `javax.management.loading.MLet`：JDK-27 已移除（JDK-8 存在）。
