# javax.management.JMRuntimeException 测试设计

## 1. API 功能
`JMRuntimeException` 是 JMX 运行时异常根类，继承 `RuntimeException`。构造器：无参、`(String message)`。
此外还有包私有 `(String, Throwable)` 重载（非导出 API，不测）。

## 2. Java 用户如何使用
```java
try {
    mbeanServer.getAttribute(name, "Missing");
} catch (JMRuntimeException e) {
    String why = e.getMessage(); // 详情消息，可能为 null（无参构造）
}
```
构造组合：`new JMRuntimeException("detail")` 保留消息；`new JMRuntimeException()` 消息为 null；`new JMRuntimeException((String)null)` 消息同样为 null。

## 3. 开源代码实际用法调研
来源：JDK 源码 `~/git/jdk25u-dev/src/java.management/share/classes`（检索该类型出现位置）与 Javadoc 契约；不依赖外部 GitHub 项目。
该类在 JMX 实现中被大量抛出（如 `MBeanServer.getAttribute`/`setAttribute` 在属性缺失/非法时、`registerMBean` 重复注册时），
JDK 自带测试 `~/git/jdk25u-dev/test/jdk/javax/management` 亦通过 `catch (JMRuntimeException e)` 校验这些失败路径。

## 4. 测试用例设计
- 白盒：`Class.forName` 校验 public/非接口/非抽象、直接父类为 `RuntimeException`、可序列化、存在无参与 `(String)` 构造器。
- 黑盒：`new JMRuntimeException("fixed-message")` 的 `getMessage()` 精确往返；无参与 `(String)null` 构造 `getMessage()` 为 null；`toString()` 含类名；`instanceof` 继承链校验。
- 确定性：固定输入字符串；只断言规范/实现稳定行为，不断言运行时生成的消息文案。

## 深化（2026-11）新建
### 1. API 功能
- javax.management 运行时异常层次根（直接父类 java.lang.RuntimeException）：`() ` / `(String)`。
- 归属表（本批范围）：RuntimeErrorException、RuntimeMBeanException、RuntimeOperationsException（monitor/relation/openmbean 的运行时异常在各自目录覆盖）。

### 2. Java 用户如何使用
JMX 运行时失败（包装 Error/RTE/非法参数）的公共祖先；不受检、无需显式 catch。

### 3. 参考来源
- Oracle javadoc javax.management.JMRuntimeException；javap 双侧对照；实现权威 `JMRuntimeException.java`。

### 4. 测试用例设计 + 易错规范细节
- 构造链 + getCause null；serialVersionUID = `6573344628407841861L`（两侧同值）；序列化 round-trip。
- 子类归属表 + instanceof RuntimeException 断言。

## 深化（2026-11）版本差异记录（本目录实测，JDK-8u372 vs JDK-27.0.1）
- 两侧 public API（javap -public）、serialVersionUID、异常类型与消息全部一致（见上文逐项）。
- 与本目录相关的全局版本差异：
  - `MBeanServer.registerMBean(null, name)`：JDK-8 抛 `NullPointerException`；JDK-27 抛 `RuntimeOperationsException`(包装 IllegalArgumentException)。
  - `javax.management.loading.MLet`：JDK-27 已移除（JDK-8 存在）。
