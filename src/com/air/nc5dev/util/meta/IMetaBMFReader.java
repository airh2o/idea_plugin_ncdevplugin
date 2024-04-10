package com.air.nc5dev.util.meta;

import com.air.nc5dev.vo.meta.ComponentAggVO;
import com.intellij.openapi.project.Project;

public interface IMetaBMFReader<T> {
    /**
     * 把一个来源读取成为 bmf的agg(详细)对象
     *
     * @param source
     * @param project
     * @return
     */
    ComponentAggVO readAggVO(T source, Project project) throws Exception;
}
