package com.air.nc5dev.util.exportpatcher.beforafter;

import com.air.nc5dev.vo.FileContentVO;
import com.air.nc5dev.vo.ExportContentVO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.function.BiConsumer;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2024/11/26 0026 20:11
 * @project
 * @Version
 */
@Data
@Accessors(chain = true)
public abstract class AbstarctBeforRule implements BiConsumer<ExportContentVO, FileContentVO> {
    AbstarctBeforRule next;

    @Override
    public void accept(ExportContentVO contentVO, FileContentVO fileContentVO) {
        doBefor(contentVO, fileContentVO);

        if (next != null) {
            next.accept(contentVO, fileContentVO);
        }
    }

    public abstract void doBefor(ExportContentVO contentVO, FileContentVO fileContentVO);

    public void addNext(AbstarctBeforRule next) {
        if (this.next == null) {
            this.next = next;
        }else{
            this.next.addNext(next);
        }
    }
}
