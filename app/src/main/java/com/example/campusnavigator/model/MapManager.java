package com.example.campusnavigator.model;

import androidx.annotation.NonNull;

import com.amap.api.maps.model.LatLng;
import com.example.campusnavigator.utility.interfaces.RouteResultReceiver;
import com.example.campusnavigator.utility.structures.List;
import com.example.campusnavigator.utility.structures.MinHeap;
import com.example.campusnavigator.utility.structures.Stack;
import com.example.campusnavigator.utility.structures.Tuple;

import java.util.Arrays;


/**
 * @Description 路线规划算法的实现类
 * @Author John
 * @email
 * @Date 2022/8/31 13:30
 * @Version 1
 */
public class MapManager extends Map {

    private Stack<Position> positionBuffer;
    private double[] dist;
    private static MapManager obj;

    private static class Status implements Comparable<Status> {
        int v;
        double d;
        double p;
        Status pre;

        public Status(int v, double d, double p, Status pre) {
            this.v = v;
            this.d = d;
            this.p = p;
            this.pre = pre;
        }

        @Override
        public int compareTo(Status o) {
            return Double.compare(p, o.p);
        }
    }

    private static class Entry implements Comparable<Entry>{
        final int v;
        final double dist;

        Entry(int v, double dist) {
            this.v = v;
            this.dist = dist;
        }

        @Override
        public int compareTo(Entry o) {
            return Double.compare(dist, o.dist);
        }
    }


    private MapManager() {
        positionBuffer = new Stack<>();
    }

    public static MapManager getInstance() {
        if (obj == null) {
            obj = new MapManager();
        }
        return obj;
    }

    private boolean checkResult(Route result) {
        return result != null &&
                result.getRoute() != null && result.getRoute().length() != 0 &&
                result.getTime() != null && !result.getTime().equals(INF) &&
                result.getDist() != null && !result.getDist().equals(INF);
    }

    public void calculate(boolean isMultiSpot, RouteResultReceiver receiver) throws Exception {
        if (isMultiSpot) {
            List<Route> results = multiDestRoute();
            for (Route result : results) {
                if (!checkResult(result)) {
                    throw new Exception("多点结果错误");
                }
            }
            receiver.onMultiRouteReceive(results);
            return;
        }

        List<Route> results = new List<>();
        Position to = positionBuffer.top();
        positionBuffer.pop();
        Position from = positionBuffer.top();
        positionBuffer.pop();

        for (int i = 0, k = 1; i < 3; i++, k++) {
            Route result = singleDestRoute(from, to, k);
            if (!checkResult(result)) {
                throw new Exception("单点结果错误");
            }
            results.push(result);
        }

        receiver.onSingleRouteReceive(results);
    }

    public List<Route> multiDestRoute() {
        List<Route> results = new List<>();
        Position to = positionBuffer.top();
        positionBuffer.pop();

        while (!positionBuffer.isEmpty()) {
            Position from = positionBuffer.top();
            positionBuffer.pop();
            Route singleDestRoute = singleDestRoute(from, to, 1);
            results.push(singleDestRoute);
            to = from;
        }

        return results;
    }

    public Route singleDestRoute(@NonNull Position from, @NonNull Position to, int k) {

        int fromId = from.getId();
        int toId = to.getId();

        Tuple<List<Position>, Double> result = Astar(fromId, toId, k);

        List<Position> route = result.first;
        double dist = result.second;
        double time = dist / SPEED_WALK;

        return new Route(route, time, dist);
    }

    public Tuple<List<Position>, Double> Astar(int source, int dest, int k) {

        // 通过Dijkstra计算dest到source的最短路径树
        // 其中dist[i]表示从dest到i的距离，将该距离作为A*的估计代价
        Dijkstra(dest, source);

        List<Position> route = new List<>();
        MinHeap<Status> heap = new MinHeap<>();
        int[] visited = new int[size];

        int vi = source;
        Status v = new Status(vi, 0, dist[vi], null);
        heap.push(v);

        while (!heap.isEmpty()) {
            v = heap.top();
            vi = v.v;
            heap.pop();

            // visited[v]记录到达v的次数
            visited[vi]++;
            // 每次走代价值最小的顶点，当第k次到达dest时，走过的路径就是k短路
            if (vi == dest && visited[vi] == k) {
                break;
            }
            // 限制到达非dest顶点的次数，防止算法困在环中
            if (visited[vi] > k) {
                continue;
            }

            for (int i = 0; i < size; i++) {
                // 遍历v的邻接点
                if (vi != i &&map[vi][i].dist != INF) {
                    // 无向图中需要假定 v.pre 到 v 为单向的，即 v.pre -> v
                    // 则下一个选择的 i != v.pre，防止算法困在 v 和 v.pre 之间
                    if (v.pre == null || v.pre.v != i) {
                        // d为实际距离，p为移动代价
                        double d = v.d + map[vi][i].dist;
                        double p = d + dist[i];
                        Status next = new Status(i, d, p, v);
                        heap.push(next);
                    }
                }
            }
        }

        // pre回溯状态
        double dist = v.p;
        Position pos = positions[v.v];
        route.push(pos);
        Status cur = v.pre;
        while (cur != null) {
            pos = positions[cur.v];
            route.push(pos);
            cur = cur.pre;
        }

        return new Tuple<>(route, dist);
    }

    private void Dijkstra(int s, int t) {
        boolean[] vis = new boolean[size];
        dist = new double[size];
        Arrays.fill(dist, INF);
        dist[s] = 0;

        int v = s;
        MinHeap<Entry> heap = new MinHeap<>();
        heap.push(new Entry(v, 0));

        while (!heap.isEmpty()) {
            v = heap.top().v;
            heap.pop();
            vis[v] = true;
            if (v == t) {
                break;
            }
            for (int i = 0; i < size; i++) {
                if (map[v][i].dist != INF && !vis[i]) {
                    if (dist[v] + map[v][i].dist < dist[i]) {
                        dist[i] = dist[v] + map[v][i].dist;
                        Entry e = new Entry(i, dist[i]);
                        heap.push(e);
                    }
                }
            }
        }
    }

    public List<Position> attachToMap(Position myPosition, Position destPosition) {
        List<Position> attachPositions = new List<>();

        // 使用最小堆取距离最小的两个点
        MinHeap<Entry> minDist = new MinHeap<>();
        for (int i = 0; i < size; i++) {
            if (checkDirection(myPosition, positions[i], destPosition)) {
                double dist = getDistance(myPosition, positions[i]);
                minDist.push(new Entry(i, dist));
            }
        }
        int min1 = minDist.top().v;
        minDist.pop();
        int min2 = minDist.top().v;
        minDist.pop();
        int min3 = minDist.top().v;
        attachPositions.push(positions[min1]);
        attachPositions.push(positions[min2]);
        attachPositions.push(positions[min3]);
        return attachPositions;
    }

    private boolean checkDirection(Position myPosition, Position position, Position destPosition) {
        LatLng me = myPosition.getLatLng();
        LatLng pos = position.getLatLng();
        LatLng dest = destPosition.getLatLng();

        double vX1 = dest.longitude - me.longitude;
        double vY1 = dest.latitude - me.latitude;

        double vX2 = pos.longitude - me.longitude;
        double vY2 = pos.latitude - me.latitude;

        // 计算两个向量的夹角，当 cos(theta) >= 0 时为true
        double dot = vX1 * vX2 + vY1 * vY2;
        double modFirst = Math.sqrt(vX1 * vX1 + vY1 * vY2);
        double modSecond = Math.sqrt(vX2 * vX2 + vY2 * vY2);
        double angle = dot / (modFirst * modSecond);

        return angle >= 0;
    }

    public void pushBuffer(Position position) {
        if (positionBuffer != null) {
            positionBuffer.push(position);
        }
    }

    public void popBuffer() {
        if (positionBuffer != null) {
            positionBuffer.pop();
        }
    }

    public void popBufferAll() {
        if (positionBuffer != null) {
            positionBuffer.popAll();
        }
    }

    public boolean isBufferEmpty() {
        if (positionBuffer != null) {
            return positionBuffer.isEmpty();
        }
        return true;
    }

    public Position bufferTop() {
        if (positionBuffer != null) {
            return positionBuffer.top();
        }
        return null;
    }

    public int bufferSize() {
        if (positionBuffer != null) {
            return positionBuffer.size();
        }
        return 0;
    }
}
