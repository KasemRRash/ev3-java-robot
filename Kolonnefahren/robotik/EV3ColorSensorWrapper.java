package robotik;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;

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

 
        public double getReflectedRed() {
            sampleProviderRed.fetchSample(samples, 0);

            return samples[0];
        }


        public double[] getReflectedRGB() {
            sampleProviderRGB.fetchSample(samples, 0);

            return new double[]{samples[0], samples[1], samples[2]};
        }

        public int getColor() {
            sampleProviderColor.fetchSample(samples, 0);

            return (int) samples[0];
        }
    }

