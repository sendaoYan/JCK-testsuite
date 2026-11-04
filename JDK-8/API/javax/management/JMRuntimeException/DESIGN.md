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
