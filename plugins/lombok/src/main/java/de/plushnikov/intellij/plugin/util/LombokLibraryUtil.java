package de.plushnikov.intellij.plugin.util;

import com.intellij.java.library.JavaLibraryUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.psi.util.CachedValuesManager;
import de.plushnikov.intellij.plugin.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class LombokLibraryUtil {

  private static final String LOMBOK_PACKAGE = "lombok.experimental";

  public static boolean hasLombokLibrary(@NotNull Project project) {
    if (project.isDefault() || !project.isInitialized()) {
      return false;
    }
    ApplicationManager.getApplication().assertReadAccessAllowed();
    return JavaLibraryUtil.hasLibraryJar(project, "org.projectlombok:lombok");
  }

  public static @NotNull String getLombokVersionCached(@NotNull Project project) {
    return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
      String lombokVersion = null;
      try {
        lombokVersion = ReadAction.nonBlocking(() -> getLombokVersionInternal(project)).executeSynchronously();
      }
      catch (ProcessCanceledException e) {
        throw e;
      }
      catch (Throwable e) {
        Logger.getInstance(LombokLibraryUtil.class).error(e);
      }
      return new Result<>(StringUtil.notNullize(lombokVersion), ProjectRootManager.getInstance(project));
    });
  }

  private static @Nullable String getLombokVersionInternal(@NotNull Project project) {
    PsiPackage aPackage = JavaPsiFacade.getInstance(project).findPackage(LOMBOK_PACKAGE);
    if (aPackage != null) {
      PsiDirectory[] directories = aPackage.getDirectories();
      if (directories.length > 0) {
        List<OrderEntry> entries =
          ProjectRootManager.getInstance(project).getFileIndex().getOrderEntriesForFile(directories[0].getVirtualFile());
        if (!entries.isEmpty()) {
          return Version.parseLombokVersion(entries.get(0));
        }
      }
    }
    return null;
  }
}
