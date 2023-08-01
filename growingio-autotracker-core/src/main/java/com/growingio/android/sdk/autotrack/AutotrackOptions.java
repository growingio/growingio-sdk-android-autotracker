/*
 * Copyright (C) 2023 Beijing Yishu Technology Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.growingio.android.sdk.autotrack;

/**
 * <p>
 * 用来配置无埋点的作用范围
 *
 * @author cpacm 2023/5/15
 */
public class AutotrackOptions {
    private boolean activityMenuItemClickEnabled = false;
    private boolean toolbarMenuItemClickEnabled = true;
    private boolean actionMenuItemClickEnabled = true;
    private boolean popupMenuItemClickEnabled = true;
    private boolean contextMenuItemClickEnabled = true;

    private boolean fragmentPageEnabled = false;
    private boolean activityPageEnabled = false;

    private boolean dialogClickEnabled = true;

    private boolean adapterViewItemClickEnabled = true;
    private boolean spinnerItemClickSelectEnabled = true;
    private boolean expandableListGroupClickEnabled = true;
    private boolean expandableListChildClickEnabled = true;
    private boolean materialToggleGroupButtonCheckEnabled = true;
    private boolean tabLayoutTabSelectedEnabled = true;
    private boolean radioGroupCheckEnabled = true;
    private boolean viewClickEnabled = true;

    private boolean editTextChangeEnabled = true;
    private boolean compoundButtonCheckEnabled = true;
    private boolean seekbarChangeEnabled = true;
    private boolean ratingBarChangeEnabled = true;
    private boolean sliderChangeEnabled = true;

    public boolean isActivityMenuItemClickEnabled() {
        return activityMenuItemClickEnabled;
    }

    public void setActivityMenuItemClickEnabled(boolean activityMenuItemClickEnabled) {
        this.activityMenuItemClickEnabled = activityMenuItemClickEnabled;
    }

    public boolean isToolbarMenuItemClickEnabled() {
        return toolbarMenuItemClickEnabled;
    }

    public void setToolbarMenuItemClickEnabled(boolean toolbarMenuItemClickEnabled) {
        this.toolbarMenuItemClickEnabled = toolbarMenuItemClickEnabled;
    }

    public boolean isActionMenuItemClickEnabled() {
        return actionMenuItemClickEnabled;
    }

    public void setActionMenuItemClickEnabled(boolean actionMenuItemClickEnabled) {
        this.actionMenuItemClickEnabled = actionMenuItemClickEnabled;
    }

    public boolean isPopupMenuItemClickEnabled() {
        return popupMenuItemClickEnabled;
    }

    public void setPopupMenuItemClickEnabled(boolean popupMenuItemClickEnabled) {
        this.popupMenuItemClickEnabled = popupMenuItemClickEnabled;
    }

    public boolean isFragmentPageEnabled() {
        return fragmentPageEnabled;
    }

    public void setFragmentPageEnabled(boolean fragmentPageEnabled) {
        this.fragmentPageEnabled = fragmentPageEnabled;
    }

    public boolean isActivityPageEnabled() {
        return activityPageEnabled;
    }

    public void setActivityPageEnabled(boolean activityPageEnabled) {
        this.activityPageEnabled = activityPageEnabled;
    }

    public boolean isDialogClickEnabled() {
        return dialogClickEnabled;
    }

    public void setDialogClickEnabled(boolean dialogClickEnabled) {
        this.dialogClickEnabled = dialogClickEnabled;
    }

    public boolean isViewClickEnabled() {
        return viewClickEnabled;
    }

    public void setViewClickEnabled(boolean viewClickEnabled) {
        this.viewClickEnabled = viewClickEnabled;
    }

    public boolean isEditTextChangeEnabled() {
        return editTextChangeEnabled;
    }

    public void setEditTextChangeEnabled(boolean editTextChangeEnabled) {
        this.editTextChangeEnabled = editTextChangeEnabled;
    }

    public boolean isSeekbarChangeEnabled() {
        return seekbarChangeEnabled;
    }

    public void setSeekbarChangeEnabled(boolean seekbarChangeEnabled) {
        this.seekbarChangeEnabled = seekbarChangeEnabled;
    }

    public boolean isAdapterViewItemClickEnabled() {
        return adapterViewItemClickEnabled;
    }

    public void setAdapterViewItemClickEnabled(boolean adapterViewItemClickEnabled) {
        this.adapterViewItemClickEnabled = adapterViewItemClickEnabled;
    }

    public boolean isExpandableListGroupClickEnabled() {
        return expandableListGroupClickEnabled;
    }

    public void setExpandableListGroupClickEnabled(boolean expandableListGroupClickEnabled) {
        this.expandableListGroupClickEnabled = expandableListGroupClickEnabled;
    }

    public boolean isExpandableListChildClickEnabled() {
        return expandableListChildClickEnabled;
    }

    public void setExpandableListChildClickEnabled(boolean expandableListChildClickEnabled) {
        this.expandableListChildClickEnabled = expandableListChildClickEnabled;
    }

    public boolean isCompoundButtonCheckEnabled() {
        return compoundButtonCheckEnabled;
    }

    public void setCompoundButtonCheckEnabled(boolean compoundButtonCheckEnabled) {
        this.compoundButtonCheckEnabled = compoundButtonCheckEnabled;
    }

    public boolean isRadioGroupCheckEnabled() {
        return radioGroupCheckEnabled;
    }

    public void setRadioGroupCheckEnabled(boolean radioGroupCheckEnabled) {
        this.radioGroupCheckEnabled = radioGroupCheckEnabled;
    }

    public boolean isRatingBarChangeEnabled() {
        return ratingBarChangeEnabled;
    }

    public void setRatingBarChangeEnabled(boolean ratingBarChangeEnabled) {
        this.ratingBarChangeEnabled = ratingBarChangeEnabled;
    }

    public boolean isSpinnerItemClickSelectEnabled() {
        return spinnerItemClickSelectEnabled;
    }

    public void setSpinnerItemClickSelectEnabled(boolean spinnerItemClickSelectEnabled) {
        this.spinnerItemClickSelectEnabled = spinnerItemClickSelectEnabled;
    }

    public boolean isMaterialToggleGroupButtonCheckEnabled() {
        return materialToggleGroupButtonCheckEnabled;
    }

    public void setMaterialToggleGroupButtonCheckEnabled(boolean materialToggleGroupButtonCheckEnabled) {
        this.materialToggleGroupButtonCheckEnabled = materialToggleGroupButtonCheckEnabled;
    }

    public boolean isTabLayoutTabSelectedEnabled() {
        return tabLayoutTabSelectedEnabled;
    }

    public void setTabLayoutTabSelectedEnabled(boolean tabLayoutTabSelectedEnabled) {
        this.tabLayoutTabSelectedEnabled = tabLayoutTabSelectedEnabled;
    }

    public boolean isContextMenuItemClickEnabled() {
        return contextMenuItemClickEnabled;
    }

    public void setContextMenuItemClickEnabled(boolean contextMenuItemClickEnabled) {
        this.contextMenuItemClickEnabled = contextMenuItemClickEnabled;
    }

    public boolean isSliderChangeEnabled() {
        return sliderChangeEnabled;
    }

    public void setSliderChangeEnabled(boolean sliderChangeEnabled) {
        this.sliderChangeEnabled = sliderChangeEnabled;
    }
}
