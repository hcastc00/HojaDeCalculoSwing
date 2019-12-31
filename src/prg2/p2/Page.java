package prg2.p2;

import java.util.Arrays;

public class Page {
    private int rows;
    private int columns;
    private String input;
    String[][] cells;
    private boolean[][] isFormula;
    int[][] solution;

    /*
    Constructor de la pagina
    Lee el tamaño
    Inicializa la matriz de celdas, la de soluciones y la que indica si es formula o no con los valores introducidos
    Lee las celadas y las guarda en la matriz de celdas
    Determina que celdas son formulas y lo escribe en la matriz de isFormula
     */
    public Page(int rows, int columns,String input) {
        this.rows = rows;
        this.columns = columns;
        this.input = input;
        this.cells = new String[rows][columns];
        this.solution = new int[rows][columns];
        this.isFormula = new boolean[rows][columns];

        for (int y = 0; y < this.rows; y++) {
            for (int x = 0; x < this.columns; x++) {
                this.isFormula[y][x] = false;
                }
            }

        checkSize();
        readCells();
        getFormulas();
    }

    /*
    Lee el tamaño de la pagina con dos valores introducidos por entrda estandar de la manera:
        "y x" con y = col y x = filas
     */
    private void checkSize() {

        if (this.rows <= 0 || this.columns <= 0) {
            UI.showError("Page too small, must be at least 1x1");
        }

        if (this.rows > 999 || this.columns > 18278) {
            UI.showError("Page too big, must be max 18278x999");
        }
    }

    /*
    Lee las celdas linea a linea
     */
    public void readCells() {
        try {
            String[] lines = input.split("\\n+");
            String[] lineCells;

            for(int y=0; y<this.rows; y++){
                lineCells = lines[y].split(" ");
                if (lineCells.length != this.columns) UI.showError("Input does not match given size");
                this.cells[y] = lineCells;
            }
            printMat(this.cells);
        }catch(Exception e){
            UI.showError("ERROR IN THE TABLE");
        }
    }

    /*
    Recorre el arry de celdas y scunado encuentra una formula lo indica con un valor true en la misma posicion en
    el array del mismo tamaño "formulas"
     */
    private void getFormulas() {
        for (int y = 0; y < this.rows; y++) {
            for (int x = 0; x < this.columns; x++) {
                if (this.cells[y][x].charAt(0) == '=') {
                    this.isFormula[y][x] = true;
                }
            }
        }
        System.out.println(Arrays.deepToString(isFormula));;
    }

    public void solve() {
        Cell form;
        //Primero paso todos los numeros a la matriz solucion
        try {
            for (int y = 0; y < this.rows; y++) {
                for (int x = 0; x < this.columns; x++) {
                    if (!this.isFormula[y][x]) {
                        this.solution[y][x] = Integer.parseInt(this.cells[y][x]);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            UI.showError("Invalid value in cell");
        }
        //Luego resuelvo las formulas
        try {
            for (int y = 0; y < this.rows; y++) {
                for (int x = 0; x < this.columns; x++) {
                    if (this.isFormula[y][x]) {
                        form = new Cell(y, x);
                        this.solution[y][x] = solveFormula(form);
                        this.isFormula[form.getY()][form.getX()] = false; //La quito como formula para ahorrar calculos si otra celda necesita ese valor
                    }
                }
            }
        }catch(Exception e){
            UI.showError("Invalid formula in the table");
        }
        //printMat(solution);
    }

    private int solveFormula(Cell form) {
        String formula = this.cells[form.getY()][form.getX()].substring(1); // Elimina el = que determina que es una formula
        String[] operators = formula.split("\\+"); //Divide la formula en sus operadores
        Cell coords;
        int value = 0;

        /*
        Va recorriendo las coordenadas de los operadores y si es una formula vuelve a ejecutar el metodos sobre ella
        sino suma al resultado el valor de esas cooredenadas en la matriz de soluciones.
         */
        for (String operator : operators) {
            coords = stringToCoords(operator);
            if (isFormula[coords.getY()][coords.getX()]) {
                value += solveFormula(coords);
            } else {
                value += this.solution[coords.getY()][coords.getX()];
            }
        }
        return value;
    }

    private Cell stringToCoords(String input) {
        StringBuilder letters = new StringBuilder();
        StringBuilder numbers = new StringBuilder();
        Cell coords;

        for (int i = 0; i < input.length(); i++) {
            if (Character.isDigit(input.charAt(i))) {
                numbers.append(input.charAt(i));
            } else {
                letters.append(input.charAt(i));
            }
        }

        coords = new Cell(Integer.parseInt(numbers.toString()) - 1, lettersToNumber(letters.toString()));
        return coords;
    }

    private int lettersToNumber(String letters) {
        for (int i = 0; i < letters.length(); i++) {
            if (letters.charAt(i) < 'A' || letters.charAt(i) > 'Z') {
                UI.showError("Invalid character in a cell, characters must be form A to Z");
            }
        }
        char[] lettersSplit = letters.toCharArray();
        int value = 0;
  /*
  Recorre el arry de letras de izda a derecha, es decir, de mas valor (exp mayor) a menos
  Las letras mayusculas emiezan en A=64, le resto 64 para que A=0
  Se hace un cambio de base, de base 26 a base 10
   */
        for (int exp = 0, pos = lettersSplit.length - 1; exp < lettersSplit.length; exp++, pos--) {
            value += (lettersSplit[pos] - 64) * Math.pow(26, exp);
        }
        value--;
        return value;
    }

    private void printMat(String[][] mat) {
        for (int y = 0; y < this.rows; y++) {
            for (int x = 0; x < this.columns; x++) {
                System.out.print(mat[y][x]);
                if (x == this.columns - 1) break; //No mete espacio al final de los ultimos numeros de cada fila
                System.out.print(" ");
            }
            System.out.println("");
        }
    }

    //Clase cell utilizada para manejar coordenadas de forma mas comoda
    class Cell {
        private int y, x;

        public Cell(int y, int x) {
            this.y = y;
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public int getX() {
            return x;
        }
    }
}