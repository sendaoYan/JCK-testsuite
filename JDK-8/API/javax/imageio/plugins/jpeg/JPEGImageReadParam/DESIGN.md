# javax.imageio.plugins.jpeg.JPEGImageReadParam — DESIGN

## 1. API 功能
`JPEGImageReadParam extends ImageReadParam`：JPEG 解码参数，核心是**缩略表注入**（给不含量化/哈夫曼表的 abbreviated JPEG 用）：
`areTablesSet()`、`setDecodeTables(JPEGQTable[], JPEGHuffmanTable[] dc, JPEGHuffmanTable[] ac)`、`unsetDecodeTables()`、`getQTables()/getDCHuffmanTables()/getACHuffmanTables()`。无 `setDestinationTables` 之类额外 API（javap 双侧 diff 确认方法集一致）。

## 2. Java 用户如何使用
读 abbreviated 流前 `param.setDecodeTables(qTables, dcTables, acTables)`（数组长度 ≤4、dc/ac 等长），再 `reader.read(0, param)`；`unsetDecodeTables()` 恢复默认（全部 null、`areTablesSet()==false`）。

## 3. 开源代码实际用法调研（来源：OpenJDK 源码路径）
- `~/git/jdk8u-dev/jdk/src/share/classes/javax/imageio/plugins/jpeg/JPEGImageReadParam.java`：`setDecodeTables` 的校验为 `(null) || (length > 4) || (dc.length != ac.length)` → 一律 `IllegalArgumentException("Invalid JPEG table arrays")`；内部对三个数组 `clone()`。
- `test/jdk/javax/imageio/plugins/jpeg/`：OpenJDK 用标准表（`JPEGQTable.K1Luminance` 等）构造读参数，思路即本测试的表往返。
- `~/git/jdk8u-dev/jdk/src/share/classes/com/sun/imageio/plugins/jpeg/JPEGImageReader.java`：`getDefaultReadParam()` 返回 `JPEGImageReadParam` 实例。

## 4. 测试用例设计 + 易错规范细节
- 反射断言声明方法集恰为 6 个、单公共构造器、无新公共字段。
- 默认状态：`areTablesSet==false`、三个 getter 全 null；fresh unset 是安全 no-op 且幂等。
- 校验矩阵（全部实测 IllegalArgumentException）：任一数组 null；q>4；dc>4/ac>4；dc/ac 长度不等（含 2 vs 1、2 vs 0）；拒绝后状态保持 false。
- 往返与防御性拷贝（易错点）：4/2/2 标准表 round-trip；**构造入参数组之后被改不影响内部**（clone 语义）；getter 每次返回新数组、改返回数组不影响后续。
- 零长度数组合法：`(q[0], dc[0], ac[0])` 接受、`areTablesSet==true`、长度 0（规范未写，实测两侧一致）。
- 状态不变量：valid set → 非法 set 抛 IAE 后旧表完整保留；unset 后可再 set。
- 与 IIOParam 既有状态共存：sourceRegion/subsampling 往返且互不污染（`setSourceSubsampling(0,...)`/`(-1,...)` → IAE 实测）。
- 端到端：jpeg writer 写 16x12 → `reader.getDefaultReadParam() instanceof JPEGImageReadParam` → 带表解码尺寸往返；同一 param 对象可复用于多次读。
