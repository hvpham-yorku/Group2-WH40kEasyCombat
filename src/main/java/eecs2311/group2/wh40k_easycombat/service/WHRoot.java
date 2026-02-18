package eecs2311.group2.wh40k_easycombat.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WHRoot {
    public int success_count;
    public int total_count;
    public String version;
    public List<WHPage> pages;
}