# java.util.prefs.AbstractPreferences 测试方案

白盒结构断言在 `AbstractPreferencesTest.testWhiteBox()`，黑盒行为断言在 `AbstractPreferencesTest.testBlackBox()` 系列方法。本文件聚焦黑盒方案，自动化实现见 `AbstractPreferencesTest.java` 黑盒部分。

## 1. API 功能（按方法分组）

- **抽象骨架（供子类实现）**：`protected AbstractPreferences(AbstractPreferences parent, String name)`；九个 `protected abstract` SPI：`putSpi`、`getSpi`、`removeSpi`、`removeNodeSpi`、`keysSpi`、`childrenNamesSpi`、`childSpi`、`syncSpi`、`flushSpi`；`protected AbstractPreferences getChild(String)`；`protected boolean isRemoved()`。
- **键值操作**：`put`、`get`、`remove`、`clear`；类型化便捷方法 `putInt/getInt`、`putLong/getLong`、`putBoolean/getBoolean`、`putFloat/getFloat`、`putDouble/getDouble`、`putByteArray/getByteArray`。
- **节点树**：`node(String)`、`nodeExists(String)`、`parent()`、`name()`、`absolutePath()`、`removeNode()`、`childrenNames()`。
- **查询/工具**：`keys()`、`isUserNode()`、`toString()`、`flush()`、`sync()`。
- **事件**：`addPreferenceChangeListener`/`removePreferenceChangeListener`、`addNodeChangeListener`/`removeNodeChangeListener`。
- **导出**：`exportNode(OutputStream)`、`exportSubtree(OutputStream)`。

## 2. Java 用户如何使用

- 用户一般不直接继承它，而是由实现方（如 `FileSystemPreferences`）继承并填充九个 SPI。
- 典型组合链路：`put → get → containsKey(get 返回默认值判断) → remove → keys().length`；`node("child/grand") → absolutePath()/parent() → removeNode() → nodeExists()`。
- 便捷类型方法内部走 `put/get`：`putInt(key, 42)` 等价 `put(key, Integer.toString(42))`；`getInt` 无法解析时回退默认值。
- 监听：注册 `PreferenceChangeListener` 后，`put`/`remove` 异步投递事件；`NodeChangeListener` 在 `node`（新节点）/`removeNode` 时投递 `childAdded`/`childRemoved`。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`，逐条标注来源，未凭记忆编造。

- `test/jdk/java/util/prefs/RemoveNullKeyCheck.java:59-80`：匿名 `new AbstractPreferences(null, "")` 并覆盖 `putSpi/getSpi/removeSpi/removeNodeSpi/keysSpi/childrenNamesSpi` 等 SPI——本测试的 in-memory 子类即沿用此形态。
- `src/java.desktop/share/classes/com/sun/media/sound/SoftSynthesizer.java:904-905`：`Preferences.userRoot()` 后 `nodeExists(notePath)` + `node(notePath)` 组合查询/建节点。
- `test/jdk/java/util/prefs/ExportNode.java:45-52`：`userRoot().node(...)` 建链、`put`、`exportNode(ByteArrayOutputStream)` 导出。
- `test/jdk/java/util/prefs/AddNodeChangeListener.java:50,127-132`：`addNodeChangeListener` 注册匿名监听，`childAdded(NodeChangeEvent)`/`childRemoved(NodeChangeEvent)` 回调。

## 4. 测试用例设计 + 自动化实现

- **白盒**：`Class.forName` 断言 public abstract、`extends Preferences`、`protected (AbstractPreferences,String)` 构造器、九个 `protected abstract` SPI 方法、代表性 public 方法存在。
- **构造/身份**：根节点 `absolutePath()=="/"`、`name()==""`、`parent()==null`；用户/系统 `isUserNode()` 与 `toString()` 前缀；非空根名/含斜杠名/空非根名抛 `IllegalArgumentException`。
- **标量往返**：`putInt/getInt`、`putLong/getLong`、`putBoolean/getBoolean`、`putFloat/getFloat`、`putDouble/getDouble`、`putByteArray/getByteArray` 及默认值；无法解析时 `getInt/getBoolean` 回退默认值。
- **keys/clear/remove**：TreeMap 保证排序，`keys()` 精确数组断言；`remove` 后键消失；`clear` 清空。
- **节点树**：`node("")==this`、`node("/")==root`、同名缓存同一实例、嵌套 `absolutePath`、`childrenNames`、`removeNode` 后 `nodeExists==false` 且已删节点 `put` 抛 `IllegalStateException`。
- **异常**：null key/value/get/remove、超长 key(>80)/value(>8192)/节点名(>80)、连续斜杠、路径以斜杠结尾、`nodeExists` 连续斜杠。
- **事件**：注册监听后 `put`/`remove` 投递 `PreferenceChangeEvent`（含 `newValue==null` 的删除事件）、`node`/`removeNode` 投递 `NodeChangeEvent`；用 `CountDownLatch` 等待异步投递；移除监听后不再接收。
- **健壮性**：in-memory TreeMap（不写真实 prefs）、固定输入、无墙钟/无 Random、无 HashMap 迭代顺序断言、异步事件用 latch 有界等待、无 `System.setProperty`/线程泄漏/`System.exit`。

自动化实现：`testWhiteBox()` + `testBlackBox()`（`testConstructorAndIdentity`/`testPutGetScalars`/`testKeysClearRemove`/`testNodeTree`/`testExceptions`/`testEventListeners`），共 100 断言。
