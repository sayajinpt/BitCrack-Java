package org.example;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;

public class Btc_Gen {

    private static SecureRandom random = new SecureRandom();
    private static final String FOUND_FILE = "found.txt"; // Path to the output file
    private static final String SAVE_FILE = "save.txt";   // Path to the save file

    private static Set<String> addressSet;
    private static AtomicLong keysProcessed = new AtomicLong();
    private static BigInteger keyStart = BigInteger.ZERO;
    private static BigInteger keyEnd = BigInteger.ZERO;
    private static BigInteger currentKey;
    private static BigInteger incrementValue = BigInteger.ONE;
    private static boolean isIncremental = false;
    private static ExecutorService executor;
    private static AtomicBoolean isRunning = new AtomicBoolean(false);

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("BitCrack Java");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLayout(new BorderLayout());

            // Dark Theme Colors
            Color backgroundColor = new Color(30, 30, 30);
            Color foregroundColor = Color.WHITE;
            Color accentColor = new Color(60, 120, 180); // Light blue for accents

            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout());
            panel.setBackground(backgroundColor);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;

            JLabel coreLabel = new JLabel("Threads:");
            coreLabel.setForeground(foregroundColor);
            panel.add(coreLabel, gbc);

            JTextField coreInput = new JTextField("4");
            coreInput.setBackground(new Color(50, 50, 50));
            coreInput.setForeground(foregroundColor);
            gbc.gridx = 1;
            panel.add(coreInput, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            JLabel startKeyLabel = new JLabel("Start Key (Hex):");
            startKeyLabel.setForeground(foregroundColor);
            panel.add(startKeyLabel, gbc);

            JTextField startKeyInput = new JTextField("0000000000000000000000000000000000000000000000020000000000000000");
            startKeyInput.setBackground(new Color(50, 50, 50));
            startKeyInput.setForeground(foregroundColor);
            gbc.gridx = 1;
            panel.add(startKeyInput, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            JLabel endKeyLabel = new JLabel("End Key (Hex):");
            endKeyLabel.setForeground(foregroundColor);
            panel.add(endKeyLabel, gbc);

            JTextField endKeyInput = new JTextField("000000000000000000000000000000000000000000000003ffffffffffffffff");
            endKeyInput.setBackground(new Color(50, 50, 50));
            endKeyInput.setForeground(foregroundColor);
            gbc.gridx = 1;
            panel.add(endKeyInput, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            JLabel modeLabel = new JLabel("Mode:");
            modeLabel.setForeground(foregroundColor);
            panel.add(modeLabel, gbc);

            JComboBox<String> modeComboBox = new JComboBox<>(new String[]{"Random", "Incremental"});
            modeComboBox.setBackground(new Color(50, 50, 50));
            modeComboBox.setForeground(foregroundColor);
            gbc.gridx = 1;
            panel.add(modeComboBox, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            JLabel incrementLabel = new JLabel("Increment by:");
            incrementLabel.setForeground(foregroundColor);
            panel.add(incrementLabel, gbc);

            JTextField incrementInput = new JTextField("1");
            incrementInput.setBackground(new Color(50, 50, 50));
            incrementInput.setForeground(foregroundColor);
            gbc.gridx = 1;
            panel.add(incrementInput, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            JLabel fileLabel = new JLabel("Address File:");
            fileLabel.setForeground(foregroundColor);
            panel.add(fileLabel, gbc);

            JTextField fileInput = new JTextField();
            fileInput.setBackground(new Color(50, 50, 50));
            fileInput.setForeground(foregroundColor);
            gbc.gridx = 1;
            panel.add(fileInput, gbc);

            gbc.gridx = 2;
            JButton fileButton = new JButton("Browse");
            fileButton.setBackground(accentColor);
            fileButton.setForeground(foregroundColor);
            panel.add(fileButton, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            JLabel singleAddressLabel = new JLabel("Single Address:");
            singleAddressLabel.setForeground(foregroundColor);
            panel.add(singleAddressLabel, gbc);

            JTextField singleAddressInput = new JTextField();
            singleAddressInput.setBackground(new Color(50, 50, 50));
            singleAddressInput.setForeground(foregroundColor);
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            panel.add(singleAddressInput, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            gbc.gridwidth = 1;
            JLabel addressTypeLabel = new JLabel("Address Type:");
            addressTypeLabel.setForeground(foregroundColor);
            panel.add(addressTypeLabel, gbc);

            JComboBox<String> addressTypeComboBox = new JComboBox<>(new String[]{"Compressed", "Uncompressed", "Both"});
            addressTypeComboBox.setBackground(new Color(50, 50, 50));
            addressTypeComboBox.setForeground(foregroundColor);
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            panel.add(addressTypeComboBox, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            gbc.gridwidth = 3;
            JButton startButton = new JButton("Start");
            startButton.setBackground(accentColor);
            startButton.setForeground(foregroundColor);
            panel.add(startButton, gbc);

            gbc.gridy++;
            JLabel statusLabel = new JLabel("Keys processed: 0");
            statusLabel.setForeground(foregroundColor);
            panel.add(statusLabel, gbc);

            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setBackground(new Color(20, 20, 20));
            textArea.setForeground(foregroundColor);
            JScrollPane scrollPane = new JScrollPane(textArea);

            frame.add(panel, BorderLayout.NORTH);
            frame.add(scrollPane, BorderLayout.CENTER);

            modeComboBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean isIncrementalSelected = modeComboBox.getSelectedItem().equals("Incremental");
                    incrementLabel.setVisible(isIncrementalSelected);
                    incrementInput.setVisible(isIncrementalSelected);
                }
            });

            modeComboBox.setSelectedItem("Random");

            fileButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    int returnValue = fileChooser.showOpenDialog(frame);
                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        fileInput.setText(selectedFile.getAbsolutePath());
                    }
                }
            });

            startButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isRunning.get()) {
                        // Stop the process
                        isRunning.set(false);
                        executor.shutdownNow();
                        startButton.setText("Start");
                    } else {
                        int numCores;
                        try {
                            numCores = Integer.parseInt(coreInput.getText());
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(frame, "Invalid number of cores.");
                            return;
                        }

                        try {
                            keyStart = new BigInteger(startKeyInput.getText(), 16);
                            keyEnd = new BigInteger(endKeyInput.getText(), 16);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(frame, "Invalid key format.");
                            return;
                        }

                        if (keyStart.compareTo(keyEnd) >= 0) {
                            JOptionPane.showMessageDialog(frame, "Start key must be less than end key.");
                            return;
                        }

                        if (modeComboBox.getSelectedItem().equals("Incremental")) {
                            try {
                                incrementValue = new BigInteger(incrementInput.getText(), 16);
                                if (incrementValue.compareTo(BigInteger.ZERO) <= 0) {
                                    JOptionPane.showMessageDialog(frame, "Increment value must be greater than 0.");
                                    return;
                                }
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(frame, "Invalid increment value.");
                                return;
                            }
                        }

                        String filePath = fileInput.getText();
                        String singleAddress = singleAddressInput.getText().trim();

                        if (!filePath.isEmpty() && !singleAddress.isEmpty()) {
                            JOptionPane.showMessageDialog(frame, "Please provide either an address file or a single address, not both.");
                            return;
                        }

                        if (filePath.isEmpty() && singleAddress.isEmpty()) {
                            JOptionPane.showMessageDialog(frame, "Please select an address file or input a single address.");
                            return;
                        }

                        if (!filePath.isEmpty()) {
                            addressSet = loadAddressesFromFile(filePath);
                            if (addressSet == null) {
                                JOptionPane.showMessageDialog(frame, "Failed to load addresses from file.");
                                return;
                            }
                        } else {
                            addressSet = new HashSet<>();
                            addressSet.add(singleAddress);
                        }

                        keysProcessed.set(0); // Reset counter
                        isIncremental = modeComboBox.getSelectedItem().equals("Incremental");

                        // Resume from save file if it exists
                        if (isIncremental && new File(SAVE_FILE).exists()) {
                            try (BufferedReader reader = new BufferedReader(new FileReader(SAVE_FILE))) {
                                currentKey = new BigInteger(reader.readLine().trim());
                            } catch (IOException ex) {
                                JOptionPane.showMessageDialog(frame, "Failed to read save file.");
                                ex.printStackTrace();
                                return;
                            }
                        } else {
                            currentKey = keyStart;
                        }

                        isRunning.set(true);
                        startButton.setText("Stop");

                        executor = Executors.newFixedThreadPool(numCores);
                        for (int i = 0; i < numCores; i++) {
                            executor.execute(createWorkerTask(textArea, statusLabel, addressTypeComboBox.getSelectedItem().toString()));
                        }
                    }
                }
            });

            frame.setVisible(true);
        });
    }

    private static Runnable createWorkerTask(JTextArea textArea, JLabel statusLabel, String addressType) {
        return () -> {
            NetworkParameters params = MainNetParams.get();
            DecimalFormat formatter = new DecimalFormat("#,###"); // Use comma as thousands separator

            while (isRunning.get()) {
                BigInteger privateKey;
                if (isIncremental) {
                    synchronized (currentKey) {
                        if (currentKey.compareTo(keyEnd) >= 0) {
                            isRunning.set(false);
                            break;
                        }
                        privateKey = currentKey;
                        currentKey = currentKey.add(incrementValue);
                    }

                    // Save the current state every minute
                    if (System.currentTimeMillis() % 60000 < 1000) {
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SAVE_FILE))) {
                            writer.write(currentKey.toString());
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                } else {
                    privateKey = generateRandomKeyInRange(keyStart, keyEnd);
                }

                ECKey key = ECKey.fromPrivate(privateKey);
                Address address = null;

                if (addressType.equals("Compressed") || addressType.equals("Both")) {
                    key = ECKey.fromPrivate(privateKey, true);
                    address = Address.fromKey(params, key, Script.ScriptType.P2PKH);
                    checkAndReportAddress(privateKey, address, textArea, statusLabel, formatter);
                }

                if (addressType.equals("Uncompressed") || addressType.equals("Both")) {
                    key = ECKey.fromPrivate(privateKey, false);
                    address = Address.fromKey(params, key, Script.ScriptType.P2PKH);
                    checkAndReportAddress(privateKey, address, textArea, statusLabel, formatter);
                }
            }
        };
    }

    private static void checkAndReportAddress(BigInteger privateKey, Address address, JTextArea textArea, JLabel statusLabel, DecimalFormat formatter) {
        String privateKeyHex = privateKey.toString(16);
        privateKeyHex = String.format("%064x", new BigInteger(privateKeyHex, 16));

        ///String res = privateKeyHex + " " + address;
        ///System.out.println(res);

        if (addressSet.contains(address.toString())) {
            String result = "Match found!\nPrivate Key (Hex): " + privateKeyHex + "\nAddress: " + address + "\n";
            SwingUtilities.invokeLater(() -> {
                textArea.append(result);
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(FOUND_FILE, true))) {
                    writer.write(result);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        }

        long count = keysProcessed.incrementAndGet();
        SwingUtilities.invokeLater(() -> statusLabel.setText("Keys processed: " + formatter.format(count)));
    }

    private static Set<String> loadAddressesFromFile(String fileName) {
        Set<String> addressSet = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                addressSet.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return addressSet;
    }

    private static BigInteger generateRandomKeyInRange(BigInteger start, BigInteger end) {
        BigInteger range = end.subtract(start);
        BigInteger randomKey;
        do {
            randomKey = new BigInteger(range.bitLength(), random);
        } while (randomKey.compareTo(range) >= 0 || randomKey.equals(BigInteger.ZERO));
        return randomKey.add(start);
    }
}














