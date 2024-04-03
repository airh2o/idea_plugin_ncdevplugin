package com.air.nc5dev.ui.compoment;

import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.ReflectUtil;
import com.air.nc5dev.util.V;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tangsu.mstsc.ui.BaseSimpleListTable;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * <br>
 * <br>
 * <br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2023/10/17 0017 14:09
 * @project
 * @Version
 */
public class SimpleListJBTable<T> extends BaseSimpleListTable {
    List<T> rows;
    ArrayList<MsgTableDialogColumnDTO> mcs;

    public static <T> SimpleListJBTable<T> of(List<T> rows) {
        Object row = rows.get(0);
        Class<?> clz = row.getClass();
        Field[] dfs = ReflectUtil.getFields(clz);

        ArrayList<MsgTableDialogColumnDTO> mcs = Lists.newArrayList();
        for (Field df : dfs) {
            MsgTableDialogColumnDTO mc = MsgTableDialogColumnDTO.of(df);
            if (mc == null) {
                continue;
            }

            mcs.add(mc);
        }

        if (mcs.isEmpty()) {
            throw new RuntimeException(
                    "要显示的数据对象中字段没有发现任何 @com.air.nc5dev.ui.compoment.SimpleListColumn 注解");
        }

        mcs.sort((m1, m2) -> m1.getSort() - m2.getSort());

        SimpleListJBTable t = new SimpleListJBTable(new DefaultTableModel(
                mcs.stream().map(MsgTableDialogColumnDTO::getTitle).toArray(), 0));
        t.rows = rows;
        t.mcs = mcs;
        t.setData2UI();

        return t;
    }

    private SimpleListJBTable(TableModel tableModel) {
        super(tableModel);
    }

    public String getSelectKey() {
        return "select";
    }

    public List<T> getDatas() {
        return new ArrayList<>(V.get(rows, Lists.newArrayList()));
    }

    public void setDatas(List<T> rows) {
        this.rows = V.get(rows, Lists.newArrayList());

        setData2UI();
    }

    public void setData2UI() {
        DefaultTableModel m = (DefaultTableModel) getModel();
        while (m.getRowCount() > 0) {
            m.removeRow(0);
        }

        if (CollUtil.isEmpty(rows)) {
            return;
        }

        for (T row : rows) {
            Vector v = new Vector();
            for (MsgTableDialogColumnDTO c : mcs) {
                v.add(ReflectUtil.getFieldValue(row, c.getKey()));
            }
            m.addRow(v);
        }

        fitTableColumns();
    }

    @Override
    public void fitTableColumns() {
        JTable myTable = this;
        JTableHeader header = myTable.getTableHeader();
        int rowCount = myTable.getRowCount();
        Enumeration columns = myTable.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            TableColumn column = (TableColumn) columns.nextElement();
            int col = header.getColumnModel().getColumnIndex(column.getIdentifier());
            if (mcs.get(col).isHide() == true) {
                column.setWidth(0);
            } else if (mcs.get(col).getWidth() != null) {
                column.setWidth(mcs.get(col).getWidth());
            } else {
                continue;
            }

            header.setResizingColumn(column);
        }

//        //行背景色
//        DefaultTableCellRenderer dtcr = new MyDefaultTableCellRenderer();
//        // 对每行的每一个单元格
//        int columnCount = myTable.getColumnCount();
//        for (int i = 0; i < columnCount; i++) {
//            myTable.getColumn(myTable.getColumnName(i)).setCellRenderer(dtcr);
//        }
    }

    @Override
    public boolean editCellAt(int row, int column) {
        return true;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MsgTableDialogColumnDTO implements Serializable, Cloneable {
        public static MsgTableDialogColumnDTO of(Field f) {
            SimpleListColumn a = f.getAnnotation(SimpleListColumn.class);
            if (a == null) {
                return null;
            }

            MsgTableDialogColumnDTO m = of(a);
            m.setKey(f.getName());
            return m;
        }

        public static MsgTableDialogColumnDTO of(SimpleListColumn a) {
            if (a == null) {
                return null;
            }

            return MsgTableDialogColumnDTO.builder()
                    .title(a.value())
                    .hide(a.hide())
                    .editbale(a.editbale())
                    .sort(a.sort())
                    .type(a.type())
                    .build();
        }

        String key;
        /**
         * @return
         */
        String title;

        /**
         * 隐藏
         *
         * @return
         */
        boolean hide;

        /**
         * 可编辑
         *
         * @return
         */
        boolean editbale;

        /**
         * 列位置， 越小越优先
         *
         * @return
         */
        int sort = 1000;

        /**
         * 列宽度
         */
        Integer width;

        /**
         * 类型
         *
         * @return
         */
        int type;
    }
}
