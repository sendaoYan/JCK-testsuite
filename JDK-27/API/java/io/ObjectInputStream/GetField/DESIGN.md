# java.io.ObjectInputStream.GetField 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 抽象类（ObjectInputStream.readFields() 返回实例）：`getObjectStreamClass()`（流端字段描述）。
- `defaulted(String)`（流中缺该字段→true，存在→false；未知名→IllegalArgumentException）。
- 九个 `get(String, T)` 重载（boolean/byte/char/short/int/long/float/double/Object）：返回流值或默认值；名字+签名都必须匹配（不匹配→IllegalArgumentException "no such field X with type Y"）。

## 2. Java 用户如何使用
- 自定义 readObject 里字段级取值（绕开反射式字段注入）：`GetField gf = in.readFields(); this.version = gf.get("version", 1);` 支持类版本演进缺省。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- FieldValues 实现（defaulted/get 查表、"no such field ... with type ..."）— `jdk25u-dev/src/java.base/share/classes/java/io/ObjectInputStream.java:2495,2585`。
- 官方用法样板 — `jdk25u-dev/test/jdk/java/io/Serializable/`（readFields 类演进用例）。

## 4. 测试用例设计 + 自动化实现（GetFieldTest.testBlackBox，探针 P13 先行）
1. 白盒：abstract、public 无参构造、getObjectStreamClass→ObjectStreamClass、恰好 9 个 get(String,T) 重载、defaulted→boolean、IOException/CNFE 异常声明。
2. readObject 回调内断言：getObjectStreamClass().getName()/SUID 精确。
3. defaulted：存在字段 false；未知名 IAE（含 "no such field"）。
4. 九类型 get 精确往返（boolean..Object 全类型样本值）。
5. 类型不匹配 get("i",1.0)→IAE 精确消息 "no such field i with type double"；未知字段同理。
6. 对象读尽后流 -1。
- 健壮性: 固定单对象流；回调内断言主线程复核计数。

产出：`GetFieldTest.java`（白盒 + 黑盒共 35 断言）。
