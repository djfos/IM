package djfos.im.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import djfos.im.BR;


public class Config extends BaseObservable {
    private int threshold;
    private String uri;
    @Bindable
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
        notifyPropertyChanged(BR.uri);

    }

    @Bindable
    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
        notifyPropertyChanged(BR.threshold);
    }
}


