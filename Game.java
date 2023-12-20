/**
 * Text game engine.
 * 
 * Needs datapack in XML file.
 * 
 * Does not support conditional selections.
 * 
 * Made only becouse i was bored. Do not expect too much.
 *
 * @author NiousekCZ
 */

package game;

import java.util.Vector;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import static java.lang.Integer.parseInt;

public class Game {
    
    // Datapack file path
    private static final String filepath = "datapack.xml";
    
    // Database
    private static Vector<node> db = new Vector();
    
    // User input
    static Scanner sc = new Scanner(System.in);
    
    // Runtime flags
    protected static boolean END = false;
    protected static int lastId = -1; // Used for resolving end of game - last succesfully displayed node
    protected static int currentId = 1; // Current and starting node ID
    
    public static void main(String[] args) throws IOException, Exception {
        
        // Initialize game - load configuration
        init();
        // Main game "loop"
        play();
        // Resolve game end
        end();
    }
    
    //=============================[ Game play ]=============================
    private static void play() {        
        while(!END) {
            // Save last node
            lastId = currentId;
            // Send new node
            currentId = display(currentId);
        }
    }
       
    private static void end() {
        // Put different endings here - select via variable "lastId".
        // Datapack nodes can be used directly with "display(int id)" function.
        System.out.println("Game end!\r\nLast visited node id: " + lastId);
    }
    
    private static int display(int id) {
        // Get current node
        node b = getNode(id);
        
        // Print node text
        System.out.println(b.desc);
        
        // No more options => Game over
        if(b.opts.size() < 1) {
            END = true;
            return(-1);
        }
        
        // Print options
        for(int j = 0; j < b.optc ; j++){    
            System.out.println("("+(j+1)+") "+b.opts.elementAt(j).text);
        }
        
        // User's selection
        int userOpt = response();
        
        // User is moron
        if(userOpt < 1 || userOpt > b.opts.size()) {
            System.out.println("Invalid option!");
            // Reload node
            return(b.id);
        }
        
        // Call next node
        return(b.opts.elementAt(userOpt-1).id);
    }
    
    private static node getNode(int k){
        for(node a : db){
            if(a.id == k) {
                return(a);
            }
        }
        return(null);
    }
    
    // Get user selection
    private static int response() {
        return sc.nextInt();
    }
    
    //=============================[ Load file ]=============================
    
    /**
     * XML formatting:
     * 
     *  <node id="1" opts="2" text="" >
     *      <option id="2" text="" />
     *      <option id="3" text="" />
     *  </node>
     *  <node id="2" opts="0" text="" ></node>
     *  <node id="3" opts="0" text="" ></node>
     * 
     * Yes, the "</node>" is needed on end of every node as I am too lazy to implement one-line "/>".
     */
    
    private static void init() throws IOException, Exception {
        String input = IStoSTR(filepath);
        regex(input);
        System.out.println("Game initialized.");
    }
    
    // Parser
    private static void regex(String input) throws Exception {
        int nl = 0; // NewLine
        String tmp = null; // Node workplace
        int ixS = 0; // Start
        int ixO = 0; // Options
        int ixN = 0; // Name
        int ixE = 0; // End
        
        int optc = 0; // Options counter
        
        // Remove comments
        input = input.replaceAll( "(?s)<!--.*?-->", "" );
             
        // Resolve each node
        for(int i = 0; /* No end condition - break */; i++){
            nl = input.indexOf("</node>");
            if(nl == -1){
                // Last line - not processed
                break;
            }
            
            tmp = input.substring(0, nl+7);
            input = input.substring(nl+7);
            tmp = tmp.replace("\t", "");

            node q = new node();
            
            ixS = tmp.indexOf("id=\"")+4;
            ixO = tmp.indexOf("\" opts=\"");
            ixN = tmp.indexOf("\" text=\"")+8;
            ixE = tmp.indexOf("\" >");
            
            optc = parseInt(tmp.substring(ixO+8, ixN-8));
            
            q.id = parseInt(tmp.substring(ixS, ixO));
            q.desc = tmp.substring(ixN, ixE);
            q.optc = optc;
            
            tmp = tmp.substring(ixE+3);
            tmp = tmp.replace("</node>", "");
            
            // Add options
            for(int j = 0; j < optc ; j++){        
                reply t = new reply();
                int optO = tmp.indexOf("<option id=\"")+12;
                int optN = tmp.indexOf("\" text=\"")+8;
                int optE = tmp.indexOf("\" />");
                
                t.id = parseInt(tmp.substring(optO, optN-8));
                t.text = tmp.substring(optN, optE);
                q.opts.add(t);
                tmp = tmp.substring(optE+4);
            }
            
            // Add to database
            db.add(q);
        }    
    }
    
    // Node holder
    private static class node {
        public int id = 0;
        public String desc = null;
        public int optc = 0;
        public Vector<reply> opts = new Vector<reply>();
        
        public node() { /* Empty Constructor */ }
        
        @Override 
        public String toString() {
            return(String.format("| Id: %8d | Options: %3d | Text: %92s |", this.id, this.optc, this.desc));
        }
    }
    private static class reply {
        public int id = 0;
        public String text = null;
    }
    
    // File to String
    private static String IStoSTR(String filepath) throws FileNotFoundException, IOException{
        InputStream is = new FileInputStream(filepath);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader reader = new BufferedReader(isr);
        StringBuffer sb = new StringBuffer();
        String str;
        while((str = reader.readLine())!= null){
            sb.append(str);
        }
        return sb.toString();
    }
    
    // NOT USED - Generate file with items from database.
    private static void dump(String filename) {
        try {
            File output = new File(filename);
            if (output.createNewFile()) {
                System.out.println("File created: " + output.getName() + "\r\nRe-Run.");
            } else {
                FileWriter fw = new FileWriter(filename);
                fw.write("+--------------+--------------+----------------------------------------------------------------------------------------------------+\r\n");
                for(node a : db) {
                    fw.append(a.toString() + "\r\n");
                }
                fw.write("+--------------+--------------+----------------------------------------------------------------------------------------------------+\r\n");
                fw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
