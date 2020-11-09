# 注意，因为本人懒等原因，最新的插件是 项目根目录的 zip文件哦，不是发行版里的哦！！！！

# 因本人目前在微服务公司，所以本人业余接开发私单，有私单才能继续参与NC 继续更新插件啊，人懒又要养家

# idea_plugin_nc5devplugin
Yonyou NC5x or U8Cloud or NC6x Idea devtool Plugin   
实现了 Idea NC5x 6x U8Cloud 开发插件，功能比较简单


# 插件截图
 
![快速面板](https://s2.ax1x.com/2020/02/20/3eonRP.jpg "快速操作面板")    

![配置NCHOME](https://s2.ax1x.com/2020/02/20/3eolqg.jpg "配置NCHOME")    

![补丁导出界面](https://s2.ax1x.com/2020/02/20/3eomGt.jpg "补丁导出界面")    

![Tools菜单下位置](https://s2.ax1x.com/2020/02/20/3eoMM8.jpg "Tools菜单下位置")    
     
# buy me a coffee:     
<img width="200px" height="200px" src="https://s1.ax1x.com/2020/03/30/GeyNdI.md.png"  alt="buy me a coffee"  />     

# 使用方式
插件安装后使用方式:
```
第一次新建项目-必须步骤：
1. Tools -> 配置NC HOME   进行NC HOME配置！ 
2. Tools -> 更新NC 库依赖 执行依赖更新! 
3. Tools -> 生成默认NC运行配置!(如果重启IDEA后丢失了运行配置 可以再次执行这个)    
4. 第一次执行上面步骤以后，会生成项目默认的client public private test 几个源文件夹 请手工在 项目结构修改几个文件夹的IDEA属性为正确值
5. 请注意 模块的编译输出路径要选 use module path 分别设置test和非test class输出路径。
```
![注意简单使用方式,数据库配置不管他 没用处的,就是个显示功能](https://s1.ax1x.com/2020/11/09/B7FcOH.jpg "注意简单使用方式,数据库配置不管他 没用处的,就是个显示功能")  
 导出补丁支持 模块写一个配置文件 自定义相对路径：     
 ```
  本文件必须放入模块的根目录 文件名： patcherconfig.properties   
  文件用法(可参考示例文件 patcherconfig.properties 所有参数都可以配或不配置 插件有默认值的哈)：   
  
    class全限定名=相对路径(也就是NC模块名字)   
    例子(对test无效)：    
    nc.ui.gl.AddVoucherLineAction=gl   
    
    支持包路径比如:    
    nc.ui.gl=gl2       
    nc.ui=gl3       
    会根据 包路径判断模块名字，优先级是 全路径》包路径(包路径从最末级逐个向上匹配)》模块猜测       
    
    特殊参数：   
    ##是否不要test代码，默认true
    config-notest=false         
    ##是否导出源代码，默认true
    config-exportsourcefile=true         
    ##是否把代码打包成jar文件， 默认false    
    config-compressjar=false        
    ##如果启用了代码打包成jar文件，是否删除class文件  默认false
    config-compressEndDeleteClass      
    ##如果打包jar，那么 META-INF.MF 文件模板磁盘全路径(可以不配置 采用默认)  
    config-ManifestFilePath=path             
    #是否猜测模块，默认false，开启后 如果配置文件没有指明的类会根据包名第三个判断模块       
    # （比如 nc.ui.pub.ButtonBar 第三个是pub 所以认为模块是 pub）     
    config-guessModule=true     
    类路径配置文件:     
        比如我要把 nc.bs.arap包里 1.txt和DzTakeF1Impl.wsdl文件输出到 arap模块下的包文件夹里:   
                                        nc.bs.arap.1.txt=arap   
        nc.bs.arap.DzTakeF1Impl.wsdl=arap    
 ```
# 常见问题
1. IDEA 重启后如果提示运行配置的
    variables 比如 FIELD_NC_HOME 等无效，请无视他
    ，因为在项目运行列表里里配置了
    ，无需在 file > setting > apperarance & behavior > path variables 里配置全局的！    


# 已知BUG
1. 注意： 如果你没有设置JAVAHOME或者ufjdk没有javap工具，导出补丁不会导出 同一个源文件内的非public类中匿名且非public的类文件。但如果有javap就会导出 但同时耗时较长！    
# 未来待实现功能
1. 元数据编辑 - 暂不完成
2. WSDL新增功能  - 暂不完成

# 版本更新
``` 
     2.2.0 版本 更新:    
           1. 优化部分代码,减少插件直接爆出错误信息    
           2. 新增 统一日志窗口，部分界面 不在使用右下角弹框    
           3. 修复 U8Cloud部分功能无法使用    
           4. 新增意见生成VO的set方法用    
           3. 修复 U8Cloud部分功能无法使用    
           3. 修复 U8Cloud部分功能无法使用    
           3. 修复 U8Cloud部分功能无法使用    


    2.1.0版本 更新:    
           1. 不修改NC本身数据源配置文件prop.xml    
           2. 生成运行配置会生成模块的补丁导出配置文件。    
           3. 修复打包成Jar Jar文件异常 无法识别。    
           4. 增加部分导出补丁的配置参数。    
           5. 导出补丁的支持包路径配置。    
      
    2.0版本重大更新:         
          
           1. 优化大量代码    
           2. 支持IDEA同时打开多个项目 区分当前按钮点击所在项目。        
           3. 修复IDEA同时打开多个项目 ,每次配置文件打开自动加载新的项目插件配置文件。        
        
```


# 感谢
部分代码 参考或使用了 部分插件开源项目的代码。
涉及到的有(列表可能不全，请谅解，实际已源码和项目为准)：
IDEA plugin 官方示例项目 或 开源插件：
比如 https://github.com/SonarSource/sonarlint-intellij , https://github.com/gejun123456/intellij-generateAllSetMethod  等
框架比如 guava hutool 等等
再次感谢开源项目参与者们的无私奉献！


























