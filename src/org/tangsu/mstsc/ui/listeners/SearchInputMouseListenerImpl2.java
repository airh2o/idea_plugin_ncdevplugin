package org.tangsu.mstsc.ui.listeners;

import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.ui.MstscDialog;
import com.alibaba.fastjson.JSON;
import org.tangsu.mstsc.ui.MainPanel;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.stream.Collectors;

public class SearchInputMouseListenerImpl2 implements MouseListener {
    MstscDialog mainPanel;

    public SearchInputMouseListenerImpl2(MstscDialog mainPanel) {
        this.mainPanel = mainPanel;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent event) {
        String s = StrUtil.trim(mainPanel.getTextField_search().getText());

        if (StrUtil.isBlank(s)) {
            mainPanel.loadDatas(mainPanel.getEs());
        }else{
            mainPanel.loadDatas(mainPanel.getEs().stream()
                    .filter(
                            e -> StrUtil.contains(JSON.toJSONString(e), s)
                    )
                    .collect(Collectors.toList()));
        }
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
}
