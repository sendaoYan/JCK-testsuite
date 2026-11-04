# java.io.ObjectOutputStream.PutField 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 抽象类（ObjectOutputStream.putFields() 返回实例）：九个 `put(String, T)` 重载（名字+签名校验，不匹配→IllegalArgumentException "no such field X with type Y"）。
- `writeFields()`（ObjectOutputStream 上）落盘；`write(ObjectOutput)` 为老协议写法（仅允许写回**同一**流，别的流→IllegalArgumentException "wrong stream"）。JDK 8 中 `write(ObjectOutput)` 仅标 `@Deprecated`（`forRemoval` 为 JDK 9+）。

## 2. Java 用户如何使用
- 自定义 writeObject 精确控制字段值：`PutField pf = out.putFields(); pf.put("x", 42); out.writeFields();`（跨版本字段映射、脱敏、计算字段）。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- PutFieldImpl.put 查表与 "no such field ... with type ..."、"wrong stream" — `jdk8u-dev/jdk/src/share/classes/java/io/ObjectOutputStream.java:1566` 区域。
- 官方样板 — `jdk8u-dev/jdk/test/java/io/Serializable/`（putFields/writeFields 配对用例）。

## 4. 测试用例设计 + 自动化实现（PutFieldTest.java，纯黑盒）
黑盒场景（writeObject 回调内驱动）:
1. put 未知名→IAE 精确消息 "no such field nope with type int"。
2. put 签名不匹配（put("i",1.0)）→IAE 精确消息 "no such field i with type double"。
3. pf.write(别的流)→IAE("wrong stream")。
4. 九类型 put + writeFields → 对端 readFields 九类型全精确读回（真往返）。
- 健壮性: 单对象流往返；JDK-8 为黑盒（无反射白盒，无 @Deprecated(forRemoval) 断言）。
