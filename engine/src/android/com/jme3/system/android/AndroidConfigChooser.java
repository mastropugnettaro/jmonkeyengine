package com.jme3.system.android;

import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView.EGLConfigChooser;
import com.jme3.renderer.android.RendererUtil;
import com.jme3.system.AppSettings;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * AndroidConfigChooser is used to determine the best suited EGL Config
 *
 * @author larynx
 */
public class AndroidConfigChooser implements EGLConfigChooser {

    private static final Logger logger = Logger.getLogger(AndroidConfigChooser.class.getName());
    public final static String SETTINGS_CONFIG_TYPE = "configType";
    protected int clientOpenGLESVersion = 0;
    protected EGLConfig bestConfig = null;
    protected EGLConfig fastestConfig = null;
    protected EGLConfig choosenConfig = null;
    protected AppSettings settings;
    protected int pixelFormat;
    protected boolean verbose = false;
    private final static int EGL_OPENGL_ES2_BIT = 4;

    public enum ConfigType {

        /**
         * RGB565, 0 alpha, 16 depth, 0 stencil
         */
        FASTEST(5, 6, 5, 0, 16, 0, 5, 6, 5, 0, 16, 0),
        /**
         * min RGB888, 0 alpha, 16 depth, 0 stencil max RGB888, 0 alpha, 32
         * depth, 8 stencil
         */
        BEST(8, 8, 8, 0, 32, 8, 8, 8, 8, 0, 16, 0),
        /**
         * Turn off config chooser and use hardcoded
         * setEGLContextClientVersion(2); setEGLConfigChooser(5, 6, 5, 0, 16,
         * 0);
         */
        LEGACY(5, 6, 5, 0, 16, 0, 5, 6, 5, 0, 16, 0),
        /**
         * min RGB888, 8 alpha, 16 depth, 0 stencil max RGB888, 8 alpha, 32
         * depth, 8 stencil
         */
        BEST_TRANSLUCENT(8, 8, 8, 8, 32, 8, 8, 8, 8, 8, 16, 0);
        /**
         * red, green, blue, alpha, depth, stencil (max values)
         */
        int r, g, b, a, d, s;
        /**
         * minimal values
         */
        int mr, mg, mb, ma, md, ms;

        private ConfigType(int r, int g, int b, int a, int d, int s, int mr, int mg, int mb, int ma, int md, int ms) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            this.d = d;
            this.s = s;
            this.mr = mr;
            this.mg = mg;
            this.mb = mb;
            this.ma = ma;
            this.md = md;
            this.ms = ms;
        }
    }

    /**
     *
     * @param type
     * @deprecated use AndroidConfigChooser(AppSettings settings)
     */
    @Deprecated
    public AndroidConfigChooser(ConfigType type) {
        this.settings = new AppSettings(true);
        settings.put(SETTINGS_CONFIG_TYPE, type);
    }

    public AndroidConfigChooser(AppSettings settings) {
        this.settings = settings;
    }

    private static int eglGetConfigAttribSafe(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute) {
        int[] value = new int[1];
        if (!egl.eglGetConfigAttrib(display, config, attribute, value)) {
            RendererUtil.checkEGLError(egl);
            throw new AssertionError();
        }
        return value[0];
    }
    
    /**
     * Gets called by the GLSurfaceView class to return the best config
     */
    @Override
    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
        logger.fine("GLSurfaceView asks for egl config, returning: ");
        logEGLConfig(choosenConfig, display, egl, Level.FINE);
        return choosenConfig;
    }

    /**
     * findConfig is used to locate the best config and init the chooser with
     *
     * @param egl
     * @param display
     * @return true if successfull, false if no config was found
     */
    public boolean findConfig(EGL10 egl, EGLDisplay display) {
        ConfigType type = (ConfigType) settings.get(SETTINGS_CONFIG_TYPE);

        ComponentSizeChooser compChooser = new ComponentSizeChooser(type, settings.getSamples());
        choosenConfig = compChooser.chooseConfig(egl, display);
        logger.log(Level.FINE, "JME3 using {0} EGL configuration available here: ", type.name());

        if (choosenConfig != null) {
            logger.info("JME3 using choosen config: ");
            logEGLConfig(choosenConfig, display, egl, Level.INFO);
            pixelFormat = getPixelFormat(choosenConfig, display, egl);
            clientOpenGLESVersion = getOpenGLVersion(choosenConfig, display, egl);
            return true;
        } else {
            logger.severe("ERROR: Unable to get a valid OpenGL ES 2.0 config, neither Fastest nor Best found! Bug. Please report this.");
            clientOpenGLESVersion = 1;
            pixelFormat = PixelFormat.UNKNOWN;
            return false;
        }
    }

    private int getPixelFormat(EGLConfig conf, EGLDisplay display, EGL10 egl) {
        //Android Pixel format is not very well documented.
        //From what i gathered, the format is chosen automatically except for the alpha channel
        //if the alpha channel has 8 bit or more, e set the pixel format to Transluscent, as it allow transparent view background
        //if it's 0 bit, the format is OPAQUE otherwise it's TRANSPARENT
        int result = eglGetConfigAttribSafe(egl, display, conf, EGL10.EGL_ALPHA_SIZE);

        if (result >= 8) {
            return PixelFormat.TRANSLUCENT;
        }
        if (result >= 1) {
            return PixelFormat.TRANSPARENT;
        }

        return PixelFormat.OPAQUE;
    }

    private int getOpenGLVersion(EGLConfig conf, EGLDisplay display, EGL10 egl) {
        int val = eglGetConfigAttribSafe(egl, display, conf, EGL10.EGL_RENDERABLE_TYPE);
        // Check if conf is OpenGL ES 2.0
        if ((val & EGL_OPENGL_ES2_BIT) != 0) {
            return 2;
        } else {
            return 1;
        }
    }

    /**
     * log output with egl config details
     *
     * @param conf
     * @param display
     * @param egl
     */
    public void logEGLConfig(EGLConfig conf, EGLDisplay display, EGL10 egl, Level level) {

        logger.log(level, "EGL_RED_SIZE = {0}",
                eglGetConfigAttribSafe(egl, display, conf, EGL10.EGL_RED_SIZE));

        logger.log(level, "EGL_GREEN_SIZE = {0}",
                eglGetConfigAttribSafe(egl, display, conf, EGL10.EGL_GREEN_SIZE));

        logger.log(level, "EGL_BLUE_SIZE = {0}",
                eglGetConfigAttribSafe(egl, display, conf, EGL10.EGL_BLUE_SIZE));

        logger.log(level, "EGL_ALPHA_SIZE = {0}",
                eglGetConfigAttribSafe(egl, display, conf, EGL10.EGL_ALPHA_SIZE));

        logger.log(level, "EGL_DEPTH_SIZE = {0}",
                eglGetConfigAttribSafe(egl, display, conf, EGL10.EGL_DEPTH_SIZE));

        logger.log(level, "EGL_STENCIL_SIZE = {0}",
                eglGetConfigAttribSafe(egl, display, conf, EGL10.EGL_STENCIL_SIZE));

        logger.log(level, "EGL_RENDERABLE_TYPE = {0}",
                eglGetConfigAttribSafe(egl, display, conf, EGL10.EGL_RENDERABLE_TYPE));

        logger.log(level, "EGL_SURFACE_TYPE = {0}",
                eglGetConfigAttribSafe(egl, display, conf, EGL10.EGL_SURFACE_TYPE));

        logger.log(level, "EGL_SAMPLE_BUFFERS = {0}",
                eglGetConfigAttribSafe(egl, display, conf, EGL10.EGL_SAMPLE_BUFFERS));

        logger.log(level, "EGL_SAMPLES = {0}",
                eglGetConfigAttribSafe(egl, display, conf, EGL10.EGL_SAMPLES));
    }

    public int getClientOpenGLESVersion() {
        return clientOpenGLESVersion;
    }

    public void setClientOpenGLESVersion(int clientOpenGLESVersion) {
        this.clientOpenGLESVersion = clientOpenGLESVersion;
    }

    public int getPixelFormat() {
        return pixelFormat;
    }

    private abstract class BaseConfigChooser implements EGLConfigChooser {

        public BaseConfigChooser() {
        }

        @Override
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {

            int[] num_config = new int[1];
            int[] configSpec = new int[]{
                EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                EGL10.EGL_NONE};

            if (!egl.eglChooseConfig(display, configSpec, null, 0, num_config)) {
                RendererUtil.checkEGLError(egl);
                throw new AssertionError();
            }

            int numConfigs = num_config[0];
            EGLConfig[] configs = new EGLConfig[numConfigs];
            if (!egl.eglChooseConfig(display, configSpec, configs, numConfigs, num_config)) {
                RendererUtil.checkEGLError(egl);
                throw new AssertionError();
            }

            logger.fine("--------------Display Configurations---------------");
            for (EGLConfig eGLConfig : configs) {
                logEGLConfig(eGLConfig, display, egl, Level.FINE);
                logger.fine("----------------------------------------");
            }

            EGLConfig config = chooseConfig(egl, display, configs);
            return config;
        }

        abstract EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                EGLConfig[] configs);
    }

    /**
     * Choose a configuration with exactly the specified r,g,b,a sizes, and at
     * least the specified depth and stencil sizes.
     */
    private class ComponentSizeChooser extends BaseConfigChooser {

        private ConfigType configType;
        protected int mSamples;

        public ComponentSizeChooser(ConfigType configType, int samples) {
            mSamples = samples;
            this.configType = configType;
        }

        @Override
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {

            EGLConfig keptConfig = null;
            int kd = 0;
            int knbMs = 0;


            // first pass through config list.  Try to find an exact match.
            for (EGLConfig config : configs) {
                int r = eglGetConfigAttribSafe(egl, display, config,
                        EGL10.EGL_RED_SIZE);
                int g = eglGetConfigAttribSafe(egl, display, config,
                        EGL10.EGL_GREEN_SIZE);
                int b = eglGetConfigAttribSafe(egl, display, config,
                        EGL10.EGL_BLUE_SIZE);
                int a = eglGetConfigAttribSafe(egl, display, config,
                        EGL10.EGL_ALPHA_SIZE);
                int d = eglGetConfigAttribSafe(egl, display, config,
                        EGL10.EGL_DEPTH_SIZE);
                int s = eglGetConfigAttribSafe(egl, display, config,
                        EGL10.EGL_STENCIL_SIZE);
                int isMs = eglGetConfigAttribSafe(egl, display, config,
                        EGL10.EGL_SAMPLE_BUFFERS);
                int nbMs = eglGetConfigAttribSafe(egl, display, config,
                        EGL10.EGL_SAMPLES);

                if (inRange(r, configType.mr, configType.r)
                        && inRange(g, configType.mg, configType.g)
                        && inRange(b, configType.mb, configType.b)
                        && inRange(a, configType.ma, configType.a)
                        && inRange(d, configType.md, configType.d)
                        && inRange(s, configType.ms, configType.s)) {
                    if (mSamples == 0 && isMs != 0) {
                        continue;
                    }
                    boolean keep;
                    //we keep the config if the depth is better or if the AA setting is better
                    if (d >= kd) {
                        kd = d;
                        keep = true;
                    } else {
                        keep = false;
                    }

                    if (mSamples != 0) {
                        if (nbMs >= knbMs && nbMs <= mSamples) {
                            knbMs = nbMs;
                            keep = true;
                        } else {
                            keep = false;
                        }
                    }

                    if (keep) {
                        keptConfig = config;
                    }
                }
            }

            if (keptConfig != null) {
                return keptConfig;
            }

            if (configType == ConfigType.BEST) {
                logger.log(Level.WARNING, "Failed to find a suitable display configuration for BEST, attempting BEST_TRANSLUCENT");
                configType = ConfigType.BEST_TRANSLUCENT;
                keptConfig = chooseConfig(egl, display, configs);
                if (keptConfig != null) {
                    return keptConfig;
                }
            }

            if (configType == ConfigType.BEST_TRANSLUCENT) {
                logger.log(Level.WARNING, "Failed to find a suitable display configuration for BEST_TRANSLUCENT, attempting FASTEST");
                configType = ConfigType.FASTEST;
                keptConfig = chooseConfig(egl, display, configs);

                if (keptConfig != null) {
                    return keptConfig;
                }
            }

            logger.log(Level.WARNING, "Failed to find a suitable display configuration for FASTEST, hoping for the best...");

            // failsafe. pick the 1st config with a 16 bit depth buffer.
            for (EGLConfig config : configs) {
                int d = eglGetConfigAttribSafe(egl, display, config,
                        EGL10.EGL_DEPTH_SIZE);
                if (d >= 16) {
                    return config;
                }
            }

            //nothing much we can do...
            return null;
        }

        private boolean inRange(int val, int min, int max) {
            return min <= val && val <= max;
        }
    }
//DON'T REMOVE THIS, USED FOR UNIT TESTING FAILING CONFIGURATION LISTS.
//    private static class Config {
//
//        int r, g, b, a, d, s, ms, ns;
//
//        public Config(int r, int g, int b, int a, int d, int s, int ms, int ns) {
//            this.r = r;
//            this.g = g;
//            this.b = b;
//            this.a = a;
//            this.d = d;
//            this.s = s;
//            this.ms = ms;
//            this.ns = ns;
//        }
//
//        @Override
//        public String toString() {
//            return "Config{" + "r=" + r + ", g=" + g + ", b=" + b + ", a=" + a + ", d=" + d + ", s=" + s + ", ms=" + ms + ", ns=" + ns + '}';
//        }
//    }
//
//    public static Config chooseConfig(List<Config> configs, ConfigType configType, int mSamples) {
//
//        Config keptConfig = null;
//        int kd = 0;
//        int knbMs = 0;
//
//
//        // first pass through config list.  Try to find an exact match.
//        for (Config config : configs) {
////                logEGLConfig(config, display, egl);
//            int r = config.r;
//            int g = config.g;
//            int b = config.b;
//            int a = config.a;
//            int d = config.d;
//            int s = config.s;
//            int isMs = config.ms;
//            int nbMs = config.ns;
//
//            if (inRange(r, configType.mr, configType.r)
//                    && inRange(g, configType.mg, configType.g)
//                    && inRange(b, configType.mb, configType.b)
//                    && inRange(a, configType.ma, configType.a)
//                    && inRange(d, configType.md, configType.d)
//                    && inRange(s, configType.ms, configType.s)) {
//                if (mSamples == 0 && isMs != 0) {
//                    continue;
//                }
//                boolean keep = false;
//                //we keep the config if the depth is better or if the AA setting is better
//                if (d >= kd) {
//                    kd = d;
//                    keep = true;
//                } else {
//                    keep = false;
//                }
//
//                if (mSamples != 0) {
//                    if (nbMs >= knbMs && nbMs <= mSamples) {
//                        knbMs = nbMs;
//                        keep = true;
//                    } else {
//                        keep = false;
//                    }
//                }
//
//                if (keep) {
//                    keptConfig = config;
//                }
//            }
//        }
//
//        if (keptConfig != null) {
//            return keptConfig;
//        }
//
//        if (configType == ConfigType.BEST) {
//            keptConfig = chooseConfig(configs, ConfigType.BEST_TRANSLUCENT, mSamples);
//
//            if (keptConfig != null) {
//                return keptConfig;
//            }
//        }
//
//        if (configType == ConfigType.BEST_TRANSLUCENT) {
//            keptConfig = chooseConfig(configs, ConfigType.FASTEST, mSamples);
//
//            if (keptConfig != null) {
//                return keptConfig;
//            }
//        }
//        // failsafe. pick the 1st config.
//
//        for (Config config : configs) {
//            if (config.d >= 16) {
//                return config;
//            }
//        }
//
//        return null;
//    }
//
//    private static boolean inRange(int val, int min, int max) {
//        return min <= val && val <= max;
//    }
//
//    public static void main(String... argv) {
//        List<Config> confs = new ArrayList<Config>();
//        confs.add(new Config(5, 6, 5, 0, 0, 0, 0, 0));
//        confs.add(new Config(5, 6, 5, 0, 16, 0, 0, 0));
//        confs.add(new Config(5, 6, 5, 0, 24, 8, 0, 0));
//        confs.add(new Config(8, 8, 8, 8, 0, 0, 0, 0));
////            confs.add(new Config(8, 8, 8, 8, 16, 0, 0, 0));
////            confs.add(new Config(8, 8, 8, 8, 24, 8, 0, 0));
//
//        confs.add(new Config(5, 6, 5, 0, 0, 0, 1, 2));
//        confs.add(new Config(5, 6, 5, 0, 16, 0, 1, 2));
//        confs.add(new Config(5, 6, 5, 0, 24, 8, 1, 2));
//        confs.add(new Config(8, 8, 8, 8, 0, 0, 1, 2));
////            confs.add(new Config(8, 8, 8, 8, 16, 0, 1, 2));
////            confs.add(new Config(8, 8, 8, 8, 24, 8, 1, 2));
//
//        confs.add(new Config(5, 6, 5, 0, 0, 0, 1, 4));
//        confs.add(new Config(5, 6, 5, 0, 16, 0, 1, 4));
//        confs.add(new Config(5, 6, 5, 0, 24, 8, 1, 4));
//        confs.add(new Config(8, 8, 8, 8, 0, 0, 1, 4));
////            confs.add(new Config(8, 8, 8, 8, 16, 0, 1, 4));
////            confs.add(new Config(8, 8, 8, 8, 24, 8, 1, 4));
//
//        Config chosen = chooseConfig(confs, ConfigType.BEST, 0);
//
//        System.err.println(chosen);
//
//    }
}
