import java.io.*;

/**
 * Created by Igor on 030. Jul 30, 16.
 */
public class Main {

    private static String[][] bmArr = new String[6000][4];
    private static String[] lines = new String[2000];
    private static int total = 0;
    private static long unixTime;


    public static void main (String[] args){


        //0 for using existing main file
        //1 for a new rewrite of main bookmark file using html files
        int refresh = 1;

        switch(refresh){
            case 0:
                readMainFile();
                resize();
                break;
            case 1:
                createList();
                resize();
                sort();
                clearDuplicates();
                resize();
                writeToFile();
                break;
        }

        //count();
        generateHTMLfile();

        //printArray();

    }

    //=================OPERA METHODS====================//

    private static void importOpera(String file){

        try{
            BufferedReader brO = new BufferedReader(new InputStreamReader(new FileInputStream("Resources/"+file)));
            String line;
            int count = 0;
            emptyLines();
            while((line = brO.readLine()) != null){
                if(line.contains("http")){
                    lines[count] = line;
                    count++;
                }
            }

            /*count = 0;
            line = "";

            while (true){
                System.out.println(lines[count]);
                if(lines[count] == null)
                    break;
                count++;

            }*/

            brO.close();
        }catch(FileNotFoundException e){
            System.out.println("File read error");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void operaExtract(){

        int count = 0;
        int qCount, bCount;
        String line;
        String link = "";
        String desc = "";

        //Loop goes through all lines
        while (true){
            line = lines[count];
            if(line == null)
                break;
            //System.out.println(line);

            //Loop goes through all chars in line
            //qCount counts up to 2 quotes (") to break out of loop (avoids "" in description part of line)
            boolean write = false;
            qCount = 0;
            bCount = 0;
            for(int x=0;x<line.length();x++){

                //if write is true, all proceeding chars are written to link string until next \"
                if (line.charAt(x) == '\"'){
                    qCount++;
                    if(qCount == 2)
                        break;
                    if(write)
                        write = false;
                    else
                        write = true;
                }
                if(write){
                    link = link + line.charAt(x);
                }
            }

            //For loop to gather descriptions of links
            for(int x=0;x<line.length();x++){
                //if write is true, all proceeding chars are written to link string until next \"
                if(line.charAt(x) == '>'){
                    bCount++;
                    if(bCount == 2){
                        desc = line.substring(x+1,line.length()-9);
                        break;
                    }
                }
            }

            link = link.substring(1);
            bmArr[total][0] = link;
            bmArr[total][1] = desc;

            total++;
            //System.out.println(link);
            //System.out.println(desc);
            link = "";
            desc = "";

            count++;
        }
    }


    //=================FIREFOX METHODS===================//

    private static void importFirefox(String file){

        try{
            BufferedReader brFF = new BufferedReader(new InputStreamReader(new FileInputStream("Resources/"+file)));
            String line;
            int count = 0;
            emptyLines();
            while((line = brFF.readLine()) != null){
                lines[count] = line;
                count++;
            }

            count = 0;
            line = "";

            while (true){
                //System.out.println(lines[count]);
                if(lines[count] == null)
                    break;
                count++;

            }
            brFF.close();
        }catch(FileNotFoundException e){
            System.out.println("File read error");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    private static void firefoxExtract(){

        int lineCount = 0;
        String line;
        String[] split;
        String[] s;
        String desc = "";

        //while loop goes through each line of HTML code
        while(true) {
            line = lines[lineCount];
            //System.out.println(line);
            if (line == null)
                break;

            if (line.contains("HREF=\"http")) {
                int bCount = 0;
                boolean write = false;

                //This
                split = line.split(" ");
                // splits the line into:
                //HREF=...
                //ADD_DATE=...
                //LAST_MODIFIED=...
                //ICON URI=...
                //ICON=...

                //For loop goes through above and picks out the list below to store in bmArr array
                for(int aa = 0; aa < split.length; aa++) {
                    //[0] = Link
                    //[1] = Description
                    //[2] = Add date
                    //[3] = Icon

                    if(split[aa].contains("HREF")){
                        //This splits using a \" (quote). s[1] will always produce the URL
                        s = split[aa].split("\"");
                        bmArr[total][0] = s[1];
                        total++;

                    }else if(split[aa].contains("ADD_DATE=")){
                        s = split[aa].split("\"");
                        bmArr[total][2] = s[1];
                        //System.out.println("Adding: "+ s[0] + " --- " + s[1]);

                    }else if(split[aa].contains("ICON=")) {
                        s = split[aa].split("\"");
                        bmArr[total][3] = s[1];
                        //System.out.println("Adding: "+ s[0] + " --- " + s[1]);
                    }
                }

                //For loop to get description from same line as URL
                for (int x = line.length() - 1; x > 0; x--) {
                    //if write is true, all previous chars are written to desc until ">"
                    //If block breaks out of loop after full description is copied from that line
                    if (line.charAt(x) == '>') {
                        bCount++;
                        if (bCount == 2)
                            break;
                    }
                    //If block turns write switch on and off
                    else if(line.charAt(x) == '<'){
                        write = true;
                    }

                    if (write) {
                        desc = line.charAt(x) + desc;
                    }
                }

                desc = desc.substring(0,desc.length()-1);


                //If next line starts with <DD> then it is a continuation of the description
                if (lines[lineCount+1].contains("<DD>")) {
                    lineCount++;
                    line = lines[lineCount];
                    for (int x = 0; x < line.length(); x++) {
                        //if write is true, all proceeding chars are written to link string until next \"
                        if (line.charAt(x) == '>') {
                            if(x+1 == line.length())
                                break;
                            if (line.substring(x - 3, x + 1).equals("<DD>")) {
                                desc = desc + " || " + line.substring(x + 1);
                                break;
                            }
                        }
                    }

                    //Infinite loop keeps checking following lines to see if they are part of description
                    //breaks at the next "<" it finds since that would be end of description
                    while(true){
                        if(lines[lineCount+1].contains("<")){
                            break;

                        }else{
                            lineCount++;
                            line = lines[lineCount];
                            desc = desc +" "+ line;
                        }
                    }
                }
                bmArr[total][1] = desc;
                desc = "";
            }
            lineCount++;
        }
    }


    //=================CHROME METHODS====================//

    private static void importChrome(String file){

        try{
            BufferedReader brC = new BufferedReader(new InputStreamReader(new FileInputStream("Resources/"+file)));
            String line;
            int count = 0;
            emptyLines();
            while((line = brC.readLine()) != null){
                lines[count] = line;
                count++;
            }

            count = 0;
            line = "";

            while (true){
                //System.out.println(lines[count]);
                if(lines[count] == null)
                    break;
                count++;

            }

            brC.close();
        }catch(FileNotFoundException e){
            System.out.println("File read error");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static void chromeExtract(){
        int count = 0;
        int miscCount = 0;
        String line;
        String link = "";
        String desc = "";

        //while loop goes through each line of HTML code

        while(true) {
            line = lines[count];
            //System.out.println(line);
            if (line == null)
                break;

            if (line.contains("HREF")) {
                int qCount, bCount;
                boolean write = false;
                qCount = 0;
                bCount = 0;
                //Main loop to extract URLs
                for (int x = 0; x < line.length(); x++) {
                    //if write is true, all proceeding chars are written to link string until next \"
                    if (line.charAt(x) == '\"') {
                        qCount++;
                        if (qCount == 2)
                            break;
                        if (write)
                            write = false;
                        else
                            write = true;
                    }
                    if (write) {
                        link = link + line.charAt(x);
                    }
                }
                link = link.substring(1);


                write = false;
                //For loop to get description from same line as URL
                for (int x = line.length() - 1; x > 0; x--) {
                    //if write is true, all proceeding chars are written to link string until next \"
                    //System.out.println(line.charAt(x));
                    //If block breaks out of loop after full description is copied
                    if (line.charAt(x) == '>') {
                        bCount++;
                        if (bCount == 2)
                            break;
                    }

                    //If block turns write switch on and off
                    else if(line.charAt(x) == '<'){
                        write = true;
                    }

                    if (write) {
                        desc = line.charAt(x) + desc;
                    }
                }

                desc = desc.substring(0,desc.length()-1);

                if(link.contains("http")) {
                    //System.out.println(link);
                    //System.out.println(desc);
                    bmArr[total][0] = link;
                    bmArr[total][1] = desc;
                    total++;
                }else {
                    miscCount++;
                }

                desc = "";
                link = "";
            }
            count++;
        }
    }


    //=================FILE METHODS===================//

    private static void writeToFile(){
        writeToFile("MAIN.txt");
    }

    private static void writeToFile(String file){

        try {
            PrintWriter writer = new PrintWriter("Resources/"+file, "UTF-8");

            for(int x = 0;x < total; x++){
                writer.println(bmArr[x][0] + "  =====  " + bmArr[x][1]);
            }

            writer.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void readMainFile(){

        try{
            BufferedReader brC = new BufferedReader(new InputStreamReader(new FileInputStream("Resources/"+"MAIN.txt")));
            String line;
            String[] split = new String[2];
            while((line = brC.readLine()) != null){
                //System.out.println(line);
                split = line.split("  =====  ");
                //System.out.println(split[0]+" ||||||||| "+split[1]);
                bmArr[total][0] = split[0];
                if(split.length == 1)
                    bmArr[total][1] = "";
                else
                    bmArr[total][1] = split[1];
                total++;
            }

            brC.close();
        }catch(FileNotFoundException e){
            System.out.println("File read error");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void generateHTMLfile(){

        try{
            PrintWriter writer = new PrintWriter("bookmarks_final.html", "UTF-8");

            //Print file header
            writer.println( "<!DOCTYPE NETSCAPE-Bookmark-file-1>\n" +
                                "<!-- This is an automatically generated file.\n" +
                                "     It will be read and overwritten.\n" +
                                "     DO NOT EDIT! -->\n" +
                                "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n" +
                                "<TITLE>Bookmarks</TITLE>\n" +
                                "<H1>Bookmarks</H1>\n" +
                                "<DL><p>");

            writer.println( "\t<DT><H3 ADD_DATE=\"" + updateTime() +
                                "\" LAST_MODIFIED=\"" + updateTime() +
                                "\" PERSONAL_TOOLBAR_FOLDER=\"true\">Bookmarks bar</H3>\n\t<DL><p>");

            for(int x = 0; x < total; x++){
                writer.println( "\t\t<DT><A HREF=\"" + bmArr[x][0] + "\" " +
                                "ADD_DATE=\"" + updateTime() + "\" " +
                                "ICON=\"data:image/png;base64" + "" +"\">" +
                                bmArr[x][1] + "</A> ");
            }

            writer.println("\t</DL><p>\n</DL><p>");


            writer.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }


    //=================ARRAY METHODS===================//

    private static void printArray(){
        for(int x = 0;x<total;x++){
            System.out.println(bmArr[x][0] + "  =====  " + bmArr[x][1]);
        }
        System.out.println("Total bookmarks = "+total);
    }

    private static void createList(){
        String[] ffFiles = {"FF1.html", "FF2.html", "FF3.html", "FF4.html", "FF5.html", "FF6.html", "FF7.html", "FF8.html", "FF9.html", "FF10.html", "FF11.html", "FF12(01-27-16).html", "bm_firefox.txt"};
        String[] cFiles = {"bm_chrome.txt", "C1.html"};
        String[] oFiles = {"bm_opera.txt", "O1.html", "O2.html"};

        for(int x = 0;x < ffFiles.length;x++){
            importFirefox(ffFiles[x]);
            firefoxExtract();
        }

        for(int x = 0;x < cFiles.length;x++){
            importChrome(cFiles[x]);
            chromeExtract();
        }

        for(int x = 0;x < oFiles.length;x++){
            importOpera(oFiles[x]);
            operaExtract();
        }

    }

    private static void resize(){

        String[][] a = new String[total][2];

        for(int x = 0; x < total; x++){
            a[x][0] = bmArr[x][0];
            a[x][1] = bmArr[x][1];
        }

        bmArr = a;

    }

    private static void sort(){

        String[] split = new String[2];
        String link1;
        String link2;
        for(int x = 0; x < total-1; x++){

            for(int y = 0; y < total-(x+1); y++){
                link1 = bmArr[y][0];
                split = link1.split("://");
                //If the url doesn't contain "www" then insert it and update main bmArr array
                if(!(split[1].substring(0,4).equals("www."))){
                    split[1] = "www." + split[1];
                    //System.out.println("Converting: " + bmArr[y][0] + "  --TO--  " + split[0]+"://"+split[1]);
                    bmArr[y][0] = split[0]+"://"+split[1];
                }
                //System.out.println(split[1]);
                link1 = split[1];

                link2 = bmArr[y+1][0];
                split = link2.split("://");
                if(!(split[1].substring(0,4).equals("www."))){
                    split[1] = "www." + split[1];
                    //System.out.println("Converting: " + bmArr[y][0] + "  --TO--  " + split[0]+"://"+split[1]);
                    bmArr[y+1][0] = split[0]+"://"+split[1];
                }
                //System.out.println(split[1]);
                link2 = split[1];

                int comp = link1.compareToIgnoreCase(link2);

                if(comp > 0){

                    String temp1 = bmArr[y][0];
                    String temp2 = bmArr[y][1];

                    bmArr[y][0] = bmArr[y+1][0];
                    bmArr[y][1] = bmArr[y+1][1];

                    bmArr[y+1][0] = temp1;
                    bmArr[y+1][1] = temp2;

                }
            }
        }
    }

    private static void clearDuplicates(){

        //Make a new temp array to store unique links, becomes new bmArr
        String[][] arr = new String[6000][2];
        int newTotal = 0;
        String oldLink = "";
        String newLink = "";

        //List is alphabetical here
        //For loop will go through and store only the first instance of each unique url it encounters
        //Compares only URLs, uncomment lines with desc for description
        //===================//
        /*String oldDesc = "";
        String newDesc = "";*/
        for(int x = 0;x < total; x++){
            //remove http(s):// from urls
            String[] split = bmArr[x][0].split("://");
            newLink = split[1];
            //newDesc = bmArr[x][1];

            if(!newLink.equals(oldLink)/* || !newDesc.equals(oldDesc)*/){
                arr[newTotal][0] = bmArr[x][0];
                arr[newTotal][1] = bmArr[x][1];
                newTotal++;
            }
            oldLink = newLink;
            //oldDesc = newDesc;
        }
        bmArr = arr;
        total = newTotal;
    }

    private static void count(){

        String[][] domains = new String[2000][2];
        String[] split;
        String link;
        String dom = "";
        int domainCount = 0;

        for(int x = 0; x < total; x++){

            split = bmArr[x][0].split("://www.");
            link = split[1];

            split = link.split("/");
            //System.out.println(split[0]);

            link = split[0];
            split = link.split("\\.");
            dom = split[split.length-2];

            if(domainCount==0){
                domains[domainCount][0] = dom;
                domains[domainCount][1] = "1";
                domainCount++;

            }else {
                boolean flag = true;
                for (int y = domainCount - 1; y >= 0; y--) {
                    if (dom.equals(domains[y][0])) {
                        int temp = Integer.parseInt(domains[y][1]);
                        temp++;
                        domains[y][1] = String.valueOf(temp);
                        flag = false;
                        break;
                    }
                }

                if (flag) {
                    domains[domainCount][0] = dom;
                    domains[domainCount][1] = "1";
                    domainCount++;

                }
            }
        }


        //For loop to sort by most used domains
        for(int a = 0;a < domainCount-1;a++){
            for(int b = 0;b < domainCount-(a+1);b++){
                int temp1 = Integer.parseInt(domains[b][1]);
                int temp2 = Integer.parseInt(domains[b+1][1]);

                if(temp1 < temp2){

                    String tem = domains[b][0];
                    domains[b][0] = domains[b+1][0];
                    domains[b+1][0] = tem;

                    tem = domains[b][1];
                    domains[b][1] = domains[b+1][1];
                    domains[b+1][1] = tem;
                }
            }
        }



        //While loop prints all domains and how many times it was found
        int num = 0;
        while(true){
            if(domains[num][0] == null)
                break;
            System.out.println(domains[num][1] + "\t\t" + domains[num][0]);
            num++;
        }
        System.out.println("\nTotal domains: " + domainCount);
    }


    //=================OTHER METHODS===================//

    private static void emptyLines(){
        for(int x = 0;x<2000;x++){
            lines[x] = null;
        }
    }

    private static long updateTime(){
        return System.currentTimeMillis() / 1000L;
    }


}
