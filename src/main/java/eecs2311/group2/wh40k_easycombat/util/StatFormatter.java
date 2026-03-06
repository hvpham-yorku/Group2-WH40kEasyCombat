package eecs2311.group2.wh40k_easycombat.util;

import eecs2311.group2.wh40k_easycombat.model.Datasheets_models;

import java.util.List;

public class StatFormatter {

    public static String buildStatLine(List<Datasheets_models> models) {

        if (models == null || models.isEmpty()) {
            return "M:- T:- SV:- W:- Ld:- OC:- Inv:-";
        }

        Datasheets_models m = models.get(0);

        return "M:" + nz(m.M()) +
                " T:" + nz(m.T()) +
                " SV:" + nz(m.Sv()) +
                " W:" + nz(m.W()) +
                " Ld:" + nz(m.Ld()) +
                " OC:" + nz(m.OC()) +
                " Inv:" + nz(m.inv_sv());
    }

    private static String nz(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}