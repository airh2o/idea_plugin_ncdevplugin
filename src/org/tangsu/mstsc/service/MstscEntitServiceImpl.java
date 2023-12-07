package org.tangsu.mstsc.service;

import lombok.Data;
import org.tangsu.mstsc.dao.BaseDao;
import org.tangsu.mstsc.entity.MstscEntity;

import java.sql.SQLException;
import java.util.List;

@Data
public class MstscEntitServiceImpl {
    BaseDao dao;

    public MstscEntitServiceImpl(String dbfile) throws Exception {
        dao = new BaseDao(dbfile);
        try {
            dao.findAll(BaseDao.buildSelectSql(new MstscEntity()) + " and 1=2 ", MstscEntity.class);
        } catch (Throwable throwables) {
            //初始化表！
           dao.update(dao.buildCreateTableSql(new MstscEntity()));
           dao.submitNoClose();
        }
    }

    public List<MstscEntity> findAll() throws SQLException, InstantiationException, IllegalAccessException {
        return dao.findAll(BaseDao.buildSelectSql(new MstscEntity()) + " order by order1 "
                , MstscEntity.class);
    }

    public MstscEntity insert(MstscEntity e) throws SQLException, InstantiationException, IllegalAccessException {
        dao.insert(e);
        dao.submitNoClose();
        return e;
    }

    public MstscEntity update(MstscEntity e) throws SQLException, InstantiationException, IllegalAccessException {
        dao.updateById(e);
        dao.submitNoClose();
        return e;
    }

    public int delete(String id) throws SQLException, InstantiationException, IllegalAccessException {
        int i = dao.deleteById(id, MstscEntity.class);

        dao.submitNoClose();

        return i;
    }
}
