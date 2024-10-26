import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

public class Lab05 {
    private JFrame frame;
    private JComboBox<String> filter1Combo, filter2Combo, filter3Combo, filter4Combo;
    private JTextArea resultArea;
    private List<DataEntry> dataEntries;

    public Lab05() {
        loadData();
        createUI();
    }

    private void createUI() {
        frame = new JFrame("CSV Analysis Tool");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new GridLayout(7, 2));

        filter1Combo = new JComboBox<>(getUniqueValues(0).toArray(new String[0]));
        filter2Combo = new JComboBox<>(getUniqueValues(1).toArray(new String[0]));
        filter3Combo = new JComboBox<>(getUniqueValues(2).toArray(new String[0]));
        filter4Combo = new JComboBox<>(getUniqueValues(3).toArray(new String[0]));

        JButton filterButton = new JButton("Filtrar");
        filterButton.addActionListener(e -> filterData());

        resultArea = new JTextArea();
        resultArea.setEditable(false);

        JButton downloadButton = new JButton("Descargar CSV");
        downloadButton.addActionListener(e -> downloadCSV());

        JButton chartButton = new JButton("Mostrar Gráfica");
        chartButton.addActionListener(e -> showChart());

        frame.add(new JLabel("Filtro 1:"));
        frame.add(filter1Combo);
        frame.add(new JLabel("Filtro 2:"));
        frame.add(filter2Combo);
        frame.add(new JLabel("Filtro 3:"));
        frame.add(filter3Combo);
        frame.add(new JLabel("Filtro 4:"));
        frame.add(filter4Combo);
        frame.add(filterButton);
        frame.add(downloadButton);
        frame.add(chartButton);
        frame.add(new JScrollPane(resultArea));

        frame.setVisible(true);
    }

    private void loadData() {
    dataEntries = new ArrayList<>();
    try (CSVReader reader = new CSVReader(new FileReader("datos.csv"))) {
        String[] line;
        while ((line = reader.readNext()) != null) {
            dataEntries.add(new DataEntry(line[0], line[1], line[2], line[3]));
        }
    } catch (IOException | com.opencsv.exceptions.CsvValidationException e) {
        e.printStackTrace();
    }
}


    private List<String> getUniqueValues(int columnIndex) {
        return dataEntries.stream()
                .map(entry -> entry.getValue(columnIndex))
                .distinct()
                .collect(Collectors.toList());
    }

    private void filterData() {
    String filter1 = (String) filter1Combo.getSelectedItem();
    String filter2 = (String) filter2Combo.getSelectedItem();
    String filter3 = (String) filter3Combo.getSelectedItem();
    String filter4 = (String) filter4Combo.getSelectedItem();

    Map<String, Long> resultCounts;
    

    resultCounts = dataEntries.stream()
            .filter(entry -> entry.matches(filter1, filter2, filter3, filter4))
            .collect(Collectors.groupingBy(entry -> entry.getValue(0), Collectors.counting()));

    resultArea.setText("Top 3 resultados:\n");
    resultCounts.entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
        .limit(3)
        .forEach(entry -> resultArea.append(entry.getKey() + ": " + entry.getValue() + "\n"));
}


    private void downloadCSV() {
        try (CSVWriter writer = new CSVWriter(new FileWriter("resultados.csv"))) {
            String[] header = {"Resultado", "Cantidad"};
            writer.writeNext(header);
           
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

private void showChart() {
    
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

   
    Map<String, Long> resultCounts = dataEntries.stream()
            .collect(Collectors.groupingBy(entry -> entry.getValue(0), Collectors.counting()));

    
    resultCounts.forEach((key, value) -> dataset.addValue(value, "Frecuencia", key));

    
    JFreeChart chart = ChartFactory.createBarChart(
            "Frecuencia de Valores", 
            "Valores", 
            "Cantidad", 
            dataset 
    );

   
    ChartPanel chartPanel = new ChartPanel(chart);
    JFrame chartFrame = new JFrame("Gráfica de datos CSV");
    chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    chartFrame.add(chartPanel);
    chartFrame.pack();
    chartFrame.setVisible(true);
}

class DataEntry {
    private String[] values;

    public DataEntry(String... values) {
        this.values = values;
    }

    public String getValue(int index) {
        return values[index];
    }

    public boolean matches(String... filters) {
        for (int i = 0; i < filters.length; i++) {
            if (filters[i] != null && !filters[i].equals(values[i])) {
                return false;
            }
        }
        return true;
    }
}