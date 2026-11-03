# java.awt.font.OpenType 设计案

## 1. API 功能分组
- **常量**：OpenType 表 tag（`TAG_CMAP/TAG_HEAD/TAG_NAME/TAG_GLYF/TAG_MAXP/...`，值为 4 字节签名）。
- **接口方法**：`getVersion()`、`getFontTable(int tag)`/`getFontTable(String tag)`、`getFontTable(int|String tag, int offset, int length)`、`getFontTableSize(int|String tag)`。

## 2. Java 用户如何使用
OpenType 字体对象实现该接口，用户按表 tag 读取字体表字节：`byte[] cmap = font.getFontTable(OpenType.TAG_CMAP);` 解析字形映射；`getFontTableSize` 预知表长。JDK 的 `Font` 通过 `sun.font` 内部实现提供。

## 3. 开源代码实际用法调研
- `java.awt.font.OpenType` 接口被 `sun.font` 的字体实现（如 `TrueTypeFont`）实现，按 tag 读取 OpenType 表（`src/java.desktop/share/classes/sun/font/`）。
- `test/jdk/java/awt/font/OpenType.java` 用例读取内置字体的若干 tag 表验证可读性（本用例匿名实现覆盖方法契约）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`OpenTypeTest.testBlackBox()`，匿名实现）：
1. 常量 tag 不同（代表不同表）。
2. 6 个 getFontTable/getFontTableSize 重载按 int/String tag 返回正确表数据。
白盒（`testWhiteBox()`）：interface、若干 TAG_* 常量、方法签名。
