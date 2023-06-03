package com.imcorp.animeprog.Default;

import android.graphics.Bitmap;

public interface LoadImageEvents {
    public void onSuccess(Bitmap bitmap);
    public boolean onFail(Exception exception);
}
