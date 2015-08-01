package kr.co.stylenetwork.simpleconnect;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.os.Handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {
    String path="http://192.168.0.137:8080/board/android/list";
    //String path="http://naver.com";
    URL url;
    HttpURLConnection con;
    InputStream is;
    InputStreamReader reader;
    BufferedReader buffr;
    /* 메인인 형님쓰레드 대신, 네트워크 여행을 할 녀석!!
    * 안드로이드에서는 왜 메인쓰레드를 이용하여 네트워크 통신을 할수 없도록
    * 금지 시켜놓앗나??
    * whi??  메인쓰레드는 사용자와의 상호작용을 위한 UI 제어나 그래픽처리
    * 를 담당하거나 이벤트 감지등 시스템 운영에 사용해야 하기때문에,
    * 절대 무한루프나 지연이 발생하는 코드에 사용해서는 안된다!!
    * */
    Thread thread;
    Handler handler;
    TextView txt_data;
    ArrayList<Movie> list = new ArrayList<Movie>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txt_data=(TextView)findViewById(R.id.txt_data);

        handler = new Handler(){
            public void handleMessage(Message msg) {
                /*
                Bundle bundle=msg.getData();
                String data=bundle.getString("data");
                */
                for(int i=0;i<list.size();i++) {
                    Movie dto=list.get(i);
                    txt_data.setText(dto.getTitle()+"\n");
                }
            }
        };

        thread = new Thread(){
            /*개발자는 메인쓰레드와는 독립적으로 수행될 로직을run에 기재함
            *   네트워크 접속!!
            * */
            public void run(){
                connect();
            }
        };

    }

    /*버튼을 누르면, 웹서버에 요청을 시도하여 json 데이터를 가져온다
    * 그 후, TextView 데이터를 출력해보자!!
    * */
    public void connect(){
        try {
            url = new URL(path);
            con=(HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setReadTimeout(5000);
            con.setConnectTimeout(15000);

            con.connect();/*웹서버에 요청시도!!*/

            if( con.getResponseCode()== HttpURLConnection.HTTP_OK) {
                is = con.getInputStream();
                /*문자기반의 스트림으로 업그레이드 했으므로, 한글 및 비영어권
                문자가 흐름시 깨져보이지 않는다*/
                reader = new InputStreamReader(is);
                /*
                * 문자기반으로 업그레이드는 했으나, 문자열단위로 데이터를 처리
                * 하는 것이 더 효율적이므로, 한줄씩 읽어들이는 스트림으로 업그레
                * 이드 하자!!
                * */
                buffr = new BufferedReader(reader);

                /*한줄씩 읽어들이기*/
                String data=null;
                StringBuffer sb = new StringBuffer();

                while(true){
                    data = buffr.readLine();
                    if(data==null)break;
                    sb.append(data);/*읽어들인 데이터 누적*/
                }
                //Log.d( this.getClass().getName()  , sb.toString());
                /*
                * 안드로이드에서는 개발자가 정의한 쓰레드(메인쓰레드 아닌 녀석)
                * 는 절대 UI제어할 수 없다 , 즉 메인쓰레드와의 충돌을 방지
                * */


                /*서버로부터 받아온 데이터가 자바스크립트 객체 표기법으로
                * 되어 있을때, 이 데이터를 해석해보자 이런형태의 데이터를
                * 가리켜 제이슨이라한다 안드로이드는 이미 제이스파서가
                * 포함되어 있다*/

                try {
                    JSONObject jsonObject = new JSONObject(sb.toString());
                    JSONArray array=jsonObject.getJSONArray("movieList");
                    for(int i=0;i<array.length();i++){
                        JSONObject obj=(JSONObject)array.get(i);

                        Movie dto = new Movie();

                        dto.setBoard_id(obj.getInt("board_id"));
                        dto.setTitle(obj.getString("title"));
                        dto.setActor(obj.getString("actor"));
                        dto.setOpenday(obj.getString("openday"));
                        dto.setIconName(obj.getString("iconName"));
                        dto.setFileName(obj.getString("fileName"));

                        list.add(dto);
                    }
                   /*핸들를 이용하여 부탁한다!!*/
                    /*
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("data", list);
                    message.setData(bundle);
                    handler.sendMessage(message);
                    */
                    handler.sendEmptyMessage(0);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            /*사용했던 스트림 자원 반납*/
            if(buffr!=null) {
                try {
                    buffr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void btnClick(View view){
        thread.start(); /*Runnable 상태로 진입*/
    }

}







