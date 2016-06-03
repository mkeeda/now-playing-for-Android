package com.example.mk_mkee.testapp5;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;

public class MainActivity extends AppCompatActivity {
    private TextView musicInfo;
    private Twitter mTwitter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //認証してなければ認証画面へ
        if(!TwitterUtils.hasAccessToken(this)){
            Intent intent = new Intent(this, TwitterOAuthActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            afterSuccessOAuth();
        }
    }

    private void afterSuccessOAuth(){
        Button pause = (Button)this.findViewById(R.id.pause);
        pause.setOnClickListener(new PauseToMusic());

        Button play = (Button)this.findViewById(R.id.play);
        play.setOnClickListener(new PlayToMusic());

        IntentFilter intentFilter = new IntentFilter();
        //再生曲が変わった瞬間
        intentFilter.addAction("com.android.music.metachanged");
        //再生状態が変わった瞬間
        intentFilter.addAction("com.android.music.playstatechanged");
        //曲が最後まで再生された瞬間
        intentFilter.addAction("com.android.music.playbackcomplete");

        musicInfo = (TextView)this.findViewById(R.id.musicInfo);
        registerReceiver(musicReceiver, intentFilter);

        mTwitter = TwitterUtils.getTwitterInstance(this);
        //リスナーのセット
        Button button = (Button)this.findViewById(R.id.tweet);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tweet();
            }
        });
    }

    private BroadcastReceiver musicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");
            Log.v("tag", action + "/" + cmd);

            //楽曲情報
            String artist = intent.getStringExtra("artist");
            String album = intent.getStringExtra("album");
            String track = intent.getStringExtra("track");
            Log.v("tag", artist + ":" + album + ":" + track);

            musicInfo.setText(
                    "artist: " + artist + "\n" +
                    "album: " + album + "\n" +
                    "track: " + track);


        }
    };

    //音楽を一時停止
    class PauseToMusic implements View.OnClickListener{

        @Override
        public void onClick(View v){
            Intent intent = new Intent("com.android.music.musicservicecommand");
            intent.putExtra("command", "pause");
            sendBroadcast(intent);
        }
    }

    //音楽を再生
    class PlayToMusic implements View.OnClickListener{

        @Override
        public void onClick(View v){
            Intent intent = new Intent("com.android.music.musicservicecommand");
            intent.putExtra("command", "play");
            sendBroadcast(intent);
        }
    }

    private void tweet(){
        AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {
                try{
                    mTwitter.updateStatus(params[0]);
                    return true;
                }
                catch (TwitterException e){
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result){
                if(result){
                    showToast("ツイート成功");
                }
                else{
                    showToast("ツイート失敗");
                }
            }
        };
        task.execute(musicInfo.getText().toString() + "\n #NowPlaying");
    }

    private void showToast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    }
