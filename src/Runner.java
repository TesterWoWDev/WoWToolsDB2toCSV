import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;

public class Runner {

    //updated in main method
    private static String buildNumber = "9.0.1.35482";
    //filled in fillTable
    private static final String[] tables = new String[20];
    //shit
    private static final String delimiter = ",";
    private static final String csvEndSuffix = ".csv";
    private static final String emptyQuotes = "\"\"";
    
//FileID maps
    private static HashMap<String, String> fileIDs;
    private static HashMap<String, String> modelFDID;
    private static HashMap<String, String> textureFDID;
//creature extra display
    private static String head = surroundQuotes("0");
    private static String shoulder = surroundQuotes("0");
    private static String chest = surroundQuotes("0");
    private static String belt = surroundQuotes("0");
    private static String legs = surroundQuotes("0");
    private static String boots = surroundQuotes("0");
    private static String gloves = surroundQuotes("0");
    private static String wrist = surroundQuotes("0");
    private static String cape = surroundQuotes("0");
    private static String shirt = surroundQuotes("0");
    private static String tabard = surroundQuotes("0");
//item textures
    private static String upArm = emptyQuotes;
    private static String lowArm = emptyQuotes;
    private static String hands = emptyQuotes;
    private static String upTor = emptyQuotes;
    private static String lowTor = emptyQuotes;
    private static String upLeg = emptyQuotes;
    private static String lowLeg = emptyQuotes;
    private static String foot = emptyQuotes;

    public static void main(String[] args) throws IOException
    {
       fillTable();
       startupText();
       startupTables();
       GroundEffects();
       itemDB2Convert();
       GameObject();
       //creatureDB2Convert();//bricked on newer build. effort to fix. it's displayextra

    }

    private static void startupText() throws IOException {
        Scanner keyboard = new Scanner(System.in);
        System.out.println("Would you like to download the CSVs? True or False(Recommended first use)");
        boolean download = keyboard.nextBoolean();
        if(download) {
            setupFolders();
            System.out.println("What is the current build? (Get this from WoW.Tools) Default is: "+ buildNumber + " No promises on builds past this.");
            keyboard.nextLine();//for the skip line(scanner sux)
            buildNumber = keyboard.nextLine();
            downloadFiles();
            sortTable(3);
            sortTable(15);
        }
    }
    //sorts itemdisplayinfomaterialres to put all displayIDs in groups for parsing later(cause blizzard cant keep their shit together)
    private static void sortTable(int tableNum) throws IOException {
        System.out.println("Sorting " + tables[tableNum]);
        BufferedReader reader = new BufferedReader(new FileReader(tables[tableNum] + csvEndSuffix));
        Map<String, List<String>> map = new TreeMap<>();
        String line;
        reader.readLine();//skip header
        while ((line = reader.readLine()) != null) {
            String key = getField(line);
            List<String> l = map.computeIfAbsent(key, k -> new LinkedList<>());
            l.add(line);
        }
        reader.close();
        FileWriter writer = new FileWriter(tables[tableNum] + "Sorted" + csvEndSuffix);
        for (List<String> list : map.values()) {
            for (String val : list) {
                writer.write(val);
                writer.write("\n");
            }
        }
        writer.close();
    }
    // extract value you want to sort on(for sorting itemdisplayinfomaterialres)Used in sortInfoMatRes()
    private static String getField(String line) {
        return line.split(delimiter)[3];
    }

    //table and path downloads for files
    private static void fillTable(){
        System.out.println("Filling Table...");
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
        tables[12] = "maps/groundeffectdoodad";
        tables[13] = "maps/groundeffecttexture";
        tables[14] = "gobs/gameobjectdisplayinfo";
        tables[15] = "gobs/gameobjectdisplayinfoxsoundkit";
        tables[16] = "sound/soundkit";
        tables[17] = "sound/soundkitentry";
        tables[18] = "sound/soundkitadvanced";
    }

    //creates folders, will mostly error. peeps got folders, but for startup
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
        file = new File("./maps");
        make = file.mkdir();
        if(!make)
            System.out.println("Error creating Maps folder(possibly already exists)");
        file = new File("./gobs");
        make = file.mkdir();
        if(!make)
            System.out.println("Error creating Gobs folder(possibly already exists)");
        file = new File("./sound");
        make = file.mkdir();
        if(!make)
            System.out.println("Error creating Sound folder(possibly already exists)");

    }

    //get all those csv downloads
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

    //fills tables for use later
    private static void startupTables() throws IOException {
        System.out.println("Starting Tables...");
        fileIDs = setupFDIDMap();
        modelFDID = setupModelMap();
        textureFDID = setupTextureMap();
    }

    //all creature csv creation
    private static void creatureDB2Convert() throws IOException
    {
        System.out.println("Starting Creatures...");
        FileWriter creatureModelWriter = new FileWriter("export/CreatureModelDataNew.csv");
        FileWriter creatureDisplayWriter = new FileWriter("export/CreatureDisplayInfoNew.csv");
        FileWriter creatureDisplayExtraWriter = new FileWriter("export/CreatureDisplayExtraNew.csv");
        HashMap<String, String> modelData = setupMap(tables[8] + csvEndSuffix);
        HashMap<String, String> displayExtra = setupMap(tables[7] + csvEndSuffix);
        HashMap<String, String> displayExtraItems = setupMultiMap(tables[9] + csvEndSuffix);
        HashMap<Integer, String> modelMap = new HashMap<>();
        HashMap<Integer, String> displayExtraMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(tables[6] + csvEndSuffix))) {
            String line;
            br.readLine();//skip header
            while ((line = br.readLine()) != null) {
                String[] displayRow = line.split(delimiter);
                String text1 = fileIDs.get(displayRow[24]);
                String text2 = fileIDs.get(displayRow[25]);
                String text3 = fileIDs.get(displayRow[26]);
                if (text1 == null) {
                    text1 = emptyQuotes;
                } else if (!text1.equals(emptyQuotes)) {//remove .blp extension
                    text1 = surroundQuotes(substringFour(returnLast(text1)));
                }
                if (text2 == null) {
                    text2 = emptyQuotes;
                } else if (!text2.equals(emptyQuotes)) {
                    text2 = surroundQuotes(substringFour(returnLast(text2)));
                }
                if (text3 == null) {
                    text3 = emptyQuotes;
                } else if (!text3.equals(emptyQuotes)) {
                    text3 = surroundQuotes(substringFour(returnLast(text3)));
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
                            String a = fileIDs.get(textureFDID.get(extraSplit[10]));
                            if (a != null) {
                                for (int i=1;i< extraSplit.length;i++){
                                    extraSplit[i] = surroundQuotes(extraSplit[i]).replace(".",delimiter);
                                }
                                String texture = surroundQuotes(returnLast(a));
                                displayExtraMap.put(Integer.parseInt(extraSplit[0]),surroundQuotes(extraSplit[0]) + delimiter + extraSplit[1] + delimiter + extraSplit[2] + delimiter + extraSplit[4] + delimiter + extraSplit[5] + delimiter + extraSplit[6] + delimiter + extraSplit[7] + delimiter + extraSplit[8] + delimiter + head + delimiter + shoulder + delimiter + shirt + delimiter + chest + delimiter + belt + delimiter + legs + delimiter + boots + delimiter + wrist + delimiter + gloves + delimiter + tabard + delimiter + cape + delimiter + surroundQuotes("0") + delimiter + texture +"\n");
                                resetVarsCreature();
                            }
                        }
                    }
                    if (modelLine != null) {
                        String path;
                        path = surroundQuotes(modelLine.substring(0, modelLine.length() - 1) + "dx");
                        for (int i=1;i< modelRow.length;i++){
                            modelRow[i] = surroundQuotes(modelRow[i]).replace(".",delimiter);
                        }
                        for (int i=0;i< displayRow.length;i++){
                            displayRow[i] = surroundQuotes(displayRow[i]).replace(".",delimiter);
                        }
                        modelMap.put(Integer.parseInt(modelRow[0]),surroundQuotes(modelRow[0]) + delimiter + modelRow[7] + delimiter + path.replace("/","\\") + delimiter + surroundQuotes("1") + delimiter + modelRow[25] + delimiter + modelRow[9] + delimiter + modelRow[10] + delimiter + modelRow[11] + delimiter + modelRow[12] + delimiter + modelRow[13] + delimiter + modelRow[14] + delimiter + surroundQuotes("0") + delimiter + modelRow[17] + delimiter + surroundQuotes("0") + delimiter + modelRow[20] + delimiter + modelRow[21] + delimiter + modelRow[30] + delimiter + modelRow[1] + delimiter + modelRow[2] + delimiter + modelRow[3] + delimiter + modelRow[4] + delimiter + modelRow[5] + delimiter + modelRow[6] + delimiter + surroundQuotes("1,0") + delimiter + modelRow[22] + delimiter + modelRow[25] + delimiter + surroundQuotes("0,0") + delimiter + surroundQuotes("0,0") + "\n");
                        creatureDisplayWriter.write(displayRow[0] + delimiter + displayRow[1] + delimiter + displayRow[2] + delimiter + displayRow[7] + delimiter + displayRow[4] + delimiter + displayRow[5] + delimiter + text1 + delimiter + text2 + delimiter + text3 + delimiter + surroundQuotes(displayRow[10]) + delimiter + displayRow[7] + delimiter + displayRow[9] + delimiter + displayRow[10] + delimiter + surroundQuotes("0") + delimiter + surroundQuotes("0") + delimiter + displayRow[13] + "\n");

                    }
                }
            }
        }
        modelMap = sortHashMap(modelMap);
        displayExtraMap = sortHashMap(displayExtraMap);
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

    //all item csv creation
    private static void itemDB2Convert() throws IOException {
        System.out.println("Starting Items...");
        HashMap<String, String> itemDisplayInfoMaterials = setupMultiMap(tables[3] + "Sorted" + csvEndSuffix);
        HashMap<String, String> itemModifiedAppearance = setupItemModMap();
        HashMap<String, String> itemAppearance = setupItemAppMap();
        HashMap<String, String> itemModifiedAppearanceReversed = setupItemModReversedMap();
        HashMap<String, String> itemAppearanceReversed = setupItemAppReversedMap();
        HashMap<String, String> itemIcon = setupItemMap();
        HashMap<String, String> itemAppearanceIcon = setupItemAppIconMap();
        HashMap<String, String> displayToModel = new HashMap<>();
        HashMap<String, String> itemIDtoSpell = new HashMap<>();
        HashMap<String, Integer> alreadyModel = new HashMap<>();
        FileWriter itemDisplayInfoWriter = new FileWriter("export/ItemDisplayInfoNew.csv");
        FileWriter itemWriter = new FileWriter("export/ItemNew.csv");
        FileWriter itemSQL = new FileWriter("export/itemSQL.sql");
        FileWriter helmGeoset = new FileWriter("export/HelmetGeosetVisData.csv");
        FileWriter beltListfileWriter = new FileWriter("export/BeltListfile.csv");
        FileWriter spellWriter = new FileWriter("export/Spell.csv");
        FileWriter spellVisualWriter = new FileWriter("export/SpellVisual.csv");
        FileWriter spellVisualKitWriter = new FileWriter("export/SpellVisualKit.csv");
        FileWriter spellVisualEffectNameWriter = new FileWriter("export/SpellVisualEffectName.csv");
        FileWriter spellVisualModelAttachWriter = new FileWriter("export/SpellVisualModelAttach.csv");
        int spellStartingID = 500000;

        try (BufferedReader br = new BufferedReader(new FileReader(tables[2] + csvEndSuffix))) {
                String line;
                br.readLine();//skip header
                while ((line = br.readLine()) != null) {
                String[] displayRow = line.split(delimiter);
                String Lmodel = fileIDs.get(modelFDID.get(displayRow[10]));
                String Rmodel = fileIDs.get(modelFDID.get(displayRow[11]));
                String Ltexture = fileIDs.get(textureFDID.get(displayRow[12]));
                String Rtexture = fileIDs.get(textureFDID.get(displayRow[13]));
                String Lmm = fileIDs.get(modelFDID.get(displayRow[10]));
                if(Lmodel == null){
                    Lmodel = emptyQuotes;
                }else{
                    Lmodel = returnLast(Lmodel);
                    if(Lmodel.endsWith("_r.m2"))
                        Lmodel = substringFour(Lmodel) + "l.m2";
                    Lmodel = Lmodel.substring(0,Lmodel.length() -3);
                    if(Lmodel.startsWith("helm_"))
                        Lmodel = substringFour(Lmodel);
                    if(Lmodel.endsWith("_"))
                        Lmodel = Lmodel.substring(0,Lmodel.length() - 1);
                    Lmodel = surroundQuotes(appendMDX(Lmodel.replaceAll("rshoulder", "lshoulder")));
                }if(Rmodel == null){
                    Rmodel = emptyQuotes;
                }else{
                    Rmodel = returnLast(Rmodel);
                    if(Rmodel.endsWith("_l.m2"))
                        Rmodel = substringFour(Rmodel) + "r.m2";
                    Rmodel = Rmodel.substring(0,Rmodel.length() -3);
                    if(Rmodel.startsWith("helm_"))
                        Rmodel = substringFour(Rmodel);
                    if(Rmodel.endsWith("_"))
                        Rmodel = Rmodel.substring(0,Rmodel.length() - 1);
                    Rmodel = surroundQuotes(appendMDX(Rmodel.replaceAll("lshoulder", "rshoulder")));
                }if(Ltexture == null){
                    Ltexture = emptyQuotes;
                }else{
                    Ltexture = surroundQuotes(substringFour(returnLast(Ltexture)));
                }if(Rtexture == null){
                    Rtexture = emptyQuotes;
                }else{
                    Rtexture = surroundQuotes(substringFour(returnLast(Rtexture)));
                }
                if(itemDisplayInfoMaterials.get(displayRow[0]) != null) {
                    String displayInfoMats = itemDisplayInfoMaterials.get(displayRow[0]);
                    String[] itemSplit = displayInfoMats.split(delimiter);
                    for (String s : itemSplit) {
                        String[] currItem = s.split("\\.");
                        setVarsItem(currItem);
                    }
                }
                String icon = emptyQuotes;
                if(itemAppearanceIcon.get(displayRow[0]) != null) {
                    if (fileIDs.get(itemAppearanceIcon.get(displayRow[0])) != null) {
                        icon = surroundQuotes(substringFour(returnLast((fileIDs.get(itemAppearanceIcon.get(displayRow[0]))))));
                    }
                }else if(itemAppearanceReversed.get(itemModifiedAppearanceReversed.get(displayRow[0])) != null){
                    icon = fileIDs.get(itemIcon.get(itemAppearanceReversed.get(itemModifiedAppearanceReversed.get(displayRow[0]))));
                    if(icon != null)
                    icon = surroundQuotes(substringFour(returnLast(icon)));
                }
                for (int i=0;i< displayRow.length;i++){
                    displayRow[i] = surroundQuotes(displayRow[i]).replace(".",delimiter);
                }
                itemDisplayInfoWriter.write(displayRow[0] + delimiter + Lmodel + delimiter + Rmodel + delimiter + Ltexture + delimiter + Rtexture + delimiter + icon + delimiter + emptyQuotes + delimiter + displayRow[16] + delimiter + displayRow[17] + delimiter + displayRow[18] + delimiter + displayRow[9] + delimiter +displayRow[6] + delimiter + "0" + delimiter + displayRow[28] + delimiter + displayRow[29] + delimiter + upArm + delimiter + lowArm + delimiter + hands + delimiter + upTor + delimiter + lowTor + delimiter + upLeg + delimiter + lowLeg + delimiter + foot + delimiter + displayRow[1] + delimiter + displayRow[2] + "\n");
                    displayToModel.put(displayRow[0],Ltexture.replace("\"","") + ";" + Lmm + "\n");
                resetVarsItem();
            }
        }
        try (BufferedReader br = new BufferedReader(new FileReader(tables[0] + csvEndSuffix))) {
            String line;
            br.readLine();//skip header
            while ((line = br.readLine()) != null) {
                String[] displayRow = line.split(delimiter);
                String display = "0";
                if(itemModifiedAppearance.get(displayRow[0]) != null) {
                    if (itemAppearance.get(itemModifiedAppearance.get(displayRow[0])) != null) {
                        display = itemAppearance.get(itemModifiedAppearance.get(displayRow[0])).split(delimiter)[0];
                    }
                }
                for (int i=0;i< displayRow.length;i++){
                    displayRow[i] = surroundQuotes(displayRow[i]).replace(".",delimiter);
                }
                String subClass = displayRow[2];
                if(subClass.equals("\"5\"") || subClass.equals("\"4\"")){
                    subClass = "\"1\"";
                }

                if(displayToModel.get(surroundQuotes(display)) != null){
                    if(displayRow[4].equals(surroundQuotes("6")) && !displayToModel.get(surroundQuotes(display)).split(";")[1].equals("\"\"") ){
                        String itemName = displayToModel.get(surroundQuotes(display)).split(";")[0].replace("\"","") + ".mdx";
                        if(alreadyModel.get(itemName) == null &&  !itemName.equals(".mdx")) {
                            spellWriter.write(surroundQuotes(String.valueOf(spellStartingID)) + ",\"0\",\"0\",\"0\",\"159645696\",\"160\",\"0\",\"0\",\"131139\",\"393224\",\"4096\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"1\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"101\",\"0\",\"0\",\"0\",\"0\",\"21\",\"0\",\"0\",\"0\",\"0\",\"0\",\"1\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"-1\",\"0\",\"0\",\"6\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"1\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"4\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"" + spellStartingID + "\",\"0\",\"1\",\"0\",\"0\",\"3D Belt\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"16712190\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"16712188\",\"Issa 3D Belt\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"16712190\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"16712188\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"1\",\"1\",\"1\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"1\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\"\n");
                            spellVisualWriter.write(surroundQuotes(String.valueOf(spellStartingID)) + ",\"0\",\"0\",\"0\"," + surroundQuotes(String.valueOf(spellStartingID)) + ",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"-1\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\"\n");
                            spellVisualKitWriter.write(surroundQuotes(String.valueOf(spellStartingID)) + ",\"-1\",\"-1\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"-1\",\"-1\",\"-1\",\"-1\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\"\n");
                            spellVisualEffectNameWriter.write(surroundQuotes(String.valueOf(spellStartingID)) + "," + surroundQuotes("3D Belt") + "," + surroundQuotes("item\\objectcomponents\\waist\\" + itemName) + ",\"1\",\"1\",\"0,01\",\"100\"\n");
                            spellVisualModelAttachWriter.write(surroundQuotes(String.valueOf(spellStartingID)) + "," + surroundQuotes(String.valueOf(spellStartingID)) + "," + surroundQuotes(String.valueOf(spellStartingID)) + ",\"53\",\"0\",\"0\",\"0\",\"0\",\"0\",\"0\"\n");
                            itemIDtoSpell.put(displayRow[0], String.valueOf(spellStartingID));
                            alreadyModel.put(itemName, spellStartingID);
                            if(!displayToModel.get(surroundQuotes(display)).split(";")[1].contains("null"))
                            beltListfileWriter.write(displayToModel.get(surroundQuotes(display)));

                        }
                        else{
                            itemIDtoSpell.put(displayRow[0], String.valueOf(spellStartingID));
                        }
                        spellStartingID++;
                    }
                }
                itemWriter.write(displayRow[0] + delimiter + displayRow[1] + delimiter + subClass + delimiter + displayRow[6] + delimiter + displayRow[3] + delimiter + surroundQuotes(display) + delimiter + displayRow[4] + delimiter + displayRow[5] + "\n");
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader(tables[5] + csvEndSuffix))) {
            String line;
            br.readLine();//skip header
            while ((line = br.readLine()) != null) {
                String[] split = line.split(delimiter);
                String spell = "";
                String ItemID = "";
                if(split.length == 17)
                    ItemID = split[2];
                if(split.length == 18)
                    ItemID = split[3];
                if(split.length == 19)
                    ItemID = split[4];

                if(itemIDtoSpell.get(surroundQuotes(ItemID)) != null){
                    String SpellTrigger = "1";
                    String SpellID = itemIDtoSpell.get(surroundQuotes(ItemID));
                    spell =  ", `spellid_1` = " + SpellID + ", `spelltrigger_1` = " + SpellTrigger;
                }
                if(split.length == 17)
                    itemSQL.write("UPDATE `item_template` SET `name` = '" + split[1].replace("'", "''").replace("\"","") + "', `Quality` = " + split[3] + spell + " WHERE `entry` = " + ItemID + ";\n");
                if(split.length == 18)
                    itemSQL.write("UPDATE `item_template` SET `name` = '" + split[1].replace("'", "''").replace("\"","") + delimiter + split[2].replace("'", "''").replace("\"","") +"', `Quality` = " + split[4] + spell + " WHERE `entry` = " + ItemID + ";\n");
                if(split.length == 19)
                    itemSQL.write("UPDATE `item_template` SET `name` = '" + split[1].replace("'", "''").replace("\"","") + delimiter + split[2].replace("'", "''").replace("\"","") + delimiter + split[3].replace("'", "''").replace("\"","") + "', `Quality` = " + split[5] + spell + " WHERE `entry` = " + ItemID + ";\n");
            }
        }
        try (BufferedReader br = new BufferedReader(new FileReader("item/helmetgeosetvisdata.csv"))) {
            String line;
            br.readLine();//skip header
            while ((line = br.readLine()) != null) {
                String[] split = line.split(delimiter);
                helmGeoset.write(surroundQuotes(split[0]) + delimiter + surroundQuotes(split[1]) + delimiter + surroundQuotes(split[2]) + delimiter + surroundQuotes(split[3]) + delimiter + surroundQuotes(split[4]) + delimiter + surroundQuotes(split[5]) + delimiter + surroundQuotes(split[6]) + delimiter + surroundQuotes(split[7]) + "\n");
            }
        }
        helmGeoset.close();
        itemSQL.close();
        itemDisplayInfoWriter.close();
        itemWriter.close();
        beltListfileWriter.close();
        spellVisualEffectNameWriter.close();
        spellVisualKitWriter.close();
        spellVisualModelAttachWriter.close();
        spellVisualWriter.close();
        spellWriter.close();
    }

    //ground effect texture/doodads
    private static void GroundEffects() throws IOException{
        System.out.println("Starting Ground Effects...");
        FileWriter groundEffectDoodad = new FileWriter("export/GroundEffectDoodad.csv");
        FileWriter groundEffectTexture = new FileWriter("export/GroundEffectTexture.csv");
        FileWriter doodadListfile = new FileWriter("export/GroundEffectDoodadListfile.csv");
        try (BufferedReader br = new BufferedReader(new FileReader(tables[12] + csvEndSuffix))) {
            String line;
            br.readLine();//skip header
            while ((line = br.readLine()) != null) {
                String[] split = line.split(delimiter);
                int flag = Integer.parseInt(split[2]);
                if (flag >1 && flag < 4){
                    flag = 1;
                }
                String flagString = String.valueOf(flag);
                String pathToModel = fileIDs.get(split[1]);
                if(pathToModel != null) {
                    pathToModel = returnLast(pathToModel);
                    pathToModel = pathToModel.substring(0,pathToModel.length()-2) + "mdl";
                    groundEffectDoodad.write(surroundQuotes(split[0]) + delimiter + surroundQuotes(pathToModel) + delimiter + surroundQuotes(flagString) + "\n");
                    doodadListfile.write(split[1] + ";" + fileIDs.get(split[1]) + "\n");
                }
            }
        }
        try (BufferedReader br = new BufferedReader(new FileReader(tables[13] + csvEndSuffix))) {
            String line;
            br.readLine();//skip header
            while ((line = br.readLine()) != null) {
                String[] split = line.split(delimiter);
                groundEffectTexture.write(surroundQuotes(split[0]) + delimiter + surroundQuotes(split[3]) + delimiter + surroundQuotes(split[4]) + delimiter + surroundQuotes(split[5]) + delimiter + surroundQuotes(split[6]) + delimiter + surroundQuotes(split[7]) + delimiter + surroundQuotes(split[8]) + delimiter + surroundQuotes(split[9]) + delimiter + surroundQuotes(split[10]) + delimiter + surroundQuotes(split[1]) + delimiter + surroundQuotes(split[2]) + "\n");
            }
        }
        groundEffectDoodad.close();
        groundEffectTexture.close();
    }

    //start of game object
    private static void GameObject() throws IOException{
        System.out.println("Starting Gameobjects...");
        FileWriter gameobjectDisplay = new FileWriter("export/GameObjectDisplayInfo.csv");
        FileWriter soundEntries = new FileWriter("export/SoundEntries.csv");
        FileWriter soundEntriesAdvanced = new FileWriter("export/SoundEntriesAdvanced.csv");
        HashMap<String, String> gameObjectIDToSoundKit = setupMultiMap(tables[15] + "Sorted" + csvEndSuffix);
        HashMap<String, String> soundKit = setupMap(tables[16] + csvEndSuffix);
        HashMap<String, String> soundKitEntry = setupSoundKitEntryMap();
        HashMap<Integer, String> soundEntriesMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(tables[14] + csvEndSuffix))) {
            String line;
            br.readLine();//skip header
            while ((line = br.readLine()) != null) {
                String[] split = line.split(delimiter);
                String[] sound = new String[10];
                Arrays.fill(sound, "0");//auto set to 0 when not a sound
                String a = gameObjectIDToSoundKit.get(split[0]);
                if (a != null){
                    for (int i = 0; i < a.split(",").length; i++) {
                        String[] mm = a.split(",")[i].split("\\.");
                        String soundkitLine = soundKit.get(mm[0]);
                        String[] soundkitsplit = soundkitLine.split(delimiter);
                        if(soundEntriesMap.get(Integer.parseInt(soundkitsplit[0])) == null){
                            sound[Integer.parseInt(mm[1])] = soundkitsplit[0];
                            String path = fileIDs.get(soundKitEntry.get(soundkitsplit[0]).split(delimiter)[2]);
                            String filename = path.split("/")[path.split("/").length - 1];
                            path = path.substring(0, path.length() - filename.length());
                            soundEntriesMap.put(Integer.parseInt(soundkitsplit[0]),surroundQuotes(soundkitsplit[0]) + delimiter + surroundQuotes(soundkitsplit[1]) + delimiter + surroundQuotes(filename) + delimiter + surroundQuotes(filename) + delimiter + emptyQuotes + delimiter + emptyQuotes + delimiter + emptyQuotes + delimiter + emptyQuotes + delimiter + emptyQuotes + delimiter + emptyQuotes+ delimiter + emptyQuotes + delimiter + emptyQuotes + delimiter + emptyQuotes + delimiter + surroundQuotes(soundKitEntry.get(soundkitsplit[0]).split(delimiter)[3]) + delimiter + surroundQuotes("0") + delimiter + surroundQuotes("0") + delimiter + surroundQuotes("0") + delimiter + surroundQuotes("0") + delimiter + surroundQuotes("0") + delimiter + surroundQuotes("0") + delimiter +surroundQuotes( "0") + delimiter + surroundQuotes("0") + delimiter + surroundQuotes("0") + delimiter + path + delimiter + surroundQuotes(soundkitsplit[2]) + delimiter + surroundQuotes(soundkitsplit[3]) + delimiter + surroundQuotes(soundkitsplit[4]) + delimiter + surroundQuotes(soundkitsplit[5]) + delimiter + surroundQuotes("0") + "\n");
                        }
                    }
                }
                gameobjectDisplay.write(surroundQuotes(split[0]) + delimiter + surroundQuotes(fileIDs.get(split[7])) + surroundQuotes(sound[0]) + delimiter + surroundQuotes(sound[1]) + delimiter + surroundQuotes(sound[2]) + delimiter + surroundQuotes(sound[3]) + delimiter + surroundQuotes(sound[4]) + delimiter + surroundQuotes(sound[5]) + delimiter + surroundQuotes(sound[6]) + delimiter + surroundQuotes(sound[7]) + delimiter + surroundQuotes(sound[8]) + delimiter + surroundQuotes(sound[9]) + delimiter + surroundQuotes(split[1]) + delimiter + surroundQuotes(split[2]) + delimiter + surroundQuotes(split[3]) + delimiter + surroundQuotes(split[4]) + delimiter + surroundQuotes(split[5]) + delimiter + surroundQuotes(split[6]) + delimiter + surroundQuotes(split[8]) + "\n");
            }
        }
        try (BufferedReader br = new BufferedReader(new FileReader(tables[18] + csvEndSuffix))) {
            String line;
            br.readLine();//skip header
            while ((line = br.readLine()) != null) {
                String[] split = line.split(delimiter);
                soundEntriesAdvanced.write(surroundQuotes(split[0]) + delimiter + surroundQuotes(split[1]) + delimiter + surroundQuotes(split[2]) + delimiter + surroundQuotes(split[4]) + delimiter + surroundQuotes(split[5]) + delimiter + surroundQuotes(split[6]) + delimiter + surroundQuotes(split[7]) + delimiter + surroundQuotes(split[8]) + delimiter + surroundQuotes(split[9]) + delimiter + surroundQuotes(split[10]) + delimiter + surroundQuotes(split[11]) + delimiter + surroundQuotes(split[14]) + delimiter + surroundQuotes(split[15]) + delimiter + surroundQuotes(split[16]) + delimiter + surroundQuotes(split[17]) + delimiter + surroundQuotes(split[22]) + delimiter + surroundQuotes(split[23]) + delimiter + surroundQuotes(split[24]) + delimiter + surroundQuotes(split[25]) + delimiter + surroundQuotes(split[26]) + delimiter + surroundQuotes(split[27]) + delimiter + surroundQuotes(split[28]) + delimiter + surroundQuotes(split[2]) + delimiter + surroundQuotes("") + "\n");
            }
        }
        TreeMap<Integer, String> sorted = new TreeMap<>(soundEntriesMap);
        for (Map.Entry<Integer, String> entry : sorted.entrySet())
            soundEntries.write(entry.getValue());
        soundEntries.close();
        gameobjectDisplay.close();
        soundEntriesAdvanced.close();

    }

    //general map, used in multiple places
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

    //general map, used in multiple places
    private static HashMap<String, String> setupSoundKitEntryMap() throws IOException
    {
        HashMap<String, String> hm = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(tables[17] + csvEndSuffix));
        String line;
        br.readLine();//skip header
        while ((line = br.readLine()) != null) {
            String[] values = line.split(delimiter);
            hm.put(values[1], line);
        }
        br.close();
        return hm;
    }

    //FDID map creation
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

    //itemModifiedAppearance map creation
    private static HashMap<String, String> setupItemModMap() throws IOException
    {
        HashMap<String, String> hm = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(tables[4] + csvEndSuffix));
        String line;
        while ( (line = br.readLine()) != null ) {
            String[] values = line.split(delimiter);
            hm.put(values[1],values[3]);
        }
        br.close();
        return hm;
    }

    //itemAppearance map creation
    private static HashMap<String, String> setupItemAppMap() throws IOException
    {
        HashMap<String, String> hm = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(tables[1] + csvEndSuffix));
        String line;
        while ( (line = br.readLine()) != null ) {
            String[] values = line.split(delimiter);
            hm.put(values[0],values[2] + delimiter + values[3]);
        }
        br.close();
        return hm;
    }

    //setup itemmodifiedappearance map but reversed
    private static HashMap<String, String> setupItemModReversedMap() throws IOException
    {
        HashMap<String, String> hm = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(tables[4] + csvEndSuffix));
        String line;
        while ( (line = br.readLine()) != null ) {
            String[] values = line.split(delimiter);
            hm.put(values[3],values[1]);
        }
        br.close();
        return hm;
    }

    //setup item appearance map but reversed
    private static HashMap<String, String> setupItemAppReversedMap() throws IOException
    {
        HashMap<String, String> hm = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(tables[1] + csvEndSuffix));
        String line;
        while ( (line = br.readLine()) != null ) {
            String[] values = line.split(delimiter);
            hm.put(values[2],values[0]);
        }
        br.close();
        return hm;
    }

    //setup item map
    private static HashMap<String, String> setupItemMap() throws IOException
    {
        HashMap<String, String> hm = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(tables[0] + csvEndSuffix));
        String line;
        while ( (line = br.readLine()) != null ) {
            String[] values = line.split(delimiter);
            hm.put(values[0],values[7]);
        }
        br.close();
        return hm;
    }

    //setup item appearance map for icons
    private static HashMap<String, String> setupItemAppIconMap() throws IOException
    {
        HashMap<String, String> hm = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(tables[1] + csvEndSuffix));
        String line;
        while ( (line = br.readLine()) != null ) {
            String[] values = line.split(delimiter);
            hm.put(values[2],values[3]);
        }
        br.close();
        return hm;
    }

    //setup tables npcmodelitemslotdisplayinfo and itemdisplayinfomaterialres and gameobjectdisplayinfoxsoundkit
    private static HashMap<String, String> setupMultiMap(String filename) throws IOException
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

    //setup creaturemodelinfo map
    private static HashMap<String, String> setupModelMap() throws IOException
    {
        HashMap<String, String> hm = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(tables[10] + csvEndSuffix));
        String line;
        while ( (line = br.readLine()) != null ) {
            String[] values = line.split(delimiter);
            hm.put(values[3],values[0]);
        }
        br.close();
        return hm;
    }

    //setup textureID->FDID map
    private static HashMap<String, String> setupTextureMap() throws IOException
    {
        HashMap<String, String> hm = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(tables[11] + csvEndSuffix));
        String line;
        while ( (line = br.readLine()) != null ) {
            String[] values = line.split(delimiter);
            hm.put(values[2],values[0]);
        }
        br.close();
        return hm;
    }

    //reset CreatureDisplayInfoExtra variables
    private static void resetVarsCreature()
    {
        head = surroundQuotes("0");
        shoulder = surroundQuotes("0");
        chest = surroundQuotes("0");
        belt = surroundQuotes("0");
        legs = surroundQuotes("0");
        boots = surroundQuotes("0");
        gloves = surroundQuotes("0");
        wrist = surroundQuotes("0");
        cape = surroundQuotes("0");
        shirt = surroundQuotes("0");
        tabard = surroundQuotes("0");
    }

        //set CreatureDisplayInfoExtra variables
    private static void setVarsCreature(String[] curr)
    {
        if(curr.length == 2) {
            curr[0] = surroundQuotes(curr[0]);
            switch (curr[1]) {
                case "1":
                    head = curr[0];
                    break;
                case "3":
                    shoulder = curr[0];
                    break;
                case "4":
                    shirt = curr[0];
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
                case "15":
                    cape = curr[0];
                    break;
                case "19":
                    tabard = curr[0];
                    break;
            }
        }
    }
    //reset ItemDisplayInfo variables
    private static void resetVarsItem()
    {
        upArm = emptyQuotes;
        lowArm = emptyQuotes;
        hands = emptyQuotes;
        upTor = emptyQuotes;
        lowTor = emptyQuotes;
        upLeg = emptyQuotes;
        lowLeg = emptyQuotes;
        foot = emptyQuotes;
    }
    //set ItemDisplayInfo variables
    private static void setVarsItem(String[] curr) {
        if(curr.length == 2) {
            String data = fileIDs.get(textureFDID.get(curr[1]));
            if (data != null) {
                data = surroundQuotes(returnLast(data.substring(0, data.length() - 6)));
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
    //helper functions
    //surrounds string with quotes for printout
    private static String surroundQuotes(String s) {
        return "\"" + s + "\"";
    }
    //returns last value after a split on /
    private static String returnLast(String str) {
        return str.split("/")[str.split("/").length -1];
    }
    private static String appendMDX(String str) {
        return str + ".mdx";
    }
    private static String substringFour(String str) {
        return str.substring(0, str.length() - 4);
    }
    public static LinkedHashMap<Integer, String> sortHashMap(HashMap<Integer,String> unsortedMap){
//LinkedHashMap preserve the ordering of elements in which they are inserted
        LinkedHashMap<Integer, String> sortedMap = new LinkedHashMap<>();
        ((Map<Integer, String>) unsortedMap).entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));
        return sortedMap;
    }
}
