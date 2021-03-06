package com.android.nearbylocation.modelPlaceDetails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by MR. Android on 07-10-2017.
 */

public class Southwest {
    @SerializedName("lat")
    @Expose
    private Double lat;
    @SerializedName("lng")
    @Expose
    private Double lng;

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}
