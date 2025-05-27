package robotik;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;

/**
     * Wrapper class to allow easier use of EV3ColorSensor.
     */
    public class EV3ColorSensorWrapper extends EV3ColorSensor {

        private final SampleProvider sampleProviderRed;
        private final SampleProvider sampleProviderRGB;
        private final SampleProvider sampleProviderColor;
        private final float[] samples;

        public EV3ColorSensorWrapper(Port port) {
            super(port);

            try {
                sampleProviderRed = super.getRedMode();
                sampleProviderRGB = super.getRGBMode();
                sampleProviderColor = super.getColorIDMode();
            } catch (IllegalArgumentException e) {
                super.close();
                throw e;
            }

            samples = new float[3];
        }

       /**
         * Measures the level of reflected light from the sensors RED LED.
         *
         * @return A value containing the intensity level (Normalized between 0
         * and 1) of reflected light.
         *
         */
        public double getReflectedRed() {
            sampleProviderRed.fetchSample(samples, 0);

            return samples[0];
        }

        /**
         * Measures the level of reflected red, green and blue light when
         * illuminated by a white light source.
         *
         * @return The sample contains 3 elements containing the intensity level
         * (Normalized between 0 and 1) of red, green and blue light
         * respectivily.
         */
        public double[] getReflectedRGB() {
            sampleProviderRGB.fetchSample(samples, 0);

            return new double[]{samples[0], samples[1], samples[2]};
        }

        /**
         * Measures the color ID of a surface. The sensor can identify 8 unique
         * colors (NONE, BLACK, BLUE, GREEN, YELLOW, RED, WHITE, BROWN).
         *
         * @return A value corresponding to identified color. See
         * {@link lejos.robotics.Color}
         */
        public int getColor() {
            sampleProviderColor.fetchSample(samples, 0);

            return (int) samples[0];
        }
    }
