//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Armies;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class ArmyRepository {
		public static int addNewArmy(Armies army) throws SQLException {
				return Dao.update(
						"INSERT INTO armies (name, isFavorite, totalPoints, warlordId, factionId, detachmentId, unitIdList, equippedRangedWeaponIdList, equippedMeleeWeaponIdList) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
						army.name(),
						army.isFavorite(),
						army.totalPoints(),
						army.warlordId(),
						army.factionId(),
						army.detachmentId(),
						IntListCodec.encode(army.unitIdList()),
						IntListCodec.encode(army.equippedRangedWeaponIdList()),
						IntListCodec.encode(army.equippedMeleeWeaponIdList())
				);
		}
		public static Armies getArmyById(int id) throws SQLException {
				return Dao.query(
						"SELECT * FROM armies WHERE id = ?",
						rs -> new Armies(
								rs.getInt("id"),
								rs.getString("name"),
								rs.getBoolean("isFavorite"),
								rs.getInt("totalPoints"),
								rs.getInt("warlordId"),
								rs.getInt("factionId"),
								rs.getInt("detachmentId"),
								IntListCodec.decode(rs.getString("unitIdList")),
								IntListCodec.decode(rs.getString("equippedRangedWeaponIdList")),
								IntListCodec.decode(rs.getString("equippedMeleeWeaponIdList"))						),
						id
				).stream().findFirst().orElse(null);
		}
		public static List<Armies> getAllArmies() throws SQLException {
				return Dao.query(
						"SELECT * FROM armies",
						rs -> new Armies(
								rs.getInt("id"),
								rs.getString("name"),
								rs.getBoolean("isFavorite"),
								rs.getInt("totalPoints"),
								rs.getInt("warlordId"),
								rs.getInt("factionId"),
								rs.getInt("detachmentId"),
								IntListCodec.decode(rs.getString("unitIdList")),
								IntListCodec.decode(rs.getString("equippedRangedWeaponIdList")),
								IntListCodec.decode(rs.getString("equippedMeleeWeaponIdList"))						)
				);
		}
		public static void updateArmy(Armies army) throws SQLException {
				Dao.update(
						"UPDATE armies SET name = ?, isFavorite = ?, totalPoints = ?, warlordId = ?, factionId = ?, detachmentId = ?, unitIdList = ?, equippedRangedWeaponIdList = ?, equippedMeleeWeaponIdList = ? WHERE id = ?",
						army.name(),
						army.isFavorite(),
						army.totalPoints(),
						army.warlordId(),
						army.factionId(),
						army.detachmentId(),
						IntListCodec.encode(army.unitIdList()),
						IntListCodec.encode(army.equippedRangedWeaponIdList()),
						IntListCodec.encode(army.equippedMeleeWeaponIdList()),
						army.id()
				);
		}
		public static void deleteArmy(Armies army) throws SQLException {
				Dao.update(
						"DELETE FROM armies WHERE id = ?",
						army.id()
				);
		}
}
