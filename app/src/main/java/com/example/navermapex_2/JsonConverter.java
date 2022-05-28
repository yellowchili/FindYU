package com.example.navermapex_2;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.InputStream;

public class JsonConverter {

    //json -> str, str-> int 배열로 변환하여 좌표배열 리턴 클래스
    private String path; //json 파일 경로
    private final Context mContext;
    private AssetManager am;
    private String strConvert;

    public JsonConverter(Context context, String path) {
        this.mContext = context;
        this.am = context.getResources().getAssets();
        this.path = path;
        jsonToString();
    }
    //json을 string으로 변환
    public void jsonToString() {
        InputStream is = null;
        String str = "";
        try {
            is = am.open(path);
            int fileSize = is.available();
            byte buf[] = new byte[fileSize];
            if (is.read(buf) > 0) {
                str = new String(buf);
            }
            is.close();
            strConvert = str;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (is != null) {
            try {
                is.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        strConvert = str;
    }

    // 2차원 좌표 배열(경로) 리턴
    public int[][] strTo2DArray() {
        String data = strConvert;
        int datalen = data.length();
        data = data.substring(1, datalen - 2);
        int idx = 0;
        int i = 0;

        String result[] = data.split("], ");

        for (String r : result) {
            result[i] = r.substring(1, r.length());
            i++;
        }
        int[][] intresult = new int[result.length][2];
        String[][] tmp = new String[1][];
        for (String r : result) {
            System.out.println();
            tmp[0] = r.split(", ");
            intresult[idx][0] = Integer.parseInt(tmp[0][0]);
            intresult[idx][1] = Integer.parseInt(tmp[0][1]);
//         System.out.println(result2[idx][0] +" "+ result2[idx][1]);
//         System.out.println();
            idx++;
        }
        return intresult;
    }

    //1차원 좌표 배열 리턴
    public int[] strToArray() {
        String data = strConvert;
        int datalen = data.length();
        data = data.substring(1,datalen-1); //배열에서 [ ] 제거
        int i=0;
        String tmp[] = data.split(", ");
        int result[] = new int[2];
        for(String t : tmp) {
            result[i] = Integer.parseInt(t);
            //System.out.println(result[i]);
            i++;
        }
        return result;
    }

}
