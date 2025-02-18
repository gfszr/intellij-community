// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.ide.customize.transferSettings.ui.representation.ideVersion.sections

import com.intellij.ide.customize.transferSettings.models.SettingsPreferences
import com.intellij.ide.customize.transferSettings.models.SettingsPreferencesKind
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.NlsContexts
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JLabel

abstract class IdeRepresentationSection(private val prefs: SettingsPreferences,
                                        final override val key: SettingsPreferencesKind,
                                        private val icon: Icon) : TransferSettingsSection {
  protected val _isSelected: AtomicBooleanProperty = AtomicBooleanProperty(prefs[key])
  protected val _isEnabled = AtomicBooleanProperty(true)
  protected open val disabledCheckboxText: String? = null
  private val leftGap = 20
  private var morePanel: JComponent? = null
  private var moreLabel: String = "More.."

  val isSelected: Boolean by _isSelected

  init {
    _isSelected.afterChange {
      prefs[key] = it
    }
  }

  protected abstract fun getContent(): JComponent

  final override fun block() {
    _isEnabled.set(false)
  }

  final override fun getUI(): DialogPanel = panel {
    customizeSpacingConfiguration(EmptySpacingConfiguration()) {
      row {
        icon(icon).align(AlignY.TOP).customize(UnscaledGaps(left = 30, right = 30)).applyToComponent {
          _isSelected.afterChange {
            this.icon = if (it) this@IdeRepresentationSection.icon else IconLoader.getDisabledIcon(icon)
          }
          _isEnabled.afterChange {
            this.icon = if (it && _isSelected.get()) this@IdeRepresentationSection.icon else IconLoader.getDisabledIcon(icon)
          }
          border = JBUI.Borders.empty()
        }

        panel {
          row {
            checkBox(name).bold().bindSelected(_isSelected).applyToComponent {
              _isSelected.afterChange {
                this.foreground = if (it) UIUtil.getLabelForeground() else UIUtil.getLabelDisabledForeground()
              }
              _isEnabled.afterChange {
                this.foreground = if (it || _isSelected.get()) UIUtil.getLabelForeground() else UIUtil.getLabelDisabledForeground()
                isEnabled = it
              }
            }.customize(UnscaledGaps(bottom = 5, top = 5))
            label("").visible(false).apply {
              applyToComponent { foreground = UIUtil.getLabelDisabledForeground() }
              _isSelected.afterChange {
                visible(!it)
                if (disabledCheckboxText != null && !it) {
                  component.text = disabledCheckboxText
                }
              }
            }.customize(UnscaledGaps(left = 10))
          }.layout(RowLayout.INDEPENDENT)

          row {
            cell(getContent()).customize(UnscaledGaps(left = leftGap)) // TODO: retrieve size of checkbox and add padding here
          }
        }.align(AlignY.TOP)
      }
    }
  }

  protected fun Row.mutableLabel(@NlsContexts.Label text: String): Cell<JLabel> = label(text).applyToComponent {
    _isSelected.afterChange {
      this.foreground = if (it) UIUtil.getLabelForeground() else UIUtil.getLabelDisabledForeground()
    }
    _isEnabled.afterChange {
      this.foreground = if (it || !_isSelected.get()) UIUtil.getLabelForeground() else UIUtil.getLabelDisabledForeground()
    }
  }

  private fun withMoreLabel(moreLbl: String?, pnl: JComponent) {
    morePanel = pnl
    moreLbl?.apply { moreLabel = this }
  }

  private fun withMoreLabel(pnl: JComponent) = withMoreLabel(null, pnl)
  protected fun withMoreLabel(moreLbl: String?, pnl: () -> JComponent): Unit = withMoreLabel(moreLbl, pnl())
  protected fun withMoreLabel(pnl: (AtomicBooleanProperty) -> JComponent): Unit = withMoreLabel(pnl(_isSelected))

}