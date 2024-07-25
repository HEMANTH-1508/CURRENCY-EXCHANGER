import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CurrencyConverterApp {

    private JFrame mainFrame;
    private JPanel mainPanel;
    private JTextField amountField;
    private JComboBox<String> currencyFromComboBox;
    private JComboBox<String> currencyToComboBox;
    private JButton convertButton;
    private JButton addCurrencyButton;
    private JButton updateCurrencyButton;
    private JTextArea resultArea;
    private CurrencyManager currencyManager;
    private JTextField newCurrencyField;
    private JTextField exchangeRateField;
    private DefaultComboBoxModel<String> currencyFromModel;
    private DefaultComboBoxModel<String> currencyToModel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CurrencyConverterApp().initUI());
    }

    private void initUI() {
        // Initialize components
        mainFrame = new JFrame("Currency Converter");
        mainPanel = new JPanel();
        amountField = new JTextField(10);
        currencyFromComboBox = new JComboBox<>();
        currencyToComboBox = new JComboBox<>();
        convertButton = new JButton("Convert");
        addCurrencyButton = new JButton("Add Currency");
        updateCurrencyButton = new JButton("Update Currency");
        resultArea = new JTextArea(5, 20);
        newCurrencyField = new JTextField(10);
        exchangeRateField = new JTextField(10);
        currencyManager = new CurrencyManager();
        currencyFromModel = new DefaultComboBoxModel<>();
        currencyToModel = new DefaultComboBoxModel<>();

        currencyFromComboBox.setModel(currencyFromModel);
        currencyToComboBox.setModel(currencyToModel);

        // Load saved currencies
        currencyManager.loadCurrencies();

        // Layout and add components
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(amountField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("From Currency:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(currencyFromComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("To Currency:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(currencyToComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("New Currency:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(newCurrencyField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("Exchange Rate (to USD):"), gbc);
        gbc.gridx = 1;
        mainPanel.add(exchangeRateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        mainPanel.add(addCurrencyButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        mainPanel.add(updateCurrencyButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        mainPanel.add(convertButton, gbc);

        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(mainPanel, BorderLayout.CENTER);
        mainFrame.add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        // Set up frame
        mainFrame.pack();
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);

        // Update currency combos
        updateCurrencyCombos();

        // Add action listeners
        addActionListeners();
    }

    private void updateCurrencyCombos() {
        SwingUtilities.invokeLater(() -> {
            currencyFromModel.removeAllElements();
            currencyToModel.removeAllElements();
            for (String currencyName : currencyManager.getCurrencyNames()) {
                currencyFromModel.addElement(currencyName);
                currencyToModel.addElement(currencyName);
            }
        });
    }

    private void addActionListeners() {
        convertButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    double amount = Double.parseDouble(amountField.getText());
                    String from = (String) currencyFromComboBox.getSelectedItem();
                    String to = (String) currencyToComboBox.getSelectedItem();
                    double result = currencyManager.convertCurrency(amount, from, to);
                    resultArea.setText(String.format("Conversion Result:\n%.2f %s = %.2f %s",
                            amount, from, result, to));
                } catch (NumberFormatException ex) {
                    resultArea.setText("Invalid amount entered.");
                }
            }
        });

        addCurrencyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String newCurrency = newCurrencyField.getText().trim().toUpperCase();
                    double exchangeRate = Double.parseDouble(exchangeRateField.getText().trim());
                    if (!newCurrency.isEmpty() && exchangeRate > 0) {
                        currencyManager.addCurrency(newCurrency, exchangeRate);
                        updateCurrencyCombos();
                        currencyManager.saveCurrencies(); // Save currencies to file
                        newCurrencyField.setText("");
                        exchangeRateField.setText("");
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, "Invalid currency or exchange rate.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(mainFrame, "Invalid exchange rate format.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        updateCurrencyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String currency = (String) currencyFromComboBox.getSelectedItem();
                    if (currency != null) {
                        String newRateString = exchangeRateField.getText().trim();
                        if (!newRateString.isEmpty()) {
                            double newRate = Double.parseDouble(newRateString);
                            if (newRate > 0) {
                                currencyManager.addCurrency(currency, newRate); // Update rate
                                updateCurrencyCombos();
                                currencyManager.saveCurrencies(); // Save updated currencies to file
                                newCurrencyField.setText("");
                                exchangeRateField.setText("");
                            } else {
                                JOptionPane.showMessageDialog(mainFrame, "Invalid exchange rate.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(mainFrame, "Exchange rate cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, "No currency selected to update.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(mainFrame, "Invalid exchange rate format.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}

class CurrencyManager {
    private static final String FILE_NAME = "currencies.dat";
    private Map<String, Double> currencies = new HashMap<>();

    public void addCurrency(String name, double rate) {
        currencies.put(name.toUpperCase(), rate);
    }

    public Double getCurrencyRate(String name) {
        return currencies.get(name.toUpperCase());
    }

    public double convertCurrency(double amount, String from, String to) {
        Double fromRate = getCurrencyRate(from);
        Double toRate = getCurrencyRate(to);
        if (fromRate != null && toRate != null) {
            return amount * (toRate / fromRate);
        }
        return 0.0;
    }

    public Set<String> getCurrencyNames() {
        return currencies.keySet();
    }

    public void loadCurrencies() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            currencies = (Map<String, Double>) ois.readObject();
        } catch (FileNotFoundException e) {
            // File not found is expected on the first run
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void saveCurrencies() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(currencies);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
