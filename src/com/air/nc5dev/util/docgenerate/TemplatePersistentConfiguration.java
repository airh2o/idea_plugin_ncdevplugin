package com.air.nc5dev.util.docgenerate;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "SpringJavaPersistentConfiguration", storages = {@Storage("spring-java-config.xml")})
public class TemplatePersistentConfiguration implements PersistentStateComponent<TemplatePersistentConfiguration> {
    private Template template = new Template();

    public Template getTemplate() {
        return template;
    }

    public static TemplatePersistentConfiguration getInstance() {
        return ServiceManager.getService(TemplatePersistentConfiguration.class);
    }

    @Override
    public @Nullable
    TemplatePersistentConfiguration getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull TemplatePersistentConfiguration state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public void setTemplate(Template template) {
        this.template = template;
    }
}