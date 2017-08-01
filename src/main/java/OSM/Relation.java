package OSM;

import java.util.Vector;

/**
 * Created by SmallApple on 2017/8/1.
 */
public class Relation extends OSMEntity {

    private Vector<String> nodeIDs = null;
    private Vector<String> wayIDs = null;
    private Vector<String> relationIDs = null;

    public Vector<String> getnodeIDs() {
        return nodeIDs;
    }
    public void setnodeIDs(Vector<String> nodeIDs) {
        this.nodeIDs = nodeIDs;
    }
    public Vector<String> getwayIDs() {
        return wayIDs;
    }
    public void setwayIDs(Vector<String> wayIDs) {
        this.wayIDs = wayIDs;
    }
    public Vector<String> getrelationIDs() {
        return relationIDs;
    }
    public void setrelationIDs(Vector<String> relationIDs) {
        this.relationIDs = relationIDs;
    }
}
