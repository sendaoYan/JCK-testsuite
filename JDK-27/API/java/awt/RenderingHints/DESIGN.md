# java.awt.RenderingHints 黑盒行为测试设计

## 1. API 功能
- 常量：`KEY_*`（`RenderingHints.Key`）与 `VALUE_*` 键值对（抗锯齿/渲染/抖动/文本抗锯齿/LCD 对比/分数度量/插值/alpha 插值/色彩渲染/描边控制/分辨率变体）。
- 构造：`(Map<Key,?>)`、`(Key,Object)`。
- Map 视图：`size/isEmpty/containsKey/containsValue/get/put/add/clear/remove/putAll/keySet/values/entrySet`。
- 复制：`clone`；值语义：`equals/hashCode/toString`。

## 2. Java 用户如何使用
`Graphics2D.setRenderingHint(s)` 传递渲染质量偏好；`RenderingHints` 是这些键值的可克隆集合。
```java
RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
g2.setRenderingHints(hints);
```
组合链路：构造 → `put` → `get` → `containsKey` → `keySet`（排序）→ `remove`/`clear` → `clone`/`equals`。

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 `src/java.desktop/share/classes/java/awt/RenderingHints.java`：`put` 先 `((Key)key).isCompatibleValue(value)` 校验（不兼容抛 `IllegalArgumentException`，null key 抛 NPE，非 Key 抛 `ClassCastException`）；`(Key,Object)` 构造器直接用 `HashMap.put` 不走校验；`clone` 深拷贝 `hintmap`；`equals` 比较 `hintmap`。
- JDK 自带测试（真实用法）：`test/jdk/java/awt/FullScreen/AltTabCrashTest/AltTabCrashTest.java`、`test/jdk/java/awt/TrayIcon/TrayIconTest.java`（`new RenderingHints` + put）。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 常量非空。
- `(Key,Object)` 构造 + `get/size/isEmpty`。
- `put` 返回旧值、`containsKey/containsValue`。
- `put` 校验（不兼容值 IAE、null key NPE、非 Key 键 CCE）。
- `putAll`/`add` 合并。
- `keySet` 排序后断言（identityHashCode 排序 + 包含断言）、`values`/`entrySet`。
- `remove`/`clear`。
- `equals/hashCode`（等值/不等值/null/异类）。
- `clone` 独立。
- `toString`（空 `{}`、非空非空串）。
断言 61 项，集合断言排序后确定（headless-safe）。


## 5. 深化补充（testContract，JDK-8+JDK-27 双侧对齐）
本任务在保留原有断言基础上追加契约级深化：
testContract：键值唯一性；toString 精确；表语义全套；视图不可加但同步；equals/hashCode 对 Map 拷贝；clone 隔离。
全部为 headless（env -u DISPLAY）实测锁定值，双端 JDK 行为一致。
