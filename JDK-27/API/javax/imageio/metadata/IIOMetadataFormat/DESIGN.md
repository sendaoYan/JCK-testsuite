# javax.imageio.metadata.IIOMetadataFormat — DESIGN

## 1. API 功能
`IIOMetadataFormat` 是描述元数据文档结构的接口：定义元素父子约束、属性名/类型/取值约束、以及节点可存 `Object` 的约束。方法分组：
- 常量：子节点策略 `CHILD_POLICY_*`（EMPTY/ALL/SOME/CHOICE/SEQUENCE/REPEAT）、取值类型 `VALUE_*`（NONE/ARBITRARY/RANGE/ENUMERATION/LIST 及 RANGE 含边界掩码）、数据类型 `DATATYPE_*`（STRING/BOOLEAN/INTEGER/FLOAT/DOUBLE）。
- 根/元素：`getRootName()`、`canNodeAppear(String, ImageTypeSpecifier)`、`getElementMinChildren/MaxChildren`、`getChildPolicy`、`getChildNames`、`getElementDescription`。
- 属性：`getAttributeNames`、`getAttributeValueType/DataType`、`isAttributeRequired`、`getAttributeDefaultValue/Enumerations/MinValue/MaxValue/ListMinLength/ListMaxLength/Description`。
- 对象值：`getObjectValueType/Class/DefaultValue/Enumerations/MinValue/MaxValue/ArrayMinLength/ArrayMaxLength`。

## 2. Java 用户如何使用
用户通常不自己实现接口，而是从 `IIOMetadata.getMetadataFormat(name)` 或 `IIOMetadataFormatImpl.getStandardFormatInstance()` 获取一个实现，再按元素/属性名查询约束：
`IIOMetadataFormat f = md.getMetadataFormat("javax_imageio_1.0"); String[] kids = f.getChildNames(f.getRootName()); int policy = f.getChildPolicy("Text");`
插件作者实现该接口（通常继承 `IIOMetadataFormatImpl`）以声明自己的原生格式。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/imageio/metadata/IIOMetadataFormat.java:78`：接口定义全部常量与方法契约，如 `getChildNames` 对 `CHILD_POLICY_EMPTY` 返回 null。
- `src/java.desktop/share/classes/javax/imageio/metadata/IIOMetadataFormatImpl.java:75`：`IIOMetadataFormatImpl implements IIOMetadataFormat`，是 JDK 提供的可复用实现。
- `src/java.desktop/share/classes/com/sun/imageio/plugins/common/StandardMetadataFormat.java`：标准 `javax_imageio_1.0` 格式实现，`getStandardFormatInstance()` 返回它（root 策略 CHILD_POLICY_SOME，Text 策略 CHILD_POLICY_REPEAT）。
- `test/jdk/javax/imageio/metadata/GetChildNames.java`：验证 `getChildNames` 对 EMPTY 节点返回 null。

## 4. 测试用例设计 + 自动化实现（`IIOMetadataFormatTest.testBlackBox`）
- 用内联 `TestFormat extends IIOMetadataFormatImpl`，但仅通过 `IIOMetadataFormat` 接口引用调用全部查询方法。
- 覆盖：root 名、`canNodeAppear`、ALL/REPEAT 子策略、REPEAT min/max、五种属性取值（arbitrary/enumeration/range/list/boolean）的 valueType/dataType/required/default/enumerations/min/max/list 长度、对象值（arbitrary 的 class/default）。
- 真实标准格式实例：root 名、CHILD_POLICY_SOME、子节点含 Chroma/Text、Text 为 REPEAT 且 max=Integer.MAX_VALUE。
- 契约异常：非 REPEAT 取 min/max children、非枚举取 enumerations、未知元素取 childPolicy 均抛 `IllegalArgumentException`。
- 白盒：接口 public abstract；全部 `CHILD_POLICY_*`/`VALUE_*`/`DATATYPE_*` 常量精确值。
