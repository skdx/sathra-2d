package eu.sathra.io;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.NotSerializableException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.dyn4j.dynamics.World;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.AssetFileDescriptor;
import android.graphics.Rect;
import android.view.animation.Interpolator;
import eu.sathra.io.adapters.InterpolatorAdapter;
import eu.sathra.io.adapters.RectAdapter;
import eu.sathra.io.adapters.TypeAdapter;
import eu.sathra.io.adapters.WorldAdapter;
import eu.sathra.io.annotations.Defaults;
import eu.sathra.io.annotations.Deserialize;
import eu.sathra.io.preprocessors.Preprocessor;

public class IO {

	private static final String CLASS_PARAM = "class";

	private static IO sInstance = null;

	private Map<Type, TypeAdapter<?>> mAdapters = new HashMap<Type, TypeAdapter<?>>();
	private Map<String, Preprocessor> mPreprocessors = new HashMap<String, Preprocessor>();

	public static IO getInstance() {
		if (sInstance == null)
			sInstance = new IO();

		return sInstance;
	}

	private IO() {
		mAdapters.put(Rect.class, new RectAdapter());
		mAdapters.put(Interpolator.class, new InterpolatorAdapter());
		mAdapters.put(World.class, new WorldAdapter());
	}

	public <T> void registerAdapter(Class<? extends T> clazz, TypeAdapter<? extends T> adapter) {
		mAdapters.put(clazz, adapter);
	}

	public void registerPreprocessor(String var, Preprocessor preprocessor) {
		mPreprocessors.put(var, preprocessor);
	}

	public <T> T load(AssetFileDescriptor afd, Class<T> clazz) throws Exception {

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				afd.createInputStream(), "UTF-8"));

		// do reading, usually loop until end of file reading
		String line;
		StringBuilder myBuilder = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			myBuilder.append(line);
		}

		reader.close();
		afd.close();

		return load(myBuilder.toString(), clazz);
	}

	public <T> T load(String json, Class<T> clazz) throws Exception {
		return load(new JSONObject(json), clazz);
	}

	@SuppressWarnings("unchecked")
	public <T> T load(JSONObject jObj, Class<T> clazz) throws Exception {

		/*
		 * In case if parameter clazz is interface or has subclasses, it is
		 * necessary to explicitly declare which class we want to deserialize.
		 * It is done by declaring JSon element "class" with full classname as a
		 * value. Example: "my_object": { "class":"eu.sathra.MyClass", ... }
		 */
		if (jObj.has(CLASS_PARAM))
			clazz = (Class<T>) Class.forName(jObj.getString(CLASS_PARAM));

		for (Constructor<?> myConstructor : clazz.getConstructors()) {
			Deserialize myDeserialize = myConstructor
					.getAnnotation(Deserialize.class);

			if (myDeserialize != null) {

				Defaults myDefaults = myConstructor
						.getAnnotation(Defaults.class);

				Object[] params = parseParams(
						myConstructor.getParameterTypes(), myDeserialize,
						myDefaults, jObj);

				return (T) myConstructor.newInstance(params);
			}

		}

		for (Method myMethod : clazz.getMethods()) {
			Deserialize myDeserialize = myMethod
					.getAnnotation(Deserialize.class);

			if (myDeserialize != null) {
				Defaults myDefaults = myMethod.getAnnotation(Defaults.class);

				Object[] params = parseParams(myMethod.getParameterTypes(),
						myDeserialize, myDefaults, jObj);

				T instance = clazz.newInstance();
				myMethod.invoke(instance, params);

				return instance;
			}
		}

		throw new NotSerializableException("A class of type "
				+ clazz.getCanonicalName() + " is not serializable.");
	}

	private Object[] parseParams(Class<?>[] paramTypes,
			Deserialize deserialize, Defaults defaults, JSONObject jObj)
			throws ArrayIndexOutOfBoundsException, IllegalArgumentException,
			Exception {

		Object[] parsed = new Object[paramTypes.length];
		String paramNames[] = deserialize.value();
		String defaultValues[] = defaults == null ? new String[paramTypes.length]
				: defaults.value();

		for (int c = 0; c < paramNames.length; ++c) {
			String param = paramNames[c];
			String defaultValue = defaultValues[c];
			Class<?> myType = paramTypes[c];

			
			
			if (!jObj.has(param)) {
				/*
				 * JSON element not found, that means we have to use default
				 * value
				 */
				jObj.put(param, defaultValue == null ? JSONObject.NULL
						: defaultValue);
			}
			
			//Log.debug("Deserializing: " + myType + " " + jObj.get(param));

			parsed[c] = getValue(jObj, param, myType);
		}

		return parsed;
	}
	
	private Object getValue(JSONArray array, Class<?> clazz) throws Exception {
		
		Object parsedArray = Array.newInstance(clazz, array.length());

		for(int c=0;c<array.length(); ++c) {
			if ((clazz.equals(String.class) || clazz.isPrimitive()) && !clazz.equals(float.class)) {
				Array.set(parsedArray, c, array.get(c));
			} else if(clazz.equals(float.class)) {
				Array.set(parsedArray, c, (float)array.getDouble(c));
			} else if(clazz.isArray()) {
				// nested array
				Array.set(parsedArray, c, getValue(array.getJSONArray(c), float.class)); // TODO
			} else {
				Array.set(parsedArray, c, load(array.getJSONObject(c), clazz));
			} 
		}
		
		return parsedArray;
	}

	// TODO: this method need refactoring
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object getValue(JSONObject jObj, String param, Class<?> clazz)
			throws ArrayIndexOutOfBoundsException, IllegalArgumentException,
			Exception {

		if (clazz.equals(String.class)) {
			return jObj.getString(param);
		} else if (clazz.equals(int.class)) {
			return jObj.getInt(param);
		} else if (clazz.equals(long.class)) {
			return jObj.getLong(param);
		} else if (clazz.equals(float.class)) {
			return (float) jObj.getDouble(param);
		} else if (clazz.equals(double.class)) {
			return jObj.getDouble(param);
		} else if (clazz.equals(boolean.class)) {
			return jObj.getBoolean(param);
		} else if (mAdapters.containsKey(clazz)) {
			return mAdapters.get(clazz).load(param, jObj);
		} else if (clazz.isEnum()) {
			return Enum.valueOf((Class<? extends Enum>) clazz,
					jObj.getString(param));
		} else if (clazz.isArray()) {
			if (isNullOrEmpty(jObj, param))
				return null;

//			JSONArray jArray = jObj.getJSONArray(param);
//			Object parsedArray = Array.newInstance(clazz.getComponentType(),
//					jArray.length());
//
//			Class<?> componentType = clazz.getComponentType();
//
//			for (int i = 0; i < jArray.length(); ++i) {
//
//				if (componentType.isPrimitive()) {
//					Array.set(parsedArray, i, jArray.get(i));
//				} else if (componentType.isArray()) {
//					JSONArray nestedArray = jArray.getJSONArray(i);
//
//					
//					
//					Array.set(
//							parsedArray,
//							i,
//							componentType.isPrimitive() ? jArray.get(i) : load(
//									jArray.getJSONObject(i),
//									clazz.getComponentType()));
//				} else {
//					Array.set(
//							parsedArray,
//							i,
//							load(jArray.getJSONObject(i),
//									clazz.getComponentType()));
//				}
				return getValue(jObj.getJSONArray(param), clazz.getComponentType());
			//}

			//return parsedArray;
		} else {
			return isNullOrEmpty(jObj, param) ? null : load(
					jObj.getJSONObject(param), clazz);
		}
	}

	// TODO: rewrite this method
	private boolean isNullOrEmpty(JSONObject jObj, String param) {

		try {
			if (jObj.has(param))
				return jObj.getString(param).equals(Deserialize.NULL)
						|| jObj.isNull(param);
			else
				return true;
		} catch (JSONException e) {
			return false;
		}
	}

}
