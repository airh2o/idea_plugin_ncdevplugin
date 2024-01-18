package com.air.nc5dev.listener;

import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.intellij.execution.Executor;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentWithExecutorListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

/**
 * 监听项目，如果运行了，运行=前把 ump等文件复制到 NC HOME里
 */
public class NCRunManagerListener implements RunContentWithExecutorListener{

    @Override
    public void contentSelected(@Nullable RunContentDescriptor runContentDescriptor, @NotNull Executor executor) {
        String ncHomePath = ProjectNCConfigUtil.getNCHomePath();
        if(null == ncHomePath || ncHomePath.trim().isEmpty()){
            return ;
        }
        Project project = ProjectManager.getInstance().getDefaultProject();
        File projectUmpDir = new File(project.getBasePath(), "META-INF");
        if(!projectUmpDir.exists()){
            return ;
        }

        File nchome = new File(ncHomePath);
        if(!nchome.exists() || !nchome.isDirectory()){
            return ;
        }

        File modeluUmpDir = new File(ncHomePath, File.separatorChar + "modules"
            + File.separatorChar + project.getName() + File.separatorChar + "META-INF");
        if(!modeluUmpDir.exists() || !modeluUmpDir.isDirectory()){
            modeluUmpDir.mkdirs();
        }

        //复制 ump 文件到这里
        File[] projectFiles = projectUmpDir.listFiles(f -> f.isFile());
        Stream.of(projectFiles).forEach(f -> {
            try {
                Files.copy(f.toPath(), new File(modeluUmpDir,f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
            }
        });
    }

    @Override
    public void contentRemoved(@Nullable RunContentDescriptor runContentDescriptor, @NotNull Executor executor) {

    }
}
