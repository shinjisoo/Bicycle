package com.bicyle.bicycle.Map;

import com.skt.Tmap.TMapPoint;

import java.io.Serializable;
import java.util.ArrayList;

public class Route_boardDTO implements Serializable {

    private String startPoint;  //출발지명
    private String endPoint;  //도착지명
    private String body; //내용
    private String uid ; //작성자 uid
    private double distance ; // 거리

    ArrayList<MyPoint> route_tmap = new ArrayList<MyPoint>();



    public Route_boardDTO()
    {

    }


    public Route_boardDTO(String startPoint, String endPoint, String body, String uid, double distance, ArrayList<MyPoint> route_tmap) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.body = body;
        this.uid = uid;
        this.distance = distance;
        this.route_tmap = route_tmap;
    }

    public String getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(String startPoint) {
        this.startPoint = startPoint;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public ArrayList<MyPoint> getRoute_tmap() {
        return route_tmap;
    }

    public void setRoute_tmap(ArrayList<MyPoint> route_tmap) {
        this.route_tmap = route_tmap;
    }
}