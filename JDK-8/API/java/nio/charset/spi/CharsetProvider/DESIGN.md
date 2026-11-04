# java.nio.charset.spi.CharsetProvider 测试方案

## 1. API 功能

为 `Charset.forName` / `Charset.availableCharsets` 提供自定义字符集的 SPI 抽象类。

- 类：`public abstract class CharsetProvider`，extends Object，protected 无参构造器（JDK-8 与 JDK-27
  结构完全一致，javap 双侧确认）。
- 抽象方法（仅有的两个抽象成员）：`public abstract Iterator<Charset> charsets()`、
  `public abstract Charset charsetForName(String charsetName)`。
- 基类不做任何名称规范化/别名解析：`charsetForName` 收到的查询串原样转发，别名解析是 provider 实现者自己的责任。
- 不 Serializable；实例化必须经由具体子类（protected 构造器对子类可见）。

## 2. Java 用户如何使用

- 实现类：继承 `CharsetProvider`，实现 `charsets()`/`charsetForName()`，经 `META-INF/services` 或
  JDK-9+ 模块 `provides ... with ...` 注册后参与全局查找。
- 直接实例化只得到一个普通对象，**不会**注册进全局查找表（`Charset.forName` 看不见）。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk8u-dev/jdk/src/share/classes/java/nio/charset/spi/CharsetProvider.java`）：
  仅 protected 构造器 + 两个抽象方法；标准字符集与扩展 provider 均经此 SPI。
- `Charset.forName` 遍历已安装 provider（`~/git/jdk8u-dev/.../Charset.java` 的 `lookup2/gate`）。
- 回归参考：`~/git/jdk/test/jdk/java/nio/charset/spi/`（历史上以 ServiceLoader 注册流为主；本测试为避免
  全局污染只做直接实例化，与 jdk 测试思路等价地覆盖 provider 契约本身）。

## 4. 测试用例设计 + 自动化实现 + 健壮性说明

文件：`CharsetProviderTest.java`（testCharsetProvider 保留 + testWhiteBox + testProviderDelegation +
testCustomCharsetContract + testCharsetCtorMatrix + testAliasesArrayCopySemantics）。

- 白盒：public abstract / 父类 Object / protected 无参构造器 / 恰好两个抽象方法 charsets+charsetForName /
  非 Serializable / 子类可赋值性 / 反射实例化抽象类 → InstantiationException。
- 黑盒（计数 provider）：`charsetForName` 命中返回自定义 charset、未命中 null、查询串逐字转发（lastQuery）、
  别名不做基类级解析、调用计数；`charsets()` 顺序保持 [a,b]、每次调用产出独立迭代器、耗尽语义；
  空 provider；直接实例化后 `Charset.forName("X-TEST-A")` 仍抛 UnsupportedCharsetException（不污染全局）。
- 自定义 Charset 子类契约：name/aliases（内容+不可变，add → UOE）/displayName()/displayName(Locale)
  默认=规范名/toString=规范名/canEncode 默认 true/isRegistered 的 X- 前缀规则/hashCode==name.hashCode()/
  equals 按规范名跨实例/compareTo 按名/contains 委派/newDecoder+newEncoder 委派后 encode/decode 往返
  "AB"/null aliases 容忍为空集/aliases() 记忆化同实例。
- Charset 构造器负例矩阵：null 名 → NPE；""/空格名/首 '-' → ICE；null 别名元素 → NPE；非法别名/空别名 → ICE；
  数字开头名与 `A+B:C_.D-E` 合法。
- **JDK-8 版本特有**：Charset 构造器对别名数组**不做防御性拷贝**（jdk8u-dev Charset.java 构造器直接赋值），
  构造后修改数组在别名集未物化前可见；JDK-27 侧为 Arrays.copyOf 防御性拷贝（各自断言，见对侧注释）。

健壮性：绝不经 ServiceLoader/META-INF/services 注册（避免全局状态污染，测试内断言不可见性）；
只用标准字符集做委派实现；无文件/网络/线程/墙钟/随机/Locale 依赖。
