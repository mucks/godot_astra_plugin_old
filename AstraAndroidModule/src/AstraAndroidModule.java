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
    private static final int updateSleepMS = 30;
    private static final String logTagFrame = "ASTRA_FRAME";
    private static final String logTagPointStream = "ASTRA_POINT";
    private static final String logTagDepthStream = "ASTRA_DEPTH";
    private static final String logTagMy = "MY_TAG";
    private boolean first_call = true;
    private int instanceId = 0;
    private int width = 640;
    private int height = 480;
    private byte[] colorByteArray = new byte[0];

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
                startBodyStream();

                /*
                 * THIS WORKS!!! ArrayList<Vector3D> vector3DList = get3DVectors();
                 *
                 * Log.e("DATA", "x of 200th vector: " + vector3DList.get(200).getX());
                 * Log.e("DATA", "y of 200th vector: " + vector3DList.get(200).getY());
                 * Log.e("DATA", "z of 200th vector:
                 * " + vector3DList.get(200).getZ()); Log.e("DATA", "size of list: " +
                 * vector3DList.size());
                 */
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                startColorStream();
            }
        }).start();
    }

    public void startColorStream() {
        try {
            Log.d(logTagMy, "Trying to open color stream");
            StreamSet streamSet = StreamSet.open();
            StreamReader reader = streamSet.createReader();
            reader.addFrameListener(new StreamReader.FrameListener() {
                public void onFrameReady(StreamReader reader, ReaderFrame frame) {
                    ColorFrame colorFrame = ColorFrame.get(frame);

                    ByteBuffer colorBuffer = colorFrame.getByteBuffer();

                    if (colorFrame.isValid()) {

                        Log.d(logTagMy, "valid color frame");
                        int byteLength = width * height * 4;

                        if (colorByteArray.length != byteLength) {
                            colorByteArray = new byte[byteLength];
                            for (int i = 0; i < byteLength; i++) {
                                colorByteArray[i] = 0;
                            }
                        }

                        for (int i = 0; i < byteLength; i++) {
                            int rgbOffSet = i * 4;
                        }

                        GodotLib.calldeferred(instanceId, "_new_color_frame", new Object[] { colorBuffer });
                    }
                }
            });
            ColorStream colorStream = ColorStream.get(reader);
            colorStream.start();

            while (true) {
                Astra.update();
                TimeUnit.MILLISECONDS.sleep(updateSleepMS);
            }

        } catch (Throwable e) {
        }
    }

    public void startBodyStream() {
        final boolean[] frameFinished = { false };

        // Call depth stream once to set resoultion. Can't set resolution via
        // point stream.
        if (first_call) {
            getDepthData();
            first_call = false;
        }

        try {
            Log.d(logTagPointStream, "Trying to open stream");
            StreamSet streamSet = StreamSet.open();
            StreamReader reader = streamSet.createReader();

            reader.addFrameListener(new StreamReader.FrameListener() {
                public void onFrameReady(StreamReader reader, ReaderFrame frame) {
                    BodyFrame bodyFrame = BodyFrame.get(frame);

                    Iterable<Body> bodies = bodyFrame.getBodies();

                    if (bodyFrame.isValid()) {

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

                        // frameFinished[0] = true;
                    }
                }
            });

            BodyStream bodyStream = BodyStream.get(reader);
            bodyStream.start();

            while (true) {
                Astra.update();
                TimeUnit.MILLISECONDS.sleep(updateSleepMS);
            }
        } catch (Throwable e) {
            Log.e(logTagPointStream, e.toString());
        }
    }

    public ArrayList<Vector3D> get3DVectors() {
        final boolean[] frameFinished = { false };
        final ArrayList<Vector3D> vector3DList = new ArrayList<>();

        // Call depth stream once to set resoultion. Can't set resolution via
        // point stream.
        if (first_call) {
            getDepthData();
            first_call = false;
        }

        try {
            Log.d(logTagPointStream, "Trying to open stream");
            StreamSet streamSet = StreamSet.open();
            StreamReader reader = streamSet.createReader();

            reader.addFrameListener(new StreamReader.FrameListener() {
                public void onFrameReady(StreamReader reader, ReaderFrame frame) {
                    PointFrame pf = PointFrame.get(frame);
                    FloatBuffer buffer = pf.getPointBuffer();

                    if (pf.isValid()) {
                        Log.d(logTagFrame, "frame is valid");
                        Log.d(logTagFrame, "height: " + pf.getHeight());
                        Log.d(logTagFrame, "width: " + pf.getWidth());

                        while (buffer.hasRemaining()) {
                            vector3DList.add(new Vector3D(buffer.get(), buffer.get(), buffer.get() * -1));
                        }
                        frameFinished[0] = true;
                    }
                }
            });

            PointStream pointStream = PointStream.get(reader);
            pointStream.start();

            while (!frameFinished[0]) {
                Astra.update();
                TimeUnit.MILLISECONDS.sleep(updateSleepMS);
            }

            pointStream.stop();
            reader.destroy();
            streamSet.close();
        } catch (Throwable e) {
            Log.e(logTagPointStream, e.toString());
        }

        Log.d(logTagPointStream, "size of list: " + vector3DList.size());

        return vector3DList;
    }

    public ArrayList<Vector3D> getDepthData() {
        final boolean[] frameFinished = { false };
        final ArrayList<Vector3D> vector3DList = new ArrayList<>();

        try {
            Log.d(logTagDepthStream, "Trying to open stream");
            StreamSet streamSet = StreamSet.open();
            StreamReader reader = streamSet.createReader();

            reader.addFrameListener(new StreamReader.FrameListener() {
                public void onFrameReady(StreamReader reader, ReaderFrame frame) {
                    DepthFrame df = DepthFrame.get(frame);
                    ShortBuffer buffer = df.getDepthBuffer();
                    Log.d(logTagFrame, "new frame");

                    if (df.isValid()) {
                        Log.d(logTagFrame, "frame is valid");
                        Log.d(logTagFrame, "height: " + df.getHeight());
                        Log.d(logTagFrame, "width: " + df.getWidth());

                        while (buffer.hasRemaining()) {
                            // TODO Check if data looks like below data
                            vector3DList.add(new Vector3D(buffer.get(), buffer.get(), buffer.get() * -1));
                        }
                        frameFinished[0] = true;
                    }
                }
            });

            DepthStream depthStream = DepthStream.get(reader);
            // depthStream.setMode(new ImageStreamMode(0, width, height, 100, 30));
            depthStream.start();

            while (!frameFinished[0]) {
                Astra.update();
                TimeUnit.MILLISECONDS.sleep(updateSleepMS);
            }

            depthStream.stop();
            reader.destroy();
            streamSet.close();
        } catch (Throwable e) {
            Log.e(logTagDepthStream, "MYERROR");
            Log.e(logTagDepthStream, e.toString());
        }

        Log.d(logTagDepthStream, "size of list: " + vector3DList.size());

        return vector3DList;
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