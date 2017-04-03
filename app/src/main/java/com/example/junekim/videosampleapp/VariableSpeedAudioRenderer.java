package com.example.junekim.videosampleapp;

import android.annotation.TargetApi;
import android.media.AudioTimestamp;
import android.media.MediaCodec;
import android.media.MediaFormat;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.audio.AudioTrack;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.util.ConditionVariable;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.logging.Handler;

/**
 * Created by JuneKim on 17. 4. 3..
 */
public class VariableSpeedAudioRenderer extends MediaCodecAudioRenderer {

    public VariableSpeedAudioRenderer(MediaCodecSelector mediaCodecSelector) {
        super(mediaCodecSelector);
    }

    private final int SAMPLES_PER_CODEC_FRAME = 10;
    private final int channelCount=4;
    private Sonic sonic;
    private ByteBuffer lastInternalBuffer;
    private byte[] sonicInputBuffer;
    private byte[] sonicOutputBuffer;
    private float audioSpeed;
    private final int bytesToRead=4;

    @Override
    protected void onOutputFormatChanged(MediaCodec codec, MediaFormat outputFormat) {

        super.onOutputFormatChanged(codec,outputFormat);
       // Two samples per frame * 2 to support audio speeds down to 0.5
        final int bufferSizeBytes = SAMPLES_PER_CODEC_FRAME * 2 * 2 * channelCount;
//        final int bufferSizeBytes = 400;

        this.sonicInputBuffer = new byte[bufferSizeBytes];
        this.sonicOutputBuffer = new byte[bufferSizeBytes];

        this.sonic = new Sonic(
                Integer.getInteger(MediaFormat.KEY_SAMPLE_RATE),
                Integer.getInteger(MediaFormat.KEY_CHANNEL_COUNT));

        this.lastInternalBuffer = ByteBuffer.wrap(sonicOutputBuffer, 0, 0);

        sonic.flushStream();
        sonic.setSpeed(audioSpeed);
    }

    @Override
    protected boolean processOutputBuffer(long positionUs, long elapsedRealtimeUs, MediaCodec codec,
                                          ByteBuffer buffer, int bufferIndex, int bufferFlags, long bufferPresentationTimeUs,
                                          boolean shouldSkip) throws ExoPlaybackException{



        buffer.get(sonicInputBuffer, 0, bytesToRead);
        sonic.writeBytesToStream(sonicInputBuffer, bytesToRead);
        sonic.readBytesFromStream(sonicOutputBuffer, sonicOutputBuffer.length);

        return super.processOutputBuffer(positionUs,elapsedRealtimeUs,codec, lastInternalBuffer, bufferIndex,bufferFlags,bufferPresentationTimeUs,shouldSkip);

    }
}
