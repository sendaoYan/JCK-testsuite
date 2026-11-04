# java.io.ObjectOutputStream.PutField 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 抽象类（ObjectOutputStream.putFields() 返回实例）：九个 `put(String, T)` 重载（名字+签名校验，不匹配→IllegalArgumentException "no such field X with type Y"）。
- `writeFields()`（ObjectOutputStream 上）落盘；`write(ObjectOutput)` 为 @Deprecated(forRemoval) 老协议写法（仅允许写回**同一**流，别的流→IllegalArgumentException "wrong stream"）。

## 2. Java 用户如何使用
- 自定义 writeObject 精确控制字段值：`PutField pf = out.putFields(); pf.put("x", 42); out.writeFields();`（跨版本字段映射、脱敏、计算字段）。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- PutFieldImpl.put 查表与 "no such field ... with type ..."、"wrong stream" — `jdk25u-dev/src/java.base/share/classes/java/io/ObjectOutputStream.java:1566` 区域。
- 官方样板 — `jdk25u-dev/test/jdk/java/io/Serializable/`（putFields/writeFields 配对用例）。

## 4. 测试用例设计 + 自动化实现（PutFieldTest.testBlackBox，探针 P13/P16 先行）
1. 白盒：abstract、public 无参构造、恰好 9 个 put(String,T)、write(ObjectOutput) 声明 IOException 且 @Deprecated(forRemoval=true)。
2. writeObject 回调内：put 未知名→IAE 精确消息；put 签名不匹配（put("i",1.0)）→IAE 精确消息。
3. pf.write(别的流)→IAE("wrong stream")。
4. 九类型 put + writeFields → 对端 readFields 九类型全精确读回（真往返）。
- 健壮性: 单对象流往返；@SuppressWarnings("removal") 只包老 API 调用点。

产出：`PutFieldTest.java`（白盒 + 黑盒共 19 断言）。
