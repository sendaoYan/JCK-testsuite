# javax.imageio.plugins.tiff.TIFFImageReadParam — DESIGN

## 1. API 功能
`TIFFImageReadParam` 是 `ImageReadParam` 的子类，为内置 TIFF 读码器提供标签集与未知标签读取控制。方法分组：
- 构造器：`TIFFImageReadParam()`（默认允许 Baseline/Fax/ExifParent/GeoTIFF 四个标签集）。
- 标签集：`addAllowedTagSet(TIFFTagSet)`、`removeAllowedTagSet(TIFFTagSet)`、`getAllowedTagSets()`。
- 未知标签：`setReadUnknownTags(boolean)`、`getReadUnknownTags()`。

## 2. Java 用户如何使用
`new TIFFImageReadParam()` 后，`addAllowedTagSet(myTagSet)` 注册额外标签集，`setReadUnknownTags(true)` 强制读未识别标签，再把参数传给 `ImageReader.read(0, param)`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/plugins/tiff/TIFFImageReadParam.java`：构造器依次 `addAllowedTagSet(Baseline/Fax/ExifParent/GeoTIFF)`；`getAllowedTagSets()` 返回内部 `List`；重复 `addAllowedTagSet` 无副作用。
- `src/java.desktop/share/classes/com/sun/imageio/plugins/tiff/TIFFImageReader.java`：读图时依据 allowedTagSets 解析标签、依据 readUnknownTags 决定是否读未知标签。
- JDK 自带测试 `test/jdk/javax/imageio/plugins/tiff/` 有 read param 相关测试。

## 4. 测试用例设计 + 自动化实现（`TIFFImageReadParamTest.testBlackBox`）
- 默认 4 个 allowed 标签集、`readUnknownTags==false`。
- `setReadUnknownTags` 往返。
- `addAllowedTagSet`：重复为 no-op、新标签集增加；`removeAllowedTagSet` 移除（含默认标签集）。
- null 入参抛 `IllegalArgumentException`。

## 5. 深化补充（第二批，2026-11-05，断言 26 → 61）
- 实测发现（javadoc 未写明）：`getAllowedTagSets()` 返回**活内部 List**（同一实例），直接 add/remove 立即生效——不是防御性拷贝；`addAllowedTagSet(null)`/`removeAllowedTagSet(null)` 抛 `IllegalArgumentException` "tagSet == null!"；移除从未加过的集合是 no-op；可移除全部默认集合再逐一加回；重复 add 幂等（计数不变）。
- 继承链契约（ImageReadParam/IIOParam）：setDestinationOffset、setSourceSubsampling（含负参 IAE 实测）、setSourceBands（负 band IAE）、setSourceRegion 往返与 null 清除、canSetSourceRenderSize false。
- 驱动真实 reader：TIFF 魔数流（II*\0 与 MM\0* 两种字节序）能发现 TIFF reader（format 名 "tif"）；reader 默认 param 即 TIFFImageReadParam（4 个默认 tag set）；手写 param 读取像素相等；清空 allowed tag sets 后基本解码仍可用（实测）。
