/*
 * Copyright (C) 2024 Beijing Yishu Technology Co., Ltd.
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
package com.growingio.android.compose;

import static com.growingio.android.compose.GrowingCompose.SEMANTICS_PROP_CLICK;
import static com.growingio.android.compose.GrowingCompose.SEMANTICS_PROP_DIALOG;
import static com.growingio.android.compose.GrowingCompose.SEMANTICS_PROP_POPUP;

import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.compose.ui.geometry.Rect;
import androidx.compose.ui.layout.LayoutCoordinates;
import androidx.compose.ui.layout.LayoutCoordinatesKt;
import androidx.compose.ui.layout.ModifierInfo;
import androidx.compose.ui.node.LayoutModifierNode;
import androidx.compose.ui.node.LayoutNode;
import androidx.compose.ui.node.LayoutNodeLayoutDelegate;
import androidx.compose.ui.node.Owner;
import androidx.compose.ui.semantics.SemanticsConfiguration;
import androidx.compose.ui.semantics.SemanticsModifier;
import androidx.compose.ui.semantics.SemanticsPropertyKey;

import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.ViewElementEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.utils.DeviceUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class ComposeAndroidView {

    private final static String TAG = "ComposeAndroidView";

    private Owner owner;

    // for screen offset:circler choose the right view
    private final int ownerOffsetX;
    private final int ownerOffsetY;

    public ComposeAndroidView(View owner) {
        if (isComposeView(owner)) {
            this.owner = (Owner) owner;
            ComposeReflectUtils.getLayoutDelegateField();

            DisplayMetrics metrics = DeviceUtil.getDisplayMetrics(owner.getContext());
            ownerOffsetX = (metrics.widthPixels - owner.getWidth()) / 2;
            ownerOffsetY = (metrics.heightPixels - owner.getHeight()) / 2;
        } else {
            ownerOffsetX = 0;
            ownerOffsetY = 0;
            Logger.e(TAG, "ComposeAndroidView owner is not a Compose View.");
        }

    }

    public void click(MotionEvent event) {
        if (owner == null) {
            Logger.w(TAG, "Can't find Compose View in activity.");
            return;
        }
        locate(event.getX(), event.getY());
        //TrackMainThread.trackMain().postActionToTrackMain(() -> locate(event.getX(), event.getY()));
    }

    private void translateLayoutNodeOffset(ComposeNode nodeInfo) {
        LayoutNode node = (LayoutNode) nodeInfo.getLayoutNode();
        node.forEachCoordinator$ui_release(coordinator -> {
            LayoutModifierNode modifierNode = coordinator.getLayoutModifierNode();
            GrowingComposeKt.INSTANCE.reflectModifierNode(nodeInfo, modifierNode, node.getLayoutDirection());
            return null;
        });
    }


    private void locate(float x, float y) {
        final Queue<ComposeNode> queue = new LinkedList<>();
        ComposeNode rootNode = new ComposeNode(owner.getRoot());
        queue.add(rootNode);
        ComposeNode targetNode = null;

        while (!queue.isEmpty()) {
            final ComposeNode nodeInfo = queue.poll();
            final LayoutNode node = nodeInfo == null ? null : (LayoutNode) nodeInfo.getLayoutNode();
            if (node == null || !node.isPlaced()) {
                continue;
            }

            Rect bounds = getLayoutNodeBounds(node);
            if (bounds == null) {
                continue;
            }
            // check offset node
            translateLayoutNodeOffset(nodeInfo);
            bounds = nodeInfo.translateRect(bounds);
            if (x < bounds.getLeft() || x > bounds.getRight() || y < bounds.getTop() || y > bounds.getBottom()) {
                continue;
            }

            calculateLayoutNode(node, nodeInfo);
            nodeInfo.calculate();

            if (nodeInfo.isClickNode()) {
                Logger.d(TAG, "found clickNode");
                if (targetNode != null && targetNode.isEnd()) {
                    Logger.d(TAG, "found targetNode");
                } else {
                    targetNode = nodeInfo;
                }
            }

            List<LayoutNode> children;
            // check the node maybe has a native view.
            List<LayoutNode> androidViewNodes = ComposeReflectUtils.getAndroidComposeViewNode(node);
            if (androidViewNodes != null && !androidViewNodes.isEmpty()) {
                children = androidViewNodes;
            } else {
                // warn:if the node has a native view, don't call getZSortedChildren, maybe cause a crash.
                children = node.getZSortedChildren().asMutableList();
            }
            for (LayoutNode layoutNode : children) {
                ComposeNode childNode = nodeInfo.appendChildNode(layoutNode);
                queue.add(childNode);
            }

        }
        sendClickEvent(targetNode);
    }

    private void sendClickEvent(ComposeNode targetNode) {
        if (targetNode == null) return;

        TrackMainThread.trackMain().postEventToTrackMain(
                new ViewElementEvent.Builder()
                        .setPath(targetNode.path())
                        .setTextValue(targetNode.calculateText())
                        .setXpath(targetNode.xpath())
                        .setIndex(targetNode.index())
                        .setXIndex(targetNode.xIndex())
                        .setAttributes(targetNode.getAttributes())
        );

    }

    private void calculateLayoutNode(LayoutNode node, ComposeNode nodeInfo) {
        final List<ModifierInfo> modifiers = node.getModifierInfo();
        nodeInfo.setPlaceOrder(node.getPlaceOrder$ui_release());
        nodeInfo.setMeasurePolicy(node.getMeasurePolicy().getClass().getSimpleName());
        for (ModifierInfo modifierInfo : modifiers) {
            if (modifierInfo.getModifier() instanceof SemanticsModifier) {
                final SemanticsModifier semanticsModifierCore = (SemanticsModifier) modifierInfo.getModifier();
                final SemanticsConfiguration semanticsConfiguration = semanticsModifierCore.getSemanticsConfiguration();
                for (Map.Entry<? extends SemanticsPropertyKey<?>, ?> entry : semanticsConfiguration) {
                    final @Nullable String key = entry.getKey().getName();
                    if (SEMANTICS_PROP_CLICK.equals(key)) {
                        nodeInfo.setClickNode(true);
                    } else if (SEMANTICS_PROP_DIALOG.equals(key) || SEMANTICS_PROP_POPUP.equals(key)) {
                        nodeInfo.setZLevel(nodeInfo.getZLevel() + 1000);
                    } else if (GrowingCompose.TAG.equals(key) || "TestTag".equalsIgnoreCase(key)) {
                        if (entry.getValue() instanceof String) {
                            String tag = (String) entry.getValue();
                            nodeInfo.setTag(tag);
                        }
                    } else if (GrowingCompose.COMPOSABLE.equals(key) && nodeInfo.getComposableName() == null) {
                        if (entry.getValue() instanceof String) {
                            String composableTag = (String) entry.getValue();
                            nodeInfo.setComposableName(composableTag);
                            Logger.d(TAG, "[COMPOSABLE] " + composableTag);
                        }
                    } else if (GrowingCompose.CALL.equals(key) && nodeInfo.getCallName() == null) {
                        if (entry.getValue() instanceof String) {
                            String callName = (String) entry.getValue();
                            nodeInfo.setCallName(callName);
                            Logger.d(TAG, "[CALLNAME] " + callName);
                        }
                    } else if (GrowingCompose.PAGE_TAG.equals(key)) {
                        if (entry.getValue() instanceof String) {
                            String alias = (String) entry.getValue();
                            nodeInfo.setAlias(alias);
                        }
                    } else if (GrowingCompose.INTERRUPT_CLICK.equals(key)) {
                        nodeInfo.setEnd(true);
                        Logger.d("targetNode setEnd:", "INTERRUPT_CLICK");
                    }
                }
            } else {
                // Newer Jetpack Compose 1.5 uses Node modifier element for click/text.
                boolean clickable = ComposeReflectUtils.isClickableElement(modifierInfo.getModifier());
                if (clickable) {
                    nodeInfo.setClickNode(true);
                }

                String text = ComposeReflectUtils.getTextFromTextElement(modifierInfo.getModifier());
                if (text != null) {
                    nodeInfo.setText(text);
                }
            }
        }
    }

    private Rect getLayoutNodeBounds(LayoutNode node) {
        Field layoutDelegateField = ComposeReflectUtils.getLayoutDelegateField();
        if (layoutDelegateField != null) {
            try {
                final LayoutNodeLayoutDelegate delegate =
                        (LayoutNodeLayoutDelegate) layoutDelegateField.get(node);
                if (delegate == null) return null;

                LayoutCoordinates lc = delegate.getOuterCoordinator().getCoordinates();
                return LayoutCoordinatesKt.boundsInWindow(lc);
            } catch (Exception e) {
                Logger.w(TAG, "Could not access layoutDelegateField in LayoutNode", e);
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        if (owner == null) return 0;
        return owner.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof ComposeAndroidView) {
            return this.hashCode() == obj.hashCode();
        }
        return super.equals(obj);
    }


    List<ComposePageNode> collectPages() {
        if (owner == null) return new ArrayList<>();
        final List<ComposePageNode> pages = new ArrayList<>();
        final Queue<LayoutNode> queue = new LinkedList<>();
        queue.add(owner.getRoot());
        while (!queue.isEmpty()) {
            final LayoutNode node = queue.poll();
            if (node == null || !node.isPlaced()) {
                continue;
            }
            final List<ModifierInfo> modifiers = node.getModifierInfo();
            for (ModifierInfo modifierInfo : modifiers) {
                if (modifierInfo.getModifier() instanceof SemanticsModifier) {
                    final SemanticsModifier semanticsModifierCore = (SemanticsModifier) modifierInfo.getModifier();
                    final SemanticsConfiguration semanticsConfiguration = semanticsModifierCore.getSemanticsConfiguration();
                    for (Map.Entry<? extends SemanticsPropertyKey<?>, ?> entry : semanticsConfiguration) {
                        final @Nullable String key = entry.getKey().getName();
                        if (GrowingCompose.PAGE_TAG.equals(key)) {
                            if (entry.getValue() instanceof String) {
                                String alias = (String) entry.getValue();
                                Rect bounds = getLayoutNodeBounds(node);
                                if (bounds != null) {
                                    Rect offsetRect = bounds.translate(ownerOffsetX, ownerOffsetY);
                                    pages.add(new ComposePageNode(alias, offsetRect, null));
                                }
                            }
                        }
                    }
                }
            }

            List<LayoutNode> children = node.getZSortedChildren().asMutableList();
            queue.addAll(children);
        }
        return pages;
    }

    List<ComposeNode> collectViews() {
        if (owner == null) return new ArrayList<>();
        final Queue<ComposeNode> queue = new LinkedList<>();
        final List<ComposeNode> clickViews = new ArrayList<>();
        ComposeNode rootNode = new ComposeNode(owner.getRoot());
        queue.add(rootNode);
        int zLevel = 0;

        while (!queue.isEmpty()) {
            final ComposeNode nodeInfo = queue.poll();
            final LayoutNode node = nodeInfo == null ? null : (LayoutNode) nodeInfo.getLayoutNode();
            if (node == null || !node.isPlaced()) {
                continue;
            }
            Rect bounds = getLayoutNodeBounds(node);
            if (bounds == null) {
                continue;
            }

            // check offset node
            translateLayoutNodeOffset(nodeInfo);
            bounds = nodeInfo.translateRect(bounds);

            Rect offsetRect = bounds.translate(ownerOffsetX, ownerOffsetY);
            nodeInfo.setBounds(offsetRect);
            calculateLayoutNode(node, nodeInfo);
            nodeInfo.calculate();

            if (nodeInfo.isClickNode()) {
                if (nodeInfo.getParent() == null || !nodeInfo.getParent().isEnd()) {
                    clickViews.add(nodeInfo);
                }
            }

            List<LayoutNode> children;
            // check the node maybe has a native view.
            List<LayoutNode> androidViewNodes = ComposeReflectUtils.getAndroidComposeViewNode(node);
            if (androidViewNodes != null && !androidViewNodes.isEmpty()) {
                children = androidViewNodes;
            } else {
                // warn:if the node has a native view, don't call getZSortedChildren, maybe cause a crash.
                children = node.getZSortedChildren().asMutableList();
            }
            for (LayoutNode layoutNode : children) {
                ComposeNode childNode = nodeInfo.appendChildNode(layoutNode);
                queue.add(childNode);
            }
        }

        return clickViews;
    }

    static boolean isComposeView(View view) {
        return view instanceof Owner;
    }
}

