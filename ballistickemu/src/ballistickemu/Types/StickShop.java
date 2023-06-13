package ballistickemu.Types;

import ballistickemu.Tools.DatabaseTools;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;

public class StickShop {
	private LinkedHashMap<Integer, Integer> ShopList;

	public StickShop() {
		this.ShopList = new LinkedHashMap<>();
	}

	public int getPriceByItemID(StickClient client, int itemID) {
		if (this.ShopList.containsKey(itemID)) {
			return (this.ShopList.get(itemID)).intValue();
		}
		if (itemID == 240 && client.getPass()) {
			try {
				PreparedStatement ps = DatabaseTools.getDbConnection().prepareStatement(
						"SELECT COUNT(id) AS mapSlots FROM inventory WHERE userid = ? AND itemid = ?");
				ps.setInt(1, client.getDbID());
				ps.setInt(2, 240);
				ResultSet set = ps.executeQuery();
				if (set.next()) {
					int mapSlots = set.getInt("mapSlots");
					switch (mapSlots) {
					case 0:
						return 400;
					case 1:
						return 800;
					case 2:
						return 1500;
					case 3:
						return 3000;
					}
				}
			} catch (SQLException e) {
				System.out.println("There was a problem buying a map slot.");
			}
		}

		return -1;
	}

	public boolean PopulateShop() {
		try {
			ResultSet rs = DatabaseTools.executeSelectQuery("select * from shop");
			while (rs.next()) {
				int IID = rs.getInt("itemID");
				int cost = rs.getInt("cost");
				this.ShopList.put(IID, cost);
			}
			if (this.ShopList.size() > 1) {
				return true;
			}
			return false;
		} catch (SQLException e) {
		}
		return false;
	}
}
