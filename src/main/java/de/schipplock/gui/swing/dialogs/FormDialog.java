/*
 * Copyright 2022 Andreas Schipplock
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.schipplock.gui.swing.dialogs;

import de.schipplock.gui.swing.dialogs.listener.DocumentAdapter;
import de.schipplock.gui.swing.dialogs.listener.TextFieldKeyListener;
import de.schipplock.gui.swing.dialogs.supplier.DialogSupplier;
import de.schipplock.gui.swing.dialogs.supplier.ValueSupplier;
import de.schipplock.gui.swing.dialogs.verifier.Verifier;
import de.schipplock.gui.swing.svgicon.SvgIconManager;
import de.schipplock.gui.swing.svgicon.SvgIcons;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;

import static java.lang.String.format;

public class FormDialog extends JDialog {

    private static final String LAYOUT_CONSTRAINTS = System.getenv("MIGLAYOUT_CONSTRAINTS");

    private final JPanel mainPanel;

    private final JPanel itemPanel;

    private final JPanel actionPanel;

    private JPanel groupPanel;

    private final JButton confirmButton = new JButton(localize("formdialog.confirmbutton.title"));

    private final JButton cancelButton = new JButton(localize("formdialog.cancelbutton.title"));

    private final Map<String, String> values = new HashMap<>();

    private final Set<JTextField> invalidTextFields = new HashSet<>();

    private final Map<String, JButton> actionButtons = new HashMap<>();

    private final Color defaultForeground = new JTextField().getForeground();

    private boolean autosize = false;

    private boolean center = false;

    public FormDialog(Frame owner, boolean modal) {
        super(owner, "", modal);

        setIconImages(SvgIconManager.getBuiltinWindowIconImages(SvgIcons.SVGICON_JOURNAL_ALBUM, "#10171c"));
        setPreferredSize(new Dimension(1, 1));
        setMinimumSize(new Dimension(1, 1));
        setResizable(true);

        mainPanel = new JPanel(new MigLayout(LAYOUT_CONSTRAINTS));

        itemPanel = new JPanel(new MigLayout(LAYOUT_CONSTRAINTS));

        actionPanel = new JPanel(new MigLayout(LAYOUT_CONSTRAINTS));
        actionPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

        var itemScrollPane = new JScrollPane(itemPanel);

        itemScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

        mainPanel.add(itemScrollPane, "span, pushx, growx, pushy, growy");

        confirmButton.addActionListener(e -> dispose());
        cancelButton.addActionListener(e -> dispose());

        getRootPane().setDefaultButton(confirmButton);
        getContentPane().add(mainPanel);
        pack();
    }

    public FormDialog(Frame owner) {
        this(owner, true);
    }

    public FormDialog title(String title) {
        setTitle(title);
        return this;
    }

    public FormDialog confirmButton() {
        confirmButton.setIcon(SvgIconManager.getBuiltinIcon(SvgIcons.SVGICON_CHECK2_CIRCLE, new Dimension(16, 16), "#fff"));
        actionButtons.put("CONFIRMBUTTON", confirmButton);
        return this;
    }

    public FormDialog confirmButton(String text) {
        confirmButton.setText(text);
        return confirmButton();
    }

    public FormDialog cancelButton() {
        cancelButton.setIcon(SvgIconManager.getBuiltinIcon(SvgIcons.SVGICON_DOOR_CLOSED, new Dimension(16, 16), "#d42700"));
        actionButtons.put("CANCELBUTTON", cancelButton);
        return this;
    }

    public FormDialog cancelButton(String text) {
        cancelButton.setText(text);
        return cancelButton();
    }

    public FormDialog beginGroup(String caption) {
        groupPanel = new JPanel(new MigLayout(LAYOUT_CONSTRAINTS));
        groupPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createSoftBevelBorder(BevelBorder.RAISED), format("<html><b>%s</b></html>", caption), TitledBorder.RIGHT, TitledBorder.DEFAULT_JUSTIFICATION));
        return this;
    }

    public FormDialog endGroup() {
        itemPanel.add(groupPanel, "span, pushx, growx");
        groupPanel = null;
        return this;
    }

    public FormDialog label(String name, String caption, String value, String tooltip, int widthInPixel) {
        JLabel label = new JLabel(value);
        label.setToolTipText(tooltip);

        if (groupPanel != null) {
            groupPanel.add(new JLabel(caption), "right, pushx");
            groupPanel.add(label, format("w %d, wrap", widthInPixel));
        } else {
            itemPanel.add(new JLabel(caption), "right, pushx");
            itemPanel.add(label, format("w %d, wrap", widthInPixel));
        }

        return this;
    }

    public FormDialog textfield(String name, String caption, String initialValue, String tooltip, int widthInPixel, Verifier verifier) {
        values.put(name, initialValue);

        JTextField textField = new JTextField();
        textField.setText(initialValue);
        textField.setToolTipText(tooltip);
        verifyTextField(textField, verifier);

        textField.addKeyListener(new TextFieldKeyListener(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                return verifyTextField((JTextField) input, verifier);
            }
        }) {
            @Override
            public void keyReleased(KeyEvent e) {
                inputVerifier.verify((JTextField) e.getSource());
            }
        });

        textField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                values.put(name, textField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                values.put(name, textField.getText());
            }
        });

        if (groupPanel != null) {
            groupPanel.add(new JLabel(caption), "right, pushx");
            groupPanel.add(textField, format("w %dpx, wrap", widthInPixel));
        } else {
            itemPanel.add(new JLabel(caption), "right, pushx");
            itemPanel.add(textField, format("w %dpx, wrap", widthInPixel));
        }

        return this;
    }

    public FormDialog textfield(String name, String caption, String initialValue, String tooltip, int widthInPixel) {
        return textfield(name, caption, initialValue, tooltip, widthInPixel, value -> true);
    }

    public FormDialog combobox(String name, String caption, int widthInPixel, String selectedValue, String[] items, ValueSupplier valueSupplier) {
        values.put(name, selectedValue);

        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setSelectedItem(selectedValue);
        comboBox.addActionListener(e -> {
            values.put(name, comboBox.getItemAt(comboBox.getSelectedIndex()));
            valueSupplier.supply(values);
        });

        if (groupPanel != null) {
            groupPanel.add(new JLabel(caption), "right, pushx");
            groupPanel.add(comboBox, format("w %d, wrap", widthInPixel));
        } else {
            itemPanel.add(new JLabel(caption), "right, pushx");
            itemPanel.add(comboBox, format("w %d, wrap", widthInPixel));
        }

        return this;
    }

    public FormDialog combobox(String name, String caption, int widthInPixel, String selectedValue, String[] items) {
        combobox(name, caption, widthInPixel, selectedValue, items, (values) -> {});
        return this;
    }

    public FormDialog onConfirm(ValueSupplier valueSupplier) {
        confirmButton.addActionListener(e -> valueSupplier.supply(values));
        return this;
    }

    public FormDialog onCancel(DialogSupplier dialogSupplier) {
        cancelButton.addActionListener(e -> dialogSupplier.supply(this));
        return this;
    }

    public FormDialog onCancel(Runnable runnable) {
        cancelButton.addActionListener(e -> runnable.run());
        return this;
    }

    public FormDialog withPreferredSize(Dimension preferredSize) {
        setPreferredSize(preferredSize);
        return this;
    }

    public FormDialog withMinimumSize(Dimension minimumSize) {
        setMinimumSize(minimumSize);
        return this;
    }

    public FormDialog center() {
        center = true;
        return this;
    }

    public FormDialog autosize() {
        autosize = true;
        return this;
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            handleActionButtons();
            handleAutoSizing();
            handleCenter();
        }
        super.setVisible(b);
    }

    private boolean verifyTextField(JTextField textField, Verifier verifier) {
        boolean verified = verifier.verify(textField.getText());
        if (!verified) {
            textField.setForeground(Color.RED);
            invalidTextFields.add(textField);
        } else {
            textField.setForeground(defaultForeground);
            invalidTextFields.remove(textField);
        }
        updateConfirmButtonState();
        return verified;
    }

    private void updateConfirmButtonState() {
        confirmButton.setEnabled(invalidTextFields.isEmpty());
    }

    private void handleActionButtons() {
        // show action panel when there are action buttons
        if (!actionButtons.isEmpty()) {
            if (actionButtons.containsKey("CANCELBUTTON")) {
                actionPanel.add(cancelButton, "right, pushx");
            }
            if (actionButtons.containsKey("CONFIRMBUTTON")) {
                if (actionButtons.containsKey("CANCELBUTTON")) {
                    actionPanel.add(confirmButton, "right");
                } else {
                    actionPanel.add(confirmButton, "right, pushx");
                }
            }
            mainPanel.add(actionPanel, "pushx, growx");
            pack();
        }
    }

    private void handleAutoSizing() {
        if (!autosize) return;
        // so macht "man" das im Jahre 2022! Ne, Scherz, ich weiß es nicht besser :)
        // das ist vermutlich sogar abhängig vom LAF und scheitert in Situationen, die ich mir jetzt nicht vorstellen kann
        int height = getHeight() + getRootPane().getHeight() + itemPanel.getHeight() + actionPanel.getHeight() - 6;
        int width = itemPanel.getWidth() + 40;
        setPreferredSize(new Dimension(width, height));
        pack();
    }

    private void handleCenter() {
        GraphicsDevice screen = MouseInfo.getPointerInfo().getDevice();
        Rectangle r = screen.getDefaultConfiguration().getBounds();
        int x = (r.width - this.getWidth()) / 2 + r.x;
        int y = (r.height - this.getHeight()) / 2 + r.y;
        setLocation(x, y);
    }

    private String localize(String key) {
        ResourceBundle bundle = ResourceBundle.getBundle(format("%s/formdialog/i18n/MessagesBundle", getClass().getPackageName().replace(".", "/")));
        return bundle.getString(key);
    }
}
