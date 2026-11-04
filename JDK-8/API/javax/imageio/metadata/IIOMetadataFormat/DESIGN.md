# javax.imageio.metadata.IIOMetadataFormat — DESIGN（JDK-8）

## 1. API 功能
`IIOMetadataFormat` 是描述元数据树合法结构的接口（元素、子策略、属性、对象值的约束描述）。26 个抽象方法 + 22 个 int 常量：
- 子策略常量（javap 实值）：`CHILD_POLICY_EMPTY=0/ALL=1/SOME=2/CHOICE=3/SEQUENCE=4/REPEAT=5/MAX=5`。
- 值类型常量：`VALUE_NONE=0/ARBITRARY=1/RANGE=2/RANGE_MIN_INCLUSIVE_MASK=4/RANGE_MAX_INCLUSIVE_MASK=8/RANGE_MIN_INCLUSIVE=6/RANGE_MAX_INCLUSIVE=10/RANGE_MIN_MAX_INCLUSIVE=14/ENUMERATION=16/LIST=32`（6=2|4、10=2|8、14=2|4|8 掩码恒等式）。
- 数据类型常量：`DATATYPE_STRING=0/BOOLEAN=1/INTEGER=2/FLOAT=3/DOUBLE=4`。
- 方法：`getRootName/canNodeAppear/getElementMinChildren/getElementMaxChildren/getElementDescription/getChildPolicy/getChildNames`（EMPTY 元素返回 null）/`getAttributeNames`（无属性元素返回空数组）/`getAttributeValueType/getAttributeDataType/isAttributeRequired/getAttributeDefaultValue/getAttributeEnumerations/getAttributeMinValue/getAttributeMaxValue/getAttributeListMinLength/getAttributeListMaxLength/getAttributeDescription`/对象值族 `getObjectValueType/getObjectClass/getObjectDefaultValue/getObjectEnumerations/getObjectMinValue/getObjectMaxValue/getObjectArrayMinLength/getObjectArrayMaxLength`。

## 2. Java 用户如何使用
用户一般从 `IIOMetadata.getMetadataFormat(name)` 或 `IIOMetadataFormatImpl.getStandardFormatInstance()` 拿实现，再按名查询约束：`IIOMetadataFormat f = md.getMetadataFormat("javax_imageio_1.0"); String[] kids = f.getChildNames(f.getRootName()); int policy = f.getChildPolicy("Text");`。插件作者实现该接口（通常继承 `IIOMetadataFormatImpl`）声明原生格式。

## 3. 开源代码实际用法调研（来源：jdk8u-dev 源码 + jdk/test，只借鉴思路）
- `jdk/src/share/classes/javax/imageio/metadata/IIOMetadataFormat.java`：接口声明全部常量与契约；javadoc 写明 `getChildNames` 对 EMPTY 返回 null、非 REPEAT 取 min/max children 抛 IAE。
- `jdk/src/share/classes/com/sun/imageio/plugins/common/StandardMetadataFormat.java`：标准 `javax_imageio_1.0` 格式实现（root SOME；Text REPEAT 且 max=Integer.MAX_VALUE；TextEntry EMPTY 含 keyword/value/language/encoding/compression 5 属性；`canNodeAppear` 对任意名字恒 true——含未知元素）。
- `jdk/test/javax/imageio/metadata/GetChildNames.java`：验证 EMPTY 返回 null。
- `jdk/test/javax/imageio/metadata/GetObjectMinValue.java`、`ObjectArrayMaxLength.java`：对象值范围/数组长度查询。

## 4. 测试用例设计 + 易错规范细节（`IIOMetadataFormatTest`）
- 白盒：接口、public、无父接口、26 个方法全 abstract、22+ 常量为 public static final int，全部常量按 javap 实值断言（含 RANGE 掩码 5 个常量与 `2|4|8==14` 恒等式）。
- 经接口引用驱动内联 `TestFormat`（ALL 根 + REPEAT 子 + arbitrary/enumeration/range/list/boolean 五种属性 + arbitrary 对象值）：全部 getter 精确断言（valueType/dataType/required/default/enumerations/min/max/list 长度、对象 class/default）；无属性元素 `getAttributeNames` 长度 0。
- 标准格式实例：root 名、SOME、root 子含 Chroma/Text/Data/Compression/Dimension；Text REPEAT min=0 max=MAX_VALUE；Chroma SOME 且子含 ColorSpaceType/NumChannels；ColorSpaceType 唯一属性 `name`（required、ENUMERATION、STRING、无默认）；NumChannels 唯一属性 `value`；TextEntry EMPTY + 5 属性、value required/arbitrary/string、`getAttributeDescription("TextEntry","value")` 非 null（JDK bundle）；root 元素描述为 null；`canNodeAppear` 未知元素返回 true（标准实现的实测行为，与"查无此元素抛 IAE"的查询方法不同）。
- 负例矩阵（实测精确类，全 IAE）：非 REPEAT 取 min/max children、非枚举取 enumerations、非 range 取 minValue、非 list 取 listMinLength、未知元素/属性/null 元素名、无对象值元素取 defaultValue/class。**易错**：`getObjectValueType` 对无对象值元素不抛 IAE 而返回 `VALUE_NONE`（与同族其它方法不同）。
- 版本差异：无（双侧 javap 完全一致，常量值相同）。
