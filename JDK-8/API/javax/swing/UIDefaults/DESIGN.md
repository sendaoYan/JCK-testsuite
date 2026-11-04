# javax.swing.UIDefaults 测试设计

## 1. API 功能
Swing 默认值表（Hashtable）：put/putDefaults、LazyValue（解析后缓存）/ActiveValue（每次重算）、
九个类型化 getter（getFont/getColor/...）。

## 2. Java 用户如何使用
LaF 的 getDefaults() 填充；程序侧 `UIManager.getDefaults().put("key", value)`。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/UIDefaults.java`；
UIManager.getDefaults()；MetalLookAndFeel.getDefaults()；JDK 自带 `test/jdk/javax/swing/` 相应模块用例。

## 4. 测试用例设计 + 易错细节
- 基本：varargs 构造器成对解析、覆盖、containsKey、remove 返回旧值/缺失 null、**put(null,v) NPE**。
- 事件：单个 put 以 key 为 propertyName 触发；putDefaults 触发一个 "UIDefaults" 事件（old/new 均 null）；
  监听器移除后静默。
- **LazyValue createValue 恰一次且结果回填**（后续 get 直接命中）；ActiveValue 每次 get 重算。
- 类型化 getter 矩阵：正确类型命中同实例；缺失→引用类型 null/getInt 0/getBoolean false；
  类型不符→同样回退（Boolean 是 false 而非 null）。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动（headless 安全，Metal LaF 先行）。

