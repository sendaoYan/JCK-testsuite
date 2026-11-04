# javax.management.ObjectName 黑盒行为测试设计

## 1. API 功能
- 构造/工厂：多种重载与 `getInstance`。
- 查询：getDomain/getKeyProperty/getKeyPropertyList/getCanonicalName/getCanonicalKeyPropertyListString。
- 谓词：isPattern/isDomainPattern/isPropertyPattern/isPropertyListPattern/isPropertyValuePattern。
- 匹配：`apply(ObjectName)`、`compareTo`；`quote/unquote`。

## 2. Java 用户如何使用
命名并定位 MBean；用模式（`*`、`?`）批量查询。
```java
ObjectName on = new ObjectName("com.example:type=Cache,name=1");
Set<ObjectName> s = mbs.queryNames(new ObjectName("com.example:type=Cache,*"), null);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/ObjectName/ApplyWildcardTest.java`、`ValueWildcardTest.java`、`ComparatorTest.java`、`NullEmptyKeyValueTest.java`、`ObjectNameGetInstanceTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/ObjectName.java`；通配匹配见 `com/sun/jmx/mbeanserver/Util.java`（仅 `*`、`?`，无 `[...]` 字符类）。

## 4. 测试用例设计 + 自动化实现
- 黑盒：构造与 getter、canonical 排序、equals/hashCode、各种 pattern 谓词、apply 匹配（含 `?`、`*`、无字符类）、compareTo、quote/unquote、MalformedObjectNameException 的精确消息。
- 白盒：public 类、implements Comparable、implements QueryExp、WILDCARD 存在。

## 深化（2026-11）
追加维度：引号值与引号内转义、通配符位置归一化、domain/key/value 非法字符矩阵、工厂与实例语义、防御性拷贝、quote/unquote 全转义集、模式名序列化。
- 引号值：`d:k="a,b"` 逗号不分割（getKeyPropertyList size==1）；canonical 保留引号原样；`d:k=""` 空引号值合法；`d:k=` 无引号空值两侧均合法（canonical `d:k=`）。
- 引号内转义：`\"`、`\\`、`\n` 合法并按原样回读；`\:`、`\e` → MalformedObjectNameException（消息 `Invalid escape sequence '\:' in quoted value`，两侧一致）；未闭合引号 → `Unterminated quoted value`。
- 引号 key 合法：`d:"q"=v`，getKeyProperty("\\"q\"")=="v"、getKeyProperty("q")==null。
- 通配符位置：`d:*,k=v` **合法**（brief 猜测会抛异常，实测两侧均归一化为 `d:k=v,*`、isPropertyListPattern true）；`d:b=2,a=1,*` canonical=`d:a=1,b=2,*`（键排序在 `*` 之前、equals 跨序相等）；`d:*=v`/`d:a*=v`（key 位置的 `*`）→ Malformed。
- domain 矩阵：`a=b:c`/`a,b:c` → `Unterminated key property part`；`a:b:c`/`::k=v` → `Invalid character ':' in key part of property`（首个 `:` 分隔 domain，第二个 `:` 落入 key）；`*:k=v`/`?:k=v` 合法 domain 模式；`:k=v` 空 domain 合法且非模式。
- value 矩阵：`d:k=a:b`/`d:k=a=b` → value 部分非法字符；空格与分号在无引号 value 中合法。
- 工厂：`getInstance` 相同串两次**不 intern**（`!=`，规范允许但不要求共享——两侧实测锁定）；`getInstance(ObjectName)` 对 exact 类返回同一实例；4 个 getInstance 重载全测。
- `getKeyPropertyList()` 返回**可变防御拷贝**（put 成功且原 name 不受影响）。
- quote 全转义集：`\n`→`"\\n"`、tab 不转义、空串→`""`、`*?"\"` round-trip；unquote(null) NPE、unquote(未引号/空串/坏转义/缺右引号) IAE。
- 3 参构造与 Hashtable 构造的 `*` 值 → isPropertyValuePattern true 且保留原值；引号值内 `*`（`d:k="a,*"`）同为 value 模式且按子串匹配。
- serialVersionUID = `1081892073854801359L`；模式名（domain/值/列表/引号）序列化 round-trip 后 flags 与 canonical 全等。

## 深化（2026-11）版本差异记录（本目录实测，JDK-8u372 vs JDK-27.0.1）
- 两侧 public API（javap -public）、serialVersionUID、异常类型与消息全部一致（见上文逐项）。
- 与本目录相关的全局版本差异：
  - `MBeanServer.registerMBean(null, name)`：JDK-8 抛 `NullPointerException`；JDK-27 抛 `RuntimeOperationsException`(包装 IllegalArgumentException)。
  - `javax.management.loading.MLet`：JDK-27 已移除（JDK-8 存在）。
