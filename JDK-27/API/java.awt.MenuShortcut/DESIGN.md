# java.awt.MenuShortcut 黑盒行为测试设计

## 1. API 功能
- 构造：`MenuShortcut(int key)`、`MenuShortcut(int key, boolean useShiftModifier)`。
- 查询：`getKey()`、`usesShiftModifier()`。
- 值对象：`equals(MenuShortcut)`/`equals(Object)`/`hashCode()`/`toString()`。

## 2. Java 用户如何使用
AWT `MenuItem` 的键盘快捷键（非 Swing KeyStroke 路径）：
```java
MenuItem mi = new MenuItem("New", new MenuShortcut(KeyEvent.VK_N));
boolean shift = shortcut.usesShiftModifier();
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/MenuShortcut.java`：`hashCode = usesShift ? ~key : key`；`equals` 比较 key 与 shift 标志；`toString` 用 `KeyEvent.getKeyText(key)`（headless 下不带修饰键前缀）。
- `src/java.desktop/share/classes/java/awt/MenuItem.java`、`MenuBar.java` 使用 MenuShortcut 注册菜单快捷键。
- `test/jdk/java/awt/Menu/`、`test/jdk/java/awt/Frame/` 用 MenuShortcut 搭菜单。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 两构造器 getKey/usesShiftModifier 回读。
- hashCode：无 shift == key、有 shift == ~key。
- equals 相等/键不同/shift 不同/null/异类/自反。
- `toString` 以 `"A"` 结尾（跨平台修饰前缀不同，只断结尾）。
headless-safe（toString 不依赖屏幕）。
