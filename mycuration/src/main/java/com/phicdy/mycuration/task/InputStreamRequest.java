package com.phicdy.mycuration.task;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

class InputStreamRequest extends Request<InputStream> {

	private final Listener<InputStream> mListener;

	private InputStreamRequest(int method, String url,
							   Listener<InputStream> listener, ErrorListener errorListener) {
		super(method, url, errorListener);
		mListener = listener;
	}

	InputStreamRequest(String url, Listener<InputStream> listener,
					   ErrorListener errorListener) {
		this(Method.GET, url, listener, errorListener);
	}

	@Override
	protected void deliverResponse(InputStream response) {
		mListener.onResponse(response);
	}

	@Override
	protected Response<InputStream> parseNetworkResponse(
			NetworkResponse response) {
		String contentType = response.headers.get("Content-Type");
		String GZIP_CONTENT_TYPE = "application/gzip;charset=UTF-8";
		InputStream is;
		if (contentType.equalsIgnoreCase(GZIP_CONTENT_TYPE)) {
			try {
				is = new GZIPInputStream(
                        new BufferedInputStream(new ByteArrayInputStream(response.data)));
			} catch (IOException e) {
				e.printStackTrace();
				return Response.error(new VolleyError(response));
			}
		}else {
			is = new ByteArrayInputStream(response.data);
		}
		return Response.success(is,
				HttpHeaderParser.parseCacheHeaders(response));
	}
}