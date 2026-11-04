# javax.sound.sampled.Mixer.Info — DESIGN

## 1. API 功能
`Mixer.Info` 描述混音器的名称、厂商、描述、版本。构造器为 `protected`（仅子类可实例化）。方法：
- 查询：`getName()`、`getVendor()`、`getDescription()`、`getVersion()`。
- 值语义：`equals(Object)`、`hashCode()`、`toString()`（final）。

## 2. Java 用户如何使用
用户通常通过 `AudioSystem.getMixerInfo()` 获得实例，然后 `info.getName()` 等展示；`Mixer.Info` 构造器受保护，自定义 provider 时通过子类调用 `super(name, vendor, desc, version)`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/sampled/Mixer.java:58-70`：protected 构造器赋值四字段。
- `test/jdk/javax/sound/sampled/Mixers/BogusMixers.java`：构造自定义 `Mixer.Info` 子类用于匹配测试。

## 4. 测试用例设计 + 自动化实现（`InfoTest.testBlackBox`）
- 白盒：公开类、继承 Object、protected 构造器、final 的 equals/hashCode。
- 黑盒：通过子类实例化，断言四字段往返；`equals` 自反/对称/不同字段/与 null 及异类型对象；`hashCode` 稳定且与 equals 一致；`toString` 非空。
