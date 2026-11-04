# javax.imageio.plugins.jpeg.JPEGQTable — DESIGN

## 1. API 功能
JPEG 量化表。公共 API：`JPEGQTable(int[] qTable)`、`getQuantizationTable()`（恰 64 个 int，zigzag 序）、`getScaledInstance(float scale, boolean forceBaseline)`、`static K1Luminance/K1LuminanceDiv2/K2Chrominance/K2ChrominanceDiv2`（附录 K 标准表）、`toString()`。
**未覆写 `equals/hashCode`** → 同一性语义（实测两侧一致，规范未写）。

## 2. Java 用户如何使用
质量参数典型写法：`JPEGQTable.K1Luminance.getScaledInstance(quality, true)`（quality 低 → 数值小 → 压得狠），经 `JPEGImageWriteParam.setEncodeTables(qt, dc, ac)` 注入；读 abbreviated 流经 `JPEGImageReadParam.setDecodeTables(qt, ...)` 注入。

## 3. 开源代码实际用法调研（来源：OpenJDK 源码路径）
- `~/git/jdk8u-dev/jdk/src/share/classes/javax/imageio/plugins/jpeg/JPEGQTable.java`：构造器只查 `(qTable == null || qTable.length != 64)` → IllegalArgumentException，**数值不做范围校验**（0/1000/-5 原样入表）；`getScaledInstance` 公式 `(int)((float)val*scale + 0.5f)` 再 clamp 到 `[1,32767]`（forceBaseline 则 `[1,255]`）。
- 标准表常量按附录 K 的 zigzag 序静态初始化（`K1LuminanceDiv2 == (K1+1)/2` 的关系在源码中逐项写死）。
- `test/tck/JPEG/`：OpenJDK 用 `JPEGQTable.K1Luminance` 组装 abbreviated 流测试。

## 4. 测试用例设计 + 易错规范细节
- 反射断言公共方法集恰 3 个、equals/hashCode 来自 Object、公共常量 4 个。
- 构造矩阵（探针实测）：null → IAE；长度 63/65 → IAE；恰 64 合法；**0、1000、-5 原样存储**（构造不管数值范围）；拒绝后可成功重建。
- 防御性拷贝：构造后改入参数组不影响内部；getter 每次返回新数组、改返回数组不影响后续。
- getScaledInstance 取整（易错点）：`(int)(v*scale+0.5f)` 四舍五入语义——3×0.5=1.5→2、5×0.5=2.5→3（probe 实测两侧一致）；scale 0 → 全 1（下限 clamp）；负 scale → 全 1；NaN scale → 全 1（`(int)(NaN+0.5f)==0`→clamp 1）；+Inf → 32767 / forceBaseline 255（上限 clamp）；forceBaseline=true 把 >255 压到 255。
- 标准表锚点（两侧实测一致）：`K1Luminance.getQuantizationTable()[0]==16`、`K2Chrominance[0]==17`、`K1LuminanceDiv2[0]==8`（== K1 逐项 `(K1+1)/2` 全表断言）、`K2ChrominanceDiv2 == (K2+1)/2`；K1Luminance 最大值 255 不需 forceBaseline。
- getScaledInstance **不改 this**：调用后原表逐项不变，返回新表。
- equals/hashCode：内容全等拷贝 equals false、与自身 true——身份语义断言。
- toString：非 null、含类名。
- 端到端：`K1Luminance.getScaledInstance(0.5f,false)` 经 `JPEGImageWriteParam.setEncodeTables` 接受（写侧注入成功即契约闭环）。
