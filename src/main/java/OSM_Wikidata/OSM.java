package OSM_Wikidata;

import FileHandle.HandleFiles;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.VCARD;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import OSM.*;

import static OSM_Wikidata.OSM_Wikidata.getURI;

/**
 * Created by SmallApple on 2017/4/17.
 */
public class OSM extends OSM_Wikidata {
    String OSM_Type = "GeoEntity";
    String OSMType = null;
    public void setOSMType(String OSMType) {
        this.OSMType = OSMType;
    }
    String getOSMType() {
        return this.OSMType;
    }
}
