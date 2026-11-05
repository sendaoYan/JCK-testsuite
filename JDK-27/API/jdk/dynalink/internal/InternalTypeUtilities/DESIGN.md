# jdk.dynalink.internal.InternalTypeUtilities（内部类，反射） 设计案

## 1. API 功能
dynalink 内部类型工具：areAssignable（纯引用赋值兼容，无装箱/拓宽）、canReferenceDirectly（类加载器可见性：app→bootstrap true、bootstrap→app false、同 loader true）、getCommonLosslessConversionType（int,long→long；无关引用→Object；有公共超类→最具体超类）。

## 2. Java 用户如何使用
不可直接使用（internal 包，需 --add-opens 或仅反射元数据）。

## 3. 开源代码实际用法调研
来源：JDK-27 src.zip jdk/dynalink/internal/InternalTypeUtilities.java；被 TypeConverterFactory 用于缓存判定。

## 4. 测试用例设计 + 易错点
反射调用三个方法断言真值表；易错：areAssignable(CharSequence,String) 与反向都为 true（isAssignableFrom 语义）；canReferenceDirectly 方向别写反——bootstrap 加载的代码看不到 app 类。

