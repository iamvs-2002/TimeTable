import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING;

public class HomePage extends JFrame implements ActionListener {
    static int[] studentCount;
    static File file;
    static HSSFSheet sheet;
    String[] semesters = {"Odd", "Even"};
    String[] programs = {"UG", "PG", "PhD"};
    private JButton button_select_file;
    private JLabel label_file_name;
    private JLabel label_output;

    HomePage() {
        button_select_file = new JButton("Select file");
        button_select_file.setSize(60, 50);
        button_select_file.addActionListener(this);
        button_select_file.setFont(new Font("Osward",Font.PLAIN,15));
        button_select_file.setBounds(60,60,120,30);

        label_file_name = new JLabel("No file selected");
        label_file_name.setFont(new Font("Osward",Font.ITALIC,12));
        label_file_name.setSize(120, 50);
        label_file_name.setBounds(200,60,150,30);


        label_output = new JLabel();
        label_output.setSize(580, 400);
        label_output.setBounds(60,100,580,600);

        setLayout(null);
        add(button_select_file);
        add(label_file_name);

        setSize(600,600);
        getContentPane().setBackground(Color.cyan);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Time Table Generator");
        setVisible(true);
        setLocation(500,20);
    }

    public static void main(String[] args) {
        new HomePage().setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==button_select_file){
            File file = selectFile();
            if(file!=null){
                label_file_name.setText(file.getName());
                JOptionPane.showMessageDialog(null,"File selected successfully. Processing file...");
                findCliques(file);
            }
            else{
                JOptionPane.showMessageDialog(null,"Error. Please try again later.");
            }
        }
    }

    File selectFile(){
        JFileChooser chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel (.xls)", "xls", "xlsx");
        chooser.addChoosableFileFilter(filter);
        int returnVal = chooser.showOpenDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            System.out.println("You chose to open this file: " +chooser.getSelectedFile().getName());
            return chooser.getSelectedFile();
        }
        return null;
    }

    void findCliques(File file) {
        // Create the graph given in the above figure
        try {

            // Create a POIFSFileSystem object
            POIFSFileSystem myFileSystem;

            try{
                FileInputStream fis = new FileInputStream(file);   //obtaining bytes from the file
                myFileSystem = new POIFSFileSystem(fis);
            }
            catch (Exception e){
                String path = "C:\\Users\\iamvs\\Downloads\\Test1.xls";
                FileInputStream fis = new FileInputStream(path);
                myFileSystem = new POIFSFileSystem(fis);
            }

            // Creating Workbook instance that refers to .xls file
            HSSFWorkbook wb = new HSSFWorkbook(myFileSystem);
            sheet = wb.getSheetAt(0);     //creating a Sheet object to retrieve object

            int rows = sheet.getLastRowNum();
            int cols = sheet.getRow(1).getLastCellNum();

            studentCount = new int[cols];

            Map<Integer, LinkedHashSet<String>> courses = new HashMap<>();

            for (int i = 1; i <= cols; i++) {
                courses.put(i, new LinkedHashSet<>());
            }
            for (int r = 1; r <= rows; r++) {
                HSSFRow row = sheet.getRow(r);
                for (int c = 0; c < cols; c++) {
                    HSSFCell cell = row.getCell((short) c);
                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case CELL_TYPE_STRING:
                                studentCount[c]++;
                                String rollNumber = row.getCell((short) c).getStringCellValue();
                                courses.get(c + 1).add(rollNumber);
                                break;

                            default:
                                break;
                        }
                    }
                }
            }

            int v = cols;
            Graph graph = new Graph(v);

            for (int i = 1; i < v; i++) {
                LinkedHashSet<String> setx = courses.get(i);
                for (int j = i + 1; j <= v; j++) {
                    LinkedHashSet<String> setY = courses.get(j);
                    Iterator<String> iteratorx = setx.iterator();
                    Map<String, Integer> compare = new HashMap<>();

                    while (iteratorx.hasNext()) {
                        compare.put(iteratorx.next(), 0);
                    }

                    Iterator<String> iteratorY = setY.iterator();
                    boolean flag = false;

                    while (iteratorY.hasNext()) {
                        String s = iteratorY.next();
                        if (compare.containsKey(s)) {
                            flag = true;
                            break;
                        }
                    }
                    if (flag == false) {
                        graph.addEdge(i, j);
                    }
                }
            }

            int cliq = Graph.findClique(v); // total cliques

            StringBuilder temp = new StringBuilder();
            temp.append("Total subjects : "+v);
            temp.append("\n");
            temp.append("Total cliques : "+cliq);
            temp.append("\n");
            temp.append(Graph.res.toString());
            System.out.println(temp);

            label_output.setText(temp.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

class Graph {
    // result containing the number of cliques, subjects and total students in each clique
    static StringBuilder res;
    // TreeSet is used to get clear understanding of graph.
    static HashMap<Integer, TreeSet<Integer>> graph;
    static HSSFRow header;

    // Graph Constructor
    public Graph(int v) {
        graph = new HashMap<>();
        header = HomePage.sheet.getRow(0);
        res = new StringBuilder();
        for (int i = 1; i <= v; i++) {
            graph.put(i, new TreeSet<>());
        }
    }

    // Adds an edge to an undirected graph
    public void addEdge(int src, int dest) {
        // Add an edge from src to dest into the set
        graph.get(src).add(dest);

        // Since graph is undirected, add an edge
        // from dest to src into the set
        graph.get(dest).add(src);
    }

    public static boolean[] visited;
    static Map<Integer, Integer> V = new HashMap<>();

    public static int findClique(int no_of_subject) {
        int v = no_of_subject;
        visited = new boolean[v + 1];
        int subject = 1;
        int count = 0;
        for (int i = 1; i <= no_of_subject; i++) {
            if (graph.containsKey(i))
                V.put(i, graph.get(i).size());
        }

        while (subject <= no_of_subject) {
            if (visited[subject]) {
                subject++;
            } else {
                Stack<Integer> clique = new Stack<Integer>();
                makeClique(subject, clique);
                count++;
                subject++;
                res.append("Clique ").append(count).append(": ");
                int totalStudents = 0;
                while (!clique.isEmpty()) {
                    int c = clique.pop();
                    int students = HomePage.studentCount[c-1];
                    totalStudents+=students;
                    String subj = header.getCell((short) (c-1)).getStringCellValue();
                    res.append(subj).append("(").append(String.valueOf(students)).append(")").append(" ");
                }
                res.append("\n");
                res.append("Total Students: ").append(totalStudents);
                res.append("\n");
                res.append("\n");
                //update V
                for (int i = 1; i <= no_of_subject; i++) {
                    if (graph.containsKey(i) && !visited[i])
                        V.put(i, graph.get(i).size());
                }
            }
        }
        return count;
    }

    public static void makeClique(int subject, Stack<Integer> clique) {
        clique.add(subject);
        visited[subject] = true;
        Map<Integer, Integer> adjacent = findAdjacent(subject);

        Map<Integer, Integer> newV = new HashMap<>();

        for (Map.Entry<Integer, Integer> entry : adjacent.entrySet()) {
            int x = entry.getKey();
            if (V.containsKey(x)) {
                newV.put(x, entry.getValue());
            }

        }
        V = newV;
        if (!V.isEmpty()) {
            int max_degree_node = Integer.MIN_VALUE;
            for (Map.Entry<Integer, Integer> entry : V.entrySet()) {
                if (entry.getValue() > max_degree_node) {
                    max_degree_node = entry.getKey();
                }
            }

            makeClique(max_degree_node, clique);
        }

    }

    public static Map<Integer, Integer> findAdjacent(int subject) {
        Map<Integer, Integer> adjacent = new HashMap<>();
        if (graph.containsKey(subject)) {
            Iterator<Integer> set = graph.get(subject).iterator();

            while (set.hasNext()) {
                int x = set.next();
                adjacent.put(x, graph.get(x).size());
            }
        }
        return adjacent;
    }
}
