# java.io.ObjectInputFilter.FilterInfo 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 回调上下文接口（函数式）：`serialClass()`（当前类，字段级回调为 null）、`arrayLength()`（非数组 -1）、`depth()`（根 1）、`references()`（累计引用计数）、`streamBytes()`（累计字节数）。

## 2. Java 用户如何使用
- lambda 过滤器读取上下文做限额：`info -> info.depth() > 4 || info.references() > 1000 ? Status.REJECTED : Status.UNDECIDED`（JEP 290 反序列化防护）。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 接口定义与语义注释 — `jdk25u-dev/src/java.base/share/classes/java/io/ObjectInputFilter.java`（FilterInfo 注释与方法）。
- 官方测试 — `jdk25u-dev/test/jdk/java/io/Serializable/serial_filter/SerialFilterFunctionTest.java`（depth/refs/bytes 断言模式）。

## 4. 测试用例设计 + 自动化实现（FilterInfoTest.testBlackBox，探针 P14/P15 先行）
1. 白盒：接口、恰好 5 个抽象访问器、serialClass→Class、其余→long、无常量。
2. 自定义类根对象：serialClass 精确、arrayLength==-1、depth==1、references==1、streamBytes>0。
3. 数组 String[7]：类描述阶段回调（arr=-1）+ 长度检查阶段回调（arr=7），depth 恒 1。
4. 嵌套 Node→next：根 depth 1、后续回调 depth 2 且 serialClass==null、references 递增、streamBytes 非降。
5. **String 走 fast path 从不触发 filter**（REJECTED 也放行、回调计数为 0）——探针实证。
6. 同流两实例：类描述只回调一次、references 计到第二实例、bytes 增长。
- 健壮性: 每流独立 setObjectInputFilter（不动全局 factory/serialFilter）；无线程。

产出：`FilterInfoTest.java`（白盒 + 黑盒共 42 断言）。
