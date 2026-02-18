package eecs2311.group2.wh40k_easycombat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;

public class ruleFile {
    public static WHRoot loadJson(String resourceName) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        InputStream is = ruleFile.class.getClassLoader().getResourceAsStream(resourceName);
        if (is == null) {
            throw new IllegalArgumentException("Resource not found: " + resourceName);
        }
        return mapper.readValue(is, WHRoot.class);
    }
}