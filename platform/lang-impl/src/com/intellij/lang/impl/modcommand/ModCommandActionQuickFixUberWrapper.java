// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.impl.modcommand;

import com.intellij.codeInsight.daemon.impl.actions.IntentionActionWithFixAllOption;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PriorityAction;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.modcommand.ModCommand;
import com.intellij.modcommand.ModCommandAction;
import com.intellij.modcommand.ModCommandExecutor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class ModCommandActionQuickFixUberWrapper extends LocalQuickFixAndIntentionActionOnPsiElement 
  implements Iconable, PriorityAction, IntentionActionWithFixAllOption {
  private final @NotNull ModCommandAction myAction;

  public ModCommandActionQuickFixUberWrapper(@NotNull ModCommandAction action, @NotNull PsiElement element) {
    super(element);
    myAction = action;
  }

  @NotNull ModCommandAction action() {
    return myAction;
  }

  @Override
  public @NotNull String getFamilyName() {
    return myAction.getFamilyName();
  }

  @Override
  public @NotNull String getText() {
    ModCommandAction.Presentation presentation = getPresentation();
    if (presentation != null) {
      return presentation.name();
    }
    return getFamilyName();
  }

  @Nullable
  private ModCommandAction.Presentation getPresentation() {
    PsiElement element = getStartElement();
    if (element == null) return null;
    ModCommandAction.ActionContext context = ModCommandAction.ActionContext.from(null, element.getContainingFile())
      .withElement(element);
    return myAction.getPresentation(context);
  }

  @Override
  public boolean isAvailable(@NotNull Project project,
                             @NotNull PsiFile file,
                             @Nullable Editor editor,
                             @NotNull PsiElement startElement,
                             @NotNull PsiElement endElement) {
    ModCommandAction.ActionContext context = ModCommandAction.ActionContext.from(editor, file).withElement(startElement);
    return myAction.getPresentation(context) != null;
  }

  @Override
  public void invoke(@NotNull Project project,
                     @NotNull PsiFile file,
                     @Nullable Editor editor,
                     @NotNull PsiElement startElement,
                     @NotNull PsiElement endElement) {
    ModCommandAction.ActionContext context = ModCommandAction.ActionContext.from(editor, file).withElement(startElement);
    ModCommand command = myAction.perform(context);
    ModCommandExecutor.getInstance().executeInteractively(context, command, editor);
  }

  @Override
  public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project, @NotNull ProblemDescriptor previewDescriptor) {
    return myAction.generatePreview(ModCommandAction.ActionContext.from(previewDescriptor));
  }

  @Override
  public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
    return myAction.generatePreview(ModCommandAction.ActionContext.from(editor, file));
  }

  @Override
  public @NotNull PriorityAction.Priority getPriority() {
    ModCommandAction.Presentation presentation = getPresentation();
    return presentation == null ? Priority.NORMAL : presentation.priority();
  }

  @Override
  public Icon getIcon(int flags) {
    ModCommandAction.Presentation presentation = getPresentation();
    return presentation == null ? null : presentation.icon();
  }

  @Override
  public @NotNull List<IntentionAction> getOptions() {
    ModCommandAction.Presentation presentation = getPresentation();
    return presentation != null && presentation.fixAllOption() != null ?
           IntentionActionWithFixAllOption.super.getOptions() : List.of();
  }

  @Override
  public boolean belongsToMyFamily(@NotNull IntentionActionWithFixAllOption action) {
    ModCommandAction.Presentation presentation = getPresentation();
    ModCommandAction unwrapped = ModCommandAction.unwrap(action);
    if (unwrapped == null || presentation == null || presentation.fixAllOption() == null) return false;
    return presentation.fixAllOption().belongsToMyFamily().test(unwrapped);
  }

  @Override
  public @NotNull String getFixAllText() {
    ModCommandAction.Presentation presentation = getPresentation();
    return presentation != null && presentation.fixAllOption() != null ?
           presentation.fixAllOption().name() : "";
  }

  @Override
  public String toString() {
    return "[LocalQuickFixAndIntentionActionOnPsiElement] " + myAction;
  }
}
