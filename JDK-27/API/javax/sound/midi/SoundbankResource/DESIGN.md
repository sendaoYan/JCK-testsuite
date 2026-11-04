# javax.sound.midi.SoundbankResource — DESIGN

## 1. API 功能
`SoundbankResource` 是音色库资源的抽象基类（`Instrument` 继承它）。方法分组：
- 构造：`protected SoundbankResource(Soundbank, String name, Class<?> dataClass)`。
- 查询：`getSoundbank()`、`getName()`、`getDataClass()`。
- 抽象：`getData()`（子类实现返回资源的字节数据）。

## 2. Java 用户如何使用
用户通常通过 `Soundbank.getResources()` 得到 `SoundbankResource[]`，再 `getData()` 取字节；`getDataClass()` 告知数据类型（如 `byte[].class`）。
子类测试：`class R extends SoundbankResource { R(Soundbank sb,String n,Class<?> c){ super(sb,n,c);} public Object getData(){ return payload; } }`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/SoundbankResource.java`：`soundBank/name/dataClass` 为 `private final`，`getSoundbank()/getName()/getDataClass()` 直接返回构造参数；`getData()` 抽象。
- `src/java.desktop/share/classes/javax/sound/midi/Instrument.java`：`Instrument` 继承本类并保存 `Patch`，是 `SoundbankResource` 的典型子类。

## 4. 测试用例设计 + 自动化实现（`SoundbankResourceTest.testBlackBox`）
- 子类 `Res` 构造，断言 `getSoundbank()` 返回同一 Soundbank、`getName()` 往返、`getDataClass()` 往返、`getData()` 返回构造传入的载荷对象。
- 白盒补齐：非接口抽象类、extends Object、`getSoundbank/getName/getDataClass/getData` 四方法齐全。
