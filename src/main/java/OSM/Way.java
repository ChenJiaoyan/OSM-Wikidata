package OSM;

import java.util.List;
import java.util.Vector;

/**
 * Created by SmallApple on 2017/8/1.
 */
public class Way extends OSMEntity {

    private List<Nodes> points;
    private Vector<String> pointids;

    public Vector<String> getPointids() {
        return pointids;
    }
    public void setPointids(Vector<String> pointids) {
        this.pointids = pointids;
    }
    public List<Nodes> getPoint() {
        return points;
    }
    public void setPoint(List<Nodes> points) {
        this.points = points;
    }
}
