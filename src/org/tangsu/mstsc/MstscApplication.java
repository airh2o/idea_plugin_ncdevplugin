package org.tangsu.mstsc;

import com.air.nc5dev.util.StringUtil;
import org.tangsu.mstsc.dao.BaseDao;
import org.tangsu.mstsc.ui.MainWindow;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.sql.SQLException;

public class MstscApplication {
    public static volatile MainWindow mainWindow;
    public static String SQL_INIT = "create table t_mstsc\n" +
            "(\n" +
            "    pk                   TEXT primary key ,\n" +
            "    ip                   TEXT,\n" +
            "    user                 TEXT,\n" +
            "    pass                 TEXT,\n" +
            "    order1                int,\n" +
            "    code                 TEXT,\n" +
            "    title                TEXT,\n" +
            "    memo                 TEXT,\n" +
            "    port                 TEXT,\n" +
            "    class_id             TEXT,\n" +
            "    desktopwidth         TEXT,\n" +
            "    desktopheight        TEXT,\n" +
            "    screen_mode_id       TEXT,\n" +
            "    session_bpp          TEXT,\n" +
            "    winposstr            TEXT,\n" +
            "    compression          TEXT,\n" +
            "    displayconnectionbar TEXT,\n" +
            "    disable_wallpaper    TEXT,\n" +
            "    disable_themes       TEXT,\n" +
            "    redirectclipboard       TEXT,\n" +
            "    autoreconnection       TEXT,\n" +
            "    dr int default 0\n" +
            ")\n";

    public static void main(String[] args) throws Exception {
        new MstscApplication().start();
    }

    public void start() throws Exception {

     /*   BeautyEyeLNFHelper.frameBorderStyle = BeautyEyeLNFHelper.FrameBorderStyle.translucencyAppleLike;
        BeautyEyeLNFHelper.launchBeautyEyeLNF();
        UIManager.put("RootPane.setupButtonVisible", false);*/

        mainWindow = new MainWindow();

        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        double w = (screensize.getWidth() - mainWindow.getWidth()) / 3;
        double h = (screensize.getHeight() - mainWindow.getHeight()) / 3;

        mainWindow.setBounds((int) w, (int) h, mainWindow.getWidth(), mainWindow.getHeight());

        initData();

        makeSystemTray();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                mainWindow.getMstscEntitService().getDao().submit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));
    }

    public void makeSystemTray() {
        if (!SystemTray.isSupported()) {
            // 判断系统是否支持系统托盘
            return;
        }

        SystemTray sysTray = SystemTray.getSystemTray(); // 获得系统托盘
        // 创建弹出菜单
        PopupMenu popupMenu = new PopupMenu();
        MenuItem m = new MenuItem("显示/隐藏");
        m.addActionListener(e -> mainWindow.setVisible(!mainWindow.isVisible()));
        popupMenu.add(m);

        m = new MenuItem("退出");
        m.addActionListener(e -> System.exit(0));
        popupMenu.add(m);

        m = new MenuItem("立即连接");
        m.addActionListener(e -> mainWindow.getMainPanel().link(null));
        popupMenu.add(m);

        // 加载一个图片用于托盘图标的显示
        Image image = Toolkit.getDefaultToolkit().getImage(
                new File(new File(BaseDao.getDefualtBaseDir(), "img"), "tray.png").getPath()
        );
        // 创建一个托盘图标（这个图标就表示最小化后的应用），其中 popup 是点击托盘图标时弹出的菜单列表
        TrayIcon trayIcon = new TrayIcon(image
                , "Mstsc远程桌面管理小工具 V1-209308343@qq.com (双击左键显示隐藏,双击中间立即连接)"
                , popupMenu);
        // 设置自动调整图标大小以适应当前平台的托盘图标显示
        trayIcon.setImageAutoSize(true);
        // 添加托盘图标到系统托盘（一个应用程序可添加多个托盘图标）
        try {
            sysTray.add(trayIcon);
            trayIcon.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() > 1) {
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            mainWindow.setVisible(!mainWindow.isVisible());
                        } else if (e.getButton() == MouseEvent.BUTTON2) {
                            mainWindow.getMainPanel().link(null);
                        }
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

                @Override
                public void mouseReleased(MouseEvent e) {

                }

                @Override
                public void mouseEntered(MouseEvent e) {

                }

                @Override
                public void mouseExited(MouseEvent e) {

                }
            });
        } catch (AWTException e) {
            e.printStackTrace();
        }

    }

    public static void initData() throws SQLException, InstantiationException, IllegalAccessException {
        mainWindow.loadDatas();
    }
}
