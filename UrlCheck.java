/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IsaProject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author sajid
 */
public class UrlCheck{
        private static ArrayList<String> words;
        private static ArrayList<String> links;
        private static ArrayList<String> linksToVisit;
        private static ArrayList<Double> values;
        private static ArrayList<String> fileNames;
        
        private static String input;
        
        static String lineSeparator = System.getProperty("line.separator");
        
        private static File indexFile = new File("/home/sajid/NetBeansProjects/IR/files/indexFile.txt");
        private static File[] allFiles;
        
        Timer timer = new Timer();
        TimerTask myTask = new TimerTask(){
            public void run(){
                System.out.println("index file updated");
                initialise();
            }
        };
        
        public void start(){
            timer.scheduleAtFixedRate(myTask, 0, 40000);
        }
            
        
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        UrlCheck u = new UrlCheck();
        u.start();
        while(true)
        {
            System.out.println("Enter the Query : ");
            input = s.nextLine();
            cosine();
        }
            
    }
    
    private static void initialise()
    {
        words =  new ArrayList<>();
        linksToVisit = new ArrayList<>();
        File folder = new File("/home/sajid/NetBeansProjects/IR/files/doc1");
        if(folder.isDirectory())
        {
            allFiles = folder.listFiles();
            for (int i = 0; i < allFiles.length; i++) 
                linksToVisit.add(allFiles[i].getAbsolutePath().toString());
            
            int i = 0;
            Words w = new Words();
            while(!linksToVisit.isEmpty() && i<linksToVisit.size())
            {
                links = new ArrayList<>();
                File f = new File(linksToVisit.get(i));
                String s = readFile(f.getAbsolutePath());
                StringTokenizer tokens = new StringTokenizer(s);
                while(tokens.hasMoreTokens())
                {
                    String token = tokens.nextToken();
                    if(token.charAt(0) == '/') 
                    {
                        links.add(token);
                        if(!linksToVisit.contains(token)) linksToVisit.add(token);
                    }
                    else {
                        token = token.toLowerCase();
                        if(!words.contains(token))
                                    words.add(token);                                          
                    }
                }
                i++;
            }
        }
        values = new ArrayList<>();
        if(!indexFile.exists())
        {
            try {
                indexFile.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(UrlCheck.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        for (int i = 0; i < linksToVisit.size(); i++) {
            String data = readFile(linksToVisit.get(i));
            StringTokenizer dataTokenizer;
            for (int j = 0; j < words.size(); j++) {
                    double k = 0.0;
                    dataTokenizer = new StringTokenizer(data);
                    while(dataTokenizer.hasMoreTokens())
                    {
                        if(words.get(j).equalsIgnoreCase(dataTokenizer.nextToken()))
                            k++;
                    }
                    values.add(k);                 
            }
        }
        
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(indexFile));
            readFileNames();
            for (int i = 0; i < fileNames.size(); i++) {
                bw.append("\t" + fileNames.get(i));
            }
            
            bw.append(lineSeparator);

            int k = 0 , l = 0;
            for (int i = 0; i < words.size(); i++) {
                bw.append(words.get(i) + "\t");
                for (int j = 0; j < values.size()/words.size(); j++) {
                    bw.append(values.get(l) + "\t");
                    l = l + words.size();
                }
                k++;
                l = k;
                bw.append(lineSeparator);
            }
            
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(UrlCheck.class.getName()).log(Level.SEVERE, null, ex);
        }
        return;
    }
    
    
    public static void cosine()
    {
        readFileNames();
        ArrayList<ArrayList> lists1 = new ArrayList<>();
        ArrayList<Double> a1;
        int k = 0;
        int l = 0;
        while(k<values.size()/words.size())
        {
            int i;
            a1 = new ArrayList<>();
            for (i = l; i < l + words.size(); i++) {
                a1.add(values.get(i));
            }
            lists1.add(a1);
            l = i;
            k++;
        }
        
        for (int i = 0; i < lists1.size(); i++) {
            a1 = lists1.get(i);
            for (int j = 0; j < a1.size(); j++) {
                if (a1.get(j) > 0) {
                    a1.set(j, (1.0 + Math.log10(a1.get(j))));
                } else {
                    a1.set(j, 0.0);
                }
            }
            lists1.set(i, a1);
        }
        
        for (int i = 0; i < lists1.size(); i++) {
            a1 = lists1.get(i);
            double sum = 0;
            for (int j = 0; j < a1.size(); j++) {
                sum += (a1.get(j) * a1.get(j));
            }
            double d = Math.sqrt(sum);
            for (int j = 0; j < a1.size(); j++) {
                a1.set(j, (a1.get(j) / d));
            }
        }
      
        ArrayList<Double> query = new ArrayList<>();
        for (int i = 0; i < words.size(); i++) {
            String nextWord = words.get(i);
            double count = 0;
            StringTokenizer st = new StringTokenizer(input);
            while (st.hasMoreTokens()) {
                if (st.nextToken().equals(nextWord)) {
                    count++;
                }
            }
            if (count > 0) {
                query.add((1 + Math.log10(count)));
            } else {
                query.add(0.0);
            }
        }
        double res = 0;
        for (int i = 0; i < query.size(); i++) {
            res += (query.get(i) * query.get(i));
        }
        double d = Math.sqrt(res);
        for (int i = 0; i < query.size(); i++) {
            query.set(i, (query.get(i) / d));
        }
        
        ArrayList<Double> result = new ArrayList<>();
        for (int i = 0; i < lists1.size(); i++) {
            double sum = 0;
            ArrayList<Double> doc = lists1.get(i);
            for (int j = 0; j < doc.size(); j++) {
                sum += (doc.get(j) * query.get(j));
            }
            result.add(sum);
        }
        
        for (int i = 0; i < result.size(); i++) {
            for (int j = 0; j < result.size() - 1; j++) {
                double val1 = result.get(j);
                double val2 = result.get(j + 1);
                String v1 = fileNames.get(j);
                String v2 = fileNames.get(j + 1);
                if (val2 > val1) {
                    result.set(j, val2);
                    result.set(j + 1, val1);
                    fileNames.set(j, v2);
                    fileNames.set(j + 1, v1);
                }
            }
        }
        
        System.out.println("-------------------Document Ranking ----------------");
        for (int i = 0; i < fileNames.size(); i++) {
            System.out.println(fileNames.get(i));
        }
        
    }

    private static void readFileNames()
    {
       fileNames = new ArrayList<>();
       for (int i = 0; i < linksToVisit.size(); i++) {
                File f = new File(linksToVisit.get(i));
                fileNames.add(f.getName());
            } 
    }
    private static String readFile(String path)
    {        
        StringBuffer sb = new StringBuffer();
        String line = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            while((line = br.readLine()) != null)
            {
                sb.append(line);
                sb.append(" ");
            }            
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UrlCheck.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UrlCheck.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }
}
