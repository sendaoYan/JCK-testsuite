# javax.imageio.metadata.IIOMetadataFormatImpl — DESIGN（JDK-8）

## 1. API 功能
`IIOMetadataFormatImpl` 是 `IIOMetadataFormat` 的可复用抽象实现，插件作者用受保护 builder 声明格式。两个 public 构造器：
- `IIOMetadataFormatImpl(String rootName, int childPolicy)`：null root 抛 IAE（`"rootName == null!"`）；childPolicy 必须在 0..4（REPEAT=5 不允许，越界抛 `"Invalid value for childPolicy!"`）。
- `IIOMetadataFormatImpl(String rootName, int minChildren, int maxChildren)`：固定 CHILD_POLICY_REPEAT；负 min 抛 `"minChildren < 0!"`、min>max 抛 `"minChildren > maxChildren!"`。
受保护 builder：`setResourceBaseName/getResourceBaseName`（默认 `子类全名 + "Resources"`；null 抛 IAE）、`addElement`×2、`addChildElement`、`removeElement`（未知元素 no-op）、`addAttribute`×4、`addBooleanAttribute`（注册的属性恒 required=true；hasDefaultValue 只决定默认值有无）、`removeAttribute`（未知 no-op）、`addObjectValue`×4、`removeObjectValue`。
查询方法全部实现（canNodeAppear 仍抽象），错误统一 `IllegalArgumentException`；`getStandardFormatInstance()` 返回共享单例（`standardMetadataFormatName = "javax_imageio_1.0"`）。

## 2. Java 用户如何使用
插件作者继承并声明结构：
```java
class MyFormat extends IIOMetadataFormatImpl {
    MyFormat() {
        super("root", CHILD_POLICY_SEQUENCE);
        addElement("item", "root", 1, 4);           // REPEAT
        addAttribute("item", "name", DATATYPE_STRING, true, "unnamed");
        addBooleanAttribute("item", "active", true, true);
        addObjectValue("item", Integer.class, true, 1);
    }
    public boolean canNodeAppear(String e, ImageTypeSpecifier t) { return true; }
}
```
标准格式：`IIOMetadataFormat f = IIOMetadataFormatImpl.getStandardFormatInstance();`

## 3. 开源代码实际用法调研（来源：jdk8u-dev 源码 + jdk/test，只借鉴思路）
- `jdk/src/share/classes/javax/imageio/metadata/IIOMetadataFormatImpl.java`：构造器校验（168/204 行附近）；`getResource` 捕获 `MissingResourceException` 返回 null（bundle 缺失不是错误）；`addAttribute` 校验 dataType/枚举列表非空/列表 min≤max；`getElement/getAttribute` 对未知名抛 IAE。
- `jdk/test/javax/imageio/metadata/IIOMetadataFormatImplTest.java`（bug 4403350/4403352/4436995/4438977）：内联子类覆盖受保护 `addAttribute`，验证 `getElementMin/MaxChildren`、`getAttributeMin/MaxValue`、`getAttributeDescription`、`getAttributeEnumerations`。
- `jdk/test/javax/imageio/metadata/BooleanAttributes.java`：`addBooleanAttribute` 语义。
- `jdk/test/javax/imageio/metadata/RemoveElement.java`：`removeElement` 行为。
- `jdk/src/share/classes/com/sun/imageio/plugins/common/StandardMetadataFormat.java`：标准格式实现，资源 bundle 提供元素描述。

## 4. 测试用例设计 + 易错规范细节（`IIOMetadataFormatImplTest`）
- 白盒（testStructural）：`standardMetadataFormatName` public static final String = "javax_imageio_1.0"；`getStandardFormatInstance` public static、返回类型为接口；两个构造器（实测是 public 而非 protected）；10 个受保护 builder 方法名齐全。
- 结构与属性（testBehavior）：SEQUENCE 根 [item, meta]、item REPEAT 1..4、meta EMPTY → `getChildNames` null、三种属性全部 getter、`addBooleanAttribute` 恒 required（实测）、`removeAttribute/removeElement` 未知名 no-op、`addAttribute(list)` 追加。
- 对象值（testObjectValues）：arbitrary/enumeration/list 三型 + Comparable range 型（`VALUE_RANGE_MIN_INCLUSIVE`、getObjectMinValue/MaxValue 精确值）；`removeObjectValue` 后 `getObjectValueType` 返回 `VALUE_NONE` 而同族其它 5 个方法抛 IAE（实测区分）。
- 资源（testResourceBundle）：默认 basename = 子类名 + "Resources"、setRbn(null) IAE、set/get 往返、缺 bundle 描述 null、null locale 合法。
- 标准实例（testStandardInstance）：单例身份（两次 `==`）、root SOME、`getElementDescription("Chroma")` 非 null 而 root 元素为 null。
- builder 负例（testBuilderNegatives，全 IAE 实测）：addElement 未知父/非法策略/REPEAT 走 3 参、min>max/负 min；addAttribute 未知元素/非法 dataType/枚举 null/枚举空/列表 min>max；addChildElement 未知父；查询侧 12 项（非 REPEAT min/max、非枚举/非范围/非列表、未知元素/属性/null）。
- ctor 负例（testCtorNegatives）：null root、策略 6/-1、(String,int) 传 REPEAT、负 min、min>max；策略 0..4 全部接受、3 参构造强制 REPEAT 且界精确。
- 实测怪癖（已断言）：同名 `addAttribute` 二次注册覆盖默认值且 `getAttributeNames` 列表长度增长（出现重复名）。
- 版本差异：无（双侧 javap 一致）。
