package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Datasheets_models")
public record Datasheets_models(
		@PK @AutoIncrement int auto_id,
        @NotNull String datasheet_id,
        @NotNull String line,
        String name,
        String M,
        String T,
        String Sv,
        String inv_sv,
        String inv_sv_descr,
        String W,
        String Ld,
        String OC,
        String base_size,
        String base_size_descr
) {}