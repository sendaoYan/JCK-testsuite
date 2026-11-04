# javax.imageio.plugins.jpeg.JPEGHuffmanTable — DESIGN

## 1. API 功能
JPEG 哈夫曼表（DC/AC 通用）。公共 API：`JPEGHuffmanTable(short[] bits, short[] values)`、`getLengths()`（16 个 BITS 段）、`getValues()`（HUFFVAL 段）、`static K1Luminance/K2Luminance/K3Chrominance/K4Chrominance`（附录 K 标准表）、`toString()`。
**未覆写 `equals/hashCode`** → 同一性语义（规范未写，实测两侧一致）。

## 2. Java 用户如何使用
标准表直接用 `JPEGHuffmanTable.K1Luminance`；自定义表 `new JPEGHuffmanTable(bits16, vals)`，再经 `JPEGImageReadParam.setDecodeTables(null, dc, ac)` / `JPEGImageWriteParam.setEncodeTables(...)` 注入编解码器。

## 3. 开源代码实际用法调研（来源：OpenJDK 源码路径）
- `~/git/jdk8u-dev/jdk/src/share/classes/javax/imageio/plugins/jpeg/JPEGHuffmanTable.java`：构造器校验 `(bits==null || values==null || bits.length>16 || values.length>256)` → `IllegalArgumentException`；内部 `clone()` 双向防御拷贝；`validate()` 私有（反射需过滤出公共方法再断言方法集）。
- `test/tck/JPEG/`：OpenJDK 回归（如 `CanReadThumbnailName.java`、`TruncatedImage.java`）只用标准表常量，不做构造边界。
- `com/sun/imageio/plugins/jpeg/JPEGImageReader.java`：表经 `JPEGImageReadParam` 注入 native 层。

## 4. 测试用例设计 + 易错规范细节
- 反射断言公共方法集恰 3 个（getLengths/getValues/toString）、equals/hashCode 来自 Object、字段 `lengths`/`values` 均为 final 且非 public。
- 构造矩阵（探针实测）：null bits / null values / bits 17 长 / values 257 长 → IllegalArgumentException；边界 16/256 合法；values.length==0 合法（空 HUFFVAL）；bits 越界元素（0 与 17）被**原样接受**（构造器只查长度，与 QTable 同一"构造不管数值"哲学）。
- 防御性拷贝（易错点）：构造后改入参数组不影响 `getLengths()/getValues()`；getter 每次返回新数组，改返回数组不影响后续调用。
- 标准表锚点（两侧实测一致）：K1Luminance.getLengths()[0]==0、sum(K1 bits)==162、K1Luminance.getValues().length==162、K1Luminance.getValues()[0]==0；K2 同长 162；K3/K4（AC 表）getValues().length==162；标准表常量每次访问同一实例。
- equals/hashCode：内容全等拷贝 `==` false、`equals` false；与自身 true；与 null/异类 false——身份语义写进断言。
- toString：非 null、含类名、两次调用内容相等。
- 端到端：自定义表经 `JPEGImageWriteParam.setEncodeTables` 接受并 `areTablesSet()==true`（与读侧参数串联验证表生命周期）。
