package ca.cineti.android;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 *
 */
public class ImageAdapter extends BaseAdapter {
	private Context mContext;
	// references to our images:
    private URL[] mThumbnailURIs;
	
	/**
	 * Construct a new ImageAdapter.
	 */
	public ImageAdapter(Context c) {
        this.mContext = c;
        // Hardcoded image URLs for prototyping.
        this.mThumbnailURIs = new URL[14];
        try {
			this.mThumbnailURIs[0] = new URL("http://cineti.ca/poster/19501_thumb.jpg");
			this.mThumbnailURIs[1] = new URL("http://cineti.ca/poster/11913_thumb.jpg");
			this.mThumbnailURIs[2] = new URL("http://cineti.ca/poster/35587_thumb.jpg");
			this.mThumbnailURIs[3] = new URL("http://cineti.ca/poster/36971_thumb.jpg");
			this.mThumbnailURIs[4] = new URL("http://cineti.ca/poster/37468_thumb.jpg");
			this.mThumbnailURIs[5] = new URL("http://cineti.ca/poster/34389_thumb.jpg");
			this.mThumbnailURIs[6] = new URL("http://cineti.ca/poster/36705_thumb.jpg");
			this.mThumbnailURIs[7] = new URL("http://cineti.ca/poster/35774_thumb.jpg");
			this.mThumbnailURIs[8] = new URL("http://cineti.ca/poster/22422_thumb.jpg");
			this.mThumbnailURIs[9] = new URL("http://cineti.ca/poster/32781_thumb.jpg");
			this.mThumbnailURIs[10] = new URL("http://cineti.ca/poster/21699_thumb.jpg");
			this.mThumbnailURIs[11] = new URL("http://cineti.ca/poster/34687_thumb.jpg");
			this.mThumbnailURIs[12] = new URL("http://cineti.ca/poster/35711_thumb.jpg");
			this.mThumbnailURIs[13] = new URL("http://cineti.ca/poster/27194_thumb.jpg");
        } catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	public int getCount() {
		return this.mThumbnailURIs.length;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * Create a new ImageView for each item referenced by the Adapter
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialise some attributes
            imageView = new ImageView(this.mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(95, 140));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }
        
        InputStream is;
		try {
			is = this.mThumbnailURIs[position].openStream();
			imageView.setImageBitmap(BitmapFactory.decodeStream(is));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return imageView;
	}

}
