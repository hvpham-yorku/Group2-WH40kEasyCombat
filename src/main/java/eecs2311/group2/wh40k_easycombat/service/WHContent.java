package eecs2311.group2.wh40k_easycombat.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WHContent {
    public int id;
    public String text;
    public String type;
}