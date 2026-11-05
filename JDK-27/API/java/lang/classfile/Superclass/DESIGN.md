# java.lang.classfile.Superclass — 设计案

## 1. API 功能
`java.lang.classfile.Superclass`（interface，extends `ClassElement`）。静态工厂：`of(ClassEntry)`。实例方法：`superclassEntry()`（ClassEntry）。

## 2. Java 用户如何使用
`cb.withSuperclass(ClassDesc)` / `cb.withSuperclass(ClassEntry)` 两重载；不指定时**默认 java/lang/Object** 且 Superclass 元素仍写入 elementList。`cm.superclass()`（Optional）回读——Object.class 与 module-info（buildModule）为 **empty**；接口类的 superclass 仍是 java/lang/Object。null 负例 NPE。

## 3. 参考来源
- JDK 27 javadoc + `javap java.lang.classfile.Superclass`。
- OpenJDK `test/jdk/jdk/classfile/`（ClassBuildingTest、ModuleBuilderTest）。
- 本机实测：Superclass.of(null)→NPE（消息 null）；withSuperclass((ClassDesc)null)→NPE 消息含 `Cannot invoke "java.lang.constant.ClassDesc.isClassOrInterface()" because "cd" is null`；默认 build 产物 superclass==java/lang/Object 且 elementList 恰 1 个 Superclass 元素；元素 entry 与 cm.superclass().get() **同一对象**。

## 4. 测试用例设计 + 易错规范细节
- 白盒（原有）+ 黑盒（原有：of(entry) 回读 + build 往返）。
- `testNullAndDefaults`（深化）：**Superclass.of(null) NPE**、**withSuperclass((ClassDesc)null) NPE 精确消息**、层次 instanceof。
- `testBuildReadback`（深化）：不指定 superclass → 默认 Object + elementList 恰 1 个 Superclass 元素；withSuperclass(ClassDesc)/(ClassEntry) 两重载往返；**Superclass 元素 superclassEntry()==cm.superclass().get()（身份）**；Object.class superclass empty；**module-info superclass empty**；接口 build superclass==Object + ACC_INTERFACE；真类 self superclass==Object；verify 干净。
- `testDefineClassLoop`（深化）：DynSuperclass+seq 继承 Exception + 生成 `<init>(String)`（ACC_PUBLIC + aload/aload/invokespecial Exception.&lt;init&gt;）→ defineClass → `getConstructor(String).newInstance("boom").getMessage()=="boom"`（**继承链真实例化闭环**）。
- 易错点：`<init>` 需显式 ACC_PUBLIC 才能被 getConstructor 找到；Object.class 资源经 `java.lang.Object.class.getResourceAsStream("Object.class")` 可读；单继承——interfaces 里不出现父类。
