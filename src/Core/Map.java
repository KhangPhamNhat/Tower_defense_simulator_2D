import java.util.*;
public class Map {
    private int[][]grid;
    private int row;
    private int col;
    private List<int[]> path;
    public Map(int[][] grid) {
        this.grid = grid;
        this.row = grid.length;
        this.col = grid[0].length;
        this.path = buildPath();
    }
    private List<int[]> buildPath() {
        //tim o spawn lam diem xuat phat
        int[] start = null;
        for(int r= 0; r<row; r++) {
            for (int c = 0; c < col; c++) {
                if (grid[r][c] == GameConstants.SPAWN) {
                    start = new int[]{r, c};
                }
            }
        }
        //bfs tim duong theo path den base
        List<int[]> result = new ArrayList<>();
            boolean[][]visited = new boolean[row][col];
            Queue<int[]> queue = new LinkedList<>();
            java.util.Map<String,int[]>parent = new HashMap<>();
            queue.add(start);
            visited[start[0]][start[1]] = true;
            int[] end = null;
            int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
            while(!queue.isEmpty()){
                int[] cur = queue.poll(); // lấy và loại bỏ phần tử đầu
                if(grid[cur[0]][cur[1]]==GameConstants.BASE){end=cur;break;}
                for(int[] dir:dirs){
                    int nextR = cur[0]+dir[0];
                    int nextC = cur[1]+dir[1];
                    if(nextR >= 0 && nextR < row && nextC >= 0 && nextC < col && !visited[nextR][nextC]
                            &&(grid[nextR][nextC]==GameConstants.PATH
                            || grid[nextR][nextC]==GameConstants.BASE)){
                        visited[nextR][nextC]=true;
                        parent.put(nextR + "," + nextC,cur);
                        queue.add(new int[]{nextR,nextC});
                    }
                }
            }
            // trace nguoc tu base ve spawn
            int[] cur = end;
            while(cur != null){
                result.add(0,cur);
                cur = parent.get(cur[0]+","+cur[1]);
            }
            result.add(0,start);
            return result;
            }
    public boolean canPlaceTower(int r, int c){
        return grid[r][c]==GameConstants.GRASS;
    }
    public int[][] getGrid(){return grid;}
    public int getRow(){return row;}
    public int getCol(){return col;}
    public List<int[]> getPath(){return path;}
    }
