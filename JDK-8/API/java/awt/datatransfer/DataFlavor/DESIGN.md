# java.awt.datatransfer.DataFlavor 测试设计

## 1. API 功能
数据 MIME 类型描述，含常量 stringFlavor/plainTextFlavor/javaFileListFlavor 等。

## 2. Java 用户如何使用
多个构造（mime / mime+humanName / Class+mime / mime+ClassLoader）；getMimeType/getPrimaryType/getSubType/getParameter/match/isMimeTypeEqual/isFlavorTextType/...；clone/equals/hashCode。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/datatransfer/DataFlavor.java

## 4. 测试用例设计 + 自动化实现
白盒: Externalizable+Cloneable；黑盒: 常量、构造、MIME 分解、humanName、match、clone、equals。

自动化实现位于同目录 `DataFlavorTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. 契约深化（本轮追加）
- 构造器矩阵：默认构造（占位，mime 为 null）；(String) null → NPE、""/"not a valid mime type!" → IAE("failed to parse:")；(String,String)；(String,String,ClassLoader) 解析 class 参数；(Class,null) → NPE；(Class,String) 生成 serialized-object 模板。
- MIME 解析：大小写不敏感（TEXT/PLAIN 归一化为小写 primary/sub）；class 参数决定 representationClass（Reader/CharBuffer/ByteBuffer 解析）；getParameter 缺失 → null。
- equals/hashCode/match：同 mime 双实例 equals 对称且 hashCode 相等；charset 不同不等不 match；humanPresentableName 不参与 equals/hashCode；equals(String) 仅比较 mime；equals((Object)非 flavor) false。
- 谓词矩阵：isFlavorTextType/isFlavorJavaFileListType/isFlavorRemoteObjectType/isMimeTypeSerializedObject/isFlavorSerializedObjectType 对 stringFlavor/javaFileListFlavor/imageFlavor/text/plain/x-java-serialized-object/x-java-remote-object 全组合。
- 表示类谓词：isRepresentationClass{Serializable,InputStream,Reader,CharBuffer,ByteBuffer,Remote} 与 getDefaultRepresentationClass{,AsString}（java.io.InputStream）。
- clone 独立性（改 humanName 不回漏）；序列化（Externalizable）round-trip 保 mime/humanName/charset/谓词（含 stringFlavor、javaFileListFlavor）。
- selectBestTextFlavor：null/空数组/无文本 → null；多文本候选返回文本 flavor。
- getReaderForText：UTF-8 InputStream flavor 解码正确；不支持 → UnsupportedFlavorException；非 Reader/InputStream 表示类（imageFlavor 且被支持）→ IAE。
- 常量精确值：javaSerializedObjectMimeType/javaRemoteObjectMimeType/javaJVMLocalObjectMimeType="application/x-java-jvm-local-objectref"、stringFlavor humanName="Unicode String"。
- 版本差异：JDK-8 三个 HTML flavor 常量非 final；JDK-9+（含 JDK-27）为 final（白盒 isFinal 断言双侧相反）。
