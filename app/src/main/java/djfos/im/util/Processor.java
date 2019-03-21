package djfos.im.util;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import djfos.im.model.Config;

import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

public class Processor {
    static final String TAG = "Processor";
    private final ViewSwitcher viewSwitcher;
    private Transformation transformation;
    private RequestOptions options;
    private GlideRequest<Drawable> requestBuilder;
    private String uri = "http://www.laverocks.co.uk/gilslandmag/desktops/tup_800.jpg";
    private boolean first = true;
    RequestListener<Drawable> ls;

    public Processor(Fragment fragment, ViewSwitcher vs, Config config) {
        viewSwitcher = vs;
        ls = getListener();
        options = new RequestOptions();
        transformation = new Transformation(config);
        options.transform(transformation);
        requestBuilder = GlideApp.with(fragment)
                .load(Uri.parse(uri))
                .skipMemoryCache(true);

    }
    public void update() {
        if (first) {
            ImageView current = (ImageView) viewSwitcher.getCurrentView();
            requestBuilder
                    .apply(options)
                    .into(current);
            first = false;
        } else {
            ImageView current = (ImageView) viewSwitcher.getCurrentView();
            ImageView next = (ImageView) viewSwitcher.getNextView();
            options.override(current.getWidth(), current.getHeight());
            requestBuilder
                    .apply(options)
                    .addListener(ls)
                    .into(next);
        }

    }


    private RequestListener<Drawable> getListener() {
        return new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                viewSwitcher.showNext();
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                viewSwitcher.showNext();
                return false;
            }
        };
    }
}
