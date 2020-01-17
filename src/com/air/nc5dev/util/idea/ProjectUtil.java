package com.air.nc5dev.util.idea;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import javax.annotation.Nonnull;

/**
 * 项目 工具类</br>
 * </br>
 * </br>
 * </br>
 *
 * @author air Email: 209308343@qq.com
 * @version NC505, JDK1.5+, V1.0
 * @date 2019/12/25 0025 8:43
 * @project
 */
public class ProjectUtil {
    /* *
      *     获取默认的项目      </br>
      *           </br>
      *           </br>
      *           </br>
      * @author air Email: 209308343@qq.com
      * @date 2019/12/25 0025 8:44
      * @Param []
      * @return com.intellij.openapi.project.Project
     */
    @Nonnull
    public static Project getDefaultProject(){
        Project[] openProjects = getProjectMannager().getOpenProjects();
        return null == openProjects || openProjects.length < 1 ? null : openProjects[0];
    }

    /* *
      *    TODO  根据项目名字获取项目,未实现，返回默认项目      </br>
      *           </br>
      *           </br>
      *           </br>
      * @author air Email: 209308343@qq.com
      * @date 2019/12/25 0025 8:47
      * @Param [name]
      * @return com.intellij.openapi.project.Project
     */
    @Deprecated
    public static Project getProjectByName(String name){
        return getDefaultProject();
    }

    /* *
      *    获取项目管理器       </br>
      *           </br>
      *           </br>
      *           </br>
      * @author air Email: 209308343@qq.com
      * @date 2019/12/25 0025 8:46
      * @Param []
      * @return com.intellij.openapi.project.ProjectManager
     */
    @Nonnull
    public static ProjectManager getProjectMannager(){
        return ProjectManager.getInstance();
    }
    private ProjectUtil() {
        throw new RuntimeException("cannot instance Util Class!");
    }
}
