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

import de.schipplock.gui.swing.lafmanager.LAFManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

public class FormDialogDemo extends JFrame {

    public FormDialogDemo() {
        setupFrame();
    }

    private void centerFrame() {
        GraphicsDevice screen = MouseInfo.getPointerInfo().getDevice();
        Rectangle r = screen.getDefaultConfiguration().getBounds();
        int x = (r.width - this.getWidth()) / 2 + r.x;
        int y = (r.height - this.getHeight()) / 2 + r.y;
        this.setLocation(x, y);
    }

    private void setupFrame() {
        setPreferredSize(new Dimension(300, 230));
        setMinimumSize(new Dimension(300, 230));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        new FormDialog(this, true)
                .title("Dialog")
                .confirmButton("Speichern")
                .cancelButton("Abbrechen")
                .beginGroup("Allgemeine Einstellungen")
                .textfield("LANGUAGE", "Sprache:", "de", "tooltip", 150, (value) -> {
                    return true;
                })
                .combobox("THEME", "Erscheinungsbild:", 150, "dark", LAFManager.create().getInstalledLookAndFeelNames())
                .endGroup()
                .beginGroup("Standardwerte für neue Einträge")
                .textfield("LIMIT", "kwH Grenze pro Eintrag:", "150w", "tooltip", 150, (value) -> {
                    try {
                        return Integer.parseInt(value) >= 0;
                    } catch (NumberFormatException ex) {
                        return false;
                    }
                })
                .textfield("PRICE", "Preis pro kwH in Cent:", "27.9", "tooltip", 150, (value) -> {
                    try {
                        return Double.parseDouble(value) >= 0;
                    } catch (NumberFormatException ex) {
                        return false;
                    }
                })
                .textfield("NOVERIFY", "Ohne Validierung", "egal", "tooltip", 150)
                .endGroup()
                .beginGroup("Nur ein paar Labels")
                .label("LABEL1", "<html><b>Alter Zählerstand:</b></html>", "123455", "tooltip", 150)
                .label("LABEL2", "<html><b>Neuer Zählerstand:</b></html>", "123555", "tooltip", 150)
                .endGroup()
                .label("LABEL3", "<html><b>Label ohne Gruppe:</b></html>", "jup, das geht auch", "tooltip", 150)
                .label("LABEL4", "<html><b>Label ohne Gruppe:</b></html>", "noch eins", "tooltip", 150)
                .onConfirm((values) -> {
                    values.forEach((componentName, value) -> {
                        System.out.printf("component %s has value %s%n", componentName, value);
                    });
                })
                .autosize()
                .center()
                .setVisible(true);

        centerFrame();
    }

    public static void createAndShowGui() {
        JFrame frame = new FormDialogDemo();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        LAFManager.create().setLookAndFeelByName("FlatLaf IntelliJ");
        SwingUtilities.invokeLater(FormDialogDemo::createAndShowGui);
    }
}
