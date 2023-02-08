
import java.io.*;
import java.lang.String;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//... here lies code to process the input file and simulate resource allocations ...
public class Deadlock {

    public static ArrayList<String> Processes;
    public static ArrayList<String> Resources;
    public static String mainNode;

    public static void writeToFile(String text, FileWriter myWriter) throws IOException {



        BufferedWriter writer = new BufferedWriter(myWriter);
        writer.write(text);
        writer.flush();

        if (text == "close") {
            myWriter.close();
            return;
        }

    }

    public static void main( String [] args ) throws IOException {

        RAG myRAG = new RAG();


        //CHANGE: the relative path below to the directory where the input data file is located
        String filename1 = "C:/Users/USAMA/IdeaProjects/Deadlock/inputs/" + args[0];
        File myFile = new File(filename1);
        File myObj = null;
        String txt = "";
        try {
            myObj = new File("output.txt");
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(myObj);

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        if (myFile.exists()) {
            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(myFile));
                String line = " ";

                while (line != null) {

                    //System.out.println(line);
                    line = reader.readLine();

                    String processName = "Process " + line.substring(0,1);
                    String resourceName = "Resource " + line.substring(4);

                    if (line.contains("N")) {

                        //If the line read contains a need request we add the process and resource name to the graph
                        myRAG.addNode(resourceName);
                        myRAG.addNode(processName);

                        //Here we print the resource request of each process after checking if resource is available
                        //System.out.print(processName + " needs " + resourceName);
                        txt = processName + " needs " + resourceName;
                        writeToFile(txt, writer);

                        if (myRAG.Nodes.get(resourceName).isEmpty()) {
                            myRAG.addEdge(resourceName, processName);
                            //System.out.print(" - " + resourceName + " is allocated to " + processName + "\n");
                            txt = " - " + resourceName + " is allocated to " + processName + "\n";
                            writeToFile(txt, writer);


                        } else {
                            myRAG.addEdge(processName, resourceName);
                            //System.out.print(" - " + processName + " must wait\n");
                            txt = " - " + processName + " must wait\n";
                            writeToFile(txt, writer);

                        }

                        /*
                            Once we have read an input we check the graph to see if the newly allocated resource or
                            r
                         */
                        for (String sKey : myRAG.Nodes.keySet()) {

                            if (myRAG.Nodes.get(sKey).isEmpty())
                                continue;

                            mainNode = sKey;
                            Processes = new ArrayList<String>();
                            Resources = new ArrayList<String>();
                            if (sKey.contains("Process")) {
                                Processes.add(sKey);
                            } else {
                                Resources.add(sKey);
                            }
                            // check for cycle
                            findCycle(myRAG, sKey, writer);
                        }

                    //here we implement the release requests when processes that have a resource release that resource
                    } else if (line.contains("R")) {
                        Boolean flag = false;
                        myRAG.removeEdge(resourceName, processName);
                       //System.out.print(processName + " releases " + resourceName);
                        txt = processName + " releases " + resourceName;
                        writeToFile(txt, writer);

                        for (String key : myRAG.Nodes.keySet()) {
                            if (myRAG.Nodes.get(key).isEmpty())
                                continue;
                            if (myRAG.Nodes.get(key).get(0).equals(resourceName)) {
                                myRAG.addEdge(resourceName, key);
                                myRAG.removeEdge(key, resourceName);
                                //System.out.print(" - " + resourceName + " is allocated to " + key + "\n");
                                txt = " - " + resourceName + " is allocated to " + key + "\n";
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            //System.out.println(" - " + resourceName + " is now free ");
                            txt = " - " + resourceName + " is now free " + "\n";

                        }
                        writeToFile(txt, writer);
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //System.out.println("EXECUTION COMPLETED: No deadlock encountered");
                txt = "EXECUTION COMPLETED: No deadlock encountered";
                writeToFile(txt, writer);
            }

        } else {
            System.out.println("File " + filename1 + " could not be found");
        }

        //String msg = "close";
        //writeToFile(msg, writer);
    }

    /*
        In this function we are simply checking if a cyclic dependency exists between the nodes in our RAG, and we are
        doing this by recursively calling this function till we reach the end of all the paths formed by the dependencies
        in our RAG. If any of the paths return to the root then a cyclic dependency is found otherwise we will declared
        that no dependencies are there.
     */
    static void findCycle(RAG myRAG, String node, FileWriter myObj) throws IOException {
        if (myRAG.Nodes.get(node).isEmpty())
            return;
        String nextNode = myRAG.Nodes.get(node).get(0);
        if (nextNode.equals(mainNode)) {
            //System.out.print("DEADLOCK DETECTED: ");
            String txt = "DEADLOCK DETECTED: ";
            writeToFile(txt, myObj);

            /*
            System.out.println("Processes "
                    + Processes.toString().replace("[", "").replace("Process ", "").replace("]", "") + " and "
                    + "Resources " + Resources.toString().replace("[", "").replace("Resource ", "").replace("]", "")
                    + " are found in a cycle");
             */
            String msg = "Processes "
                    + Processes.toString().replace("[", "").replace("Process ", "").replace("]", "") + " and "
                    + "Resources " + Resources.toString().replace("[", "").replace("Resource ", "").replace("]", "")
                    + " are found in a cycle";
            writeToFile(msg, myObj);

            System.exit(0);
        } else {
            if (nextNode.contains("Process")) {
                Processes.add(nextNode);
            } else
                Resources.add(nextNode);
            findCycle(myRAG, nextNode, myObj);
        }
        return;
    }
}

/*
    In our RAG implementation we have declared a Map data structure which allows us to sort the data which is the processes
    and resources given in the input file. We use key and value pairs to easily identify the processes and resources, and we
    have an ArrayList structure to hold all the requests made processes.
 */
class RAG {
    
    Map<String, List<String>> Nodes;

    RAG() {
        Nodes = new LinkedHashMap<String, List<String>>();
    }

    void addNode(String identifier) {
        Nodes.putIfAbsent(identifier, new ArrayList<>());
    }

    void addEdge(String firstNode, String secondNode) {
        Nodes.get(firstNode).add(secondNode);
    }

    void removeEdge(String firstNode, String secondNode) {
        List<String> rmvEd = Nodes.get(firstNode);
        if (rmvEd != null)
            rmvEd.remove(secondNode);
    }

}
