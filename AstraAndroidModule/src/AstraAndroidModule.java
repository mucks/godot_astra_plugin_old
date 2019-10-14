package org.godotengine.godot;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.godot.game.R;
import com.orbbec.astra.Astra;
import com.orbbec.astra.Body;
import com.orbbec.astra.BodyFrame;
import com.orbbec.astra.BodyStream;
import com.orbbec.astra.ColorFrame;
import com.orbbec.astra.ColorStream;
import com.orbbec.astra.DepthFrame;
import com.orbbec.astra.DepthStream;
import com.orbbec.astra.ImageStreamMode;
import com.orbbec.astra.Joint;
import com.orbbec.astra.PointFrame;
import com.orbbec.astra.PointStream;
import com.orbbec.astra.ReaderFrame;
import com.orbbec.astra.StreamReader;
import com.orbbec.astra.StreamSet;
import com.orbbec.astra.Vector3D;
import com.orbbec.astra.android.AstraAndroidContext;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class AstraAndroidModule extends Godot.SingletonBase { // class name should be same as java file
                                                              // name...duh
    private AstraAndroidContext aac;
    private String logTagAstra = "ASTRA";
    private static final int updateSleepMS = 0;
    private int instanceId = 0;

    public void set_instance_id(int instance_id) {
        this.instanceId = instance_id;
    }

    public int myFirstFunction(int a, int b) {
        return a + b * 2; // should return integer
    }

    static public Godot.SingletonBase initialize(Activity p_activity) {
        return new AstraAndroidModule(p_activity);
    }

    public AstraAndroidModule(Activity p_activity) {
        registerClass("AstraAndroidModule", new String[] { "myFirstFunction", "getData", "set_instance_id" });

        aac = new AstraAndroidContext(p_activity.getApplicationContext());
        aac.initialize();
        aac.openAllDevices();
    }

    private void getData() {
        new Thread(new Runnable() {
            public void run() {
                StreamSet streamSet = StreamSet.open();
                StreamReader reader = streamSet.createReader();

                // startDepthStream(reader);
                startBodyStream(reader);
                startColorStream(reader);

                addListener(reader);

                try {
                    while (true) {
                        Astra.update();
                        TimeUnit.MILLISECONDS.sleep(updateSleepMS);
                    }
                } catch (Exception e) {
                }
            }
        }).start();
    }

    public void addListener(StreamReader reader) {
        reader.addFrameListener(new StreamReader.FrameListener() {
            public void onFrameReady(StreamReader reader, ReaderFrame frame) {
                updateBody(frame);
                updateColor(frame);
                updateDepth(frame);
            }
        });
    }

    private void updateColor(ReaderFrame frame) {
        ColorFrame colorFrame = ColorFrame.get(frame);

        if (colorFrame.isValid()) {
            ByteBuffer colorBufferRGB = colorFrame.getByteBuffer();
            int img_width = colorFrame.getWidth();
            int img_height = colorFrame.getHeight();

            int byteLength = img_width * img_height * 4;

            byte[] colorBufferRGBA = new byte[byteLength];

            for (int i = 0; i < img_width * img_height; i++) {
                int rgba_index = i * 4;
                int rgb_index = i * 3;

                colorBufferRGBA[rgba_index] = colorBufferRGB.get(rgb_index);
                colorBufferRGBA[rgba_index + 1] = colorBufferRGB.get(rgb_index + 1);
                colorBufferRGBA[rgba_index + 2] = colorBufferRGB.get(rgb_index + 2);
                colorBufferRGBA[rgba_index + 3] = (byte) 255;
            }

            GodotLib.calldeferred(instanceId, "_new_color_frame",
                    new Object[] { img_width, img_height, colorBufferRGBA });
        }
    }

    private void updateBody(ReaderFrame frame) {
        BodyFrame bodyFrame = BodyFrame.get(frame);

        if (bodyFrame.isValid()) {
            Iterable<Body> bodies = bodyFrame.getBodies();

            for (Body body : bodies) {
                Dictionary joints = new Dictionary();
                for (Joint joint : body.getJoints()) {
                    int jointId = joint.getType().ordinal();
                    Vector3D pos = joint.getWorldPosition();

                    Dictionary godotPos = new Dictionary();
                    godotPos.put("x", pos.getX());
                    godotPos.put("y", pos.getY());
                    godotPos.put("z", pos.getZ());

                    joints.put(jointId + "", godotPos);
                }
                GodotLib.calldeferred(instanceId, "_new_body_frame", new Object[] { joints });
            }
        }
    }

    public void updateDepth(ReaderFrame frame) {
        DepthFrame depthFrame = DepthFrame.get(frame);

        if (depthFrame.isValid()) {
            ShortBuffer depthBuffer = depthFrame.getDepthBuffer();

            ArrayList<Dictionary> positions = new ArrayList<Dictionary>();

            while (depthBuffer.hasRemaining()) {
                Dictionary godotPos = new Dictionary();
                godotPos.put("x", depthBuffer.get());
                godotPos.put("y", depthBuffer.get());
                godotPos.put("z", depthBuffer.get() * -1);
                positions.add(godotPos);
            }
            GodotLib.calldeferred(instanceId, "_new_depth_frame", new Object[] { positions });
        }
    }

    public void startColorStream(StreamReader reader) {
        ColorStream colorStream = ColorStream.get(reader);
        colorStream.start();
    }

    public void startBodyStream(StreamReader reader) {
        BodyStream bodyStream = BodyStream.get(reader);
        bodyStream.start();
    }

    public void startDepthStream(StreamReader reader) {
        DepthStream depthStream = DepthStream.get(reader);
        depthStream.start();
    }

    // Not needed for our purposes

    // forwarded callbacks you can reimplement, as SDKs often need them
    // protected void onMainActivityResult(int requestCode, int resultCode,
    // Intent data) {}
    //
    // protected void onMainPause() {}
    // protected void onMainResume() {}
    // protected void onMainDestroy() {}
    //
    // protected void onGLDrawFrame(GL10 gl) {}
    // protected void onGLSurfaceChanged(GL10 gl, int width, int height) {} //
    // singletons will always miss first onGLSurfaceChanged call
}