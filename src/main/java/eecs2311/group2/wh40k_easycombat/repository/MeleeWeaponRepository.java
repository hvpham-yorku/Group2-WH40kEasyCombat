//-- Auto Generated Java File --
package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.MeleeWeapons;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class MeleeWeaponRepository {
		public static int addNewMeleeWeapon(MeleeWeapons meleeweapon) throws SQLException {
				return Dao.update(
						"INSERT INTO melee_weapons (name, A, WS, S, AP, D, keywordIdList VALUES (?, ?, ?, ?, ?, ?, ?)",
						meleeweapon.name(),
						meleeweapon.A(),
						meleeweapon.WS(),
						meleeweapon.S(),
						meleeweapon.AP(),
						meleeweapon.D(),
						IntListCodec.encode(meleeweapon.keywordIdList())
				);
		}
		public static MeleeWeapons getMeleeWeaponById(int id) throws SQLException {
				return Dao.query(
						"SELECT * FROM melee_weapons WHERE id = ?",
						rs -> new MeleeWeapons(
								rs.getInt("id"),
								rs.getString("name"),
								rs.getString("A"),
								rs.getInt("WS"),
								rs.getInt("S"),
								rs.getInt("AP"),
								rs.getString("D"),
								IntListCodec.decode(rs.getString("keywordIdList"))
						),
						id
				).stream().findFirst().orElse(null);
		}
		public static List<MeleeWeapons> getAllMeleeWeapons() throws SQLException {
				return Dao.query(
						"SELECT * FROM melee_weapons",
						rs -> new MeleeWeapons(
								rs.getInt("id"),
								rs.getString("name"),
								rs.getString("A"),
								rs.getInt("WS"),
								rs.getInt("S"),
								rs.getInt("AP"),
								rs.getString("D"),
								IntListCodec.decode(rs.getString("keywordIdList"))
						)						
				);
		}
		public static void updateMeleeWeapon(MeleeWeapons meleeweapon) throws SQLException {
				Dao.update(
						"UPDATE melee_weapons SET name = ?, A = ?, WS = ?, S = ?, AP = ?, D = ?, keywordIdList = ? WHERE id = ?",
						meleeweapon.name(),
						meleeweapon.A(),
						meleeweapon.WS(),
						meleeweapon.S(),
						meleeweapon.AP(),
						meleeweapon.D(),
						IntListCodec.encode(meleeweapon.keywordIdList()),
						meleeweapon.id()
				);
		}
		public static void deleteMeleeWeapon(MeleeWeapons meleeweapon) throws SQLException {
				Dao.update(
						"DELETE FROM melee_weapons WHERE id = ?",
						meleeweapon.id()
				);
		}
}