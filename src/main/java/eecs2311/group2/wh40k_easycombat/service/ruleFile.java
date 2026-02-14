package eecs2311.group2.wh40k_easycombat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;

public class ruleFile {
    public static WHRoot loadJson(String filePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(filePath), WHRoot.class);
    }
}