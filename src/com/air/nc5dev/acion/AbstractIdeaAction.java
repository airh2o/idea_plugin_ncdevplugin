package com.air.nc5dev.acion;

import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
  *   这是IDEA action顶级抽象类，请所有的action继承他        </br>
  *           </br>
  *           </br>
  *           </br>
  * @author air Email: 209308343@qq.com
  * @date 2020/3/18 0018 8:53
  * @Param
  * @return
 */
public abstract class AbstractIdeaAction extends AnAction {
    private static boolean isWindowsSystem = isIsWindowsSystem();
    @Override
    public void actionPerformed(AnActionEvent e) {
        //设置默认项目
        ProjectUtil.setProject(e.getProject());
        
        doHandler(e);
    }
    /**
      *  当前是否是windows系统         </br>
      *           </br>
      *           </br>
      *           </br>
      * @author air Email: 209308343@qq.com
      * @date 2020/3/18 0018 8:55
      * @Param []
      * @return boolean
     */
    protected boolean isWindows() {
        return isWindowsSystem;
    }

    /**
     *  当前是否是windows系统         </br>
     *           </br>
     *           </br>
     *           </br>
     * @author air Email: 209308343@qq.com
     * @date 2020/3/18 0018 8:55
     * @Param []
     * @return boolean
     */
    protected static boolean isIsWindowsSystem() {
        return StringUtil.get(System.getProperty("os.name")).toLowerCase().contains("windows");
    }

    /**
      *     事件处理方法      </br>
      *           </br>
      *           </br>
      *           </br>
      * @author air Email: 209308343@qq.com
      * @date 2020/3/18 0018 8:55
      * @Param [e]
      * @return void
     */
    protected abstract void doHandler(AnActionEvent e);
}
