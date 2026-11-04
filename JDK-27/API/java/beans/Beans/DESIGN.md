# java.beans.Beans — 设计案

## 1. API 功能
Bean 工具类。isDesignTime/isGuiAvailable 及对应 setter、instantiate（按类名/Class）、isInstanceOf/getInstanceOf。

## 2. Java 用户如何使用
`Beans.isDesignTime()` 判断设计期；`Beans.instantiate(cl, "pkg.Bean")` 动态创建 Bean。

## 3. 开源代码实际用法调研
JDK 源码 `Beans.java`；IDE 设计工具使用。

## 4. 测试用例设计 + 自动化实现
白盒：类。黑盒（`testBlackBox`）：designTime/guiAvailable 开关、isInstanceOf/getInstanceOf、instantiate 两种重载。

## 5. 深化补充（batch beansG，96 断言）
在原有白盒+黑盒基础上追加（旧 12 → 新 96 断言），与 JDK-8 侧文件同构、仅版本差异段不同：
- **testWhiteBoxStructure**：public 具体类、直继承 Object、无接口/泛型/字段、单一 public 无参构造器、全部声明方法 static；每个 instantiate 重载声明 IOException+ClassNotFoundException（JDK-27 共 2 个重载）。
- **testInstanceOfMatrix**：精确类/超类/接口/数组/原始类型/void 全矩阵；**协变陷阱**：`String[]`→`Object[]`、→`CharSequence[]` 为 false（isInstanceOf 委托 `Introspector.isSubclass`，沿超类链+接口闭包，不做数组协变；`String[]/int[]`→Cloneable/Serializable 为 true）；null targetType false 无异常；null bean → NPE（两侧一致）。
- **testGetInstanceOf**：任意 targetType（含不匹配/null）恒等返回原引用；null bean → null。
- **testDesignTimeToggle / testGuiAvailableRelative**：默认 false、往返、幂等，**finally 恢复**；guiAvailable 只做相对断言（headless 初值 false）。
- **testInstantiatePositive**：嵌套二进制名（null loader 与显式 loader）、每次新实例、`<Bean>BeanInfo` 存在不干扰、java.lang.String。
- **testInstantiateNegative**（负例精确异常表，与 JDK-8 实测一致）：不存在/空/畸形点号名 → ClassNotFoundException；抽象类/接口/数组名 → ClassNotFoundException 包装 InstantiationException；私有构造器 → 包装 IllegalAccessException；`instantiate(null, null)` → NPE（未声明）。
- **testInstantiateWithBeanContext**：3 参重载（JDK-23 起 deprecated-for-removal，加 @SuppressWarnings("removal") 继续覆盖）：bean 加入 ctx（contains/size）、null ctx 合法。
- **testDesignModeStateIndependence**：Beans 全局开关与 BeanContextSupport 本地 flag 互不传播（探针 jdk-27.0.1 与 jdk8u372 一致）。
- **testVersionSpecificSignatures（JDK-27 版）**：`Class.forName("java.beans.AppletInitializer")` → ClassNotFoundException（JDK-9 删除）；无任何 4 参 instantiate；instantiate 重载恰 2 个；`setDesignTime/setGuiAvailable` 的 getExceptionTypes() 为空（JDK-8 声明 SecurityException，JDK-27 不再声明）。
