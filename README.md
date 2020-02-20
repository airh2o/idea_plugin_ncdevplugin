# idea_plugin_nc5devplugin
Yonyou NC5x or U8Cloud or NC6x Idea devtool Plugin   
实现了 Idea NC5x 6x U8Cloud 开发插件，功能比较简单


# 插件截图
    
![快速面板](https://s2.ax1x.com/2020/02/20/3eonRP.jpg "快速操作面板")    

![配置NCHOME](https://s2.ax1x.com/2020/02/20/3eolqg.jpg "配置NCHOME")    

![补丁导出界面](https://s2.ax1x.com/2020/02/20/3eomGt.jpg "补丁导出界面")    

![Tools菜单下位置](https://s2.ax1x.com/2020/02/20/3eoMM8.jpg "Tools菜单下位置")    
     
# 使用方式
插件安装后使用方式:
```
第一次新建项目-必须步骤：
1. Tools -> 配置NC HOME   进行NC HOME配置！ 
2. Tools -> 更新NC 库依赖 执行依赖更新! 
3. Tools -> 生成默认NC运行配置!(如果重启IDEA后丢失了运行配置 可以再次执行这个)    
4. 第一次执行上面步骤以后，会生成项目默认的client public private test 几个源文件夹 请手工在 项目结构修改几个文件夹的IDEA属性为正确值
```

 导出补丁支持 模块写一个配置文件 自定义相对路径：     
 ```
  本文件必须放入模块的根目录 文件名： patcherconfig.properties   
  文件用法(可参考示例文件 patcherconfig.properties 所有参数都可以配或不配置 插件有默认值的哈)：   
  
    class全限定名=相对路径(也就是NC模块名字)   
    例子(对test无效)：    
    nc.ui.gl.AddVoucherLineAction=gl   
    
    特殊参数：   
    config-notest=false    ##是否不要test代码，默认true     
    config-exportsourcefile=true     ##是否导出源代码，默认true        
    config-compressjar=false    ##是否把代码打包成jar文件， 默认false    
    config-compressEndDeleteClass    ##如果启用了代码打包成jar文件，是否删除class文件  默认false    
    config-ManifestFilePath=path    ##如果打包jar，那么 META-INF.MF 文件模板磁盘全路径(可以不配置 采用默认)        
    
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
1. 一个java文件非public类不会导出补丁！请手工操作.

# 未来待实现功能
1. 元数据编辑 - 暂不完成
2. WSDL新增功能  - 暂不完成