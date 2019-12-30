package prg2.p2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.io.*;

public class UI extends JPanel {
    static boolean isLoaded;
    static int rows;
    static int columns;
    static Page page;
    static JFrame window;
    static JMenuBar menu;
    static JMenu archivo;
    static JMenu editar;
    static JMenuItem cargar;
    static JMenuItem nuevo;
    static JMenuItem guardar;
    static JMenuItem deshacer ;
    static JMenuItem rehacer;
    static JTable table;
    static JButton solve;
    static JLabel cell;
    static JFileChooser explorer = new JFileChooser();

    public static void main(String[] args){
        if(!isLoaded) getTableSize();
        showPage();

    }

    private static void initializeElements(){
        menu = new JMenuBar();

        archivo = new JMenu("Archivo");
        editar = new JMenu("Editar");

        cell = new JLabel();

        solve = new JButton("=");
        solve.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                solveTable();
            }
        });

        nuevo = new JMenuItem("Nuevo Archivo");

        nuevo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                isLoaded = false;
                window.dispose();
                UI.main(null);
            }
        });

        cargar = new JMenuItem("Cargar");
        cargar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                loadTable();
            }
        });

        guardar = new JMenuItem("Guardar");
        guardar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    saveTable();
                } catch (IOException e) {
                    showError("Error while saving");
                }
            }
        });

        deshacer = new JMenuItem("Deshacer");
        rehacer = new JMenuItem("Rehacer");
    }

    private static void solveTable() {

        String table = tableToString();
        Page page = new Page(rows,columns,table);


    }

    private static void getTableSize() {
        try {
            rows = Integer.parseInt(JOptionPane.showInputDialog("Filas:"));
        } catch (Exception e) {
            showError("Error parsing number, please introduce a valid number value");
        }

        try {
            columns = Integer.parseInt(JOptionPane.showInputDialog("Columnas:"));
        } catch (Exception e) {
            System.err.println("Invalid input");
        }
    }

    private static void showPage(){
        window = new JFrame("Hoja de Cálculo");
        window.setSize(600,400);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        initializeElements();
        window.add(cell,BorderLayout.SOUTH);
        window.add(solve,BorderLayout.NORTH);
        window.setJMenuBar(menu);
        archivo.add(guardar);
        archivo.add(cargar);
        archivo.add(nuevo);
        editar.add(deshacer);
        editar.add(rehacer);
        menu.add(archivo);
        menu.add(editar);

        drawTable(columns,rows);

        window.setVisible(true);

    }

    private static void drawTable(int columns, int rows){
        DefaultTableModel tm = new DefaultTableModel(rows,columns+1) {
            @Override
            public boolean isCellEditable(int row, int column) {

                if(0 == column)
                    return false;
                return super.isCellEditable(row,column);
            }


        };

        for(int  i=0; i<rows; i++)
            tm.setValueAt(i+1, i, 0);

        table = new JTable(tm) {
            @Override
            public void changeSelection(int rowIndex, int columnIndex,
                                        boolean toggle, boolean extend) {
                if(columnIndex == 0)
                    super.changeSelection(rowIndex, columnIndex+1, toggle, extend);
                else
                    super.changeSelection(rowIndex, columnIndex, toggle, extend);
            }
        };

        String[] identifiers = new String[columns+1];
        for(int i= 1; i<table.getColumnCount();i++){
            identifiers[i] = table.getColumnName(i-1);
        }
        identifiers[0] = "";

        tm.setColumnIdentifiers(identifiers);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setCellRenderer(table.getTableHeader().getDefaultRenderer());
        table.setAutoResizeMode(0);
        window.add(new JScrollPane(table));

        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                getClickedCell();
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                getClickedCell();
            }
        });

    }

    private static void saveTable() throws IOException {
        explorer.setDialogTitle("Guardar Archivo");
        File fileToSave = null;
        StringBuffer data = new StringBuffer();

        data.append(rows).append(" ").append(columns).append("\n");
        System.out.println(tableToString());
        data.append(tableToString());

        int userSelection = explorer.showSaveDialog(window);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            fileToSave = explorer.getSelectedFile();
            System.out.println("Save as file: " + fileToSave.getAbsolutePath());
        }

        System.out.println(fileToSave);
        assert fileToSave != null;
        FileWriter fw = new FileWriter(fileToSave);
        System.out.println(fw);
        try{
            BufferedWriter bw = new BufferedWriter(fw);
            System.out.println(bw);
            bw.write(data.toString());
            bw.flush();
            bw.close();
        }
        catch(Exception e){
            showError("Error while saving");
        }
    }

    private static String tableToString(){

        StringBuilder out = new StringBuilder();

        for (int i=0;i<table.getRowCount();i++){
            for (int j=1; j<table.getColumnCount();j++){
                if (table.getValueAt(i,j) == null){
                    out.append(0);
                }else{
                    out.append(table.getValueAt(i,j));
                }
                if (j == table.getColumnCount()-1) break;
                out.append(" ");
            }
            if (i == table.getRowCount()-1) break;
            out.append("\n");
        }
        return out.toString();
    }

    private static void loadTable(){
        explorer.setDialogTitle("Cargar Archivo");
        File fileToLoad = null;
        StringBuffer file = new StringBuffer();
        isLoaded = true;

        int userSelection = explorer.showOpenDialog(window);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            fileToLoad = explorer.getSelectedFile();
            System.out.println("Load file: " + fileToLoad.getAbsolutePath());
        }

        try{
            assert fileToLoad != null;
            FileReader fr = new FileReader(fileToLoad);
            BufferedReader br = new BufferedReader(fr);

            rows = br.read()-48; //ACII to number
            br.skip(1); //Skips space char
            columns = br.read()-48;

            while(br.ready()){
                System.out.println(br.readLine());
            }

        }catch(Exception e){
            showError("Error while reading");
        }
    }

    private static void getClickedCell(){
        int rowSelected = table.getSelectedRow();
        int colSelected = table.getSelectedColumn();
        cell.setText("Celda: "+table.getColumnName(colSelected)+(rowSelected+1));
    }

    public static void showError(String error){
        JOptionPane.showMessageDialog(null,error,"",JOptionPane.ERROR_MESSAGE);
        System.exit(-1);
    }
}

