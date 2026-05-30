import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

public class XMLLoader {
    public static java.util.Map<String,int[]> loadTowerStats(String path){
        java.util.Map<String,int[]>stats = new HashMap<>();
        try{
            Document doc =  getDocument(path);
            NodeList tower =  doc.getElementsByTagName("tower");
            for(int i=0;i<tower.getLength();i++){
                Element e = (Element) tower.item(i);
                String name = e.getAttribute("name");
                int cost =  Integer.parseInt(e.getAttribute("cost"));
                int damage =  Integer.parseInt(e.getAttribute("damage"));
                int range = Integer.parseInt(e.getAttribute("range"));
                int firerate =  Integer.parseInt(e.getAttribute("firerate"));
                stats.put(name,new int[]{cost,damage,range,firerate});
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return stats;
    }
    public static int[][] loadMap(String path,int MapID){
        try{
            Document doc = getDocument(path);
            NodeList map =  doc.getElementsByTagName("map");
            for(int i=0;i<map.getLength();i++){
                Element e = (Element) map.item(i);
                if(Integer.parseInt(e.getAttribute("id")) != MapID){continue;}
                Element gridEl = (Element) e.getElementsByTagName("grid").item(0);
                int row = Integer.parseInt(gridEl.getAttribute("rows"));
                int col = Integer.parseInt(gridEl.getAttribute("cols"));
                int[][] grid = new int[row][col];
                NodeList rowList = e.getElementsByTagName("row");
                for(int j=0;j<rowList.getLength();j++){
                    String[] vals = rowList.item(j).getTextContent().trim().split(",");
                    for(int k=0;k<vals.length;k++){
                        grid[j][k] = Integer.parseInt(vals[k]);
                    }
                }
                return grid;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static List<WaveConfig> loadWaves(String path,int MapID){
        List<WaveConfig> waves = new ArrayList<>();
        try{
            Document doc = getDocument(path);
            NodeList map =   doc.getElementsByTagName("map");
            for(int i=0;i<map.getLength();i++){
                Element e = (Element) map.item(i);
                if(Integer.parseInt(e.getAttribute("id")) != MapID){continue;}
                NodeList waveList=   e.getElementsByTagName("wave");
                for(int j=0;j<waveList.getLength();j++){
                    Element waveEl = (Element) waveList.item(j);
                    int number = Integer.parseInt(waveEl.getAttribute("number"));
                    int delay = Integer.parseInt(waveEl.getAttribute("spawnDelay"));
                    boolean hasBoss = Boolean.parseBoolean(waveEl.getAttribute("hasBoss"));
                    String[] enemies = waveEl.getAttribute("enemies").split(",");
                    waves.add(new WaveConfig(number,enemies,delay,hasBoss));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return waves;
    }
    private static Document getDocument(String path) throws Exception{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputStream is = XMLLoader.class.getClassLoader().getResourceAsStream(path);
        return db.parse(is);
    }
}
