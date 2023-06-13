package com.air.nc5dev.ui.actionurlsearch;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.nccrequstsearch.RequestMappingItemProvider;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.vo.NCCActionInfoVO;
import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.ide.DataManager;
import com.intellij.ide.IdeEventQueue;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereManager;
import com.intellij.lang.java.JavaDocumentationProvider;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.ClassFileViewProvider;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.compiled.ClsClassImpl;
import com.intellij.psi.impl.java.stubs.JavaClassElementType;
import com.intellij.psi.impl.java.stubs.JavaStubElementTypes;
import com.intellij.psi.impl.java.stubs.impl.PsiClassStubImpl;
import com.intellij.psi.impl.java.stubs.impl.PsiJavaFileStubImpl;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.ui.table.JBTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.tangsu.mstsc.ui.MainPanel;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.InputStream;
import java.util.*;

@Getter
public class ActionResultListTable extends JBTable {
    NCCActionURLSearchUI nccActionURLSearchUI;
    DefaultTableModel tableModel;
    List<ActionResultDTO> datas;

    public ActionResultListTable(DefaultTableModel tableModel, NCCActionURLSearchUI nccActionURLSearchUI) {
        super(tableModel);
        this.tableModel = tableModel;
        this.nccActionURLSearchUI = nccActionURLSearchUI;
        addMouseListener(new NccActionTableMouseListenerImpl(this));
        addMouseListener(new MySelectFileTableMouseAdpaterImpl(this));
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        int row = rowAtPoint(event.getPoint());
        int column = columnAtPoint(event.getPoint());

        String txt = "";

        if (row > -1 && column > -1) {
            txt = StringUtil.getSafeString(getValueAt(row, column));
        }

        ActionResultDTO vo = getDataOfRow(row);
        if (column == -111115) {
            try {
                VirtualFile file = findClassFile(vo);
                if (file == null) {
                    return txt;
                }

                JavaDocumentationProvider jdp = new JavaDocumentationProvider();
                String simpleName = txt.substring(txt.lastIndexOf('.') + 1);
                String packge = txt.substring(0, txt.lastIndexOf('.'));
                PsiJavaFileStubImpl parent = new PsiJavaFileStubImpl(new PsiJavaFileImpl(
                        new ClassFileViewProvider(PsiManager.getInstance(getNccActionURLSearchUI().getProject()), file))
                        , packge, null, true);
                PsiClassStubImpl pcs = new PsiClassStubImpl(JavaStubElementTypes.CLASS, parent, txt, simpleName
                        , null, (short) 0);
                return jdp.generateHoverDoc(new ClsClassImpl(pcs), null);
            } catch (Throwable e) {
                e.printStackTrace();
                return txt;
            }
        } else if (column == -1000000 && vo != null && vo.getType() == NCCActionInfoVO.TYPE_ACTION) {
            return "/nccloud/" + txt.replace('.', '/') + ".do";
        } else if (vo != null) {
            return vo.displayText().replace("\n", "<br>");
        }

        return txt;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public void fitTableColumns() {
        JTable myTable = this;
        JTableHeader header = myTable.getTableHeader();
        int rowCount = myTable.getRowCount();
        Enumeration columns = myTable.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            TableColumn column = (TableColumn) columns.nextElement();
            int col = header.getColumnModel().getColumnIndex(
                    column.getIdentifier());
            if (MainPanel.tableAttrs[col].equals("pk")) {
                column.setWidth(50);
                header.setResizingColumn(column);
                continue;
            } else if (MainPanel.tableAttrs[col].equals("title")) {
                column.setWidth(400);
                header.setResizingColumn(column);
                continue;
            } else if (MainPanel.tableAttrs[col].equals("ip")) {
                column.setWidth(200);
                header.setResizingColumn(column);
                continue;
            } else {
                column.setWidth(50);
                header.setResizingColumn(column);
                continue;
            }

            /*int width = (int) myTable.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(myTable,
                            column.getIdentifier(), false, false, -1, col)
                    .getPreferredSize().getWidth();
            for (int row = 0; row < rowCount; row++) {
                int preferedWidth = (int) myTable.getCellRenderer(row, col)
                        .getTableCellRendererComponent(myTable,
                                myTable.getValueAt(row, col), false, false,
                                row, col).getPreferredSize().getWidth();
                width = Math.max(width, preferedWidth);
            }
            header.setResizingColumn(column);
            column.setWidth(width + myTable.getIntercellSpacing().width);*/
        }

        //行背景色
       /* DefaultTableCellRenderer dtcr = new MyDefaultTableCellRenderer();
        // 对每行的每一个单元格
        int columnCount = myTable.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            myTable.getColumn(myTable.getColumnName(i)).setCellRenderer(dtcr);
        }*/
    }

    public void openXml(ActionResultDTO vo) {
        ActionResultDTO re = vo;
        if (re == null) {
            return;
        }

        // 直接 打开 文件 编辑
        final FileEditorManager fileEditorManager = FileEditorManager.getInstance(
                getNccActionURLSearchUI().getProject());
        String xml = re.getXmlPath();

        VirtualFile virtualFile = null;

        String jar = "jar://" + StrUtil.replace(StrUtil.replace(xml, "\\", "/"), ".jar/", ".jar!/");
        if (jar.toLowerCase().contains(".jar!/")) {
            virtualFile = VirtualFileManager.getInstance().findFileByUrl(jar);
        } else {
            virtualFile = VirtualFileManager.getInstance().findFileByNioPath(new File(xml).toPath());
        }

        if (virtualFile == null) {
            IoUtil.tryOpenFileExpolor(new File(xml));
            return;
        }

        openFile(getNccActionURLSearchUI().getProject(), virtualFile, re.getRow(), re.getColumn());
    }

    private static void matchAutlRowColumn(List<String> lines, ActionResultDTO vo) {
        for (int x = 0; x < lines.size(); x++) {
            if (lines.get(x).contains(vo.getName())) {
                vo.setAuth_row(x);
                vo.setAuth_column(lines.get(x).indexOf(vo.getName()));
                return;
            }
        }
    }

    public void openAuthXml(ActionResultDTO vo) {
        ActionResultDTO re = vo;
        if (re == null || StringUtil.isBlank(re.getAuthPath())) {
            return;
        }

        // 直接 打开 文件 编辑
        final FileEditorManager fileEditorManager = FileEditorManager.getInstance(
                getNccActionURLSearchUI().getProject());
        String xml = re.getAuthPath();
        VirtualFile virtualFile = null;
        String jar = "jar://" + StrUtil.replace(StrUtil.replace(xml, "\\", "/"), ".jar/", ".jar!/");
        if (jar.toLowerCase().contains(".jar!/")) {
            virtualFile = VirtualFileManager.getInstance().findFileByUrl(jar);
        } else {
            virtualFile = VirtualFileManager.getInstance().findFileByNioPath(new File(xml).toPath());
        }

        if (virtualFile == null) {
            IoUtil.tryOpenFileExpolor(new File(xml));
            return;
        }

        if (re.getAuth_column() < 1) {
            try {
                InputStream in = virtualFile.getInputStream();
                List<String> lines = cn.hutool.core.io.IoUtil.readUtf8Lines(in,
                        new ArrayList<>());
                IoUtil.close(in);
                matchAutlRowColumn(lines, vo);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        openFile(getNccActionURLSearchUI().getProject(), virtualFile, re.getAuth_row(), re.getAuth_column());
    }

    public void searchClass(ActionResultDTO vo) {
        if (vo == null) {
            return;
        }

        SearchEverywhereManager seManager = SearchEverywhereManager.getInstance(nccActionURLSearchUI.getProject());
        FeatureUsageTracker.getInstance().triggerFeatureUsed("SearchEverywhere");
        IdeEventQueue.getInstance().getPopupManager().closeAllPopups(false);
        DataContext dataContext = DataManager.getInstance().getDataContext(nccActionURLSearchUI.createUI());
        AnActionEvent anActionEvent = new AnActionEvent(null, dataContext
                , this.getClass().getSimpleName(), new Presentation()
                , ActionManager.getInstance(), 0);
        seManager.show("ClassSearchEverywhereContributor", vo.getClazz(), anActionEvent);
    }

    public void setRows(List<ActionResultDTO> results) {
        this.datas = results;
        removeAll();
        for (ActionResultDTO e : results) {
            Vector row = new Vector();
            for (String arr : NCCActionURLSearchUI.tableAttrs) {
                row.add(ReflectUtil.getFieldValue(e, arr));
            }
            tableModel.addRow(row);
        }

        if (nccActionURLSearchUI.getCheckBox_AutoColumnSize().isSelected()) {
            fitTableColumns();
        }
    }


    public void removeAll() {
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }
    }

    @Data
    @AllArgsConstructor
    public static class MySelectFileTableMouseAdpaterImpl extends MouseAdapter {
        ActionResultListTable table;

        @Override
        public void mouseClicked(MouseEvent e) {
            final int row = table.rowAtPoint(e.getPoint());
            if (row == -1) {
                return;
            }
            final ActionResultDTO vo = table.getDataOfRow(row);
            table.getNccActionURLSearchUI().getTextArea_detail().setText(vo == null ? "" : vo.displayText());
        }

        @Override
        public void mousePressed(MouseEvent me) {
            if (SwingUtilities.isRightMouseButton(me)) {
                final int row = table.rowAtPoint(me.getPoint());
                if (row == -1) {
                    return;
                }

                final ActionResultDTO vo = table.getDataOfRow(row);
                final int column = table.columnAtPoint(me.getPoint());
                final JPopupMenu popup = new JPopupMenu();

                JMenuItem openClass = new JMenuItem("打开Class");
                popup.add(openClass);
                openClass.setEnabled(vo != null && StringUtil.isNotBlank(vo.getClazz()));
                openClass.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        table.openClass(vo);
                    }
                });

                JMenuItem searchClass = new JMenuItem("搜索Class");
                popup.add(searchClass);
                searchClass.setEnabled(vo != null && StringUtil.isNotBlank(vo.getClazz()));
                searchClass.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        table.searchClass(vo);
                    }
                });

                JMenuItem openXml = new JMenuItem("打开XML");
                popup.add(openXml);
                openXml.setEnabled(vo != null && StringUtil.isNotBlank(vo.getXmlPath()));
                openXml.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        table.openXml(vo);
                    }
                });

                JMenuItem openAuthXml = new JMenuItem("打开鉴权XML");
                popup.add(openAuthXml);
                openAuthXml.setEnabled(vo != null && StringUtil.isNotBlank(vo.getAuthPath()));
                openAuthXml.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        table.openAuthXml(vo);
                    }
                });

                popup.add(new JSeparator());

                openClass = new JMenuItem("复制类名称");
                popup.add(openClass);
                openClass.setEnabled(vo != null && StringUtil.isNotBlank(vo.getClazz()));
                openClass.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        ActionResultDTO v = vo;
                        if (v != null) {
                            StringUtil.setIntoClipboard(v.getClazz());
                        }
                    }
                });

                openClass = new JMenuItem("复制XML路径");
                popup.add(openClass);
                openClass.setEnabled(vo != null && StringUtil.isNotBlank(vo.getXmlPath()));
                openClass.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        ActionResultDTO v = vo;
                        if (v != null) {
                            StringUtil.setIntoClipboard(v.getXmlPath());
                        }
                    }
                });

                openClass = new JMenuItem("复制鉴权XML路径");
                popup.add(openClass);
                openClass.setEnabled(vo != null && StringUtil.isNotBlank(vo.getAuthPath()));
                openClass.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        ActionResultDTO v = vo;
                        if (v != null) {
                            StringUtil.setIntoClipboard(v.getAuthPath());
                        }
                    }
                });

                openClass = new JMenuItem("复制完整信息");
                popup.add(openClass);
                openClass.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        ActionResultDTO v = vo;
                        if (v != null) {
                            StringUtil.setIntoClipboard(v.displayText());
                        }
                    }
                });

                popup.show(me.getComponent(), me.getX(), me.getY());
            }
        }


    }

    public VirtualFile findClassFile(ActionResultDTO vo) {
        String path = null;
        vo.setClazz(StrUtil.trim(vo.getClazz()));
        //搜索 工程里的Java文件!
        String clz = vo.getClazz().substring(vo.getClazz().lastIndexOf('.') + 1);
        String classPt = StrUtil.replace(vo.getClazz(), ".", File.separator);
        String classPtDir = classPt.substring(0, classPt.lastIndexOf(File.separator));
        Module[] modules = ModuleManager.getInstance(getNccActionURLSearchUI().getProject()).getModules();
        for (Module module : modules) {
            VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
            for (VirtualFile sourceRoot : sourceRoots) {
                File f = new File(sourceRoot.getPath(), classPtDir);
                File[] fs = f.listFiles();
                if (fs == null) {
                    continue;
                }
                for (File file : fs) {
                    if (!file.isFile()) {
                        continue;
                    }

                    if (file.getName().equalsIgnoreCase(clz + ".java")
                            || file.getName().toLowerCase().endsWith("$" + clz.toLowerCase() + ".java")) {
                        path = file.getPath();
                        break;
                    }
                }
            }
        }

        //搜索HOME里的class文件
        if (path == null) {
            File hotwebs = new File(ProjectNCConfigUtil.getNCHome(), "hotwebs");
            File nccloud = new File(hotwebs, "nccloud");
            File webinf = new File(nccloud, "WEB-INF");
            File classes = new File(webinf, "classes");
            File cz = new File(classes, classPt);
            if (cz.isDirectory()) {
                File[] fs = cz.listFiles();
                for (File file : fs) {
                    if (!file.isFile()) {
                        continue;
                    }

                    if (file.getName().equalsIgnoreCase(clz + ".class")
                            || file.getName().toLowerCase().endsWith("$" + clz.toLowerCase() + ".class")) {
                        path = file.getPath();
                        break;
                    }
                }
            }
        }

        if (path == null) {
            path = findClzPath(vo, vo.getClazz());
        }

        if (path == null) {
            //我要是用大招了， 全局拉一边jar
            Map<String, NCCActionInfoVO> map =
                    RequestMappingItemProvider.ALL_ACTIONS.get(RequestMappingItemProvider.getKey(getNccActionURLSearchUI().getProject()));
            if (CollUtil.isNotEmpty(map)) {
                Collection<NCCActionInfoVO> vs = map.values();
                for (NCCActionInfoVO v : vs) {
                    path = findClzPath(v, vo.getClazz());
                    if (path != null) {
                        break;
                    }
                }
            }
        }

        if (path == null) {
            return null;
        }

        VirtualFile virtualFile = null;

        if (path.startsWith("jar://")) {
            virtualFile = VirtualFileManager.getInstance().findFileByUrl(path);
        } else {
            virtualFile = VirtualFileManager.getInstance().findFileByNioPath(new File(path).toPath());
        }

        return virtualFile;
    }

    public void openClass(ActionResultDTO vo) {
        if (vo == null) {
            return;
        }

        VirtualFile virtualFile = findClassFile(vo);

        if (virtualFile == null) {
            searchClass(vo);
            return;
        }

        openFile(getNccActionURLSearchUI().getProject(), virtualFile, 0, 0);
    }

    public static void openFile(Project project, VirtualFile vf, int row, int column) {
        ProjectUtil.openFile(project, vf, row, column);
    }

    public ActionResultDTO getDataOfRow(int row) {
        if (row < 0) return null;

        if (CollUtil.isEmpty(getDatas()) || getDatas().size() < row) {
            return null;
        }

        return getDatas().get(row);
    }

    public static String findClzPath(NCCActionInfoVO vo, String classz) {
        String path = null;
        //可能是jar
        String jar = null;
        if (vo.getXmlPath() != null && vo.getXmlPath().toLowerCase().contains(".jar" + File.separatorChar)) {
            jar =
                    vo.getXmlPath().substring(0,
                            vo.getXmlPath().toLowerCase().lastIndexOf(".jar" + File.separatorChar) + 4);
        } else if (vo.getAuthPath() != null && vo.getAuthPath().toLowerCase().contains(".jar" + File.separatorChar)) {
            jar =
                    vo.getAuthPath().substring(0,
                            vo.getAuthPath().toLowerCase().lastIndexOf(".jar" + File.separatorChar) + 4);
        }

        if (jar != null) {
            path = "jar://" + jar + "!/" + StrUtil.replace(classz, ".", "/") + ".class";

            if (VirtualFileManager.getInstance().findFileByUrl(path) != null) {
                return path;
            }
        }

        return null;
    }
}
