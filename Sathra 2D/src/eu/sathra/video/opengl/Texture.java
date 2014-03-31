package eu.sathra.video.opengl;

import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import eu.sathra.io.annotations.Deserialize;
import eu.sathra.util.Log;

public class Texture {

	private int mHandle = 0;
	private int mWidth;
	private int mHeight;

	public Texture() {
		mHandle = 0;
		mWidth = 0;
		mHeight = 0;
	}
	
	public Texture(Texture other) {
		this(other.getHandle(), other.getWidth(), other.getHeight());
	}
	
	public Texture(int handle, int width, int height)
	{
		mHandle = handle;
		mWidth = width;
		mHeight = height;
	}
	
	public Texture(Bitmap bitmap)
	{
		load(bitmap);
	}
	
	@Deserialize({ Deserialize.NULL, "filename" } )
	public void load(Context context, String filename) throws IOException {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;	// No pre-scaling

		Log.debug("Loading texture from file: " + filename);
		
		final Bitmap bitmap = 
				BitmapFactory.decodeStream(context.getAssets().open(filename));
		
		load(bitmap);
	}
	
	public void load(Context context, int resourceId) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;	// No pre-scaling
		
		final Bitmap bitmap = 
				BitmapFactory.decodeResource(context.getResources(), resourceId, options);
		
		load(bitmap);
	}
	
	public void load(Bitmap bitmap) {
		final int[] textureHandle = new int[1];

		GLES20.glGenTextures(1, textureHandle, 0);
		mHandle = textureHandle[0];
		
		if (mHandle != 0)
		{
			// Bind to the texture in OpenGL
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

			//GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
			
			// Set filtering
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MIN_FILTER,
					GLES20.GL_LINEAR_MIPMAP_LINEAR);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
//			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
//			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			
			// Load the bitmap into the bound texture.
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
			GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
			// Recycle the bitmap, since its data has been loaded into OpenGL.
			bitmap.recycle();	// TODO: reconsider	
			
			mWidth = bitmap.getWidth();
			mHeight = bitmap.getHeight();
			
			//mPlane = new Plane(bitmap.getWidth(), bitmap.getHeight());
		} else {
			throw new RuntimeException("Error loading texture.");
		}
		
		Log.debug("Texture loaded [" + mWidth + "x" + mHeight +"]");
	}
	
	public int getHandle() {
		return mHandle;
	}
	
	public int getWidth() {
		return mWidth;
	}
	
	public int getHeight() {
		return mHeight;
	}

}
