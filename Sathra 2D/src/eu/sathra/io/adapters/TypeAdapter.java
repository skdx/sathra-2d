package eu.sathra.io.adapters;

import org.json.JSONException;
import org.json.JSONObject;

import eu.sathra.io.annotations.Deserialize;

public interface TypeAdapter<T> {
	T load(String param, JSONObject parent) throws JSONException;
}
