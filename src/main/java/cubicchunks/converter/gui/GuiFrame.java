/*
 *  This file is part of CubicChunksConverter, licensed under the MIT License (MIT).
 *
 *  Copyright (c) 2017 contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package cubicchunks.converter.gui;

import cubicchunks.converter.lib.Utils;
import cubicchunks.converter.lib.anvil2cc.Anvil2CCDataConverter;
import cubicchunks.converter.lib.anvil2cc.Anvil2CCLevelInfoConverter;
import cubicchunks.converter.lib.anvil2cc.AnvilChunkReader;
import cubicchunks.converter.lib.anvil2cc.CubicChunkWriter;
import cubicchunks.converter.lib.convert.WorldConverter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class GuiFrame extends JFrame {

    private boolean isConverting = false;
    private boolean hasChangedDst;

    private JButton convertBtn;
    private JProgressBar progressBar;
    private JProgressBar convertFill, ioFill;

    private JTextField srcPathField;
    private JTextField dstPathField;

    public void init() {
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(640, 0));

        JPanel root = new JPanel(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());

        JPanel selection = new JPanel(new GridBagLayout());
        addSelectFilePanel(selection, false);
        addSelectFilePanel(selection, true);

        convertBtn = new JButton("Convert");
        progressBar = new JProgressBar();
        convertFill = new JProgressBar();
        ioFill = new JProgressBar();

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(selection, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(convertBtn, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(progressBar, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(convertFill, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(ioFill, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel label = new JLabel("WARNING: USE ON MODDED WORLDS AT YOUR OWN RISK! THIS IS NOT DESIGNED TO HANDLE THEM!");
        label.setForeground(Color.RED);
        mainPanel.add(label, gbc);

        root.add(mainPanel, BorderLayout.CENTER);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        updateConvertBtn();
        convertBtn.addActionListener(x -> convert());

        progressBar.setPreferredSize(new Dimension(100, (int) convertBtn.getPreferredSize().getHeight()));
        convertFill.setPreferredSize(new Dimension(100, (int) convertBtn.getPreferredSize().getHeight()));
        ioFill.setPreferredSize(new Dimension(100, (int) convertBtn.getPreferredSize().getHeight()));

        this.add(root);

        this.pack();
        this.setMinimumSize(new Dimension(200, this.getHeight()));
        this.setTitle("CubicChunks Save Converter");
        this.setVisible(true);

    }

    private void addSelectFilePanel(JPanel panel, boolean isSrc) {
        JLabel label = new JLabel(isSrc ? "Source: " : "Destination: ");

        Path srcPath = Utils.getApplicationDirectory().resolve("saves").resolve("New World");
        Path dstPath = getDstForSrc(srcPath);
        JTextField path = new JTextField((isSrc ? srcPath : dstPath).toString());
        if (isSrc) {
            this.srcPathField = path;
        } else {
            this.dstPathField = path;
        }
        JButton selectBtn = new JButton("...");

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridy = isSrc ? 0 : 1;

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(label, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(path, gbc);

        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(selectBtn, gbc);

        selectBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileHidingEnabled(false);
            chooser.setCurrentDirectory(getDefaultSaveLocation().toFile());
            int result = chooser.showDialog(this, "Select");
            if (result == JFileChooser.APPROVE_OPTION) {
                onSelectLocation(isSrc, chooser);
            }
        });
        selectBtn.setPreferredSize(new Dimension(30, (int) path.getPreferredSize().getHeight()));
        selectBtn.setMinimumSize(new Dimension(30, (int) path.getPreferredSize().getHeight()));

        path.getDocument().addDocumentListener(new DocumentListener() {

            @Override public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override public void changedUpdate(DocumentEvent e) {
                update();
            }

            private void update() {
                if (!isSrc) {
                    hasChangedDst = true;
                }
                updateConvertBtn();
            }
        });


        label.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    private void updateConvertBtn() {
        convertBtn.setEnabled(!isConverting && Utils.isValidPath(dstPathField.getText()) && Utils.fileExists(srcPathField.getText()));
    }

    private void onSelectLocation(boolean isSrc, JFileChooser chooser) {
        Path file = chooser.getSelectedFile().toPath();

        if (isSrc) {
            srcPathField.setText(file.toString());
            if (!hasChangedDst) {
                dstPathField.setText(getDstForSrc(file).toString());
            }
        } else {
            dstPathField.setText(file.toString());
            hasChangedDst = true;
        }
        updateConvertBtn();
    }

    private Path getDstForSrc(Path src) {
        return src.getParent().resolve(src.getFileName().toString() + " - CubicChunks");
    }

    private Path getDefaultSaveLocation() {
        return Utils.getApplicationDirectory().resolve("saves");
    }

    private void convert() {
        if (!Utils.fileExists(srcPathField.getText())) {
            updateConvertBtn();
            return;
        }
        Path srcPath = Paths.get(srcPathField.getText());
        Path dstPath;
        try {
            dstPath = Paths.get(dstPathField.getText());
        } catch (InvalidPathException e) {
            updateConvertBtn();
            return;
        }
        progressBar.setStringPainted(true);
        convertFill.setStringPainted(true);
        ioFill.setStringPainted(true);
        isConverting = true;
        updateConvertBtn();
        WorldConverter<?, ?> converter = new WorldConverter<>(
            new Anvil2CCLevelInfoConverter(srcPath, dstPath),
            new AnvilChunkReader(srcPath),
            new Anvil2CCDataConverter(),
            new CubicChunkWriter(dstPath)
        );
        ConverterWorker w = new ConverterWorker(converter, progressBar, convertFill, ioFill, () -> {
            isConverting = false;
            progressBar.setString("Done!");
            progressBar.setValue(0);
            updateConvertBtn();
        });
        w.execute();
    }
}
