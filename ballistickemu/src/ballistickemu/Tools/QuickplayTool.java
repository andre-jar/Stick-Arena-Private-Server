/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ballistickemu.Tools;
import java.sql.ResultSet;
import java.util.Random;
import java.util.LinkedHashMap;
import ballistickemu.Types.StickItem;
import ballistickemu.Types.StickColour;
import java.sql.SQLException;
/**
 *
 * @author Simon
 */
public class QuickplayTool {
    private static LinkedHashMap<Integer, String> FirstNames;
    private static LinkedHashMap<Integer, String> LastNames;
    
 public static boolean PopulateNameList()
    {
     FirstNames = new LinkedHashMap<>();
     LastNames = new LinkedHashMap<>();
        try
        {

            ResultSet rs = DatabaseTools.executeSelectQuery("select * from quickplay_names");
            int i_First = 0;
            int i_Last = 0;
            while (rs.next())
            {
                int type = rs.getInt("type");
                String value = rs.getString("value");

                if(type == 0)
                {
                    FirstNames.put(i_First, value);
                    i_First++;
                }
                else if(type == 1)
                {
                    LastNames.put(i_Last, value);
                    i_Last++;
                }
            }
         return (FirstNames.size() > 1) && (LastNames.size() > 1);
        }
        catch (SQLException e)
        {
            return false;
        }
    }

    public static String getRandomName()
    {
        String[] Name = new String[2];
        Name[0] = FirstNames.get(getRandomNum(0, FirstNames.size()));
        Name[1] = LastNames.get(getRandomNum(1, LastNames.size()));
        return Name[0] + Name[1];
    }

    private static int getRandomNum(int min, int max)
    {
        Random r = new Random();
        return r.nextInt(max - min) + min;
    }

    public static LinkedHashMap<Integer, StickItem> getRandomInventory()
    {
        LinkedHashMap<Integer, StickItem> Inventory = new LinkedHashMap<>();
        
        int[] color = new int[3];
        int greater128 = new Random().nextInt(3);
        int second=new Random().nextInt(3);
        while(second==greater128) {
        	second = new Random().nextInt(3);
        }
        int third = 3 - greater128 - second;
        color[greater128] = getRandomNum(128,255);
        color[second] = getRandomNum(0,255);
        int sum = color[second]+color[greater128];
        int boundary1 =  Math.max(0, sum-248);
        int boundary2 = Math.min(255, 522-sum);
        color[third] = getRandomNum(Math.min(boundary1,boundary2),Math.max(boundary1, boundary2));
        
		StickColour SpinnerCol = new StickColour(color[0], color[1], color[2],color[0], color[1], color[2]);

        int SpinnerID = 100;

        //    public StickItem(int _ItemID, int _dbID, int _userDBID, int _itemType, Boolean _selected, StickColour _colour)
        StickItem Spinner = new StickItem(SpinnerID, 0, -1, 1, true, SpinnerCol);
        StickItem DummyPet = new StickItem(200, 1, -1, 2, true, SpinnerCol);

        Inventory.put(0, Spinner);
        Inventory.put(1, DummyPet);

        return Inventory;
    }
}
