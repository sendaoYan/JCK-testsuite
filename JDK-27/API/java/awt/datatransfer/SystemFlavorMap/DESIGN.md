# java.awt.datatransfer.SystemFlavorMap 测试设计

## 1. API 功能
系统 flavor 映射表（FlavorMap+FlavorTable 单例），含 Java MIME 编解码工具。

## 2. Java 用户如何使用
getDefaultFlavorMap；getNativesForFlavor/getFlavorsForNative/getNativesForFlavors；encodeJavaMIMEType/decodeJavaMIMEType/isJavaMIMEType/encodeDataFlavor/decodeDataFlavor。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/datatransfer/SystemFlavorMap.java

## 4. 测试用例设计 + 自动化实现
白盒: final、FlavorMap+FlavorTable；黑盒: 默认映射非空、flavor/native 互查、Java MIME 编解码 roundtrip。

自动化实现位于同目录 `SystemFlavorMapTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. 契约深化（本轮追加）
- 单例身份：getDefaultFlavorMap() 两次调用同一实例；无 public 构造器（javap 双侧确认，非"仅 JDK-27 存在"——两侧 API 一致）。
- FlavorTable：stringFlavor → 非空 native 列表；STRING → 非空 flavor 列表；未知任意 native → 空（非 null）列表；JAVA_DATAFLAVOR 编码 native → 解码出完整文本 flavor 列表；imageFlavor → 非空平台 native。
- FlavorMap：getNativesForFlavors/getFlavorsForNatives(null) → 全量映射（含 stringFlavor/STRING 键）；空数组 → 空映射。
- JAVA_DATAFLAVOR 编解码：encodeJavaMIMEType 前缀校验（任意 JAVA_DATAFLAVOR: 前缀字符串均识别）；decode 非 encoded → null；encodeDataFlavor/decodeDataFlavor round-trip；decodeDataFlavor 非 encoded → null 不抛异常。
- 映射可变性与恢复：addUnencodedNativeForFlavor/setNativesForFlavor（恢复原映射）、addFlavorForUnencodedNative/setFlavorsForNative(空数组清空)；STRING 映射不受污染。
- null 参数：四个 mutation 方法对 null flavor/native 均 NPE。
