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

## 深化追加（契约级）— GetField 读端字段契约（批次 ioI）

### 覆盖维度
1. **字段演进（同一类名模拟旧流）**：不走双类加载器，直接对序列化字节做手术 `removeIntField`（从首个 TC_CLASSDESC 删除字段描述符、字段计数减一、并删除其 4 字节数据；布局见 ObjectInputStream DESIGN「serialVersionUID 字节布局」）。旧流(a,b) 对新类(a,b,c)：present 字段照常恢复，流缺字段 `get("c",默认)` 返回**调用方默认值**，`defaulted("c")==true`、`defaulted("a")==false`；修剪后的流仍干净终止（read()==-1）。
2. **defaulted() 全类型**：九种类型字段全部在流中 → 全部 false。
3. **IAE 精确消息表（6 条实测）**：`no such field i with type long` / `no such field z with type int` / `no such field o with type int` / `no such field j with type int` / `no such field d with type float` / `no such field missing with type class java.lang.Object`。要点：**原始重载优先的重载决议**——`get("o", 5)`（对象字段配 int 默认值）解析到 `get(String,int)` 而非 Object 重载。
4. **getObjectStreamClass()**：与 `ObjectStreamClass.lookup(cls)` 名称/UID 一致；`lookup(Object.class)==null`（非 Serializable 无描述符）。
5. **极值往返**：NaN、-0.0（doubleToRawLongBits 位级断言）。
6. **readFields 上下文规则**：二次 readFields、readFields 之后 defaultReadObject → NotActiveException `not in readObject invocation or fields already read`。
7. **对象字段图身份**：同一节点写入两个字段 → 读回 rx==ry；**字段在流中但值为 null ≠ 字段缺失**（前者 get 返回 null 且 defaulted=false，后者返回默认值且 defaulted=true）。

### 版本差异（8 vs 27）
- `GetField.get(String,Object)` 自 JDK-9 起声明 `throws ClassNotFoundException`，27 侧测试须同时捕获 CNFE；行为与消息完全一致。

产出：`GetFieldTest.java` 35 → 79 断言（含白盒结构断言；jtreg 通过）。
