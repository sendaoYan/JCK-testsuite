# javax.imageio.plugins.tiff.ExifGPSTagSet — DESIGN

## 1. API 功能
`ExifGPSTagSet` 是 Exif GPS 信息 IFD 的标签集合（`final`，单例）。方法分组：
- 工厂：`getInstance()`。
- 继承查询：`getTag(int)`、`getTag(String)`、`getTagNames()`、`getTagNumbers()`。
- 常量：`TAG_GPS_VERSION_ID`、`TAG_GPS_LATITUDE_REF` 等标签号与 `LATITUDE_REF_NORTH` 等值常量。

## 2. Java 用户如何使用
`ExifGPSTagSet.getInstance().getTag("GPSLatitudeRef")` 拿标签，或用 `getTag(1)` 按号查；`getTagNames()` 遍历全部 GPS 标签。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/plugins/tiff/ExifGPSTagSet.java`：`TAG_GPS_VERSION_ID=0`、`TAG_GPS_LATITUDE_REF=1`、`TAG_GPS_LATITUDE=2`、`TAG_GPS_LONGITUDE_REF=3`、`TAG_GPS_LONGITUDE=4`、`TAG_GPS_ALTITUDE=6`；`GPSLatitudeRef` 带 `N`/`S` mnemonic 值名。
- `src/java.desktop/share/classes/com/sun/imageio/plugins/tiff/TIFFIFD.java`：用 tagSet 集合把 raw 标签号映射到 `TIFFTag`。
- JDK 自带测试 `test/jdk/javax/imageio/plugins/tiff/` 有 Exif GPS 元数据读写测试。

## 4. 测试用例设计 + 自动化实现（`ExifGPSTagSetTest.testBlackBox`）
- `getInstance()` 非空且单例。
- `getTagNames()/getTagNumbers()` 非空且一一对应、每个名称可解析。
- 精确比对关键标签号（0/1/2/3/4/6）。
- `GPSLatitudeRef` 的 mnemonic 值名（`N`/`S`）。

## 5. 深化补充（第二批，2026-11-05，断言 21 → 202）
- 全部 31 个 TAG_ 编号常量精确值表（0…30），每个断言集合内可解析 + 号自洽 + 名字非空；集合规模精确 31，号域连续 [0,30]。
- 全部字符串常量精确值（GPS_VERSION_2_2="2200"、N/S/E/W、状态 A/V、量测模式 2/3、速度 K/M/N、方向 T/M、目标距离 K/M/N）+ 数值常量（ALTITUDE_REF 0/1、DIFFERENTIAL 0/1）。
- 类型契约实测：GPSVersionID BYTE（count=-1）、GPSLatitudeRef/MapDatum ASCII、GPSLatitude/GPSAltitude/GPSTimeStamp RATIONAL（GPSAltitude 拒 LONG）。
- 值名实测：GPSAltitudeRef 0="Sea level"、1="Sea level reference (negative value)"、namedValues=[0,1]；普通 GPS tag 无值名。
- 边界：tag 31/-1/未知名 miss 返回 null；GPSDestBearingRef 号 23。
