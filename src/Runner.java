import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;

    public class Runner {
    private static HashMap<String, String> fileIDs;
    private static HashMap<String, String> modelFDID;
    private static HashMap<String, String> textureFDID;
    private static final String delimiter = ",";
//creature extra display
    private static String head = "0";
    private static String shoulder = "0";
    private static String chest = "0";
    private static String belt = "0";
    private static String legs = "0";
    private static String boots = "0";
    private static String rings = "0";
    private static String gloves = "0";
    private static String wrist = "0";
    private static String cape = "0";
//item textures
    private static String upArm = "\"\"";
    private static String lowArm = "\"\"";
    private static String hands = "\"\"";
    private static String upTor = "\"\"";
    private static String lowTor = "\"\"";
    private static String upLeg = "\"\"";
    private static String lowLeg = "\"\"";
    private static String foot = "\"\"";
//shit to do
    private static String buildNumber = "9.0.1.35482";
    private static final String[] tables = new String[12];

    public static void main(String[] args) throws IOException
    {
       Scanner keyboard = new Scanner(System.in);

       fillTable();
       setupFolders();
       System.out.println("Would you like to download the CSVs? True or False(Recommended first use)");
       boolean download = keyboard.nextBoolean();
       if(download) {
           setupFolders();
           System.out.println("What is the current build? (Get this from WoW.Tools) Default is: "+buildNumber);
           buildNumber = keyboard.nextLine();
           buildNumber = keyboard.nextLine();
           downloadFiles();
           sortInfoMatRes();
       }
       startupTables();
       creatureDB2Convert();
       itemDB2Convert();
    }
    private static void sortInfoMatRes() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("item/itemdisplayinfomaterialres.csv"));
        Map<String, List<String>> map = new TreeMap<>();
        String line;
        reader.readLine();//skip header
        while ((line = reader.readLine()) != null) {
            String key = getField(line);
            List<String> l = map.computeIfAbsent(key, k -> new LinkedList<>());
            l.add(line);
        }
        reader.close();
        FileWriter writer = new FileWriter("item/itemdisplayinfomaterialresSorted.csv");
        for (List<String> list : map.values()) {
            for (String val : list) {
                writer.write(val);
                writer.write("\n");
            }
        }
        writer.close();
    }

    private static String getField(String line) {
        return line.split(",")[3];// extract value you want to sort on
    }

    private static void fillTable(){
        tables[0] = "item/item";
        tables[1] = "item/itemappearance";
        tables[2] = "item/itemdisplayinfo";
        tables[3] = "item/itemdisplayinfomaterialres";
        tables[4] = "item/itemmodifiedappearance";
        tables[5] = "item/itemsearchname";
        tables[6] = "creature/creaturedisplayinfo";
        tables[7] = "creature/creaturedisplayinfoextra";
        tables[8] = "creature/creaturemodeldata";
        tables[9] = "creature/npcmodelitemslotdisplayinfo";
        tables[10] = "listfile/modelfiledata";
        tables[11] = "listfile/texturefiledata";
    }

    private static void setupFolders(){
        System.out.println("Setting up folders...");
        File file = new File("./item");
        boolean make = file.mkdir();
        if(!make)
            System.out.println("Error creating Item folder(possibly already exists)");
        file = new File("./creature");
        make = file.mkdir();
        if(!make)
            System.out.println("Error creating Creature folder(possibly already exists)");
        file = new File("./listfile");
        make = file.mkdir();
        if(!make)
            System.out.println("Error creating Listfile folder(possibly already exists)");
        file = new File("./export");
        make = file.mkdir();
        if(!make)
            System.out.println("Error creating Export folder(possibly already exists)");
    }

    private static void downloadFiles() throws IOException {
        System.out.println("Starting downloads...");
        for (String table : tables) {
            System.out.println("Currently downloading... " + table.split("/")[1]);
            InputStream in = new URL("https://wow.tools/dbc/api/export/?name=" + table.split("/")[1] + "&build=" + buildNumber).openStream();
            ReadableByteChannel readableByteChannel = Channels.newChannel(in);
            FileOutputStream fileOutputStream = new FileOutputStream("./" + table + ".csv");
            fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        }
        System.out.println("Currently downloading... listfile");
        InputStream in = new URL("https://wow.tools/casc/listfile/download/csv/unverified").openStream();
        ReadableByteChannel readableByteChannel = Channels.newChannel(in);
        FileOutputStream fileOutputStream = new FileOutputStream("./listfile/listfile.csv");
        fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
    }

    private static void startupTables() throws IOException {
        System.out.println("Starting Tables...");
        fileIDs = setupFDIDMap();
        modelFDID = setupModelMap();
        textureFDID = setupTextureMap();
    }

    private static void creatureDB2Convert() throws IOException
    {
        System.out.println("Starting Creatures...");
        FileWriter creatureModelWriter = new FileWriter("export/CreatureModelInfoNew.csv");
        FileWriter creatureDisplayWriter = new FileWriter("export/CreatureDisplayInfoNew.csv");
        FileWriter creatureDisplayExtraWriter = new FileWriter("export/CreatureDisplayExtraNew.csv");
        HashMap<String, String> modelData = setupMap("creature/creaturemodeldata.csv");
        HashMap<String, String> displayExtra = setupMap("creature/creaturedisplayinfoextra.csv");
        HashMap<String, String> displayExtraItems = setupDisplayExtraItemsMap("creature/npcmodelitemslotdisplayinfo.csv");
        HashMap<String, String> modelMap = new HashMap<>();
        HashMap<String, String> displayExtraMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("creature/creaturedisplayinfo.csv"))) {
            String line;
            br.readLine();//skip header
            while ((line = br.readLine()) != null) {
                String[] displayRow = line.split(delimiter);
                String text1 = fileIDs.get(displayRow[24]);
                String text2 = fileIDs.get(displayRow[25]);
                String text3 = fileIDs.get(displayRow[26]);
                if (text1 == null) {
                    text1 = "\"\"";
                } else if (!text1.equals("\"\"")) {//remove .blp extension
                    text1 = "\"" + text1.substring(0, text1.length() - 4) + "\"";
                }
                if (text2 == null) {
                    text2 = "\"\"";
                } else if (!text2.equals("\"\"")) {
                    text2 = "\"" + text2.substring(0, text2.length() - 4) + "\"";
                }
                if (text3 == null) {
                    text3 = "\"\"";
                } else if (!text3.equals("\"\"")) {
                    text3 = "\"" + text3.substring(0, text3.length() - 4) + "\"";
                }
                if (modelData.get(displayRow[1]) != null) {
                    String[] modelRow = (modelData.get(displayRow[1])).split(delimiter);
                    String modelLine = fileIDs.get(modelRow[8]);
                    if (!displayRow[7].equals("0")) {
                        if (displayExtra.get(displayRow[7]) != null && displayExtraItems.get(displayRow[7]) != null) {
                            String displayExtraLine = displayExtra.get(displayRow[7]);
                            String[] extraSplit = displayExtraLine.split(delimiter);
                            String displayExtraItemsLine = displayExtraItems.get(displayRow[7]);
                            String[] itemSplit = displayExtraItemsLine.split(delimiter);
                            for (String s : itemSplit) {
                                String[] currItem = s.split("\\.");
                                setVarsCreature(currItem);
                            }
                            String a = fileIDs.get(extraSplit[10]);
                            if (a != null) {
                                String texture = a.split("/")[a.split("/").length - 1];
                                displayExtraMap.put(extraSplit[0],extraSplit[0] + delimiter + extraSplit[1] + delimiter + extraSplit[2] + delimiter + extraSplit[4] + delimiter + extraSplit[5] + delimiter + extraSplit[6] + delimiter + extraSplit[7] + delimiter + extraSplit[8] + delimiter + head + delimiter + shoulder + delimiter + chest + delimiter + belt + delimiter + legs + delimiter + boots + delimiter + rings + delimiter + gloves + delimiter + wrist + delimiter + cape + delimiter + "0" + delimiter + texture + ",\n");
                                resetVarsCreature();
                            }
                        }
                    }
                    if (modelLine != null) {
                        String path; //NOTE if there are dupes(write the model and displayextra to a linked list, then loop it at the end and write)
                        path = "\"" + modelLine.substring(0, modelLine.length() - 1) + "dx" + "\"";
                        modelMap.put(modelRow[0],modelRow[0] + delimiter + modelRow[7] + delimiter + path + delimiter + "1" + delimiter + modelRow[19] + delimiter + modelRow[26] + delimiter + modelRow[10] + delimiter + "0x41900000" + delimiter + "0x41400000" + delimiter + "1" + delimiter + modelRow[15] + delimiter + "0" + delimiter + modelRow[17] + delimiter + "0" + delimiter + modelRow[20] + delimiter + modelRow[21] + delimiter + modelRow[30] + delimiter + modelRow[1] + delimiter + modelRow[2] + delimiter + modelRow[3] + delimiter + modelRow[4] + delimiter + modelRow[5] + delimiter + modelRow[6] + delimiter + "1" + delimiter + modelRow[22] + delimiter + modelRow[25] + delimiter + "0x0" + delimiter + "0" + ",\n");
                        creatureDisplayWriter.write(displayRow[0] + delimiter + displayRow[1] + delimiter + displayRow[2] + delimiter + displayRow[7] + delimiter + displayRow[4] + delimiter + displayRow[5] + delimiter + text1 + delimiter + text2 + delimiter + text3 + delimiter + displayRow[10] + delimiter + displayRow[7] + delimiter + displayRow[9] + delimiter + displayRow[10] + delimiter + "0" + delimiter + "0x0" + delimiter + displayRow[13] + ",\n");

                    }
                }
            }
        }
        for (@SuppressWarnings("rawtypes") Map.Entry me : modelMap.entrySet()) {
            creatureModelWriter.write(me.getValue().toString());
        }
        for (@SuppressWarnings("rawtypes") Map.Entry me : displayExtraMap.entrySet()) {
            creatureDisplayExtraWriter.write(me.getValue().toString());
        }
        creatureDisplayWriter.close();
        creatureModelWriter.close();
        creatureDisplayExtraWriter.close();
    }

    private static void itemDB2Convert() throws IOException {
        System.out.println("Starting Items...");
        HashMap<String, String> itemDisplayInfoMaterials = setupDisplayExtraItemsMap("item/itemdisplayinfomaterialresSorted.csv");
        HashMap<String, String> itemmodifiedappearance = setupItemModMap();
        HashMap<String, String> itemappearance = setupItemAppMap();
        HashMap<String, String> itemmodifiedappearanceReversed = setupItemModReversedMap();
        HashMap<String, String> itemappearanceReversed = setupItemAppReversedMap();
        HashMap<String, String> itemIcon = setupItemMap();
        HashMap<String, String> itemappearanceIcon = setupItemAppIconMap();
        FileWriter itemDisplayInfoWriter = new FileWriter("export/ItemDisplayInfoNew.csv");
        FileWriter itemWriter = new FileWriter("export/ItemNew.csv");
        FileWriter itemSQL = new FileWriter("export/itemSQL.sql");
        try (BufferedReader br = new BufferedReader(new FileReader("item/itemdisplayinfo.csv"))) {
                String line;
                br.readLine();//skip header
                while ((line = br.readLine()) != null) {
                String[] displayRow = line.split(delimiter);
                String Lmodel = fileIDs.get(modelFDID.get(displayRow[10]));
                String Rmodel = fileIDs.get(modelFDID.get(displayRow[11]));
                String Ltexture = fileIDs.get(textureFDID.get(displayRow[12]));
                String Rtexture = fileIDs.get(textureFDID.get(displayRow[13]));
                if(Lmodel == null){
                    Lmodel = "\"\"";
                }else{
                    Lmodel = Lmodel.split("/")[Lmodel.split("/").length -1];
                    Lmodel = Lmodel.substring(0,Lmodel.length() -3);
                    if(Lmodel.startsWith("helm_"))
                        Lmodel = Lmodel.substring(0,Lmodel.length() -4);
                    if(Lmodel.endsWith("_"))
                        Lmodel = Lmodel.substring(0,Lmodel.length() - 1);
                    Lmodel = "\"" + Lmodel.replaceAll("rshoulder", "lshoulder")+ ".mdx\"";

                }if(Rmodel == null){
                    Rmodel = "\"\"";
                }else{
                    Rmodel = Rmodel.split("/")[Rmodel.split("/").length -1];
                    Rmodel = Rmodel.substring(0,Rmodel.length() -3);
                    if(Rmodel.startsWith("helm_"))
                        Rmodel = Rmodel.substring(0,Rmodel.length() -4);
                    if(Rmodel.endsWith("_"))
                        Rmodel = Rmodel.substring(0,Rmodel.length() - 1);

                    Rmodel = "\"" + Rmodel.replaceAll("lshoulder", "rshoulder")+ ".mdx\"";
                }if(Ltexture == null){
                    Ltexture = "\"\"";
                }else{
                    Ltexture = Ltexture.split("/")[Ltexture.split("/").length -1];
                    Ltexture = "\"" + Ltexture.substring(0,Ltexture.length() -4)+ "\"";
                }if(Rtexture == null){
                    Rtexture = "\"\"";
                }else{
                    Rtexture = Rtexture.split("/")[Rtexture.split("/").length -1];
                    Rtexture = "\"" + Rtexture.substring(0,Rtexture.length() -4)+ "\"";
                }
                if(itemDisplayInfoMaterials.get(displayRow[0]) != null) {
                    String displayInfoMats = itemDisplayInfoMaterials.get(displayRow[0]);
                    String[] itemSplit = displayInfoMats.split(delimiter);
                    for (String s : itemSplit) {
                        String[] currItem = s.split("\\.");
                        setVarsItem(currItem);
                    }
                }
                String icon = "\"\"";
                if(itemappearanceIcon.get(displayRow[0]) != null) {
                    if (fileIDs.get(itemappearanceIcon.get(displayRow[0])) != null) {
                        icon = fileIDs.get(itemappearanceIcon.get(displayRow[0]));
                        icon = "\"" + icon.split("/")[icon.split("/").length - 1].substring(0, icon.split("/")[icon.split("/").length - 1].length() - 4) + "\"";
                    }
                }else if(itemappearanceReversed.get(itemmodifiedappearanceReversed.get(displayRow[0])) != null){
                    icon = fileIDs.get(itemIcon.get(itemappearanceReversed.get(itemmodifiedappearanceReversed.get(displayRow[0]))));
                    if(icon != null)
                    icon = "\"" + icon.split("/")[icon.split("/").length - 1].substring(0, icon.split("/")[icon.split("/").length - 1].length() - 4) + "\"";
                }
                itemDisplayInfoWriter.write(displayRow[0] + delimiter + Lmodel + delimiter + Rmodel + delimiter + Ltexture + delimiter + Rtexture + delimiter + icon + delimiter + "\"\"" + delimiter + displayRow[16] + delimiter + displayRow[17] + delimiter + displayRow[18] + delimiter + displayRow[9] + delimiter + displayRow[6] + delimiter + "0" + delimiter + displayRow[28] + delimiter + displayRow[29] + delimiter + upArm + delimiter + lowArm + delimiter + hands + delimiter + upTor + delimiter + lowTor + delimiter + upLeg + delimiter + lowLeg + delimiter + foot + delimiter + displayRow[1] + delimiter + displayRow[2] + ",\n");
                resetVarsItem();
            }
        }
        try (BufferedReader br = new BufferedReader(new FileReader("item/item.csv"))) {
            String line;
            br.readLine();//skip header
            while ((line = br.readLine()) != null) {
                String[] displayRow = line.split(delimiter);
                String display = "0";
                if(itemmodifiedappearance.get(displayRow[0]) != null) {
                    if (itemappearance.get(itemmodifiedappearance.get(displayRow[0])) != null) {
                        display = itemappearance.get(itemmodifiedappearance.get(displayRow[0])).split(delimiter)[0];
                    }
                }
                itemWriter.write(displayRow[0] + delimiter + displayRow[1] + delimiter + displayRow[2] + delimiter + displayRow[6] + delimiter + displayRow[3] + delimiter + display + delimiter + displayRow[4] + delimiter + displayRow[5] + ",\n");
            }
        }
        try (BufferedReader br = new BufferedReader(new FileReader("item/itemsearchname.csv"))) {
            String line;
            br.readLine();//skip header
            while ((line = br.readLine()) != null) {
                String[] split = line.split(delimiter);
                if(split.length == 17)
                    itemSQL.write("UPDATE `item_template` SET `name` = '" + split[1].replace("'", "''").replace("\"","") + "' WHERE `entry` = " + split[2] + ";\n");
                if(split.length == 18)
                    itemSQL.write("UPDATE `item_template` SET `name` = '" + split[1].replace("'", "''").replace("\"","") + delimiter + split[2].replace("'", "''").replace("\"","") + "' WHERE `entry` = " + split[3] + ";\n");
                if(split.length == 19)
                    itemSQL.write("UPDATE `item_template` SET `name` = '" + split[1].replace("'", "''").replace("\"","") + delimiter + split[2].replace("'", "''").replace("\"","") + delimiter + split[3].replace("'", "''").replace("\"","") + "' WHERE `entry` = " + split[4] + ";\n");
            }
        }
        itemSQL.close();
        itemDisplayInfoWriter.close();
        itemWriter.close();
    }

    private static HashMap<String, String> setupMap(String filename) throws IOException
    {
        HashMap<String, String> hm = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        br.readLine();//skip header
        while ((line = br.readLine()) != null) {
            String[] values = line.split(delimiter);
            hm.put(values[0], line);
        }
        br.close();
        return hm;
    }

    private static HashMap<String, String> setupFDIDMap() throws IOException
    {
        String delim = ";";
        HashMap<String, String> hm = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader("listfile/listfile.csv"));
        String line;
        while ( (line = br.readLine()) != null ) {
            String[] values = line.split(delim);
            hm.put(values[0],values[1]);
        }
        br.close();
        return hm;
    }

    private static HashMap<String, String> setupItemModMap() throws IOException
    {
        HashMap<String, String> hm = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader("item/itemmodifiedappearance.csv"));
        String line;
        while ( (line = br.readLine()) != null ) {
            String[] values = line.split(delimiter);
            hm.put(values[1],values[3]);
        }
        br.close();
        return hm;
    }

    private static HashMap<String, String> setupItemAppMap() throws IOException
    {
        HashMap<String, String> hm = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader("item/itemappearance.csv"));
        String line;
        while ( (line = br.readLine()) != null ) {
            String[] values = line.split(delimiter);
            hm.put(values[0],values[2] + delimiter + values[3]);
        }
        br.close();
        return hm;
    }

        private static HashMap<String, String> setupItemModReversedMap() throws IOException
        {
            HashMap<String, String> hm = new HashMap<>();
            BufferedReader br = new BufferedReader(new FileReader("item/itemmodifiedappearance.csv"));
            String line;
            while ( (line = br.readLine()) != null ) {
                String[] values = line.split(delimiter);
                hm.put(values[3],values[1]);
            }
            br.close();
            return hm;
        }

        private static HashMap<String, String> setupItemAppReversedMap() throws IOException
        {
            HashMap<String, String> hm = new HashMap<>();
            BufferedReader br = new BufferedReader(new FileReader("item/itemappearance.csv"));
            String line;
            while ( (line = br.readLine()) != null ) {
                String[] values = line.split(delimiter);
                hm.put(values[2],values[0]);
            }
            br.close();
            return hm;
        }

        private static HashMap<String, String> setupItemMap() throws IOException
        {
            HashMap<String, String> hm = new HashMap<>();
            BufferedReader br = new BufferedReader(new FileReader("item/item.csv"));
            String line;
            while ( (line = br.readLine()) != null ) {
                String[] values = line.split(delimiter);
                hm.put(values[0],values[7]);
            }
            br.close();
            return hm;
        }

    private static HashMap<String, String> setupItemAppIconMap() throws IOException
    {
        HashMap<String, String> hm = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader("item/itemappearance.csv"));
        String line;
        while ( (line = br.readLine()) != null ) {
            String[] values = line.split(delimiter);
            hm.put(values[2],values[3]);
        }
        br.close();
        return hm;
    }

    private static HashMap<String, String> setupDisplayExtraItemsMap(String filename) throws IOException
    {
        String splitter = ".";
        HashMap<String, String> hm = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        StringBuilder perline = new StringBuilder();
        String last = "";
        String line;
        br.readLine();//skip header
        while ( (line = br.readLine()) != null ) {
            String[] values = line.split(delimiter);
            if (!last.equals(values[3])){
                hm.put(last, perline.toString());
                perline = new StringBuilder();
            }

            perline.append(values[1]).append(splitter).append(values[2]).append(delimiter);
            last = values[3];
        }
        br.close();
        return hm;
    }

    private static HashMap<String, String> setupModelMap() throws IOException
    {
        HashMap<String, String> hm = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader("listfile/modelfiledata.csv"));
        String line;
        while ( (line = br.readLine()) != null ) {
            String[] values = line.split(delimiter);
            hm.put(values[3],values[0]);
        }
        br.close();
        return hm;
    }

    private static HashMap<String, String> setupTextureMap() throws IOException
    {
        HashMap<String, String> hm = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader("listfile/texturefiledata.csv"));
        String line;
        while ( (line = br.readLine()) != null ) {
            String[] values = line.split(delimiter);
            hm.put(values[2],values[0]);
        }
        br.close();
        return hm;
    }

    private static void resetVarsCreature()
    {
        head = "0";
        shoulder = "0";
        chest = "0";
        belt = "0";
        legs = "0";
        boots = "0";
        rings = "0";
        gloves = "0";
        wrist = "0";
        cape = "0";
    }

    private static void setVarsCreature(String[] curr)
    {
        if(curr.length == 2) {
            switch (curr[1]) {
                case "1":
                    head = curr[0];
                    break;
                case "3":
                    shoulder = curr[0];
                    break;
                case "5":
                    chest = curr[0];
                    break;
                case "6":
                    belt = curr[0];
                    break;
                case "7":
                    legs = curr[0];
                    break;
                case "8":
                    boots = curr[0];
                    break;
                case "9":
                    wrist = curr[0];
                    break;
                case "10":
                    gloves = curr[0];
                    break;
                case "11":
                    rings = curr[0];
                    break;
                case "15":
                    cape = curr[0];
                    break;
            }
        }
    }

    private static void resetVarsItem()
    {
        upArm = "\"\"";
        lowArm = "\"\"";
        hands = "\"\"";
        upTor = "\"\"";
        lowTor = "\"\"";
        upLeg = "\"\"";
        lowLeg = "\"\"";
        foot = "\"\"";
    }

    private static void setVarsItem(String[] curr) {
        if(curr.length == 2) {
            String delim = "/";
            String data = fileIDs.get(textureFDID.get(curr[1]));
            if (data != null) {
                data = data.split(delim)[data.split(delim).length - 1];
                data = data.substring(0, data.length() - 6);
                switch (curr[0]) {
                    case "0":
                        upArm = data;
                        break;
                    case "1":
                        lowArm = data;
                        break;
                    case "2":
                        hands = data;
                        break;
                    case "3":
                        upTor = data;
                        break;
                    case "4":
                        lowTor = data;
                        break;
                    case "5":
                        upLeg = data;
                        break;
                    case "6":
                        lowLeg = data;
                        break;
                    case "7":
                        foot = data;
                        break;
                }
            }
        }
    }
}
