package eecs2311.group2.wh40k_easycombat.classes.entity;

public class Ability {

  private int id;
  private String name;
  private String description;

  public Ability(){}

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
