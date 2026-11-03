# java.io.ObjectInputFilter.Status 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 枚举三常量（声明序）：UNDECIDED（留给后续过滤）、ALLOWED（放行，短路后续拒绝）、REJECTED（终止反序列化）。
- 枚举机制继承：values()/valueOf(String)/name/ordinal/compareTo/Serializable 单例。

## 2. Java 用户如何使用
- 过滤器返回值：白名单 `isAllowed(c) ? ALLOWED : REJECTED`；宽进严出链 `merge(f, rejectUndecidedClass(...))` 用 UNDECIDED 占位。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 枚举定义与 checkFilter 消费（"filter status: " + status → InvalidClassException）— `jdk25u-dev/src/java.base/share/classes/java/io/ObjectInputFilter.java` 与 `ObjectInputStream.java` checkFilter。
- 官方测试 — `jdk25u-dev/test/jdk/java/io/Serializable/serial_filter/`（大量 Status 断言）。

## 4. 测试用例设计 + 自动化实现（StatusTest.testBlackBox，探针 P15 先行）
1. values() 声明序 UNDECIDED/ALLOWED/REJECTED，ordinal 0/1/2，name/toString、compareTo 全序。
2. values() 每次新数组；valueOf 精确命中、大小写敏感 IAE、未知名 IAE。
3. 真实接线：REJECTED→readObject 抛 InvalidClassException（消息以 "filter status: REJECTED" 结尾）。
4. ALLOWED 放行自定义类；UNDECIDED 孤立过滤器放行普通类（留给默认决策）。
5. 枚举常量经序列化往返保持单例同一性。
- 健壮性: 每流独立 filter；无全局设置。

产出：`StatusTest.java`（白盒 + 黑盒共 24 断言）。
