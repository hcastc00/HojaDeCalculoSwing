package prg2.p2;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.LinkedList;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

public class UI extends JPanel {
    static boolean isLoaded;
    static boolean isUndo;
    static int rows;
    static int columns;
    static LinkedList<String> state = new LinkedList<>();
    static int statePos = 0;
    static Page page;
    static JFrame window;
    static JMenuBar menu;
    static JMenu archivo;
    static JMenu editar;
    static JMenuItem cargar;
    static JMenuItem nuevo;
    static JMenuItem guardar;
    static JMenuItem deshacer;
    static JMenuItem rehacer;
    static JTable table;
    static JButton solve;
    static JLabel cell;
    static JFileChooser explorer = new JFileChooser();

    public static void main(String[] args) {
        if (!isLoaded) {
            getTableSize();
        }
        showPage();
        state.add(tableToString());

    }

    private static void initializeElements() {
        menu = new JMenuBar();

        archivo = new JMenu("Archivo");
        editar = new JMenu("Editar");

        cell = new JLabel();

        solve = new JButton("=");
        solve.addActionListener(actionEvent -> solveTable());

        nuevo = new JMenuItem("Nuevo Archivo");

        nuevo.setAccelerator(KeyStroke.getKeyStroke("control N"));
        nuevo.addActionListener(actionEvent -> newPage());

        cargar = new JMenuItem("Cargar");
        cargar.setAccelerator(KeyStroke.getKeyStroke("control L"));
        cargar.addActionListener(actionEvent -> loadTable());

        guardar = new JMenuItem("Guardar");
        guardar.setAccelerator(KeyStroke.getKeyStroke("control S"));
        guardar.addActionListener(actionEvent -> {
            try {
                saveTable();
            } catch (IOException e) {
                showError("Error while saving");
            }
        });

        deshacer = new JMenuItem("Deshacer");
        deshacer.setAccelerator(KeyStroke.getKeyStroke("control Z"));
        deshacer.addActionListener(actionEvent -> undo());
        rehacer = new JMenuItem("Rehacer");
        rehacer.setAccelerator(KeyStroke.getKeyStroke("control y"));
        rehacer.addActionListener(actionEvent -> redo());
    }

    private static void newPage() {
        isLoaded = false;
        state.clear();
        statePos = 0;
        window.dispose();
        columns = 0;
        rows = 0;
        UI.main(null);
    }

    private static void solveTable() {
        page = new Page(rows, columns, tableToString());
        page.solve();
        for (int i = 0; i < table.getRowCount(); i++) {
            for (int j = 1; j < table.getColumnCount(); j++) {
                isUndo = true;
                table.setValueAt(page.solution[i][j - 1], i, j);
            }
        }

        state.add(tableToString());
        statePos++;
    }

    private static void getTableSize() {
        while(rows<=0 || columns<=0 || rows>=99 || columns>18278) {
            try {
                columns = Integer.parseInt(JOptionPane.showInputDialog("Columnas:"));
            } catch (Exception e) {
                System.err.println("Please introduce a valid value");
                System.exit(-1);
            }

            try {
                rows = Integer.parseInt(JOptionPane.showInputDialog("Filas:"));
            } catch (Exception e) {
                showError("Please introduce a valid value");
                System.exit(-1);
            }

            if (rows <= 0 || columns <= 0) {
                showError("Page too small, must be at least 1x1");
            }

            if (rows > 999 || columns > 18278) {
                showError("Page too big, must be max 18278x999");
            }
        }
    }

    private static void showPage() {
        window = new JFrame("Hoja de Cálculo");
        window.setSize(600, 400);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        initializeElements();
        window.add(cell, BorderLayout.SOUTH);
        window.add(solve, BorderLayout.NORTH);
        window.setJMenuBar(menu);
        archivo.add(guardar);
        archivo.add(cargar);
        archivo.add(nuevo);
        editar.add(deshacer);
        editar.add(rehacer);
        menu.add(archivo);
        menu.add(editar);

        drawTable(columns, rows);

        window.setVisible(true);

    }

    private static void drawTable(int columns, int rows) {
        DefaultTableModel tm = new DefaultTableModel(rows, columns + 1) {
            @Override
            public boolean isCellEditable(int row, int column) {

                if (0 == column)
                    return false;
                return super.isCellEditable(row, column);
            }
        };

        for (int i = 0; i < rows; i++)
            tm.setValueAt(i + 1, i, 0);

        table = new JTable(tm) {
            @Override
            public void changeSelection(int rowIndex, int columnIndex,
                                        boolean toggle, boolean extend) {
                if (columnIndex == 0)
                    super.changeSelection(rowIndex, columnIndex + 1, toggle, extend);
                else
                    super.changeSelection(rowIndex, columnIndex, toggle, extend);
            }

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component componenet = super.prepareRenderer(renderer, row, column);

                if(getValueAt(row, column).toString().charAt(0) == '=') {
                    componenet.setBackground(Color.PINK);
                }else if(column!=0){
                    componenet.setBackground(Color.WHITE);
                }

                return componenet;
            }
        };

        String[] identifiers = new String[columns + 1];
        for (int i = 1; i < table.getColumnCount(); i++) {
            identifiers[i] = table.getColumnName(i - 1);
        }
        identifiers[0] = "";

        for (int i = 0; i < table.getRowCount(); i++) {
            for (int j = 1; j < table.getColumnCount(); j++) {
                table.setValueAt(0, i, j);
            }
        }

        tm.setColumnIdentifiers(identifiers);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setCellRenderer(table.getTableHeader().getDefaultRenderer());
        table.setRowHeight(50);

        //Alinear texto al centro

        table.setSelectionMode(SINGLE_SELECTION);
        window.add(new JScrollPane(table),BorderLayout.CENTER);

        //Listeners para indicar la celda
        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                getSelectedCell();
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                getSelectedCell();
            }
        });

        //Listener para guardar entados
        table.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent tableModelEvent) {

                //Cambio hecho a mano (NO UNDO O REDO)
                if(!isUndo){
                    //ELIMINA LOS REDO AL HACER UN CAMBIO MANUAL
                    for(int i = state.size()-statePos-1 ; i>0; i--){
                        System.out.println(state);
                        state.removeLast();
                    }
                    System.out.println("Cambio manual");
                    state.add(tableToString());
                    statePos++;


                    System.out.println(state);
                }else {
                    isUndo = false;
                }

            }
        });
    }

    private static void saveTable() throws IOException {
        explorer.setDialogTitle("Guardar Archivo");
        File fileToSave = null;
        StringBuilder data = new StringBuilder();

        data.append(columns).append(" ").append(rows).append("\n");
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
        try {
            BufferedWriter bw = new BufferedWriter(fw);
            System.out.println(bw);
            bw.write(data.toString());
            bw.flush();
            bw.close();
        } catch (Exception e) {
            showError("Error while saving");
        }
    }

    private static String tableToString() {

        StringBuilder out = new StringBuilder();

        for (int i = 0; i < table.getRowCount(); i++) {
            for (int j = 1; j < table.getColumnCount(); j++) {
                if (table.getValueAt(i, j) == null) {
                    out.append(0);
                } else {
                    out.append(table.getValueAt(i, j));
                }
                if (j == table.getColumnCount() - 1) break;
                out.append(" ");
            }
            if (i == table.getRowCount() - 1) break;
            out.append("\n");
        }
        return out.toString();
    }

    private static void loadTable() {
        explorer.setDialogTitle("Cargar Archivo");
        File fileToLoad = null;
        StringBuilder cells = new StringBuilder();
        isLoaded = true;

        int userSelection = explorer.showOpenDialog(window);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            fileToLoad = explorer.getSelectedFile();
            System.out.println("Load file: " + fileToLoad.getAbsolutePath());
        }

        try {
            assert fileToLoad != null;
            FileReader fr = new FileReader(fileToLoad);
            BufferedReader br = new BufferedReader(fr);
            cells = new StringBuilder();

            columns = br.read() - 48; //ACII to number
            br.skip(1); //Skips space char
            rows = br.read() - 48;
            br.skip(1); //Skips \n at the end of rows and columns

            while (br.ready()) {
                cells.append(br.readLine()).append("\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error while reading");
        }

        System.out.println(cells);
        page = new Page(rows, columns, cells.toString());
        window.dispose();

        isLoaded = false; //Valores por defecto
        state.clear();
        statePos = 0;
        UI.main(null); //Reinicia
        cellsToTable();
    }

    private static void cellsToTable() {
        if(table.isEditing()) table.getCellEditor().stopCellEditing();
        for (int i = 0; i < table.getRowCount(); i++) {
            for (int j = 1; j < table.getColumnCount(); j++) {
                isUndo = true;
                table.setValueAt(page.cells[i][j - 1], i, j);
            }
        }
    }

    private static void getSelectedCell() {
        int rowSelected = table.getSelectedRow();
        int colSelected = table.getSelectedColumn();
        cell.setText("Celda: " + table.getColumnName(colSelected) + (rowSelected + 1));
    }

    private static void undo(){
        if(statePos>0) {
            isUndo = true;
            statePos--;
            page = new Page(rows, columns, state.get(statePos));
            cellsToTable();
        }
    }

    private static void redo(){
       if (statePos<state.size()-1) {
           isUndo = true;
           statePos++;
           page = new Page(rows, columns, state.get(statePos));
           cellsToTable();
       }
    }

    public static void showError(String error) {
        JOptionPane.showMessageDialog(null, error, "", JOptionPane.ERROR_MESSAGE);
        //System.exit(-1);
    }
}


