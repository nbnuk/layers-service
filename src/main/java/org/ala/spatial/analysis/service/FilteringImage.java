package org.ala.spatial.analysis.service;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Comparator;

import javax.imageio.ImageIO;

import org.ala.spatial.util.TabulationSettings;
import org.ala.spatial.util.Tile;

/**
 * for generating layer images from layer filter specifications
 * 
 * @author adam
 *
 */
public class FilteringImage implements Serializable {

	static final long serialVersionUID = 6111740847515567882L;

	
    /**
     * todo: dynamic width
     */
    final int WIDTH = 252;
    
    /**
     * todo: dynamic height
     */
    final int HEIGHT = 210;
    
    /**
     * value of transparent image cell
     */
    final int TRANSPARENT = 0x00000000;
    
    /**
     * value of edge of transparent area
     */
    int edgeColour;
    
    /**
     * hidden colour for current filtered image
     */
    int hidden_colour;
    
    /**
     * filename for current filtered image
     */
    String filename;
    
    /**
     * image bytes for alteration for current filtered image
     */
    int[] image_bytes;
    
    /**
     * current image 
     */
    BufferedImage image;
        
    /**
     * constructor for new, fully hidden, layer/image
     * 
     * TODO: dynamic width, height and resolution
     * 
     * @param filename_ full path to filename where image can be saved
     * @param hidden_colour_ value for the hidden colour in use
     */
    public FilteringImage(String filename_, int hidden_colour_) {
    	TabulationSettings.load();
    	
        filename = filename_;
        hidden_colour = hidden_colour_;

        int i;
     
        /* make image */        
        image = new BufferedImage(WIDTH, HEIGHT,
                BufferedImage.TYPE_4BYTE_ABGR);        
        
        /* get bytes structure */
        image_bytes = image.getRGB(0, 0, image.getWidth(), image.getHeight(),
                null, 0, image.getWidth());

        /* init with hidden colour */
        for (i = 0; i < image_bytes.length; i++) {
            image_bytes[i] = hidden_colour; 
        }  
        
        /* setup edge colour as darker version of hidden_colour */
        int r = (hidden_colour & 0x00ff0000);
        int g = (hidden_colour & 0x0000ff00);
        int b = (hidden_colour & 0x000000ff);
        
        edgeColour = 0xff000000 | ((r/3) & 0x00ff0000) | ((b/3) & 0x0000ff00) | ((b/3) & 0x000000ff);
    }


    /**
     * saves the filtered image to the filename from
     * constructor
     */
    public void writeImage() {
        try {
        	/* edge detect before writeback 
        	 * - if not transparent and any neighbor (x8) is
        	 * 
        	 * TODO: decide if image is nice or not
        	 */
        	int i, j, pos;
        	int n1, n2, n3;
        	int n4,     n5;
        	int n6, n7, n8;
        	pos = 0;
        	for (i = 0; i < HEIGHT; i++) {
        		for (j = 0; j < WIDTH; j++) {
        			if (image_bytes[pos] != TRANSPARENT) {
        				/* check neighbours */
    					n1 = pos - WIDTH - 1;
    					n2 = pos - WIDTH;
    					n3 = pos - WIDTH + 1;
    					n4 = pos - 1;
    					n5 = pos + 1;
    					n6 = pos + WIDTH - 1;
    					n7 = pos + WIDTH;
    					n8 = pos + WIDTH + 1;
    					
    					if (j == 0) {
    						n1 = -1;
    						n4 = -1;
    						n6 = -1;
    					} else if( j == WIDTH - 1) {
    						n3 = -1;
    						n5 = -1;
    						n8 = -1;
    					}
    					//n1, n2, n3 already < 0 if (i == 0)
    					if (i == HEIGHT - 1) {
    						n6 = -1;
    						n7 = -1;
    						n8 = -1;
    					}
    					
    					if ( (n1 >= 0 && image_bytes[n1] == TRANSPARENT)
    						|| (n2 >= 0 && image_bytes[n2] == TRANSPARENT)
    						|| (n3 >= 0 && image_bytes[n3] == TRANSPARENT)
    						|| (n4 >= 0 && image_bytes[n4] == TRANSPARENT)
    						|| (n5 >= 0 && image_bytes[n5] == TRANSPARENT)
    						|| (n6 >= 0 && image_bytes[n6] == TRANSPARENT)
    						|| (n7 >= 0 && image_bytes[n7] == TRANSPARENT)
    						|| (n8 >= 0 && image_bytes[n8] == TRANSPARENT)
    						){
    						image_bytes[pos] = edgeColour;
    					}
        			}
        			pos++;
        		}
        	}
        	
        	/* write back image bytes */
        	image.setRGB(0, 0, image.getWidth(), image.getHeight(),
                     image_bytes, 0, image.getWidth());       	
        	 
        	/* write image */
            ImageIO.write(image, "png",
                    new File(filename));

        } catch (IOException e) {

        }
    }
   
    /**
     * applies a continous/grid/environmental filter to imagebytes 
     * @param layer_name layer name for source data (grid file/environmental)
     * @param new_min_ minimum filter value
     * @param new_max_ maximum filter value
     */
    public void applyFilter(String layer_name, double new_min_, double new_max_) {    	
    	Tile [] data = null;
    	
        /* load data */       
        try {            
            FileInputStream fis = new FileInputStream(
                    TabulationSettings.index_path
                    + "SPL_IMG_T_" + layer_name + ".dat");
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis);            
            data = (Tile []) ois.readObject();
           
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
       
        /* seeking */
        Tile t = new Tile((float)new_min_,0);
        int start = java.util.Arrays.binarySearch(data,t,
            	new Comparator<Tile>(){
    				public int compare(Tile i1, Tile i2) {
    					if (i1.value_ < i2.value_) {
    						return -1;
    					} else if (i1.value_ > i2.value_) {
    						return 1;
    					}
    					return 0;
    				}
    			});

        if (start < 0) {
        	start = start * -1 -1;
        }
        
        t.value_ = (float) new_max_;
        int end = java.util.Arrays.binarySearch(data,t,
            	new Comparator<Tile>(){
    				public int compare(Tile i1, Tile i2){
    					if(i1.value_ < i2.value_){
    						return -1;
    					}else if(i1.value_ > i2.value_){
    						return 1;
    					}
    					return 0;
    				}
    			});
  
        if (end < 0) {
        	end = end * -1 -1;
        }
        if (end >= data.length) {
        	end = data.length-1;
        }
        
        int i;
        for (i = start; i <= end; i++) {
        	image_bytes[data[i].pos_] = TRANSPARENT;
        } 
    }
    
    /**
     * applies a catagorical/shapefile/contextual filter to imagebytes 
     * 
     * note: hides nothing so cannot be used together with the
     * other applyFilter()
     * 
     * @param layer_name layer name for source data (shapefile/contextual)
     * @param show_list list of indexes to unhide 
     */
    public void applyFilter(String layer_name, int [] show_list) {
        
        Tile [] data = null;
        Boolean has_index;	//checking that index is present
        int [] index = null;
        
        /* load data */
        try {            
            FileInputStream fis = new FileInputStream(
                    TabulationSettings.index_path
                    + "SPL_IMG_T_" + layer_name + ".dat");
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis);            
            data = (Tile []) ois.readObject();
            has_index = (Boolean) ois.readObject();
            if (has_index) {
            	index = (int[]) ois.readObject();
            } else {
            	//do nothing, layer is not contextual
            	return;
            }
            ois.close();
        } catch (Exception e) {
            
        }
        
        java.util.Arrays.sort(show_list);        
        
        int i,j,end;
        
        /* make imagebytes transparent */
        for (i = 0; i < show_list.length; i++ ) {        	
        	end = index[show_list[i]+1];    
        	for (j = index[show_list[i]]; j < end; j++) {
        		image_bytes[data[j].pos_] = TRANSPARENT;
        	}
        }
    }
}