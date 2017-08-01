package OSM;

/**
 * Created by SmallApple on 2017/8/1.
 */
public class OSMEntity {
    private String id;
    private String tag;
    private String label = null;
    private String name_zh = null;
    private String name_en = null;
    /*
    private String version;
    private String uid;
    private String user;
    private String changeset;
    private String timestamp;
    //private String visible;
    */
    public String getTag() {
        return tag;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public void setName_zh(String Name_zh) {
        this.name_zh = Name_zh;
    }
    public String getName_zh() {
        return name_zh;
    }
    public void setName_en(String Name_en) {
        this.name_en = Name_en;
    }
    public String getName_en() {
        return name_en;
    }
    /*
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public String getChangeset() {
        return changeset;
    }
    public void setChangeset(String changeset) {
        this.changeset = changeset;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    /*public String getVisible() {
        return visible;
    }
    public void setVisible(String visible) {
        this.visible = visible;
    }*/
}
