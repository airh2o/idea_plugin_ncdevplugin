package com.air.nc5dev.util.jdbc.resulthandel;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * JDBC 查询结果处理器， 处理 Jdbc查询返回结果是 List[ Map[String,Object] ]  的结果   <br/>
 * 因为JPA不支持自定义 查询结果处理器，没有办法只能 jpa查询返回值设置 list里是map，然后由这个统一进行 结果转换<br/>
 * ，尤其是针对 native sql查询，他不支持智能映射查询结果字段到 VO属性，只能用List的Map接受结果后 有这个接口进行处理<br/>
 * <br/>
 * <br/>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2020-9-18 09:32:02
 * @project
 * @Version <br/>
 */
@FunctionalInterface
public interface MapResultSetExtractor<T> {
    /**
     * 把jdbc查询结果行数据，转换成需要的东西
     * @param rows jdbc查询出的原始多行 数据
     * @return 你需要的vo对象
     * @throws SQLException
     */
    T extractData(List<Map<String, Object>> rows) throws SQLException;
}
