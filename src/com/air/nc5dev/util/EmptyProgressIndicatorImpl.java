package com.air.nc5dev.util;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 单纯避免com.intellij.openapi.progress.ProgressIndicator为空的
 */
public class EmptyProgressIndicatorImpl implements ProgressIndicator {
    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void cancel() {

    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public void setText(@NlsContexts.ProgressText String s) {

    }

    @Override
    public @NlsContexts.ProgressText String getText() {
        return null;
    }

    @Override
    public void setText2(@NlsContexts.ProgressDetails String s) {

    }

    @Override
    public @NlsContexts.ProgressDetails String getText2() {
        return null;
    }

    @Override
    public double getFraction() {
        return 0;
    }

    @Override
    public void setFraction(double v) {

    }

    @Override
    public void pushState() {

    }

    @Override
    public void popState() {

    }

    @Override
    public boolean isModal() {
        return false;
    }

    @Override
    public @NotNull
    ModalityState getModalityState() {
        return null;
    }

    @Override
    public void setModalityProgress(@Nullable ProgressIndicator progressIndicator) {

    }

    @Override
    public boolean isIndeterminate() {
        return false;
    }

    @Override
    public void setIndeterminate(boolean b) {

    }

    @Override
    public void checkCanceled() {

    }

    @Override
    public boolean isPopupWasShown() {
        return false;
    }

    @Override
    public boolean isShowing() {
        return false;
    }
}
