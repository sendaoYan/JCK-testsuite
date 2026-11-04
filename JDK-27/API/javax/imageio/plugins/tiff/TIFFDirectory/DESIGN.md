# javax.imageio.plugins.tiff.TIFFDirectory — DESIGN

## 1. API 功能
`TIFFDirectory` 表示一个 TIFF IFD，是 `TIFFField` 的集合。方法分组：
- 工厂/构造：`createFromMetadata(IIOMetadata)`、`TIFFDirectory(TIFFTagSet[], TIFFTag)`。
- 标签集：`getTagSets()`、`addTagSet`、`removeTagSet`、`getParentTag()`、`getTag(int)`。
- 字段管理：`addTIFFField`、`getTIFFField`、`getTIFFFields`、`containsTIFFField`、`removeTIFFField`、`removeTIFFFields`、`getNumTIFFFields`。
- 转换/克隆：`getAsMetadata()`、`clone()`。

## 2. Java 用户如何使用
写 TIFF 元数据时：`TIFFDirectory dir = new TIFFDirectory(new TIFFTagSet[]{ BaselineTIFFTagSet.getInstance() }, null)`，然后 `dir.addTIFFField(new TIFFField(...))`，最后 `dir.getAsMetadata()` 交给 `ImageWriter`。读时用 `TIFFDirectory.createFromMetadata(reader.getImageMetadata(0))` 再 `getTIFFField(256)` 取字段。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/plugins/tiff/TIFFDirectory.java`：低标签号字段存数组、高标签号存 `TreeMap`；`getTIFFFields()` 按标签号递增返回；`getAsMetadata()` 用 `TIFFIFD.getDirectoryAsIFD(this)` 构造 `TIFFImageMetadata`。
- `src/java.desktop/share/classes/com/sun/imageio/plugins/tiff/TIFFImageWriter.java` / `TIFFImageReader.java`：读写时在 `TIFFDirectory` 与 native 元数据间转换。
- JDK 自带测试 `test/jdk/javax/imageio/plugins/tiff/` 有 TIFFDirectory 元数据往返测试。

## 4. 测试用例设计 + 自动化实现（`TIFFDirectoryTest.testBlackBox`）
- 构造、`getParentTag()==null`、`getTagSets()` 初始含 Baseline、`getTag(256)` 解析。
- `addTagSet/removeTagSet` 往返。
- 字段增删：`addTIFFField/getTIFFField/containsTIFFField/getTIFFFields`（按号递增）、`removeTIFFField/removeTIFFFields`。
- 元数据往返：`getAsMetadata()` → `createFromMetadata()` 后 `ImageWidth` 值保留。
- `clone()` 独立（改 clone 不影响原目录）。

## 5. 深化补充（第二批，2026-11-05，断言 31 → 70）
- 异常矩阵实测（精确类）：`TIFFDirectory(null,null)`→NPE "tagSets == null!"；addTagSet/removeTagSet/addTIFFField null→NPE；重复 addTagSet/移除不存在的 tagSet/field 均为 no-op；可移除至 0 个 tag set。
- 实测发现：`addTIFFField` 对同 tag number 是替换语义（数量不变、值更新）；`getTIFFFields()`/`getTagSets()` 均为防御性拷贝（改返回数组不影响内部）；`getAsMetadata()` 每次调用新实例（TIFFImageMetadata），format 名 `{javax_imageio_tiff_image_1.0, javax_imageio_1.0}`，native 树根→TIFFIFD，未知 format→IAE。
- `createFromMetadata(null)`→NPE；不支持 TIFF format 的 IIOMetadata→IAE "Parameter does not support required metadata format!"；空目录 metadata 往返 num=0。
- clone 独立性扩展到 tag set 与 removeTIFFFields；字段顺序恒按 tag number 升序（与插入顺序无关）。
- 端到端：TIFFDirectory+自定义 ASCII tag（DocumentName）经真实 TIFF writer/reader 往返，tag 与像素均保持。
