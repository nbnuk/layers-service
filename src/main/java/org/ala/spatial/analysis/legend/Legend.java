/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ala.spatial.analysis.legend;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import javax.imageio.ImageIO;

/**
 *
 * @author Adam
 */
public abstract class Legend {

    /*
     * Colours are all set as transparent for no reason
     *
     * There are groups+1 colours
     */
    final static int[] colours = {0x00ffffff, 0x00009999, 0x0099FF66, 0x00FFFF66, 0x00FFFF00, 0x00FF9900, 0x00FF6600, 0x00FF6666, 0x00FF3300, 0x00CC33FF, 0x00FF33FF};

    /*
     * for determining the records that are equal to the maximum value
     */
    float[] cutoffs;
    /**
     * number of group members
     */
    int[] groupSizes;

    /*
     * min/max values
     */
    float min, max;

    /*
     * number of non-NaN values
     */
    int numberOfRecords;

    /*
     * number of unique values
     */
    int numberOfUniqueValues;

    /*
     * array position of last value
     */
    int lastValue;

    /**
     * generate the legend cutoff points.
     *
     * @param d asc sorted float []
     */
    abstract public void generate(float[] d);

    /**
     * return nice name for the method that this class uses to generate the
     * cutoff points
     *
     * @return name as String
     */
    abstract public String getTypeName();

    /**
     * some common values
     *
     * @param d as sorted float []
     */
    void init(float[] d) {
        //NaN sorted last.
        min = d[0];

        for (int i = 0; i < d.length; i++) {
            if (!Float.isNaN(d[i])) {
                numberOfRecords++;

                if (i == 0 || d[i] != d[i - 1]) {
                    numberOfUniqueValues++;
                }
            }
        }

        lastValue = numberOfRecords;
        if(numberOfRecords == 0) {
            max = Float.NaN;
        } else {
            max = d[numberOfRecords - 1];
        }
    }

    /**
     * size is represented by number of unique values.
     *
     * @param d float [] sorted in ascending order
     */
    void determineGroupSizes(float[] d) {
        if(cutoffs == null) {
            return;
        }
        groupSizes = new int[cutoffs.length];

        int cutoffPos = 0;
        for (int i = 0; i < d.length; i++) {
            if (Float.isNaN(d[i])) {
                continue;
            } else if (d[i] > cutoffs[cutoffPos]) {
                cutoffPos++;
            }
            if (i == 0 || d[i - 1] != d[i]) {
                groupSizes[cutoffPos]++;    //max cutoff == max value
            }
        }
    }

    /**
     * lower is better
     *
     * @param d
     * @return
     */
    double evaluateStdDev(float[] d) {
        if(Float.isNaN(max)) {
            return Double.NaN;
        }
        determineGroupSizes(d);

        float stdev = 0;
        float mean = numberOfUniqueValues / (float) groupSizes.length;
        for (int i = 0; i < groupSizes.length; i++) {
            stdev += Math.pow(groupSizes[i] - mean, 2) / (float) groupSizes.length;
        }

        stdev = (float) Math.sqrt(stdev);

        return stdev;
    }

    /**
     * save to a file as a type (filename extension).
     *
     * Option to scale down image size by discarding values/pixels
     * 
     * @param d float [] of raster data to have legend applied
     * @param width row width
     * @param filename output filename
     */
    void exportImage(float[] d, int width, String filename, int scaleDownBy) {
        try {
            /* make image */
            BufferedImage image = new BufferedImage(width / scaleDownBy, d.length / width / scaleDownBy,
                    BufferedImage.TYPE_INT_BGR);

            /* get bytes structure */
            int[] image_bytes = image.getRGB(0, 0, image.getWidth(), image.getHeight(),
                    null, 0, image.getWidth());

            //fill
            for (int i = 0; i < image_bytes.length; i++) {
                int x = i % (width / scaleDownBy);
                int y = i / (width / scaleDownBy);

                int dataX = x * scaleDownBy;
                int dataY = y * scaleDownBy;

                int dataI = dataX + dataY * width;

                image_bytes[i] = getColour(d[dataI]);
            }

            /* write back image bytes */
            image.setRGB(0, 0, image.getWidth(), image.getHeight(),
                    image_bytes, 0, image.getWidth());

            /* write image */
            String extension = filename.substring(filename.length() - 3);
            ImageIO.write(image, extension, new File(filename));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param d asc sorted float []
     * @return colour of d after applying cutoff's.
     */
    public int getColour(float d) {
        if (Float.isNaN(d)) {
            return 0xFFFFFFFF;
        }
        int pos = java.util.Arrays.binarySearch(cutoffs, d);
        if (pos < 0) {
            pos = (pos * -1) - 1;
        }
        if (pos >= cutoffs.length) {
            return 0xFFFFFFFF;
        } else {
            double upper = cutoffs[pos];
            double lower;
            if (pos == 0) {
                lower = min;
            } else {
                lower = cutoffs[pos - 1];
            }

            //translate value to 0-1 position between the colours
            double v = (d - lower) / (upper - lower);
            double vt = 1 - v;

            //there are groups+1 colours
            int red = (int) ((colours[pos] & 0x00FF0000) * vt + (colours[pos + 1] & 0x00FF0000) * v);
            int green = (int) ((colours[pos] & 0x0000FF00) * vt + (colours[pos + 1] & 0x0000FF00) * v);
            int blue = (int) ((colours[pos] & 0x00000FF) * vt + (colours[pos + 1] & 0x000000FF) * v);

            return (red & 0x00FF0000) | (green & 0x0000FF00) | (blue & 0x000000FF) | 0xFF000000;
        }
    }

    /**
     * colourize input between provided ranges.
     *
     * @param d value to colourize as float
     * @param min minimum of range as float
     * @param max maximum of range as float
     * @return colour of d scaled between min and max as int ARGB with A == 0xFF.
     *  Defaults to black.
     */
    public static int getColour(double d, double min, double max) {
        if (Double.isNaN(d) || d < min || d > max) {
            return 0xFF000000;
        }
        double range = max - min;
        double a = (d - min) / range;

        //10 colour steps
        int pos = (int) (a);  //fit 0 to 10
        if (pos == 10) {
            pos--;
        }
        double lower = (pos / 10.0) * range + min;
        double upper = ((pos + 1) / 10.0) * range + min;

        //translate value to 0-1 position between the colours
        double v = (d - lower) / (upper - lower);
        double vt = 1 - v;

        //there are groups+1 colours
        int red = (int) ((colours[pos] & 0x00FF0000) * vt + (colours[pos + 1] & 0x00FF0000) * v);
        int green = (int) ((colours[pos] & 0x0000FF00) * vt + (colours[pos + 1] & 0x0000FF00) * v);
        int blue = (int) ((colours[pos] & 0x00000FF) * vt + (colours[pos + 1] & 0x000000FF) * v);

        return (red & 0x00FF0000) | (green & 0x0000FF00) | (blue & 0x000000FF) | 0xFF000000;
    }

    public static int getLinearColour(double d, double min, double max, int startColour, int endColour) {
        //translate value to 0-1 position between the colours
        double v = (d - min) / (max - min);
        double vt = 1 - v;

        int red = (int) ((startColour & 0x00FF0000) * vt + (endColour & 0x00FF0000) * v);
        int green = (int) ((startColour & 0x0000FF00) * vt + (endColour & 0x0000FF00) * v);
        int blue = (int) ((startColour & 0x00000FF) * vt + (endColour & 0x000000FF) * v);

        return (red & 0x00FF0000) | (green & 0x0000FF00) | (blue & 0x000000FF) | 0xFF000000;
    }

    /**
     * get cutoff values as String
     *
     * includes group sizes if calculated
     *
     * @return String of cutoff values
     */
    public String getCutoffs() {
        StringBuffer sb = new StringBuffer();
        System.out.println(getTypeName());
        for (int i = 0; i < cutoffs.length; i++) {
            if (groupSizes != null) {
                sb.append(String.valueOf(cutoffs[i])).append("\t").append(String.valueOf(groupSizes[i])).append("\n");
            } else {
                sb.append(String.valueOf(cutoffs[i])).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * write the cutoff points to a file as text
     *
     * @param filename
     */
    void exportLegend(String filename) {
        try {
            FileWriter fw = new FileWriter(filename);
            fw.append(getCutoffs());
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * get cutoff's
     * @return cutoff upper segment values as float[] (missing min value)
     */
    public float[] getCutoffFloats() {
        return cutoffs;
    }

    /**
     * 
     * @return float[] of [min, max] 
     */
    public float[] getMinMax() {
        float [] f = {min, max};
        return f;
    }
}