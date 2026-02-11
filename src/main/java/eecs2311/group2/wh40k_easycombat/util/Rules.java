import java.util.List;

public class Rules {
    private String name;
    private String type;
    private String description;
    private String faction;
    private List<String> keyword;
    private int cost;
    private String phase;

    public Rules(String name, String type, String description, String faction, List<String> keyword, int cost, String phase){
        this.name = name;
        this.type = type;
        this.description = description;
        this.faction = faction;
        this.keyword = keyword;
        this.cost = cost;
        this.phase = phase;
    }

    public void setName (String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setType(String type){
        this.type = type;
    }

    public String getType(){
        return type;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }

    public void setFaction(String faction){
        this.faction = faction;
    }

    public String getFaction(){
        return faction;
    }

    public void setKeyword(List<String> keyword){
        this.keyword = keyword;
    }

    public List<String> getKeyword(){
        return keyword;
    }

    public void setCost(int cost){
        this.cost = cost;
    }

    public int getCost(){
        return cost;
    }

    public void setPhase(String phase){
        this.phase = phase;
    }

    public String getPhase(){
        return phase;
    }
}