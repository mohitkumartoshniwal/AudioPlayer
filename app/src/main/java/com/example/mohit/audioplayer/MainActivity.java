package com.example.mohit.audioplayer;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private Button play, pause, forward, backward, highlight, bookmark, activity2;
    private Button choose;

    /**
     * Handles playback of all the sound files
     */
    private MediaPlayer mediaPlayer;
    /**
     * Handles audio focus when playing a sound file
     */
    private AudioManager mAudioManager;
    private long seektopos = 0;
    private boolean sethighlight = true;
    private static final int PICK_AUDIO = 42;


    private long startTime;
    private long finalTime;
    private long currentTime;
    private boolean First = true;
    private boolean Last = true;
    private long extraTime;

    private long noteStarted;
    private Uri uri;



    private Handler myHandler;
    private Runnable runnable;
    long endAT;//to run handler for notes and also to change time if forward or backward is clicked

    private CountDownTimer countDownTimer = null;
    private boolean intentReceivedOrScreenRotated = false;

    private int forwardTime = 10000;
    private int backwardTime = 10000;
    private int id = 0;
    private int mediaplayerResourceId;
    private String str;
//    SharedPreferences sharedPref;

    //  private SeekBar seekbar;



    /* This listener gets triggered whenever the audio focus changes
     * (i.e., we gain or lose audio focus because of another app or device).
     */
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                    focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                // The AUDIOFOCUS_LOSS_TRANSIENT case means that we've lost audio focus for a
                // short amount of time. The AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK case means that
                // our app is allowed to continue playing sound but at a lower volume. We'll treat
                // both cases the same way because our app is playing short sound files.

                // Pause playback and reset player to the start of the file. That way, we can
                // play the word from the beginning when we resume playback.
                Log.i("app", "onAudioFocusChange:t and duck ");
                mediaPlayer.pause();
                // mediaPlayer.seekTo((int)seektopos);
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // The AUDIOFOCUS_GAIN case means we have regained focus and can resume playback.
                Log.i("app", "onAudioFocusChange: gain ");
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }

            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // The AUDIOFOCUS_LOSS case means we've lost audio focus and
                // Stop playback and clean up resources
                Log.i("app", "onAudioFocusChange: release");
                releaseMediaPlayer();
            }
        }
    };
    //    When audio is completed
    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            // Now that the sound file has finished playing, release the media player resources.


            Toast.makeText(getApplicationContext(), " " + seektopos, Toast.LENGTH_SHORT).show();
            // releaseMediaPlayer();
            mediaPlayer.pause();
            mediaPlayer.seekTo((int) startTime);


//        mediaPlayer.seekTo(0);
//        mediaPlayer.start();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        id = intent.getIntExtra("id", 0);
        str = intent.getStringExtra("type");
        //if(id!=0){
        intentReceivedOrScreenRotated = true;
        //}

        Log.i("app", "onCreate: uri"+uri);




        if (uri!=null){

        }


        else {




            mediaplayerResourceId = R.raw.ubuntu;//// Add a different audio file to make it work






            Log.i("app", "onCreate: id  else:" + id);
            startTime = 0;
            Log.i("app", "onCreate: startTime " + startTime);
            Log.i("app", "onCreate: else executed");


        }

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            seektopos = savedInstanceState.getLong("seektopos");
            forwardTime = savedInstanceState.getInt("forwardTime");
            backwardTime = savedInstanceState.getInt("backwardTime");
            uri=savedInstanceState.getParcelable("uri");
            // highlightStart=savedInstanceState.getDouble("highlightStart");

        }

        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.



        // Create and setup the {@link AudioManager} to request audio focus
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


        play = findViewById(R.id.play);
        pause = findViewById(R.id.pause);
        forward = findViewById(R.id.forward);
        backward = findViewById(R.id.backward);

        choose = findViewById(R.id.button2);


        //seekbar = findViewById(R.id.seekBar);
        //seekbar.setClickable(false);


        pause.setEnabled(false);
        play.setEnabled(false);


        // Request audio focus so in order to play the audio file.

        int result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // We have audio focus now.
            pause.setEnabled(true);
            play.setEnabled(true);
        }

        mediaPlayer = MediaPlayer.create(this, mediaplayerResourceId);
        Log.i("mp created", "onCreate: ");


        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // currentTime=mediaPlayer.getCurrentPosition();


                // Release the media player if it currently exists because we are about to
                // play a different sound file

                //releaseMediaPlayer();
                if (mediaPlayer != null) {
                    currentTime = mediaPlayer.getCurrentPosition();
                    if (!mediaPlayer.isPlaying()) {
                        Log.i("mp satrted", "onClick: ");
                        if (seektopos != 0) {

                            mediaPlayer.seekTo((int) seektopos);
                            Log.i("app", "onClick: " + seektopos);
                            seektopos = 0;
                        }// Start the audio file

                        mediaPlayer.start();


                            finalTime = mediaPlayer.getDuration();//for starting audio
                            pause.setEnabled(true);



                        // Setup a listener on the media player, so that we can stop and release the
                        // media player once the sound has finished playing.
                        mediaPlayer.setOnCompletionListener(mCompletionListener);//Should this line come at last?


                        Log.i("app", "onClick: play finaltime" + finalTime);


                    }
                }
//                    play.setEnabled(false);

            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying())
                        mediaPlayer.pause();


                }


            }
        });

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null) {
                    currentTime = mediaPlayer.getCurrentPosition();
                    extraTime = finalTime - currentTime;
                    Log.i("app", "onClick: currentTime" + currentTime);
                    Log.i("app", "onClick: finalTime" + finalTime);

                    Log.i("app", "backward: " + (currentTime + forwardTime) + "<=" + finalTime);

                    if ((currentTime + forwardTime) <= finalTime) {
                        currentTime = currentTime + forwardTime;
                        mediaPlayer.seekTo((int) currentTime);



                        Toast.makeText(getApplicationContext(), "You have Jumped forward" + forwardTime / 1000 + "seconds", Toast.LENGTH_SHORT).show();
                    } else if (extraTime > 0 && extraTime < 10000) {
                        Toast.makeText(getApplicationContext(), "Audio is going to end at " + extraTime / 1000 + "seconds", Toast.LENGTH_SHORT).show();
                    }

//
                }
            }
        });
        backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null) {
                    currentTime = mediaPlayer.getCurrentPosition();
                    extraTime = currentTime - startTime;
                    Log.i("app", "onClick: currentTime" + currentTime);
                    Log.i("app", "onClick: startTime" + startTime);

                    Log.i("app", "backward: " + (currentTime - backwardTime) + ">" + startTime);
                    if ((currentTime - backwardTime) > startTime) {


                        currentTime = currentTime - backwardTime;
                        mediaPlayer.seekTo((int) currentTime);


                        Toast.makeText(getApplicationContext(), "You have Jumped backward" + backwardTime / 1000 + "seconds", Toast.LENGTH_SHORT).show();


                    } else if (extraTime > 0 && extraTime < 10000) {
                        mediaPlayer.seekTo((int) startTime);




                        Toast.makeText(getApplicationContext(), "You have Jumped backward" + extraTime / 1000 + "seconds", Toast.LENGTH_SHORT).show();// for testing


                    }
//
                }
            }
        });




        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();

        Log.i("onStart is called", "onStart: ");
    }

    @Override
    protected void onPause() {
        super.onPause();



    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
        Log.i("app", "onSaveInstanceState: ");
        if (mediaPlayer != null) {
            savedInstanceState.putLong("seektopos", mediaPlayer.getCurrentPosition());
            savedInstanceState.putInt("forwardTime", forwardTime);
            savedInstanceState.putInt("backwardTime", backwardTime);
            if(uri!=null)
                savedInstanceState.putParcelable("uri",uri);

//            sharedPref=getPreferences(Context.MODE_PRIVATE);
//            SharedPreferences.Editor editor = sharedPref.edit();
//            editor.putLong("SeektoposAfterHomePress", mediaPlayer.getCurrentPosition());
////
//            editor.commit();
            // savedInstanceState.putDouble("highlightStart",highlightStart);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i("app", "onRestoreInstanceState: ");
    }

    @Override
    protected void onResume() {
        super.onResume();



//        sharedPref=getPreferences(Context.MODE_PRIVATE);
//       long seektoposAfterHomePress = sharedPref.getLong("SeektoposAfterHomePress",0);
//        Log.i("app", "onResume: "+seektoposAfterHomePress);
        if(uri!=null){

//            if (intentReceivedOrScreenRotated) {
//
//                Log.i("app", "onResume: seektopos" + seektopos);
//                Log.i("Resume is called", "onResume: " + mediaPlayer.getCurrentPosition());
//                intentReceivedOrScreenRotated = false;
//
//            }
//
//            else{
            try {
                mediaPlayer=new MediaPlayer();
                mediaPlayer.setDataSource(getApplicationContext(), uri);
                mediaPlayer.prepare();
                Log.i("app", "executed: onresume uri part ");
            } catch (IOException e) {
                e.printStackTrace();
            }
            //}



        }
       else{
            mediaPlayer = MediaPlayer.create(this, mediaplayerResourceId);
            if (intentReceivedOrScreenRotated) {

                Log.i("app", "onResume: seektopos" + seektopos);
                Log.i("Resume is called", "onResume: " + mediaPlayer.getCurrentPosition());
                intentReceivedOrScreenRotated = false;

            }

        }

        Log.i("Resume is called", "onResume: " + mediaPlayer.getCurrentPosition());
        mediaPlayer.seekTo((int) seektopos);

    }

    @Override
    public void onStop() {
        super.onStop();

        // When the activity is stopped, release the media player resources because we won't
        // be playing any more sounds.

        releaseMediaPlayer();
        // pause.setEnabled(false);
        Log.i("mp released", "onStop: ");

    }


    /**
     * Clean up the media player by releasing its resources.
     */
    private void releaseMediaPlayer() {
        // If the media player is not null, then it may be currently playing a sound.
        if (mediaPlayer != null) {
            // Regardless of the current state of the media player, release its resources
            // because we no longer need it.
            mediaPlayer.release();

            // Set the media player back to null. For our code, we've decided that
            // setting the media player to null is an easy way to tell that the media player
            // is not configured to play an audio file at the moment.
            mediaPlayer = null;
            Log.i("app", "releaseMediaPlayer:media is null ");

            // Regardless of whether or not we were granted audio focus, abandon it. This also
            // unregisters the AudioFocusChangeListener so we don't get anymore callbacks.
            mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        }
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == R.id.mybutton) {
            // do something here
            menu();
            Log.i("app", "menu() called: ");


        }
        return super.onOptionsItemSelected(item);
    }

    public void menu() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.pick_speed)
                .setItems(R.array.speed_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        Log.i("app", "forward time changed ");
                        switch (which) {
                            case 0:
                                forwardTime = backwardTime = 10000;
                                break;
                            case 1:
                                forwardTime = backwardTime = 15000;
                                break;
                            case 2:
                                forwardTime = backwardTime = 30000;
                                break;
                            case 3:
                                forwardTime = backwardTime = 60000;
                                break;

                        }
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }



    @Override
    protected void onDestroy() {

        super.onDestroy();

        //cancelTimer();

        Log.i("app", "onDestroy: ");
    }







    public void setFinalTime(long finalTime) {
        this.finalTime = finalTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }


    public void ResumeAudio(final long resumeTime) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

//               sharedPref=getPreferences(Context.MODE_PRIVATE);
//        final long seektoposAfterHomePress = sharedPref.getLong("SeektoposAfterHomePress",0);

        alertDialogBuilder.setMessage("Do you want to resume or restart the audio?");
        alertDialogBuilder.setPositiveButton("Resume",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        seektopos = resumeTime;
                        setStartTime(0);
                        setFinalTime(mediaPlayer.getDuration());
                        Log.i("app", "onClick: Resume seektopos" + seektopos);
                        Log.i("app", "onClick: Resume startTime" + startTime);
                        Log.i("app", "onClick: Resume finalTime" + finalTime);


                    }
                });

        alertDialogBuilder.setNegativeButton("Restart", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                seektopos=0;
//                mediaPlayer.seekTo((int)seektopos);
                seektopos = 0;
                Log.i("app", "onClick: Restart " + seektopos);
                setFinalTime(mediaPlayer.getDuration());
                Log.i("app", "onClick: Restart seektopos" + seektopos);
                Log.i("app", "onClick: Restart startTime" + startTime);
                Log.i("app", "onClick: Restart startTime" + finalTime);
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }




    private void openGallery() {

        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("audio/mpeg");


        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setDataAndType(uri,"audio/mpeg");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Audio");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_AUDIO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        super.onActivityResult(requestCode,resultCode,resultData);
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == PICK_AUDIO && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i("app", "Uri: " + uri.toString());
                Toast.makeText(getApplicationContext(), "picked audio"+uri, Toast.LENGTH_SHORT).show();
//                mediaPlayer = new MediaPlayer();
//                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                try{
//                    mediaPlayer.setDataSource(getApplicationContext(), uri);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

            }
        }
    }
}



