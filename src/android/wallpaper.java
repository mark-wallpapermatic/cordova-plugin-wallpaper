package com.wallpapermatic.cordova.plugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import org.apache.cordova.PluginResult;
import java.io.IOException;
import java.io.InputStream;

public class wallpaper extends CordovaPlugin
{
	public Context context = null;
	private static final boolean IS_AT_LEAST_LOLLIPOP = Build.VERSION.SDK_INT >= 21;

	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException
	{
		context = IS_AT_LEAST_LOLLIPOP ? cordova.getActivity().getWindow().getContext() : cordova.getActivity().getApplicationContext();

		String imgSrc = "";
		Boolean base64 = false;

		if (action.equals("start"))
		{
			imgSrc = args.getString(0);
			base64 = args.getBoolean(1);
			this.echo(imgSrc, base64, context);

			PluginResult pr = new PluginResult(PluginResult.Status.OK);
			pr.setKeepCallback(true);
			callbackContext.sendPluginResult(pr);
			return true;
		}
		callbackContext.error("Set wallpaper is not a supported.");
        	return false;
	}

	public void echo(String image, Boolean base64, Context context)
	{
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(metrics);
	    int phoneHeight = metrics.heightPixels;
	    int phoneWidth = metrics.widthPixels;

		try
		{
			AssetManager assetManager = context.getAssets();
			Bitmap bitmap;
			if(base64) //Base64 encoded
			{
				byte[] decoded = android.util.Base64.decode(image, android.util.Base64.DEFAULT);
				bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
			}
			else //normal path
			{
				InputStream instr = assetManager.open("www/" + image);
				bitmap = BitmapFactory.decodeStream(instr);
			}

			//Bitmap adjusted = returnBitmap(bitmap, phoneWidth, phoneHeight);
			Bitmap adjusted = centerCropWallpaper(bitmap, phoneWidth, phoneHeight);

			WallpaperManager myWallpaperManager = WallpaperManager.getInstance(context);
			myWallpaperManager.setBitmap(adjusted);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Bitmap centerCropWallpaper(Bitmap wallpaper, int desiredWidth, int desiredHeight)
	{
	    float scale = (float) desiredHeight / wallpaper.getHeight();
	    int scaledWidth = (int) (scale * wallpaper.getWidth());
	    int imageCenterWidth = scaledWidth / 2;
	    int widthToCut = imageCenterWidth - desiredWidth / 2;
	    int leftWidth = scaledWidth - widthToCut;
	    Bitmap scaledWallpaper = Bitmap.createScaledBitmap(wallpaper, scaledWidth, desiredHeight, false);
	    Bitmap croppedWallpaper = Bitmap.createBitmap(
	        scaledWallpaper,
	        widthToCut,
	        0,
	        leftWidth,
	        desiredHeight
	    );
	    return croppedWallpaper;
	}

	private Bitmap returnBitmap(Bitmap originalImage, int width, int height)
	{
		Bitmap background = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);

		float originalWidth = originalImage.getWidth();
		float originalHeight = originalImage.getHeight();

		Canvas canvas = new Canvas(background);

		float scale = width / originalWidth;

		float xTranslation = 0.0f;
		float yTranslation = (height - originalHeight * scale) / 2.0f;

		Matrix transformation = new Matrix();
		transformation.postTranslate(xTranslation, yTranslation);
		transformation.preScale(scale, scale);

		Paint paint = new Paint();
		paint.setFilterBitmap(true);

		canvas.drawBitmap(originalImage, transformation, paint);

		return background;
	}

	private Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth)
	{
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        RectF targetRect = new RectF(left, top, left + scaledWidth, top
                + scaledHeight);//from ww w  .j a va 2s. co m

        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight,
                source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);

        return dest;
    }
	// private Bitmap returnBitmap(Bitmap originalImage, int width, int height)
	// {
	//     //Bitmap background = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
	//
	//     float originalWidth = originalImage.getWidth();
	//     float originalHeight = originalImage.getHeight();
	//
	//     // Canvas canvas = new Canvas(background);
	// 	//
	//     // float scale = width / originalWidth;
	// 	//
	//     // float xTranslation = 0.0f;
	//     // float yTranslation = (height - originalHeight * scale) / 2.0f;
	//
	//     Matrix transformation = new Matrix();
	//     //transformation.postTranslate(xTranslation, yTranslation);
	//     //transformation.preScale(scale, scale);
	// 	RectF drawableRect = new RectF(0, 0, originalWidth, originalHeight);
	// 	RectF viewRect = new RectF(0, 0, width, height);
	// 	transformation.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
	//
	//     // Paint paint = new Paint();
	//     // paint.setFilterBitmap(true);
	// 	//
	//     // canvas.drawBitmap(originalImage, transformation, paint);
	//
	// 	Bitmap background = Bitmap.createBitmap(originalImage, 0, 0, originalImage.getWidth(), originalImage.getHeight(), transformation, true);
	//
	//     return background;
	// }
}
