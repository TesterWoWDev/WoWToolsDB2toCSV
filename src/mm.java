import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class mm {
    public static void main(String[] args) throws IOException
    {
        FileWriter spellSQL = new FileWriter("SpellSQL.csv");
        try (BufferedReader br = new BufferedReader(new FileReader("spell.csv"))) {
            String line;
            br.readLine();//skip header
            while ((line = br.readLine()) != null) {
                String[] displayRow = line.split(",");
                System.out.println(Arrays.toString(displayRow));
                if(isParsable(displayRow[0].replace("\"","")))
                spellSQL.write("UPDATE `item_template` SET `Name_lang` = '" + displayRow[1].replace("\"","") + "', `Description_lang = '" + displayRow[3].replace("\"","") + "', `AuraDescription_lang` = '" + displayRow[4].replace("\"","") + "', `NameSubtext_lang` = '" + displayRow[2].replace("\"","") + "' WHERE `ID` = " +  displayRow[0].replace("\"","") + ";\n");
            }
        }
        spellSQL.close();

    }
    public static boolean isParsable(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }
}
