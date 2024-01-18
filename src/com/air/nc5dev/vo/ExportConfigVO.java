package com.air.nc5dev.vo;

import com.air.nc5dev.util.StringUtil;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;
import java.util.Properties;

/**
 * 导出补丁的 配置文件 配置信息VO <br/>
 * <br/>
 * <br/>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2020/11/8 11:41
 * @project
 * @Version
 */
@Data
public class ExportConfigVO {
    /**
     * 是否导出源文件
     */
    public boolean hasSource = false;
    /**
     * 是否不导出 测试代码
     */
    public boolean noTest = true;
    /**
     * 是否导出 的代码打包成 一个个的jar文件
     */
    public boolean toJar = false;
    /**
     * 如果tojar了，是否删除class文件
     */
    public boolean toJarThenDelClass = true;
    /**
     * 是否 自动猜测模块
     */
    public boolean guessModule = false;
    /**
     * 是否不导 这个模块
     */
    public boolean ignoreModule = false;
    /**
     * Manifest 文件路径
     */
    public String manifestFilePath;
    /**
     * 是否关闭 javap 方式识别class路径
     */
    public boolean closeJavaP = false;
    /**
     * 忽略文件的列表
     */
    public List<String> ignoreFiles = Lists.newLinkedList();
    /**
     * 所有的配置信息
     */
    public Properties prop;
    /**
     * NCC的话，出补丁 client里哪些class的packge文件需要放入hotwebs，如果这个是空 就全部！
     */
    public List<String> nccClientHotwebsPackges;
    /**
     * 是否不导出 modelue.xml 默认false
     */
    public boolean notExportModelueXml = false;




    /**
     * 获取配置的其他信息
     *
     * @param key
     * @return
     */
    public String getProperty(String key) {
        return StringUtil.trim(prop.getProperty(key));
    }

    /**
     * 获取配置的其他信息
     *
     * @param key
     * @return
     */
    public String getProperty(String key, String ifnull) {
        return StringUtil.get(StringUtil.trim(prop.getProperty(key)), ifnull);
    }
}
