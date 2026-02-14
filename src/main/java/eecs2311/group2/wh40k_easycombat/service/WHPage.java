package eecs2311.group2.wh40k_easycombat.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WHPage {
    public int page_id;
    public List<WHContent> content;
}
