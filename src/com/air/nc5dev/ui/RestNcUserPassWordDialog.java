package com.air.nc5dev.ui;

import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.exception.BusinessException;
import com.air.nc5dev.util.*;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.jdbc.ConnectionUtil;
import com.air.nc5dev.util.jdbc.resulthandel.NoValueResultSetExtractor;
import com.air.nc5dev.util.jdbc.resulthandel.ResultSetExtractor;
import com.air.nc5dev.util.jdbc.resulthandel.VOArrayListResultSetExtractor;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.air.nc5dev.vo.NcUserVo;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.intellij.designer.clipboard.SimpleTransferable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/***
 *  重置操作员密码 弹框UI        </br>
 *           </br>
 *           </br>
 *           </br>
 * @author air Email: 209308343@qq.com
 * @date 2019/12/25 0025 15:29
 * @Param
 * @return
 */
public class RestNcUserPassWordDialog
        extends DialogWrapper {
    private ResetNCUserPassWordPanel contentPane;
    private AnActionEvent event;
    /**
     * key nc版本， value 默认查询smuser的sql
     */
    private static ConcurrentHashMap<NcVersionEnum, String> nc2SmUserSql = new ConcurrentHashMap<>();

    static {
        nc2SmUserSql.put(NcVersionEnum.NC5, "select \n" +
                "    cuserid as id, user_password as pass \n" +
                "     ,user_code as code, user_name as name,locked_tag as locked \n" +
                "from sm_user\n" +
                "where locked_tag <> 'y'");
        nc2SmUserSql.put(NcVersionEnum.U8Cloud, "select \n" +
                "    cuserid as id, user_password as pass \n" +
                "     ,user_code as code, user_name as name,locked_tag as locked \n" +
                "from sm_user \n" +
                "where locked_tag <> 'y'");

        nc2SmUserSql.put(NcVersionEnum.NC6, "select\n" +
                "    cuserid as id, user_password as pass\n" +
                "     ,user_code as code, user_name as name,islocked as locked\n" +
                "from sm_user\n" +
                "where islocked <> 'y'");
        nc2SmUserSql.put(NcVersionEnum.NCC, "select\n" +
                "    cuserid as id, user_password as pass\n" +
                "     ,user_code as code, user_name as name,islocked as locked\n" +
                "from sm_user\n" +
                "where islocked <> 'y'");
    }

    public RestNcUserPassWordDialog(AnActionEvent event) {
        super(event.getProject());
        this.event = event;
        setTitle("重置NC操作员密码");

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        if (contentPane == null) {
            contentPane = new ResetNCUserPassWordPanel();
            NCPropXmlUtil.loadConfFromFile(ProjectNCConfigUtil.getNCHomePath());
            List<NCDataSourceVO> dataSourceVOS = NCPropXmlUtil.getDataSourceVOS();
            if (CollUtil.isEmpty(dataSourceVOS)) {
                throw new BusinessException("请检查NCHOME或者NC数据源配置,数据源为空!");
            }

            for (NCDataSourceVO d : dataSourceVOS) {
                contentPane.comboBox_dataSource.addItem(d);
            }

            initListeners();
            refreshUI();
        }

        contentPane.setPreferredSize(new Dimension(800, 540));
        contentPane.setVisible(true);
        return this.contentPane;
    }

    /***
     *   点击了确认       </br>
     *           </br>
     *           </br>
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 16:17
     * @Param []
     * @return void
     */
    public void onOK() {
        dispose();
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return "RestNcUserPassWordDialog";
    }

    private void onCancel() {
        dispose();
    }

    /**
     * 初始化监听注册
     */
    private void initListeners() {
        contentPane.btn_loadUserList.addActionListener(this::loadUserList);
        contentPane.btn_executeOk.addActionListener(this::execute2Db);
        contentPane.btn_copyExecuteSqls.addActionListener(this::copyExecuteSqls);
        contentPane.list_selectUsers.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    NcUserVo vo = (NcUserVo) contentPane.list_selectUsers.getSelectedValue();
                    if (vo == null) {
                        return;
                    }

                    String str = vo.getName() + " |  " + NCPassWordUtil.decode(vo.getPass());
                    CopyPasteManager.getInstance().setContents(new SimpleTransferable(str, DataFlavor.stringFlavor));
                    LogUtil.info("复制用户列表元素: " + JSON.toJSONString(vo)
                            + " , 明文密码可能是: " + NCPassWordUtil.decode(vo.getPass()));
                }
            }
        });
        contentPane.comboBox_dataSource.addItemListener(e -> {
            refreshUI();
        });
    }

    /**
     * 刷新UI界面
     */
    public void refreshUI() {
        contentPane.textArea_queryUserSql.setText(nc2SmUserSql.get(ProjectNCConfigUtil.getNCVersion()));
        contentPane.listModel_selectUsers.clear();
    }

    /**
     * 直接执行修改密码语句到数据库
     */
    public void execute2Db(ActionEvent e) {
        List<String> sqls = buildUpdateSqls();

        if (CollUtil.isEmpty(sqls)) {
            LogUtil.error("未选中任何用户!");
            return;
        }

        int updateClassPath = Messages.showYesNoDialog("是否确认执行sql? (请注意备份数据,执行的sql列表参见插件日志窗口输出.)"
                , "询问", Messages.getQuestionIcon());
        if (updateClassPath != Messages.OK) {
            return;
        }

        int i = doDbUpdates(sqls);

        LogUtil.info("影响: " + i + " 行,直接执行到数据库的更新密码的SQL列表: \n " + Joiner.on("\n").skipNulls().join(sqls));

        loadUserList(null);
    }

    /**
     * 修改密码 语句搞到剪切板
     */
    public void copyExecuteSqls(ActionEvent e) {
        List<String> sqls = buildUpdateSqls();

        String s = Joiner.on(" \n ").skipNulls().join(sqls);
        CopyPasteManager.getInstance().setContents(new SimpleTransferable(s, DataFlavor.stringFlavor));
        LogUtil.info("复制到剪切板的更新密码的SQL列表: \n " + s);
    }

    public List<String> buildUpdateSqls() {
        List<String> sqls = null;
        if (NcVersionEnum.NC5.equals(ProjectNCConfigUtil.getNCVersion())
                || NcVersionEnum.U8Cloud.equals(ProjectNCConfigUtil.getNCVersion())) {
            sqls = buildNc5xUpdateSqls();
        } else if (NcVersionEnum.NC6.equals(ProjectNCConfigUtil.getNCVersion())
                || NcVersionEnum.NCC.equals(ProjectNCConfigUtil.getNCVersion())) {
            sqls = buildNc6xUpdateSqls();
        }

        if (sqls == null) {
            sqls = CollUtil.emptyList();
        }

        return sqls;
    }

    private List<String> buildNc6xUpdateSqls() {
        String newPass = getUserPass();
        newPass = StringUtils.trim(V.get(newPass));

        List<NcUserVo> vos = getSelectUserVos();

        if (CollUtil.isEmpty(vos)) {
            LogUtil.error("没有选择任何用户!");
        }

        boolean hasCpUser = true;
        try {
            doDbQuery("select cuserid,user_password from cp_user where 1=2", new NoValueResultSetExtractor());
        } catch (Throwable e) {
            hasCpUser = false;
        }

        ArrayList<String> sqls = Lists.newArrayListWithCapacity(vos.size());
        for (NcUserVo u : vos) {
            sqls.add(
                    "update sm_user set user_password='"
                            + NCPassWordUtil.encode(newPass, u) + "'"
                            + " where " + "cuserid='" + u.getId() + "' "
            );
            if (hasCpUser) {
                sqls.add(
                        "update cp_user set user_password='"
                                + NCPassWordUtil.encode(newPass, u) + "'"
                                + " where " + "cuserid='" + u.getId() + "' "
                );
            }
        }

        return sqls;
    }

    private List<String> buildNc5xUpdateSqls() {
        String newPass = getUserPass();
        newPass = StringUtils.trim(V.get(newPass));

        List<NcUserVo> vos = getSelectUserVos();

        if (CollUtil.isEmpty(vos)) {
            LogUtil.error("没有选择任何用户!");
        }

        ArrayList<String> sqls = Lists.newArrayListWithCapacity(vos.size());
        for (NcUserVo u : vos) {
            sqls.add(
                    "update sm_user set user_password='" + NCPassWordUtil.encode(newPass, u) + "' " +
                            "where "
                            + "cuserid='" + u.getId() + "' "
            );
        }

        return sqls;
    }

    /**
     * 加载 sm_user 用户列表
     */
    public void loadUserList(ActionEvent e) {
        List<NcUserVo> userVos = queryUserList();
        contentPane.listModel_selectUsers.clear();
        for (NcUserVo userVo : userVos) {
            contentPane.listModel_selectUsers.addElement(userVo);
        }
    }

    public List<NcUserVo> queryUserList() {
        ArrayList<NcUserVo> vos = doDbQuery(getQueryUserListSql()
                , new VOArrayListResultSetExtractor<NcUserVo>
                        (NcUserVo.class));

        return V.get(vos, CollUtil.emptyList());
    }

    public <T> T doDbQuery(String sql, ResultSetExtractor<T> resultSetExtractor) {
        NCDataSourceVO dataSourceVo = getSelectDataSourceVo();
        Connection conn = null;
        Statement st = null;
        try {
            conn = ConnectionUtil.getConn(dataSourceVo);
            conn.setAutoCommit(true);
            st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            T vos = resultSetExtractor.extractData(rs);

            IoUtil.close(rs);
            return vos;
        } catch (SQLException e1) {
            LogUtil.error(e1.toString(), e1);
        } catch (ClassNotFoundException e) {
            LogUtil.error(e.toString(), e);
        } finally {
            IoUtil.close(st);
            IoUtil.close(conn);
        }

        return null;
    }

    public int doDbUpdates(List<String> sqls) {
        NCDataSourceVO dataSourceVo = getSelectDataSourceVo();
        Connection conn = null;
        Statement st = null;
        try {
            conn = ConnectionUtil.getConn(dataSourceVo);
            conn.setAutoCommit(true);
            st = conn.createStatement();

            for (String s : sqls) {
                st.addBatch(s);
            }
            int[] ints = st.executeBatch();

            return Arrays.stream(ints).sum();
        } catch (SQLException e1) {
            LogUtil.error(e1.toString(), e1);
        } catch (ClassNotFoundException e) {
            LogUtil.error(e.toString(), e);
        } finally {
            IoUtil.close(st);
            IoUtil.close(conn);
        }

        return 0;
    }

    public NCDataSourceVO getSelectDataSourceVo() {
        return (NCDataSourceVO) contentPane.comboBox_dataSource.getSelectedItem();
    }

    public String getQueryUserListSql() {
        return contentPane.textArea_queryUserSql.getText();
    }

    public String getUserPass() {
        return contentPane.textInput_newPass.getText();
    }


    public List<NcUserVo> getSelectUserVos() {
        List list = contentPane.list_selectUsers.getSelectedValuesList();
        if (CollUtil.isEmpty(list)) {
            return CollUtil.emptyList();
        }
        List<NcUserVo> re = Lists.newArrayListWithCapacity(list.size());
        for (Object o : list) {
            re.add((NcUserVo) o);
        }

        return re;
    }

}
