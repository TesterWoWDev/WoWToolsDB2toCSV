import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Runner {
    public static HashMap<String, String> fileIDs;
    public static HashMap<String, String> modelFDID;
    public static HashMap<String, String> textureFDID;
    public static String delimiter = ",";
//creature extra display
    public static String head = "0";
    public static String shoulder = "0";
    public static String chest = "0";
    public static String belt = "0";
    public static String legs = "0";
    public static String boots = "0";
    public static String rings = "0";
    public static String gloves = "0";
    public static String wrist = "0";
    public static String cape = "0";
//item textures
    public static String upArm = "\"\"";
    public static String lowArm = "\"\"";
    public static String hands = "\"\"";
    public static String upTor = "\"\"";
    public static String lowTor = "\"\"";
    public static String upLeg = "\"\"";
    public static String lowLeg = "\"\"";
    public static String foot = "\"\"";

    public static void main(String[] args) throws IOException
    {
       startupTables();
       creatureDB2Convert();
       itemDB2Convert();
    }
    public static void startupTables() throws IOException {
        System.out.println("Starting Tables...");
        fileIDs = setupFDIDMap("listfile/listfile.csv");
        modelFDID = setupModelMap("listfile/modelfiledata.csv");
        textureFDID = setupTextureMap("listfile/texturefiledata.csv");
    }
    public static void creatureDB2Convert() throws IOException
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

    public static void itemDB2Convert() throws IOException {
        System.out.println("Starting Items...");
        HashMap<String, String> itemDisplayInfoMaterials = setupDisplayExtraItemsMap("item/itemdisplayinfomaterialres.csv");
        HashMap<String, String> itemmodifiedappearance = setupItemModMap("item/itemmodifiedappearance.csv");
        HashMap<String, String> itemappearance = setupItemAppMap("item/itemappearance.csv");
        HashMap<String, String> itemappearanceIcon = setupItemAppIconMap("item/itemappearance.csv");
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
                if(itemappearanceIcon.get(displayRow[0]) != null)
                    if(fileIDs.get(itemappearanceIcon.get(displayRow[0])) != null){
                        icon = fileIDs.get(itemappearanceIcon.get(displayRow[0]));
                        icon = "\"" + icon.split("/")[icon.split("/").length -1].substring(0,icon.split("/")[icon.split("/").length -1].length() -4) + "\"";
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

    public static HashMap<String, String> setupMap(String filename) throws IOException
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

    public static HashMap<String, String> setupFDIDMap(String filename) throws IOException
    {
        String delim = ";";
        HashMap<String, String> hm = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while ( (line = br.readLine()) != null ) {
            String[] values = line.split(delim);
            hm.put(values[0],values[1]);
        }
        br.close();
        return hm;
    }

    public static HashMap<String, String> setupItemModMap(String filename) throws IOException
    {
        HashMap<String, String> hm = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while ( (line = br.readLine()) != null ) {
            String[] values = line.split(delimiter);
            hm.put(values[1],values[3]);
        }
        br.close();
        return hm;
    }

    public static HashMap<String, String> setupItemAppMap(String filename) throws IOException
    {
        HashMap<String, String> hm = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while ( (line = br.readLine()) != null ) {
            String[] values = line.split(delimiter);
            hm.put(values[0],values[2] + delimiter + values[3]);
        }
        br.close();
        return hm;
    }

    public static HashMap<String, String> setupItemAppIconMap(String filename) throws IOException
    {
        HashMap<String, String> hm = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while ( (line = br.readLine()) != null ) {
            String[] values = line.split(delimiter);
            hm.put(values[2],values[3]);
        }
        br.close();
        return hm;
    }

    public static HashMap<String, String> setupDisplayExtraItemsMap(String filename) throws IOException
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
            if (!last.equals(values[3]) && !last.equals("")){
                hm.put(values[3], perline.toString());
                perline = new StringBuilder();
            }
            perline.append(values[1]).append(splitter).append(values[2]).append(delimiter);
            last = values[3];
        }
        br.close();
        return hm;
    }

    public static HashMap<String, String> setupModelMap(String filename) throws IOException
    {
        HashMap<String, String> hm = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while ( (line = br.readLine()) != null ) {
            String[] values = line.split(delimiter);
            hm.put(values[3],values[0]);
        }
        br.close();
        return hm;
    }

    public static HashMap<String, String> setupTextureMap(String filename) throws IOException
    {
        HashMap<String, String> hm = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while ( (line = br.readLine()) != null ) {
            String[] values = line.split(delimiter);
            hm.put(values[2],values[0]);
        }
        br.close();
        return hm;
    }

    public static void resetVarsCreature()
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

    public static void setVarsCreature(String[] curr)
    {
        switch (curr[1])
        {
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

    public static void resetVarsItem()
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

    public static void setVarsItem(String[] curr) {
        String delim = "/";
        String data = fileIDs.get(textureFDID.get(curr[1]));
        if(data != null){
            data = data.split(delim)[data.split(delim).length -1];
            data = data.substring(0,data.length() - 9);
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
