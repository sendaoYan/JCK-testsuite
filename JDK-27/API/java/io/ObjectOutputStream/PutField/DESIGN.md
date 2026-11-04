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

## 深化追加（契约级）— PutField 写端字段契约（批次 ioI）

### 覆盖维度（P1–P11）
1. **P1 零值缓冲**：putFields() 返回零初始化缓冲（**不**从实例取初值）——只 put("a",55) 时其余 int→0、boolean→false、Object→null 全部写出。
2. **P2 null 合法值**：put("o",null) 写出 null；读端 `get("o","DEF")` 得 null 而非默认——**显式 null 击败 get 默认值**（字段存在 ≠ 缺失）。
3. **P3 名称拒绝**：静态字段名 `put("STAT",1)`、大小写不匹配 `put("A",1)` → IAE `no such field STAT with type int` / `no such field A with type int`（大小写敏感）。
4. **P4 九类型极值**：boolean true、Byte.MIN_VALUE、Character.MAX_VALUE、Short.MIN_VALUE、Integer.MIN_VALUE、Long.MIN_VALUE、Float.NaN、double -0.0（位级）、对象值，全部经 readFields 精确读回。
5. **P5 七条错误类型消息表（实测）**：`no such field i with type long`（int 字段配 long）、`no such field z with type int`（boolean 配 int）、`no such field b with type int`（byte 配 int）、`no such field i with type class java.lang.Object`（int 配 String → 解析到 Object 重载）、`no such field null with type int`（null 名）、`no such field i with type float`、`no such field b with type boolean`。要点：重载决议先于字段查找——String/float/boolean 值会分别落到 Object/float/boolean 重载。
6. **P6 writeFields 后接自定义数据**：writeFields()+writeInt(99) ↔ 读端 readFields()+readInt() 配对，且流的每个字节都被消费（read()==-1）。
7. **P7 putFields() 同一性**：两次 putFields() 返回**同一缓冲**（f1==f2）；经两个引用先后 put 同名字段，后者胜。
8. **P8 双重 writeFields()**：写端不抛（写出成功），流不可读 → 读端 StreamCorruptedException `invalid type code: 00`。
9. **P9 deprecated write(owner)**：仅对象字段的类经 `pf.write(out)` 往返成功（null 照读 null，字节全消费）；含原始字段则流损坏 → SCE `invalid type code: 00`（该方法废弃的原因）。
10. **P10 对象字段承载引用类型**：enum 常量读回 ==（同一常量）；Class 对象保持身份（String.class ==）。
11. **P11 共享引用**：同一对象 put() 进两个字段 → 读回 back.x==back.y（TC_REFERENCE），内容保持。

### 版本差异（8 vs 27）
- 全部行为与消息逐字一致；27 侧新 fixture 以 `@Serial` 标注 serialVersionUID，deprecated `write(ObjectOutput)` 在 27 编译产生 removal 警告（fixture 以 @SuppressWarnings("removal") 压制，不影响断言）。

产出：`PutFieldTest.java` 19 → 66 断言（含白盒结构断言与 @Serial 标注；jtreg 通过）。
