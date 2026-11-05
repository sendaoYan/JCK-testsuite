# javax.management.openmbean.OpenMBeanParameterInfo — 测试设计

## 1. API 功能
开放 MBean 参数元数据接口：描述操作/构造器参数的名称、描述、开放类型与取值约束（默认值、
合法值集合、最小/最大值）。方法集（javap 权威，15 个）：getName、getDescription、getOpenType、
getDefaultValue、getLegalValues、getMinValue、getMaxValue、hasDefaultValue、hasLegalValues、
hasMinValue、hasMaxValue、isValue(Object)、equals、hashCode、toString。无公开字段。

## 2. Java 用户典型用法
通过 OpenMBeanParameterInfoSupport 构造后以接口类型持有/传递；JConsole 等客户端用 getOpenType
渲染输入控件、用 isValue 做提交前校验。链路：Support 构造 → 接口引用 getOpenType/isValue →
equals 比较两个 MBean 的参数签名是否一致。

## 3. 参考来源
- Oracle javadoc：javax.management.openmbean.OpenMBeanParameterInfo（接口章节）。
- OpenJDK test/jdk/javax/management/openmbean/EqualsTest.java、OpenMBeanInfoSupportTest（equals 语义思路）。
- 行为以双侧 javap + 探测程序实测为准（本批）。

## 4. 测试用例设计 + 易错细节（OpenMBeanParameterInfoTest，批 G 深化）
- 结构：接口精确方法集匹配（name/参数个数集合相等，多一个少一个都报错）；无公开字段；
  不 extends OpenMBeanAttributeInfo（两者平级）。
- 契约：15 个方法全部经接口引用在 Support 实例上取真值；min/max 形态、legal 形态、plain 形态
  （一切约束缺失）分别断言；**易错：有 defaultValue 时 isValue(null)==true**（null 表示“用默认值”），
  plain 形态 isValue(null)==false——两种形态都要断言。
- 版本差异：JDK-8 与 JDK-27 方法集、行为完全一致（双侧探测 diff 为零）。
