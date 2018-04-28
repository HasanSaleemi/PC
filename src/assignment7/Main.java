package assignment7;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class Main {
    private static void analyse(String dir, int phraseSize, int thres){
        File[] allFiles = (new File(dir)).listFiles();
        if(allFiles == null)
            return;
        Map<Integer, LinkedList<String>> megaMap = Collections.synchronizedMap(new HashMap<>());

        LinkedList<Thread> readers = new LinkedList<>();
        int numThreads = (int)Math.ceil(Math.log(allFiles.length));
        int len = (int)Math.ceil(1.0 * allFiles.length / numThreads);

        for(int i = 0; i < numThreads; i++){
            final int lower = i * len;
            final int upper = (i + 1)*len > allFiles.length ? allFiles.length : (i + 1)*len;
            readers.add(new Thread(() -> {
                for(int numFile = lower; numFile < upper; numFile++){
                    try {
                        File current = allFiles[numFile];
                        ArrayList<String> words = new ArrayList<>();
                        Scanner read = new Scanner(new FileReader(current));
                        while (read.hasNext()) {
                            String theNext = read.next().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                            if (theNext.length() > 0) {
                                if (words.size() == phraseSize) {
                                    words.remove(0);
                                }
                                words.add(theNext);
                                if (words.size() == phraseSize) {
                                    megaMap.computeIfAbsent(words.hashCode(), k -> new LinkedList<>());
                                    megaMap.get(words.hashCode()).add(current.getName());
                                }
                            }
                        }
                        read.close();
                    } catch (FileNotFoundException ignored) {}
                }
            }));
        }

        for(Thread t : readers) {
            t.start();
        }
        for(Thread t : readers) {
            try {
                t.join();
            } catch (InterruptedException ignored) {}
        }

        Map<String, Integer> totals = new HashMap<>();

        int start = 1;
        for(File file1 : allFiles){
            for (int i = start; i < allFiles.length; i++) {
                totals.put(file1.getName() + ">" + allFiles[i].getName(), 0);
            }
            start++;
        }
        for(Integer hash : megaMap.keySet()){
            for(String file1 : megaMap.get(hash)){
                for(String file2 : megaMap.get(hash)){
                    if(totals.get(file1 + ">" + file2) != null)
                        totals.put(file1 + ">" + file2, totals.get(file1 + ">" + file2) + 1);
                }
            }
        }

        List<String> relevantFiles = new ArrayList<>();

        for(String s : totals.keySet()){
            if(!relevantFiles.contains(s) && totals.get(s) > thres){
                relevantFiles.add(s);
            }
        }

        relevantFiles.sort((o1, o2) -> totals.get(o2) - totals.get(o1));

        for(String filePair : relevantFiles){
            System.out.format("%-6s", totals.get(filePair)+":");
            System.out.println(filePair.replace(">", ", "));
        }

        /*List<String> relevantFiles = new ArrayList<>();

        for(String s : totals.keySet()){
            String formatted = s.substring(0, s.indexOf('>'));
            if(!relevantFiles.contains(formatted) && totals.get(s) > thres){
                relevantFiles.add(formatted);
            }
        }

        System.out.print("          ");
        for(String file1 : relevantFiles){
            String formattedName = file1.replace(".txt", "");
            if(formattedName.length() > 8)
                formattedName = formattedName.substring(0, 8);
            System.out.format("%10s", formattedName);
        }
        System.out.println();

        int start = 0;
        for(String file1 : relevantFiles){
            String formattedName = file1.replace(".txt", "");
            if(formattedName.length() > 8)
                formattedName = formattedName.substring(0, 8);
            System.out.format("%-10s", formattedName);

            for(int i = 0; i < relevantFiles.size(); i++){
                if(i > start) {
                    int am = totals.get(file1 + ">" + relevantFiles.get(i));
                    System.out.format("%10s", am > thres ? am : "-");
                } else
                    System.out.format("%10s", "--------");
            }
            System.out.println();
            start++;
        }*/
    }

    public static void main(String[] args) {
        //analyse("big_doc_set", 6, 200);
        analyse(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
    }
}
