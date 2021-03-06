import java.io.*;

/**
 * Created by Igor on 030. Jul 30, 16.
 */
public class Main {

    private static String[][] bmArr = new String[6000][4];
    private static String[][][] bmArrSep;
    private static int numGroups = 0;
    private static String[] lines = new String[2000];
    private static int total = 0;


    public static void main (String[] args){

        System.out.println("Process Started\n\n");
        long startTime = System.currentTimeMillis();
        long endTime;

        //0 for using existing main file
        //1 for a new rewrite of main bookmark file using html files
        int refresh = 0;

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

        createDomainGroups();
        sortGroups();
        generateHTMLfile();

        endTime = System.currentTimeMillis();
        System.out.println("\n\n\nProcess Complete\nRUNTIME: " + ((endTime - startTime)/1000L) + " sec  " + ((endTime - startTime)%1000L) + " ms");


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
            //LINE FORMAT:  <DT><A HREF="http://luxury-alliance.tumblr.com/">The Luxury Alliance</A></DT>
            //qCount counts up to 2 quotes (") to break out of loop (this will avoid the quotes ("") in description part of line)
            boolean writeMode = false;
            qCount = 0;
            bCount = 0;
            for(int x=0;x < line.length();x++){

                //if write mode is true, all proceeding chars are written to link string variable until next quote \" is reached
                if (line.charAt(x) == '\"'){
                    qCount++;
                    if(qCount == 2)
                        break;
                    //writeMode switch only switches when a quote \" is reached
                    if(writeMode)
                        writeMode = false;
                    else
                        writeMode = true;
                }
                if(writeMode){
                    link = link + line.charAt(x);
                }
            }

            //For loop to gather descriptions of links
            for(int x=0;x<line.length();x++){
                //if write is true, all proceeding chars are written to link string until next >
                if(line.charAt(x) == '>'){
                    bCount++;
                    if(bCount == 2){
                        //Cut off end tags
                        desc = line.substring(x+1,line.length()-9);
                        break;
                    }
                }
            }

            //Gets rid of unnecessary quote at start
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
                boolean writeMode = false;

                /*LINE FORMAT:
                *
                    <DT><A HREF="http://coelhealy.bandcamp.com/album/bleak-summer-demo" ADD_DATE="1397191909" LAST_MODIFIED="1397191909" ICON_URI="http://f0.bcbits.com/img/a0442004104_3.jpg" ICON="data:image/png;base64,iVBOR.....=" LAST_CHARSET="UTF-8">▶ Bleak Summer (demo) | Coel Healy</A>
                    <DD>Bleak Summer (demo) by Coel Healy, released 09 April 2014
                    1. Yucatán Aquifer
                    2. Karakum Crater
                    3. Kep
                */


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
                        //This splits using a quote \". s[0] = HREF  and s[1] will always produce the URL
                        s = split[aa].split("\"");
                        bmArr[total][0] = s[1];

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
                //Loop starts on the last char of the line and reads right to left (in reverse)
                for (int x = line.length() - 1; x > 0; x--) {
                    //if writeMode is true, all previous chars are written to desc until ">"
                    //If block will break out of loop after full description is copied from that line
                    if (line.charAt(x) == '>') {
                        bCount++;
                        if (bCount == 2)
                            break;
                    }
                    //If block turns write switch on and off
                    else if(line.charAt(x) == '<'){
                        writeMode = true;
                    }

                    if (writeMode) {
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
                            //This exits loop if <DD> line is empty
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
                            //Entire line is part of description
                            desc = desc +" || "+ line;
                        }
                    }
                }
                bmArr[total][1] = desc;
                total++;
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
                bmArr[total][1] = desc;
                total++;
                desc = "";
            }
            lineCount++;
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
                writer.println(bmArr[x][0] + "  =====  " + bmArr[x][1] + "  =====  " + bmArr[x][2] + "  =====  " + (bmArr[x][3]==null ? "" : bmArr[x][3] ));
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
            String[] split;
            while((line = brC.readLine()) != null){
                //System.out.println(line);
                split = line.split("  =====  ");
                bmArr[total][0] = split[0];
                bmArr[total][1] = split[1];
                bmArr[total][2] = split[2];
                if(split.length == 3)
                    bmArr[total][3] = null;
                else
                    bmArr[total][3] = split[3];

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
            PrintWriter writer = new PrintWriter("bookmarks_final_grouped.html", "UTF-8");

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



            //This loop counts the number of groups there are in total
            int groupTotal = 0;
            for(int b = 0; b < bmArrSep.length; b++){
                if(bmArrSep[b][0][0] == null)
                    break;
                else
                    groupTotal++;
            }
            System.out.println("Found " + groupTotal + " groups.");


            //Loop makes a HTML folder for every group
            int numURLsInGroup = 0;
            String dom;
            boolean header4Made = false;
            boolean header3Made = false;
            boolean header2Made = false;
            boolean header1Made = false;
            for(int z = 0; z < groupTotal; z++ ){
                dom = extractDomain(0, bmArrSep[z]);
                while(!(bmArrSep[z][numURLsInGroup][0] == null))
                    numURLsInGroup++;

                //This if else block will first makes groups for websites with more than 4 favourites
                //Then it will make a group called 4 which will contain all groups of 4 URLs in subgroups
                //After that it does the same for groups of 3 and 2
                if(numURLsInGroup > 4) {

                    //Prints the folder header
                    writer.println( "\t\t<DT><H3 ADD_DATE=\"" + updateTime() +
                                    "\" LAST_MODIFIED=\"" + updateTime() + "\">" +
                                    (dom.length() <= 3 ? dom.toUpperCase() : dom.substring(0, 1).toUpperCase() + dom.substring(1)) +
                                    " (" + numURLsInGroup + ")</H3>\n\t\t<DL><p>");


                    //For loop prints all URLs in the current group/folder (bmArrSep[z])
                    for(int x = 0; x < numURLsInGroup; x++){
                        String icon = bmArrSep[z][x][3];
                        writer.println( "\t\t\t<DT><A HREF=\"" + bmArrSep[z][x][0] +
                                        "\" ADD_DATE=\"" + (bmArrSep[z][x][2] == null ? "0" : bmArrSep[z][x][2]) +
                                        (icon == null ? "" : ("\" ICON=\""+ icon)) +
                                        "\">" + bmArrSep[z][x][1] + "</A>");
                    }
                    writer.println("\t\t</DL><p>");


                //TODO: Clean this up...very messy
                }else if(numURLsInGroup == 4){

                    if(!header4Made){
                        writer.println( "\t\t<DT><H3 ADD_DATE=\"" + updateTime() + "\" LAST_MODIFIED=\"" + updateTime() + "\">" + "4 Links </H3>\n\t\t<DL><p>");
                        header4Made = true;
                    }

                    writer.println( "\t\t\t<DT><H3 ADD_DATE=\"" + updateTime() +
                            "\" LAST_MODIFIED=\"" + updateTime() + "\">" +
                            (dom.length() <= 3 ? dom.toUpperCase() : dom.substring(0, 1).toUpperCase() + dom.substring(1)) +
                            " (" + numURLsInGroup + ")</H3>\n\t\t\t<DL><p>");

                    for(int x = 0; x < numURLsInGroup; x++){
                        String icon = bmArrSep[z][x][3];
                        writer.println( "\t\t\t\t<DT><A HREF=\"" + bmArrSep[z][x][0] +
                                "\" ADD_DATE=\"" + (bmArrSep[z][x][2] == null ? "0" : bmArrSep[z][x][2]) +
                                (icon == null ? "" : ("\" ICON=\""+ icon)) +
                                "\">" + bmArrSep[z][x][1] + "</A>");
                    }
                    writer.println("\t\t\t</DL><p>");


                }else if(numURLsInGroup == 3){

                    if(!header3Made){
                        writer.println("\t\t</DL><p>");
                        writer.println( "\t\t<DT><H3 ADD_DATE=\"" + updateTime() + "\" LAST_MODIFIED=\"" + updateTime() + "\">" + "3 Links </H3>\n\t\t<DL><p>");
                        header3Made = true;
                    }

                    writer.println( "\t\t\t<DT><H3 ADD_DATE=\"" + updateTime() +
                            "\" LAST_MODIFIED=\"" + updateTime() + "\">" +
                            (dom.length() <= 3 ? dom.toUpperCase() : dom.substring(0, 1).toUpperCase() + dom.substring(1)) +
                            " (" + numURLsInGroup + ")</H3>\n\t\t\t<DL><p>");

                    for(int x = 0; x < numURLsInGroup; x++){
                        String icon = bmArrSep[z][x][3];
                        writer.println( "\t\t\t\t<DT><A HREF=\"" + bmArrSep[z][x][0] +
                                "\" ADD_DATE=\"" + (bmArrSep[z][x][2] == null ? "0" : bmArrSep[z][x][2]) +
                                (icon == null ? "" : ("\" ICON=\""+ icon)) +
                                "\">" + bmArrSep[z][x][1] + "</A>");
                    }
                    writer.println("\t\t\t</DL><p>");


                }else if(numURLsInGroup == 2){


                    if(!header2Made){
                        writer.println("\t\t</DL><p>");
                        writer.println( "\t\t<DT><H3 ADD_DATE=\"" + updateTime() + "\" LAST_MODIFIED=\"" + updateTime() + "\">" + "2 Links </H3>\n\t\t<DL><p>");
                        header2Made = true;
                    }

                    writer.println( "\t\t\t<DT><H3 ADD_DATE=\"" + updateTime() +
                            "\" LAST_MODIFIED=\"" + updateTime() + "\">" +
                            (dom.length() <= 3 ? dom.toUpperCase() : dom.substring(0, 1).toUpperCase() + dom.substring(1)) +
                            " (" + numURLsInGroup + ")</H3>\n\t\t\t<DL><p>");

                    for(int x = 0; x < numURLsInGroup; x++){
                        String icon = bmArrSep[z][x][3];
                        writer.println( "\t\t\t\t<DT><A HREF=\"" + bmArrSep[z][x][0] +
                                "\" ADD_DATE=\"" + (bmArrSep[z][x][2] == null ? "0" : bmArrSep[z][x][2]) +
                                (icon == null ? "" : ("\" ICON=\""+ icon)) +
                                "\">" + bmArrSep[z][x][1] + "</A>");
                    }
                    writer.println("\t\t\t</DL><p>");


                }else if(numURLsInGroup == 1){


                    if(!header1Made){
                        writer.println("\t\t</DL><p>");
                        writer.println( "\t\t<DT><H3 ADD_DATE=\"" + updateTime() + "\" LAST_MODIFIED=\"" + updateTime() + "\">" + "1 Link </H3>\n\t\t<DL><p>");
                        header1Made = true;
                    }

                    for(int x = 0; x < numURLsInGroup; x++){
                        String icon = bmArrSep[z][x][3];
                        writer.println( "\t\t\t<DT><A HREF=\"" + bmArrSep[z][x][0] +
                                "\" ADD_DATE=\"" + (bmArrSep[z][x][2] == null ? "0" : bmArrSep[z][x][2]) +
                                (icon == null ? "" : ("\" ICON=\""+ icon)) +
                                "\">" + bmArrSep[z][x][1] + "</A>");
                    }

                }
                numURLsInGroup = 0;
            }
            writer.println("\t\t</DL><p>");
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
        String[] ffFiles = {"FF1.html", "FF2.html", "FF3.html", "FF4.html", "FF5.html", "FF6.html", "FF7.html", "FF8.html", "FF9.html", "FF10.html", "FF11.html", "FF13.html", "FF14.html", "FF15.html", "FF12(01-27-16).html", "bm_firefox.txt"};
        String[] cFiles = {"bm_chrome.txt", "C1.html", "C2.html"};
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

        String[][] a = new String[total][4];

        for(int x = 0; x < total; x++){

            /*System.out.println(bmArr[x][0]);
            System.out.println(bmArr[x][1]);
            System.out.println(bmArr[x][2]);
            System.out.println(bmArr[x][3]);*/

            a[x][0] = bmArr[x][0];
            a[x][1] = bmArr[x][1];
            a[x][2] = bmArr[x][2];
            a[x][3] = bmArr[x][3];
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
                //This is to keep the same format among all URLs
                if(!(split[1].substring(0,4).equals("www."))){
                    split[1] = "www." + split[1];
                    //System.out.println("Converting: " + bmArr[y][0] + "  --TO--  " + split[0]+"://"+split[1]);
                    bmArr[y][0] = split[0]+"://"+split[1];
                }
                //System.out.println(split[1]);
                link1 = split[1];

                link2 = bmArr[y+1][0];
                split = link2.split("://");
                //Add a www. to the next URL as well like above
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
                    String temp3 = bmArr[y][2];
                    String temp4 = bmArr[y][3];

                    bmArr[y][0] = bmArr[y+1][0];
                    bmArr[y][1] = bmArr[y+1][1];
                    bmArr[y][2] = bmArr[y+1][2];
                    bmArr[y][3] = bmArr[y+1][3];

                    bmArr[y+1][0] = temp1;
                    bmArr[y+1][1] = temp2;
                    bmArr[y+1][2] = temp3;
                    bmArr[y+1][3] = temp4;

                }
            }
        }
    }

    private static void sortByDomain(){

        String link1;
        String link2;
        for(int x = 0; x < total-1; x++){
            for(int y = 0; y < total-(x+1); y++){

                link1 = extractDomain(y);
                link2 = extractDomain(y+1);

                if(link1.compareToIgnoreCase(link2) > 0){

                    String temp1 = bmArr[y][0];
                    String temp2 = bmArr[y][1];
                    String temp3 = bmArr[y][2];
                    String temp4 = bmArr[y][3];

                    bmArr[y][0] = bmArr[y+1][0];
                    bmArr[y][1] = bmArr[y+1][1];
                    bmArr[y][2] = bmArr[y+1][2];
                    bmArr[y][3] = bmArr[y+1][3];

                    bmArr[y+1][0] = temp1;
                    bmArr[y+1][1] = temp2;
                    bmArr[y+1][2] = temp3;
                    bmArr[y+1][3] = temp4;

                }
            }
        }
        writeToFile("DOMAIN_SORTED.txt");
    }

    private static void clearDuplicates(){

        //Make a new temp array to store unique links, becomes new bmArr, resize after
        String[][] arr = new String[6000][4];
        int newTotal = 0;
        String oldLink = "";
        String newLink;
        String[] split;
        for(int x = 0;x < total; x++){
            //remove http(s):// from urls
            split = bmArr[x][0].split("://");
            newLink = split[1];


            //TODO: This only checks URLS. Check for icons as well as some are getting discarded
            if(!newLink.equals(oldLink)){
                arr[newTotal][0] = bmArr[x][0];
                arr[newTotal][1] = bmArr[x][1];
                arr[newTotal][2] = bmArr[x][2];
                arr[newTotal][3] = bmArr[x][3];
                newTotal++;
            }
            oldLink = newLink;
        }
        bmArr = arr;
        total = newTotal;
    }

    private static void createDomainGroups(){

        String[][] domains = new String[2000][2];
        bmArrSep = new String[1000][500][4];
        String dom = "";
        int domainCount = 0;
        int groupLength = 0;

        sortByDomain();

        for(int x = 0; x < total; x++){

            dom = extractDomain(x);

            if(domainCount==0){
                domains[domainCount][0] = dom;
                domains[domainCount][1] = "1";

                //System.out.println("Group " + domainCount + "   ---   ADDING: " + dom + "  ----  " + bmArr[x][0]);
                bmArrSep[domainCount][groupLength][0] = bmArr[x][0];
                bmArrSep[domainCount][groupLength][1] = bmArr[x][1];
                bmArrSep[domainCount][groupLength][2] = bmArr[x][2];
                bmArrSep[domainCount][groupLength][3] = bmArr[x][3];
                numGroups++;
                domainCount++;

            }else {
                boolean flag = true;
                for (int y = domainCount - 1; y >= 0; y--) {
                    if (dom.equals(domains[y][0])) {
                        int temp = Integer.parseInt(domains[y][1]);
                        temp++;
                        domains[y][1] = String.valueOf(temp);
                        flag = false;

                        //System.out.println("Group " + y + "   ---   ADDING: " + dom + "  ----  " + bmArr[x][0]);
                        bmArrSep[y][groupLength][0] = bmArr[x][0];
                        bmArrSep[y][groupLength][1] = bmArr[x][1];
                        bmArrSep[y][groupLength][2] = bmArr[x][2];
                        bmArrSep[y][groupLength][3] = bmArr[x][3];
                        groupLength++;
                        break;
                    }
                }

                if (flag) {
                    domains[domainCount][0] = dom;
                    domains[domainCount][1] = "1";

                    //System.out.println("Group " + domainCount + "   ---   ADDING: " + dom + "  ----  " + bmArr[x][0]);
                    groupLength = 0;
                    bmArrSep[domainCount][groupLength][0] = bmArr[x][0];
                    bmArrSep[domainCount][groupLength][1] = bmArr[x][1];
                    bmArrSep[domainCount][groupLength][2] = bmArr[x][2];
                    bmArrSep[domainCount][groupLength][3] = bmArr[x][3];
                    groupLength++;
                    numGroups++;
                    domainCount++;

                }
            }
        }


        //For loop to sort by most common domains
        for(int a = 0;a < domainCount-1;a++){
            for(int b = 0;b < domainCount-(a+1);b++){

                String[][] temp1 = new String[400][4];

                int arrCount1 = 0;
                int arrCount2 = 0;

                while(!(bmArrSep[b][arrCount1][0] == null))
                    arrCount1++;

                while(!(bmArrSep[b+1][arrCount2][0] == null))
                    arrCount2++;

                if(arrCount1 < arrCount2){

                    for(int e = 0; e < 400; e++){
                        for( int f = 0; f < 4; f++){
                            temp1[e][f] = bmArrSep[b][e][f];
                            bmArrSep[b][e][f] = "";
                        }
                    }

                    for(int e = 0; e < 400; e++){
                        for( int f = 0; f < 4; f++){
                            bmArrSep[b][e][f] = bmArrSep[b+1][e][f];
                            bmArrSep[b+1][e][f] = temp1[e][f];
                            temp1[e][f] = "";

                        }
                    }
                }
            }
        }


        boolean print = false;

        if(print) {
            for (int q = 0; q < bmArrSep.length; q++) {
                if (bmArrSep[q][0][0] == null)
                    break;

                int arrCount1 = 0;
                while (!(bmArrSep[q][arrCount1][0] == null))
                    arrCount1++;

                System.out.println("Group " + q + "  -  " + arrCount1 + " Links");
                for (int r = 0; r < bmArrSep[q].length; r++) {
                    if (bmArrSep[q][r][0] == null)
                        break;
                    System.out.println("\t" + (r + 1) + " ---> " + bmArrSep[q][r][0]);
                }
                System.out.println("\n\n");

            }
        }

    }

    private static void sortGroups(){


        for(int x = 0; x < numGroups; x++) {
            long link1;
            long link2;
            int numURLsInGroup = 0;

            while (!(bmArrSep[x][numURLsInGroup][0] == null))
                numURLsInGroup++;

            if (numURLsInGroup > 1) {
                for (int a = 0; a < numURLsInGroup - 1; a++) {

                    for (int b = 0; b < numURLsInGroup - (a + 1); b++) {
                        link1 = Long.parseLong(bmArrSep[x][b][2]);
                        link2 = Long.parseLong(bmArrSep[x][b + 1][2]);

                        if (link1 > link2) {

                            String temp1 = bmArrSep[x][b][0];
                            String temp2 = bmArrSep[x][b][1];
                            String temp3 = bmArrSep[x][b][2];
                            String temp4 = bmArrSep[x][b][3];

                            bmArrSep[x][b][0] = bmArrSep[x][b + 1][0];
                            bmArrSep[x][b][1] = bmArrSep[x][b + 1][1];
                            bmArrSep[x][b][2] = bmArrSep[x][b + 1][2];
                            bmArrSep[x][b][3] = bmArrSep[x][b + 1][3];

                            bmArrSep[x][b + 1][0] = temp1;
                            bmArrSep[x][b + 1][1] = temp2;
                            bmArrSep[x][b + 1][2] = temp3;
                            bmArrSep[x][b + 1][3] = temp4;


                        }
                    }
                }
            }
        }
    }


    //=================OTHER METHODS===================//

    public static String extractDomain(int x){ return extractDomain(x, bmArr);}

    private static String extractDomain(int x, String[][] extractArray){

        String[] split;
        String link;

        //Example URL: https://www.easyweb.td.com/waw/idp/login.htm?execution=e1s1

        split = extractArray[x][0].split("://www.");
        link = split[1];
        //SPLIT MAKES: "https      ://www.      easyweb.td.com/waw/idp/login.htm?execution=e1s1"

        split = link.split("/");
        link = split[0];
        //SPLIT MAKES: "easyweb.td.com    /    waw    /    idp    /     login.htm?execution=e1s1"

        split = link.split("\\.");
        //SPLIT MAKES: "easyweb   .   td   .   com"

        //This if else block will determine what the main domain is
        //if the second-last element of split array is 2 or less characters long, that element is probably the first part of a top level domain (such as gc.ca or co.uk)
        //therefore, instead of being the second-last element, the main domain would be the third-last element of split array (the element BEFORE gc or co)
        //ONE EXCEPTION IS HARDCODED IN (td) since it breaks above rules. More domains can be hard coded in by duplicating last condition (main domain is 2 or less characters)
        if(     split[split.length-2].length() <= 2 &&
                split.length > 2 &&
                !split[split.length-2].equals("td"))
        {
            return split[split.length-3];

        }else{
            return split[split.length-2];
        }
    }

    private static void emptyLines(){
        for(int x = 0;x<2000;x++){
            lines[x] = null;
        }
    }

    private static long updateTime(){
        return System.currentTimeMillis() / 1000L;
    }


}
