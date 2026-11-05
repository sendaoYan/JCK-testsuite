# java.lang.classfile.constantpool.ConstantPoolBuilder — 设计案

## 1. API 功能
`java.lang.classfile.constantpool.ConstantPoolBuilder`（interface）。工厂/静态方法：of。查询/实例方法：canWriteDirect, utf8Entry, classEntry, packageEntry, moduleEntry, nameAndTypeEntry, fieldRefEntry, methodRefEntry, interfaceMethodRefEntry, methodTypeEntry。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `ConstantPoolBuilderTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.constantpool.ConstantPoolBuilder` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`ConstantPoolBuilderTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.constantpool.ConstantPoolBuilder")` + public/interface/继承 + 关键成员。黑盒（`ConstantPoolBuilderTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05）：ConstantPoolBuilder 契约
- fresh size()==1（slot 0 保留）；首条目 index==1；put 精确增量：utf8/int/float/string +1、long/double +2（双槽）；重复 put 增量 0。
- 三 utf8 重载：utf8Entry(String/ClassDesc/MethodTypeDesc) 互相去重（ClassDesc 重载产**字段描述符**、MethodTypeDesc 重载产方法描述符）。
- of(ClassModel) 拷贝：size 相同、迭代序列 index/tag/toString 逐位相同、canWriteDirect(源池)==true、可继续扩容（探测值运行期计算，避免与自身类池常量撞车去重）。
- canWriteDirect 矩阵：自身 true；无关 fresh builder false；解析出的池 false；null → NPE。
- 分派工厂：constantValueEntry(Integer/String/Long/Float/Double)→对应 Entry 且与专用 put **同实例**；ClassDesc/MethodTypeDesc/MethodHandleDesc → IAE("Illegal type")；loadableConstantEntry(MTD/ClassDesc/Float/MHdesc) 正确分派。
- null 矩阵 19 项全 NPE（utf8×3 重载/classEntry×2/stringEntry×2/nameAndTypeEntry/methodRefEntry/fieldRefEntry/interfaceMethodRefEntry/methodHandleEntry×2/methodTypeEntry/moduleEntry/packageEntry/constantValueEntry/loadableConstantEntry/of(null)）。
- **易错**：方法名是 utf8Entry/intEntry/...，不是 putUtf8Entry/putInt。
### 深化通用要点（2026-11-05，全部实测锁定）
- 去重契约：同值 put 两次返回**同一实例**（==）且 index() 相同；不同值不同 index。
- 发射闭环三段：build 发射指令 → re-parse 常量池含对应条目 → 访问器值回读。
- JDK-27 类初始化环：`MethodTypeDesc.ofDescriptor("(I)V")` 若是程序首个 AMHC 触点会触发 ConstantDescs↔PrimitiveClassDescImpl 初始化环（ExceptionInInitializerError）；本测试以 testConstantDescsPreinit()（先引用 ConstantDescs.CD_Object）规避，新增方法必须在 testBlackBox 之后运行。
- javac 产物真类池断言用测试类自带成员（static final 常量 / lambda 字段）保证条目存在。
