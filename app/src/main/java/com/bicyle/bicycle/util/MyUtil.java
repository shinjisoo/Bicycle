package com.bicyle.bicycle.util;


import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MyUtil {


    //boardKind int -> String
    public static String getBoardKind(int kind) {
        switch (kind) {
            case 0:
                return "자유게시판";
            case 1:
                return "동호회홍보";
            case 2:
                return "자전거분실";
            case 3:
                return "사고팔기";
        }
        return "ERROR";

    }

    //날짜구하기
    public static String getDate() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        //SimpleDateFormat dateFm = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dateFm = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ");
        return dateFm.format(date);
    }

    public static String getChannelId(List<String> userIds) {
        String channelID = "";

        Collections.sort(userIds);

        for (String id : userIds) {
            channelID += id;
        }
        return channelID;
    }

    //거리구하기
    public static double calDistance(double lat1, double lon1, double lat2, double lon2){
        double theta, dist;
        theta = lon1 - lon2;
        dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);

        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;    // 단위 mile 에서 km 변환.
        dist = dist * 1000.0;      // 단위  km 에서 m 로 변환

        return dist;
    }

    // 주어진 도(degree) 값을 라디언으로 변환
    private static double deg2rad(double deg){
        return (double)(deg * Math.PI / (double)180d);
    }

    // 주어진 라디언(radian) 값을 도(degree) 값으로 변환
    private static double rad2deg(double rad){
        return (double)(rad * (double)180d / Math.PI);
    }

}
