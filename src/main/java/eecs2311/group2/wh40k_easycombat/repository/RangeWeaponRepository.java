//-- Auto Generated Java File --
package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.RangeWeapons;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class RangeWeaponRepository {
		public static int addNewRangeWeapon(RangeWeapons rangeweapon) throws SQLException {
				return Dao.insert(
						"INSERT INTO ranged_weapons (name, range, A, BS, S, AP, D, keywordIdList VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
						rangeweapon.name(),
						rangeweapon.range(),
						rangeweapon.A(),
						rangeweapon.BS(),
						rangeweapon.S(),
						rangeweapon.AP(),
						rangeweapon.D(),
						IntListCodec.encode(rangeweapon.keywordIdList())
				).get(0);
		}
		public static RangeWeapons getRangeWeaponById(int id) throws SQLException {
				return Dao.query(
						"SELECT * FROM ranged_weapons WHERE id = ?",
						rs -> new RangeWeapons(
								rs.getInt("id"),
								rs.getString("name"),
								rs.getInt("range"),
								rs.getString("A"),
								rs.getInt("BS"),
								rs.getInt("S"),
								rs.getInt("AP"),
								rs.getString("D"),
								IntListCodec.decode(rs.getString("keywordIdList"))
						),
						id
				).stream().findFirst().orElse(null);
		}
		public static List<RangeWeapons> getAllRangeWeapons() throws SQLException {
				return Dao.query(
						"SELECT * FROM ranged_weapons",
						rs -> new RangeWeapons(
								rs.getInt("id"),
								rs.getString("name"),
								rs.getInt("range"),
								rs.getString("A"),
								rs.getInt("BS"),
								rs.getInt("S"),
								rs.getInt("AP"),
								rs.getString("D"),
								IntListCodec.decode(rs.getString("keywordIdList"))
						)						
				);
		}
		public static void updateRangeWeapon(RangeWeapons rangeweapon) throws SQLException {
				Dao.update(
						"UPDATE ranged_weapons SET name = ?, range = ?, A = ?, BS = ?, S = ?, AP = ?, D = ?, keywordIdList = ? WHERE id = ?",
						rangeweapon.name(),
						rangeweapon.range(),
						rangeweapon.A(),
						rangeweapon.BS(),
						rangeweapon.S(),
						rangeweapon.AP(),
						rangeweapon.D(),
						IntListCodec.encode(rangeweapon.keywordIdList()),
						rangeweapon.id()
				);
		}
		public static void deleteRangeWeapon(RangeWeapons rangeweapon) throws SQLException {
				Dao.update(
						"DELETE FROM ranged_weapons WHERE id = ?",
						rangeweapon.id()
				);
		}
}