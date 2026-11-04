# java.io.ObjectInputStream.GetField 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 抽象类（ObjectInputStream.readFields() 返回实例）：`getObjectStreamClass()`（流端字段描述）。
- `defaulted(String)`（流中缺该字段→true，存在→false；未知名→IllegalArgumentException）。
- 九个 `get(String, T)` 重载（boolean/byte/char/short/int/long/float/double/Object）：返回流值或默认值；名字+签名都必须匹配（不匹配→IllegalArgumentException "no such field X with type Y"）。
- JDK 8 差异：`get(String,Object)` 仅声明 `IOException`（`ClassNotFoundException` 声明为 JDK 9+ 新增）。

## 2. Java 用户如何使用
- 自定义 readObject 里字段级取值（绕开反射式字段注入）：`GetField gf = in.readFields(); this.version = gf.get("version", 1);` 支持类版本演进缺省。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- FieldValues 实现（defaulted/get 查表、"no such field ... with type ..."）— `jdk8u-dev/jdk/src/share/classes/java/io/ObjectInputStream.java:2495,2585`。
- 官方用法样板 — `jdk8u-dev/jdk/test/java/io/Serializable/`（readFields 类演进用例）。

## 4. 测试用例设计 + 自动化实现（GetFieldTest.java，纯黑盒）
黑盒场景（readObject 回调内断言）:
1. getObjectStreamClass().getName()/SUID 精确。
2. defaulted：存在字段 false；未知名 IAE（含 "no such field"）。
3. 九类型 get 精确往返（boolean..Object 全类型样本值）。
4. 类型不匹配 get("i",1.0)→IAE 精确消息 "no such field i with type double"；未知字段同理。
5. 对象读尽后流 -1。
- 健壮性: 固定单对象流；JDK-8 为黑盒（无反射白盒）；catch 仅 `IOException`（JDK 8 无 CNFE 声明）。
