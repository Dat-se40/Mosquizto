package com.example.mosquizto.Util;

import android.text.TextUtils;
import android.widget.ImageView;

import com.example.mosquizto.R;
import com.squareup.picasso.Picasso;

public final class AvatarImageHelper {

    private AvatarImageHelper() {
    }

    public static void loadInto(ImageView imageView, String imgUri) {
        if (imageView == null) {
            return;
        }
        if (!TextUtils.isEmpty(imgUri)) {
            Picasso.get()
                    .load(imgUri)
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_default_avatar);
        }
    }
}
