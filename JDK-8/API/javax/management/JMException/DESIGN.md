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
